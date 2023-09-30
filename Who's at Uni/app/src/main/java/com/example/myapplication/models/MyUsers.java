package com.example.myapplication.models;
public class MyUsers {

    private final String User;
    private final String Status;
    private final Long Time;

    public MyUsers(String User, String Status, Long Time){
        this.User = User;
        this.Status = Status;
        this.Time = Time;
    }

    public String getUser(){
        return User;
    }

    public  String getStatus(){
        return Status;
    }

    public Long getTime() { return Time; }

}
