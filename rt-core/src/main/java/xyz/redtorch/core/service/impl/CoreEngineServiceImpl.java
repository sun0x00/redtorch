package xyz.redtorch.core.service.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.RingBuffer;

import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.LocalPositionDetail;
import xyz.redtorch.core.entity.LogData;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.gateway.Gateway;
import xyz.redtorch.core.gateway.GatewaySetting;
import xyz.redtorch.core.service.MongoDBService;
import xyz.redtorch.core.service.CoreEngineDataService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandlerAbstract;
import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.utils.CommonUtil;

/**
 * @author sun0x00@gmail.com
 */
@Service
public class CoreEngineServiceImpl extends FastEventDynamicHandlerAbstract
		implements CoreEngineService, InitializingBean {

	private static Logger log = LoggerFactory.getLogger(CoreEngineServiceImpl.class);

	@Autowired
	private MongoDBService mongoDBService;
	@Autowired
	private CoreEngineDataService coreEngineDataService;
	@Autowired
	private FastEventEngineService fastEventEngineService;

	private Map<String, Gateway> gatewayMap = new HashMap<>();
	private Map<String, Contract> mixContractMap = new HashMap<>();
	private Map<String, Contract> contractMap = new HashMap<>();
	private Map<String, Tick> tickMap = new HashMap<>();

	private Map<String, Order> orderMap = new HashMap<>();
	private Map<String, Order> workingOrderMap = new HashMap<>();
	private Map<String, Trade> tradeMap = new HashMap<>();
	private Map<String, Account> accountMap = new HashMap<>();
	private Map<String, LocalPositionDetail> localPositionDetailMap = new HashMap<>();
	private Map<String, Position> positionMap = new HashMap<>();
	private List<LogData> logDataList = new ArrayList<>();

	private Map<String, Set<String>> subscriberRelationshipMap = new HashMap<>();

	private Map<String, HashSet<SubscribeReq>> subscribeReqSetMap = new HashMap<>();
	
	private HashMap<String, String> originalOrderIDMap = new HashMap<>();

	@Override
	public void afterPropertiesSet() throws Exception {

		fastEventEngineService.addHandler(this);

		subscribeEvent(EventConstant.EVENT_TICK);
		subscribeEvent(EventConstant.EVENT_TRADE);
		subscribeEvent(EventConstant.EVENT_ORDER);
		subscribeEvent(EventConstant.EVENT_POSITION);
		subscribeEvent(EventConstant.EVENT_ACCOUNT);
		subscribeEvent(EventConstant.EVENT_CONTRACT);
		subscribeEvent(EventConstant.EVENT_ERROR);
		subscribeEvent(EventConstant.EVENT_GATEWAY);
		subscribeEvent(EventConstant.EVENT_LOG);
		subscribeEvent(EventConstant.EVENT_LOG + "ZEUS|");
	}

	@Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {

		if (!subscribedEventSet.contains(fastEvent.getEvent())) {
			return;
		}

		// 判断消息类型
		if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
			try {
				Tick tick = fastEvent.getTick();
				onTick(tick);
			} catch (Exception e) {
				log.error("onTick发生异常", e);
			}
		} else if (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())) {
			try {
				Trade trade = fastEvent.getTrade();
				onTrade(trade);
			} catch (Exception e) {
				log.error("onTrade发生异常", e);
			}
		} else if (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())) {
			try {
				Order order = fastEvent.getOrder();
				onOrder(order);
			} catch (Exception e) {
				log.error("onOrder发生异常", e);
			}
		} else if (EventConstant.EVENT_CONTRACT.equals(fastEvent.getEventType())) {
			try {
				Contract contract = fastEvent.getContract();
				onContract(contract);
			} catch (Exception e) {
				log.error("onContract发生异常", e);
			}
		} else if (EventConstant.EVENT_POSITION.equals(fastEvent.getEventType())) {
			try {
				Position position = fastEvent.getPosition();
				onPosition(position);
			} catch (Exception e) {
				log.error("onPosition发生异常", e);
			}
		} else if (EventConstant.EVENT_ACCOUNT.equals(fastEvent.getEventType())) {
			try {
				Account account = fastEvent.getAccount();
				onAccount(account);
			} catch (Exception e) {
				log.error("onAccount发生异常", e);
			}
		} else if (EventConstant.EVENT_LOG.equals(fastEvent.getEventType())) {
			try {
				LogData logData = fastEvent.getLogData();
				onLogData(logData);
			} catch (Exception e) {
				log.error("onLogData发生异常", e);
			}
		} else if (EventConstant.EVENT_GATEWAY.equals(fastEvent.getEventType())) {
			// nop
		} else {
			log.warn("未能识别的事件数据类型{}", JSON.toJSONString(fastEvent.getEvent()));
		}
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onShutdown() {
		shutdownLatch.countDown();
	}

	private void onLogData(LogData logData) {
		logDataList.add(logData);
	}

	private void onContract(Contract contract) {
		mixContractMap.put(contract.getSymbol(), contract); // 常规代码不包含交易所,可能会合约导致重复
		mixContractMap.put(contract.getRtSymbol(), contract); // 多个网关同一个交易所之间的代码可能重复
		mixContractMap.put(contract.getSymbol() + "." + contract.getGatewayID(), contract);
		mixContractMap.put(contract.getRtContractID(), contract);

		contractMap.put(contract.getRtContractID(), contract);

		// CTP重连时Trade可能先于Contract到达,在此处重新赋值
		String exchange = contract.getExchange();
		String symbol = contract.getSymbol();
		String contractName = contract.getName();
		int contractSize = contract.getSize();
		String positionDetailKey = contract.getRtContractID();
		LocalPositionDetail localPositionDetail = localPositionDetailMap.get(positionDetailKey);
		if (localPositionDetail != null) {
			localPositionDetail.setExchange(exchange);
			localPositionDetail.setSymbol(symbol);
			localPositionDetail.setContractName(contractName);
			localPositionDetail.setContractSize(contractSize);
		}

	}

	private void onAccount(Account account) {
		accountMap.put(account.getRtAccountID(), account);
	}

	private void onOrder(Order order) {
		orderMap.put(order.getRtOrderID(), order);
		if (RtConstant.STATUS_FINISHED.contains(order.getStatus())) {
			if (workingOrderMap.containsKey(order.getRtOrderID())) {
				workingOrderMap.remove(order.getRtOrderID());
			}
		} else {
			workingOrderMap.put(order.getRtOrderID(), order);
		}

		String positionDetailKey = order.getRtSymbol() + "." + order.getRtAccountID();
		LocalPositionDetail localPositionDetail;
		if (localPositionDetailMap.containsKey(positionDetailKey)) {
			localPositionDetail = localPositionDetailMap.get(positionDetailKey);
		} else {
			localPositionDetail = createLocalPositionDetail(order.getGatewayID(), order.getGatewayDisplayName(),
					order.getAccountID(), order.getRtAccountID(), order.getExchange(), order.getRtSymbol(),
					order.getSymbol());
		}

		localPositionDetail.updateOrder(order);
	}

	private void onTrade(Trade trade) {
		tradeMap.put(trade.getRtTradeID(), trade);
		String positionDetailKey = trade.getRtSymbol() + "." + trade.getRtAccountID();

		LocalPositionDetail localPositionDetail;
		if (localPositionDetailMap.containsKey(positionDetailKey)) {
			localPositionDetail = localPositionDetailMap.get(positionDetailKey);
		} else {
			localPositionDetail = createLocalPositionDetail(trade.getGatewayID(), trade.getGatewayDisplayName(),
					trade.getAccountID(), trade.getRtAccountID(), trade.getExchange(), trade.getRtSymbol(),
					trade.getSymbol());
		}

		localPositionDetail.updateTrade(trade);
	}

	private void onPosition(Position position) {
		positionMap.put(position.getRtPositionID(), position);

		LocalPositionDetail localPositionDetail;
		String positionDetailKey = position.getRtSymbol() + "." + position.getRtAccountID();
		if (localPositionDetailMap.containsKey(positionDetailKey)) {
			localPositionDetail = localPositionDetailMap.get(positionDetailKey);
		} else {
			localPositionDetail = createLocalPositionDetail(position.getGatewayID(), position.getGatewayDisplayName(),
					position.getAccountID(), position.getRtAccountID(), position.getExchange(), position.getRtSymbol(),
					position.getSymbol());
		}

		localPositionDetail.updatePosition(position);

	}

	private void onTick(Tick tick) {
		// 更新指定网关的持仓盈亏
		// LocalPositionDetail localPositionDetail=
		// getLocalPositionDetail(tick.getRtSymbol(),tick.getGatewayID());
		// localPositionDetail.updateLastPrice(tick.getLastPrice());

		// 更新所有相同rtSymbol的持仓盈亏
		for (LocalPositionDetail localPositionDetail : getLocalPositionDetails()) {
			if (localPositionDetail.getRtSymbol().equals(tick.getRtSymbol())) {
				localPositionDetail.updateLastPrice(tick.getLastPrice());
			}
		}
		tickMap.put(tick.getRtTickID(), tick);

	}

	@Override
	public Contract getContractByFuzzySymbol(String fuzzySymbol) {
		if (StringUtils.isEmpty(fuzzySymbol)) {
			log.error("查询合约不允许使用空字符串或null");
			return null;
		} else {
			return mixContractMap.get(fuzzySymbol);
		}
	}

	@Override
	public Contract getContract(String rtSymbol, String gatewayID) {
		if (StringUtils.isEmpty(rtSymbol)) {
			log.error("查询合约,rtSymbol不允许使用空字符串或null");
			return null;
		} else if (StringUtils.isEmpty(gatewayID)) {
			log.error("查询合约,gatewayID不允许使用空字符串或null");
			return null;
		} else {
			return contractMap.get(rtSymbol + "." + gatewayID);
		}
	}

	@Override
	public List<Contract> getContracts() {
		return new ArrayList<Contract>(new HashSet<>(contractMap.values()));
	}

	@Override
	public List<Tick> getTicks() {
		return new ArrayList<Tick>(tickMap.values());
	}

	@Override
	public List<Account> getAccounts() {
		return new ArrayList<Account>(accountMap.values());
	}

	@Override
	public Account getAccount(String rtAccountID) {
		return accountMap.get(rtAccountID);
	}

	@Override
	public List<Order> getOrders() {
		return new ArrayList<Order>(orderMap.values());
	}

	@Override
	public List<Order> getWorkingOrders() {
		return new ArrayList<Order>(workingOrderMap.values());
	}

	@Override
	public List<Trade> getTrades() {
		return new ArrayList<Trade>(tradeMap.values());
	}

	@Override
	public List<LocalPositionDetail> getLocalPositionDetails() {
		return new ArrayList<>(localPositionDetailMap.values());
	}

	@Override
	public List<Position> getPositions() {
		return new ArrayList<>(positionMap.values());
	}

	public LocalPositionDetail createLocalPositionDetail(String gatewayID, String gatewayDisplayName, String accountID,
			String rtAccountID, String exchange, String rtSymbol, String symbol) {

		String positionDetailKey = rtSymbol + "." + rtAccountID;
		Contract contract = getContract(rtSymbol, gatewayID);

		int contractSize = 0;
		String contractName = null;
		// 在CTP重新登陆时可能存在查不到合约的情况
		if (contract != null) {
			exchange = contract.getExchange();
			symbol = contract.getSymbol();
			contractName = contract.getName();
			contractSize = contract.getSize();
		}
		LocalPositionDetail localPositionDetail = new LocalPositionDetail(gatewayID, gatewayDisplayName, accountID,
				rtAccountID, exchange, rtSymbol, symbol, contractName, contractSize);
		localPositionDetailMap.put(positionDetailKey, localPositionDetail);
		return localPositionDetail;

	}

	@Override
	public String sendOrder(OrderReq orderReq) {
		Gateway gateway = getGateway(orderReq.getGatewayID());
		if (gateway != null) {
			String rtOrderID = gateway.sendOrder(orderReq);
			originalOrderIDMap.put(rtOrderID, orderReq.getOriginalOrderID());
			Account account = getAccount(orderReq.getRtAccountID());
			if (account == null) {
				log.error("发单失败,未能查询到账户,账户ID-[{}]", orderReq.getRtAccountID());
				return null;
			}else {
				orderReq.setAccountID(account.getAccountID());
				orderReq.setRtAccountID(account.getRtAccountID());
			}
			
			orderReq.setGatewayDisplayName(gateway.getGatewayDisplayName());
			if (StringUtils.isNotBlank(rtOrderID)) {
				// 更新到本地持仓
				updateOrderReq(orderReq, rtOrderID);
			}
			return rtOrderID;
		} else {
			log.error("发送委托失败,未能找到网关,OrderReq-{}", JSON.toJSONString(orderReq));
			return null;
		}
	}

	@Override
	public MongoDBService getDataEngine() {
		return mongoDBService;
	}

	@Override
	public Order getOrder(String rtOrderID) {
		return orderMap.get(rtOrderID);
	}

	@Override
	public void cancelOrder(CancelOrderReq cancelOrderReq) {
		Gateway gateway = getGateway(cancelOrderReq.getGatewayID());
		if (gateway != null) {
			gateway.cancelOrder(cancelOrderReq);
		} else {
			log.error("撤单失败,未能找到网关,cancelOrderReq-{}", JSON.toJSONString(cancelOrderReq));
		}
	}

	@Override
	public boolean subscribe(SubscribeReq subscribeReq, String subscriberID) {

		if (subscribeReq == null || StringUtils.isBlank(subscribeReq.getSymbol())
				|| StringUtils.isBlank(subscribeReq.getRtSymbol()) || StringUtils.isBlank(subscribeReq.getGatewayID())
				|| StringUtils.isBlank(subscribeReq.getExchange())) {
			log.warn("订阅行情失败,未能提供有效信息,订阅者ID[{}],subscribeReq-{}", subscriberID, subscribeReq);
		}

		String gatewayID = subscribeReq.getGatewayID();

		log.warn("订阅行情,合约[{}]网关[{}]订阅者ID[{}],", subscribeReq.getRtSymbol(), subscribeReq.getGatewayID(), subscriberID);

		if (subscriberID != null) {
			String subscriberRelationshipKey = subscribeReq.getRtSymbol() + "." + subscribeReq.getGatewayID();

			Set<String> subscriberIDSet;
			if (subscriberRelationshipMap.containsKey(subscriberRelationshipKey)) {
				subscriberIDSet = subscriberRelationshipMap.get(subscriberRelationshipKey);
			} else {
				subscriberIDSet = new HashSet<>();
				subscriberRelationshipMap.put(subscriberRelationshipKey, subscriberIDSet);
			}
			subscriberIDSet.add(subscriberID);
		}

		// 加入主引擎缓存,如果网关重连,会自动重新订阅
		HashSet<SubscribeReq> subscribedSymbols = null;
		if (subscribeReqSetMap.containsKey(gatewayID)) {
			subscribedSymbols = subscribeReqSetMap.get(gatewayID);
		} else {
			subscribedSymbols = new HashSet<>();
			subscribeReqSetMap.put(gatewayID, subscribedSymbols);
		}
		subscribedSymbols.add(subscribeReq);

		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			gateway.subscribe(subscribeReq);
			log.info("订阅行情成功,网关ID-[{}],代码-[{}],订阅者ID-[{}]", subscribeReq.getGatewayID(), subscribeReq.getRtSymbol(),
					subscriberID);
			return true;
		} else {
			log.error("订阅行情失败,未找到网关,网关ID-[{}],代码-[{}],订阅者ID-[{}]", subscribeReq.getGatewayID(),
					subscribeReq.getRtSymbol(), subscriberID);
			return false;
		}
	}

	@Override
	public boolean unsubscribe(String rtSymbol, String gatewayID, String subscriberID) {
		if (StringUtils.isEmpty(rtSymbol) || StringUtils.isEmpty(gatewayID)) {
			log.error("无法取消订阅,参数不允许为空!");
			return false;
		}
		String subscriberRelationshipKey = rtSymbol + "." + gatewayID;

		Set<String> subscriberIDSet = null;
		if (subscriberRelationshipMap.containsKey(subscriberRelationshipKey)) {
			subscriberIDSet = subscriberRelationshipMap.get(subscriberRelationshipKey);
		}

		if (subscriberID != null && subscriberIDSet != null) {
			subscriberIDSet.remove(subscriberID);
		}
		if (subscriberIDSet == null || subscriberIDSet.isEmpty()) {

			subscriberRelationshipMap.remove(subscriberRelationshipKey);

			// 从断开重新注册Map中删除
			if (subscribeReqSetMap.containsKey(gatewayID)) {
				Set<SubscribeReq> subscribeReqSet = subscribeReqSetMap.get(gatewayID);
				subscribeReqSet = subscribeReqSet.stream().filter(value -> !value.getRtSymbol().equals(rtSymbol))
						.collect(Collectors.toSet());
			}

			Gateway gateway = getGateway(gatewayID);
			if (gateway != null) {
				gateway.unSubscribe(rtSymbol);
				// 删除Tick缓存
				tickMap = tickMap.entrySet().stream()
						.filter(map -> !(map.getValue().getRtSymbol().equals(rtSymbol)
								&& map.getValue().getGatewayID().equals(gatewayID)))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

				log.info("成功取消订阅行情,网关ID-[{}],代码-[{}],订阅者ID-[{}]", gatewayID, rtSymbol, subscriberID);

				emitSimpleEvent(EventConstant.EVENT_TICKS_CHANGED, EventConstant.EVENT_TICKS_CHANGED);

				return true;
			} else {
				log.error("取消订阅行情失败,未找到网关,网关ID-[{}],代码-[{}],订阅者ID-[{}]", gatewayID, rtSymbol, subscriberID);
				return false;
			}
		} else {
			log.error("取消订阅行情失败,存在其它订阅者,网关ID-[{}],代码-[{}],订阅者ID-[{}]", gatewayID, rtSymbol, subscriberID);
			return false;
		}
	}

	@Override
	public void updateOrderReq(OrderReq orderReq, String rtOrderID) {

		LocalPositionDetail localPositionDetail;
		String positionDetailKey = orderReq.getRtSymbol() + "." + orderReq.getRtAccountID();
		if (localPositionDetailMap.containsKey(positionDetailKey)) {
			localPositionDetail = localPositionDetailMap.get(positionDetailKey);
		} else {
			localPositionDetail = createLocalPositionDetail(orderReq.getGatewayID(), orderReq.getGatewayDisplayName(),
					orderReq.getAccountID(), orderReq.getRtAccountID(), orderReq.getExchange(), orderReq.getRtSymbol(),
					orderReq.getSymbol());
		}

		localPositionDetail.updateOrderReq(orderReq, rtOrderID);
	}

	@Override
	public Gateway getGateway(String gatewayID) {
		if (StringUtils.isEmpty(gatewayID)) {
			log.error("查询合约,gatewayID不允许使用空字符串或null");
			return null;
		}
		return gatewayMap.get(gatewayID);
	}

	@Override
	public synchronized void disconnectGateway(String gatewayID) {
		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			if (gateway.isConnected()) {
				gateway.close();
			}
			gatewayMap.remove(gatewayID);

			// 删除账户缓存
			accountMap = accountMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 删除持仓详细缓存
			localPositionDetailMap = localPositionDetailMap.entrySet().stream()
					.filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 删除持仓缓存
			positionMap = positionMap.entrySet().stream()
					.filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 删除委托缓存
			orderMap = orderMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 删除成交缓存
			tradeMap = tradeMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 删除未完成委托缓存
			workingOrderMap = workingOrderMap.entrySet().stream()
					.filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 删除Tick缓存
			tickMap = tickMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 删除Contract缓存
			contractMap = contractMap.entrySet().stream()
					.filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			mixContractMap = contractMap.entrySet().stream()
					.filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			// 等待再发送事件,留出少量通讯时间
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// nop
			}
			emitSimpleEvent(EventConstant.EVENT_GATEWAY, EventConstant.EVENT_GATEWAY);
		} else {
			log.error("网关{}不存在,无法断开!", gatewayID);
		}
	}

	@Override
	public List<String> scanGatewayImpl() {
		List<String> gatewayNameList = new ArrayList<>();
		Set<Class<?>> classes = CommonUtil.getClasses("xyz.redtorch.trader");
		if (classes == null) {
			log.error("未能在包xyz.redtorch.trader下扫描到任何类");
		} else {
			// 寻找Gateway的实现类,不包含抽象类
			Set<Class<?>> filteredClasses = CommonUtil.getImplementsByInterface(Gateway.class, classes, false);
			if (filteredClasses.isEmpty()) {
				log.error("未能在包xyz.redtorch.trader下扫描到任何Gateway网关的实现类");
			} else {
				for (Class<?> clazz : filteredClasses) {
					String className = clazz.getSimpleName();
					gatewayNameList.add(className);
				}
			}
		}
		return gatewayNameList;
	}

	@Override
	public synchronized void connectGateway(String gatewayID) {
		if (gatewayMap.containsKey(gatewayID)) {
			log.warn("网关已在缓存中存在,网关ID-[{}]", gatewayID);
			return;
		}
		log.warn("连接网关,ID-[{}]", gatewayID);
		GatewaySetting gatewaySetting = coreEngineDataService.queryGatewaySetting(gatewayID);

		if (gatewaySetting == null) {
			log.warn("无法连接网关,数据库中不存在,ID-[{}]", gatewayID);
			return;
		}

		String gatewayClassName = gatewaySetting.getGatewayClassName();

		try {
			Class<?> clazz = Class.forName(gatewayClassName);
			Constructor<?> c = clazz.getConstructor(FastEventEngineService.class, GatewaySetting.class);
			Gateway gateway = (Gateway) c.newInstance(fastEventEngineService, gatewaySetting);
			gateway.connect();
			// 重新订阅之前的合约
			if (subscribeReqSetMap.containsKey(gatewayID)) {
				HashSet<SubscribeReq> subscribeReqSet = subscribeReqSetMap.get(gatewayID);
				for (SubscribeReq subscribeReq : subscribeReqSet) {
					gateway.subscribe(subscribeReq);
				}
			}
			gatewayMap.put(gateway.getGatewayID(), gateway);
			// 等待再发送事件,留出少量通讯时间
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// nop
			}
			emitSimpleEvent(EventConstant.EVENT_GATEWAY, EventConstant.EVENT_GATEWAY);

		} catch (Exception e) {
			log.error("创建网关实例发生异常,网关ID-[{}],Java实现类-[{}],GatewaySetting-{}", gatewayID, gatewayClassName,
					JSON.toJSONString(gatewaySetting), e);
		}

		log.warn("连接网关完成,ID-[{}]", gatewayID);
	}

	@Override
	public void saveGateway(GatewaySetting gatewaySetting) {
		coreEngineDataService.saveGatewaySetting(gatewaySetting);
	}

	@Override
	public void deleteGateway(String gatewayID) {
		disconnectGateway(gatewayID);
		coreEngineDataService.deleteGatewaySetting(gatewayID);
	}

	@Override
	public List<Gateway> getGateways() {
		List<Gateway> gatewayList = new ArrayList<>(gatewayMap.values());
		return gatewayList;
	}

	@Override
	public void updateGateway(GatewaySetting gatewaySetting) {
		String gatewayID = gatewaySetting.getGatewayID();

		boolean isLoaded = false;
		if (getGateway(gatewayID) != null) {
			disconnectGateway(gatewaySetting.getGatewayID());
			isLoaded = true;
		}

		coreEngineDataService.deleteGatewaySetting(gatewayID);
		coreEngineDataService.saveGatewaySetting(gatewaySetting);

		// 重新连接
		if (isLoaded) {
			connectGateway(gatewayID);
		}
	}

	@Override
	public GatewaySetting queryGatewaySetting(String gatewayID) {
		return coreEngineDataService.queryGatewaySetting(gatewayID);
	}

	@Override
	public List<GatewaySetting> queryGatewaySettings() {
		return coreEngineDataService.queryGatewaySettings();
	}

	@Override
	public List<LogData> getLogDatas() {
		return logDataList;
	}

	private void emitSimpleEvent(String eventType, String event) {
		RingBuffer<FastEvent> ringBuffer = fastEventEngineService.getRingBuffer();
		long sequence = ringBuffer.next(); // Grab the next sequence
		try {
			FastEvent fastEvent = ringBuffer.get(sequence); // Get the entry in the Disruptor for the sequence
			fastEvent.setEventType(eventType);
			fastEvent.setEvent(event);

		} finally {
			ringBuffer.publish(sequence);
		}
	}

}
