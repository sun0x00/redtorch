package xyz.redtorch.trader.engine.main.impl;

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

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.engine.data.impl.DataEngineImpl;
import xyz.redtorch.trader.engine.event.EventConstant;
import xyz.redtorch.trader.engine.event.FastEvent;
import xyz.redtorch.trader.engine.event.FastEventDynamicHandlerAbstract;
import xyz.redtorch.trader.engine.main.MainEngine;
import xyz.redtorch.trader.engine.main.MainDataUtil;
import xyz.redtorch.trader.entity.Account;
import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.Contract;
import xyz.redtorch.trader.entity.LocalPositionDetail;
import xyz.redtorch.trader.entity.LogData;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.Position;
import xyz.redtorch.trader.entity.SubscribeReq;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.gateway.Gateway;
import xyz.redtorch.trader.gateway.GatewaySetting;
import xyz.redtorch.utils.CommonUtil;

/**
 * @author sun0x00@gmail.com
 */
public class MainEngineImpl extends FastEventDynamicHandlerAbstract implements MainEngine {

	private static Logger log = LoggerFactory.getLogger(MainEngineImpl.class);

	private DataEngine dataEngine;

	private MainDataUtil mainDataUtil;

	private Map<String, Gateway> gatewayMap = new HashMap<>();
	private Map<String, Contract> contractMap = new HashMap<>();

	private Map<String, Order> orderMap = new HashMap<>();
	private Map<String, Order> workingOrderMap = new HashMap<>();
	private Map<String, Trade> tradeMap = new HashMap<>();
	private Map<String, Account> accountMap = new HashMap<>();
	private Map<String, LocalPositionDetail> localPositionDetailMap = new HashMap<>();
	private Map<String, Position> positionMap = new HashMap<>();
	private List<LogData> logDataList = new ArrayList<>();

	private Map<String, Set<String>> subscriberRelationshipMap = new HashMap<>();

	private Map<String, HashSet<SubscribeReq>> subscribeReqSetMap = new HashMap<>();

	public MainEngineImpl() {
		log.info("MAIN_ENGINE:事件引擎初始化");
		try {
			log.info("MAIN_ENGINE:数据引擎初始化");
			dataEngine = new DataEngineImpl();
			mainDataUtil = new MainDataUtilImpl(dataEngine);
		} catch (Exception e) {
			log.error("MAIN_ENGINE:数据引擎初始化失败,程序退出", e);
			System.exit(1);
		}
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
				log.error("MAIN_ENGINE:onTick发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())) {
			try {
				Trade trade = fastEvent.getTrade();
				onTrade(trade);
			} catch (Exception e) {
				log.error("MAIN_ENGINE:onTrade发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())) {
			try {
				Order order = fastEvent.getOrder();
				onOrder(order);
			} catch (Exception e) {
				log.error("MAIN_ENGINE:onOrder发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_CONTRACT.equals(fastEvent.getEventType())) {
			try {
				Contract contract = fastEvent.getContract();
				onContract(contract);
			} catch (Exception e) {
				log.error("MAIN_ENGINE:onContract发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_POSITION.equals(fastEvent.getEventType())) {
			try {
				Position position = fastEvent.getPosition();
				onPosition(position);
			} catch (Exception e) {
				log.error("MAIN_ENGINE:onPosition发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_ACCOUNT.equals(fastEvent.getEventType())) {
			try {
				Account account = fastEvent.getAccount();
				onAccount(account);
			} catch (Exception e) {
				log.error("MAIN_ENGINE:onAccount发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_LOG.equals(fastEvent.getEventType())) {
			try {
				LogData logData = fastEvent.getLogData();
				onLogData(logData);
			} catch (Exception e) {
				log.error("MAIN_ENGINE:onLogData发生异常!!!", e);
			}
		} else {
			log.warn("MAIN_ENGINE:未能识别的事件数据类型{}", JSON.toJSONString(fastEvent.getEvent()));
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
		contractMap.put(contract.getSymbol(), contract); // 常规代码不包含交易所,可能会合约导致重复
		contractMap.put(contract.getRtSymbol(), contract); // 多个接口同一个交易所之间的代码可能重复
		contractMap.put(contract.getSymbol() + "." + contract.getGatewayID(), contract);
		contractMap.put(contract.getRtSymbol() + "." + contract.getGatewayID(), contract);

		// CTP重连时Trade可能先于Contract到达,在此处重新赋值
		String exchange = contract.getExchange();
		String symbol = contract.getSymbol();
		String contractName = contract.getName();
		int contractSize = contract.getSize();
		String positionDetailKey = contract.getRtSymbol() + "." + contract.getGatewayID();
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

		LocalPositionDetail localPositionDetail = getLocalPositionDetail(order.getRtSymbol(), order.getGatewayID());
		localPositionDetail.updateOrder(order);
	}

	private void onTrade(Trade trade) {
		LocalPositionDetail localPositionDetail = getLocalPositionDetail(trade.getRtSymbol(), trade.getGatewayID());
		localPositionDetail.updateTrade(trade);
		tradeMap.put(trade.getRtTradeID(), trade);
	}

	private void onPosition(Position position) {

		Contract contract = getContract(position.getSymbol(), position.getGatewayID());
		String rtSymbol = contract.getSymbol() + "." + position.getExchange();

		LocalPositionDetail localPositionDetail = getLocalPositionDetail(rtSymbol, position.getGatewayID());
		localPositionDetail.updatePosition(position);

		positionMap.put(position.getRtPositionName(), position);

	}

	private void onTick(Tick tick) {
		// 更新指定接口的持仓盈亏
		// LocalPositionDetail localPositionDetail=
		// getLocalPositionDetail(tick.getRtSymbol(),tick.getGatewayID());
		// localPositionDetail.updateLastPrice(tick.getLastPrice());

		// 更新所有相同rtSymbol的持仓盈亏
		for (LocalPositionDetail localPositionDetail : getLocalPositionDetails()) {
			if (localPositionDetail.getRtSymbol().equals(tick.getRtSymbol())) {
				localPositionDetail.updateLastPrice(tick.getLastPrice());
			}
		}

	}

	@Override
	public Contract getContract(String rtSymbol) {
		if (StringUtils.isEmpty(rtSymbol)) {
			log.error("MAIN_ENGINE:查询合约不允许使用空字符串或null!!!");
			return null;
		} else {
			return contractMap.get(rtSymbol);
		}
	}

	@Override
	public Contract getContract(String rtSymbol, String gatewayID) {
		if (StringUtils.isEmpty(rtSymbol)) {
			log.error("MAIN_ENGINE:查询合约,rtSymbol不允许使用空字符串或null!!!");
			return null;
		} else if (StringUtils.isEmpty(gatewayID)) {
			log.error("MAIN_ENGINE:查询合约,gatewayID不允许使用空字符串或null!!!");
			return null;
		} else {
			return contractMap.get(rtSymbol + "." + gatewayID);
		}
	}

	@Override
	public List<Contract> getContracts() {
		return new ArrayList<Contract>(contractMap.values());
	}

	@Override
	public List<Account> getAccounts() {
		return new ArrayList<Account>(accountMap.values());
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

	@Override
	public LocalPositionDetail getLocalPositionDetail(String rtSymbol, String gatewayID) {

		String positionDetailKey = rtSymbol + "." + gatewayID;
		if (localPositionDetailMap.containsKey(positionDetailKey)) {
			return localPositionDetailMap.get(positionDetailKey);
		} else {
			Gateway gateway = getGateway(gatewayID);
			String gatewayDisplayName = gateway.getGatewayDisplayName();

			String exchange = "";
			String symbol = "";
			String contractName = "";
			int contractSize = 0;
			Contract contract = getContract(rtSymbol, gatewayID);

			// 在CTP重新登陆时可能存在查不到合约的情况
			if (contract != null) {
				exchange = contract.getExchange();
				symbol = contract.getSymbol();
				contractName = contract.getName();
				contractSize = contract.getSize();
			}

			LocalPositionDetail localPositionDetail = new LocalPositionDetail(gatewayID, gatewayDisplayName, exchange,
					rtSymbol, symbol, contractName, contractSize);

			localPositionDetailMap.put(positionDetailKey, localPositionDetail);
			return localPositionDetail;
		}

	}

	@Override
	public String sendOrder(OrderReq orderReq) {
		Gateway gateway = getGateway(orderReq.getGatewayID());
		if (gateway != null) {
			String rtOrderID = gateway.sendOrder(orderReq);
			// 更新到本地持仓
			updateOrderReq(orderReq, rtOrderID);
			return rtOrderID;
		} else {
			log.error("MAIN_ENGINE:发送委托失败,未能找到接口,OrderReq-{}", JSON.toJSONString(orderReq));
			return null;
		}
	}

	@Override
	public void queryAccount(String gatewayID) {
		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			gateway.queryAccount();
		} else {
			log.error("MAIN_ENGINE:查询账户失败,未能找到接口{}", gatewayID);
		}
	}

	@Override
	public void queryPosition(String gatewayID) {
		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			gateway.queryPosition();
		} else {
			log.error("MAIN_ENGINE:查询持仓失败,未能找到接口{}", gatewayID);
		}
	}

	@Override
	public DataEngine getDataEngine() {
		return dataEngine;
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
			log.error("MAIN_ENGINE: 撤单失败,未能找到接口,cancelOrderReq-", JSON.toJSONString(cancelOrderReq));
		}
	}

	@Override
	public boolean subscribe(SubscribeReq subscribeReq, String subscriberID) {

		Contract contract;
		if (StringUtils.isEmpty(subscribeReq.getGatewayID())) {
			contract = getContract(subscribeReq.getRtSymbol());
		} else {
			contract = getContract(subscribeReq.getRtSymbol(), subscribeReq.getGatewayID());
		}
		if (contract == null) {
			log.error("MAIN_ENGINE:无法订阅行情,合约[{}]接口[{}]订阅者ID[{}],未找到Contract", subscribeReq.getRtSymbol(),
					subscribeReq.getGatewayID(), subscriberID);
			return false;
		}

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

		String gatewayID = contract.getGatewayID();
		subscribeReq.setRtSymbol(contract.getRtSymbol());
		subscribeReq.setSymbol(contract.getSymbol());
		subscribeReq.setGatewayID(gatewayID);
		subscribeReq.setProductClass(contract.getProductClass());
		subscribeReq.setExchange(contract.getExchange());
		subscribeReq.setOptionType(contract.getOptionType());
		subscribeReq.setStrikePrice(contract.getStrikePrice());
		subscribeReq.setExpiry(contract.getExpiryDate());
		// 加入主引擎缓存,如果接口重连,会自动重新订阅
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
			log.info("MAIN_ENGINE:成功订阅行情,合约[{}]接口[{}]订阅者ID[{}]", subscribeReq.getRtSymbol(),
					subscribeReq.getGatewayID(), subscriberID);
			return true;
		} else {
			log.error("MAIN_ENGINE:成功订阅行情,合约[{}]接口[{}]订阅者ID[{}]未找到接口", subscribeReq.getRtSymbol(),
					subscribeReq.getGatewayID(), subscriberID);
			return false;
		}
	}

	@Override
	public boolean unsubscribe(String rtSymbol, String gatewayID, String subscriberID) {
		if (StringUtils.isEmpty(rtSymbol) || StringUtils.isEmpty(gatewayID)) {
			log.error("MAIN_ENGINE:无法取消订阅,参数不允许为空!");
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
				String[] rtSymbolArray = rtSymbol.split("\\.");
				if (rtSymbolArray.length > 1) {
					gateway.unSubscribe(rtSymbolArray[0]);
				}
				log.info("MAIN_ENGINE:成功取消订阅行情,合约[{}]接口[{}]订阅者ID[{}]", rtSymbol, gatewayID, subscriberID);
				return true;
			} else {
				log.error("MAIN_ENGINE:取消订阅行情失败,合约[{}]接口[{}]订阅者ID[{}],未找到接口", rtSymbol, gatewayID, subscriberID);
				return false;
			}
		} else {
			log.error("MAIN_ENGINE:取消订阅行情失败,合约[{}]接口[{}]订阅者ID[{}],存在其它订阅者", rtSymbol, gatewayID, subscriberID);
			return false;
		}
	}

	@Override
	public void updateOrderReq(OrderReq orderReq, String rtOrderID) {
		LocalPositionDetail localPositionDetail = getLocalPositionDetail(orderReq.getRtSymbol(),
				orderReq.getGatewayID());
		localPositionDetail.updateOrderReq(orderReq, rtOrderID);
	}

	@Override
	public Gateway getGateway(String gatewayID) {
		if (StringUtils.isEmpty(gatewayID)) {
			log.error("MAIN_ENGINE:查询合约,gatewayID不允许使用空字符串或null!!!");
			return null;
		}
		return gatewayMap.get(gatewayID);
	}

	@Override
	public void disconnectGateway(String gatewayID) {
		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			gateway.close();
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

			// 删除为完成委托缓存
			workingOrderMap = workingOrderMap.entrySet().stream()
					.filter(map -> !map.getValue().getGatewayID().equals(gatewayID))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		} else {
			log.error("MAIN_ENGINE:接口{}不存在,无法断开!", gatewayID);
		}
	}

	@Override
	public List<String> scanGatewayImpl() {
		List<String> gatewayNameList = new ArrayList<>();
		Set<Class<?>> classes = CommonUtil.getClasses("xyz.redtorch.trader");
		if (classes == null) {
			log.error("MAIN_ENGINE:未能在包xyz.redtorch.trader下扫描到任何类");
		} else {
			// 寻找Gateway的实现类,不包含抽象类
			Set<Class<?>> filteredClasses = CommonUtil.getImplementsByInterface(Gateway.class, classes, false);
			if (filteredClasses.isEmpty()) {
				log.error("MAIN_ENGINE:未能在包xyz.redtorch.trader下扫描到任何Gateway接口的实现类");
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
	public void connectGateway(String gatewayID) {

		GatewaySetting gatewaySetting = mainDataUtil.queryGatewaySetting(gatewayID);

		if (gatewaySetting == null) {
			log.warn("MAIN_ENGINE:接口{}无法连接,数据库中不存在", gatewayID);
			return;
		}

		String gatewayClassName = gatewaySetting.getGatewayClassName();

		try {
			Class<?> clazz = Class.forName(gatewayClassName);
			Constructor<?> c = clazz.getConstructor(GatewaySetting.class);
			Gateway gateway = (Gateway) c.newInstance(gatewaySetting);
			gateway.connect();
			// 重新订阅之前的合约
			if (subscribeReqSetMap.containsKey(gatewayID)) {
				HashSet<SubscribeReq> subscribeReqSet = subscribeReqSetMap.get(gatewayID);
				for (SubscribeReq subscribeReq : subscribeReqSet) {
					gateway.subscribe(subscribeReq);
				}
			}
			gatewayMap.put(gateway.getGatewayID(), gateway);

		} catch (Exception e) {
			log.error("MAIN_ENGINE:接口ID{},创建接口{}实例发生异常,GatewaySetting{}", gatewayID, gatewayClassName,
					JSON.toJSONString(gatewaySetting), e);
		}
	}

	@Override
	public void saveGateway(GatewaySetting gatewaySetting) {
		mainDataUtil.saveGatewaySetting(gatewaySetting);
	}

	@Override
	public void deleteGateway(String gatewayID) {
		disconnectGateway(gatewayID);
		mainDataUtil.deleteGatewaySetting(gatewayID);
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

		mainDataUtil.deleteGatewaySetting(gatewayID);
		mainDataUtil.saveGatewaySetting(gatewaySetting);

		// 重新连接
		if (isLoaded) {
			connectGateway(gatewayID);
		}
	}

	@Override
	public GatewaySetting queryGatewaySetting(String gatewayID) {
		return mainDataUtil.queryGatewaySetting(gatewayID);
	}

	@Override
	public List<GatewaySetting> queryGatewaySettings() {
		return mainDataUtil.queryGatewaySettings();
	}

	@Override
	public List<LogData> getLogDatas() {
		return logDataList;
	}
}
