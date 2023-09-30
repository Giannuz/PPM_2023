package com.example.myapplication.adapters;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.interfaces.RecyclerViewInterface;
import com.example.myapplication.models.MyUsers;

import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {

    private List<MyUsers> users;
    private final RecyclerViewInterface recyclerViewInterface;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView UsernameBox;
        private final TextView StatusBox;
        private final TextView TimeBox;


        public ViewHolder(View view, RecyclerViewInterface recyclerViewInterface) {
            super(view);
            // Define click listener for the ViewHolder's View

            this.UsernameBox = view.findViewById(R.id.UsernameBox);
            this.StatusBox = view.findViewById(R.id.StatusBox);
            this.TimeBox = view.findViewById(R.id.TimeBox);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recyclerViewInterface != null){

                        int position = getAdapterPosition();

                        //verifico che position sia valido
                        if(position != RecyclerView.NO_POSITION){
                            recyclerViewInterface.onItemClick(position, v);
                        }

                    }
                }
            });


        }

        public TextView getUsernameBox() {
            return UsernameBox;
        }

        public TextView StatusBox() {
            return StatusBox;
        }

        public TextView TimeBox() {
            return TimeBox;
        }

    }

    public UsersListAdapter(List<MyUsers> users, RecyclerViewInterface recyclerViewInterface) {
        this.users = users;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Crea l'oggetto vista, inserisce l'oggetto vista all'interno della recicle view
        // Gli diciamo quarecyclerView.addOnItemTouchListener(l'è il template che deve prendere (single_user_layout)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout, parent, false);
        return new ViewHolder(view, recyclerViewInterface);

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Riempie l'oggetto vista con i dati dell'utente

        viewHolder.getUsernameBox().setText(String.valueOf(this.users.get(position).getUser()));
        viewHolder.StatusBox().setText(String.valueOf(this.users.get(position).getStatus()));

        if (this.users.get(position).getTime() < 60) {
            viewHolder.TimeBox().setText(String.valueOf(this.users.get(position).getTime()) + " " +  viewHolder.itemView.getContext().getResources().getString(R.string.secondsago));
        } else if (this.users.get(position).getTime() < 3600) {
            viewHolder.TimeBox().setText(Math.round(this.users.get(position).getTime() / 60) + " " + viewHolder.itemView.getContext().getResources().getString(R.string.minutesago));
        } else if (this.users.get(position).getTime() < 86400) {
            viewHolder.TimeBox().setText(Math.round(this.users.get(position).getTime() / 3600) + " " + viewHolder.itemView.getContext().getResources().getString(R.string.hoursago));
        } else {
            viewHolder.TimeBox().setText(Math.round(this.users.get(position).getTime() / 86400) + " " + viewHolder.itemView.getContext().getResources().getString(R.string.daysago));
        }

    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    public void clearData() {
        users.clear();; // clear list
        notifyDataSetChanged(); // let adapter know about the changes and reload view.
    }


    public String getStatus(int position){ return this.users.get(position).getStatus();}

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Log.d(TAG, "swipe");
        }
    };

}