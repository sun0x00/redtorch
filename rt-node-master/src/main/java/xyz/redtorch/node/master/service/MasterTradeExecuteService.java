package xyz.redtorch.node.master.service;

import java.util.List;
import java.util.Set;

import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

public interface MasterTradeExecuteService {
	void subscribe(CommonReqField commonReq, ContractField contract);

	void unsubscribe(CommonReqField commonReq, ContractField contract);

	void submitOrder(CommonReqField commonReq, SubmitOrderReqField submitOrderReq);

	void cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq);

	void searchContract(CommonReqField commonReq, ContractField contract);

	Integer getNodeIdByOrderId(String orderId);

	Integer getNodeIdByOriginOrderId(String originOrderId);

	void removeSubscribeRelationByNodeId(Integer nodeId);

	Set<Integer> getSubscribedNodeIdSet(String subscribeKey);

	Set<String> getSubscribKeySet(int nodeId);

	List<ContractField> getSubscribedContract();
	
	SubmitOrderReqField getSubmitOrderReqByOrderId(String orderId);
}
