package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.GatewayPo;

import java.util.List;

public interface GatewayService {
    GatewayPo getGatewayByGatewayId(String gatewayId);

    void upsertGatewayByGatewayId(GatewayPo gateway);

    List<GatewayPo> getGatewayList();

    void deleteGatewayByGatewayId(String gatewayId);

    void connectGatewayByGatewayId(String gatewayId);

    void disconnectGatewayByGatewayId(String gatewayId);

    void disconnectAllGateways();

    void connectAllGateways();
}
