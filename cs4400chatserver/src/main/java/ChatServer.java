package main.java;

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
	private static List<ConnectedClient> listOfAllActiveClients;
	
	static int serverPort;
	
	static String serverIP;
	
	public static int getServerPort(){return serverPort;}
	public static String getServerIp(){return serverIP;}
	public static void setRunningValue(boolean bool){isServerRunning = bool;}
	public static ServerSocket getServerSocket(){return serverSocket;}
	private static List<ConnectedClient> getListOfAllConnectedClients() {return listOfAllActiveClients;}
	static List<ChatRoom> getListOfAllActiveChatRooms() {return listOfAllActiveChatRooms;}
	
	public static void main(String[] args){
		try{
			initialiseServer(args[0]);
			while(true){
				if(isServerRunning == false){
					shutServerDown();
				}
				takeCareOfConnection();
			}
		}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Occurred when taking in new client");
			e.printStackTrace();
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
		System.out.println(serverPort);
		serverSocket = new ServerSocket(serverPort);
		System.out.println(serverSocket);
		serverIP = InetAddress.getLocalHost().getHostAddress().toString();
		System.out.println(serverIP);
		intialiseServerVariables();
		ErrorAndPrintHandler.printString(String.format("Server has begun running on port number: %i", serverPort));
	}

	private static void intialiseServerVariables() {
		listOfAllActiveClients = new ArrayList<ConnectedClient>();
		listOfAllActiveChatRooms = new ArrayList<ChatRoom>();
		isServerRunning = true;
		nextClientId = new AtomicInteger(0);
		nextChatRoomId = new AtomicInteger(0);
	}

	private static void shutServerDown() {
		try{
			ErrorAndPrintHandler.printString("Shutting down server");
			for(ConnectedClient connectedClient : getListOfAllConnectedClients()){
				connectedClient.getSocket().close();
				connectedClient.getPrintWriter().close();
				connectedClient.getBufferedReader().close();
			}
			listOfAllActiveChatRooms.clear();
			listOfAllActiveClients.clear();
			serverSocket.close();
		}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Error Occurred when shutting down server");
		}
	}
	

	 static void removeClientFromServer(RequestTypeNode requestTypeNode, ConnectedClient connectedClient) throws IOException {
		ErrorAndPrintHandler.printString(String.format("Client: %s is leaving the chatroom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));
		if(getListOfAllConnectedClients().contains(connectedClient)){
			for(ChatRoom chatRoom : getListOfAllActiveChatRooms()){
				if(chatRoom.getListOfAllConnectedClients().contains(connectedClient)){
					chatRoom.removeClientRecord(connectedClient, requestTypeNode);
					ErrorAndPrintHandler.printString(String.format("Removes Client: %s Fom chatroom: %s", 
							connectedClient.getId(), chatRoom.getChatRoomId()));
					String message = String.format("%s has left chat room %s", requestTypeNode.getName(), chatRoom.getChatRoomId());
					String clientLeaveMessage = String.format(ResponceFromServer.CHAT.getValue(), chatRoom.getChatRoomRef(),
							requestTypeNode.getName(), message);
					chatRoom.broadcastMessageToEntireChatRoom(clientLeaveMessage);
				}
			}
		}
	}
	
	static void addClientToServer(ConnectedClient connectedClient, RequestTypeNode requestTypeNode) {
		if((requestTypeNode.getRequestType().equals(RequestType.JoinChatroom)) && (!getListOfAllConnectedClients().contains(connectedClient)) 
				&& (getChatRoomByIdIfExist(requestTypeNode.getChatRoomId()) != null)){
			for(ConnectedClient connectedClient1 : listOfAllActiveClients){
				if(connectedClient1 == connectedClient){
					ErrorAndPrintHandler.printString("Client already added to server.");
					return;
				}
			}
			getListOfAllConnectedClients().add(connectedClient);
			ErrorAndPrintHandler.printString(String.format("Client: %s added", connectedClient.getId()));
		}
	}

	static synchronized ChatRoom getChatRoomByIdIfExist(String chatRoomId) {
		for(ChatRoom chatRoom : listOfAllActiveChatRooms){
			if(chatRoom.getChatRoomId().equals(chatRoomId)){
				return chatRoom;
			}
		}
		return null;
	}
		
	public static ChatRoom getChatRoomByRefIfExist(String chatRoomRef){
		for(ChatRoom chatRoom : listOfAllActiveChatRooms){
			if(chatRoom.getChatRoomRef().equals(chatRoomRef)){
				return chatRoom;
			}
		}
		return null;
	}
	
	
}
