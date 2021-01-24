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
	boolean asyncSubscribe(ContractField contract, String sessionId, String transactionId);

	boolean subscribe(ContractField contract, String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncUnsubscribe(ContractField contract, String sessionId, String transactionId);

	boolean unsubscribe(ContractField contract, String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, String sessionId, String transactionId);

	String submitOrder(SubmitOrderReqField submitOrderReq, String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, String sessionId, String transactionId);

	boolean cancelOrder(CancelOrderReqField cancelOrderReq, String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncSearchContract(ContractField contract, String sessionId, String transactionId);

	boolean searchContract(ContractField contract, String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncGetContractList(String sessionId, String transactionId);

	RpcGetContractListRsp getContractList(String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncGetTickList(String sessionId, String transactionId);

	RpcGetTickListRsp getTickList(String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncGetOrderList(String sessionId, String transactionId);

	RpcGetOrderListRsp getOrderList(String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncGetPositionList(String sessionId, String transactionId);

	RpcGetPositionListRsp getPositionList(String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncGetTradeList(String sessionId, String transactionId);

	RpcGetTradeListRsp getTradeList(String sessionId, String transactionId, Integer timeoutSeconds);

	boolean asyncGetAccountList(String sessionId, String transactionId);

	RpcGetAccountListRsp getAccountList(String sessionId, String transactionId, Integer timeoutSeconds);
}
