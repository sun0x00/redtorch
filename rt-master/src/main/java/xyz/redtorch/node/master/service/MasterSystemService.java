package xyz.redtorch.node.master.service;

import java.util.List;

import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;

public interface MasterSystemService {
	List<Integer> getNodeIdList();

	Integer getNodeIdByGatewayId(String gatewayId);

	List<GatewaySettingField> queryGatewaySettingList(CommonReqField commonReq, List<GatewayField> gatewayList, Integer nodeId);

	void removeGatewayIdByNodeId(Integer nodeId);
}
