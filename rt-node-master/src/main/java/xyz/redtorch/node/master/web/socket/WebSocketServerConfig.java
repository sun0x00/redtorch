package xyz.redtorch.node.master.web.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketServerConfig implements WebSocketConfigurer {

	@Autowired
	private WebSockeServerHandshakeInterceptor webSockeServerHandshakeInterceptor;
	@Autowired
	private WebSocketServerHandler webSocketServerHandler;

	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(webSocketServerHandler, "/websocket").setAllowedOrigins("*").addInterceptors(webSockeServerHandshakeInterceptor);
	}
}