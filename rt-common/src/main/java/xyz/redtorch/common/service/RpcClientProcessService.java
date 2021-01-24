package xyz.redtorch.common.service;

import com.google.protobuf.ByteString;

import xyz.redtorch.pb.CoreRpc.RpcId;

public interface RpcClientProcessService {
	void processData(byte[] data);

	boolean sendRpc(RpcId rpcId, String transactionId, ByteString content);

	boolean sendAsyncHttpRpc(RpcId rpcId, String transactionId, ByteString content);
}
