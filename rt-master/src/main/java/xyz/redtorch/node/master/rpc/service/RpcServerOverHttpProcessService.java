package xyz.redtorch.node.master.rpc.service;

public interface RpcServerOverHttpProcessService {
    byte[] processData(String sessionId, byte[] data);
}
