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

	byte[] queryOrderListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol);

	byte[] getTradeList(CommonReqField commonReq);

	byte[] queryTradeByTradeId(CommonReqField commonReq, String tradeId);

	byte[] queryTradeListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol);

	byte[] queryTradeListByAccountId(CommonReqField commonReq, String accountId);

	byte[] queryTradeListByOrderId(CommonReqField commonReq, String orderId);

	byte[] queryTradeListByOriginOrderId(CommonReqField commonReq, String originOrderId);

	byte[] getPositionList(CommonReqField commonReq);

	byte[] queryPositionByPositionId(CommonReqField commonReq, String positionId);

	byte[] queryPositionListByAccountId(CommonReqField commonReq, String accountId);

	byte[] queryPositionListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol);

	byte[] getAccountList(CommonReqField commonReq);

	byte[] queryAccountByAccountId(CommonReqField commonReq, String accountId);

	byte[] queryAccountListByAccountCode(CommonReqField commonReq, String accountCode);

	byte[] getContractList(CommonReqField commonReq);

	byte[] queryContractByContractId(CommonReqField commonReq, String contractId);

	byte[] queryContractListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol);

	byte[] queryContractListByGatewayId(CommonReqField commonReq, String gatewayId);

	byte[] getMixContractList(CommonReqField commonReq);

	byte[] getTickList(String sessionId,CommonReqField commonReq);

	// ---------------------------------------------------------------------
	byte[] syncSlaveNodeRuntimeData(String sessionId, CommonReqField commonReq, List<GatewayField> gatewayList);

	byte[] queryDBBarList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String unifiedSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType);

	byte[] queryDBTickList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String unifiedSymbol, MarketDataDBTypeEnum marketDataDBType);

	byte[] queryVolumeBarList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String unifiedSymbol, int volume);
}
