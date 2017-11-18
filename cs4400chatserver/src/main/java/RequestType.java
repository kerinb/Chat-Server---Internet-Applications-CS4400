package main.java;

public enum RequestType {
	HelloText("HELO text\n"), JoinChatroom("JOIN_CHATROOM"), Chat("CHAT"), LeaveChatroom("LEAVE_CHATROOM"), KillService(
			"KILL_SERVICE\n"), Disconnect("DISCONNECT"), HELO("HELO"), Null("");

	private String value;

	RequestType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
