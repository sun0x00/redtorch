package xyz.redtorch.node.slave.rpc.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;

public interface RpcClientApiService {
	boolean asyncSyncSlaveNodeRuntimeData(String reqId, List<GatewayField> gatewayList);

	RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(List<GatewayField> gatewayList);

	RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(Integer timeoutSeconds, List<GatewayField> gatewayList);

	boolean emitPositionRtn(PositionField position);

	boolean emitAccountRtn(AccountField account);

	boolean emitContractRtn(ContractField contract);

	boolean emitTickRtn(TickField tick);

	boolean emitTradeRtn(TradeField trade);

	boolean emitOrderRtn(OrderField order);

	boolean emitPositionListRtn(List<PositionField> positionList);

	boolean emitAccountListRtn(List<AccountField> accountList);

	boolean emitContractListRtn(List<ContractField> contractList);

	boolean emitTickListRtn(List<TickField> tickList);

	boolean emitTradeListRtn(List<TradeField> tradeList);

	boolean emitOrderListRtn(List<OrderField> orderList);
}
