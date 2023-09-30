package com.example.myapplication.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class LogFragment extends Fragment {

    private static final String ARG_PARAM1 = "method_name";
    private static final String ARG_PARAM2 = "method_prms";

    protected String callbackName;
    protected Class[] callbackPrms;

    public static LogFragment newInstance(Class<? extends LogFragment> clazz, String param1, Class<?>... prms) {
        // LoginFragment fragment = new LoginFragment();
        LogFragment instance = null;
        try {
            Constructor<?> constructor = clazz.getConstructor();
            instance = (LogFragment) constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 java.lang.InstantiationException e) {
            // TODO: Handle exeption
            throw new RuntimeException(e);
        }

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_PARAM2, prms);
        instance.setArguments(args);

        return instance;
    }

    protected void initArguments() {
        /* NOTE: You can pass some args to the fragment in the Bundle object
        In the caller (Activity) context:
            ```
            Bundle bundle = new Bundle();
            String myMessage = "This is a message";
            bundle.putString("param_name", myMessage );
            ```
        In the onCreate of the Fragment class:
            ```
            if (getArguments() != null) {
                mParam1 = getArguments().getString("param_name");
            }
            ```
        */
        if (getArguments() != null) {
            this.callbackName = getArguments().getString(ARG_PARAM1);
            this.callbackPrms = (Class[]) getArguments().getSerializable(ARG_PARAM2);
        }
    }}