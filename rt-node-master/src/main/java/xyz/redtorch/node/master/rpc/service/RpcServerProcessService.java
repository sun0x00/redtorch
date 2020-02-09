package xyz.redtorch.node.master.rpc.service;

import com.google.protobuf.ByteString;

import xyz.redtorch.pb.CoreRpc.RpcId;

public interface RpcServerProcessService {
	void processData(int sourceNodeId, byte[] data);

	boolean sendCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);

	boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);

	boolean sendLz4CoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);
}
