package xyz.redtorch.desktop.service;

import java.net.URI;
import java.util.Set;

public interface ConfigService {
    Set<URI> getRpcURISet();

    Set<URI> getLoginURISet();

    Set<URI> getWebSocketURISet();

    URI getPriorityRpcURI();

    URI getPriorityPriorityLoginURI();

    URI getPriorityWebSocketURI();

    int getRpcProcessNormalThreadsNum();

    String getAuthToken();

    void setAuthToken(String authToken);

    String getOperatorId();

    void setOperatorId(String operatorId);

    String getPriorityHostPort();

    void setPriorityHostPort(String priorityHostPort);

}
