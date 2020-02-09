package xyz.redtorch.node.master.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.master.dao.OperatorDao;
import xyz.redtorch.node.master.po.OperatorPo;
import xyz.redtorch.node.master.service.OperatorService;

@Service
public class OperatorServiceImpl implements OperatorService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(OperatorServiceImpl.class);

	@Autowired
	private OperatorDao operatorDao;
	@Value("${rt.node.master.operatorId}")
	private String adminOperatorId;

	private Map<String, OperatorPo> operatorMap = new ConcurrentHashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {
		this.getOperatorList();

	}

	@Override
	public OperatorPo getOperatorByOperatorId(String operatorId) {
		return operatorMap.get(operatorId);
	}

	@Override
	public List<OperatorPo> getOperatorList() {
		List<OperatorPo> operatorList = operatorDao.queryOperatorList();
		if (operatorList != null) {
			Map<String, OperatorPo> newOperatorMap = new ConcurrentHashMap<>();
			for (OperatorPo operator : operatorList) {
				newOperatorMap.put(operator.getOperatorId(), operator);
			}
			operatorMap = newOperatorMap;
		}
		return new ArrayList<>(operatorMap.values());
	}

	@Override
	public void upsertOperatorByOperatorId(OperatorPo operator) {
		if (operator == null) {
			logger.error("更新或新增操作员错误,参数operator缺失");
			throw new IllegalArgumentException("更新或新增操作员错误,参数operator缺失");
		}

		if (StringUtils.isBlank(operator.getOperatorId())) {
			logger.error("根据操作员ID删除操作员错误,参数operatorId缺失");
			throw new IllegalArgumentException("根据操作员ID删除操作员错误,参数operatorId缺失");
		}

		OperatorPo dbOperator = operatorDao.queryOperatorByOperatorId(operator.getOperatorId());
		if (dbOperator != null && dbOperator.isAssociatedToUser()) {
			operator.setUsername(dbOperator.getUsername());
			operator.setAssociatedToUser(true);
		}

		operatorDao.upsertOperatorByOperatorId(operator);
		this.getOperatorList();
	}

	@Override
	public void deleteOperatorByOperatorId(String operatorId) {
		if (StringUtils.isBlank(operatorId)) {
			logger.error("根据操作员ID删除操作员错误,参数operatorId缺失");
			throw new IllegalArgumentException("根据操作员ID删除操作员错误,参数operatorId缺失");
		}
		operatorDao.deleteOperatorByOperatorId(operatorId);
		this.getOperatorList();
	}

	@Override
	public OperatorPo createOperator() {
		OperatorPo operator = new OperatorPo();
		operator.setOperatorId(UUIDStringPoolUtils.getUUIDString());
		this.upsertOperatorByOperatorId(operator);
		return operator;
	}

	@Override
	public boolean checkSubscribePermission(String operatorId, String unifiedSymbol) {
		boolean canSubscribe = false;
		if (adminOperatorId.equals(operatorId)) {
			canSubscribe = true;
		} else {
			OperatorPo operator = getOperatorByOperatorId(operatorId);
			if (operator != null && operator.isCanSubscribeAllContracts() && !operator.getDenySubscribeSpecialUnifiedSymbolSet().contains(unifiedSymbol)) {
				canSubscribe = true;
			} else if (operator != null && operator.getAcceptSubscribeSpecialUnifiedSymbolSet().contains(unifiedSymbol)
					&& !operator.getDenySubscribeSpecialUnifiedSymbolSet().contains(unifiedSymbol)) {
				canSubscribe = true;
			}
		}
		return canSubscribe;
	}

	@Override
	public boolean checkReadAccountPermission(String operatorId, String accountId) {
		boolean canReadAccount = false;
		if (adminOperatorId.equals(operatorId)) {
			canReadAccount = true;
		} else {
			OperatorPo operator = getOperatorByOperatorId(operatorId);
			if (operator != null && operator.isCanReadAllAccounts() && !operator.getDenyReadSpecialAccountIdSet().contains(accountId)) {
				canReadAccount = true;
			} else if (operator != null && operator.getAcceptReadSpecialAccountIdSet().contains(accountId) && !operator.getDenyReadSpecialAccountIdSet().contains(accountId)) {
				canReadAccount = true;
			}
		}
		return canReadAccount;
	}

	@Override
	public boolean checkTradeAccountPermission(String operatorId, String accountId) {
		boolean canTradeAccount = false;
		if (adminOperatorId.equals(operatorId)) {
			canTradeAccount = true;
		} else {
			OperatorPo operator = getOperatorByOperatorId(operatorId);
			if (operator != null && operator.isCanTradeAllAccounts() && !operator.getDenyTradeSpecialAccountIdSet().contains(accountId)) {
				canTradeAccount = true;
			} else if (operator != null && operator.getAcceptTradeSpecialAccountIdSet().contains(accountId) && !operator.getDenyTradeSpecialAccountIdSet().contains(accountId)) {
				canTradeAccount = true;
			}
		}
		return canTradeAccount;
	}

	@Override
	public boolean checkTradeContractPermission(String operatorId, String unifiedSymbol) {
		boolean canTradeContract = false;

		if (adminOperatorId.equals(operatorId)) {
			canTradeContract = true;
		} else {
			OperatorPo operator = getOperatorByOperatorId(operatorId);

			if (operator != null && operator.isCanTradeAllContracts() && !operator.getDenyTradeSpecialUnifiedSymbolSet().contains(unifiedSymbol)) {
				canTradeContract = true;
			} else if (operator != null && operator.getAcceptTradeSpecialUnifiedSymbolSet().contains(unifiedSymbol) && !operator.getDenyTradeSpecialUnifiedSymbolSet().contains(unifiedSymbol)) {
				canTradeContract = true;
			}

		}

		return canTradeContract;
	}

}
