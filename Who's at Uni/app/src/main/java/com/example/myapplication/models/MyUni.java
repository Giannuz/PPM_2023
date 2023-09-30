package com.example.myapplication.models;

public class MyUni {

    private final String Name;
    private final double ltbranch;
    private final double lnbranch;

    public MyUni(String Name, double ltbranch, double lnbranch){

        this.Name = Name;
        this.ltbranch = ltbranch;
        this.lnbranch = lnbranch;

    }

    public String getName(){ return Name; }
    public double getLtbranch() { return ltbranch; }
    public double getLnbranch() { return lnbranch; }

}


