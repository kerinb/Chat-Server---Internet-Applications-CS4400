package com.company;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {
	public static AtomicInteger nextClientId;
	public static AtomicInteger nextChatRoomId;
	
	private static ServerSocket serverSocket;
	
	private static volatile boolean isServerRunning;
	
	private static List<ChatRoom> listOfAllActiveChatRooms;
	private static List<Socket> listOfAllActiveClients;
	
	static int serverPort;
	
	static String serverIP;
	
	public static int getServerPort(){return serverPort;}
	public static String getServerIp(){return serverIP;}
	public static void setRunningValue(boolean bool){isServerRunning = bool;}
	public static ServerSocket getServerSocket(){return serverSocket;}
	private static List<Socket> getListOfAllConnectedClients() {return listOfAllActiveClients;}
	private static List<ChatRoom> getListOfAllActiveChatRooms() {return listOfAllActiveChatRooms;}
	
	public static void main(String[] args){
		try{
			initialiseServer(args[0]);
			while(true){
				if(isServerRunning = false){
					shutServerDown();
				}
				takeCareOfConnection();
			}
		}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when taking in new client");
		}finally{
			shutServerDown();
		}
	}

	private static void takeCareOfConnection() throws IOException {
		Socket socket = maintainNewConenction();
		runClientThread(socket);		
	}

	private static void runClientThread(Socket socket) {
		ClientThread clientThread = new ClientThread(socket);
		clientThread.start();		
	}

	private static Socket maintainNewConenction() throws IOException {
		Socket socket = serverSocket.accept();
		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);
		return socket;
	}

	private static void initialiseServer(String string) throws IOException {
		serverPort = Integer.parseInt(string);
		serverSocket = new ServerSocket(serverPort);
		serverIP = InetAddress.getLocalHost().getHostAddress().toString();
		intialiseServerVariables();
	}

	private static void intialiseServerVariables() {
		listOfAllActiveClients = new ArrayList<Socket>();
		listOfAllActiveChatRooms = new ArrayList<ChatRoom>();
		isServerRunning = true;
		nextClientId = new AtomicInteger(0);
		nextChatRoomId = new AtomicInteger(0);
	}

	private static void shutServerDown() {
		try{
			ErrorAndPrintHandler.printString("Shutting down server");
			for(Socket socket : getListOfAllConnectedClients()){
				socket.getInputStream().close();
				socket.getOutputStream().close();
				socket.close();
			}
			listOfAllActiveChatRooms.clear();
			listOfAllActiveClients.clear();
			serverSocket.close();
		}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Error Occurred when shutting down server");
		}
	}
	
	static synchronized void recordChangeOfClientWithServer(RequestType requestType, Socket socket,
			RequestTypeNode requestTypeNode) throws IOException{
		if(requestTypeNode != null){
			if((requestType.equals(RequestType.JoinChatroom)) && (!getListOfAllConnectedClients().contains(socket)) 
					&& (getChatRoomByIdIfExist(requestTypeNode.getChatRoomId()) != null)){
				addClientToServer(socket);
				return;
			}else if((requestType.equals(RequestType.Disconnect)) && (getListOfAllConnectedClients().contains(socket))){
				removeClientFromServer(socket, getChatRoomByIdIfExist(requestTypeNode.getChatRoomId()));
				return;
			}
		}
	}

	private static void removeClientFromServer(Socket socket, Object object) throws IOException {
		for(ChatRoom chatRoom : listOfAllActiveChatRooms){
			if(chatRoom == object){
				chatRoom.getListOfAllConnectedClients().remove(socket);
				break;
			}
			listOfAllActiveClients.remove(socket);
			socket.close();
			return;
		}
	}

	private static void addClientToServer(Socket clientSocket) {
		for(Socket socket : listOfAllActiveClients){
			if(clientSocket == socket){
				ErrorAndPrintHandler.printString("Client Not Added: Already present in Chat Room");
				return;
			}
		}
		listOfAllActiveClients.add(clientSocket);
	}

	private static synchronized ChatRoom getChatRoomByIdIfExist(String chatRoomId) {
		for(ChatRoom chatRoom : listOfAllActiveChatRooms){
			if(chatRoom.getChatRoomId().equals(chatRoomId)){
				return chatRoom;
			}
		}
		return null;
	}
		
	private static ChatRoom getChatRoomByRefIfExist(String chatRoomRef){
		for(ChatRoom chatRoom : listOfAllActiveChatRooms){
			if(chatRoom.getChatRoomRef().equals(chatRoomRef)){
				return chatRoom;
			}
		}
		return null;
	}
	
	
}
