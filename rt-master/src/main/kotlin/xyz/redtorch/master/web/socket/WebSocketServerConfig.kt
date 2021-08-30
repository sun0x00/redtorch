package xyz.redtorch.master.web.socket

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
open class WebSocketServerConfig : WebSocketConfigurer {

    @Autowired
    lateinit var webSocketServerHandshakeInterceptor: WebSocketServerHandshakeInterceptor

    @Autowired
    lateinit var tradeClientWebSocketHandler: TradeClientWebSocketHandler

    @Autowired
    lateinit var slaveNodeWebSocketHandler: SlaveNodeWebSocketHandler

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(tradeClientWebSocketHandler, "/ws/trade")//
            .addHandler(slaveNodeWebSocketHandler, "/ws/slave")//
            .setAllowedOrigins("*")//
            .addInterceptors(webSocketServerHandshakeInterceptor)//
    }
}