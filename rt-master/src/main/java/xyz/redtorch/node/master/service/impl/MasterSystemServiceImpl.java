package xyz.redtorch.node.master.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.master.dao.GatewayDao;
import xyz.redtorch.node.master.po.GatewayPo;
import xyz.redtorch.node.master.service.MasterSystemService;
import xyz.redtorch.pb.CoreEnum.ConnectStatusEnum;
import xyz.redtorch.pb.CoreEnum.GatewayAdapterTypeEnum;
import xyz.redtorch.pb.CoreEnum.GatewayTypeEnum;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;
import xyz.redtorch.pb.CoreField.GatewaySettingField.CtpApiSettingField;
import xyz.redtorch.pb.CoreField.GatewaySettingField.IbApiSettingField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class MasterSystemServiceImpl implements MasterSystemService {

    private static final Logger logger = LoggerFactory.getLogger(MasterSystemServiceImpl.class);
    private final Lock gatewayIdNodeIdMapLock = new ReentrantLock();
    @Autowired
    private GatewayDao gatewayDao;
    @Value("${rt.slave.operatorId}")
    private String slaveOperatorId;
    private Map<String, Integer> gatewayIdNodeIdMap = new HashMap<>();

    private Map<String, GatewayField> gatewayMap = new HashMap<>(5000);

    @Override
    public GatewayField getGatewayByGatewayId(String gatewayId){
        return gatewayMap.get(gatewayId);
    }

    @Override
    public Integer getNodeIdByGatewayId(String gatewayId) {
        return gatewayIdNodeIdMap.get(gatewayId);
    }

    @Override
    public void removeGatewayByNodeId(Integer nodeId) {

        gatewayMap = gatewayMap.entrySet().stream().filter(map->map.getValue().getTargetNodeId()!=nodeId).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        gatewayIdNodeIdMapLock.lock();
        try {
            gatewayIdNodeIdMap = gatewayIdNodeIdMap.entrySet().stream().filter(map -> !(map.getValue().equals(nodeId))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            logger.error("根据节点ID删除网关ID异常,节点ID:{}", nodeId, e);
        } finally {
            gatewayIdNodeIdMapLock.unlock();
        }
    }

    @Override
    public List<Integer> getNodeIdList() {
        gatewayIdNodeIdMapLock.lock();
        try {
            return new ArrayList<>(gatewayIdNodeIdMap.values());
        } catch (Exception e) {
            logger.error("获取网关节点ID列表异常", e);
        } finally {
            gatewayIdNodeIdMapLock.unlock();
        }
        return new ArrayList<>();
    }

    @Override
    public List<GatewaySettingField> queryGatewaySettingList(CommonReqField commonReq, List<GatewayField> gatewayList, Integer nodeId) {

        List<GatewaySettingField> gatewaySettingList = new ArrayList<>();

        if (!slaveOperatorId.equals(commonReq.getOperatorId())) {
            logger.error("查询网关设置列表错误,从节点操作员ID验证失败,业务ID:{}", commonReq.getTransactionId());
            return gatewaySettingList;
        }

        gatewayMap = gatewayMap.entrySet().stream().filter(map->map.getValue().getTargetNodeId()!=nodeId).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 处理节点报送过来的网关状态信息
        if (gatewayList != null) {
            for (GatewayField gateway : gatewayList) {
                gatewayMap.put(gateway.getGatewayId(), gateway);
                if (gateway.getStatus() == ConnectStatusEnum.CS_Connected) {
                    gatewayIdNodeIdMapLock.lock();
                    gatewayIdNodeIdMap.put(gateway.getGatewayId(), nodeId);
                    gatewayIdNodeIdMapLock.unlock();
                }
            }
        }

        List<GatewayPo> gatewayPoList = gatewayDao.queryGatewayList();

        for (GatewayPo gatewayPo : gatewayPoList) {
            if (gatewayPo.getTargetNodeId().equals(nodeId)) {
                try {
                    GatewaySettingField.Builder gatewaySettingBuilder = GatewaySettingField.newBuilder();

                    gatewaySettingBuilder.setGatewayId(gatewayPo.getGatewayId());
                    gatewaySettingBuilder.setGatewayName(gatewayPo.getGatewayName());
                    gatewaySettingBuilder.setGatewayDescription(StringUtils.isBlank(gatewayPo.getGatewayDescription()) ? "" : gatewayPo.getGatewayDescription());
                    gatewaySettingBuilder.setImplementClassName(gatewayPo.getImplementClassName());
                    gatewaySettingBuilder.setTargetNodeId(gatewayPo.getTargetNodeId());
                    gatewaySettingBuilder.setVersion(gatewayPo.getVersion());
                    gatewaySettingBuilder.setAutoConnectTimeRanges(StringUtils.isBlank(gatewayPo.getAutoConnectTimeRanges()) ? "" : gatewayPo.getAutoConnectTimeRanges());

                    gatewaySettingBuilder.setStatus(ConnectStatusEnum.forNumber(gatewayPo.getStatus()));
                    gatewaySettingBuilder.setGatewayType(GatewayTypeEnum.forNumber(gatewayPo.getGatewayType()));
                    gatewaySettingBuilder.setGatewayAdapterType(GatewayAdapterTypeEnum.forNumber(gatewayPo.getGatewayAdapterType()));

                    if (gatewayPo.getGatewayAdapterType() == GatewayAdapterTypeEnum.GAT_IB_VALUE) {
                        IbApiSettingField.Builder ibApiSettingBuilder = IbApiSettingField.newBuilder();
                        ibApiSettingBuilder.setClientId(gatewayPo.getIbSetting().getClientId());
                        ibApiSettingBuilder.setHost(gatewayPo.getIbSetting().getHost());
                        ibApiSettingBuilder.setPort(gatewayPo.getIbSetting().getPort());
                        gatewaySettingBuilder.setIbApiSetting(ibApiSettingBuilder);
                    } else if (gatewayPo.getGatewayAdapterType() == GatewayAdapterTypeEnum.GAT_CTP_VALUE) {
                        CtpApiSettingField.Builder ctpApiSettingBuilder = CtpApiSettingField.newBuilder();
                        ctpApiSettingBuilder.setUserId(gatewayPo.getCtpSetting().getUserId());
                        ctpApiSettingBuilder.setPassword(gatewayPo.getCtpSetting().getPassword());
                        ctpApiSettingBuilder.setBrokerId(gatewayPo.getCtpSetting().getBrokerId());
                        ctpApiSettingBuilder.setTdHost(gatewayPo.getCtpSetting().getTdHost());
                        ctpApiSettingBuilder.setTdPort(gatewayPo.getCtpSetting().getTdPort());
                        ctpApiSettingBuilder.setMdHost(gatewayPo.getCtpSetting().getMdHost());
                        ctpApiSettingBuilder.setMdPort(gatewayPo.getCtpSetting().getMdPort());
                        ctpApiSettingBuilder.setAuthCode(gatewayPo.getCtpSetting().getAuthCode());
                        ctpApiSettingBuilder.setUserProductInfo(gatewayPo.getCtpSetting().getUserProductInfo());
                        ctpApiSettingBuilder.setAppId(gatewayPo.getCtpSetting().getAppId());
                        gatewaySettingBuilder.setCtpApiSetting(ctpApiSettingBuilder);
                    }

                    gatewaySettingList.add(gatewaySettingBuilder.build());
                } catch (Exception e) {
                    logger.error("转换到PB类型发生错误,GatewayPo:{}", JSON.toJSONString(gatewayPo), e);
                }

            }
        }
        return gatewaySettingList;
    }

}
