package xyz.redtorch.node.slave.rpc.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.util.UuidStringPoolUtils;
import xyz.redtorch.node.slave.rpc.service.RpcClientApiService;
import xyz.redtorch.node.slave.rpc.service.RpcClientRspHandlerService;
import xyz.redtorch.pb.CoreEnum.CommonStatusEnum;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreField.CommonRtnField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcAccountListRtn;
import xyz.redtorch.pb.CoreRpc.RpcAccountRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractListRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractRtn;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcOrderListRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionRtn;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataReq;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;
import xyz.redtorch.pb.CoreRpc.RpcTickListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeRtn;

@Service
public class RpcClientApiServiceImpl implements RpcClientApiService {

	private Logger logger = LoggerFactory.getLogger(RpcClientApiServiceImpl.class);

	@Value("${rt.rpc.client.node-id}")
	private int nodeId;
	@Value("${rt.rpc.client.sync-default-timeout-seconds}")
	private int defaultRpcTimeoutSeconds;
	@Value("${rt.node.slave.operatorId}")
	private String slaveOperatorId;

	@Autowired
	private RpcClientProcessService rpcClientProcessService;

	@Autowired
	private RpcClientRspHandlerService rpcClientRspHandlerService;

	@Override
	public boolean asyncSyncSlaveNodeRuntimeData(String reqId, List<GatewayField> gatewayList) {

		if (StringUtils.isBlank(reqId)) {
			logger.error("订阅错误,参数reqId为空");
			return false;
		}

		if (gatewayList == null) {
			logger.error("订阅错误,参数gatewayList为空");
			return false;
		}

		Integer targetNodeId = 0;
		Integer sourceNodeId = nodeId;

		CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
				.setSourceNodeId(sourceNodeId) //
				.setTargetNodeId(targetNodeId) //
				.setOperatorId(slaveOperatorId) //
				.setReqId(reqId); //

		RpcSyncSlaveNodeRuntimeDataReq.Builder rpcSyncSlaveNodeRuntimeDataReqBuilder = RpcSyncSlaveNodeRuntimeDataReq
				.newBuilder();

		rpcSyncSlaveNodeRuntimeDataReqBuilder.setCommonReq(commonReqBuilder);
		rpcSyncSlaveNodeRuntimeDataReqBuilder.addAllGateway(gatewayList);

		return rpcClientProcessService.sendRoutineCoreRpc(targetNodeId,
				rpcSyncSlaveNodeRuntimeDataReqBuilder.build().toByteString(), reqId,
				RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_REQ);
	}

	@Override
	public RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(List<GatewayField> gatewayList) {
		return syncSlaveNodeRuntimeData(null, gatewayList);
	}

	@Override
	public RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(Integer timoutSeconds,
			List<GatewayField> gatewayList) {

		Integer rpcTimoutSeconds = timoutSeconds;
		if (timoutSeconds == null || timoutSeconds < 1 || timoutSeconds > 300) {
			rpcTimoutSeconds = defaultRpcTimeoutSeconds;
		}

		String reqId = UuidStringPoolUtils.getUuidString();

		long startTime = System.currentTimeMillis();

		if (asyncSyncSlaveNodeRuntimeData(reqId, gatewayList)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimoutSeconds * 1000) {
				RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp = rpcClientRspHandlerService
						.getAndRemoveSyncSlaveNodeRuntimeDataResult(reqId);
				if (rpcSyncSlaveNodeRuntimeDataRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("同步节点数据错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcSyncSlaveNodeRuntimeDataRsp.getCommonRsp();
					CommonStatusEnum requestStatus = commonRsp.getRequestStatus();
					if (requestStatus == CommonStatusEnum.SUCCESS) {
						return rpcSyncSlaveNodeRuntimeDataRsp;
					} else if (requestStatus == CommonStatusEnum.INFO) {
						logger.info("同步节点数据完成,请求ID:{},远程信息回报:{}", reqId, commonRsp.getInfo());
						return rpcSyncSlaveNodeRuntimeDataRsp;
					} else if (requestStatus == CommonStatusEnum.INFO) {
						logger.warn("同步节点数据完成,请求ID:{},远程警告回报:{}", reqId, commonRsp.getInfo());
						return rpcSyncSlaveNodeRuntimeDataRsp;
					} else if (requestStatus == CommonStatusEnum.ERROR) {
						logger.warn("同步节点数据完成,请求ID:{},远程错误回报:{}", reqId, commonRsp.getInfo());
						return null;
					} else {
						logger.warn("同步节点数据完成,请求ID:{},未知的请求状态");
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("同步节点数据错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}

	}

	@Override
	public boolean emitPositionRtn(PositionField position) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcPositionRtn.Builder rpcPositionRtnBuilder = RpcPositionRtn.newBuilder();
		rpcPositionRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcPositionRtnBuilder.setPosition(position);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId, rpcPositionRtnBuilder.build().toByteString(),
				"", RpcId.POSITION_RTN);
	}

	@Override
	public boolean emitAccountRtn(AccountField account) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcAccountRtn.Builder rpcAccountRtnBuilder = RpcAccountRtn.newBuilder();
		rpcAccountRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcAccountRtnBuilder.setAccount(account);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId, rpcAccountRtnBuilder.build().toByteString(),
				"", RpcId.ACCOUNT_RTN);
	}

	@Override
	public boolean emitContractRtn(ContractField contract) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcContractRtn.Builder rpcContractRtnBuilder = RpcContractRtn.newBuilder();
		rpcContractRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcContractRtnBuilder.setContract(contract);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId, rpcContractRtnBuilder.build().toByteString(),
				"", RpcId.CONTRACT_RTN);
	}

	@Override
	public boolean emitTickRtn(TickField tick) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcTickRtn.Builder rpcTickRtnBuilder = RpcTickRtn.newBuilder();
		rpcTickRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcTickRtnBuilder.setTick(tick);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId, rpcTickRtnBuilder.build().toByteString(), "",
				RpcId.TICK_RTN);
	}

	@Override
	public boolean emitTradeRtn(TradeField trade) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcTradeRtn.Builder rpcTradeRtnBuilder = RpcTradeRtn.newBuilder();
		rpcTradeRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcTradeRtnBuilder.setTrade(trade);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId, rpcTradeRtnBuilder.build().toByteString(),
				"", RpcId.TRADE_RTN);
	}

	@Override
	public boolean emitOrderRtn(OrderField order) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcOrderRtn.Builder rpcOrderRtnBuilder = RpcOrderRtn.newBuilder();
		rpcOrderRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcOrderRtnBuilder.setOrder(order);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId, rpcOrderRtnBuilder.build().toByteString(),
				"", RpcId.ORDER_RTN);
	}

	@Override
	public boolean emitPositionListRtn(List<PositionField> positionList) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcPositionListRtn.Builder rpcPositionListRtnBuilder = RpcPositionListRtn.newBuilder();
		rpcPositionListRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcPositionListRtnBuilder.addAllPosition(positionList);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId,
				rpcPositionListRtnBuilder.build().toByteString(), "", RpcId.POSITION_LIST_RTN);
	}

	@Override
	public boolean emitAccountListRtn(List<AccountField> accountList) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcAccountListRtn.Builder rpcAccountListRtnBuilder = RpcAccountListRtn.newBuilder();
		rpcAccountListRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcAccountListRtnBuilder.addAllAccount(accountList);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId,
				rpcAccountListRtnBuilder.build().toByteString(), "", RpcId.ACCOUNT_LIST_RTN);
	}

	@Override
	public boolean emitContractListRtn(List<ContractField> contractList) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcContractListRtn.Builder rpcContractListRtnBuilder = RpcContractListRtn.newBuilder();
		rpcContractListRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcContractListRtnBuilder.addAllContract(contractList);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId,
				rpcContractListRtnBuilder.build().toByteString(), "", RpcId.CONTRACT_LIST_RTN);
	}

	@Override
	public boolean emitTickListRtn(List<TickField> tickList) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcTickListRtn.Builder rpcTickListRtnBuilder = RpcTickListRtn.newBuilder();
		rpcTickListRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcTickListRtnBuilder.addAllTick(tickList);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId, rpcTickListRtnBuilder.build().toByteString(),
				"", RpcId.TICK_LIST_RTN);
	}

	@Override
	public boolean emitTradeListRtn(List<TradeField> tradeList) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcTradeListRtn.Builder rpcTradeListRtnBuilder = RpcTradeListRtn.newBuilder();
		rpcTradeListRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcTradeListRtnBuilder.addAllTrade(tradeList);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId,
				rpcTradeListRtnBuilder.build().toByteString(), "", RpcId.TRADE_LIST_RTN);
	}

	@Override
	public boolean emitOrderListRtn(List<OrderField> orderList) {
		Integer rtnTargetNodeId = 0;
		Integer rtnSourceNodeId = nodeId;

		CommonRtnField.Builder commonRtnBuilder = CommonRtnField.newBuilder();
		commonRtnBuilder.setTargetNodeId(rtnTargetNodeId);
		commonRtnBuilder.setSourceNodeId(rtnSourceNodeId);

		RpcOrderListRtn.Builder rpcOrderListRtnBuilder = RpcOrderListRtn.newBuilder();
		rpcOrderListRtnBuilder.setCommonRtn(commonRtnBuilder);
		rpcOrderListRtnBuilder.addAllOrder(orderList);

		return rpcClientProcessService.sendRoutineCoreRpc(rtnTargetNodeId,
				rpcOrderListRtnBuilder.build().toByteString(), "", RpcId.ORDER_LIST_RTN);
	}

}
