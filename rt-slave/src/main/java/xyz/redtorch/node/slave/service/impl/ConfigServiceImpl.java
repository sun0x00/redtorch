package xyz.redtorch.node.slave.service.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.slave.service.ConfigService;

import java.net.URI;

@Service
public class ConfigServiceImpl implements ConfigService, InitializingBean {

    @Value("${rt.slave.operatorId}")
    private String operatorId;
    @Value("${rt.slave.masterServerHostPort}")
    private String masterServerHostPort;
    @Value("${rt.slave.rpcPath}")
    private String rpcPath;
    @Value("${rt.slave.websocketPath}")
    private String websocketPath;
    @Value("${rt.slave.authToken}")
    private String authToken;
    @Value("${rt.slave.syncRuntimeDataPeriod}")
    private int syncRuntimeDataPeriod;
    @Value("${rt.slave.rpcProcessNormalThreadsNum}")
    private int rpcProcessNormalThreadsNum;

    private URI rpcUri;
    private URI websocketUri;

    @Override
    public void afterPropertiesSet() {
        rpcUri = URI.create("http://" + masterServerHostPort + rpcPath);
        websocketUri = URI.create("ws://" + masterServerHostPort + websocketPath);
    }

    @Override
    public URI getRpcURI() {
        return rpcUri;
    }

    @Override
    public URI getWebSocketURI() {
        return websocketUri;
    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public String getOperatorId() {
        return operatorId;
    }

    @Override
    public int getRpcProcessNormalThreadsNum() {
        return rpcProcessNormalThreadsNum;
    }

    @Override
    public int getSyncRuntimeDataPeriod() {
        return syncRuntimeDataPeriod;
    }
}
