package xyz.redtorch.node.slave.web.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.web.socket.RtWebSocketClient;
import xyz.redtorch.common.web.socket.RtWebSocketClient.RtWebSocketClientCallBack;
import xyz.redtorch.node.slave.service.ConfigService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class WebSocketClientHandler implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private RtWebSocketClient rtWebSocketClient;

    private final LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();

    @Autowired
    private RpcClientProcessService rpcClientProcessService;
    @Autowired
    private ConfigService configService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void afterPropertiesSet() {
        RtWebSocketClientCallBack callBack = new RtWebSocketClientCallBack() {
            @Override
            public void onDisconnected(String clientId) {

            }

            @Override
            public void onConnected(String clientId) {

            }

            @Override
            public void onBinaryMessage(String clientId, byte[] data) {
                dataQueue.add(data);
            }

            @Override
            public void onPongMessage(String clientId, long pingTimestamp) {
                long delay = System.currentTimeMillis() - pingTimestamp;
                logger.info("收到PING回报,客户端ID:{},延迟{}ms", clientId, delay);
            }
        };

        rtWebSocketClient = new RtWebSocketClient(configService.getWebSocketURI(), callBack);
        rtWebSocketClient.setAuthToken(configService.getAuthToken());

        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (rtWebSocketClient.isConnected() && !rtWebSocketClient.isAuthFailed()) {
                    logger.info("发起PING,客户端ID:{}", rtWebSocketClient.getClientId());
                    rtWebSocketClient.ping();
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

                if (!rtWebSocketClient.isConnected()) {
                    rtWebSocketClient.connect();
                }
                try {
                    Thread.sleep(5 * 1000);
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
        return rtWebSocketClient.sendData(byteArray);
    }

}
