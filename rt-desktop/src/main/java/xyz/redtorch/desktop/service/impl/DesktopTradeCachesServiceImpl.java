package xyz.redtorch.desktop.service.impl;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetMixContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;

@Service
public class DesktopTradeCachesServiceImpl implements DesktopTradeCachesService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(DesktopTradeCachesServiceImpl.class);

	private ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();

	@Autowired
	private RpcClientApiService rpcClientApiService;

	private Map<String, OrderField> workingOrderMap = new HashMap<>(2000);
	private Map<String, ContractField> contractMap = new HashMap<>(200000);
	private Map<String, ContractField> mixContractMap = new HashMap<>(10000);
	private Map<String, TickField> tickMap = new HashMap<>(1000);
	private Map<String, TickField> mixTickMap = new HashMap<>(1000);
	private Map<String, OrderField> orderMap = new HashMap<>(50000);
	private Map<String, TradeField> tradeMap = new HashMap<>(100000);
	private Map<String, AccountField> accountMap = new HashMap<>(500);
	private Map<String, PositionField> positionMap = new HashMap<>(5000);

	private Lock contractMapLock = new ReentrantLock();
	private Lock tickMapLock = new ReentrantLock();
	private Lock orderMapLock = new ReentrantLock();
	private Lock tradeMapLock = new ReentrantLock();
	private Lock accountMapLock = new ReentrantLock();
	private Lock positionMapLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Exception {

		// 刷新缓存
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {
				reloadData();
			} catch (Exception e) {
				logger.error("定时从主节点刷新数据异常", e);
			}
		}, 60, 60, TimeUnit.SECONDS);

	}

	@Override
	public void reloadData() {
		RpcGetAccountListRsp rpcGetAccountListRsp = rpcClientApiService.getAccountList(null, null);
		if (rpcGetAccountListRsp != null && rpcGetAccountListRsp.getAccountList() != null) {
			clearAndCacheAccountList(rpcGetAccountListRsp.getAccountList());
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// NOP
		}

		RpcGetPositionListRsp rpcGetPositionListRsp = rpcClientApiService.getPositionList(null, null);
		if (rpcGetPositionListRsp != null && rpcGetPositionListRsp.getPositionList() != null) {
			clearAndCachePositionList(rpcGetPositionListRsp.getPositionList());
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// NOP
		}

		RpcGetTradeListRsp rpcGetTradeListRsp = rpcClientApiService.getTradeList(null, null);
		if (rpcGetTradeListRsp != null && rpcGetTradeListRsp.getTradeList() != null) {
			clearAndCacheTradeList(rpcGetTradeListRsp.getTradeList());
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// NOP
		}

		RpcGetOrderListRsp rpcGetOrderListRsp = rpcClientApiService.getOrderList(null, null);
		if (rpcGetOrderListRsp != null && rpcGetOrderListRsp.getOrderList() != null) {
			clearAndCacheOrderList(rpcGetOrderListRsp.getOrderList());
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// NOP
		}

		RpcGetTickListRsp rpcGetTickListRsp = rpcClientApiService.getTickList(null, null);
		if (rpcGetTickListRsp != null && rpcGetTickListRsp.getTickList() != null) {
			clearAndCacheTickList(rpcGetTickListRsp.getTickList());
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// NOP
		}

		RpcGetMixContractListRsp rpcGetMixContractListRsp = rpcClientApiService.getMixContractList(null, null);
		if (rpcGetMixContractListRsp != null && rpcGetMixContractListRsp.getContractList() != null) {
			clearAndCacheContractList(rpcGetMixContractListRsp.getContractList());
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// NOP
		}
	}

	@Override
	public List<OrderField> getOrderList() {
		List<OrderField> orderList = new ArrayList<>();
		orderMapLock.lock();
		try {
			orderList = new ArrayList<>(orderMap.values());
		} catch (Exception e) {
			logger.error("获取委托列表异常", e);
		} finally {
			orderMapLock.unlock();
		}
		return orderList;
	}

	@Override
	public List<OrderField> getWorkingOrderList() {
		List<OrderField> orderList = new ArrayList<>();
		orderMapLock.lock();
		try {
			orderList = new ArrayList<>(workingOrderMap.values());
		} catch (Exception e) {
			logger.error("获取活动委托列表异常", e);
		} finally {
			orderMapLock.unlock();
		}
		return orderList;
	}

	@Override
	public OrderField queryOrderByOrderId(String orderId) {

		OrderField order = orderMap.get(orderId);

		return order;
	}

	@Override
	public OrderField queryOrderByOriginOrderId(String originOrderId) {
		// TODO 需要补充
		return null;
	}

	@Override
	public List<OrderField> queryOrderListByAccountId(String accountId) {

		List<OrderField> orderList = new ArrayList<>();
		orderMapLock.lock();
		try {
			orderList = new ArrayList<>(orderMap.values());
		} catch (Exception e) {
			logger.error("根据账户ID获取委托列表异常", e);
		} finally {
			orderMapLock.unlock();
		}
		return orderList;
	}

	@Override
	public List<OrderField> queryOrderListByUnifiedSymbol(String unifiedSymbol) {
		List<OrderField> orderList = new ArrayList<>();
		orderMapLock.lock();
		try {
			orderList = new ArrayList<>(orderMap.values());
		} catch (Exception e) {
			logger.error("根据统一标识获取委托列表异常", e);
		} finally {
			orderMapLock.unlock();
		}
		return orderList;
	}

	@Override
	public List<TradeField> getTradeList() {
		List<TradeField> tradeList = new ArrayList<>();
		tradeMapLock.lock();
		try {
			tradeList = new ArrayList<>(tradeMap.values());
		} catch (Exception e) {
			logger.error("获取成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
		return tradeList;
	}

	@Override
	public TradeField queryTradeByTradeId(String tradeId) {
		TradeField trade = tradeMap.get(tradeId);
		return trade;
	}

	@Override
	public List<TradeField> queryTradeListByUnifiedSymbol(String unifiedSymbol) {
		List<TradeField> tradeList = new ArrayList<>();
		tradeMapLock.lock();
		try {
			tradeList = new ArrayList<>(tradeMap.values());
		} catch (Exception e) {
			logger.error("根据统一标识获取成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
		return tradeList;
	}

	@Override
	public List<TradeField> queryTradeListByAccountId(String accountId) {
		List<TradeField> tradeList = new ArrayList<>();
		tradeMapLock.lock();
		try {
			tradeList = tradeMap.values().stream().filter(trade -> trade.getAccountId().equals(accountId)).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("根据账户ID获取成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
		return tradeList;
	}

	@Override
	public List<TradeField> queryTradeListByOrderId(String orderId) {
		List<TradeField> tradeList = new ArrayList<>();
		tradeMapLock.lock();
		try {
			tradeList = new ArrayList<>(tradeMap.values());
		} catch (Exception e) {
			logger.error("根据委托ID获取成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
		return tradeList;
	}

	@Override
	public List<TradeField> queryTradeListByOriginOrderId(String originOrderId) {
		List<TradeField> tradeList = new ArrayList<>();
		tradeMapLock.lock();
		try {
			tradeList = new ArrayList<>(tradeMap.values());
		} catch (Exception e) {
			logger.error("根据原始委托ID获取成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
		return tradeList;
	}

	@Override
	public List<PositionField> getPositionList() {
		List<PositionField> positionList = new ArrayList<>();
		positionMapLock.lock();
		try {
			positionList = new ArrayList<>(positionMap.values());
		} catch (Exception e) {
			logger.error("获取持仓列表异常", e);
		} finally {
			positionMapLock.unlock();
		}
		return positionList;
	}

	@Override
	public PositionField queryPositionByPositionId(String positionId) {
		PositionField position = positionMap.get(positionId);
		return position;
	}

	@Override
	public List<PositionField> queryPositionListByAccountId(String accountId) {
		List<PositionField> positionList = new ArrayList<>();
		positionMapLock.lock();
		try {
			positionList = new ArrayList<>(positionMap.values());

		} catch (Exception e) {
			logger.error("根据账户ID获取持仓列表异常", e);
		} finally {
			positionMapLock.unlock();
		}
		return positionList;
	}

	@Override
	public List<PositionField> queryPositionListByUnifiedSymbol(String unifiedSymbol) {
		List<PositionField> positionList = new ArrayList<>();
		positionMapLock.lock();
		try {
			positionList = new ArrayList<>(positionMap.values());
		} catch (Exception e) {
			logger.error("根据统一标识获取持仓列表异常", e);
		} finally {
			positionMapLock.unlock();
		}
		return positionList;
	}

	@Override
	public List<AccountField> getAccountList() {
		List<AccountField> accountList = new ArrayList<>();
		accountMapLock.lock();
		try {
			accountList = new ArrayList<AccountField>(accountMap.values());
		} catch (Exception e) {
			logger.error("获取账户列表异常", e);
		} finally {
			accountMapLock.unlock();
		}
		return accountList;
	}

	@Override
	public AccountField queryAccountByAccountId(String accountId) {
		return accountMap.get(accountId);
	}

	@Override
	public List<AccountField> queryAccountListByAccountCode(String accountCode) {
		List<AccountField> accountList = new ArrayList<>();
		accountMapLock.lock();
		try {
			accountList = new ArrayList<>(accountMap.values());
		} catch (Exception e) {
			logger.error("根据账户代码获取账户列表异常", e);
		} finally {
			accountMapLock.unlock();
		}
		return accountList;
	}

	@Override
	public List<ContractField> getContractList() {
		List<ContractField> contractList = new ArrayList<>();
		contractMapLock.lock();
		try {
			contractList = new ArrayList<ContractField>(contractMap.values());
		} catch (Exception e) {
			logger.error("获取合约列表异常", e);
		} finally {
			contractMapLock.unlock();
		}
		return contractList;
	}

	@Override
	public List<TickField> getTickList() {
		List<TickField> tickList = new ArrayList<>();
		tickMapLock.lock();
		try {
			tickList = new ArrayList<>(tickMap.values());
		} catch (Exception e) {
			logger.error("获取Tick列表异常", e);
		} finally {
			tickMapLock.unlock();
		}
		return tickList;
	}

	@Override
	public TickField queryTickByUnifiedSymbol(String unifiedSymbol) {
		return mixTickMap.get(unifiedSymbol);
	}

	@Override
	public List<TickField> getMixTickList() {
		List<TickField> contractList = new ArrayList<>();
		contractMapLock.lock();
		try {
			contractList = new ArrayList<TickField>(mixTickMap.values());
		} catch (Exception e) {
			logger.error("获取混合Tick列表异常", e);
		} finally {
			contractMapLock.unlock();
		}
		return contractList;
	}

	@Override
	public List<ContractField> getMixContractList() {
		List<ContractField> contractList = new ArrayList<>();
		contractMapLock.lock();
		try {
			contractList = new ArrayList<ContractField>(mixContractMap.values());
		} catch (Exception e) {
			logger.error("获取混合合约列表异常", e);
		} finally {
			contractMapLock.unlock();
		}
		return contractList;
	}

	@Override
	public ContractField queryContractByUnifiedSymbol(String unifiedSymbol) {
		return mixContractMap.get(unifiedSymbol);
	}

	@Override
	public ContractField queryContractByContractId(String contractId) {
		return contractMap.get(contractId);
	}

	@Override
	public List<ContractField> queryContractListByUnifiedSymbol(String unifiedSymbol) {
		List<ContractField> contractList = new ArrayList<>();
		contractMapLock.lock();
		try {
			contractList = contractMap.values().stream().filter(contractField -> contractField.getUnifiedSymbol().equals(unifiedSymbol)).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("根据统一标识获取合约列表异常", e);
		} finally {
			contractMapLock.unlock();
		}
		return contractList;
	}

	@Override
	public List<ContractField> queryContractListByGatewayId(String gatewayId) {
		List<ContractField> contractList = new ArrayList<>();
		contractMapLock.lock();
		try {
			contractList = contractMap.values().stream().filter(contractField -> contractField.getGatewayId().equals(gatewayId)).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error("根据网关ID获取合约列表异常", e);
		} finally {
			contractMapLock.unlock();
		}
		return contractList;
	}

	@Override
	public void cacheOrder(OrderField order) {
		orderMapLock.lock();
		try {
			String orderId = order.getOrderId();
			if (!orderMap.containsKey(orderId) || !CommonConstant.ORDER_STATUS_FINISHED_SET.contains(orderMap.get(orderId).getOrderStatus())) {

				orderMap.put(order.getOrderId(), order);
			}
		} catch (Exception e) {
			logger.error("存储定单异常", e);
		} finally {
			orderMapLock.unlock();
		}
	}

	@Override
	public void cacheTrade(TradeField trade) {
		tradeMapLock.lock();
		try {
			tradeMap.put(trade.getTradeId(), trade);
		} catch (Exception e) {
			logger.error("存储成交异常", e);
		} finally {
			tradeMapLock.unlock();
		}
	}

	@Override
	public void cacheContract(ContractField contract) {
		contractMapLock.lock();
		try {
			contractMap.put(contract.getContractId(), contract);
			mixContractMap.put(contract.getUnifiedSymbol(), contract);
		} catch (Exception e) {
			logger.error("存储合约异常", e);
		} finally {
			contractMapLock.unlock();
		}
	}

	@Override
	public void cachePosition(PositionField position) {
		positionMapLock.lock();
		try {
			positionMap.put(position.getPositionId(), position);
		} catch (Exception e) {
			logger.error("存储持仓异常", e);
		} finally {
			positionMapLock.unlock();
		}
	}

	@Override
	public void cacheAccount(AccountField account) {
		accountMapLock.lock();
		try {
			accountMap.put(account.getAccountId(), account);
		} catch (Exception e) {
			logger.error("存储账户异常", e);
		} finally {
			accountMapLock.unlock();
		}
	}

	@Override
	public void cacheTick(TickField tick) {
		tickMapLock.lock();
		try {
			tickMap.put(tick.getUnifiedSymbol() + "@" + tick.getGatewayId(), tick);
			mixTickMap.put(tick.getUnifiedSymbol(), tick);
		} catch (Exception e) {
			logger.error("存储Tick异常", e);
		} finally {
			tickMapLock.unlock();
		}

	}

	@Override
	public void cacheOrderList(List<OrderField> orderList) {
		orderMapLock.lock();
		try {
			for (OrderField order : orderList) {
				orderMap.put(order.getOrderId(), order);
			}
		} catch (Exception e) {
			logger.error("存储定单列表异常", e);
		} finally {
			orderMapLock.unlock();
		}
	}

	@Override
	public void cacheTradeList(List<TradeField> tradeList) {
		tradeMapLock.lock();
		try {
			for (TradeField trade : tradeList) {
				tradeMap.put(trade.getTradeId(), trade);
			}
		} catch (Exception e) {
			logger.error("存储成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
	}

	@Override
	public void cacheContractList(List<ContractField> contractList) {
		contractMapLock.lock();
		try {
			for (ContractField contract : contractList) {
				contractMap.put(contract.getContractId(), contract);
				mixContractMap.put(contract.getUnifiedSymbol(), contract);
			}
		} catch (Exception e) {
			logger.error("存储合约列表异常", e);
		} finally {
			contractMapLock.unlock();
		}
	}

	@Override
	public void cachePositionList(List<PositionField> positionList) {
		positionMapLock.lock();
		try {
			for (PositionField position : positionList) {
				positionMap.put(position.getPositionId(), position);
			}
		} catch (Exception e) {
			logger.error("存储持仓列表异常", e);
		} finally {
			positionMapLock.unlock();
		}
	}

	@Override
	public void cacheAccountList(List<AccountField> accountList) {
		accountMapLock.lock();
		try {
			for (AccountField account : accountList) {
				accountMap.put(account.getAccountId(), account);
			}
		} catch (Exception e) {
			logger.error("存储账户列表异常", e);
		} finally {
			accountMapLock.unlock();
		}
	}

	@Override
	public void cacheTickList(List<TickField> tickList) {
		tickMapLock.lock();
		try {
			for (TickField tick : tickList) {
				tickMap.put(tick.getUnifiedSymbol() + "@" + tick.getGatewayId(), tick);
				mixTickMap.put(tick.getUnifiedSymbol(), tick);
			}

		} catch (Exception e) {
			logger.error("存储Tick列表异常", e);
		} finally {
			tickMapLock.unlock();
		}
	}

	@Override
	public void clearAndCacheOrderList(List<OrderField> orderList) {
		orderMapLock.lock();
		try {
			orderMap.clear();
			for (OrderField order : orderList) {
				orderMap.put(order.getOrderId(), order);
			}
		} catch (Exception e) {
			logger.error("存储定单列表异常", e);
		} finally {
			orderMapLock.unlock();
		}
	}

	@Override
	public void clearAndCacheTradeList(List<TradeField> tradeList) {
		tradeMapLock.lock();
		try {
			tradeMap.clear();
			for (TradeField trade : tradeList) {
				tradeMap.put(trade.getTradeId(), trade);
			}
		} catch (Exception e) {
			logger.error("存储成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
	}

	@Override
	public void clearAndCacheContractList(List<ContractField> contractList) {
		contractMapLock.lock();
		try {
			contractMap.clear();
			mixContractMap.clear();
			for (ContractField contract : contractList) {
				contractMap.put(contract.getContractId(), contract);
				mixContractMap.put(contract.getUnifiedSymbol(), contract);
			}
		} catch (Exception e) {
			logger.error("存储合约列表异常", e);
		} finally {
			contractMapLock.unlock();
		}

	}

	@Override
	public void clearAndCachePositionList(List<PositionField> positionList) {
		positionMapLock.lock();
		try {
			positionMap.clear();
			for (PositionField position : positionList) {
				positionMap.put(position.getPositionId(), position);
			}
		} catch (Exception e) {
			logger.error("存储持仓列表异常", e);
		} finally {
			positionMapLock.unlock();
		}
	}

	@Override
	public void clearAndCacheAccountList(List<AccountField> accountList) {
		accountMapLock.lock();
		try {
			accountMap.clear();
			for (AccountField account : accountList) {
				accountMap.put(account.getAccountId(), account);
			}
		} catch (Exception e) {
			logger.error("存储账户列表异常", e);
		} finally {
			accountMapLock.unlock();
		}

	}

	@Override
	public void clearAndCacheTickList(List<TickField> tickList) {
		tickMapLock.lock();
		try {
			tickMap.clear();
			mixTickMap.clear();
			for (TickField tick : tickList) {
				tickMap.put(tick.getUnifiedSymbol() + "@" + tick.getGatewayId(), tick);
				mixTickMap.put(tick.getUnifiedSymbol(), tick);
			}

		} catch (Exception e) {
			logger.error("存储Tick列表异常", e);
		} finally {
			tickMapLock.unlock();
		}
	}

}
