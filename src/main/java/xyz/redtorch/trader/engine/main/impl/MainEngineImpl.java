package xyz.redtorch.trader.engine.main.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.engine.data.impl.DataEngineImpl;
import xyz.redtorch.trader.engine.event.EventConstant;
import xyz.redtorch.trader.engine.event.EventData;
import xyz.redtorch.trader.engine.event.EventEngine;
import xyz.redtorch.trader.engine.event.impl.EventEngineImpl;
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
import xyz.redtorch.trader.module.Module;
import xyz.redtorch.utils.CommonUtil;

/**
 * @author sun0x00@gmail.com
 */
public class MainEngineImpl implements MainEngine {
	
	
	private static Logger log = LoggerFactory.getLogger(MainEngineImpl.class);

	LinkedBlockingQueue<EventData> eventDataQueue = new LinkedBlockingQueue<>();

	private EventEngine eventEngine;
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
	
	private Map<String, HashSet<SubscribeReq>> subscribeReqSetMap = new HashMap<>();

	public MainEngineImpl() {
		log.info("事件引擎初始化");
		this.eventEngine = new EventEngineImpl();
		try {
			log.info("数据引擎初始化");
			dataEngine  = new DataEngineImpl();
			mainDataUtil = new MainDataUtilImpl(dataEngine);
		}catch (Exception e) {
			log.error("数据引擎初始化失败,程序退出",e);
			System.exit(1);
		}
		eventEngine.registerListener(EventConstant.EVENT_TICK, this);
		eventEngine.registerListener(EventConstant.EVENT_TRADE, this);
		eventEngine.registerListener(EventConstant.EVENT_ORDER, this);
		eventEngine.registerListener(EventConstant.EVENT_POSITION, this);
		eventEngine.registerListener(EventConstant.EVENT_ACCOUNT, this);
		eventEngine.registerListener(EventConstant.EVENT_CONTRACT, this);
		eventEngine.registerListener(EventConstant.EVENT_ERROR, this);
		eventEngine.registerListener(EventConstant.EVENT_GATEWAY, this);
		eventEngine.registerListener(EventConstant.EVENT_LOG, this);
		//eventEngine.registerListener("", this);
		
	}

	@Override
	public void onEvent(EventData eventData) {
		if (eventData != null) {
			eventDataQueue.add(eventData);
		}
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			EventData ed = null;
			try {
				ed = eventDataQueue.take();
			} catch (InterruptedException e) {
				log.error("主引擎捕获到线程中断异常，线程停止！！！", e);
			}
			// 判断消息类型
			if (EventConstant.EVENT_TICK.equals(ed.getEventType())) {
				try {
					Tick tick = (Tick) ed.getEventObj();
					onTick(tick);
				} catch (Exception e) {
					log.error("主引擎onTick发生异常！！！", e);
				}
			} else if (EventConstant.EVENT_TRADE.equals(ed.getEventType())) {
				try {
					Trade trade = (Trade) ed.getEventObj();
					onTrade(trade);
				} catch (Exception e) {
					log.error("主引擎onTrade发生异常！！！", e);
				}
			} else if (EventConstant.EVENT_ORDER.equals(ed.getEventType())) {
				try {
					Order order = (Order) ed.getEventObj();
					onOrder(order);
				} catch (Exception e) {
					log.error("主引擎onOrder发生异常！！！", e);
				}
			} else if (EventConstant.EVENT_CONTRACT.equals(ed.getEventType())) {
				try {
					Contract contract = (Contract) ed.getEventObj();
					onContract(contract);
				} catch (Exception e) {
					log.error("主引擎onContract发生异常！！！", e);
				}
			} else if (EventConstant.EVENT_POSITION.equals(ed.getEventType())) {
				try {
					Position position = (Position) ed.getEventObj();
					onPosition(position);
				} catch (Exception e) {
					log.error("主引擎onPosition发生异常！！！", e);
				}
			} else if (EventConstant.EVENT_ACCOUNT.equals(ed.getEventType())) {
				try {
					Account account = (Account) ed.getEventObj();
					onAccount(account);
				} catch (Exception e) {
					log.error("主引擎onAccount发生异常！！！", e);
				}
			} else if (EventConstant.EVENT_LOG.equals(ed.getEventType())) {
				try {
					LogData logData = (LogData) ed.getEventObj();
					onLogData(logData);
				} catch (Exception e) {
					log.error("主引擎onLogData生异常！！！", e);
				}
			} else if(EventConstant.EVENT_THREAD_STOP.equals(ed.getEventType())){
				// 弃用
				//Thread.currentThread().interrupt();
				break;
			} else {
				log.warn("主引擎未能识别的事件数据类型{}", JSON.toJSONString(ed));
			}
		}
	}

	private void onLogData(LogData logData) {
		logDataList.add(logData);
	}

	private void onContract(Contract contract) {
		contractMap.put(contract.getSymbol(), contract); // 常规代码不包含交易所，可能会合约导致重复
		contractMap.put(contract.getRtSymbol(), contract); // 多个接口同一个交易所之间的代码可能重复
		contractMap.put(contract.getSymbol() + "." + contract.getGatewayID(), contract);
		contractMap.put(contract.getRtSymbol() + "." + contract.getGatewayID(), contract);
		
		// 补充要素,修正CTP重连时Trade先于Contract到达导致的要素缺失
		String exchange = contract.getExchange(); 
		String symbol = contract.getSymbol();
		String contractName = contract.getName();
		int contractSize = contract.getSize();
		String positionDetailKey = contract.getRtSymbol()+"."+contract.getGatewayID();
		LocalPositionDetail localPositionDetail = localPositionDetailMap.get(positionDetailKey);
		if(localPositionDetail!=null) {
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
		if(RtConstant.STATUS_FINISHED.contains(order.getStatus())){
			if(workingOrderMap.containsKey(order.getRtOrderID())){
				workingOrderMap.remove(order.getRtOrderID());
			}
		}else {
			workingOrderMap.put(order.getRtOrderID(), order);
		}
		
		LocalPositionDetail localPositionDetail= getLocalPositionDetail(order.getRtSymbol(),order.getGatewayID());
		localPositionDetail.updateOrder(order);
	}

	private void onTrade(Trade trade) {
		LocalPositionDetail localPositionDetail= getLocalPositionDetail(trade.getRtSymbol(),trade.getGatewayID());
		localPositionDetail.updateTrade(trade);
		tradeMap.put(trade.getRtTradeID(), trade);
	}

	private void onPosition(Position position) {
		
		Contract contract = getContract(position.getSymbol(), position.getGatewayID());
		String rtSymbol = contract.getSymbol() + "." + position.getExchange();
	
		
		LocalPositionDetail localPositionDetail= getLocalPositionDetail(rtSymbol,position.getGatewayID());
		localPositionDetail.updatePosition(position);
		
		positionMap.put(position.getRtPositionName(), position);
	
	}

	private void onTick(Tick tick) {
		// 更新指定接口的持仓盈亏
		//LocalPositionDetail localPositionDetail= getLocalPositionDetail(tick.getRtSymbol(),tick.getGatewayID());
		//localPositionDetail.updateLastPrice(tick.getLastPrice());
		
		// 更新所有相同rtSymbol的持仓盈亏
		for(LocalPositionDetail  localPositionDetail:getLocalPositionDetails()) {
			if(localPositionDetail.getRtSymbol().equals(tick.getRtSymbol())) {
				localPositionDetail.updateLastPrice(tick.getLastPrice());
			}
		}
	
	}

	@Override
	public Contract getContract(String rtSymbol) {
		if (StringUtils.isEmpty(rtSymbol)) {
			log.error("查询合约不允许使用空字符串或null");
			return null;
		} else {
			return contractMap.get(rtSymbol);
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
	public List<LocalPositionDetail> getLocalPositionDetails(){
		return new ArrayList<>(localPositionDetailMap.values());
	}
	
	@Override
	public List<Position> getPositions() {
		return new ArrayList<>(positionMap.values());
	}

	@Override
	public LocalPositionDetail getLocalPositionDetail(String rtSymbol, String gatewayID) {
	
		String positionDetailKey = rtSymbol+"."+gatewayID;
		if(localPositionDetailMap.containsKey(positionDetailKey)) {
			return localPositionDetailMap.get(positionDetailKey);
		}else {
			Gateway gateway = getGateway(gatewayID);
			String gatewayDisplayName = gateway.getGatewayDisplayName();
			
			String exchange = "";
			String symbol = "";
			String contractName = "";
			int contractSize = 0;
			Contract contract = getContract(rtSymbol, gatewayID);
			
			// 在CTP重新登陆是可能存在查不到合约的情况
			if(contract!=null) {
				exchange = contract.getExchange(); 
				symbol = contract.getSymbol();
				contractName = contract.getName();
				contractSize = contract.getSize();
			}
			
			LocalPositionDetail localPositionDetail = new LocalPositionDetail(gatewayID,gatewayDisplayName, exchange, rtSymbol, symbol, contractName,contractSize);
			
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
			updateOrderReq(orderReq,rtOrderID);
			return rtOrderID;
		} else {
			log.error("主引擎未能找到接口,OrderReq{}", JSON.toJSONString(orderReq));
			return null;
		}
	}

	@Override
	public void queryAccount(String gatewayID) {
		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			gateway.queryAccount();
		} else {
			log.error("主引擎未能找到接口");
		}
	}

	@Override
	public void queryPosition(String gatewayID) {
		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			gateway.queryPosition();
		} else {
			log.error("主引擎未能找到接口");
		}
	}

	@Override
	public EventEngine getEventEngine() {
		return eventEngine;
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
			log.error("主引擎未能找到接口,cancelOrderReq{}", JSON.toJSONString(cancelOrderReq));
		}
	}

	@Override
	public boolean subscribe(SubscribeReq subscribeReq) {
		Contract contract;
		if(StringUtils.isEmpty(subscribeReq.getGatewayID())) {
			contract = getContract(subscribeReq.getRtSymbol());
		}else {
			contract = getContract(subscribeReq.getRtSymbol(),subscribeReq.getGatewayID());
		}
		if(contract == null) {
			log.error("主引擎未能找到合约,SubscribeReq-{}", JSON.toJSONString(subscribeReq));
			return false;
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
		// 加入主引擎缓存，如果接口重连，会自动重新订阅
		HashSet<SubscribeReq> subscribedSymbols = null;
		if(subscribeReqSetMap.containsKey(gatewayID)) {
			subscribedSymbols = subscribeReqSetMap.get(gatewayID);
		}else {
			subscribedSymbols = new HashSet<>();
			subscribeReqSetMap.put(gatewayID, subscribedSymbols);
		}
		subscribedSymbols.add(subscribeReq);
		
		Gateway gateway = getGateway(gatewayID);
		if (gateway != null) {
			gateway.subscribe(subscribeReq);
			return true;
		} else {
			log.error("主引擎未能找到接口{},SubscribeReq{}", gatewayID, JSON.toJSONString(subscribeReq));
			return false;
		}
	}

	@Override
	public void updateOrderReq(OrderReq orderReq, String rtOrderID) {
		LocalPositionDetail localPositionDetail = getLocalPositionDetail(orderReq.getRtSymbol(), orderReq.getGatewayID());
		localPositionDetail.updateOrderReq(orderReq, rtOrderID);
	}
	
	@Override
	public void stop() {
		eventEngine.removeListener(null, this);
		// 通知其他线程
		EventData eventData = new EventData();
		eventData.setEvent(EventConstant.EVENT_THREAD_STOP);
		eventData.setEventType(EventConstant.EVENT_THREAD_STOP);
		eventDataQueue.add(eventData);
	}

	@Override
	public Gateway getGateway(String gatewayID) {
		if (StringUtils.isEmpty(gatewayID)) {
			log.error("查找接口时GatewayID不允许为空字符串或null");
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
			accountMap = accountMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			// 删除持仓详细缓存
			localPositionDetailMap = localPositionDetailMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			// 删除持仓缓存
			positionMap = positionMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			// 删除委托缓存
			orderMap = orderMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			// 删除成交缓存
			tradeMap = tradeMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			
			// 删除为完成委托缓存
			workingOrderMap = workingOrderMap.entrySet().stream().filter(map -> !map.getValue().getGatewayID().equals(gatewayID)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
					
		} else {
			log.warn("接口{}不存在,断开无效", gatewayID);
		}
	}

	@Override
	public List<String> scanGatewayImpl() {
		List<String> gatewayNameList = new ArrayList<>();
		Set<Class<?>> classes = CommonUtil.getClasses("xyz.redtorch.trader");
		if (classes == null) {
			log.error("{} 未能在包xyz.redtorch.trader下扫描到任何类");
		} else {
			// 寻找Gateway的实现类，不包含抽象类
			Set<Class<?>> filteredClasses = CommonUtil.getImplementsByInterface(Gateway.class, classes, false);
			if (filteredClasses.isEmpty()) {
				log.error("主引擎未能在包xyz.redtorch.trader下扫描到任何实现了Gateway接口的实现类");
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
		
		if(gatewaySetting == null) {
			String logContent = "接口"+gatewayID+"无法连接,数据库中不存在";
			CommonUtil.emitWarnLog(eventEngine, logContent);
			log.warn(logContent);
			return;
		}
		
		String gatewayClassName = gatewaySetting.getGatewayClassName();

		try {
			Class<?> clazz = Class.forName(gatewayClassName);
			Constructor<?> c = clazz.getConstructor(GatewaySetting.class, EventEngine.class);
			Gateway gateway = (Gateway) c.newInstance(gatewaySetting, eventEngine);
			gateway.connect();
			// 重新订阅之前的合约
			if(subscribeReqSetMap.containsKey(gatewayID)) {
				HashSet<SubscribeReq> subscribeReqSet = subscribeReqSetMap.get(gatewayID);
				for(SubscribeReq subscribeReq :subscribeReqSet) {
					gateway.subscribe(subscribeReq);
				}
			}
			gatewayMap.put(gateway.getGatewayID(), gateway);

		} catch (Exception e) {
			CommonUtil.emitErrorLog(eventEngine, "接口"+gatewayID+"无法连接,创建实例异常");
			log.error("创建接口{}实例发生异常,GatewaySetting{}", gatewayClassName, JSON.toJSONString(gatewaySetting), e);
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

	@Override
	public void addModel(Module module) {
		
		// 预留，暂时没用到
	}

}
