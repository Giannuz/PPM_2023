package com.example.myapplication.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.models.CaptureAct;
import com.example.myapplication.models.Friend;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Objects;


public class ScanQrFragment extends Fragment {

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View externalView = inflater.inflate(R.layout.fragment_scan_qr, container, false);

        Button btn_scan = externalView.findViewById(R.id.ScanButton);
        btn_scan.setOnClickListener(view -> ScanCode());

        Button btn_manualadd = externalView.findViewById(R.id.ManualAdd);

        btn_manualadd.setOnClickListener(v -> ShowPopUp(null));


        return externalView;
    }

    private void ScanCode(){

        ScanOptions options = new ScanOptions();
        options.setPrompt(getResources().getString(R.string.vlmupflash));
        //options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        // request permissions to use the camera among other things
        options.setCaptureActivity(CaptureAct.class);

        barLauncher.launch(options);


    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{

        if(result.getContents() != null){

            ShowPopUp(result);

        }

    });

    // instance of what implement the interface
    ScanQrListener mListener;

    // get the access to the activity (i think that this will not work because we are getting the context from FriendsActivity, but let's try)
    @Override
    public void onAttach(@NonNull Context context){

        super.onAttach(context);
        mListener = (ScanQrListener) context;

    }

    // define the interface in the fragment
    public interface ScanQrListener{
        void addFriendexternal(Friend NewExternalFriend);
    }

    public void ShowPopUp(ScanIntentResult result){

        View view1 = LayoutInflater.from(getContext()).inflate(R.layout.fragment_new_friend_sheet, null);

        TextInputEditText newFriendUID = view1.findViewById(R.id.NewFriendEditText);
        // automatically put what I scanned into the "popup" fragment

        if(result != null) {

            newFriendUID.setText(result.getContents());

        }

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext()).setView(view1).setPositiveButton("OK", (dialog, which) -> {

            TextInputEditText newFriendName = view1.findViewById(R.id.NewFriendNameEditText);

            if (Objects.requireNonNull(newFriendName.getText()).toString().isEmpty()){
                newFriendName.setError(getText(R.string.namerequired));
            } else if (Objects.requireNonNull(newFriendUID.getText()).toString().isEmpty()) {

                newFriendUID.setError(getText(R.string.uidrequired));

            } else {

                // Replace this with a call to a function inside main activity that calls FriendListRequest()

                Friend toAdd = new Friend(newFriendName.getText().toString(), newFriendUID.getText().toString());

                mListener.addFriendexternal(toAdd);

                dialog.dismiss();
            }

        }).create();
        alertDialog.show();

    }


}