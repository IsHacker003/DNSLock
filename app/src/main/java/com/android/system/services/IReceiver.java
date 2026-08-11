package com.android.system.services;

import static android.provider.Settings.Global.getString;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Calendar;

public class IReceiver extends BroadcastReceiver {
    String PrivateDnsHostname;
    String C_PrivateDnsHostname;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")
                == PackageManager.PERMISSION_GRANTED) {
            try {
                C_PrivateDnsHostname = getString(context.getContentResolver(), "private_dns_specifier");
            }
            catch (Exception e) {
                Log.e("SetDnsBroadcast", e.toString());
                throw new RuntimeException(e);
            }
        }
        else {
            Log.e("SetDnsBroadcast", "Error: Please grant android.permission.WRITE_SECURE_SETTINGS through ADB first!");
            throw new RuntimeException();
        }
        SharedPreferences sharedPref = context.getSharedPreferences("DnsPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (intent.getStringExtra("PrivateDnsHostname") != null) {
            PrivateDnsHostname = intent.getStringExtra("PrivateDnsHostname");
            editor.putString("private_dns_hostname", PrivateDnsHostname);
            editor.apply();
            createRepeatingBroadcast(context);
            Log.d("SetDnsBroadcast", "Given hostname: " + PrivateDnsHostname);
            Log.d("SetDnsBroadcast", "Current hostname: " + C_PrivateDnsHostname);
        } else if (intent.getStringExtra("PrivateDnsHostname") == null && sharedPref.getString("private_dns_hostname", null) == null) {
            Log.e("SetDnsBroadcast", "Failed. Please provide a Private DNS hostname first!");
        } else {
            PrivateDnsHostname = sharedPref.getString("private_dns_hostname", null);
            createRepeatingBroadcast(context);
            Log.d("SetDnsBroadcast", "Given hostname: " + PrivateDnsHostname);
            Log.d("SetDnsBroadcast", "Current hostname: " + C_PrivateDnsHostname);
        }
    }
    private void createRepeatingBroadcast(Context context) {
        PendingIntent service = null;
        Intent intentForService = new Intent(context.getApplicationContext(), IReceiver.class);
        intentForService.setAction("android.intent.action.MAIN");
        final AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        final Calendar time = Calendar.getInstance();
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        service = PendingIntent.getBroadcast(context, 0,
                intentForService, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setRepeating(AlarmManager.RTC, time.getTime()
                .getTime(), 60000, service);
    }

    private String executeCmd(String Cmd) {
        StringBuffer output = null;
        String sOutput;
        if (Cmd != null) {
            try {
                Process process = Runtime.getRuntime().exec(Cmd);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                int read;
                char[] buffer = new char[4096];
                output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();
                process.waitFor();
                sOutput = output.toString().trim();
            } catch (Exception e) {
                Log.e("SetDnsBroadcast", e.toString());
                throw new RuntimeException(e);
            }
        } else {
            sOutput = "";
        }
        return sOutput;
    }
}
