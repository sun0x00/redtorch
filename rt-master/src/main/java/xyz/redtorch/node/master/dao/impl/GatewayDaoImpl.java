package xyz.redtorch.node.master.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.db.ZookeeperService;
import xyz.redtorch.node.master.dao.GatewayDao;
import xyz.redtorch.node.master.po.GatewayPo;

import java.util.ArrayList;
import java.util.List;

@Service
public class GatewayDaoImpl implements GatewayDao {
    private static final Logger logger = LoggerFactory.getLogger(NodeDaoImpl.class);

    private static final String GATEWAY_COLLECTION_NAME = "gateway";

    @Autowired
    ZookeeperService zookeeperService;

    @Override
    public GatewayPo queryGatewayByGatewayId(String gatewayId) {
        if (StringUtils.isBlank(gatewayId)) {
            logger.error("根据网关ID查询网关错误,参数gatewayId缺失");
            throw new IllegalArgumentException("根据网关ID查询网关错误,参数gatewayId缺失");
        }

        try {
            JSONObject jsonObject = zookeeperService.findById(GATEWAY_COLLECTION_NAME, gatewayId);
            if (jsonObject != null) {
                return jsonObject.toJavaObject(GatewayPo.class);
            }
        } catch (Exception e) {
            logger.error("根据网关ID查询网关,数据转换发生错误", e);
        }
        return null;
    }

    @Override
    public List<GatewayPo> queryGatewayList() {
        List<GatewayPo> gatewayList = new ArrayList<>();
        try {
            List<JSONObject> jsonObjectList = zookeeperService.find(GATEWAY_COLLECTION_NAME);
            if (jsonObjectList != null) {
                for (JSONObject jsonObject : jsonObjectList) {
                    try {
                        gatewayList.add(jsonObject.toJavaObject(GatewayPo.class));
                    } catch (Exception e) {
                        logger.error("查询网关列表,数据转换发生错误", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询网关列表发生错误", e);
        }

        return gatewayList;
    }

    @Override
    public void upsertGatewayByGatewayId(GatewayPo gateway) {
        if (gateway == null) {
            logger.error("根据网关ID更新或保存网关错误,参数gateway缺失");
            throw new IllegalArgumentException("根据网关ID更新或保存网关错误,参数gateway缺失");
        }
        if (StringUtils.isBlank(gateway.getGatewayId())) {
            logger.error("根据网关ID更新或保存网关错误,参数gatewayId缺失");
            throw new IllegalArgumentException("根据网关ID更新或保存网关错误,参数gatewayId缺失");
        }

        try {
            zookeeperService.upsert(GATEWAY_COLLECTION_NAME, gateway.getGatewayId(), JSON.toJSONString(gateway));
        } catch (Exception e) {
            logger.error("根据网关ID更新或保存网关错误", e);
        }
    }

    @Override
    public void deleteGatewayByGatewayId(String gatewayId) {
        if (StringUtils.isBlank(gatewayId)) {
            logger.error("根据网关ID删除网关错误,参数gatewayId缺失");
            throw new IllegalArgumentException("根据网关ID删除网关错误,参数gatewayId缺失");
        }

        try {
            zookeeperService.deleteById(GATEWAY_COLLECTION_NAME, gatewayId);
        } catch (Exception e) {
            logger.error("根据网关ID删除网关发生错误,网关ID:{}", gatewayId, e);
        }
    }

}
