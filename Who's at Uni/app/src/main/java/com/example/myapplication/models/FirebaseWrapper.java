package com.example.myapplication.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class FirebaseWrapper {

    public static class Callback {

        // reflection

        private static final String TAG = Callback.class.getCanonicalName();

        private final Method method; // instance of the method we want to invoke
        private final Object thiz; // object on which we want to invoke it

        public Callback(Method method, Object thiz) {
            this.method = method;
            this.thiz = thiz;
        }

        public static Callback newInstance(Object thiz, String name, Class<?>... prms) {
            Class<?> clazz = thiz.getClass();
            try {
                return new Callback(clazz.getMethod(name, prms), thiz);
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Cannot find method " + name + " in class " + clazz.getCanonicalName());

                // TODO: Better handling of the error
                throw new RuntimeException(e);
            }
        }

        public void invoke(Object... objs) { // Can receive a generic number of objects of any type
            // invokes method
            try {
                this.method.invoke(thiz, objs);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Error in reflection, Message: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

    }

    public static class Auth {
        // define all methods
        private final static String TAG = Auth.class.getCanonicalName();
        private final FirebaseAuth auth;

        public Auth() {
            this.auth = FirebaseAuth.getInstance();
        }

        public boolean isAuthenticated() {
            return this.auth.getCurrentUser() != null;
        }

        public FirebaseUser getUser() {
            return this.auth.getCurrentUser();
        }

        public void signOut() {
            this.auth.signOut();
        }

        public void signIn(String email, String password, FirebaseWrapper.Callback callback) {
            this.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            callback.invoke(task.isSuccessful());
                        }
                    });
        }

        public void signUp(String email, String password, FirebaseWrapper.Callback callback) {
            this.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            callback.invoke(task.isSuccessful());
                        }
                    });
        }

        public String getUid() {
            // TODO: remove this assert and better handling of non logged-in users
            assert this.isAuthenticated();
            return this.getUser().getUid();
        }


    }

    public static class RTDatabase {

        private static String TAG = RTDatabase.class.getCanonicalName();

        private static final String CHILD = "Users";

        private DatabaseReference getDB(){

            DatabaseReference ref = FirebaseDatabase.getInstance("https://ppm2023-7c845-default-rtdb.europe-west1.firebasedatabase.app").getReference();

            return ref;

        }

        public void WriteUserDbData(String userUID, MyUsers myUsers) {

            DatabaseReference ref = getDB().child("Users");

            if(ref == null){
                return;
            }

            ref.child(String.valueOf(myUsers.getUser())).setValue(myUsers);

        }



        public void AddFirendsDbData(String userUID, Friend newfriend, List oldfriends){

            //DatabaseReference ref = getDB().child("friends");
            DatabaseReference ref = getDB().child("friends").child(userUID);
            DatabaseReference refp = getDB().child("privatefriends").child(userUID);

            if(ref == null){
                return;
            }

            if(!oldfriends.contains(newfriend)){

                /*
                String key = ref.push().getKey();
                Map<String, Object> map = new HashMap<>();
                map.put("", newfriend.getUID());
                ref.updateChildren(map);
                */

                ref.child(newfriend.getUID()).child("name").setValue(newfriend.getName());
                ref.child(newfriend.getUID()).child("status").setValue("unknown");
                ref.child(newfriend.getUID()).child("time").setValue(System.currentTimeMillis() / 1000L);

                // PrivateFriendListAdd

                refp.child(newfriend.getUID()).child("name").setValue(newfriend.getName());


            }

            /*

            if(!oldfriends.contains(newfriend)){
                oldfriends.add(newfriend);
                ref.child("friends").removeValue();
                ref.child(userUID).setValue(oldfriends);
            }

             */

        }



        public void readDbData(FirebaseWrapper.Callback callback, String userUID){
            DatabaseReference ref = getDB().child("Users").child(userUID);
            //DatabaseReference ref = getDB();
            if (ref == null) {
                return;
            }

            // Read from the database
            ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    callback.invoke(task);
                }
            });
        }

        public void getfriendsDbData(FirebaseWrapper.Callback callback, String userUID){

            //DatabaseReference ref = getDB().child("friends/"+userUID);
            DatabaseReference ref = getDB().child("privatefriends").child(userUID);
            if (ref == null) {
                return;
            }

            // Read from the database
            ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    callback.invoke(task);
                }
            });

        }

        public void getfriendsPublicDbData(FirebaseWrapper.Callback callback, String userUID){

            //DatabaseReference ref = getDB().child("friends/"+userUID);
            DatabaseReference ref = getDB().child("friends").child(userUID);
            if (ref == null) {
                return;
            }

            // Read from the database
            ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    callback.invoke(task);
                }
            });

        }

        public void updateData(String newStatus, String userUID, Long Unixtime, List<Friend> oldfriends) {

            DatabaseReference ref = getDB().child("Users");

            ref.child(userUID).child("status").setValue(newStatus);
            ref.child(userUID).child("time").setValue(Unixtime);


            // Aggiorno lo status anche per gli amici
            DatabaseReference refFriends = getDB().child("friends");

            for(Friend i : oldfriends){

                String friendUID = i.getUID();

                refFriends.child(i.getUID()).child(userUID).child("status").setValue(newStatus);
                refFriends.child(i.getUID()).child(userUID).child("time").setValue(Unixtime);

            }

        }

        public void removePrivateFrined(String FriendUID, String userUID){

            DatabaseReference ref = getDB().child("privatefriends").child(userUID);
            ref.child(FriendUID).removeValue();

            DatabaseReference ref2 = getDB().child("friends").child(userUID);
            ref2.child(FriendUID).removeValue();

        }

    }


}