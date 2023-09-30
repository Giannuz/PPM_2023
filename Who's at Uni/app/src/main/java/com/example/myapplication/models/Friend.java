package com.example.myapplication.models;

public class Friend {

    private String name;
    private String UID;

    public Friend (String name, String UID){
        this.name = name;
        this.UID = UID;
    }

    public String getName(){
        return name;
    }

    public String getUID(){
        return UID;
    }
}
