package xyz.redtorch.node.master.rpc.service;

import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.*;

import java.util.List;

public interface RpcServerOverHttpReqHandlerService {


	byte[] getOrderList(CommonReqField commonReq);

	byte[] getWorkingOrderList(CommonReqField commonReq);

	byte[] queryOrderByOrderId(CommonReqField commonReq, String orderId);

	byte[] queryOrderByOriginOrderId(CommonReqField commonReq, String originOrderId);

	byte[] queryOrderListByAccountId(CommonReqField commonReq, String accountId);

	byte[] queryOrderListByUniformSymbol(CommonReqField commonReq, String uniformSymbol);

	byte[] getTradeList(CommonReqField commonReq);

	byte[] queryTradeByTradeId(CommonReqField commonReq, String tradeId);

	byte[] queryTradeListByUniformSymbol(CommonReqField commonReq, String uniformSymbol);

	byte[] queryTradeListByAccountId(CommonReqField commonReq, String accountId);

	byte[] queryTradeListByOrderId(CommonReqField commonReq, String orderId);

	byte[] queryTradeListByOriginOrderId(CommonReqField commonReq, String originOrderId);

	byte[] getPositionList(CommonReqField commonReq);

	byte[] queryPositionByPositionId(CommonReqField commonReq, String positionId);

	byte[] queryPositionListByAccountId(CommonReqField commonReq, String accountId);

	byte[] queryPositionListByUniformSymbol(CommonReqField commonReq, String uniformSymbol);

	byte[] getAccountList(CommonReqField commonReq);

	byte[] queryAccountByAccountId(CommonReqField commonReq, String accountId);

	byte[] queryAccountListByAccountCode(CommonReqField commonReq, String accountCode);

	byte[] getContractList(CommonReqField commonReq);

	byte[] queryContractByUniformSymbol(CommonReqField commonReq, String contractId);

	byte[] getTickList(String sessionId,CommonReqField commonReq);

	// ---------------------------------------------------------------------
	byte[] syncSlaveNodeRuntimeData(String sessionId, CommonReqField commonReq, List<GatewayField> gatewayList);

	byte[] queryDBBarList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType);

	byte[] queryDBTickList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String uniformSymbol, MarketDataDBTypeEnum marketDataDBType);

	byte[] queryVolumeBarList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String uniformSymbol, int volume);
}
