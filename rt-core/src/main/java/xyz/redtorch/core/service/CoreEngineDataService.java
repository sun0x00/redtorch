package xyz.redtorch.core.service;

import java.util.List;

import xyz.redtorch.core.gateway.GatewaySetting;

/**
 * @author sun0x00@gmail.com
 */
public interface CoreEngineDataService {
	GatewaySetting queryGatewaySetting(String gatewayID);

	List<GatewaySetting> queryGatewaySettings();

	void deleteGatewaySetting(String gatewayID);

	void saveGatewaySetting(GatewaySetting gatewaySetting);
}
