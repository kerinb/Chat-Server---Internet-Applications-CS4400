package com.company;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;

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
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred in setting up test server instance");
		}
	}
	
	@AfterClass
	public static void shutDownTestServer(){
		try {
			ChatServer.killServer();
		} catch (IOException e) {
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred in killing the test server instance");
		}
	}
	
	@Test
	public void testAddClientToServer(){
		ClientNode clientNode = new ClientNode( ConstantTestValues.CLIENT, "1", 1, constantTestValues.joinClientMockSocket);
		
		ChatServer.addClientToServer(clientNode);
		System.out.println(ChatServer.getAllClientsConnected().size());
		assertTrue("There is only 1 node in the server currently", ChatServer.getAllClientsConnected().size() == 1);
	}
	
	@Test 
	public void noDuplicatedAddedClients(){
		ClientNode clientNode = new ClientNode( ConstantTestValues.CLIENT, "1", 1, constantTestValues.joinClientMockSocket);
		
		ChatServer.addClientToServer(clientNode);
		ChatServer.addClientToServer(clientNode);

		assertTrue("Duplicate node is not added to server", ChatServer.getAllClientsConnected().size() == 1);
	}

	@Test
	public void joinRequestTest() throws IOException{
		Socket clientSocketMock = constantTestValues.joinClientMockSocket;
		ClientNode clientNodeMock = new ClientNode( "client", "1", 0, clientSocketMock);
		boolean test = clientNodeDataMatches(ChatServer.getClientInfoFromMessage(clientSocketMock, RequestType.JoinChatroom), clientNodeMock);
		System.out.println(test);
		assertTrue(test);
	}
	
	private boolean clientNodeDataMatches(ClientNode extractedClientInfo, ClientNode mockClientInfo) throws IOException{
		if(extractedClientInfo.getConnection().getInputStream() != mockClientInfo.getConnection().getInputStream()){
			return false;
		}
		if(!(extractedClientInfo.getName().equals(mockClientInfo.getName()))){
			return false;
		}
		if(!(extractedClientInfo.getChatRoomId() == null)&&!(mockClientInfo.getChatRoomId() == null)){
			if(!(extractedClientInfo.getChatRoomId() != mockClientInfo.getChatRoomId())){
				return false;
			}
		}else{
			if(extractedClientInfo.getChatRoomId() != mockClientInfo.getChatRoomId()){
				return false;
			}
		}
		if(!extractedClientInfo.getMemberId().equals(mockClientInfo.getMemberId())){
			return false;
		}
		return true;
	}
}
