package xyz.redtorch.node.slave.service;

import xyz.redtorch.pb.CoreField.*;

import java.util.List;

public interface SlaveTradeCachesService {
    void removeAllCachesByGatewayId(String gatewayId);

    OrderField queryOrderByOrderId(String orderId);

    List<OrderField> getOrderList();

    List<TradeField> getTradeList();

    List<PositionField> getPositionList();

    List<AccountField> getAccountList();

    List<ContractField> getContractList();

    List<TickField> getTickList();
}
