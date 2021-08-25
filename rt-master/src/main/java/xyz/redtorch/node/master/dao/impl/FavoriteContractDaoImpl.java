package xyz.redtorch.node.master.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.db.ZookeeperService;
import xyz.redtorch.node.master.dao.FavoriteContractDao;
import xyz.redtorch.node.master.po.ContractPo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteContractDaoImpl implements FavoriteContractDao {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteContractDaoImpl.class);

    private static final String FAVORITE_CONTRACT_COLLECTION_NAME = "favorite_contract";

    @Autowired
    ZookeeperService zookeeperService;

    @Override
    public List<ContractPo> queryContractList() {
        List<ContractPo> contractList = new ArrayList<>();
        try {
            List<JSONObject> jsonObjectList = zookeeperService.find(FAVORITE_CONTRACT_COLLECTION_NAME);
            if (jsonObjectList != null) {
                for (JSONObject jsonObject : jsonObjectList) {
                    try {
                        contractList.add(jsonObject.toJavaObject(ContractPo.class));
                    } catch (Exception e) {
                        logger.error("查询合约列表,数据转换发生错误", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询网关列表发生错误", e);
        }
        return contractList;
    }

    @Override
    public List<ContractPo> queryContractListByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            logger.error("根据用户名查询常用合约列表错误,参数username缺失");
            throw new IllegalArgumentException("根据用户名查询常用合约列表错误,参数username缺失");
        }

        List<ContractPo> contractPoList = queryContractList();

        return contractPoList.stream().filter(contract -> contract.getContractId().startsWith(username + "-")).collect(Collectors.toList());

    }

    @Override
    public void upsertContractByUsernameAndUniformSymbol(String username, ContractPo contract) {
        if (StringUtils.isBlank(username)) {
            logger.error("根据用户名和合约统一标识更新或保存合约错误,参数username缺失");
            throw new IllegalArgumentException("根据用户名和合约统一标识更新或保存合约错误,参数username缺失");
        }
        if (contract == null) {
            logger.error("根据用户名和合约统一标识更新或保存合约错误,参数contract缺失");
            throw new IllegalArgumentException("根据用户名和合约统一标识更新或保存合约错误,参数contract缺失");
        }
        if (StringUtils.isBlank(contract.getUniformSymbol())) {
            logger.error("根据用户名和合约统一标识更新或保存合约错误,参数uniformSymbol缺失");
            throw new IllegalArgumentException("根据用户名和合约统一标识更新或保存合约错误,参数uniformSymbol缺失");
        }
        try {
            String contractId = username + "-" + contract.getUniformSymbol();
            ContractPo newContract = JSON.parseObject(JSON.toJSONString(contract), ContractPo.class);
            newContract.setContractId(contractId);
            zookeeperService.upsert(FAVORITE_CONTRACT_COLLECTION_NAME, contractId, JSON.toJSONString(newContract));
        } catch (IllegalArgumentException e) {
            logger.error("根据用户名和合约统一标识更新或保存合约错误", e);
        }

    }

    @Override
    public void deleteContractByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            logger.error("根据用户名删除合约错误,参数username缺失");
            throw new IllegalArgumentException("根据用户名删除合约错误,参数username缺失");
        }

        try {
            List<ContractPo> contractPoList = queryContractListByUsername(username);
            for (ContractPo contractPo : contractPoList) {
                zookeeperService.deleteById(FAVORITE_CONTRACT_COLLECTION_NAME, contractPo.getContractId());
            }
        } catch (IllegalArgumentException e) {
            logger.error("根据用户名删除合约错误,用户名:{}", username, e);
        }
    }

    @Override
    public void deleteContractByUsernameAndUniformSymbol(String username, String uniformSymbol) {
        if (StringUtils.isBlank(uniformSymbol)) {
            logger.error("根据用户名和合约统一标识删除合约错误,参数uniformSymbol缺失");
            throw new IllegalArgumentException("根据用户名和合约统一标识删除合约错误,参数uniformSymbol缺失");
        }

        if (StringUtils.isBlank(username)) {
            logger.error("根据用户名和合约统一标识删除合约错误,参数username缺失");
            throw new IllegalArgumentException("根据用户名和合约统一标识删除合约错误,参数username缺失");
        }

        try {
            zookeeperService.deleteById(FAVORITE_CONTRACT_COLLECTION_NAME, username + "-" + uniformSymbol);
        } catch (IllegalArgumentException e) {
            logger.error("根据用户名和合约统一标识删除合约错误,用户名:{}", username, e);
        }
    }
}
