package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.myapplication.adapters.FriendsListAdapter;
import com.example.myapplication.models.FirebaseWrapper;
import com.example.myapplication.models.Friend;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageFriendsActivity extends AppCompatActivity {

    private List<Friend> Friends = new ArrayList<>();

    FriendsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_friends);

        PrivateFriendListrequest();

    }

    private void PrivateFriendListrequest() {

        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        try {
            //leggi da firebase
            new FirebaseWrapper.RTDatabase()
                    .getfriendsDbData(
                            new FirebaseWrapper.Callback(
                                    this.getClass().getMethod("firebaseDbCallbackReadPrivateFriends",
                                            Task.class), this), auth.getUid());

        } catch (NoSuchMethodException e) {
            // TODO: migliorare questa eccezione
            throw new RuntimeException(e);
        }

        // To add the back arrow button in title bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }

    public void firebaseDbCallbackReadPrivateFriends(Task <DataSnapshot> result){

        if(result.isSuccessful()){

            for(DataSnapshot child : result.getResult().getChildren()) {

                String name = Objects.requireNonNull(child.child("name").getValue()).toString();
                String UID = Objects.requireNonNull(child.getKey());

                Friends.add(new Friend(name, UID));

            }

            // RecyclerView to visualize in a compact way all the usr friends
            RecyclerView recyclerView = this.findViewById(R.id.ManageFriendsRecyclerView);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new FriendsListAdapter(Friends);
            recyclerView.setAdapter(adapter);

        }

    }

    public void removeFriend(String UID, int position){

        Toast.makeText(this, "rimuovi premuto e attivato da activity", Toast.LENGTH_SHORT).show();

        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();

        FirebaseWrapper.RTDatabase db = new FirebaseWrapper.RTDatabase();
        db.removePrivateFrined(UID, auth.getUid());

        // Remove from recycler view to visually communicate the removal of the friend to the usr
        Friends.remove(position);
        adapter.notifyItemRemoved(position);



    }

    // Back arrow in Title Bar pressed
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }



}