package com.example.myapplication.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.EnterActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.FirebaseWrapper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupFragment extends LogFragment {

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
        View externalView = inflater.inflate(R.layout.fragment_signup, container, false);

        TextView link = externalView.findViewById(R.id.LoginTextSwitch);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EnterActivity) SignupFragment.this.requireActivity()).renderFragment(true);
            }
        });

        Button button = externalView.findViewById(R.id.LoginButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email = externalView.findViewById(R.id.LoginUserEmail);
                EditText password = externalView.findViewById(R.id.LoginUserPW);
                EditText password2 = externalView.findViewById(R.id.LoginUserPW2);

                if (email.getText().toString().isEmpty() ||
                        password.getText().toString().isEmpty() ||
                        password2.getText().toString().isEmpty()) {
                    // TODO: Better error handling
                    email.setError(getText(R.string.usr_required));
                    password.setError(getText(R.string.pw_required));
                    password2.setError(getText(R.string.pw_required));
                    return;
                }

                if (!password.getText().toString().equals(password2.getText().toString())) {
                    // TODO: Better error handling
                    Toast
                            .makeText(SignupFragment.this.requireActivity(), "@strings/different_pw", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                // Perform SignIn
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                auth.signUp(
                        email.getText().toString(),
                        password.getText().toString(),
                        FirebaseWrapper.Callback
                                .newInstance(SignupFragment.this.requireActivity(),
                                        SignupFragment.this.callbackName,
                                        SignupFragment.this.callbackPrms)
                );
            }
        });

        return externalView;
    }
}