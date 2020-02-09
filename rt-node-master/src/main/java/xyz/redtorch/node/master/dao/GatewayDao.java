package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.GatewayPo;

public interface GatewayDao {
	GatewayPo queryGatewayByGatewayId(String gatewayId);

	List<GatewayPo> queryGatewayList();

	void upsertGatewayByGatewayId(GatewayPo gateway);

	void deleteGatewayByGatewayId(String gatewayId);
}
