package xyz.redtorch.node.master.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.db.ZookeeperService;
import xyz.redtorch.node.master.dao.MarketDataRecordingDao;
import xyz.redtorch.node.master.po.ContractPo;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketDataRecordingDaoImpl implements MarketDataRecordingDao {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataRecordingDaoImpl.class);

    private static final String MARKET_DATA_RECORDING_COLLECTION_NAME = "market_data_recording";

    @Autowired
    ZookeeperService zookeeperService;

    @Override
    public void upsertContractByUniformSymbol(ContractPo user) {
        if (user == null) {
            logger.error("根据合约统一标识更新或保存合约错误,参数user缺失");
            throw new IllegalArgumentException("根据合约统一标识更新或保存合约错误,参数user缺失");
        }
        if (StringUtils.isBlank(user.getUniformSymbol())) {
            logger.error("根据合约统一标识更新或保存合约错误,参数uniformSymbol缺失");
            throw new IllegalArgumentException("根据合约统一标识更新或保存合约错误,参数uniformSymbol缺失");
        }

        try {
            zookeeperService.upsert(MARKET_DATA_RECORDING_COLLECTION_NAME, user.getUniformSymbol(), JSON.toJSONString(user));
        } catch (IllegalArgumentException e) {
            logger.error("根据合约统一标识更新或保存合约错误", e);
        }
    }

    @Override
    public List<ContractPo> queryContractList() {
        List<ContractPo> userList = new ArrayList<>();
        try {
            List<JSONObject> jsonObjectList = zookeeperService.find(MARKET_DATA_RECORDING_COLLECTION_NAME);
            if (jsonObjectList != null) {
                for (JSONObject jsonObject : jsonObjectList) {
                    try {
                        userList.add(jsonObject.toJavaObject(ContractPo.class));
                    } catch (Exception e) {
                        logger.error("查询合约列表,数据转换发生错误", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询合约列表发生错误", e);
        }

        return userList;
    }

    @Override
    public void deleteContractByUniformSymbol(String uniformSymbol) {
        if (StringUtils.isBlank(uniformSymbol)) {
            logger.error("根据合约统一标识删除合约错误,参数uniformSymbol缺失");
            throw new IllegalArgumentException("根据合约统一标识删除合约错误,参数uniformSymbol缺失");
        }
        try {
            zookeeperService.deleteById(MARKET_DATA_RECORDING_COLLECTION_NAME, uniformSymbol);
        } catch (IllegalArgumentException e) {
            logger.error("根据合约统一标识删除合约错误,节点ID:{}", uniformSymbol, e);
        }

    }

}
