package xyz.redtorch.node.master.dao;

import xyz.redtorch.node.master.po.GatewayPo;

import java.util.List;

public interface GatewayDao {
    GatewayPo queryGatewayByGatewayId(String gatewayId);

    List<GatewayPo> queryGatewayList();

    void upsertGatewayByGatewayId(GatewayPo gateway);

    void deleteGatewayByGatewayId(String gatewayId);
}
