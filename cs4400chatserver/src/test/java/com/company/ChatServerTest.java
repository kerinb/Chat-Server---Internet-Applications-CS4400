package com.company;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.AfterClass;

public class ChatServerTest {
	private ConstantTestValues constantTestValues = new ConstantTestValues();
	
	@BeforeClass
	public static void setUpTest(){
		try {
			ChatServer.initialiseServer(ConstantTestValues.getPortNumberTest());
		} catch (IOException e) {
			ErrorHandler.printError(e.getMessage(), "Occurred in setting up test server instance");
		}
	}
	
	@AfterClass
	public static void shutDownTestServer(){
		try {
			ChatServer.killServer();
		} catch (IOException e) {
			ErrorHandler.printError(e.getMessage(), "Occurred in killing the test server instance");
		}
	}
}
