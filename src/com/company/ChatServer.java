package com.company;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {

	// Note //"HELO text\nIP:[ip address]\nPort:[port number]\nStudentID:[your
	// student ID]";
	private static final int UNDEFINED_JOIN_ID = -1;
	private static final String JOIN_CHATROOM_IDENTIFIER = "JOIN_CHATROOM: ";
	private static final String CHAT_IDENTIFIER = "CHAT: ";
	private static final String LEAVE_CHATROOM_IDENTIFIER = "LEAVE_CHATROOM: ";
	private static final String JOIN_ID_IDENTIFIER = "JOIN_ID: ";
	private static final String CLIENT_NAME_IDENTIFIER = "CLIENT_NAME: ";

	private static final int SERVER_PORT = 12345;
	// Use String.format() method for HELO_RESPONSE
	private static final String HELO_RESPONSE = "HELO text\nIP:[%s]\nPort:[%s]\nStudentID:[%s]";
	private static final int STUDENT_ID = 12345678; // TODO @Breand�n change
	private static final SocketAddress SERVER_ADDRESS = null; // TODO initialise
	private static AtomicBoolean terminateServer;
	private static ServerSocket serverSocket;
	private static int serverPort;
    public static AtomicInteger clientId;
    private static String fullHelloResponse = String.format(HELO_RESPONSE, SERVER_ADDRESS, SERVER_PORT, STUDENT_ID);

	private static AtomicInteger numActiveConnections; // threadsafe
    private static ConcurrentSkipListMap<ChatRoom, ConcurrentSkipListSet<ClientNode>> ListOfActiveChatRooms;
	private static ConcurrentSkipListSet<Socket> listOfActiveClients = new ConcurrentSkipListSet<Socket>(); // threadsafe

	public static void main(String[] args) {
		try {
			initialiseServer();
			try {
				while (!terminateServer.equals(Boolean.TRUE)) {
					handleClientConnection();
				}
			} catch (Exception e) {
				System.out.println(String.format("Error encountered with client connection: %s", e.getMessage()));
			}
		} catch (Exception e){
			// TODO some sort of logging here 
			System.out.println(String.format("Error encountered with server initialisation: %s", e.getMessage()));
		}
		finally {
			try {
				killServer(false);
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		}
	}

	private static void killServer(boolean requestedFromClient) throws IOException {
		// TODO some error handling here
		for (Socket clientConnection : listOfActiveClients) {
			clientConnection.close();
		}
		// TODO revise this implementation
		listOfActiveClients.clear();
		if (requestedFromClient) {
			terminateServer = new AtomicBoolean(true);
		} else {
			serverSocket.close();
		}
	}

	private static void handleClientConnection() throws IOException {
		// accept connection and identify request type
		Socket clientSocket = serverSocket.accept();
		RequestType requestType = parseRequestType(clientSocket);
		handleClientRequest(requestType);
		updateServerRecordsIfRequired(requestType, clientSocket);
	}

	private static void updateServerRecordsIfRequired(RequestType requestType, Socket clientSocket) throws IOException {
		// Note: In case of chat, there is no change to the number of clients
		// present
		switch (requestType) {
		case JoinChatroom:
			addNewClientRecord(clientSocket);
			break;
		case HelloText:
			sendHelloResponse(clientSocket);
			break;
		case LeaveChatroom:
			removeClientFromChatroom(clientSocket);
			break;
		case Disconnect:
			removeClientRecord(clientSocket);
			break;
		case KillService:
			killServer(true);
		default:
			// TODO check if there are any edge cases to address
			break;
		}
	}

    static synchronized void updateClientListing(RequestType requestType, ClientNode clientNode){
	    if(clientNode!=null){
	        if(requestType.equals(RequestType.JoinChatroom) && !getAllActiveChatRooms().values().contains(clientNode)){
                addClientToServer(clientNode);
            }else if( requestType.equals(RequestType.Disconnect) && getAllActiveChatRooms().values().contains(clientNode)){
	            removeClientFromServer(clientNode, getRequestedChatRoomIfIsThere(clientNode.getChatRoomId()));
            }
        }
    }

    private static void addClientToServer(ClientNode clientNode){

    }

    private static void removeClientFromServer(ClientNode clientNode, ChatRoom chatRoom){
        
    }

	private static void removeClientRecord(Socket clientSocket) {
		if (clientSocket != null && listOfActiveClients.contains(clientSocket)) {
			listOfActiveClients.remove(clientSocket);
			numActiveConnections.getAndDecrement();
		}
	}

	private static void removeClientFromChatroom(Socket clientSocket) {
		// TODO remove the specified client from the current chatroom

	}

	private static void sendHelloResponse(Socket clientSocket) throws IOException {
		writeStringToClientSocket(clientSocket, fullHelloResponse);
	}

	private static void writeStringToClientSocket(Socket clientSocket, String response) throws IOException {
		DataOutputStream outputToClient = new DataOutputStream(clientSocket.getOutputStream());
		outputToClient.writeBytes(response);
	}

	private static void addNewClientRecord(Socket clientSocket) {
		if (clientSocket != null && !listOfActiveClients.contains(clientSocket)) {
			listOfActiveClients.add(clientSocket);
			numActiveConnections.getAndIncrement();
		}
	}

	private static void handleClientRequest(RequestType parseRequestType) {
		// TODO @Breand�n
		// Based on the type, we should either create a new thread or not
		// Depending on the type of request, may need different threads
	}

	private static RequestType parseRequestType(Socket clientSocket) throws IOException {
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String clientSentence = inFromClient.readLine();
		String[] request = clientSentence.split(":", 1);
		return RequestType.valueOf(request[0]);
	}

	private static void initialiseServer() throws IOException {
		terminateServer = new AtomicBoolean(false);
		serverSocket.bind(SERVER_ADDRESS, SERVER_PORT);
		// TODO any other initialisation		
	}

	public static int getServerPortNumber() {
		return serverPort;
	}

	public static void killChatService(AtomicBoolean atomicBoolean) {
		terminateServer = atomicBoolean;
	}

	public static ChatRoom getRequestedChatRoomIfIsThere(String ChatRoomToJoin){
	    for(Map.Entry<ChatRoom, ConcurrentSkipListSet<ClientNode>> entry : ListOfActiveChatRooms.entrySet()){
	        if(entry.getKey().getChatRoomId() == ChatRoomToJoin)
	            return entry.getKey();
        }
        return null;
    }

    public static ConcurrentSkipListMap<ChatRoom, ConcurrentSkipListSet<ClientNode>> getAllActiveChatRooms() {
        return ListOfActiveChatRooms;
    }
}
