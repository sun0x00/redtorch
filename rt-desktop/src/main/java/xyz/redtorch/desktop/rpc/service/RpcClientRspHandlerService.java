package xyz.redtorch.desktop.rpc.service;

import xyz.redtorch.pb.CoreRpc.RpcCancelOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetMixContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryVolumeBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;

public interface RpcClientRspHandlerService {

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

	void onGetContractListRsp(RpcGetContractListRsp rpcGetContractListRsp);

	void onGetMixContractListRsp(RpcGetMixContractListRsp rpcGetMixContractListRsp);

	void onGetOrderListRsp(RpcGetOrderListRsp rpcGetOrderListRsp);

	void onGetTradeListRsp(RpcGetTradeListRsp rpcGetTradeListRsp);

	void onGetPositionListRsp(RpcGetPositionListRsp rpcGetPositionListRsp);

	void onGetAccountListRsp(RpcGetAccountListRsp rpcGetAccountListRsp);

	void onGetTickListRsp(RpcGetTickListRsp rpcGetTickListRsp);

	void onQueryDBBarListRsp(RpcQueryDBBarListRsp rpcQueryDBBarListRsp);

	void onQueryDBTickListRsp(RpcQueryDBTickListRsp rpcQueryDBTickListRsp);

	void onQueryVolumeBarListRsp(RpcQueryVolumeBarListRsp rpcQueryVolumeBarListRsp);

	RpcGetContractListRsp getAndRemoveRpcGetContractListRsp(String reqId);

	RpcGetMixContractListRsp getAndRemoveRpcGetMixContractListRsp(String reqId);

	RpcGetOrderListRsp getAndRemoveRpcGetOrderListRsp(String reqId);

	RpcGetTradeListRsp getAndRemoveRpcGetTradeListRsp(String reqId);

	RpcGetPositionListRsp getAndRemoveRpcGetPositionListRsp(String reqId);

	RpcGetAccountListRsp getAndRemoveRpcGetAccountListRsp(String reqId);

	RpcGetTickListRsp getAndRemoveRpcGetTickListRsp(String reqId);

	RpcQueryDBBarListRsp getAndRemoveRpcQueryDBBarListRsp(String reqId);

	RpcQueryDBTickListRsp getAndRemoveRpcQueryDBTickListRsp(String reqId);

	RpcQueryVolumeBarListRsp getAndRemoveRpcQueryVolumeBarListRsp(String reqId);

}
