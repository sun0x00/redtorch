package xyz.redtorch.node.master.rpc.service;

import com.google.protobuf.ByteString;

import xyz.redtorch.pb.CoreRpc.RpcId;

/**
 * 
 * @author sun0x00@gmail.com
 *
 */
public interface RpcServerProcessService {
	void processData(int sourceNodeId, byte[] data);

	boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);

	boolean sendLz4CoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId);
}
