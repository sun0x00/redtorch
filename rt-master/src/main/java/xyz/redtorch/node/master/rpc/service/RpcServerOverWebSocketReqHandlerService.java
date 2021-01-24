package xyz.redtorch.node.master.rpc.service;

import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

public interface RpcServerOverWebSocketReqHandlerService {

	void subscribe(String sessionId, CommonReqField commonReq, ContractField contract);

	void unsubscribe(String sessionId, CommonReqField commonReq, ContractField contract);

	void submitOrder(String sessionId, CommonReqField commonReq, SubmitOrderReqField submitOrderReq);

	void cancelOrder(String sessionId, CommonReqField commonReq, CancelOrderReqField cancelOrderReq);

	void searchContract(String sessionId, CommonReqField commonReq, ContractField contract);

}
