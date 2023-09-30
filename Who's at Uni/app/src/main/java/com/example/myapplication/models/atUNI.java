package com.example.myapplication.models;

import java.util.ArrayList;

public class atUNI {

    public static String isAtUni(double ltusr, double lnusr, String WifiSSID){

        ArrayList<MyUni> Branchlist = new ArrayList<MyUni>();

        // Unige Locations

        Branchlist.add(new MyUni("Via Dodecaneso", 44.40270954462025, 8.9716697298481));
        Branchlist.add(new MyUni("Via Opera Pia", 44.40310481482127, 8.958729268589996));
        Branchlist.add(new MyUni("Corso Andrea Podesta'", 44.40682010321859, 8.940000705432878));
        Branchlist.add(new MyUni("Via Delle Fontane", 44.413287758984886, 8.927467838954598));
        Branchlist.add(new MyUni("Via Balbi", 44.41464620749775, 8.926909465113853));
        Branchlist.add(new MyUni("Via F. Vivaldi", 44.41427166383654, 8.922483825934682));
        Branchlist.add(new MyUni("Viale Benedetto XV", 44.4063256317634, 8.968800964917776));

        for(MyUni element : Branchlist){



            if(isUserHere(element.getLtbranch(), element.getLnbranch(), ltusr, lnusr)){

                return element.getName();

            }

        }

        // do one last check with wifi as a last resort
        if (WifiSSID.equals("eduroam") || WifiSSID.equals("GenuaWifi")){

            return "At Uni";

        }

        return "not at Uni";

    }


    private static boolean isUserHere(double ltbranch, double lnbranch, double ltusr, double lnusr){

        // Heversine Formula

        double dLat = (ltusr - ltbranch) * Math.PI / 180;
        double dLon = (lnusr - lnbranch) * Math.PI / 180;

        double a = 0.5 - Math.cos(dLat) / 2 + Math.cos(ltbranch * Math.PI / 180) * Math.cos(ltusr * Math.PI / 180) * (1 - Math.cos(dLon)) / 2;

        double distance = Math.round(6371000 * 2 * Math.asin(Math.sqrt(a)));

        if(distance > 150){
            return false;
        } else {
            return true;
        }

    }

}
