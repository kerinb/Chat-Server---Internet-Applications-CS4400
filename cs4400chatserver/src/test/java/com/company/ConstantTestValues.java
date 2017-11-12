package com.company;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;

import org.mockito.Mockito;

public class ConstantTestValues { 
	public static final String HELLO = "hello \n";
	public static final String MESSAGE = "The snow leopard or ounce (Panthera uncia) is a large cat native to the mountain"
			+ " ranges of Central and South Asia. It is listed as Vulnerable on the IUCN Red List of Threatened Species "
			+ "because the global population is estimated to number less than 10,000 mature individuals and decline about "
			+ "10% in the next 23 years. As of 2016, the global population was estimated at 4,500 to 8,745 mature individual \n";
	public static final String CLIENT = "client";
	public static final String JOIN_CHATROOM = "JOIN_CHATROOM: %s\n" + "CLIENT_IP: 0.0.0.0\n" + "PORT: 0\n" 
			+ "CLIENT_NAME: %s";
	public static final String JOINED_CHATROOM = "JOINED_CHATROOM: %s\n" + "SERVER_IP: %s" + "PORT: %s\n" + "JOIN_ID: %s";
	public static final String CHAT = "CHAT: %s\n" + "CLIENT_NAME: %s\n" + "JOIN_ID: %s\n" + "MESSAGE: %s";
	public static final String LEAVE_CHATROOM = "LEAVE_CHATROOM: %s\n" + "JOIN_ID: %s\n" + "CLIENT_NAME: %s";
	public static final String DISCONNECT = "DISCONNECT: 0\n" + "PORT: 0\n" + "CLIENT_NAME: %s\n";
	
	public Socket joinClientMockSocket;
	public Socket chatClientMockSocket;
	public Socket helloClientMockSocket;
	public Socket leaveClientMockSocket;
	public Socket disconnectClientMockSocket;
	public Socket killClientMockSocket;
	
	public static final String PORT_NUMBER_TEST = "1236";

	//Constructor
	public ConstantTestValues(){
		this.joinClientMockSocket = mockClientSocket(String.format(JOIN_CHATROOM, 1, CLIENT));
		this.chatClientMockSocket = mockClientSocket(String.format(CHAT, 1, 1, CLIENT, MESSAGE));
		this.leaveClientMockSocket = mockClientSocket(String.format(LEAVE_CHATROOM, 1, 1, CLIENT));
		this.disconnectClientMockSocket = mockClientSocket(String.format(DISCONNECT, CLIENT));
		this.helloClientMockSocket = mockClientSocket(HELLO);
	}
	
	public static String getPortNumberTest(){return ConstantTestValues.PORT_NUMBER_TEST;}

	private Socket mockClientSocket(String mockRequest) {
		Socket clientSocketMock = Mockito.mock(Socket.class);
		try{
			Mockito.when(clientSocketMock.getInputStream()).thenReturn(new ByteArrayInputStream(mockRequest.getBytes()));
			Mockito.when(clientSocketMock.getOutputStream()).thenReturn(new ByteArrayOutputStream());
			return clientSocketMock;
		}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), String.format("Occurred when mocking %s", mockRequest));
			return null;
		}
	}
}

// NOTE: the message above is an excerpt from Wikipedia's page on Snow Leopards...