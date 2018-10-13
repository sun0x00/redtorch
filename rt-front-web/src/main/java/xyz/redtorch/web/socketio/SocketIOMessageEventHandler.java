package xyz.redtorch.web.socketio;

import com.corundumstudio.socketio.SocketIOClient;  
import com.corundumstudio.socketio.SocketIOServer;  
import com.corundumstudio.socketio.annotation.OnConnect;  
import com.corundumstudio.socketio.annotation.OnDisconnect;

import xyz.redtorch.web.service.TokenService;

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
    static ArrayList<UUID> loginClientIDList = new ArrayList<>();  
    static ArrayList<UUID> presetClientIDList = new ArrayList<>(); 

    @Autowired
    private TokenService tokenService;
    
    @Autowired  
    public SocketIOMessageEventHandler(SocketIOServer socketIOServer) {  
        this.socketIOServer = socketIOServer;  
    }  
  
    @OnConnect  
    public void onConnect(SocketIOClient client) {

        /* 分为登录客户端和预置客户端主要是性能原因。过多的事件会导致WEB网页等低性能客户端性能过低，
         * 因此上级接口可以在调用时加以缓存，批量发送，但如果使用Python等语言接入便无需考虑此问题。
         * 一般认为预置ID接入的客户端为高性能客户端，以此加以区分
         */
    	String token = client.getHandshakeData().getSingleUrlParam("token");
    	if("PRESET".equals(tokenService.getUsername(token))) {
    		presetClientIDList.add(client.getSessionId()); 
    		log.info("SoketIO客户端,SessionID-[{}],Token-[{}],已连接,认证方式为预置",client.getSessionId(),token);
    	}else {
    		loginClientIDList.add(client.getSessionId()); 
    		log.info("SoketIO客户端,SessionID-[{}],Token-[{}],已连接,认证方式为登录",client.getSessionId(),token);
    	}
    	 
       
    }  
  
    @OnDisconnect  
    public void onDisconnect(SocketIOClient client) {
    	log.info("SoketIO客户端:" + client.getSessionId() + "断开连接");
    }
  
    /**
     * 向登录客户端发送
     * @param event
     * @param obj
     */
    public void sendEventToLoginClient(String event, Object obj) {   //这里就是向客户端推消息了  
        for (UUID clientId : loginClientIDList) {  
            if (socketIOServer.getClient(clientId) == null) {
            	continue;  
            }
            socketIOServer.getClient(clientId).sendEvent(event, obj);  
        }   
    }
    
    /**
     * 向预置客户端发送
     * @param event
     * @param obj
     */
    public void sendEventToPresetClient(String event, Object obj) {   //这里就是向客户端推消息了  
    	
//    	for(SocketIOClient socketIOClient :socketIOServer.getAllClients()) {
//    		socketIOClient.sendEvent(event, obj); 
//    	}
    	
        for (UUID clientId : presetClientIDList) {  
            if (socketIOServer.getClient(clientId) == null) {
            	continue;  
            }
            socketIOServer.getClient(clientId).sendEvent(event, obj);  
        }  
    }
}