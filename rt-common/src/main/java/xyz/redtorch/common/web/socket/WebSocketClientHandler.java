package xyz.redtorch.common.web.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import xyz.redtorch.common.service.RpcClientProcessService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.IOException;

//@Component
public class WebSocketClientHandler implements InitializingBean {
	private final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

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
	@Value("${rt.rpc.client.skip-trade-events}")
	private String skipTradeEvents;
	@Value("${rt.node.slave.operatorId}")
	private String operatorId;

	private ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();

	private ExecutorService executor = Executors.newFixedThreadPool(1);

	private WebSocketClient webSocketClient = new StandardWebSocketClient();
	private WebSocketHandler webSocketHandler;
	private SafeSendWebSocketSession webSocketSession;

	private LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();

	public WebSocketClientHandler() {
		webSocketHandler = new AbstractWebSocketHandler() {
			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				session.getHandshakeHeaders();
				logger.info("连接已建立,会话ID:{}", session.getId());
				webSocketSession = new SafeSendWebSocketSession(session);
				rpcClientProcessService.onWsConnected();
			}

			@Override
			public void handleTextMessage(WebSocketSession session, TextMessage message)
					throws InterruptedException, IOException {
				logger.warn("接收到文本消息,会话ID:{}", session.getId());
				session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Text messages not supported"));
			}

			@Override
			protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
				ByteBuffer byteBuffer = message.getPayload();
				byte[] data = new byte[byteBuffer.remaining()];
				byteBuffer.get(data);
				dataQueue.put(data);
			}

			@Override
			protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
				Long pingTimestamp = message.getPayload().asLongBuffer().get();
				logger.info("收到PONG,延时{}ms", System.currentTimeMillis() - pingTimestamp);
			}

			@Override
			public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
				logger.error("传输错误,会话ID:{}", session.getId(), exception);
				rpcClientProcessService.onWsError();
				try {
					if (session.isOpen()) {
						session.close();
					}
				} catch (Exception e) {
					logger.error("关闭会话错误,会话ID:{}", session.getId(), exception);
				}

			}

			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
				logger.info("连接已关闭,会话ID:{}", session.getId());
				webSocketSession = null;
				rpcClientProcessService.onWsClosed();
			}
		};
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						byte[] data = dataQueue.take();
						rpcClientProcessService.processData(data);
					} catch (Exception e) {
						logger.error("数据队列处理线程发生错误", e);
					}

				}
			}
		});
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {
				if (webSocketSession != null && webSocketSession.isOpen()) {
					logger.info("PING服务器");
					ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).flip();
					PingMessage message = new PingMessage(byteBuffer);
					webSocketSession.sendMessage(message);
				}
			} catch (Exception e) {
				logger.error("定时PING发生异常", e);
			}
		}, 10, 10, TimeUnit.SECONDS);

		if (autoReconnect) {

			logger.warn("自动重连服务器已启用");
			scheduledExecutorService.scheduleWithFixedDelay(() -> {
				try {
					if (webSocketSession == null || !webSocketSession.isOpen()) {
						logger.info("自动重连服务器");
						connect();
					}
				} catch (Exception e) {
					logger.error("自动重连服务器发生异常");
				}
			}, 3, 3, TimeUnit.SECONDS);
		} else {
			logger.warn("自动重连服务器未启用");
			connect();
		}

	}

	public void connect() {
		try {
			webSocketClient.doHandshake(webSocketHandler, new WebSocketHttpHeaders(),
					URI.create(masterServerUri + "?nodeId=" + URLEncoder.encode(nodeId + "", "utf-8") + "&token="
							+ URLEncoder.encode(token, "utf-8") + "&skipTradeEvents="
							+ URLEncoder.encode("true", "utf-8") + "&operatorId="
							+ URLEncoder.encode(operatorId, "utf-8")))
					.get();
		} catch (Exception e) {
			logger.error("连接服务器发生错误", e);
		}
	}

	public boolean sendData(byte[] data) {
		if (webSocketSession == null) {
			logger.error("发送二进制数据错误,未能找到会话");
			return false;
		}
		if (!webSocketSession.isOpen()) {
			logger.error("发送二进制数据错误,会话处于关闭状态,会话ID:{}", webSocketSession.getId());
			return false;
		}
		BinaryMessage message = new BinaryMessage(data);
		try {
			webSocketSession.sendMessage(message);
			return true;
		} catch (IOException e) {
			logger.error("发送二进制数据错误,会话ID:{}", webSocketSession.getId(), e);
			return false;
		}
	}

}