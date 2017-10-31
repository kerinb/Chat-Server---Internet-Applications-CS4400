package com.company;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class ErrorHandler {
	

	static String getTodaysDate() {
		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
		return formatter.format(today);
	}

	static void printError(String errorMessage, String errorNote) {
		String messageToPrint = "ERROR: " + errorNote + errorMessage + "\n" + "Occurred at: " + getTodaysDate();
		System.out.println(messageToPrint);
	}
}
