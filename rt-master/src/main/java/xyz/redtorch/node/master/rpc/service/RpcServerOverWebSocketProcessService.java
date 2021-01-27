package xyz.redtorch.node.master.rpc.service;

import com.google.protobuf.ByteString;

import xyz.redtorch.pb.CoreRpc.RpcId;

public interface RpcServerOverWebSocketProcessService {
	void processData(String sessionId, byte[] data);
	boolean sendCoreRpc(String sessionId, RpcId rpcId, String transactionId, ByteString content);
}
