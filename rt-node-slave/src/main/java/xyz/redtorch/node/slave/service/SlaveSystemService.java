package xyz.redtorch.node.slave.service;

import java.util.List;

import xyz.redtorch.gateway.GatewayApi;
import xyz.redtorch.pb.CoreField.GatewaySettingField;

public interface SlaveSystemService {
	void disconnectGatewayApi(String gatewayId);

	void connectGatewayApi(GatewaySettingField gatewaySettingField);

	List<GatewayApi> getGatewayApiList();

	GatewayApi getGatewayApi(String gatewayId);

	List<String> getConnectedGatewayIdList();
}
