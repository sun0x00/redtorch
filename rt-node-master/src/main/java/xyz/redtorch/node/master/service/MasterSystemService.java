package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;

public interface MasterSystemService {
	List<Integer> getSlaveNodeIdList();

	Integer getSlaveNodeIdByGatewayId(String gatewayId);

	List<GatewaySettingField> queryGatewaySettingList(CommonReqField commonReq, List<GatewayField> gatewayList);

	void removeGatewayIdByNodeId(int nodeId);
}
