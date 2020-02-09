package xyz.redtorch.node.master.rpc.service;

import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;

public interface RpcServerApiService {
	boolean asyncSubscribe(ContractField contract, Integer targetNodeId, String reqId);

	boolean subscribe(ContractField contract, Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncUnsubscribe(ContractField contract, Integer targetNodeId, String reqId);

	boolean unsubscribe(ContractField contract, Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, Integer targetNodeId, String reqId);

	String submitOrder(SubmitOrderReqField submitOrderReq, Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, Integer targetNodeId, String reqId);

	boolean cancelOrder(CancelOrderReqField cancelOrderReq, Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncSearchContract(ContractField contract, Integer targetNodeId, String reqId);

	boolean searchContract(ContractField contract, Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncGetContractList(Integer targetNodeId, String reqId);

	RpcGetContractListRsp getContractList(Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncGetTickList(Integer targetNodeId, String reqId);

	RpcGetTickListRsp getTickList(Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncGetOrderList(Integer targetNodeId, String reqId);

	RpcGetOrderListRsp getOrderList(Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncGetPositionList(Integer targetNodeId, String reqId);

	RpcGetPositionListRsp getPositionList(Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncGetTradeList(Integer targetNodeId, String reqId);

	RpcGetTradeListRsp getTradeList(Integer targetNodeId, String reqId, Integer timeoutSeconds);

	boolean asyncGetAccountList(Integer targetNodeId, String reqId);

	RpcGetAccountListRsp getAccountList(Integer targetNodeId, String reqId, Integer timeoutSeconds);
}
