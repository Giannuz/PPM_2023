package com.example.myapplication.models;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import com.example.myapplication.Manifest;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class PermissionManager {

    // VARIABILI ----------------------------------------------------------------------------------

    // array di permessi che mi servono
    public final static String[] NEEDED_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final static String TAG = PermissionManager.class.getCanonicalName();
    private final Activity activity;
    private boolean PermissionDenied = false;

    // --------------------------------------------------------------------------------------------


    // we need to know which activity is applying for permits
    public PermissionManager(Activity activity){

        this.activity = activity;

    }

    public boolean askNeededPermission(int RequestCode, boolean performRequest){

        ArrayList<String> MissingPermissions = new ArrayList<String>();

        for (String permission : NEEDED_PERMISSION) {

            // verifico che abbia il permesso di accedere alla posizione
            if (ContextCompat.checkSelfPermission(this.activity, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permission " + permission + " has been granted");
                if (MissingPermissions.isEmpty()){
                    Log.d(TAG, "IS EMPTY");
                }
                continue;
            } /* else if (shouldShowRequestPermissionRationale(...)) {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
            showInContextUI(...);

            } */
            else {
                MissingPermissions.add(permission);
                Log.d(TAG, "The permission " + permission + " is not granted");
            }

        }

        if(MissingPermissions.isEmpty()){

            if(ContextCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                return false;

            } else {

                ask4backgroundlocation(RequestCode);

            }

        }

        // if permits are missing we ask for them
        if (performRequest) {



            if(!PermissionDenied) {

                Log.d(TAG, "Request for missing permissions " + MissingPermissions);
                // https://developer.android.com/reference/androidx/core/app/ActivityCompat#requestPermissions(android.app.Activity,java.lang.String[],int)
                ActivityCompat.requestPermissions(this.activity, MissingPermissions.toArray(new String[MissingPermissions.size()]), RequestCode);

                PermissionDenied = true;

            } else {


                // Required to ensure operation of the app in the background

                Log.d(TAG, "Opening settings to manually grant" + MissingPermissions);
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", this.activity.getPackageName(), null);
                intent.setData(uri);

                this.activity.startActivity(intent);

            }

        }



        return true;

    }

    public boolean ask4backgroundlocation(int RequestCode){

        ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, RequestCode);

        return true;

    }

}
