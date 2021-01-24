package xyz.redtorch.desktop.web.socket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.web.socket.RtWebSocketClient;
import xyz.redtorch.common.web.socket.RtWebSocketClient.RtWebSocketClientCallBack;
import xyz.redtorch.desktop.layout.base.MainLayout;
import xyz.redtorch.desktop.service.ConfigService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class WebSocketClientHandler implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final Map<String, RtWebSocketClient> rtWebSocketClientMap = new HashMap<>();

    private final LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();

    @Autowired
    private RpcClientProcessService rpcClientProcessService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private MainLayout mainLayout;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void afterPropertiesSet() throws Exception {


        RtWebSocketClientCallBack callBack = new RtWebSocketClientCallBack() {
            @Override
            public void onDisconnected(String clientId) {
                mainLayout.onDisconnected();
            }

            @Override
            public void onConnected(String clientId) {
                mainLayout.onConnected();
            }

            @Override
            public void onBinaryMessage(String clientId, byte[] data) {
                dataQueue.add(data);
            }

            @Override
            public void onPongMessage(String clientId, long pingTimestamp) {
                long delay = System.currentTimeMillis() - pingTimestamp;
                logger.info("收到PING回报,客户端ID:{},延迟{}ms", clientId, delay);
                mainLayout.onHeartbeat(delay + "ms");
            }
        };

        Set<URI> webSocketURISet = configService.getWebSocketURISet();

        for (URI uri : webSocketURISet) {
            RtWebSocketClient rtWebSocketClient = new RtWebSocketClient(uri, callBack);
            rtWebSocketClientMap.put(uri.getHost() + ":" + uri.getPort(), rtWebSocketClient);
        }

        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                for (RtWebSocketClient rtWebSocketClient : rtWebSocketClientMap.values()) {
                    if (rtWebSocketClient.isConnected() && !rtWebSocketClient.isAuthFailed()) {
                        logger.info("发起PING,客户端ID:{}", rtWebSocketClient.getClientId());
                        rtWebSocketClient.ping();
                    }
                }
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    logger.error("捕获到中断", e);
                    return;
                }
            }
        });

        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {

                boolean connectedFlag = false;
                for (RtWebSocketClient rtWebSocketClient : rtWebSocketClientMap.values()) {
                    if (!rtWebSocketClient.isConnected()) {
                        String authToken = configService.getAuthToken();
                        if (StringUtils.isNotBlank(authToken)) {
                            rtWebSocketClient.setAuthToken(authToken);
                            rtWebSocketClient.connect();
                        }
                    } else {
                        connectedFlag = true;
                    }
                }

                if (connectedFlag) {
                    mainLayout.onConnected();
                } else {
                    mainLayout.onDisconnected();
                }

                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    logger.error("捕获到中断", e);
                    return;
                }
            }
        });

        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] data = dataQueue.take();
                    rpcClientProcessService.processData(data);
                } catch (InterruptedException e) {
                    logger.error("捕获到中断", e);
                    return;
                }
            }
        });


    }

    public boolean sendData(byte[] byteArray) {
        return rtWebSocketClientMap.get(configService.getPriorityHostPort()).sendData(byteArray);
    }

}
