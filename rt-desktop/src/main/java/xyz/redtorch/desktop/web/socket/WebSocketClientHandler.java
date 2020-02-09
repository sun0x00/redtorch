package xyz.redtorch.desktop.web.socket;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.web.socket.RtWebSocketClient;
import xyz.redtorch.desktop.service.AuthService;

@Component
public class WebSocketClientHandler implements InitializingBean {

	@Autowired
	private RpcClientProcessService rpcClientProcessService;

	@Value("${rt.rpc.client.master-server-uri}")
	private String masterServerUri;

	@Autowired
	private AuthService authService;

	private RtWebSocketClient rtWebSocketClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		rtWebSocketClient = new RtWebSocketClient(rpcClientProcessService);
		rtWebSocketClient.setMasterServerUri(masterServerUri);
		rtWebSocketClient.setAutoReconnect(false);
		rtWebSocketClient.setSkipTradeEvents(false);
		rtWebSocketClient.initialize();
	}

	public boolean sendData(byte[] byteArray) {
		return rtWebSocketClient.sendData(byteArray);
	}

	public void connectRtWebSocketClient() {
		if (authService.getLoginStatus() && authService.getResponseHttpHeaders() != null) {
			rtWebSocketClient.setUsingHttpSession(true);

			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Cookie", authService.getResponseHttpHeaders().getFirst("Set-Cookie"));
			rtWebSocketClient.setHttpHeaders(httpHeaders);

			rtWebSocketClient.connect();

		}
	}

	public void closeRtWebSocketClient() {
		if (rtWebSocketClient != null) {
			rtWebSocketClient.setAutoReconnect(false);
			rtWebSocketClient.clsoe(CloseStatus.NORMAL);
		}
	}

}
