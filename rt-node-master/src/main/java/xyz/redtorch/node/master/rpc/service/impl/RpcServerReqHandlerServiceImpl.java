package xyz.redtorch.node.master.rpc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.common.service.MarketDataService;
import xyz.redtorch.node.master.rpc.service.RpcServerProcessService;
import xyz.redtorch.node.master.rpc.service.RpcServerReqHandlerService;
import xyz.redtorch.node.master.service.MasterSystemService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.pb.CoreEnum.BarCycleEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcQueryAccountByAccountIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryAccountListByAccountCodeRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryContractByContractIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryContractListByGatewayIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryContractListByUnifiedSymbolRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderByOriginOrderIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderByOrderIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderListByAccountIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderListByUnifiedSymbolRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryPositionByPositionIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryPositionListByUnifiedSymbolRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeByTradeIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByAccountIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByOriginOrderIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByOrderIdRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByUnifiedSymbolRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryVolumeBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetMixContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetWorkingOrderListRsp;

@Service
public class RpcServerReqHandlerServiceImpl implements RpcServerReqHandlerService {

	private static final Logger logger = LoggerFactory.getLogger(RpcServerReqHandlerServiceImpl.class);

	@Autowired
	private MasterTradeCachesService masterTradeCachesService;
	@Autowired
	private MasterTradeExecuteService masterTradeExecuteService;
	@Autowired
	private RpcServerProcessService rpcServerProcessService;
	@Autowired
	private MasterSystemService masterSystemService;
	@Autowired
	private MarketDataService marketDataService;

	@Override
	public void subscribe(CommonReqField commonReq, ContractField contract) {
		masterTradeExecuteService.subscribe(commonReq, contract);
	}

	@Override
	public void unsubscribe(CommonReqField commonReq, ContractField contract) {
		masterTradeExecuteService.unsubscribe(commonReq, contract);
	}

	@Override
	public void submitOrder(CommonReqField commonReq, SubmitOrderReqField submitOrderReq) {
		masterTradeExecuteService.submitOrder(commonReq, submitOrderReq);
	}

	@Override
	public void cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq) {
		masterTradeExecuteService.cancelOrder(commonReq, cancelOrderReq);

	}

	@Override
	public void searchContract(CommonReqField commonReq, ContractField contract) {
		masterTradeExecuteService.searchContract(commonReq, contract);

	}

	// ---------------------------------------------------------------------------------

	@Override
	public void getOrderList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<OrderField> orderList = masterTradeCachesService.getOrderList(commonReq.getOperatorId());

		if (orderList == null) {
			orderList = new ArrayList<>();
		}

		RpcGetOrderListRsp.Builder rpcGetOrderListRspBuilder = RpcGetOrderListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllOrder(orderList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetOrderListRspBuilder.build().toByteString(), reqId, RpcId.GET_ORDER_LIST_RSP);
	}

	@Override
	public void getWorkingOrderList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<OrderField> workingOrderList = masterTradeCachesService.getWorkingOrderList(commonReq.getOperatorId());

		if (workingOrderList == null) {
			workingOrderList = new ArrayList<>();
		}

		RpcGetWorkingOrderListRsp.Builder rpcGetWorkingOrderListRspBuilder = RpcGetWorkingOrderListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllOrder(workingOrderList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetWorkingOrderListRspBuilder.build().toByteString(), reqId, RpcId.GET_WORKING_ORDER_LIST_RSP);
	}

	@Override
	public void queryOrderByOrderId(CommonReqField commonReq, String orderId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		OrderField order = null;

		if (StringUtils.isBlank(orderId)) {
			logger.error("参数orderId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数orderId缺失");
		} else {
			order = masterTradeCachesService.queryOrderByOrderId(commonReq.getOperatorId(), orderId);
		}

		RpcQueryOrderByOrderIdRsp.Builder rpcQueryOrderByOrderIdRspBuilder = RpcQueryOrderByOrderIdRsp.newBuilder();
		if (order == null) {
			rpcQueryOrderByOrderIdRspBuilder.setCommonRsp(commonRspBuilder);
		} else {
			rpcQueryOrderByOrderIdRspBuilder.setCommonRsp(commonRspBuilder).setOrder(order);
		}
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryOrderByOrderIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_ORDER_BY_ORDER_ID_RSP);
	}

	@Override
	public void queryOrderByOriginOrderId(CommonReqField commonReq, String originOrderId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		OrderField order = null;

		if (StringUtils.isBlank(originOrderId)) {
			logger.error("参数originOrderId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数originOrderId缺失");
		} else {
			order = masterTradeCachesService.queryOrderByOriginOrderId(commonReq.getOperatorId(), originOrderId);
		}

		RpcQueryOrderByOriginOrderIdRsp.Builder rpcQueryOrderByOriginOrderIdRspBuilder = RpcQueryOrderByOriginOrderIdRsp.newBuilder();
		if (order == null) {
			rpcQueryOrderByOriginOrderIdRspBuilder.setCommonRsp(commonRspBuilder);
		} else {
			rpcQueryOrderByOriginOrderIdRspBuilder.setCommonRsp(commonRspBuilder).setOrder(order);
		}
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryOrderByOriginOrderIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_ORDER_BY_ORIGIN_ORDER_ID_RSP);
	}

	@Override
	public void queryOrderListByAccountId(CommonReqField commonReq, String accountId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<OrderField> orderList = null;

		if (StringUtils.isBlank(accountId)) {
			logger.error("参数accountId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数accountId缺失");
		} else {
			orderList = masterTradeCachesService.queryOrderListByAccountId(commonReq.getOperatorId(), accountId);
		}

		if (orderList == null) {
			orderList = new ArrayList<>();
		}

		RpcQueryOrderListByAccountIdRsp.Builder rpcQueryOrderListByAccountIdRspBuilder = RpcQueryOrderListByAccountIdRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllOrder(orderList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryOrderListByAccountIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_ORDER_LIST_BY_ACCOUNT_ID_RSP);
	}

	@Override
	public void queryOrderListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<OrderField> orderList = null;

		if (StringUtils.isBlank(unifiedSymbol)) {
			logger.error("参数unifiedSymbol缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数unifiedSymbol缺失");
		} else {
			orderList = masterTradeCachesService.queryOrderListByUnifiedSymbol(commonReq.getOperatorId(), unifiedSymbol);
		}
		if (orderList == null) {
			orderList = new ArrayList<>();
		}

		RpcQueryOrderListByUnifiedSymbolRsp.Builder rpcQueryOrderListByUnifiedSymbolRspBuilder = RpcQueryOrderListByUnifiedSymbolRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllOrder(orderList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryOrderListByUnifiedSymbolRspBuilder.build().toByteString(), reqId, RpcId.QUERY_ORDER_LIST_BY_UNIFIED_SYMBOL_RSP);
	}

	@Override
	public void getTradeList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<TradeField> tradeList = masterTradeCachesService.getTradeList(commonReq.getOperatorId());
		if (tradeList == null) {
			tradeList = new ArrayList<>();
		}

		RpcGetTradeListRsp.Builder rpcGetTradeListRspBuilder = RpcGetTradeListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllTrade(tradeList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetTradeListRspBuilder.build().toByteString(), reqId, RpcId.GET_TRADE_LIST_RSP);
	}

	@Override
	public void queryTradeByTradeId(CommonReqField commonReq, String tradeId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		TradeField trade = null;

		if (StringUtils.isBlank(tradeId)) {
			logger.error("参数tradeId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数tradeId缺失");
		} else {
			trade = masterTradeCachesService.queryTradeByTradeId(commonReq.getOperatorId(), tradeId);
		}

		RpcQueryTradeByTradeIdRsp.Builder rpcQueryTradeByTradeIdRspBuilder = RpcQueryTradeByTradeIdRsp.newBuilder();
		if (trade == null) {
			rpcQueryTradeByTradeIdRspBuilder.setCommonRsp(commonRspBuilder);
		} else {
			rpcQueryTradeByTradeIdRspBuilder.setCommonRsp(commonRspBuilder).setTrade(trade);
		}

		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryTradeByTradeIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_TRADE_BY_TRADE_ID_RSP);
	}

	@Override
	public void queryTradeListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<TradeField> tradeList = null;

		if (StringUtils.isBlank(unifiedSymbol)) {
			logger.error("参数unifiedSymbol缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数unifiedSymbol缺失");
		} else {
			tradeList = masterTradeCachesService.queryTradeListByUnifiedSymbol(commonReq.getOperatorId(), unifiedSymbol);
		}

		if (tradeList == null) {
			tradeList = new ArrayList<>();
		}

		RpcQueryTradeListByUnifiedSymbolRsp.Builder rpcQueryTradeListByUnifiedSymbolRspBuilder = RpcQueryTradeListByUnifiedSymbolRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllTrade(tradeList); //
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryTradeListByUnifiedSymbolRspBuilder.build().toByteString(), reqId, RpcId.QUERY_TRADE_LIST_BY_UNIFIED_SYMBOL_RSP);
	}

	@Override
	public void queryTradeListByAccountId(CommonReqField commonReq, String accountId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<TradeField> tradeList = null;

		if (StringUtils.isBlank(accountId)) {
			logger.error("参数accountId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数accountId缺失");
		} else {
			tradeList = masterTradeCachesService.queryTradeListByAccountId(commonReq.getOperatorId(), accountId);
		}

		if (tradeList == null) {
			tradeList = new ArrayList<>();
		}

		RpcQueryTradeListByAccountIdRsp.Builder rpcQueryTradeListByAccountIdRspBuilder = RpcQueryTradeListByAccountIdRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllTrade(tradeList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryTradeListByAccountIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_TRADE_LIST_BY_ACCOUNT_ID_RSP);
	}

	@Override
	public void queryTradeListByOrderId(CommonReqField commonReq, String orderId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<TradeField> tradeList = null;

		if (StringUtils.isBlank(orderId)) {
			logger.error("参数orderId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数orderId缺失");
		} else {
			tradeList = masterTradeCachesService.queryTradeListByOrderId(commonReq.getOperatorId(), orderId);
		}

		if (tradeList == null) {
			tradeList = new ArrayList<>();
		}

		RpcQueryTradeListByOrderIdRsp.Builder rpcQueryTradeListByOrderIdRspBuilder = RpcQueryTradeListByOrderIdRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllTrade(tradeList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryTradeListByOrderIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_TRADE_LIST_BY_ORDER_ID_RSP);
	}

	@Override
	public void queryTradeListByOriginOrderId(CommonReqField commonReq, String originOrderId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<TradeField> tradeList = null;

		if (StringUtils.isBlank(originOrderId)) {
			logger.error("参数originOrderId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数originOrderId缺失");
		} else {
			tradeList = masterTradeCachesService.queryTradeListByOriginOrderId(commonReq.getOperatorId(), originOrderId);
		}

		if (tradeList == null) {
			tradeList = new ArrayList<>();
		}

		RpcQueryTradeListByOriginOrderIdRsp.Builder rpcQueryTradeListByOriginOrderIdRspBuilder = RpcQueryTradeListByOriginOrderIdRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllTrade(tradeList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryTradeListByOriginOrderIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_TRADE_LIST_BY_ORIGIN_ORDER_ID_RSP);
	}

	@Override
	public void getPositionList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<PositionField> positionList = masterTradeCachesService.getPositionList(commonReq.getOperatorId());

		if (positionList == null) {
			positionList = new ArrayList<>();
		}

		RpcGetPositionListRsp.Builder rpcGetPositionListRspBuilder = RpcGetPositionListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllPosition(positionList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetPositionListRspBuilder.build().toByteString(), reqId, RpcId.GET_POSITION_LIST_RSP);
	}

	@Override
	public void queryPositionByPositionId(CommonReqField commonReq, String positionId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		PositionField position = null;

		if (StringUtils.isBlank(positionId)) {
			logger.error("参数positionId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数positionId缺失");
		} else {
			position = masterTradeCachesService.queryPositionByPositionId(commonReq.getOperatorId(), positionId);
		}

		RpcQueryPositionByPositionIdRsp.Builder rpcQueryPositionByPositionIdRsp = RpcQueryPositionByPositionIdRsp.newBuilder();
		if (position == null) {
			rpcQueryPositionByPositionIdRsp.setCommonRsp(commonRspBuilder);
		} else {
			rpcQueryPositionByPositionIdRsp.setCommonRsp(commonRspBuilder).setPosition(position);
		}
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryPositionByPositionIdRsp.build().toByteString(), reqId, RpcId.QUERY_POSITION_BY_POSITION_ID_RSP);
	}

	@Override
	public void queryPositionListByAccountId(CommonReqField commonReq, String accountId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<PositionField> positionList = null;
		if (StringUtils.isBlank(accountId)) {
			logger.error("参数accountId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数accountId缺失");
		} else {
			positionList = masterTradeCachesService.queryPositionListByAccountId(commonReq.getOperatorId(), accountId);
		}
		if (positionList == null) {
			positionList = new ArrayList<>();
		}

		RpcGetPositionListRsp.Builder rpcGetPositionListRspBuilder = RpcGetPositionListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllPosition(positionList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetPositionListRspBuilder.build().toByteString(), reqId, RpcId.QUERY_POSITION_LIST_BY_ACCOUNT_ID_RSP);
	}

	@Override
	public void queryPositionListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<PositionField> positionList = null;
		if (StringUtils.isBlank(unifiedSymbol)) {
			logger.error("参数unifiedSymbol缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数unifiedSymbol缺失");
		} else {
			positionList = masterTradeCachesService.queryPositionListByUnifiedSymbol(commonReq.getOperatorId(), unifiedSymbol);
		}
		if (positionList == null) {
			positionList = new ArrayList<>();
		}

		RpcQueryPositionListByUnifiedSymbolRsp.Builder rpcQueryPositionListByUnifiedSymbolRspBulider = RpcQueryPositionListByUnifiedSymbolRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllPosition(positionList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryPositionListByUnifiedSymbolRspBulider.build().toByteString(), reqId, RpcId.QUERY_POSITION_LIST_BY_UNIFIED_SYMBOL_RSP);
	}

	@Override
	public void getAccountList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<AccountField> accountList = masterTradeCachesService.getAccountList(commonReq.getOperatorId());

		if (accountList == null) {
			accountList = new ArrayList<>();
		}

		RpcGetAccountListRsp.Builder rpcGetAccountListRspBuilder = RpcGetAccountListRsp.newBuilder().setCommonRsp(commonRspBuilder) //
				.addAllAccount(accountList); //
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetAccountListRspBuilder.build().toByteString(), reqId, RpcId.GET_ACCOUNT_LIST_RSP);
	}

	@Override
	public void queryAccountByAccountId(CommonReqField commonReq, String accountId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		AccountField account = null;

		if (StringUtils.isBlank(accountId)) {
			logger.error("参数accountId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数accountId缺失");
		} else {
			account = masterTradeCachesService.queryAccountByAccountId(commonReq.getOperatorId(), accountId);
		}

		RpcQueryAccountByAccountIdRsp.Builder rpcQueryAccountByAccountIdRsp = RpcQueryAccountByAccountIdRsp.newBuilder();
		if (account == null) {
			rpcQueryAccountByAccountIdRsp.setCommonRsp(commonRspBuilder);
		} else {
			rpcQueryAccountByAccountIdRsp.setCommonRsp(commonRspBuilder).setAccount(account);
		}
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryAccountByAccountIdRsp.build().toByteString(), reqId, RpcId.QUERY_ACCOUNT_BY_ACCOUNT_ID_RSP);
	}

	@Override
	public void queryAccountListByAccountCode(CommonReqField commonReq, String accountCode) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<AccountField> accountList = null;
		if (StringUtils.isBlank(accountCode)) {
			logger.error("参数accountCode缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数accountCode缺失");
		} else {
			accountList = masterTradeCachesService.queryAccountListByAccountCode(commonReq.getOperatorId(), accountCode);
		}
		if (accountList == null) {
			accountList = new ArrayList<>();
		}

		RpcQueryAccountListByAccountCodeRsp.Builder rpcQueryAccountListByAccountCodeRspBuilder = RpcQueryAccountListByAccountCodeRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllAccount(accountList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryAccountListByAccountCodeRspBuilder.build().toByteString(), reqId, RpcId.QUERY_ACCOUNT_LIST_BY_ACCOUNT_CODE_RSP);
	}

	@Override
	public void getContractList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<ContractField> contractList = masterTradeCachesService.getContractList(commonReq.getOperatorId());

		if (contractList == null) {
			contractList = new ArrayList<>();
		}

		RpcGetContractListRsp.Builder rpcGetContractListRspBuilder = RpcGetContractListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllContract(contractList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetContractListRspBuilder.build().toByteString(), reqId, RpcId.GET_CONTRACT_LIST_RSP);
	}

	@Override
	public void getMixContractList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<ContractField> mixContractList = masterTradeCachesService.getMixContractList(commonReq.getOperatorId());

		if (mixContractList == null) {
			mixContractList = new ArrayList<>();
		}

		RpcGetMixContractListRsp.Builder rpcGetMixContractListRspBuilder = RpcGetMixContractListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllContract(mixContractList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetMixContractListRspBuilder.build().toByteString(), reqId, RpcId.GET_MIX_CONTRACT_LIST_RSP);
	}

	@Override
	public void queryContractByContractId(CommonReqField commonReq, String contractId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		ContractField contract = null;

		if (StringUtils.isBlank(contractId)) {
			logger.error("参数contractId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("合约ID缺失");
		} else {
			contract = masterTradeCachesService.queryContractByContractId(commonReq.getOperatorId(), contractId);
		}

		RpcQueryContractByContractIdRsp.Builder rpcQueryContractByContractIdRsp = RpcQueryContractByContractIdRsp.newBuilder();
		if (contract == null) {
			rpcQueryContractByContractIdRsp.setCommonRsp(commonRspBuilder);
		} else {
			rpcQueryContractByContractIdRsp.setCommonRsp(commonRspBuilder).setContract(contract);
		}
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryContractByContractIdRsp.build().toByteString(), reqId, RpcId.QUERY_CONTRACT_BY_CONTRACT_ID_RSP);
	}

	@Override
	public void queryContractListByUnifiedSymbol(CommonReqField commonReq, String unifiedSymbol) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<ContractField> contractList = null;
		if (StringUtils.isBlank(unifiedSymbol)) {
			logger.error("参数unifiedSymbol缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数unifiedSymbol缺失");
		} else {
			contractList = masterTradeCachesService.queryContractListByUnifiedSymbol(commonReq.getOperatorId(), unifiedSymbol);
		}
		if (contractList == null) {
			contractList = new ArrayList<>();
		}

		RpcQueryContractListByUnifiedSymbolRsp.Builder rpcQueryContractListByUnifiedSymbolRspBuilder = RpcQueryContractListByUnifiedSymbolRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllContract(contractList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryContractListByUnifiedSymbolRspBuilder.build().toByteString(), reqId, RpcId.QUERY_CONTRACT_LIST_BY_UNIFIED_SYMBOL_RSP);
	}

	@Override
	public void queryContractListByGatewayId(CommonReqField commonReq, String gatewayId) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<ContractField> contractList = null;
		if (StringUtils.isBlank(gatewayId)) {
			logger.error("参数gatewayId缺失");
			commonRspBuilder.setErrorId(1).setErrorMsg("参数gatewayId缺失");
		} else {
			contractList = masterTradeCachesService.queryContractListByUnifiedSymbol(commonReq.getOperatorId(), gatewayId);
		}
		if (contractList == null) {
			contractList = new ArrayList<>();
		}

		RpcQueryContractListByGatewayIdRsp.Builder rpcQueryContractListByGatewayIdRspBuilder = RpcQueryContractListByGatewayIdRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllContract(contractList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryContractListByGatewayIdRspBuilder.build().toByteString(), reqId, RpcId.QUERY_CONTRACT_LIST_BY_GATEWAY_ID_RSP);
	}

	@Override
	public void syncSlaveNodeRuntimeData(CommonReqField commonReq, List<GatewayField> gatewayList) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<GatewaySettingField> gatewaySettingList = masterSystemService.queryGatewaySettingList(commonReq, gatewayList);
		if (gatewaySettingList == null) {
			gatewaySettingList = new ArrayList<>();
		}

		List<ContractField> contractList = masterTradeExecuteService.getSubscribedContract();
		if (contractList == null) {
			contractList = new ArrayList<>();
		}

		RpcSyncSlaveNodeRuntimeDataRsp.Builder rpcQueryGatewaySettingListRspBuilder = RpcSyncSlaveNodeRuntimeDataRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllSubscribedContract(contractList).addAllGatewaySetting(gatewaySettingList); //
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryGatewaySettingListRspBuilder.build().toByteString(), reqId, RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_RSP);

	}

	@Override
	public void getTickList(CommonReqField commonReq) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<TickField> tickList = masterTradeCachesService.getTickList(commonReq.getOperatorId());

		int sourceNodeId = commonReq.getSourceNodeId();

		Set<String> subscribKeySet = masterTradeExecuteService.getSubscribKeySet(sourceNodeId);

		List<TickField> resultTickList = new ArrayList<>();
		if (tickList != null && subscribKeySet != null && subscribKeySet.size() > 0) {
			for (TickField tick : tickList) {
				String subscribeKey1 = tick.getUnifiedSymbol();
				String subscribeKey2 = tick.getUnifiedSymbol() + "@" + tick.getGatewayId();

				if (subscribKeySet.contains(subscribeKey1) || subscribKeySet.contains(subscribeKey2)) {
					resultTickList.add(tick);
				}

			}
		}

		RpcGetTickListRsp.Builder rpcGetTickListRspBuilder = RpcGetTickListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllTick(resultTickList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetTickListRspBuilder.build().toByteString(), reqId, RpcId.GET_TICK_LIST_RSP);
	}

	@Override
	public void queryDBBarList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String unifiedSymbol, BarCycleEnum barCycle, MarketDataDBTypeEnum marketDataDBType) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<BarField> barList = null;
		if (barList == null) {
			barList = new ArrayList<>();
		}

		if (MarketDataDBTypeEnum.MDDT_MIX.equals(marketDataDBType)) {
			if (BarCycleEnum.B_5Sec.equals(barCycle)) {
				barList = marketDataService.queryBar5SecList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_1Min.equals(barCycle)) {
				barList = marketDataService.queryBar1MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_3Min.equals(barCycle)) {
				barList = marketDataService.queryBar3MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_5Min.equals(barCycle)) {
				barList = marketDataService.queryBar5MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_15Min.equals(barCycle)) {
				barList = marketDataService.queryBar15MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_1Day.equals(barCycle)) {
				barList = marketDataService.queryBar1DayList(startTimestamp, endTimestamp, unifiedSymbol);
			}
		} else if (MarketDataDBTypeEnum.MDDT_TD.equals(marketDataDBType)) {
			if (BarCycleEnum.B_5Sec.equals(barCycle)) {
				barList = marketDataService.queryTodayBar5SecList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_1Min.equals(barCycle)) {
				barList = marketDataService.queryTodayBar1MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_3Min.equals(barCycle)) {
				barList = marketDataService.queryTodayBar3MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_5Min.equals(barCycle)) {
				barList = marketDataService.queryTodayBar5MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_15Min.equals(barCycle)) {
				barList = marketDataService.queryTodayBar15MinList(startTimestamp, endTimestamp, unifiedSymbol);
			}
		} else if (MarketDataDBTypeEnum.MDDT_HIST.equals(marketDataDBType)) {
			if (BarCycleEnum.B_5Sec.equals(barCycle)) {
				barList = marketDataService.queryHistBar5SecList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_1Min.equals(barCycle)) {
				barList = marketDataService.queryHistBar1MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_3Min.equals(barCycle)) {
				barList = marketDataService.queryHistBar3MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_5Min.equals(barCycle)) {
				barList = marketDataService.queryHistBar5MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_15Min.equals(barCycle)) {
				barList = marketDataService.queryHistBar15MinList(startTimestamp, endTimestamp, unifiedSymbol);
			} else if (BarCycleEnum.B_1Day.equals(barCycle)) {
				barList = marketDataService.queryHistBar1DayList(startTimestamp, endTimestamp, unifiedSymbol);
			}
		}

		RpcQueryDBBarListRsp.Builder rpcQueryDBBarListRspBuilder = RpcQueryDBBarListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllBar(barList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryDBBarListRspBuilder.build().toByteString(), reqId, RpcId.QUERY_DB_BAR_LIST_RSP);
	}

	@Override
	public void queryDBTickList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String unifiedSymbol, MarketDataDBTypeEnum marketDataDBType) {
		String reqId = commonReq.getReqId();

		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<TickField> tickList = null;

		if (MarketDataDBTypeEnum.MDDT_MIX.equals(marketDataDBType)) {
			tickList = marketDataService.queryTickList(startTimestamp, endTimestamp, unifiedSymbol);
		} else if (MarketDataDBTypeEnum.MDDT_TD.equals(marketDataDBType)) {
			tickList = marketDataService.queryTodayTickList(startTimestamp, endTimestamp, unifiedSymbol);
		} else if (MarketDataDBTypeEnum.MDDT_HIST.equals(marketDataDBType)) {
			tickList = marketDataService.queryHistTickList(startTimestamp, endTimestamp, unifiedSymbol);
		}

		if (tickList == null) {
			tickList = new ArrayList<>();
		}

		RpcQueryDBTickListRsp.Builder rpcQueryDBTickListRspBuilder = RpcQueryDBTickListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllTick(tickList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryDBTickListRspBuilder.build().toByteString(), reqId, RpcId.QUERY_DB_TICK_LIST_RSP);
	}

	@Override
	public void queryVolumeBarList(CommonReqField commonReq, long startTimestamp, long endTimestamp, String unifiedSymbol, int volume) {
		String reqId = commonReq.getReqId();
		int targetNodeId = commonReq.getSourceNodeId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(0);

		List<BarField> barList = null;
		List<TickField> tickList = marketDataService.queryTickList(startTimestamp, endTimestamp, unifiedSymbol);

		if (tickList != null && !tickList.isEmpty()) {
			/* PRIVATE */
			// barList = BarUtils.generateVolBar(volume, tickList);
		}
		if (barList == null) {
			barList = new ArrayList<>();
		}

		RpcQueryVolumeBarListRsp.Builder rpcQueryVolumeBarListRsp = RpcQueryVolumeBarListRsp.newBuilder() //
				.setCommonRsp(commonRspBuilder) //
				.addAllBar(barList);
		rpcServerProcessService.sendCoreRpc(targetNodeId, rpcQueryVolumeBarListRsp.build().toByteString(), reqId, RpcId.QUERY_VOLUME_BAR_LIST_RSP);
	}

}
