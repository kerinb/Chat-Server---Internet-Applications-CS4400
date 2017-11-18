package main.java;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class ErrorAndPrintHandler {
	

	static String getTodaysDate() {
		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
		return formatter.format(today);
	}

	static void printError(String errorMessage, String errorNote) {
		String messageToPrint = "ERROR: " + errorNote + errorMessage + "\n" + "Occurred at: " + getTodaysDate();
		System.out.println(messageToPrint);
	}
	
	static void printString(String message){
		System.out.println(message);
	}
}