package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.node.master.po.GatewayPo;

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
