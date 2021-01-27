package xyz.redtorch.desktop.service.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.desktop.service.ConfigService;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ConfigServiceImpl implements ConfigService, InitializingBean {

    @Value("#{'${rt.desktop.masterServerHostPortSet}'.split(',')}")
    private Set<String> masterServerHostPortSet;
    @Value("${rt.desktop.loginPath}")
    private String loginPath;
    @Value("${rt.desktop.rpcPath}")
    private String rpcPath;
    @Value("${rt.desktop.websocketPath}")
    private String websocketPath;
    @Value("${rt.desktop.rpcProcessNormalThreadsNum}")
    private int rpcProcessNormalThreadsNum;

    private String authToken;
    private String operatorId;
    private String priorityHostPort;

    private final Set<URI> rpcUriSet = new HashSet<>();
    private final Set<URI> websocketUriSet = new HashSet<>();
    private final Set<URI> loginUriSet = new HashSet<>();

    private final Map<String, URI> rpcUriMap = new HashMap<>();
    private final Map<String, URI> websocketUriMap = new HashMap<>();
    private final Map<String, URI> loginUriMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        for (String masterServerHostPort : masterServerHostPortSet) {
            if (priorityHostPort == null) {
                this.priorityHostPort = masterServerHostPort;
            }

            URI loginUri = URI.create("http://" + masterServerHostPort + loginPath);
            loginUriSet.add(loginUri);
            loginUriMap.put(masterServerHostPort, loginUri);

            URI rpcUri = URI.create("http://" + masterServerHostPort + rpcPath);
            rpcUriSet.add(rpcUri);
            rpcUriMap.put(masterServerHostPort, rpcUri);

            URI websocketUri = URI.create("ws://" + masterServerHostPort + websocketPath);
            websocketUriSet.add(websocketUri);
            websocketUriMap.put(masterServerHostPort, websocketUri);
        }
    }

    @Override
    public Set<URI> getRpcURISet() {
        return rpcUriSet;
    }

    @Override
    public Set<URI> getWebSocketURISet() {
        return websocketUriSet;
    }

    @Override
    public Set<URI> getLoginURISet() {
        return loginUriSet;
    }

    @Override
    public int getRpcProcessNormalThreadsNum() {
        return rpcProcessNormalThreadsNum;
    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public String getOperatorId() {
        return operatorId;
    }

    @Override
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    @Override
    public String getPriorityHostPort() {
        return priorityHostPort;
    }

    @Override
    public void setPriorityHostPort(String priorityHostPort) {
        this.priorityHostPort = priorityHostPort;
    }

    @Override
    public URI getPriorityRpcURI() {
        return rpcUriMap.get(priorityHostPort);
    }

    @Override
    public URI getPriorityPriorityLoginURI() {
        return loginUriMap.get(priorityHostPort);
    }

    @Override
    public URI getPriorityWebSocketURI() {
        return websocketUriMap.get(priorityHostPort);
    }

}
