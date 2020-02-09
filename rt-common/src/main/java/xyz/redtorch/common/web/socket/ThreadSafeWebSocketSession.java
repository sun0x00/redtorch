package xyz.redtorch.common.web.socket;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

public class ThreadSafeWebSocketSession extends ConcurrentWebSocketSessionDecorator{

	public ThreadSafeWebSocketSession(WebSocketSession session) {
		super(session, 30*1000, Integer.MAX_VALUE);
	}

}

//import java.io.IOException;
//
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.WebSocketMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//public class ThreadSafeWebSocketSession {
//	private WebSocketSession session;
//	public ThreadSafeWebSocketSession(WebSocketSession session) {
//		this.session = session;
//	}
//	public synchronized void sendMessage(WebSocketMessage<?> message) throws IOException {
//		session.sendMessage(message);
//	}
//	public String getId() {
//		return session.getId();
//	}
//	public boolean isOpen() {
//		return session.isOpen();
//	}
//	public void close(CloseStatus closeStatus) throws IOException {
//		session.close(closeStatus);
//	}
//
//}


