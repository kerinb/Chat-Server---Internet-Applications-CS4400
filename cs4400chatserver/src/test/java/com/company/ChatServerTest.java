package com.company;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

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
	
	@Test
	public void testAddClientToServer(){
		ClientNode clientNode = new ClientNode( ConstantTestValues.CLIENT, "1", 1, constantTestValues.joinClientMockSocket);
		
		ChatServer.addClientToServer(clientNode);
		assertTrue("There is only 1 node in the server currently", ChatServer.getAllClientsConnected().size() == 1);
	}
	
	@Test 
	public void noDuplicatedAddedClients(){
		ClientNode clientNode = new ClientNode( ConstantTestValues.CLIENT, "1", 1, constantTestValues.joinClientMockSocket);

		ChatServer.addClientToServer(clientNode);
		ChatServer.addClientToServer(clientNode);
		assertTrue("Duplicate node is not added to server", ChatServer.getAllClientsConnected().size() == 1);
	}
}
