package xyz.redtorch.node.master.service;

import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;

import java.util.List;

public interface MasterSystemService {
    List<Integer> getNodeIdList();

    GatewayField getGatewayByGatewayId(String gatewayId);

    Integer getNodeIdByGatewayId(String gatewayId);

    List<GatewaySettingField> queryGatewaySettingList(CommonReqField commonReq, List<GatewayField> gatewayList, Integer nodeId);

    void removeGatewayByNodeId(Integer nodeId);
}
