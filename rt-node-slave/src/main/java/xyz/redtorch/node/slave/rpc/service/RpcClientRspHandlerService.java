package xyz.redtorch.node.slave.rpc.service;

import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;

public interface RpcClientRspHandlerService {
	void onSyncSlaveNodeRuntimeDataRsp(RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp);

	RpcSyncSlaveNodeRuntimeDataRsp getAndRemoveSyncSlaveNodeRuntimeDataResult(String reqId);

	void onExceptionRsp(RpcExceptionRsp rpcExceptionRsp);

	RpcExceptionRsp getAndRemoveRpcExceptionRsp(String reqId);

	void registerWaitReqId(String reqId);

	void unregisterWaitReqId(String reqId);
}
