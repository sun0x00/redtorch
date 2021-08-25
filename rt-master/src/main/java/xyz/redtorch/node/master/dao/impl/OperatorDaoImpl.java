package xyz.redtorch.node.master.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.db.ZookeeperService;
import xyz.redtorch.node.master.dao.OperatorDao;
import xyz.redtorch.node.master.po.OperatorPo;

import java.util.ArrayList;
import java.util.List;

@Service
public class OperatorDaoImpl implements OperatorDao {
    private static final Logger logger = LoggerFactory.getLogger(NodeDaoImpl.class);

    private static final String OPERATOR_COLLECTION_NAME = "operator";

    @Autowired
    ZookeeperService zookeeperService;


    @Override
    public OperatorPo queryOperatorByOperatorId(String operatorId) {
        if (StringUtils.isBlank(operatorId)) {
            logger.error("根据操作员ID查询操作员错误,参数operatorId缺失");
            throw new IllegalArgumentException("根据操作员ID查询操作员错误,参数operatorId缺失");
        }

        try {
            JSONObject jsonObject = zookeeperService.findById(OPERATOR_COLLECTION_NAME, operatorId);
            if (jsonObject != null) {
                return jsonObject.toJavaObject(OperatorPo.class);
            }
        } catch (Exception e) {
            logger.error("根据操作员ID查询操作员,数据转换发生错误", e);
        }
        return null;
    }

    @Override
    public List<OperatorPo> queryOperatorList() {
        List<OperatorPo> operatorList = new ArrayList<>();
        try {
            List<JSONObject> jsonObjectList = zookeeperService.find(OPERATOR_COLLECTION_NAME);
            if (jsonObjectList != null) {
                for (JSONObject jsonObject : jsonObjectList) {
                    try {
                        operatorList.add(jsonObject.toJavaObject(OperatorPo.class));
                    } catch (Exception e) {
                        logger.error("查询操作员列表,数据转换发生错误", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询操作员列表发生错误", e);
        }

        return operatorList;
    }

    @Override
    public void upsertOperatorByOperatorId(OperatorPo operator) {
        if (operator == null) {
            logger.error("根据操作员ID更新或保存操作员错误,参数operator缺失");
            throw new IllegalArgumentException("根据操作员ID更新或保存操作员错误,参数operator缺失");
        }
        if (StringUtils.isBlank(operator.getOperatorId())) {
            logger.error("根据操作员ID更新或保存操作员错误,参数operatorId缺失");
            throw new IllegalArgumentException("根据操作员ID更新或保存操作员错误,参数operatorId缺失");
        }

        try {
            zookeeperService.upsert(OPERATOR_COLLECTION_NAME, operator.getOperatorId(), JSON.toJSONString(operator));
        } catch (Exception e) {
            logger.error("根据操作员ID更新或保存操作员错误", e);
        }
    }

    @Override
    public void deleteOperatorByOperatorId(String operatorId) {
        if (StringUtils.isBlank(operatorId)) {
            logger.error("根据操作员ID删除操作员错误,参数operatorId缺失");
            throw new IllegalArgumentException("根据操作员ID删除操作员错误,参数operatorId缺失");
        }

        try {
            zookeeperService.deleteById(OPERATOR_COLLECTION_NAME, operatorId);
        } catch (Exception e) {
            logger.error("根据操作员ID删除操作员发生错误,操作员ID:{}", operatorId, e);
        }
    }

}
