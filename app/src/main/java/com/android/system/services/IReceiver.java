package com.android.system.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class IReceiver extends BroadcastReceiver {
    String PrivateDnsHostname;
    String C_PrivateDnsHostname;
    String PrivateDnsMode;
    Boolean isFromWorkManager;

    final String PRIVATE_DNS_MODE_DESIRED = "hostname";

    WorkManager mWorkManager;
    PeriodicWorkRequest DnsRequest;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")
                == PackageManager.PERMISSION_GRANTED) {
            try {
                PrivateDnsMode = Settings.Global.getString(context.getContentResolver(), "private_dns_mode");
                C_PrivateDnsHostname = Settings.Global.getString(context.getContentResolver(), "private_dns_specifier");
            } catch (Exception e) {
                Log.e("SetDnsBroadcast", e.toString());
                throw new RuntimeException(e);
            }
        } else {
            Log.e("SetDnsBroadcast", "Error: Please grant android.permission.WRITE_SECURE_SETTINGS through ADB first!");
            return;
        }
        isFromWorkManager = intent.getBooleanExtra("FromWM", false);
        SharedPreferences sharedPref = context.getSharedPreferences("DnsPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (intent.getStringExtra("PrivateDnsHostname") != null) {
            PrivateDnsHostname = intent.getStringExtra("PrivateDnsHostname");
            editor.putString("private_dns_hostname", PrivateDnsHostname);
            editor.apply();
            Settings.Global.putString(context.getContentResolver(), "private_dns_specifier", PrivateDnsHostname);
            if (!Objects.equals(PrivateDnsMode, PRIVATE_DNS_MODE_DESIRED)) {
                Settings.Global.putString(context.getContentResolver(), "private_dns_mode", PRIVATE_DNS_MODE_DESIRED);
            }
            if (!isFromWorkManager) {
                createRepeatingBroadcast(context);
            }
            Log.d("SetDnsBroadcast", "Given hostname: " + PrivateDnsHostname);
            Log.d("SetDnsBroadcast", "Current hostname: " + C_PrivateDnsHostname);
        } else if (intent.getStringExtra("PrivateDnsHostname") == null && sharedPref.getString("private_dns_hostname", null) == null) {
            Log.e("SetDnsBroadcast", "Failed. Please provide a Private DNS hostname first!");
        } else {
            PrivateDnsHostname = sharedPref.getString("private_dns_hostname", null);
            Settings.Global.putString(context.getContentResolver(), "private_dns_specifier", PrivateDnsHostname);
            if (!Objects.equals(PrivateDnsMode, PRIVATE_DNS_MODE_DESIRED)) {
                Settings.Global.putString(context.getContentResolver(), "private_dns_mode", PRIVATE_DNS_MODE_DESIRED);
            }
            if (!isFromWorkManager) {
                createRepeatingBroadcast(context);
            }
            Log.d("SetDnsBroadcast", "Given hostname: " + PrivateDnsHostname);
            Log.d("SetDnsBroadcast", "Current hostname: " + C_PrivateDnsHostname);
        }
    }

    private void createRepeatingBroadcast(Context context) {
        mWorkManager = WorkManager.getInstance(context);
        DnsRequest = new PeriodicWorkRequest.Builder(IWorker.class, 60, TimeUnit.SECONDS).build();
        mWorkManager.enqueue(DnsRequest);
    }
}
