package xyz.redtorch.node.master.dao;

import java.util.List;

import xyz.redtorch.node.master.po.GatewayPo;

/**
 * 
 * @author sun0x00@gmail.com
 *
 */
public interface GatewayDao {

	GatewayPo queryGatewayByGatewayId(String gatewayId);

	List<GatewayPo> queryGatewayList();

	void upsertGatewayByGatewayId(GatewayPo gateway);

	void deleteGatewayByGatewayId(String gatewayId);
}
