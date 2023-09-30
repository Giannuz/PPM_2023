package com.example.myapplication.models;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;
//import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
//import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

//import com.example.myapplication.MainActivity;
//import com.example.myapplication.R;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


public class MyWorker extends ListenableWorker implements LocationListener{

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */

    String WifiSSID = "WIFI";
    private final List<Friend> oldfriends = new ArrayList<>();

    protected LocationManager mLocationManager;
    Context mContext;

    private static final String KILL_SWITCH_PREFERENCE = "K_S_P";


    public MyWorker(@NonNull Context mContext, @NonNull WorkerParameters workerParams) {

        super(mContext, workerParams);

        this.mContext = mContext;

    }
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        Log.d(TAG, "WORKER ATTIVO");

        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
        if (auth.isAuthenticated()) {

            FriendListRequest();

            return Futures.immediateFuture(Result.success());

        } else {

            Log.d(TAG, "User not logged in");

            return Futures.immediateFuture(Result.success());

        }

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);

        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }


    public void firebaseDbCallbackReadAndAddFriends(Task <DataSnapshot> result){

        assert result != null;
        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        if(result.isSuccessful()){

            for(DataSnapshot child : result.getResult().getChildren()) {

                String name = child.child("/name").getValue().toString();
                String UID = child.getKey().toString();

                Friend oldfriend = new Friend(name, UID);

                oldfriends.add(oldfriend);

            }

            setStatus();

        }





    }

    public void FriendListRequest(){

        //newfriend = new Friend(FriendToAddName, FriendToAddUID);

        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        try {
            //leggi da firebase
            new FirebaseWrapper.RTDatabase()
                    .getfriendsDbData(
                            new FirebaseWrapper.Callback(
                                    this.getClass().getMethod("firebaseDbCallbackReadAndAddFriends",
                                            Task.class), this), auth.getUid());

        } catch (NoSuchMethodException e) {
            // TODO: migliorare questa eccezione
            throw new RuntimeException(e);
        }

    }

    private void setStatus(){

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(KILL_SWITCH_PREFERENCE, MODE_PRIVATE);

        boolean pass = sharedPreferences.getBoolean(KILL_SWITCH_PREFERENCE, false);

        if(pass) { // killswitch check

            // FIREBASE
            FirebaseWrapper.RTDatabase db = new FirebaseWrapper.RTDatabase();


            // GEOLOG

            String context = Context.LOCATION_SERVICE;

            mLocationManager = (LocationManager) mContext.getSystemService(context);

            // Wifi SSID

            WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo;

            wifiInfo = wifiManager.getConnectionInfo();

            if(wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {

                WifiSSID = wifiInfo.getSSID();

            }


            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {

                String resultuni = atUNI.isAtUni(location.getLatitude(),location.getLongitude(),WifiSSID);
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                db.updateData(resultuni, auth.getUid(),System.currentTimeMillis() / 1000L, oldfriends);
                Log.d(TAG, "stato aggiornato");

                /*

                if (atUNI.isAtUni(location.getLatitude(), location.getLongitude())) {
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                } else {
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("not at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                }

                 */


            } else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                while (location == null) {

                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Log.d(TAG, "aspettando posizione..");


                }

                String resultuni = atUNI.isAtUni(location.getLatitude(),location.getLongitude(),WifiSSID);
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                db.updateData(resultuni, auth.getUid(),System.currentTimeMillis() / 1000L, oldfriends);
                Log.d(TAG, "stato aggiornato");

                /*

                if (atUNI.isAtUni(location.getLatitude(), location.getLongitude())) {
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                } else {
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("not at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                    Log.d(TAG, "stato aggiornato");
                }

                 */

            }

        } else {

            Log.d(TAG, "killswitch active, state not updated");

        }


    }


}


