package xyz.redtorch.node.slave.service;

import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

public interface SlaveTradeExecuteService {
	String submitOrder(CommonReqField commonReq, SubmitOrderReqField submitOrderReq);

	boolean cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq);

	void searchContract(CommonReqField commonReq, ContractField contract);
}
