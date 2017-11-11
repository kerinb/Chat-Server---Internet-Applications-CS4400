package com.company;

import java.util.List;

public class RequestTypeNode implements Comparable<RequestTypeNode>{
	private String clientName;
	private Integer joinId;
	private String chatRoomId;
	private List<String> receivedFromClient;
	
	public RequestTypeNode(String clientName, Integer joinId, String chatRoomId){
		this.clientName = clientName;
		this.joinId = joinId;
		this.chatRoomId = chatRoomId;
	}
	
	public String getName(){
		return this.clientName;
	}

	@Override
	public int compareTo(RequestTypeNode o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
