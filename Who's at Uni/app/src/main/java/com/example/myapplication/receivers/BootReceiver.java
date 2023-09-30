package com.example.myapplication.receivers;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.MainActivity;
import com.example.myapplication.models.FirebaseWrapper;
import com.example.myapplication.models.MyWorker;
import com.example.myapplication.models.PermissionManager;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = BootReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {


        if(new FirebaseWrapper.Auth().isAuthenticated()) { // check if a user is authenticated

            // check permits

            for(String permission : PermissionManager.NEEDED_PERMISSION ){

                if(ContextCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_DENIED){

                    Log.d(TAG, "Permission " + permission + " is missing");
                    return; // Mi manca un permesso, non posso fare nulla

                }

            }

            Log.d(TAG, "Boot Receiver TRIGGERATO");

            // launch a OneTimeWorkRequest 5 minutes after boot if everything is ok
            OneTimeWorkRequest worker = new OneTimeWorkRequest.Builder(MyWorker.class)
                    .setInitialDelay(10, TimeUnit.MINUTES)
                    .build();

            PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(MyWorker.class,
                    25, TimeUnit.MINUTES,
                    5, TimeUnit.MINUTES)
                    .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "updateUserStatusUNI",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork
            );

            WorkManager.getInstance(context).enqueue(worker);

        }

    }
}