package com.android.system.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class IWorker extends Worker {

    public IWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context c = this.getApplicationContext();
        try {
            Intent intentForService = new Intent(c, IReceiver.class);
            intentForService.setAction("android.intent.action.MAIN");
            intentForService.putExtra("FromWM", true);
            c.sendBroadcast(intentForService);
            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
