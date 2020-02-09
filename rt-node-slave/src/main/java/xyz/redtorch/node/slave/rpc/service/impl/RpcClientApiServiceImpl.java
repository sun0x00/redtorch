package xyz.redtorch.node.slave.rpc.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.slave.rpc.service.RpcClientApiService;
import xyz.redtorch.node.slave.rpc.service.RpcClientRspHandlerService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
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

	private static final Logger logger = LoggerFactory.getLogger(RpcClientApiServiceImpl.class);

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

		RpcSyncSlaveNodeRuntimeDataReq.Builder rpcSyncSlaveNodeRuntimeDataReqBuilder = RpcSyncSlaveNodeRuntimeDataReq.newBuilder();

		rpcSyncSlaveNodeRuntimeDataReqBuilder.setCommonReq(commonReqBuilder);
		rpcSyncSlaveNodeRuntimeDataReqBuilder.addAllGateway(gatewayList);

		return rpcClientProcessService.sendCoreRpc(targetNodeId, rpcSyncSlaveNodeRuntimeDataReqBuilder.build().toByteString(), reqId, RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_REQ);
	}

	@Override
	public RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(List<GatewayField> gatewayList) {
		return syncSlaveNodeRuntimeData(null, gatewayList);
	}

	@Override
	public RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(Integer timeoutSeconds, List<GatewayField> gatewayList) {

		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		String reqId = UUIDStringPoolUtils.getUUIDString();

		long startTime = System.currentTimeMillis();

		if (asyncSyncSlaveNodeRuntimeData(reqId, gatewayList)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp = rpcClientRspHandlerService.getAndRemoveSyncSlaveNodeRuntimeDataResult(reqId);
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
					if (commonRsp.getErrorId() == 0) {
						logger.info("同步节点数据完成");
						return rpcSyncSlaveNodeRuntimeDataRsp;
					} else {
						logger.error("同步节点数据错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
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

		RpcPositionRtn.Builder rpcPositionRtnBuilder = RpcPositionRtn.newBuilder();
		rpcPositionRtnBuilder.setPosition(position);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcPositionRtnBuilder.build().toByteString(), "", RpcId.POSITION_RTN);
	}

	@Override
	public boolean emitAccountRtn(AccountField account) {
		RpcAccountRtn.Builder rpcAccountRtnBuilder = RpcAccountRtn.newBuilder();
		rpcAccountRtnBuilder.setAccount(account);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcAccountRtnBuilder.build().toByteString(), "", RpcId.ACCOUNT_RTN);
	}

	@Override
	public boolean emitContractRtn(ContractField contract) {
		RpcContractRtn.Builder rpcContractRtnBuilder = RpcContractRtn.newBuilder();
		rpcContractRtnBuilder.setContract(contract);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcContractRtnBuilder.build().toByteString(), "", RpcId.CONTRACT_RTN);
	}

	@Override
	public boolean emitTickRtn(TickField tick) {
		RpcTickRtn.Builder rpcTickRtnBuilder = RpcTickRtn.newBuilder();
		rpcTickRtnBuilder.setTick(tick);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcTickRtnBuilder.build().toByteString(), "", RpcId.TICK_RTN);
	}

	@Override
	public boolean emitTradeRtn(TradeField trade) {
		RpcTradeRtn.Builder rpcTradeRtnBuilder = RpcTradeRtn.newBuilder();
		rpcTradeRtnBuilder.setTrade(trade);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcTradeRtnBuilder.build().toByteString(), "", RpcId.TRADE_RTN);
	}

	@Override
	public boolean emitOrderRtn(OrderField order) {
		RpcOrderRtn.Builder rpcOrderRtnBuilder = RpcOrderRtn.newBuilder();
		rpcOrderRtnBuilder.setOrder(order);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcOrderRtnBuilder.build().toByteString(), "", RpcId.ORDER_RTN);
	}

	@Override
	public boolean emitPositionListRtn(List<PositionField> positionList) {
		RpcPositionListRtn.Builder rpcPositionListRtnBuilder = RpcPositionListRtn.newBuilder();
		rpcPositionListRtnBuilder.addAllPosition(positionList);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcPositionListRtnBuilder.build().toByteString(), "", RpcId.POSITION_LIST_RTN);
	}

	@Override
	public boolean emitAccountListRtn(List<AccountField> accountList) {
		RpcAccountListRtn.Builder rpcAccountListRtnBuilder = RpcAccountListRtn.newBuilder();
		rpcAccountListRtnBuilder.addAllAccount(accountList);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcAccountListRtnBuilder.build().toByteString(), "", RpcId.ACCOUNT_LIST_RTN);
	}

	@Override
	public boolean emitContractListRtn(List<ContractField> contractList) {
		RpcContractListRtn.Builder rpcContractListRtnBuilder = RpcContractListRtn.newBuilder();
		rpcContractListRtnBuilder.addAllContract(contractList);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcContractListRtnBuilder.build().toByteString(), "", RpcId.CONTRACT_LIST_RTN);
	}

	@Override
	public boolean emitTickListRtn(List<TickField> tickList) {
		RpcTickListRtn.Builder rpcTickListRtnBuilder = RpcTickListRtn.newBuilder();
		rpcTickListRtnBuilder.addAllTick(tickList);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcTickListRtnBuilder.build().toByteString(), "", RpcId.TICK_LIST_RTN);
	}

	@Override
	public boolean emitTradeListRtn(List<TradeField> tradeList) {
		RpcTradeListRtn.Builder rpcTradeListRtnBuilder = RpcTradeListRtn.newBuilder();
		rpcTradeListRtnBuilder.addAllTrade(tradeList);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcTradeListRtnBuilder.build().toByteString(), "", RpcId.TRADE_LIST_RTN);
	}

	@Override
	public boolean emitOrderListRtn(List<OrderField> orderList) {
		RpcOrderListRtn.Builder rpcOrderListRtnBuilder = RpcOrderListRtn.newBuilder();
		rpcOrderListRtnBuilder.addAllOrder(orderList);

		return rpcClientProcessService.sendCoreRpc(nodeId, rpcOrderListRtnBuilder.build().toByteString(), "", RpcId.ORDER_LIST_RTN);
	}

}
