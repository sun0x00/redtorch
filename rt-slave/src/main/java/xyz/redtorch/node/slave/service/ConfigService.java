package xyz.redtorch.node.slave.service;

import java.net.URI;

public interface ConfigService {
    URI getRpcURI();
    URI getWebSocketURI();
    String getAuthToken();
    String getOperatorId();
    int getRpcProcessNormalThreadsNum();
    int getSyncRuntimeDataPeriod();
}
