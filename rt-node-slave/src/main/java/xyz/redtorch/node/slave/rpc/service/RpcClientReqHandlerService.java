package xyz.redtorch.node.slave.rpc.service;

import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

public interface RpcClientReqHandlerService {
	// 提交定单
	void submitOrder(CommonReqField commonReq, SubmitOrderReqField submitOrderReq);

	// 撤销定单
	void cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq);

	// 搜寻合约
	void searchContract(CommonReqField commonReq, ContractField contract);

	void getOrderList(CommonReqField commonReq);

	void getTradeList(CommonReqField commonReq);

	void getPositionList(CommonReqField commonReq);

	void getAccountList(CommonReqField commonReq);

	void getContractList(CommonReqField commonReq);

	void getTickList(CommonReqField commonReq);
}
