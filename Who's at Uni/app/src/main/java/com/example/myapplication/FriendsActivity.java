package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
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
import android.view.MenuItem;
import android.widget.Toast;

import com.example.myapplication.adapters.MyViewPagerAdapter;
import com.example.myapplication.fragment.ScanQrFragment;
import com.example.myapplication.models.FirebaseWrapper;
import com.example.myapplication.models.Friend;
import com.example.myapplication.models.atUNI;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class FriendsActivity extends AppCompatActivity implements ScanQrFragment.ScanQrListener {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyViewPagerAdapter myViewPagerAdapter;

    private Friend newfriend;
    private List<Friend> oldfriends = new ArrayList<>();
    public final List<Friend> oldfriendstoupdate = new ArrayList<>();
    private boolean write;
    private static final String KILL_SWITCH_PREFERENCE = "K_S_P";
    protected LocationManager mLocationManager;
    String Result;
    private String WifiSSID = "WIFI";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        myViewPagerAdapter = new MyViewPagerAdapter(this);

        viewPager2.setAdapter(myViewPagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();

        if(wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {

            WifiSSID = wifiInfo.getSSID();

        }

        // To add the back arrow button in title bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }



    @Override
    public void addFriendexternal(Friend NewExternalFriend) {
        Log.d(TAG, "Friend Recived!: " + NewExternalFriend.getUID() + " " + NewExternalFriend.getName());

        write = true;
        newfriend = NewExternalFriend;
        FriendListRequest();

        Toast.makeText(FriendsActivity.this, getResources().getString(R.string.FriendAdded), Toast.LENGTH_SHORT).show();

        /*

        USING WHAT FOLLOW I GET:

        java.lang.IllegalStateException: There are multiple DataStores active for the same file: /data/user/0/com.example.myapplication/files/datastore/settings.preferences_pb. You should either maintain your DataStore as a singleton or confirm that there is no two DataStore's active on the same file (by confirming that the scope is cancelled).

        // Convert Friend Object to Json so that we don't have to use Parcelable or Serializable
        // which is apparently less efficient
        Gson gS = new Gson();
        String target = gS.toJson(NewExternalFriend);

        // Intent to pass the new "json string" to main activity
        Intent intent = new Intent(FriendsActivity.this, MainActivity.class);
        intent.putExtra("FriendObjectJson", target);

        startActivity(intent);

         */

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

    public void firebaseDbCallbackReadAndAddFriends(Task <DataSnapshot> result){

        assert result != null;
        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        if(result.isSuccessful()){

            for(DataSnapshot child : result.getResult().getChildren()) {

                String name = Objects.requireNonNull(child.child("/name").getValue()).toString();
                String UID = child.getKey();

                Friend oldfriend = new Friend(name, UID);

                oldfriends.add(oldfriend);
                oldfriendstoupdate.add(oldfriend);

            }

            // disabled to test if without this it becomes more stable
            //setStatus();

        }

        if(write) {

            FirebaseWrapper.RTDatabase db = new FirebaseWrapper.RTDatabase();
            db.AddFirendsDbData(auth.getUid(), newfriend, oldfriends);
            oldfriends.clear();
            write = false;

        }


    }

    private void setStatus(){

        FirebaseWrapper.RTDatabase db = new FirebaseWrapper.RTDatabase();

        if(loadDataKillSwitch()){

            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // checks if LastKnownLocation is recent enough, if it is not it draws on the current location
            if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {

                Result = atUNI.isAtUni(location.getLatitude(),location.getLongitude(),WifiSSID);
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                db.updateData(Result, auth.getUid(),location.getTime()/1000, oldfriendstoupdate);

            }else {

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);

                Log.d(TAG, "get new position");

                while (location == null) {

                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                }

                String result = atUNI.isAtUni(location.getLatitude(),location.getLongitude(),WifiSSID);
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                db.updateData(result, auth.getUid(),location.getTime() / 1000, oldfriendstoupdate);

            }

        } else {

            // "if the killswitch is active we put not in uni regardless"
            FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
            db.updateData("not at Uni", auth.getUid(), System.currentTimeMillis() / 1000L, oldfriendstoupdate);

        }



    }

    public boolean loadDataKillSwitch(){

        SharedPreferences sharedPreferences = getSharedPreferences(KILL_SWITCH_PREFERENCE, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KILL_SWITCH_PREFERENCE, false);

    }

    // Back arrow in Title Bar pressed
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }



}