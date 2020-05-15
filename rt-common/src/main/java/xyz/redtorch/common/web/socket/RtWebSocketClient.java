package xyz.redtorch.common.web.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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

import java.io.IOException;

public class RtWebSocketClient {
	private static final Logger logger = LoggerFactory.getLogger(RtWebSocketClient.class);

	private RpcClientProcessService rpcClientProcessService;
	private String masterServerUri;
	private Boolean autoReconnect = true;
	private int nodeId;
	private String token;
	private Boolean skipTradeEvents = true;
	private String operatorId;
	private Boolean usingHttpSession = false;
	private HttpHeaders httpHeaders;

	private boolean initialized = false;

	public String getMasterServerUri() {
		return masterServerUri;
	}

	public void setMasterServerUri(String masterServerUri) {
		this.masterServerUri = masterServerUri;
	}

	public Boolean getAutoReconnect() {
		return autoReconnect;
	}

	public void setAutoReconnect(Boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getSkipTradeEvents() {
		return skipTradeEvents;
	}

	public void setSkipTradeEvents(Boolean skipTradeEvents) {
		this.skipTradeEvents = skipTradeEvents;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public Boolean getUsingHttpSession() {
		return usingHttpSession;
	}

	public void setUsingHttpSession(Boolean usingHttpSession) {
		this.usingHttpSession = usingHttpSession;
	}

	public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(HttpHeaders httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	private ExecutorService executor = Executors.newCachedThreadPool();

	private WebSocketClient webSocketClient = new StandardWebSocketClient();
	private WebSocketHandler webSocketHandler;
	private ThreadSafeWebSocketSession webSocketSession;

	private LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();
	
	private Long pingStartTimestamp = null;

	public RtWebSocketClient(RpcClientProcessService rpcClientProcessService) {
		this.rpcClientProcessService = rpcClientProcessService;
		webSocketHandler = new AbstractWebSocketHandler() {
			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				session.getHandshakeHeaders();
				logger.info("连接已建立,会话ID:{}", session.getId());
				webSocketSession = new ThreadSafeWebSocketSession(session);
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							rpcClientProcessService.onWsConnected();
						} catch (Exception e) {
							logger.error("建立连接后响应方法异常", e);
						}
					}
				});
			}

			@Override
			public void handleTextMessage(WebSocketSession session, TextMessage message) throws InterruptedException, IOException {
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
				Long delay = System.currentTimeMillis() - pingTimestamp;
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							rpcClientProcessService.onHeartbeat(delay + "ms");
						} catch (Exception e) {
							logger.error("心跳响应方法异常", e);
						}
					}
				});
				logger.info("收到PONG,延时{}ms", delay);
				pingStartTimestamp = null;
			}

			@Override
			public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
				logger.error("传输错误,会话ID:{}", session.getId(), exception);
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							rpcClientProcessService.onWsError();
						} catch (Exception e) {
							logger.error("连接错误后响应方法异常", e);
						}
					}
				});
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
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							rpcClientProcessService.onWsClosed();
						} catch (Exception e) {
							logger.error("连接关闭后响应方法异常", e);
						}
					}
				});
			}
		};
	}

	public void initialize() {
		if (!initialized) {

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

			if (autoReconnect) {
				logger.warn("自动重连服务器已启用");
			} else {
				logger.warn("自动重连服务器未启用");
			}

			scheduledExecutorService.scheduleAtFixedRate(() -> {
				try {
					
					if(pingStartTimestamp!=null) {
						if(System.currentTimeMillis()-pingStartTimestamp>21000) {
							logger.error("PING服务器超时,主动断开");
							WebSocketSession closeWebSocketSession = webSocketSession;
							webSocketSession = null;
							closeWebSocketSession.close();
							pingStartTimestamp = null;
						}
					}
					
					if (webSocketSession != null && webSocketSession.isOpen()) {
						logger.info("PING服务器");
						ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES).putLong(System.currentTimeMillis()).flip();
						PingMessage message = new PingMessage(byteBuffer);
						webSocketSession.sendMessage(message);
						if(pingStartTimestamp==null) {
							pingStartTimestamp = System.currentTimeMillis();
						}
					}
				} catch (Exception e) {
					if (rpcClientProcessService != null) {
						executor.execute(new Runnable() {
							@Override
							public void run() {
								try {
									rpcClientProcessService.onHeartbeat("ERROR");
								} catch (Exception e) {
									logger.error("心跳响应方法异常", e);
								}
							}
						});
					}
					logger.error("定时PING发生异常", e);
				}
			}, 10, 10, TimeUnit.SECONDS);

			scheduledExecutorService.scheduleWithFixedDelay(() -> {
				try {
					if (autoReconnect) {
						if (webSocketSession == null || !webSocketSession.isOpen()) {
							logger.info("自动重连服务器");
							connect();
						}
					}
				} catch (Exception e) {
					logger.error("自动重连服务器发生异常");
				}
			}, 3, 3, TimeUnit.SECONDS);

			initialized = true;
		}

	}

	public void connect() {
		try {
			if (usingHttpSession) {
				webSocketClient.doHandshake(webSocketHandler, new WebSocketHttpHeaders(httpHeaders), URI.create(masterServerUri + "?nodeId="
						+ URLEncoder.encode(nodeId + "", "utf-8") + "&skipTradeEvents=" + URLEncoder.encode(skipTradeEvents + "", "utf-8"))).get();
			} else {
				webSocketClient.doHandshake(webSocketHandler, new WebSocketHttpHeaders(),
						URI.create(masterServerUri + "?nodeId=" + URLEncoder.encode(nodeId + "", "utf-8") + "&token=" + URLEncoder.encode(token, "utf-8")
								+ "&skipTradeEvents=" + URLEncoder.encode(skipTradeEvents + "", "utf-8") + "&operatorId="
								+ URLEncoder.encode(operatorId, "utf-8")))
						.get();
			}

		} catch (Exception e) {
			logger.error("连接服务器发生错误", e);
		}
	}

	public void clsoe(CloseStatus closeStatus) {
		try {
			if (webSocketSession != null) {
				webSocketSession.close(closeStatus);
			}
		} catch (Exception e) {
			logger.error("关闭连接错误", e);
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