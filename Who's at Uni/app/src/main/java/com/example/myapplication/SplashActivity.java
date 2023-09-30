package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.models.FirebaseWrapper;
import com.example.myapplication.models.Friend;
import com.example.myapplication.models.PermissionManager;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    // First Activity to be launched
    // Check that permissions have been granted and whether the user is logged in
    // If the user is not logged in, the login screen pops up

    private static final int PERMISSION_REQUEST_CODE = (new Random()).nextInt() & Integer.MAX_VALUE;

    private void goToActivity(Class<?> activity) { // allows us to move to another activity

        Intent intent = new Intent(this, activity); // from this (SplashActivity), I want to go to the activity that was passed
        this.startActivity(intent);
        finish(); // good practice
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*
        if(!new PermissionManager(this).askNeededPermission(1)){

            // vai alla Main activity

        }
        */

        Button backgroundpositionbutton = this.findViewById(R.id.grant_background_position_button);
        backgroundpositionbutton.setEnabled(false);

        // check if the user is authenticated
        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
        if (!auth.isAuthenticated()) {

            // Go to activity for LogIn or SignUp
            this.goToActivity(EnterActivity.class);
            finish();

        }

        // if there are no permits to be granted go to the main activity
        PermissionManager pm = new PermissionManager(this); 
        if(!pm.askNeededPermission(PERMISSION_REQUEST_CODE, false)){

            if(auth.isAuthenticated()) {
                this.goToActivity(MainActivity.class);
            }

        }

        this.findViewById(R.id.GrantPermissionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){



                if (!pm.askNeededPermission(PERMISSION_REQUEST_CODE, true)){

                    if(auth.isAuthenticated()) {

                        SplashActivity.this.goToActivity(MainActivity.class);
                        finish();

                    }

                }

                backgroundpositionbutton.setEnabled(true);

            }
        });

        backgroundpositionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (!pm.ask4backgroundlocation(PERMISSION_REQUEST_CODE)){

                    if(auth.isAuthenticated()) {

                        SplashActivity.this.goToActivity(MainActivity.class);
                        finish();

                    }

                }
            }
        });

    }

    // override the function that asks android if the user has agreed
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int res : grantResults){

            // check the return values, if even one permission is not given don't move forward
            if(res == PackageManager.PERMISSION_DENIED){

                return;


            } else if(res == PackageManager.PERMISSION_GRANTED){

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    // create my entries on firebase so that the app doesn't crash trying to get
                    // them from the server

                    setfirststatus();

                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

                    if(auth.isAuthenticated()) {

                        SplashActivity.this.goToActivity(MainActivity.class);
                        finish();

                    }

                }

            }

        }

    }

    private void setfirststatus(){

        FirebaseWrapper.RTDatabase db = new FirebaseWrapper.RTDatabase();
        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
        final List<Friend> dummyoldfriendstoupdate = new ArrayList<>();
        db.updateData("not at uni", auth.getUid(),System.currentTimeMillis() / 1000L, dummyoldfriendstoupdate);

    }

}