package com.snabeel.systeminformation;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Util {

    public static String getFormattedDate(String timeInMilliSec){

        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy h:mm a");

        long milliSeconds= Long.parseLong(timeInMilliSec);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getFormattedTime(String timeInMilliSec){
        @SuppressLint("SimpleDateFormat") DateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        long timeInMillis = Long.parseLong(timeInMilliSec);
        calendar.setTimeInMillis(timeInMillis);
        return timeFormat.format(calendar.getTime());
    }


}
