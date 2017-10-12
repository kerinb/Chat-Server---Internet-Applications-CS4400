package com.company;

/*
 * This enum is used to create a simple identifier catalogue for the requests that can be received by the client.
 * Can be used by the server to determine the appropriate response. 
 */

public enum RequestType {
	HelloText("HELO text\n"), JoinChatroom("JOIN_CHATROOM"), Chat("CHAT"), LeaveChatroom("LEAVE_CHATROOM"), KillService(
			"KILL_SERVICE"), Disconnect("DISCONNECT");

	private String value;

	RequestType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
