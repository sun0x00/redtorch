package xyz.redtorch.common.web.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.UUIDStringPoolUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class RtWebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(RtWebSocketClient.class);

    private URI websocketUri;
    private String authToken;

    private final String clientId = UUIDStringPoolUtils.getUUIDString();
    private final WebSocketClient webSocketClient = new StandardWebSocketClient();
    private final WebSocketHandler webSocketHandler;
    private ThreadSafeWebSocketSession webSocketSession;

    private boolean connectedFlag = false;
    private boolean connectingFlag = false;
    private boolean authFailFlag = false;

    public RtWebSocketClient( URI websocketUri, RtWebSocketClientCallBack callBack) {
        this.websocketUri = websocketUri;

        webSocketHandler = new AbstractWebSocketHandler() {
            private void tryClose(WebSocketSession session){
                try {
                    if(session.isOpen()){
                        session.close();
                    }
                    webSocketSession = null;
                    connectingFlag = false;
                    connectedFlag = false;
                } catch (Exception e) {
                    logger.error("关闭异常,客户端ID:{},URI:{}",clientId, websocketUri.toString(), e);
                }
            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                logger.info("连接已建立,准备发起验证,客户端ID:{},URI:{},会话ID:{}", clientId, websocketUri.toString(), session.getId());
                connectingFlag = true;
                try {
                    JSONObject jsonData = new JSONObject();
                    jsonData.put(CommonConstant.KEY_AUTH_TOKEN, authToken);
                    session.sendMessage(new TextMessage(jsonData.toJSONString()));
                } catch (Exception e) {
                    logger.error("发送验证信息异常,客户端ID:{},URI:{}", clientId, websocketUri.toString(), e);
                    tryClose(session);
                }
            }

            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                try {
                    if (connectedFlag) {
                        logger.error("重复推送文本消息,客户端ID:{},URI:{},会话ID:{}", clientId, websocketUri.toString(), session.getId());
                        tryClose(session);
                    } else {
                        String data = message.getPayload();
                        if (JSON.parseObject(data).getBoolean(CommonConstant.KEY_VERIFIED)) {
                            logger.info("连接已通过验证,客户端ID:{},URI:{},会话ID:{}", clientId, websocketUri.toString(), session.getId());
                            webSocketSession = new ThreadSafeWebSocketSession(session);
                            connectedFlag = true;
                            connectingFlag = false;
                            callBack.onConnected(clientId);
                        }else{
                            authFailFlag = true;
                            logger.error("连接已通过失败,客户端ID:{},URI:{},会话ID:{}", clientId, websocketUri.toString(), session.getId());
                            tryClose(session);
                        }
                    }
                } catch (Exception e) {
                    logger.error("处理文本消息数据发生错误,客户端ID:{},URI:{},会话ID:{}", clientId, websocketUri.toString(), session.getId(),e);
                    tryClose(session);
                }
            }

            @Override
            protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
                ByteBuffer byteBuffer = message.getPayload();
                byte[] data = new byte[byteBuffer.remaining()];
                byteBuffer.get(data);
                callBack.onBinaryMessage(clientId, data);
            }

            @Override
            protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
                long pingTimestamp = message.getPayload().asLongBuffer().get();
                callBack.onPongMessage(clientId, pingTimestamp);
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                logger.error("传输错误,客户端ID:{},URI:{},会话ID:{}", clientId, websocketUri.toString(), session.getId(),exception);
                tryClose(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                logger.error("连接已关闭,客户端ID:{},URI:{},会话ID:{}", clientId, websocketUri.toString(), session.getId());
                webSocketSession = null;
                connectedFlag = false;
                connectingFlag = false;
            }
        };
    }

    public URI getWebsocketUri() {
        return websocketUri;
    }

    public void setWebsocketUri(URI websocketUri) {
        this.websocketUri = websocketUri;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authFailFlag = false;
        this.authToken = authToken;
    }

    public String getClientId(){
        return  clientId;
    }

    public long ping(){
        long pingTimestamp = System.currentTimeMillis();

        if (connectingFlag ||!connectedFlag ||webSocketSession == null) {
            logger.error("发送PING错误,会话不存在,客户端ID:{},URI:{}",clientId, websocketUri.toString());
        }else if (!webSocketSession.isOpen()) {
            logger.error("发送PING错误,会话处于关闭状态,客户端ID:{},URI:{}",clientId, websocketUri.toString());
        }
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES).putLong(pingTimestamp).flip();
            PingMessage message = new PingMessage(byteBuffer);
            webSocketSession.sendMessage(message);
        } catch (IOException e) {
            logger.error("发送PING错误,客户端ID:{},URI:{}",clientId, websocketUri.toString(),e);
        }
        return pingTimestamp;
    }

    public void connect() {
        try {
            if (!connectingFlag) {
                logger.info("开始连接,客户端ID:{},URI:{}",clientId, websocketUri.toString());
                connectingFlag = true;
                webSocketClient.doHandshake(webSocketHandler, new WebSocketHttpHeaders(), websocketUri).get();
            } else {
                logger.warn("拒绝发起新连接,仍在验证中,客户端ID:{},URI:{}",clientId, websocketUri.toString());
            }
        } catch (Exception e) {
            logger.error("发起连接错误,客户端ID:{},URI:{}",clientId, websocketUri.toString(), e);
            connectingFlag = false;
            connectedFlag = false;
        }
    }

    public void close(CloseStatus closeStatus) {
        try {
            if (webSocketSession != null) {
                webSocketSession.close(closeStatus);
            }
        } catch (Exception e) {
            logger.error("关闭连接错误,客户端ID:{},URI:{}",clientId, websocketUri.toString(), e);
        }
        webSocketSession = null;
        connectingFlag = false;
        connectedFlag = false;
    }

    public boolean isConnected(){
        return connectedFlag;
    }

    public boolean isAuthFailed(){
        return authFailFlag;
    }

    public boolean sendData(byte[] data) {
        if (connectingFlag ||!connectedFlag ||webSocketSession == null) {
            logger.error("发送二进制数据错误,会话不存在,客户端ID:{},URI:{}",clientId, websocketUri.toString());
            return false;
        }
        if (!webSocketSession.isOpen()) {
            logger.error("发送二进制数据错误,会话处于关闭状态,客户端ID:{},URI:{}",clientId, websocketUri.toString());
            return false;
        }
        BinaryMessage message = new BinaryMessage(data);
        try {
            webSocketSession.sendMessage(message);
            return true;
        } catch (IOException e) {
            logger.error("发送二进制数据错误,客户端ID:{},URI:{}",clientId, websocketUri.toString(),e);
            return false;
        }
    }

    public interface RtWebSocketClientCallBack {
        void onDisconnected(String clientId);
        void onConnected(String clientId);
        void onBinaryMessage(String clientId, byte[] data);
        void onPongMessage(String clientId, long pingTimestamp);
    }

}