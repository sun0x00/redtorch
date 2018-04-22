package xyz.redtorch.trader.engine.main;

import java.util.List;

import xyz.redtorch.trader.gateway.GatewaySetting;
/**
 * @author sun0x00@gmail.com
 */
public interface MainDataUtil {
	GatewaySetting queryGatewaySetting(String gatewayID);

	List<GatewaySetting> queryGatewaySettings();

	void deleteGatewaySetting(String gatewayID);

	void saveGatewaySetting(GatewaySetting gatewaySetting);
}
