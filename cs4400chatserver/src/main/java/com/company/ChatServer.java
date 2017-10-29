package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {

	// Note //"HELO text\nIP:[ip address]\nPort:[port number]\nStudentID:[your
	// student ID]";
	private static final int UNDEFINED_JOIN_ID = -1;
	private static int serverPort;

	private static final String JOIN_CHATROOM_IDENTIFIER = "JOIN_CHATROOM: ";
	private static final String CHAT_IDENTIFIER = "CHAT: ";
	private static final String LEAVE_CHATROOM_IDENTIFIER = "LEAVE_CHATROOM: ";
	private static final String JOIN_ID_IDENTIFIER = "JOIN_ID: ";
	private static final String CLIENT_NAME_IDENTIFIER = "CLIENT_NAME: ";

	private static AtomicBoolean terminateServer;

	public static AtomicInteger clientId;

	private static ServerSocket serverSocket;

	private static ConcurrentSkipListMap<ChatRoom, ConcurrentSkipListSet<ClientNode>> ListOfActiveChatRooms;

	// NOTE: args[0] is port number
	public static void main(String[] args) {
		try {
			initialiseServer(args[0]);
			try {
				handleClientConnection();
			} catch (Exception e) {
				ErrorHandler.printError(e.getMessage(), " occurred with client connection: ");
			}
		} catch (Exception e) {
			ErrorHandler.printError(e.getMessage(), " encountered with server initialisation: ");

		} finally {
			try {
				killServer();
			} catch (IOException e) {
				ErrorHandler.printError(e.getMessage(), " encountered with killing server: ");
			}
		}
	}

	private static void initialiseServer(String portNumber) throws IOException {
		serverPort = Integer.parseInt(portNumber);
		serverSocket = new ServerSocket(serverPort);
		initialiseServerValues();
		String message = "Server is listening on port number: %s " + portNumber;
		System.out.println(message);
	}

	private static void initialiseServerValues() {
		ListOfActiveChatRooms = new ConcurrentSkipListMap<ChatRoom, ConcurrentSkipListSet<ClientNode>>();
		setTerminateServer(new AtomicBoolean(Boolean.FALSE));
		clientId = new AtomicInteger(0);
	}

	private static void killServer() throws IOException {
		try {
			System.out.println("Killing Server......");
			for (Map.Entry<ChatRoom, ConcurrentSkipListSet<ClientNode>> entry : getAllActiveChatRooms().entrySet()) {
				for (ClientNode clientNode : entry.getValue()) {
					clientNode.getConnection().close();
				}
			}
			getAllActiveChatRooms().clear();
			serverSocket.close();
		} catch (Exception e) {
			ErrorHandler.printError(e.getMessage(), " occurred when shutting down server: ");
		}
	}

	public static ClientNode getClientInfoFromMessage(Socket clientSocket, RequestType requestType) throws IOException {
		List<String> entireMessageFromClient = getMessageFromClient(clientSocket);
		switch (requestType) {
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
			return new ClientNode(null, null, UNDEFINED_JOIN_ID, clientSocket);
		default:
			return null;
		}
	}

	private static List<String> getMessageFromClient(Socket socket) throws IOException {
		BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		List<String> linesOfTextFromClient = new LinkedList<String>();
		String lineOfText = messageFromClient.readLine();
		while (lineOfText != null) {
			linesOfTextFromClient.add(lineOfText);
			lineOfText = messageFromClient.readLine();
		}
		return linesOfTextFromClient;
	}

	private static void handleClientConnection() throws IOException {
		// accept connection and identify request type
		Socket clientSocket = serverSocket.accept();
		RequestType requestType = request(clientSocket);
		ClientNode clientNode = getClientInfoFromMessage(clientSocket, requestType);
		List<String> message = getMessageFromClient(clientSocket);
		ClientThread newConnectedClientThread = new ClientThread(clientNode, requestType, message);

		newConnectedClientThread.run();
		updateClientListing(requestType, clientNode);
	}

	public static void updateClientListing(RequestType requestType, ClientNode clientNode) {
		if (clientNode != null) {
			if (requestType.equals(RequestType.JoinChatroom)
					&& !getAllActiveChatRooms().values().contains(clientNode)) {
				addClientToServer(clientNode);
			} else if (requestType.equals(RequestType.Disconnect)
					&& getAllActiveChatRooms().values().contains(clientNode)) {
				removeClientFromServer(clientNode, getRequestedChatRoomIfIsThere(clientNode.getChatRoomId()));
			}
		}
	}

	public static RequestType request(Socket clientSocket) throws IOException {
		String request = parseRequestType(clientSocket);
		try {
			return RequestType.valueOf(request);
		} catch (Exception e) {
			ErrorHandler.printError(e.getMessage(), " occurred when getting client request: ");
			return null;
		}
	}

	private static String parseRequestType(Socket clientSocket) throws IOException {
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String clientSentence = inFromClient.readLine();
		String[] request = clientSentence.split(":", 1);
		return request[0];
	}

	private static void addClientToServer(ClientNode clientNode) {
		for (Map.Entry<ChatRoom, ConcurrentSkipListSet<ClientNode>> entry : getAllActiveChatRooms().entrySet()) {
			if (Objects.equals(entry.getKey().getChatRoomId(), clientNode.getChatRoomId())) {
				if (!entry.getValue().contains(clientNode)) {
					entry.getValue().add(clientNode);
					return;
				}
			}
		}
	}

	private static void removeClientFromServer(ClientNode clientNode, ChatRoom chatRoom) {
		for (Map.Entry<ChatRoom, ConcurrentSkipListSet<ClientNode>> entry : getAllActiveChatRooms().entrySet()) {
			if (entry.getKey() == chatRoom) {
				entry.getValue().remove(clientNode);
				try {
					clientNode.getConnection().close();
				} catch (IOException e) {
					ErrorHandler.printError(e.getMessage(), " occurred with removing client connection: ");
				}
				return;
			}
		}
	}

	public static ConcurrentSkipListMap<ChatRoom, ConcurrentSkipListSet<ClientNode>> getAllActiveChatRooms() {
		return ListOfActiveChatRooms;
	}

	public static ChatRoom getRequestedChatRoomIfIsThere(String ChatRoomToJoin) {
		for (Map.Entry<ChatRoom, ConcurrentSkipListSet<ClientNode>> entry : ListOfActiveChatRooms.entrySet()) {
			if (Objects.equals(entry.getKey().getChatRoomId(), ChatRoomToJoin))
				return entry.getKey();
		}
		return null;
	}

	public static int getServerPortNumber() {
		return serverPort;
	}

	public static synchronized ConcurrentSkipListSet<ClientNode> getAllConnectedClients() {
		ConcurrentSkipListSet<ClientNode> allClients = new ConcurrentSkipListSet<ClientNode>();
		for (ConcurrentSkipListSet<ClientNode> clients : ListOfActiveChatRooms.values()) {
			allClients.addAll(clients);
		}
		return allClients;
	}

	public static void killChatService(AtomicBoolean atomicBoolean) {
		setTerminateServer(atomicBoolean);
	}

	public static AtomicBoolean getTerminateServer() {
		return terminateServer;
	}

	public static void setTerminateServer(AtomicBoolean terminateServer) {
		ChatServer.terminateServer = terminateServer;
	}
}
