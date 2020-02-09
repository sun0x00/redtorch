package xyz.redtorch.node.slave.web.socket;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.web.socket.RtWebSocketClient;

@Component
public class WebSocketClientHandler implements InitializingBean {

	@Autowired
	private RpcClientProcessService rpcClientProcessService;

	@Value("${rt.rpc.client.master-server-uri}")
	private String masterServerUri;
	@Value("#{new Boolean('${rt.rpc.client.auto-reconnect-to-master}')}")
	private Boolean autoReconnect;
	@Value("${rt.rpc.client.node-id}")
	private int nodeId;
	@Value("${rt.rpc.client.token}")
	private String token;
	@Value("#{new Boolean('${rt.rpc.client.skip-trade-events}')}")
	private Boolean skipTradeEvents;
	@Value("${rt.node.slave.operatorId}")
	private String operatorId;

	RtWebSocketClient rtWebSocketClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		rtWebSocketClient = new RtWebSocketClient(rpcClientProcessService);
		rtWebSocketClient.setMasterServerUri(masterServerUri);
		rtWebSocketClient.setAutoReconnect(autoReconnect);
		rtWebSocketClient.setNodeId(nodeId);
		rtWebSocketClient.setToken(token);
		rtWebSocketClient.setSkipTradeEvents(skipTradeEvents);
		rtWebSocketClient.setOperatorId(operatorId);
		rtWebSocketClient.initialize();
	}

	public boolean sendData(byte[] byteArray) {
		return rtWebSocketClient.sendData(byteArray);
	}

}
