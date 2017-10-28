package com.company;

import com.sun.security.ntlm.Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
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
			initialiseServer(args[0]);
			try {
				while (!terminateServer.equals(Boolean.TRUE)) {
					handleClientConnection();
				}
			} catch (Exception e) {

                String ErrorMessage = "ERROR: occurred with client connection " + e.getMessage() + " \n OCCURRED: "
                        + ErrorHandler.getTodaysDate();
                System.out.println(ErrorMessage);
			}
		} catch (Exception e){
		    String ErrorMessage = "ERROR: encountered with server initialisation: " + e.getMessage() + " \n OCCURRED: "
                    + ErrorHandler.getTodaysDate();
			System.out.println(ErrorMessage);
		}
		finally {
			try {
				killServer(false);
			} catch (IOException e) {
                String ErrorMessage = "ERROR: encountered with killing server : " + e.getMessage() + " \n OCCURRED: "
                        + ErrorHandler.getTodaysDate();
                System.out.println(ErrorMessage);
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
		RequestType requestType = request(clientSocket);
        ClientNode clientNode = getClientInfoFromMessage(clientSocket, requestType);
        List<String> message = getMessageFromClient(clientSocket);
        ClientThread newConnectedClientThread = new ClientThread(clientNode, requestType, message);

        newConnectedClientThread.run();

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

	private static synchronized void handleClientRequest(RequestType parseRequestType) throws IOException {
		Socket clientSocket = serverSocket.accept();
        String message = "New connection received from %s..." + clientSocket.getInetAddress().toString();
        System.out.println(message);

        RequestType requestType = request(clientSocket);
        ClientNode clientNode = getClientInfoFromMessage(clientSocket, requestType);
        List<String> messages = getMessageFromClient(clientSocket);
        ClientThread newClientThread = new ClientThread(clientNode, requestType, messages);
    }

    public static RequestType request(Socket clientSocket) throws IOException {
        String request = parseRequestType(clientSocket);
        try{
            return RequestType.valueOf(request);
        }catch (Exception e){
            return null;
        }
    }

    public static ClientNode getClientInfoFromMessage(Socket clientSocket, RequestType requestType) throws IOException {
        List<String> entireMessageFromClient = getMessageFromClient(clientSocket);
        switch (requestType){
            case JoinChatroom:
                return new ClientNode(entireMessageFromClient.get(3).split(CLIENT_NAME_IDENTIFIER, 0)[1],
                        entireMessageFromClient.get(0).split(JOIN_CHATROOM_IDENTIFIER, 0)[1], clientId.getAndIncrement(),
                        clientSocket);
            case Chat:
                return new ClientNode(entireMessageFromClient.get(2).split(CLIENT_NAME_IDENTIFIER, 0)[1],
                        entireMessageFromClient.get(0).split(CHAT_IDENTIFIER, 0)[1],
                        Integer.parseInt(entireMessageFromClient.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]), clientSocket);
            case LeaveChatroom:
                return new ClientNode(entireMessageFromClient.get(2).split(CLIENT_NAME_IDENTIFIER, 0)[1],
                        entireMessageFromClient.get(0).split(LEAVE_CHATROOM_IDENTIFIER, 0)[1],
                        Integer.parseInt(entireMessageFromClient.get(1).split(JOIN_ID_IDENTIFIER, 0)[1]), clientSocket);
            case Disconnect:
                return new ClientNode(entireMessageFromClient.get(2).split(CLIENT_NAME_IDENTIFIER, 0)[1], null,
                        UNDEFINED_JOIN_ID, clientSocket);
            case HelloText:
                return new ClientNode(null, null, UNDEFINED_JOIN_ID, clientSocket);
            case KillService:
                return new ClientNode( null, null, UNDEFINED_JOIN_ID, clientSocket);
            default:
                return null;
        }
    }

    private static List<String> getMessageFromClient(Socket socket) throws IOException {
        BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        List<String> linesOfTextFromClient = new LinkedList<String>();
        String lineOfText = messageFromClient.readLine();
        while (lineOfText!= null){
            linesOfTextFromClient.add(lineOfText);
            lineOfText = messageFromClient.readLine();
        }
        return linesOfTextFromClient;
    }

	private static String parseRequestType(Socket clientSocket) throws IOException {
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String clientSentence = inFromClient.readLine();
		String[] request = clientSentence.split(":", 1);
		return request[0];
	}

	private static void initialiseServer(String portNumber) throws IOException {
		serverPort = Integer.parseInt(portNumber);
		serverSocket = new ServerSocket(serverPort);
		initialiseServerValues();
		String message = "Serveris listening on port number: %s " + portNumber;
		System.out.println(message);
	}

	private static void initialiseServerValues(){
		ListOfActiveChatRooms = new ConcurrentSkipListMap<ChatRoom, ConcurrentSkipListSet<ClientNode>>();
		terminateServer = new AtomicBoolean(Boolean.FALSE);
		clientId = new AtomicInteger(0);
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
