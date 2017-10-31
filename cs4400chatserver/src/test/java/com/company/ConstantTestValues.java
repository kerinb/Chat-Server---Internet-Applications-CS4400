package com.company;

import java.net.Socket;

import org.mockito.Mockito;

public class ConstantTestValues { 
	private static final String HELLO = "hello \n";
	private static final String MESSAGE = "The snow leopard or ounce (Panthera uncia) is a large cat native to the mountain"
			+ " ranges of Central and South Asia. It is listed as Vulnerable on the IUCN Red List of Threatened Species "
			+ "because the global population is estimated to number less than 10,000 mature individuals and decline about "
			+ "10% in the next 23 years. As of 2016, the global population was estimated at 4,500 to 8,745 mature individual \n";
	private static final String CLIENT = "client";
	private static final String JOIN_CHATROOM = "JOIN_CHATROOM: %s\n" + "CLIENT_IP: 0.0.0.0\n" + "PORT: 0\n" 
			+ "CLIENT_NAME: %s";
	private static final String JOINED_CHATROOM = "JOINED_CHATROOM: %s\n" + "SERVER_IP: %s" + "PORT: %s\n" + "JOIN_ID: %s";
	private static final String CHAT = "CHAT: %s\n" + "CLIENT_NAME: %s\n" + "JOIN_ID: %s\n" + "MESSAGE: %s";
	private static final String LEAVE_CHATROOM = "LEAVE_CHATROOM: %s\n" + "JOIN_ID: %s\n" + "CLIENT_NAME: %s";
	private static final String DISCONNECT = "DISCONNECT: %s\n" + "PORT: %s\n" + "CLIENT_NAME: %s\n";
	
	public Socket joinClientMockSocket;
	public Socket chatClientMockSocket;
	public Socket helloClientMockSocket;
	public Socket leaveClientMockSocket;
	public Socket disconnectClientMockSocket;
	public Socket killClientMockSocket;

	//Constructor
	public ConstantTestValues(){
		this.joinClientMockSocket = mockClientSocket(String.format(JOIN_CHATROOM, 1, CLIENT));
		this.chatClientMockSocket = mockClientSocket(String.format(CHAT, 1, 1, MESSAGE));
		this.leaveClientMockSocket = mockClientSocket(String.format(LEAVE_CHATROOM, 1, 1, CLIENT));
		this.disconnectClientMockSocket = mockClientSocket(String.format(DISCONNECT, CLIENT));
		this.helloClientMockSocket = mockClientSocket(HELLO);
	}

	private Socket mockClientSocket(String mockRequest) {
		// TODO Auto-generated method stub
		return null;
	}
}

// NOTE: the message above is an excerpt from Wikipedia's page on Snow Leopards...