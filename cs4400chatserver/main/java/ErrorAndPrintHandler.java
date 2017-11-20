package main.java;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class ErrorAndPrintHandler {
	static String getTodaysDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
		return formatter.format(Calendar.getInstance().getTime());
	}

	static void printError(String errorMessage, String errorNote) {System.out.println("ERROR: " + errorNote + errorMessage + "\n" + "Occurred at: " + getTodaysDate());}
	static void printString(String message){System.out.println(message);}
}
