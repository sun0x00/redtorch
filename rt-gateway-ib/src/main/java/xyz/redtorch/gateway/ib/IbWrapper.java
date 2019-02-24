package xyz.redtorch.gateway.ib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Bar;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;
import com.ib.client.ContractDetails;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.DepthMktDataDescription;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.FamilyCode;
import com.ib.client.HistogramEntry;
import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.client.NewsProvider;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.PriceIncrement;
import com.ib.client.SoftDollarTier;
import com.ib.client.TagValue;
import com.ib.client.TickAttr;

import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;


public class IbWrapper implements EWrapper {
	
	private Logger log = LoggerFactory.getLogger(IbWrapper.class);
	
	// main client
	private EJavaSignal eJavaSignal = new EJavaSignal();
	private EClientSocket eClientSocket = new EClientSocket(this, eJavaSignal);
	
	private ExecutorService executor = Executors.newFixedThreadPool(2);

	/* 
	注意事项：
	1. 只能获取和操作当前连接后发生的数据，包括定单，成交信息，重连后丢失
	2.持仓和账户更新可以订阅为主推模式，因此不必主动查询持仓和账户
	3. 目前只支持股票和期货交易
	4 海外市场的交易规则和国内有很多细节上的不同，所以一些字段类型的映射可能不合理，如果发现问题欢迎指出
	 */

	// 连接地址
	private String host;
	// 连接端口
	private int port;
	// 用户编号
	private int clientID;
	// 账户编号
//	private String accountCode;
	// 订阅行情时的代码编号   
	private int tickerID = 0;
	// tick快照字典，key为tickerId，value为Tick对象
	private Map<Integer,Tick> tickMap = new HashMap<>();
	// tick对应的产品类型字典，key为tickerId，value为产品类型
	private Map<Integer,String> tickProductMap = new HashMap<>();
    // 订单编号
	private int orderID = 0;
	// 报单字典，key为orderId，value为Order对象
	private Map<String,xyz.redtorch.core.entity.Order> orderMap = new HashMap<>();
	// 账户字典
	private Map<String,Account>  accountMap = new HashMap<>();
	// 合约字典
	private Map<String,xyz.redtorch.core.entity.Contract>  contractMap = new HashMap<>();
	// 用来保存订阅请求的字典 
	private Map<String,SubscribeReq>  subscribeReqMap = new HashMap<>();
	// 原始ID映射
	private HashMap<String,String> originalOrderIDMap = new HashMap<>();
	
	// utils
//	private long ts;
//	private PrintStream m_output;
//	private int m_outputCounter = 0;
//	private int m_messageCounter;	

	protected EClientSocket client() { return eClientSocket; }
	
	private IbGateway ibGateway;
	protected IbWrapper(IbGateway ibGateway) {
		// 连接地址
		this.host = ibGateway.getGatewaySetting().getIbSetting().getHost();
		// 连接端口
		this.port = ibGateway.getGatewaySetting().getIbSetting().getPort();
		// 用户编号
		this.clientID = ibGateway.getGatewaySetting().getIbSetting().getClientID();
		// 账户编号
//		this.accountCode = ibGateway.getGatewaySetting().getIbSetting().getAccountCode();
		this.ibGateway = ibGateway;
		
		attachDisconnectHook(this);
	}
	
    // 连接状态
	private boolean apiStatus = false;
	
	public boolean isConnected() {
		return apiStatus;
	}

	public void connect() {
		eClientSocket.eConnect(host, port, clientID);
		
        final EReader reader = new EReader(eClientSocket, eJavaSignal);
        
        reader.start();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (eClientSocket.isConnected()) {
                	eJavaSignal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        log.error("IB处理数据发生错误",e);
                    }
                }
            }
        });

	}

	
	public void reqCurrentTime() {
		eClientSocket.reqCurrentTime();
	}
	
	public void subscribe(SubscribeReq subscribeReq) {
		
		if(!isConnected()) {
			subscribeReqMap.put(subscribeReq.getSymbol(), subscribeReq);
			return;
		}
		
		Contract contract = new Contract();
		contract.localSymbol(subscribeReq.getSymbol());
	    contract.exchange(IbConstant.exchangeMap.getOrDefault(subscribeReq.getExchange(), ""));
	    contract.secType(IbConstant.productClassMap.getOrDefault(subscribeReq.getProductClass(), ""));
	    contract.currency(IbConstant.currencyMap.getOrDefault(subscribeReq.getCurrency(), ""));
	    contract.includeExpired(false);
//	    contract.expiry(subscribeReq.getExpiry());
	    contract.strike(subscribeReq.getStrikePrice());
	    contract.right(IbConstant.optionTypeMap.getOrDefault(subscribeReq.getOptionType(), ""));
	    

	    // 获取合约详细信息
        tickerID+=1;
        eClientSocket.reqContractDetails(tickerID, contract);
        // 创建合约对象并保存到字典中
        xyz.redtorch.core.entity.Contract rtContract = new xyz.redtorch.core.entity.Contract();
        rtContract.setGatewayID(ibGateway.getGatewayID());
        rtContract.setSymbol(subscribeReq.getSymbol());
        rtContract.setExchange(subscribeReq.getExchange());
        rtContract.setRtSymbol(rtContract.getSymbol()+"."+rtContract.getExchange());
        rtContract.setRtContractID(rtContract.getSymbol()+"."+rtContract.getExchange()+"."+ibGateway.getGatewayID());
        rtContract.setProductClass(subscribeReq.getProductClass());
        contractMap.put(rtContract.getRtSymbol(), rtContract);
        
        // 订阅行情
        tickerID+=1;
        //eClientSocket.reqMktData(tickerID, contract, "", false, new ArrayList<TagValue>());
        eClientSocket.reqMktData(tickerID, contract, "", false, false, new ArrayList<TagValue>());
        
        // 创建Tick对象并保存到Map中
        Tick tick = new Tick();
        tick.setGatewayID(ibGateway.getGatewayID());
        tick.setSymbol(subscribeReq.getSymbol());
        tick.setExchange(subscribeReq.getExchange());
        tick.setRtSymbol(tick.getSymbol()+"."+tick.getExchange());
        tick.setRtTickID(tick.getSymbol()+"."+tick.getExchange()+"."+ibGateway.getGatewayID());
        tickMap.put(tickerID, tick);
        
        tickProductMap.put(tickerID, subscribeReq.getProductClass());
	}
	
	public void unSubscribe(String rtSymbol) {
		for(Entry<Integer,Tick> entry: tickMap.entrySet()) {
			Integer tickerID = entry.getKey();
			Tick tick = entry.getValue();
			if(tick.getRtSymbol().equals(rtSymbol)) {
				eClientSocket.cancelMktData(tickerID);
			}
		}
		
	}
	
	public String sendOrder(OrderReq orderReq) {
        // 增加报单号1，最后再次进行查询
        // 这里双重设计的目的是为了防止某些情况下，连续发单时，nextOrderId的回调推送速度慢导致没有更新
        orderID += 1;
        
        // 创建合约对象
        Contract contract = new Contract();
        contract.localSymbol(orderReq.getSymbol());
        contract.exchange(IbConstant.exchangeMap.getOrDefault(orderReq.getExchange(), ""));
        contract.secType(IbConstant.productClassMap.getOrDefault(orderReq.getProductClass(), ""));
        contract.currency(IbConstant.currencyMap.getOrDefault(orderReq.getCurrency(), ""));
//        contract.expiry(orderReq.getExpiry());
        contract.strike(orderReq.getStrikePrice());
        contract.right(IbConstant.optionTypeMap.getOrDefault(orderReq.getOptionType(), ""));
        contract.lastTradeDateOrContractMonth(orderReq.getLastTradeDateOrContractMonth());
        contract.multiplier(orderReq.getMultiplier());
        
        // 创建委托对象
        Order order = new Order();
        order.orderId(orderID);
        order.clientId(clientID);
        order.action(IbConstant.directionMap.getOrDefault(orderReq.getDirection(), ""));
        order.lmtPrice(orderReq.getPrice());
        order.totalQuantity(orderReq.getVolume());
        order.orderType(IbConstant.priceTypeMap.getOrDefault(orderReq.getPriceType(), ""));
        order.account(orderReq.getAccountID());
        
        // 发送委托
        eClientSocket.placeOrder(orderID, contract, order);
        
        
        // 查询下一个有效编号
        eClientSocket.reqIds(1);
        
        // 返回委托编号
        String rtOrderID =  ibGateway.getGatewayID()+"."+orderID;
        
		if(StringUtils.isNotBlank(orderReq.getOriginalOrderID())) {
			originalOrderIDMap.put(rtOrderID,orderReq.getOriginalOrderID());
		}
        
        return rtOrderID;

	}
    
	public void cancelOrder(CancelOrderReq cancelOrderReq) {
        eClientSocket.cancelOrder(Integer.valueOf(cancelOrderReq.getOrderID()));
	}
	
	public void disconnect() {
		eClientSocket.eDisconnect();
		executor.shutdown();	
	}

	/* ***************************************************************
	 * AnyWrapper
	 *****************************************************************/

	public void error(Exception e) {
		log.error(ibGateway.getGatewayLogInfo() + "未知错误",e);
	}

	public void error(String str) {
		log.error(str);
	}

	public void error(int id, int errorCode, String errorMsg) {
		// important 如果发现errorMsg乱码，请将IB API的消息传送模式改为英文
		log.warn(ibGateway.getGatewayLogInfo() + "Error id=" + id + " code=" + errorCode + " msg=" + errorMsg);
	}

	public void connectionClosed() {
		this.apiStatus = false;
		log.error("连接已断开,GatewayID:"+ibGateway.getGatewayID());
	}	

	/* ***************************************************************
	 * EWrapper
	 *****************************************************************/

	@Override
	public void tickSize(int tickerId, int field, int size) {
		logIn("tickSize");
		if(IbConstant.tickFieldMap.containsKey(field)) {
			// 对于股票、期货等行情，有新价格推送时仅更新tick缓存
            // 只有当发生成交后，tickString更新最新成交价时才推送新的tick
            // 即bid/ask的价格变动并不会触发新的tick推送
			Tick cachedTick = tickMap.get(tickerId);
			
			if(field == 0) {
				cachedTick.setBidVolume1(size);
			}else if(field == 3) {
				cachedTick.setAskVolume1(size);
			}else if(field == 5) {
				cachedTick.setLastVolume(size);
			}else if(field == 8) {
				cachedTick.setVolume(size);
			}else if(field == 22) {
				cachedTick.setOpenInterest(size);
			}else {
				log.error("未能识别的数量字段,field:"+field);
			}
		}
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		logIn("tickGeneric");
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		logIn("tickString");
        // 如果是最新成交时间戳更新
        if(tickType == 45) {
			Tick cachedTick = tickMap.get(tickerId);

			DateTime dateTime = new DateTime(Long.valueOf(value));
			cachedTick.setDateTime(dateTime);
			cachedTick.setActionTime(dateTime.toString(RtConstant.T_FORMAT_WITH_MS_INT_FORMATTER));
			cachedTick.setActionDay(dateTime.toString(RtConstant.D_FORMAT_INT_FORMATTER));
			cachedTick.setTradingDay(cachedTick.getActionDay());

            Tick tick = SerializationUtils.clone(cachedTick);
			tick.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
			ibGateway.emitTick(tick);     
        }
            
	}	

	@Override
	public void tickSnapshotEnd(int tickerId) {
		logIn("tickSnapshotEnd");
	}	

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol,
			double delta, double optPrice, double pvDividend,
			double gamma, double vega, double theta, double undPrice) {
		logIn("tickOptionComputation");
	}	

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {
		logIn("tickEFP");
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		logIn("openOrder");
		
		String orderID = orderId+"";
         
		xyz.redtorch.core.entity.Order rtOrder;
		if(orderMap.containsKey(orderID)) {
			rtOrder = orderMap.get(orderID);
		}else {
			rtOrder = new xyz.redtorch.core.entity.Order();
			rtOrder.setOrderID(orderID);
			rtOrder.setRtOrderID(ibGateway.getGatewayID()+"."+orderID);
			rtOrder.setGatewayID(ibGateway.getGatewayID());
			rtOrder.setSymbol(contract.localSymbol());
			rtOrder.setExchange(IbConstant.exchangeMapReverse.getOrDefault(contract.exchange(), ""));
			rtOrder.setRtSymbol(rtOrder.getSymbol()+"."+rtOrder.getExchange());
			rtOrder.setAccountID(order.account());
			
			orderMap.put(orderID, rtOrder);
		}
		rtOrder.setDirection(IbConstant.directionMapReverse.getOrDefault(order.getAction(), ""));
		rtOrder.setPrice(order.lmtPrice());
		rtOrder.setTotalVolume((int)order.totalQuantity());
		
		String originalOrderID = originalOrderIDMap.get(rtOrder.getRtOrderID());
		rtOrder.setOriginalOrderID(originalOrderID);

		rtOrder.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
        ibGateway.emitOrder(SerializationUtils.clone(rtOrder));
	}

	@Override
	public void openOrderEnd() {
		logIn("openOrderEnd");
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		logIn("updateAccountValue");
	    // 仅逐个字段更新数据，这里对于没有currency的推送忽略
		if(StringUtils.isNotBlank(currency)) {
			String accountKey = accountName + "." + currency;
			Account account;
			if(accountMap.containsKey(accountKey)) {
				account = accountMap.get(accountKey);
			}else {
				account = new Account();
				account.setAccountID(accountName);
				account.setCurrency(currency);
				account.setRtAccountID(accountKey+"."+ibGateway.getGatewayID());
				account.setGatewayID(ibGateway.getGatewayID());
				accountMap.put(accountKey, account);
			}
			
			if(IbConstant.accountKeyMap.containsKey(key)) {
				
				if("NetLiquidationByCurrency".equals(key)) {
					account.setBalance(Double.valueOf(value));
				}else if("NetLiquidation".equals(key)) {
					account.setBalance(Double.valueOf(value));
				}else if("UnrealizedPnL".equals(key)) {
					account.setPositionProfit(Double.valueOf(value));
				}else if("AvailableFunds".equals(key)) {
					account.setAvailable(Double.valueOf(value));
				}else if("MaintMarginReq".equals(key)) {
					account.setMargin(Double.valueOf(value));
				}else{
					log.error("未能识别的Account字段:"+key);
				}
				
			}
		}
	}

	@Override
	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
			double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		logIn("updatePortfolio");
		xyz.redtorch.core.entity.Position rtPostion = new Position();
		
		rtPostion.setSymbol(contract.localSymbol());
		rtPostion.setExchange(IbConstant.exchangeMapReverse.getOrDefault(contract.primaryExch(), ""));
		rtPostion.setRtSymbol(rtPostion.getSymbol()+"."+rtPostion.getExchange());
		rtPostion.setDirection(RtConstant.DIRECTION_NET);
		rtPostion.setPosition((int)position);
		rtPostion.setPrice(averageCost);
		rtPostion.setGatewayID(ibGateway.getGatewayID());
		rtPostion.setRtPositionID(rtPostion.getGatewayID() + rtPostion.getRtSymbol() + rtPostion.getDirection());
		rtPostion.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
		
		rtPostion.setAccountID(accountName);
		rtPostion.setRtAccountID(accountName+"."+contract.currency()+"."+ibGateway.getGatewayID());
		
        ibGateway.emitPosition(rtPostion);
		
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		logIn("updateAccountTime");
		for(Account account:accountMap.values()) {
			account.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
			ibGateway.emitAccount(SerializationUtils.clone(account));
		}
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		logIn("accountDownloadEnd");
	}

	@Override
	public void nextValidId(int orderId) {
		logIn("nextValidId");
		orderID = orderId;
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		logIn("contractDetails");
        String symbol = contractDetails.contract().localSymbol();
        String exchange = IbConstant.exchangeMapReverse.getOrDefault(contractDetails.contract().exchange(), RtConstant.EXCHANGE_UNKNOWN);
        String rtSymbol = symbol+"."+exchange;
        
        xyz.redtorch.core.entity.Contract rtContract = contractMap.get(rtSymbol);
        
        if(rtContract==null) {
        	return;
        }
        rtContract.setName(contractDetails.longName());
        rtContract.setPriceTick(contractDetails.minTick());

        rtContract.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
        ibGateway.emitContract(rtContract);
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		logIn("contractDetailsEnd");
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		logIn("bondContractDetails");
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		logIn("execDetails");
        Trade trade = new Trade();
        trade.setGatewayID(ibGateway.getGatewayID());
        trade.setTradeID(execution.execId());
        trade.setRtTradeID(trade.getGatewayID()+"."+trade.getTradeID());
    
        trade.setSymbol(contract.localSymbol());
        trade.setExchange(IbConstant.exchangeMapReverse.getOrDefault(contract.exchange(), ""));
        trade.setRtSymbol(trade.getSymbol()+"."+trade.getExchange());
    
        trade.setOrderID(execution.orderId()+"");
        trade.setRtOrderID(trade.getGatewayID()+"."+trade.getOrderID());
        trade.setDirection(IbConstant.directionMapReverse.getOrDefault(execution.side(), ""));
        trade.setPrice(execution.price());
        trade.setVolume((int)execution.shares());
        trade.setTradeTime(execution.time());
        trade.setAccountID(orderMap.get(execution.orderId()+"").getAccountID());
    
		String originalOrderID = originalOrderIDMap.get(trade.getRtOrderID());
		trade.setOriginalOrderID(originalOrderID);

		trade.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
		
		
        ibGateway.emitTrade(trade);
		
	}

	@Override
	public void execDetailsEnd(int reqId) {
		logIn("execDetailsEnd");
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		logIn("updateMktDepth");
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation,
			int side, double price, int size) {
		logIn("updateMktDepthL2");
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		logIn("updateNewsBulletin");
	}

	@Override
	public void managedAccounts(String accountsList) {
		logIn("managedAccounts");
	       String[] accounts = accountsList.split(",");
	       // 请求账户数据主推更新
	       for(String account :accounts) {
	    	   eClientSocket.reqAccountUpdates(true, account); 
	       }
	    	            
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		logIn("receiveFA");
	}

	@Override
	public void scannerParameters(String xml) {
		logIn("scannerParameters");
	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance,
			String benchmark, String projection, String legsStr) {
		logIn("scannerData");
	}

	@Override
	public void scannerDataEnd(int reqId) {
		logIn("scannerDataEnd");
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, 
			long volume, double wap, int count) {
		logIn("realtimeBar");
	}

	@Override
	public void currentTime(long millis) {
		logIn("currentTime");
		this.apiStatus = true;
		
		Iterator<Map.Entry<String,SubscribeReq>> it = subscribeReqMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,SubscribeReq> entry = it.next();
            SubscribeReq subscribeReq = entry.getValue();
            subscribe(subscribeReq);
            it.remove();//使用迭代器的remove()方法删除元素
        }
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		logIn("fundamentalData");    	
	}

	@Override
	public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
		logIn("deltaNeutralValidation");    	
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		logIn("marketDataType");
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		logIn("commissionReport");
	}

	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		logIn("position");
	}

	@Override
	public void positionEnd() {
		logIn("positionEnd");
	}

	@Override
	public void accountSummary( int reqId, String account, String tag, String value, String currency) {
		logIn("accountSummary");
	}

	@Override
	public void accountSummaryEnd( int reqId) {
		logIn("accountSummaryEnd");
	}

	@Override
	public void verifyMessageAPI( String apiData) {
		logIn("verifyMessageAPI");
	}

	@Override
	public void verifyCompleted( boolean isSuccessful, String errorText){
		logIn("verifyCompleted");
	}

	@Override
	public void verifyAndAuthMessageAPI( String apiData, String xyzChallenge) {
		logIn("verifyAndAuthMessageAPI");
	}

	@Override
	public void verifyAndAuthCompleted( boolean isSuccessful, String errorText){
		logIn("verifyAndAuthCompleted");
	}

	@Override
	public void displayGroupList( int reqId, String groups){
		logIn("displayGroupList");
	}

	@Override
	public void displayGroupUpdated( int reqId, String contractInfo){
		logIn("displayGroupUpdated");
	}

	@Override
	public void positionMulti( int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
		logIn("positionMulti");
	}

	@Override
	public void positionMultiEnd( int reqId) {
		logIn("positionMultiEnd");
	}

	@Override
	public void accountUpdateMulti( int reqId, String account, String modelCode, String key, String value, String currency) {
		logIn("accountUpdateMulti");
	}

	@Override
	public void accountUpdateMultiEnd( int reqId) {
		logIn("accountUpdateMultiEnd");
	}

	/* ***************************************************************
	 * Helpers
	 *****************************************************************/
	protected void logIn(String method) {
		log.debug(method);
	}

	private static void attachDisconnectHook(final IbWrapper ut) {
		Runtime.getRuntime().addShutdownHook(new Thread() {				
			public void run() {
				ut.disconnect();
			}
		});			    	
	}
	
	@Override
	public void connectAck() {
		eClientSocket.startAPI();
	}

	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass,
			String multiplier, Set<String> expirations, Set<Double> strikes) {
		logIn("securityDefinitionOptionalParameter");
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
		logIn("securityDefinitionOptionalParameterEnd");
	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		logIn("softDollarTiers");	
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttr attrib) {
		logIn("tickPrice");
		if(IbConstant.tickFieldMap.containsKey(field)) {
			// 对于股票、期货等行情，有新价格推送时仅更新tick缓存
            // 只有当发生成交后，tickString更新最新成交价时才推送新的tick
            // 即bid/ask的价格变动并不会触发新的tick推送
			Tick cachedTick = tickMap.get(tickerId);
			
		    if(field == 1) {
				cachedTick.setBidPrice1(price);
			}else if(field == 2) {
				cachedTick.setAskPrice1(price);
			}else if(field == 4) {
				cachedTick.setLastPrice(price);
			}else if(field == 6) {
				cachedTick.setHighPrice(price);
			}else if(field == 7) {
				cachedTick.setLowPrice(price);
			}else if(field == 9) {
				cachedTick.setPreClosePrice(price);
			}else if(field == 14) {
				cachedTick.setOpenPrice(price);
			}else {
				log.error("未能识别的价格字段,field:"+field);
			}
			
			// IB的外汇行情没有成交价和时间，通过本地计算生成，同时立即推送
			String  product = tickProductMap.get(tickerId);
			if(product.equals(RtConstant.PRODUCT_FOREX)||product.equals(RtConstant.PRODUCT_SPOT)) {
				cachedTick.setLastPrice((cachedTick.getAskPrice1()+cachedTick.getBidPrice1())/2);
				DateTime dateTimeNow = new DateTime();
				cachedTick.setDateTime(dateTimeNow);
				cachedTick.setActionTime(dateTimeNow.toString(RtConstant.T_FORMAT_WITH_MS_INT_FORMATTER));
				cachedTick.setActionDay(dateTimeNow.toString(RtConstant.D_FORMAT_INT_FORMATTER));
				cachedTick.setTradingDay(cachedTick.getActionDay());
				
				Tick tick = SerializationUtils.clone(cachedTick);
				tick.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
				ibGateway.emitTick(tick);
			}
			
		}else {
			log.warn("发现未知field:"+field);
		}
	}

	@Override
	public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice,
			int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		logIn("orderStatus");
		
		String orderID = orderId+"";
         
		xyz.redtorch.core.entity.Order rtOrder;
		if(orderMap.containsKey(orderID)) {
			rtOrder = orderMap.get(orderID);
		}else {
			rtOrder = new xyz.redtorch.core.entity.Order();
			rtOrder.setOrderID(orderID);
			rtOrder.setRtOrderID(ibGateway.getGatewayID()+"."+orderID);
			rtOrder.setGatewayID(ibGateway.getGatewayID());
			
			orderMap.put(orderID, rtOrder);
		}
        
		rtOrder.setStatus(IbConstant.statusMapReverse.getOrDefault(status, RtConstant.STATUS_UNKNOWN));
		rtOrder.setTradedVolume((int)filled);
		
		String originalOrderID = originalOrderIDMap.get(rtOrder.getRtOrderID());
		rtOrder.setOriginalOrderID(originalOrderID);

		rtOrder.setGatewayDisplayName(ibGateway.getGatewayDisplayName());
        ibGateway.emitOrder(SerializationUtils.clone(rtOrder));
	}

	@Override
	public void historicalData(int reqId, Bar bar) {
		logIn("historicalData");
	}

	@Override
	public void familyCodes(FamilyCode[] familyCodes) {
		logIn("familyCodes");			
	}

	@Override
	public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
		logIn("symbolSamples");			
	}

	@Override
	public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
		logIn("historicalDataEnd");	
	}

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
		logIn("mktDepthExchanges");	
	}

	@Override
	public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline,
			String extraData) {
		logIn("tickNews");	
	}

	@Override
	public void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {
		logIn("smartComponents");	
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		logIn("tickReqParams");		
	}

	@Override
	public void newsProviders(NewsProvider[] newsProviders) {
		logIn("newsProviders");		
	}

	@Override
	public void newsArticle(int requestId, int articleType, String articleText) {
		logIn("newsArticle");		
	}

	@Override
	public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
		logIn("historicalNews");		
	}

	@Override
	public void historicalNewsEnd(int requestId, boolean hasMore) {
		logIn("historicalNewsEnd");			
	}

	@Override
	public void headTimestamp(int reqId, String headTimestamp) {
		logIn("headTimestamp");		
	}

	@Override
	public void histogramData(int reqId, List<HistogramEntry> items) {
		logIn("histogramData");
	}

	@Override
	public void historicalDataUpdate(int reqId, Bar bar) {
		logIn("historicalDataUpdate");		
	}

	@Override
	public void rerouteMktDataReq(int reqId, int conId, String exchange) {
		logIn("rerouteMktDataReq");
	}

	@Override
	public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
		logIn("rerouteMktDepthReq");
	}

	@Override
	public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
		logIn("marketRule");			
	}

	@Override
	public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
		logIn("pnl");			
	}

	@Override
	public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
		logIn("pnlSingle");		
	}

	@Override
	public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
		logIn("historicalTicks");
	}

	@Override
	public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
		logIn("historicalTicksBidAsk");
	}

	@Override
	public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
		logIn("historicalTicksLast");
	}

	@Override
	public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttr attribs,
			String exchange, String specialConditions) {
		logIn("tickByTickAllLast");
	}

	@Override
	public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
			TickAttr attribs) {
		logIn("tickByTickBidAsk");
	}

	@Override
	public void tickByTickMidPoint(int reqId, long time, double midPoint) {
		logIn("tickByTickMidPoint");
	}

}
