package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.fragment.LogFragment;
import com.example.myapplication.fragment.LoginFragment;
import com.example.myapplication.fragment.SignupFragment;

public class EnterActivity extends AppCompatActivity {
    private static final String TAG = EnterActivity.class.getCanonicalName();

    private FragmentManager fragmentManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        renderFragment(true);
    }

    // https://developer.android.com/guide/fragments/transactions
    public void renderFragment(boolean isLogin) {
        Fragment fragment = null;
        if (isLogin) {
            fragment = LogFragment.newInstance(LoginFragment.class, "signinCallback", boolean.class);
        } else {
            fragment = LogFragment.newInstance(SignupFragment.class, "signinCallback", boolean.class);
        }
        if (this.fragmentManager == null) {
            this.fragmentManager = this.getSupportFragmentManager();
        }

        FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();

        // For optimizations -- See: https://developer.android.com/reference/androidx/fragment/app/FragmentTransaction#setReorderingAllowed(boolean)
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.loginRegisterFragment, fragment);

        fragmentTransaction.commit();
    }

    public void signinCallback(boolean result) {
        if (!result) {
            Toast
                    .makeText(this, "Username or password are not valid", Toast.LENGTH_LONG)
                    .show();
        } else {
            // Go To Splash to check the permissions
            Intent intent = new Intent(this, SplashActivity.class);
            this.startActivity(intent);
            this.finish();
        }
    }

}