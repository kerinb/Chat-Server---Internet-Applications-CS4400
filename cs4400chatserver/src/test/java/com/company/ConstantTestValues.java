package com.company;

import java.net.Socket;

public class ConstantTestValues { 
	private static final String HELLO = "hello \n";
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

	
}
