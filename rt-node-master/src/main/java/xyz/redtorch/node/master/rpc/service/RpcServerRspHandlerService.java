package xyz.redtorch.node.master.rpc.service;

import xyz.redtorch.pb.CoreRpc.RpcCancelOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;

public interface RpcServerRspHandlerService {

	void onSubscribeRsp(RpcSubscribeRsp rpcSubscribeRsp);

	void onUnsubscribeRsp(RpcUnsubscribeRsp rpcUnsubscribeRsp);

	void onSubmitOrderRsp(RpcSubmitOrderRsp rpcSubmitOrderRsp);

	void onCancelOrderRsp(RpcCancelOrderRsp rpcCancelOrderRsp);

	void onSearchContractRsp(RpcSearchContractRsp rpcSearchContractRsp);

	void onExceptionRsp(RpcExceptionRsp rpcExceptionRsp);

	void registerWaitReqId(String reqId);

	void unregisterWaitReqId(String reqId);

	RpcExceptionRsp getAndRemoveRpcExceptionRsp(String reqId);

	RpcSubscribeRsp getAndRemoveRpcSubscribeRsp(String reqId);

	RpcUnsubscribeRsp getAndRemoveRpcUnsubscribeRsp(String reqId);

	RpcSubmitOrderRsp getAndRemoveRpcSubmitOrderRsp(String reqId);

	RpcCancelOrderRsp getAndRemoveRpcCancelOrderRsp(String reqId);

	RpcSearchContractRsp getAndRemoveRpcSearchContractRsp(String reqId);

}
