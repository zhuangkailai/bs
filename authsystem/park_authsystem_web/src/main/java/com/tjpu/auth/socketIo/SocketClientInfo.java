package com.tjpu.auth.socketIo;

import com.corundumstudio.socketio.SocketIOClient;

public class SocketClientInfo {
	private SocketIOClient socketIOClient;
	private String sessionId;
	private String userId;
	private String userAccount;
	private String groupId;
	public SocketIOClient getSocketIOClient() {
		return socketIOClient;
	}

	public void setSocketIOClient(SocketIOClient socketIOClient) {
		this.socketIOClient = socketIOClient;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
