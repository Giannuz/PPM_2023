package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.FriendsActivity;
import com.example.myapplication.ManageFriendsActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.Friend;

import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> {

    private List<Friend> friends;

    public FriendsListAdapter(List<Friend> friends) {
        this.friends = friends;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView FriendName;
        private final TextView FriendUID;

        public ViewHolder(@NonNull View view) {
            super(view);

            this.FriendName = view.findViewById(R.id.FriendName);
            this.FriendUID = view.findViewById(R.id.FriendUID);

            itemView.findViewById(R.id.RemoveButton).setOnClickListener(v -> {

                ((ManageFriendsActivity) view.getContext()).removeFriend(FriendUID.getText().toString(), getLayoutPosition());

            });

        }

        public TextView getFriendNameBox() {
            return FriendName;
        }

        public TextView getFriendUIDBox() {
            return FriendUID;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // get the template
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_friend_layout, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListAdapter.ViewHolder viewHolder, int position) {

        // Fill the list
        viewHolder.getFriendNameBox().setText(String.valueOf(this.friends.get(position).getName()));
        viewHolder.getFriendUIDBox().setText(String.valueOf((this.friends.get(position).getUID())));

    }

    @Override
    public int getItemCount() {
        return this.friends.size();
    }

    public int getIndex(String UID, String Name){

        Friend friend = new Friend(Name, UID);

        return this.friends.indexOf(friend);

    }


}
