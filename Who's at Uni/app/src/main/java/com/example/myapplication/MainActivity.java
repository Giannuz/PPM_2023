package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.adapters.UsersListAdapter;
import com.example.myapplication.interfaces.RecyclerViewInterface;
import com.example.myapplication.models.FirebaseWrapper;
import com.example.myapplication.models.Friend;
import com.example.myapplication.models.MyUsers;
import com.example.myapplication.models.MyWorker;
import com.example.myapplication.models.atUNI;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MainActivity extends AppCompatActivity implements LocationListener, RecyclerViewInterface {

    protected LocationManager mLocationManager;
    protected Context context;

    UsersListAdapter ULA;
    private List<MyUsers> users = new ArrayList<>();
    private boolean write;
    private Friend newfriend;
    private List<Friend> oldfriends = new ArrayList<>();
    public final List<Friend> oldfriendstoupdate = new ArrayList<>();
    private boolean condition;
    private static final String SP_WORKER_STARTED = "worker_start";
    private static final String KILL_SWITCH_PREFERENCE = "K_S_P";
    private String WifiSSID = "WIFI";
    String Result;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;

    private RxDataStore<Preferences> dataStore =
            new RxPreferenceDataStoreBuilder(this, "settings").build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lanciamo il thread per il worker
        new Thread(new DataStorageThread()).start();



        new Thread(new Runnable() {
            @Override
            public void run() {

                synchronized (MainActivity.this){
                    try {
                        MainActivity.this.wait();
                    } catch (InterruptedException e) {

                        // TODO: handle for this event

                        throw new RuntimeException(e);
                    }
                }

                if (condition) {

                    PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(MyWorker.class,
                            25, TimeUnit.MINUTES,
                            5, TimeUnit.MINUTES)
                            .build();

                    WorkManager.getInstance(MainActivity.this).enqueueUniquePeriodicWork(
                            "updateUserStatusUNI",
                            ExistingPeriodicWorkPolicy.KEEP,
                            periodicWork
                    );MainActivity.this.finish();

                    setWorkerStarted();

                }

            }

        }).start();

        // GET WIFI SSID ###########################################################################

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();

        if(wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {

            WifiSSID = wifiInfo.getSSID();

            //txtLat = findViewById(R.id.wifiSSID);
            //txtLat.setText(WifiSSID);

        }

        // take the position and communicate it to the server ######################################

        write = false;
        FriendListRequest();
        //setStatus();

        // Take data from Firebase regarding Usr ###################################################

        getRemoteData();

        // get friends status ######################################################################

        getFriendsStatus();

        // button "+" #############################################################################

        FloatingActionButton addbutton = this.findViewById(R.id.add_fab);

        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* OLD implamentation with the Friend sheet

                View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.fragment_new_friend_sheet, null);

                AlertDialog alertDialog = new MaterialAlertDialogBuilder(MainActivity.this).setView(view1).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText newFriendUID = view1.findViewById(R.id.NewFriendEditText);
                        TextInputEditText newFriendName = view1.findViewById(R.id.NewFriendNameEditText);

                        if (newFriendName.getText().toString().isEmpty()){
                            newFriendName.setError(getText(R.string.namerequired));
                        } else if (newFriendUID.getText().toString().isEmpty()) {

                            newFriendUID.setError(getText(R.string.uidrequired));

                        } else {
                            write = true;
                            newfriend = new Friend(newFriendName.getText().toString(), newFriendUID.getText().toString());
                            FriendListRequest();
                            dialog.dismiss();
                        }

                    }
                }).create();
                alertDialog.show();


                 */

                Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                MainActivity.this.startActivity(intent);
                //MainActivity.this.finish(); removed so that user can jump back to main activity with back button on his screen


            }
        });

        // SWIPE TO REFRESH ########################################################################

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // First check if the list of friends, then set status
                FriendListRequest(); // this also sets status
                users.clear();
                oldfriends.clear();
                if (ULA != null) {
                    ULA.clearData();
                }
                getRemoteData();
                getFriendsStatus();
                pullToRefresh.setRefreshing(false);

            }
        });

        // KILLSWITCH ##############################################################################

        final Button killswitch = findViewById(R.id.killswitch);
        CardView card = findViewById(R.id.CardView);

        if(loadDataKillSwitch()){
            killswitch.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#198768")));
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1daf86")));
            //killswitch.setText(getResources().getString(R.string.killswitchTextstop));
            killswitch.setText("⏯");
        } else {
            killswitch.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DD2C00")));
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#C8102E")));
            //killswitch.setText(getResources().getString(R.string.killswitchTextresume));
            killswitch.setText("⏯︎");
        }

        killswitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                if(loadDataKillSwitch()){

                    SaveKillSwitchPreference();

                    CharSequence text = getResources().getString(R.string.killswitchtoast1);
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                    toast.show();
                    killswitch.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DD2C00"))); //red
                    card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#C8102E")));
                    //killswitch.setText(getResources().getString(R.string.killswitchTextresume));
                    killswitch.setText("⏯");


                } else {

                    SaveKillSwitchPreference();
                    setStatus();

                    CharSequence text = getResources().getString(R.string.killswitchtoast2);
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                    toast.show();

                    killswitch.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#198768"))); // green
                    card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#1daf86")));
                    //killswitch.setText(getResources().getString(R.string.killswitchTextstop));
                    killswitch.setText("⏯");


                }

            }
        });

        // Cardview update #########################################################################

        TextView UsrStatus = findViewById(R.id.UsrStatusBox);
        UsrStatus.setText(Result);

        // Drawer ##################################################################################

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_managefriends:{

                        Intent intent = new Intent(MainActivity.this, ManageFriendsActivity.class);
                        MainActivity.this.startActivity(intent);
                        drawerLayout.closeDrawers();
                        break;

                    }
                    case R.id.nav_logout:{
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, EnterActivity.class);
                        startActivity(intent);
                        dataStore.dispose();
                        finish();
                        break;
                    }
                    case R.id.nav_infoapp:{
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Giannuz"));
                        startActivity(browserIntent);
                        drawerLayout.closeDrawers();
                        break;
                    }
                }
                return false;
            }
        });


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

    public void firebaseDbCallback(Task <DataSnapshot> result){

        assert result != null;

        if(result.isSuccessful()) {

            DataSnapshot statusSnapshot = result.getResult().child("status");

            String status = Objects.requireNonNull(statusSnapshot.getValue()).toString();
            //Long time = Objects.requireNonNull(timeSnapshot.getValue(Long.class));

            //long currentUnixTime = System.currentTimeMillis() / 1000L;

            //long secondsago = currentUnixTime - time;

            TextView statusbox = findViewById(R.id.UsrStatusBox);
            statusbox.setText(status);


        }

        /* OLD

        if(result.isSuccessful()){

            // iterazione sugli oggetti ricevuti dal db remoto, mi salvo gli utenti e il loro stato
            // nella lista users
            for(DataSnapshot child : result.getResult().getChildren()) {

                String UID = child.getKey().toString();
                String status = child.getValue().toString();
                Long time = child.getValue(Long.class);

                long currentUnixTime = System.currentTimeMillis() / 1000L;

                long secondsago = currentUnixTime - time;

                int e = 0;

                for(Friend a : oldfriends){

                    String friendUID = oldfriends.get(e).getUID();
                    
                    if(UID.equals(auth.getUid())){
                        
                        UsrStatus = status;
                        //Usrsecondsago = secondsago;
                        
                    }

                    if(UID.equals(friendUID)){

                        users.add(new MyUsers(oldfriends.get(e).getName(), status, secondsago));

                    }

                    e++;

                }

            }

        }

        //visualizzo nella RecyclerView gli utenti ricevuti e il loro stato
        RecyclerView recyclerView = this.findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ULA = new UsersListAdapter(users, this);
        recyclerView.setAdapter(ULA);
        //ULA.notifyDataSetChanged();
        
        TextView statusbox = findViewById(R.id.UsrStatusBox);
        statusbox.setText(UsrStatus);


         */

    }

    public void firebaseDbCallbackReadAndAddFriends(Task <DataSnapshot> result){

        assert result != null;
        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        if(result.isSuccessful()){

            for(DataSnapshot child : result.getResult().getChildren()) {

                String name = child.child("/name").getValue().toString();
                String UID = child.getKey();

                Friend oldfriend = new Friend(name, UID);

                oldfriends.add(oldfriend);
                oldfriendstoupdate.add(oldfriend);

            }

            setStatus();

        }

        if(write) {

            FirebaseWrapper.RTDatabase db = new FirebaseWrapper.RTDatabase();
            db.AddFirendsDbData(auth.getUid(), newfriend, oldfriends);
            oldfriends.clear();
            write = false;

        }


    }

    public void populateRecyclerView(Task <DataSnapshot> result){

        assert result != null;

        long currentUnixTime = System.currentTimeMillis() / 1000L;

        if(result.isSuccessful()){

            for(DataSnapshot child : result.getResult().getChildren()) {

                if(child.hasChild("name")) {

                    String name = Objects.requireNonNull(child.child("name").getValue()).toString();
                    String status = Objects.requireNonNull(child.child("status").getValue()).toString();
                    Long time = Objects.requireNonNull(child.child("time").getValue(Long.class));
                    long secondsago = currentUnixTime - time;

                    users.add(new MyUsers(name, status, secondsago));

                }

            }

            // display in the RecyclerView the users received and their status
            RecyclerView recyclerView = this.findViewById(R.id.recycle);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ULA = new UsersListAdapter(users, this);
            recyclerView.setAdapter(ULA);

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

    /* Now external

    // create a datastore
    private RxDataStore<Preferences> dataStore =
            new RxPreferenceDataStoreBuilder(this, "settings").build();

     */

    // save whether the worker has already started or not
    private Preferences.Key<Boolean> EXAMPLE_SP = PreferencesKeys.booleanKey(SP_WORKER_STARTED);


    private boolean isWorkerStarted(){

        Flowable<Boolean> val = dataStore.data().map(prefs -> {
           return prefs.contains(EXAMPLE_SP) ? prefs.get(EXAMPLE_SP) : false;
        });

        // wait for blocking answer
        Boolean b = val.blockingFirst();
        return b != null && b;

    }

    private void SaveKillSwitchPreference(){


        SharedPreferences sharedPreferences = getSharedPreferences(KILL_SWITCH_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(loadDataKillSwitch()) {

            // Stop Sharing
            editor.putBoolean(KILL_SWITCH_PREFERENCE, false);
            editor.apply();

        } else{

            // resume sharing
            editor.putBoolean(KILL_SWITCH_PREFERENCE, true);
            editor.apply();
        }

    }

    public boolean loadDataKillSwitch(){

        SharedPreferences sharedPreferences = getSharedPreferences(KILL_SWITCH_PREFERENCE, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KILL_SWITCH_PREFERENCE, false); // TODO: redundant, here 4 debug

    }

    private void setWorkerStarted(){

        Single<Preferences> updateResult = dataStore.updateDataAsync(PrefsIn -> {

            // write to the datastore, create a mutable preference map and associate values with it
            // use the key 'EXAMPLE_SP' and set its value to 'true',

            MutablePreferences mutablePreferences = PrefsIn.toMutablePreferences();
            mutablePreferences.set(EXAMPLE_SP, true);
            return Single.just(mutablePreferences);


        });

        // freeze until the request is finished
        updateResult.blockingSubscribe();

    }


    @Override
    public void onItemClick(int position, View v) {

        // code that is executed when usr touches one of the items in the list

        TextView usrnameTextView = (TextView) v.findViewById(R.id.StatusBox);

        String street = usrnameTextView.getText().toString();

        /*

        CharSequence text = "Hello there! " + street;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        toast.show();

         */

        {

        }

        if(!street.equals("not at Uni") && !street.equals("not at uni")) {

            // Uri for maps app

            street = street.trim().replaceAll("'+'", " ");

            String UriString = "google.navigation:q="+street+",+Genova+italia";

            Uri gmmIntentUri = Uri.parse(UriString);

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

            startActivity(mapIntent);

        } else {

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.UsrNotAtUni), Toast.LENGTH_SHORT).show();

        }

    }



    // Class for thead, so we don't block the GUI
    private class DataStorageThread implements Runnable {

        @Override
        public void run() {

            synchronized (MainActivity.this) {

                condition = !MainActivity.this.isWorkerStarted();
                MainActivity.this.notify();

            }

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

                /*

                if(atUNI.isAtUni(location.getLatitude(),location.getLongitude())){
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                } else {
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("not at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                }

                 */


            }else {

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                Log.d(TAG, "get new position");

                while (location == null) {

                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                }

                String result = atUNI.isAtUni(location.getLatitude(),location.getLongitude(),WifiSSID);
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                db.updateData(result, auth.getUid(),location.getTime() / 1000, oldfriendstoupdate);

                /*

                if (atUNI.isAtUni(location.getLatitude(), location.getLongitude())) {
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                } else {
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    db.updateData("not at DIMA", auth.getUid(), System.currentTimeMillis() / 1000L);
                }

                 */

            }

        } else {

            // "if the killswitch is active we put not in uni regardless"
            FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
            db.updateData("not at Uni", auth.getUid(), System.currentTimeMillis() / 1000L, oldfriendstoupdate);

        }



    }

    private void getRemoteData(){

        FriendListRequest();

        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        try {
            //leggi da firebase
            new FirebaseWrapper.RTDatabase()
                    .readDbData(
                            new FirebaseWrapper.Callback(
                                    this.getClass().getMethod("firebaseDbCallback",
                                            Task.class), this), auth.getUid());

        } catch (NoSuchMethodException e) {
            // TODO: migliorare questa eccezione
            throw new RuntimeException(e);
        }


    }

    public void getFriendsStatus(){

        //newfriend = new Friend(FriendToAddName, FriendToAddUID);

        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        try {
            //read from firebase
            new FirebaseWrapper.RTDatabase()
                    .getfriendsPublicDbData(
                            new FirebaseWrapper.Callback(
                                    this.getClass().getMethod("populateRecyclerView",
                                            Task.class), this), auth.getUid());

        } catch (NoSuchMethodException e) {
            // TODO: migliorare questa eccezione
            throw new RuntimeException(e);
        }

    }

    public void Info(MenuItem item) {

        Log.d(TAG, "info pressed!");

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Giannuz"));

        startActivity(browserIntent);

    }

    // override the onOptionsItemSelected()
    // function to implement
    // the item click listener callback
    // to open and close the navigation
    // drawer when the icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}