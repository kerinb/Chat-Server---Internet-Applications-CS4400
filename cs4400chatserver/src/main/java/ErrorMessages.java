package src.main.java;

public enum ErrorMessages {
	InvalidRequest(0, "Invalid request provided"), JoinChatroom(1,
			"Error occurred when attempting to join chatroom"), Chat(2,
					"Error occurred when trying to chat"), LeaveChatroom(3,
							"Error occurred when trying to leave chatroom"), KillService(4,
									"Couldn't process kill service request"), Helo(5,
											"Couldn't process hello request"), GenericFailure(6,
													"Generic failure"), NodeAlreadyExists(7,
															"Client already exists in chatroom");

	private int errorCode;
	private String description;

	ErrorMessages(final int errorCode, final String errorDescription) {
		this.errorCode = errorCode;
		this.description = errorDescription;
	}

	public int getValue() {
		return this.errorCode;
	}

	public String getDescription() {
		return this.description;
	}
}