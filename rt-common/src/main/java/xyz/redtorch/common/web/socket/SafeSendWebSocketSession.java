package xyz.redtorch.common.web.socket;

import java.io.IOException;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class SafeSendWebSocketSession {
	private WebSocketSession webSocketSession;

	public SafeSendWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}

	public synchronized void sendMessage(WebSocketMessage<?> message) throws IOException {
		webSocketSession.sendMessage(message);
	}

	public void close() throws IOException {
		webSocketSession.close();
	}

	public boolean isOpen() {
		return webSocketSession.isOpen();
	}

	public String getId() {
		return webSocketSession.getId();
	}

	public WebSocketSession getWebSocketSession() {
		return this.webSocketSession;
	}

	public void close(CloseStatus status) throws IOException {
		webSocketSession.close(status);
	}
}
