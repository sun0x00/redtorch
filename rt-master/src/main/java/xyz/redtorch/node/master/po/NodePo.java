package xyz.redtorch.node.master.po;

public class NodePo {

    public static final Integer NODE_STATUS_DISCONNECTED = 0;
    public static final Integer NODE_STATUS_CONNECTED = 1;

    private Integer nodeId;
    private Integer status;
    private String token;
    private String description;

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
