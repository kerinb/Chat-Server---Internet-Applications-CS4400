package com.company;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ErrorHandler {

    public static String getTodaysDate(){
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss");
        return formatter.format(today);
    }
}
