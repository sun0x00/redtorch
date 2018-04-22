package xyz.redtorch.web.socketio;

import com.corundumstudio.socketio.SocketIOServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.boot.CommandLineRunner;  
import org.springframework.stereotype.Component;  

/**
 * @author sun0x00@gmail.com
 */
@Component
public class SocketIOCommandLineRunner implements CommandLineRunner {
	
	private Logger log = LoggerFactory.getLogger(SocketIOCommandLineRunner.class);

    private final SocketIOServer socketIOServer;  
    
    
    @Autowired  
    public SocketIOCommandLineRunner(SocketIOServer socketIOServer) {  
        this.socketIOServer = socketIOServer;  
    }  
  
  
    @Override  
    public void run(String... args) throws Exception {  
    	socketIOServer.start();  
        log.info("SocketIO启动成功！");  
    }  

}
