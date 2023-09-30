package com.example.myapplication.fragment;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.myapplication.EnterActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.FirebaseWrapper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends LogFragment {
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // See: https://developer.android.com/reference/android/view/LayoutInflater#inflate(org.xmlpull.v1.XmlPullParser,%20android.view.ViewGroup,%20boolean)
        View externalView = inflater.inflate(R.layout.fragment_login, container, false);

        TextView link = externalView.findViewById(R.id.LoginTextSwitch);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EnterActivity)LoginFragment.this.requireActivity()).renderFragment(false);
            }
        });

        Button button = externalView.findViewById(R.id.LoginButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email = externalView.findViewById(R.id.LoginUserEmail);
                EditText password = externalView.findViewById(R.id.LoginUserPW);

                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    // TODO: Better error handling + remove this hardcoded strings
                    email.setError(getText(R.string.usr_required));
                    password.setError(getText(R.string.pw_required));
                    return;
                }

                // Perform SignIn
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                auth.signIn(
                        email.getText().toString(),
                        password.getText().toString(),
                        FirebaseWrapper.Callback
                                .newInstance(LoginFragment.this.requireActivity(),
                                        LoginFragment.this.callbackName,
                                        LoginFragment.this.callbackPrms)
                );
            }
        });

        return externalView;
    }
}