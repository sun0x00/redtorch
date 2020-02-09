package xyz.redtorch.common.service;

import com.google.protobuf.ByteString;

import xyz.redtorch.pb.CoreRpc.RpcId;

public interface RpcClientProcessService {
	void processData(byte[] data);

	boolean sendCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);
	
	boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);

	boolean sendLz4CoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);

	void onWsClosed();

	void onWsError();

	void onWsConnected();

	void onHeartbeat(String result);
}
