package xyz.redtorch.web.socketio;

import com.corundumstudio.socketio.SocketIOClient;  
import com.corundumstudio.socketio.SocketIOServer;  
import com.corundumstudio.socketio.annotation.OnConnect;  
import com.corundumstudio.socketio.annotation.OnDisconnect;  
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Component;  
  
import java.util.ArrayList;  
import java.util.UUID;  
  

/**
 * @author sun0x00@gmail.com
 */
@Component  
public class SocketIOMessageEventHandler {  
	private Logger log = LoggerFactory.getLogger(SocketIOMessageEventHandler.class);
	
    public SocketIOServer socketIOServer;  
    static ArrayList<UUID> listClient = new ArrayList<>();  
    static final int limitSeconds = 60;  
  
    @Autowired  
    public SocketIOMessageEventHandler(SocketIOServer socketIOServer) {  
        this.socketIOServer = socketIOServer;  
    }  
  
    @OnConnect  
    public void onConnect(SocketIOClient client) {  
        listClient.add(client.getSessionId());  
        log.info("SoketIO客户端:" + client.getSessionId() + "已连接");
    }  
  
    @OnDisconnect  
    public void onDisconnect(SocketIOClient client) { 
    	log.info("SoketIO客户端:" + client.getSessionId() + "断开连接");
    }   
  
    public void sendEvent(String event, Object obj) {   //这里就是向客户端推消息了  
    	
    	for(SocketIOClient socketIOClient :socketIOServer.getAllClients()) {
    		socketIOClient.sendEvent(event, obj); 
    	}
    	
//        for (UUID clientId : listClient) {  
//            if (socketIoServer.getClient(clientId) == null) {
//            	continue;  
//            }
//            socketIoServer.getClient(clientId).sendEvent(event, obj);  
//        }  
    }
}