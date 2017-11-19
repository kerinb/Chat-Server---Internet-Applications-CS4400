package main.java;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {
	private static ServerSocket serverSocket;
	private static volatile boolean isServerRunning;
	private static List<ChatRoom> listOfAllActiveChatRooms;
	private static List<ConnectedClient> listOfAllActiveClients;
	public static AtomicInteger nextClientId;
	public static AtomicInteger nextChatRoomId;
	static int serverPort;
	static String serverIP;
	
	public static int getServerPort(){return serverPort;}
	public static String getServerIp(){return serverIP;}
	public static void setRunningValue(boolean boolValue){isServerRunning = boolValue;}
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
		ErrorAndPrintHandler.printString("Received connection from " + socket.getInetAddress().toString());
		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);
		return socket;
	}

	private static void initialiseServer(String string) throws IOException {
		ErrorAndPrintHandler.printString("Initialising server");
		serverPort = Integer.parseInt(string);
		serverSocket = new ServerSocket(serverPort);
		serverIP = InetAddress.getLocalHost().getHostAddress().toString();
		intialiseServerVariables();
		ErrorAndPrintHandler.printString(String.format("Server has begun running on port number: %s", serverPort));
	}

	private static void intialiseServerVariables() {
		ErrorAndPrintHandler.printString("Initialising server variables");
		nextClientId = new AtomicInteger(0);
		nextChatRoomId = new AtomicInteger(0);
		listOfAllActiveClients = new ArrayList<ConnectedClient>();
		listOfAllActiveChatRooms = new ArrayList<ChatRoom>();
		isServerRunning = true;
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
			ErrorAndPrintHandler.printString("Shut down server");
		}catch(Exception e){
			ErrorAndPrintHandler.printError(e.getMessage(), "Error Occurred when shutting down server");
		}
	}
	

	 static void removeClientFromServer(RequestTypeNode requestTypeNode, ConnectedClient connectedClient) 
			 throws Exception {
		ErrorAndPrintHandler.printString(String.format("Client: %s is leaving the chatroom: %s",requestTypeNode.getName(), 
				requestTypeNode.getChatRoomId()));
		if(getListOfAllConnectedClients().contains(connectedClient)){
			for(ChatRoom chatRoom : getListOfAllActiveChatRooms()){
				
				if(chatRoom.getListOfAllConnectedClients().contains(connectedClient)){
					
					ErrorAndPrintHandler.printString(String.format("Removes Client: %s Fom chatroom: %s", 
							connectedClient.getId(), chatRoom.getChatRoomId()));
					
					String message = String.format("%s has left this chatroom", requestTypeNode.getName());
					
					String clientLeaveMessage = String.format(ResponceFromServer.CHAT.getValue(), 
							chatRoom.getChatRoomRef(), requestTypeNode.getName(), message);
					chatRoom.broadcastMessageToEntireChatRoom(clientLeaveMessage);
					chatRoom.removeClientRecord(connectedClient, requestTypeNode);
					ErrorAndPrintHandler.printString(String.format("Broadcasted message to chatroom: %s",
							chatRoom.getChatRoomId()));
				}
			}
		}
	}
	
	static void addClientToServer(ConnectedClient connectedClient, RequestTypeNode requestTypeNode) {
		ErrorAndPrintHandler.printString(String.format("Adding Client %s to server", requestTypeNode.getName()));
		if((requestTypeNode.getRequestType().equals(RequestType.JOIN_CHATROOM)) && 
				(!getListOfAllConnectedClients().contains(connectedClient)) 
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
			ErrorAndPrintHandler.printString("Room#: " + chatRoom.getChatRoomId());
			if(chatRoom.getChatRoomId().equals(chatRoomId)){
				return chatRoom;
			}
		}
		return null;
	}
		
	public static ChatRoom getChatRoomByRefIfExist(int chatRoomRef){
		ErrorAndPrintHandler.printString(String.format("Checking server if client ref: %s exists....", chatRoomRef));
		for(ChatRoom chatRoom : listOfAllActiveChatRooms){
			ErrorAndPrintHandler.printString("Chatroom: " + chatRoom.getChatRoomRef());
			if(chatRoom.getChatRoomRef().equals(chatRoomRef)){
				ErrorAndPrintHandler.printString("Chatroom found");
				return chatRoom;
			}
		}
		ErrorAndPrintHandler.printString("Chatroom not found");
		return null;
	}
	
	
}
