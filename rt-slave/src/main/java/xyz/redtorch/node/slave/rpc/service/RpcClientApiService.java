package xyz.redtorch.node.slave.rpc.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;

public interface RpcClientApiService {

	RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(List<GatewayField> gatewayList);

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

	boolean emitNotice(NoticeField notice);
}
