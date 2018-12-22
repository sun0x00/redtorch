package xyz.redtorch.core.zeus.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.utils.BarGenerator;
import xyz.redtorch.core.utils.BarGenerator.CommonBarCallBack;
import xyz.redtorch.core.utils.BarGenerator.XMinBarGenerator;
import xyz.redtorch.core.zeus.ZeusConstant;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.entity.ContractPositionDetail;
import xyz.redtorch.core.zeus.entity.PositionDetail;
import xyz.redtorch.utils.CommonUtil;

/**
 * 策略基本实现抽象类
 * 
 * @author sun0x00@gmail.com
 *
 */
public abstract class StrategyAbstract implements Strategy {
	private static final Logger log = LoggerFactory.getLogger(StrategyAbstract.class);

	protected String logStr; // 日志拼接字符串
	protected boolean initStatus = false; // 初始化状态
	protected boolean tradingStatus = false; // 交易开关

	protected ZeusEngineService zeusEngineService; // 策略引擎

	protected StrategySetting strategySetting; // 策略配置

	protected Map<String, ContractPositionDetail> contractPositionMap = new HashMap<>(); // 合约仓位维护

	protected Map<String, Order> workingOrderMap = new HashMap<>(); // 委托单

	protected HashSet<String> rtTradeIDSet = new HashSet<>(); // 用于过滤可能重复的Trade推送

	/**
	 * 必须使用有参构造方法
	 * 
	 * @param zeusEngineService
	 * @param strategySetting
	 */
	public StrategyAbstract(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
		
		strategySetting.fixSetting();
		this.strategySetting = strategySetting;

		this.logStr = "策略-[" + strategySetting.getStrategyName() + "] ID-[" + strategySetting.getStrategyID() + "] >>> ";

		this.zeusEngineService = zeusEngineService;

	}

	/**
	 * 策略ID
	 * 
	 * @return
	 */
	@Override
	public String getID() {
		return strategySetting.getStrategyID();
	}

	/**
	 * 获取策略名称
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return strategySetting.getStrategyName();
	}

	/**
	 * 快捷获取日志拼接字符串
	 * 
	 * @return
	 */
	@Override
	public String getLogStr() {
		return logStr;
	}

	@Override
	public boolean isInitStatus() {
		return initStatus;
	}

	@Override
	public boolean isTrading() {
		return tradingStatus;
	}

	@Override
	public int getEngineType() {
		return this.zeusEngineService.getEngineType();
	}

	@Override
	public StrategySetting getStrategySetting() {
		return strategySetting;
	}

	@Override
	public void startTrading() {
		if (!initStatus) {
			log.warn(logStr + "策略尚未初始化,无法开始交易!");
			return;
		}

		if (tradingStatus) {
			log.warn(logStr + "策略正在运行,请勿重复操作!");
			return;
		}
		this.tradingStatus = true;
		try {
			onStartTrading();
			log.info(logStr + "开始交易!");
		} catch (Exception e) {
			stopTrading(false);
			log.error(logStr + "调用onStartTrading发生异常,停止策略!!!", e);
		}
	}

	/**
	 * 停止交易
	 */
	@Override
	public void stopTrading(boolean finishedCorrectly) {
		if (!tradingStatus) {
			log.warn(logStr + "策略已经停止,请勿重复操作!");
			return;
		}

		// 保存持仓
		savePosition();
		// 保存策略配置
		saveStrategySetting();
		this.tradingStatus = false;
		try {
			onStopTrading(finishedCorrectly);
		} catch (Exception e) {
			log.error(logStr + "策略停止后调用onStopTrading发生异常!", e);
		}
	}

	/**
	 * 初始化策略
	 */
	@Override
	public void init() {
		if (initStatus == true) {
			log.warn(logStr + "策略已经初始化,请勿重复操作!");
			return;
		}
		initStatus = true;
		try {
			onInit();
			log.info(logStr + "初始化!");
		} catch (Exception e) {
			initStatus = false;
			log.error(logStr + "调用onInit发生异常!", e);
		}
	}

	/**
	 * 销毁通知，一般用于重新加载策略
	 */
	@Override
	public void destroy() {
	}

	@Override
	public void saveStrategySetting() {
		zeusEngineService.asyncSaveStrategySetting(strategySetting);
	}

	@Override
	public void setVarValue(String key, String value) {
		strategySetting.getVarMap().put(key, value);
		saveStrategySetting();
	}

	@Override
	public String sendOrder(String rtSymbol, String orderType, String priceType, double price, int volume,
			String rtAccountID) {


		OrderReq orderReq = new OrderReq();
		
		orderReq.setRtAccountID(rtAccountID);
		orderReq.setRtSymbol(rtSymbol);
		orderReq.setPrice(price);
		orderReq.setVolume(volume);
		// 使用策略ID作为OID
		orderReq.setOperatorID(getID());

		orderReq.setPriceType(priceType);
		
		if (zeusEngineService.getEngineType() == ZeusConstant.ENGINE_TYPE_BACKTESTING) {
			String symbol;
			String exchange;
			double priceTick = 0;
			String[] rtSymbolArray = rtSymbol.split("\\.");
			symbol = rtSymbolArray[0];
			exchange = rtSymbolArray[1];
			priceTick = strategySetting.getContractSetting(rtSymbol).getBacktestingPriceTick();
			
			orderReq.setSymbol(symbol);
			orderReq.setExchange(exchange);
			orderReq.setPrice(CommonUtil.rountToPriceTick(priceTick, price));
		}


		if (ZeusConstant.ORDER_BUY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_OPEN);

		} else if (ZeusConstant.ORDER_SELL.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_CLOSE);

		} else if (ZeusConstant.ORDER_SHORT.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_OPEN);

		} else if (ZeusConstant.ORDER_COVER.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_CLOSE);

		} else if (ZeusConstant.ORDER_SELLTODAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_CLOSETODAY);

		} else if (ZeusConstant.ORDER_SELLYESTERDAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_SHORT);
			orderReq.setOffset(RtConstant.OFFSET_CLOSEYESTERDAY);

		} else if (ZeusConstant.ORDER_COVERTODAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_CLOSETODAY);

		} else if (ZeusConstant.ORDER_COVERYESTERDAY.equals(orderType)) {

			orderReq.setDirection(RtConstant.DIRECTION_LONG);
			orderReq.setOffset(RtConstant.OFFSET_CLOSEYESTERDAY);
		}

		// 这里可以考虑换为更快的唯一ID生成方式 TODO
		String originalOrderID = orderReq.getOriginalOrderID();

		zeusEngineService.sendOrder(orderReq);

		if (contractPositionMap.containsKey(rtSymbol)) {
			contractPositionMap.get(rtSymbol).updateOrderReq(orderReq);
		}

		return originalOrderID;
	}

	@Override
	public void cancelOrder(String originalOrderID) {
		if (StringUtils.isEmpty(originalOrderID)) {
			log.error(logStr + "无法撤单,originalOrderID为空");
			return;
		}
		if (workingOrderMap.containsKey(originalOrderID)) {
			zeusEngineService.cancelOrder(originalOrderID,getID());
			workingOrderMap.remove(originalOrderID);
		}
	}

	@Override
	public void cancelAll() {

		for (Entry<String, Order> entry : workingOrderMap.entrySet()) {
			String originalOrderID = entry.getKey();
			Order order = entry.getValue();
			if (!RtConstant.STATUS_FINISHED.contains(order.getStatus())) {
				cancelOrder(originalOrderID);
			}
		}
	}

	@Override
	public String buy(String rtSymbol, int volume, double price, String rtAccountID) {

		return sendOrder(rtSymbol, ZeusConstant.ORDER_BUY, RtConstant.PRICETYPE_LIMITPRICE, price, volume, rtAccountID);

	}

	@Override
	public String sell(String rtSymbol, int volume, double price, String rtAccountID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SELL, RtConstant.PRICETYPE_LIMITPRICE, price, volume, rtAccountID);
	}

	@Override
	public String sellTd(String rtSymbol, int volume, double price, String rtAccountID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SELLTODAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				rtAccountID);
	}

	@Override
	public String sellYd(String rtSymbol, int volume, double price, String rtAccountID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SELLYESTERDAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				rtAccountID);
	}

	@Override
	public String sellShort(String rtSymbol, int volume, double price, String rtAccountID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_SHORT, RtConstant.PRICETYPE_LIMITPRICE, price, volume, rtAccountID);
	}

	@Override
	public String buyToCover(String rtSymbol, int volume, double price, String rtAccountID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_COVER, RtConstant.PRICETYPE_LIMITPRICE, price, volume, rtAccountID);

	}

	@Override
	public String buyToCoverTd(String rtSymbol, int volume, double price, String rtAccountID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_COVERTODAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				rtAccountID);

	}

	@Override
	public String buyToCoverYd(String rtSymbol, int volume, double price, String rtAccountID) {
		return sendOrder(rtSymbol, ZeusConstant.ORDER_COVERYESTERDAY, RtConstant.PRICETYPE_LIMITPRICE, price, volume,
				rtAccountID);

	}

	// X分钟Bar生成器,由构造方法xMin参数决定是否实例化生效
	private Map<String, XMinBarGenerator> xMinBarGeneratorMap = new HashMap<>();
	private Map<String, BarGenerator> barGeneratorMap = new HashMap<>();

	/**
	 * 在一分钟Bar产生时调用 <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个网关订阅同一个合约导致的同一个合约同一个时间的bar重复调用
	 * 
	 * @param bar
	 * @throws Exception
	 */
	public abstract void onBar(Bar bar) throws Exception;

	/**
	 * 在X分钟Bar产生时调用 <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个网关订阅同一个合约导致的同一个合约同一个时间的bar重复调用
	 * 
	 * @param bar
	 * @throws Exception
	 */
	public abstract void onXMinBar(Bar bar) throws Exception;

	/**
	 * 保存持仓
	 */
	public void savePosition() {
		List<PositionDetail> positionDetailList = new ArrayList<>();
		for (ContractPositionDetail contractPositionDetail : contractPositionMap.values()) {
			positionDetailList.addAll(new ArrayList<>(contractPositionDetail.getPositionDetailMap().values()));
			zeusEngineService.asyncSavePositionDetail(positionDetailList);
		}
	}

	/**
	 * 重置策略,一般被回测引擎使用,用于实现连续回测
	 * 
	 * @param strategySetting
	 */
	@Override
	public void resetStrategy(StrategySetting strategySetting) {
		// 清空委托ID过滤集合,避免影响下一个交易日回测
		this.rtTradeIDSet.clear();
		// 清空持仓信息
		this.contractPositionMap.clear();
		// 强制校正新的配置数据
		strategySetting.fixSetting();
		this.strategySetting = strategySetting;
	}

	/**
	 * 获取持仓Map
	 * 
	 * @return
	 */
	@Override
	public Map<String, ContractPositionDetail> getContractPositionMap() {
		return contractPositionMap;
	}

	@Override
	public void processTick(Tick tick) {
		try {
			onTick(tick);
			// 基于合约的onBar和onMinBar
			String bgKey = tick.getRtSymbol();
			// 基于合约+网关的onBar和onMinBar,使用这个key会多次触发同一策略下同一品种的相同时间bar的事件
			// String bgKey = tick.getRtSymbol()+tick.getGatewayID();
			BarGenerator barGenerator;
			if (barGeneratorMap.containsKey(bgKey)) {
				barGenerator = barGeneratorMap.get(bgKey);
			} else {
				barGenerator = new BarGenerator(new CommonBarCallBack() {
					@Override
					public void call(Bar bar) {
						processBar(bar);
					}
				});
				barGeneratorMap.put(bgKey, barGenerator);
			}

			// 更新1分钟bar生成器
			barGenerator.updateTick(tick);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onTick发生异常,停止策略!!!", logStr, e);
		}
	}

	@Override
	public void processTrade(Trade trade) {
		try {
			// 过滤重复
			if (!rtTradeIDSet.contains(trade.getRtTradeID())) {
				if (contractPositionMap.containsKey(trade.getRtSymbol())) {
					ContractPositionDetail contractPositionDetail = contractPositionMap.get(trade.getRtSymbol());
					contractPositionDetail.updateTrade(trade);
					savePosition();
				}else {
					log.warn("{} 合约[{}]的预配置不存在,不会更新数据库持仓！", logStr, trade.getRtSymbol());
				}
				rtTradeIDSet.add(trade.getRtTradeID());

				onTrade(trade);
			}

		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onTrade发生异常,停止策略!!!", logStr, e);
		}
	}

	@Override
	public void processOrder(Order order) {
		try {
			workingOrderMap.put(order.getOriginalOrderID(), order);
			if (RtConstant.STATUS_FINISHED.contains(order.getStatus())) {
				workingOrderMap.remove(order.getOriginalOrderID());
			}
			if (contractPositionMap.containsKey(order.getRtSymbol())) {
				ContractPositionDetail contractPositionDetail = contractPositionMap.get(order.getRtSymbol());
				contractPositionDetail.updateOrder(order);
			}else {
				log.warn("{} 合约[{}]的预配置不存在,不会更新数据库持仓！", logStr, order.getRtSymbol());
			}
			onOrder(order);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onOrder发生异常,停止策略!!!", logStr, e);
		}
	}

	/**
	 * 处理Bar
	 * 
	 * @param bar
	 */
	@Override
	public void processBar(Bar bar) {

		String bgKey = bar.getRtSymbol();
		// 调用onBar方法,此方法会在onTick->bg.updateTick执行之后再执行
		try {
			onBar(bar);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onBar发生异常,停止策略!!!", logStr, e);
		}
		// 判断是否需要调用xMinBarGenerate,设置xMin大于1分钟xMinBarGenerate会生效
		if (strategySetting.getxMin() > 1) {
			XMinBarGenerator xMinBarGenerator;
			if (xMinBarGeneratorMap.containsKey(bgKey)) {
				xMinBarGenerator = xMinBarGeneratorMap.get(bgKey);
			} else {
				xMinBarGenerator = new XMinBarGenerator(strategySetting.getxMin(), new CommonBarCallBack() {
					@Override
					public void call(Bar bar) {
						try {
							// 调用onXMinBar方法
							// 此方法会在onTick->bg.updateTick->onBar->xbg.updateBar执行之后再执行
							onXMinBar(bar);
						} catch (Exception e) {
							stopTrading(true);
							log.error("{} 调用onXMinBar发生异常,停止策略!!!", logStr, e);
						}
					}
				});
				xMinBarGeneratorMap.put(bgKey, xMinBarGenerator);
			}
			xMinBarGenerator.updateBar(bar);
		}
	}


}
