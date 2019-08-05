package xyz.redtorch.node.master.rpc.service;

import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

public interface RpcServerApiService {
	boolean asyncSubscribe(ContractField contract, String gatewayId, Integer targetNodeId, String reqId);

	boolean subscribe(ContractField contract, String gatewayId, Integer targetNodeId, String reqId,
			Integer timoutSeconds);

	boolean asyncUnsubscribe(ContractField contract, String gatewayId, Integer targetNodeId, String reqId);

	boolean unsubscribe(ContractField contract, String gatewayId, Integer targetNodeId, String reqId,
			Integer timoutSeconds);

	boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, Integer targetNodeId, String reqId);

	String submitOrder(SubmitOrderReqField submitOrderReq, Integer targetNodeId, String reqId, Integer timoutSeconds);

	boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, Integer targetNodeId, String reqId);

	boolean cancelOrder(CancelOrderReqField cancelOrderReq, Integer targetNodeId, String reqId, Integer timoutSeconds);

	boolean asyncSearchContract(ContractField contract, Integer targetNodeId, String reqId);

	boolean searchContract(ContractField contract, Integer targetNodeId, String reqId, Integer timoutSeconds);

}
