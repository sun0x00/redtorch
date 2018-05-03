package xyz.redtorch.trader.module.zeus.strategy.routine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.entity.ContractPositionDetail;
import xyz.redtorch.trader.module.zeus.entity.PositionDetail;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting.TradeGatewaySetting;
import xyz.redtorch.trader.module.zeus.strategy.StrategyAbstract;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting.ContractSetting;

/**
 * 常规策略模板
 * 
 * 策略支持复杂仓位管理、onBar、onXminBar、回测等功能，区别于HFT策略模板
 * 
 * @author sun0x00@gmail.com
 *
 */
public abstract class RoutineStrategyTemplate extends StrategyAbstract{

	private static final Logger log = LoggerFactory.getLogger(RoutineStrategyTemplate.class);
	
	public RoutineStrategyTemplate(ZeusEngine zeusEngine, StrategySetting strategySetting) {
		super(zeusEngine, strategySetting);
		/**
		 * 初始化基本的持仓数据结构
		 */
		initContractPositionMap();
		
	}
	
	// X分钟Bar生成器,由构造方法xMin参数决定是否实例化生效
	private Map<String, XMinBarGenerator> xMinBarGeneratorMap = new HashMap<>();
	private Map<String, BarGenerator> barGeneratorMap = new HashMap<>();
	
	/**
	 * 在一分钟Bar产生时调用
	 * <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个接口订阅同一个品种导致的同一个品种重复调用
	 * @param bar
	 * @throws Exception
	 */
	public abstract void onBar(Bar bar) throws Exception;
	
	/**
	 * 在X分钟Bar产生时调用
	 * <br/>
	 * 注意,此处默认<b>过滤</b>同一个策略使用多个接口订阅同一个品种导致的同一个品种重复调用
	 * @param bar
	 * @throws Exception
	 */
	public abstract void onXMinBar(Bar bar) throws Exception;

	/**
	 * 停止策略，这里比父类方法多了一个保存持仓动作
	 */
	@Override
	public void stopTrading(boolean isException) {
		if (!trading) {
			log.warn("{} 策略已经停止,请勿重复操作!", logStr);
			return;
		}
		// 保存持仓
		savePosition();
		super.stopTrading(isException);
		
	}
	
	/**
	 * 保存持仓
	 */
	public void savePosition() {
		List<PositionDetail> positionDetailList = new ArrayList<>();
		for (ContractPositionDetail contractPositionDetail : contractPositionMap.values()) {
			positionDetailList.addAll(new ArrayList<>(contractPositionDetail.getPositionDetailMap().values()));
			zeusEngine.asyncSavePositionDetail(positionDetailList);
		}
	}
	
	/**
	 * 重置策略,一般被回测引擎使用,用于实现连续回测
	 * @param strategySetting
	 */
	public void resetStrategy(StrategySetting strategySetting) {
		// 清空活动的停止单
		this.workingStopOrderMap.clear();
		// 清空委托ID过滤集合,避免影响下一个交易日回测
		this.rtTradeIDSet.clear();
		// 清空持仓信息
		this.contractPositionMap.clear();

		// 强制校正新的配置数据
		strategySetting.fixSetting();
		this.strategySetting = strategySetting;

		// 初始化持仓
		initContractPositionMap();

	}
	

	/**
	 * 初始化持仓数据结构
	 */
	private void initContractPositionMap() {

		String tradingDay = strategySetting.getTradingDay();

		for (ContractSetting contractSetting : strategySetting.getContracts()) {
			String rtSymbol = contractSetting.getRtSymbol();
			String exchange = contractSetting.getExchange();
			int contractSize = contractSetting.getSize();

			ContractPositionDetail contractPositionDetail = new ContractPositionDetail(rtSymbol, tradingDay, name, id,
					exchange, contractSize);
			for (TradeGatewaySetting tradeGatewaySetting : contractSetting.getTradeGateways()) {
				String gatewayID = tradeGatewaySetting.getGatewayID();
				PositionDetail positionDetail = new PositionDetail(rtSymbol, tradeGatewaySetting.getGatewayID(),
						tradingDay, name, id, exchange, contractSize);
				contractPositionDetail.getPositionDetailMap().put(gatewayID, positionDetail);
			}
			contractPositionMap.put(rtSymbol, contractPositionDetail);
		}
	}
	
	/**
	 * 获取持仓结构
	 * @return
	 */
	public Map<String, ContractPositionDetail> getContractPositionMap() {
		return contractPositionMap;
	}
	
	/**
	 * 根据预设配置买开多
	 * @param rtSymbol
	 * @param price
	 */
	public void buyByPreset(String rtSymbol, double price) {

		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		ContractSetting contractSetting = strategySetting.getContractSetting(rtSymbol);
		if (contractSetting != null) {
			List<TradeGatewaySetting> tradeGateways = contractSetting.getTradeGateways();
			if (tradeGateways != null && !tradeGateways.isEmpty()) {
				if (contractPositionDetail != null) {
					int longPos = contractPositionDetail.getLongPos();
					int fixedPos = contractSetting.getTradeFixedPos();
					if (longPos == fixedPos) {
						log.warn("合约{}的多头总持仓量已经达到预设值,指令终止!", rtSymbol);
						return;
					} else if (longPos > fixedPos) {
						log.error("合约{}的多头总持仓量{}已经超过预设值{},指令终止!!", rtSymbol);
						stopTrading(true);
						return;
					}
				}

				for (TradeGatewaySetting tradeGteway : tradeGateways) {
					String gatewayID = tradeGteway.getGatewayID();
					int gatewayFixedPos = tradeGteway.getTradeFixedPos();
					int tradePos = gatewayFixedPos;
					if (gatewayFixedPos > 0) {
						PositionDetail positionDetail = contractPositionDetail.getPositionDetailMap().get(gatewayID);

						if (positionDetail != null) {
							int gatewayLongPos = positionDetail.getLongPos();
							int gatewayLongOpenFrozenPos = positionDetail.getLongOpenFrozen();
							if (gatewayLongPos + gatewayLongOpenFrozenPos == gatewayFixedPos) {
								log.warn("合约{}接口{}的多头持仓量加开仓冻结量已经达到预设值,指令忽略!", rtSymbol, gatewayID);
								continue;
							} else if (gatewayLongPos > gatewayFixedPos) {
								log.error("合约{}接口{}的多头持仓量{}加开仓冻结量{}已经超过预设值{},指令忽略!", rtSymbol, gatewayID,
										gatewayLongPos, gatewayLongOpenFrozenPos, gatewayFixedPos);
								stopTrading(true);
								continue;
							} else {
								tradePos = gatewayFixedPos - (gatewayLongPos + gatewayLongOpenFrozenPos);
							}
						}

						buy(rtSymbol, tradePos, price, gatewayID);
					} else {
						log.error("合约{}接口{}配置中的仓位大小不正确", rtSymbol, gatewayID);
						stopTrading(true);
					}
				}
			} else {
				log.error("未找到合约{}配置中的接口配置", rtSymbol);
				stopTrading(true);
			}
		} else {
			log.error("未找到合约{}的配置", rtSymbol);
			stopTrading(true);
		}

	}

	/**
	 * 根据仓位通用卖平多逻辑
	 * @param rtSymbol
	 * @param price
	 * @param offsetType
	 */
	private void commonSellByPosition(String rtSymbol, double price, int offsetType) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		if (contractPositionDetail != null) {
			int longPos = contractPositionDetail.getLongPos();
			if (longPos == 0) {
				log.warn("合约{}的多头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (longPos < 0) {
				log.error("合约{}的多头总持仓量{}小于0!", rtSymbol, longPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {
					continue;
				}

				if (positionDetail.getLongPos() > 0) {
					if (offsetType >= 0) {
						if (positionDetail.getLongOpenFrozen() > 0) {
							log.warn("合约{}接口{}多头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID,
									positionDetail.getLongOpenFrozen());
						}
						if (positionDetail.getLongTd() > 0) {
							sellTd(rtSymbol, positionDetail.getLongTd(), price, gatewayID);
						}
					}
					if (offsetType <= 0) {
						if (positionDetail.getLongYd() > 0) {
							sellYd(rtSymbol, positionDetail.getLongYd(), price, gatewayID);
						}
					}
				} else {
					log.error("合约{}接口{}多头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	/**
	 * 根据仓位卖平多
	 * @param rtSymbol
	 * @param price
	 */
	public void sellByPosition(String rtSymbol, double price) {
		commonSellByPosition(rtSymbol, price, 0);
	}

	/**
	 * 根据仓位卖平今多
	 * @param rtSymbol
	 * @param price
	 */
	public void sellTdByPosition(String rtSymbol, double price) {
		commonSellByPosition(rtSymbol, price, 1);
	}

	/**
	 * 根据仓位卖平昨多
	 * @param rtSymbol
	 * @param price
	 */
	public void sellYdByPosition(String rtSymbol, double price) {
		commonSellByPosition(rtSymbol, price, -1);
	}

	/**
	 * 根据预设配置卖开空
	 * @param rtSymbol
	 * @param price
	 */
	public void sellShortByPreset(String rtSymbol, double price) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		ContractSetting contractSetting = strategySetting.getContractSetting(rtSymbol);
		if (contractSetting != null) {
			List<TradeGatewaySetting> tradeGateways = contractSetting.getTradeGateways();
			if (tradeGateways != null && !tradeGateways.isEmpty()) {

				if (contractPositionDetail != null) {
					int shortPos = contractPositionDetail.getShortPos();
					int fixedPos = contractSetting.getTradeFixedPos();
					if (shortPos == fixedPos) {
						log.warn("合约{}的空头总持仓量已经达到预设值,指令终止!", rtSymbol);
						return;
					} else if (shortPos > fixedPos) {
						log.error("合约{}的空头总持仓量{}已经超过预设值{},指令终止!", rtSymbol);
						stopTrading(true);
						return;
					}
				}

				for (TradeGatewaySetting tradeGteway : tradeGateways) {
					String gatewayID = tradeGteway.getGatewayID();
					int gatewayFixedPos = tradeGteway.getTradeFixedPos();
					int tradePos = gatewayFixedPos;
					if (gatewayFixedPos > 0) {
						PositionDetail positionDetail = contractPositionDetail.getPositionDetailMap().get(gatewayID);

						if (positionDetail != null) {
							int gatewayShortPos = positionDetail.getShortPos();
							int gatewayShortOpenFrozenPos = positionDetail.getShortOpenFrozen();
							if (gatewayShortPos + gatewayShortOpenFrozenPos == gatewayFixedPos) {
								log.warn("合约{}接口{}的空头持仓量加开仓冻结量已经达到预设值,指令忽略!", rtSymbol, gatewayID);
								continue;
							} else if (gatewayShortPos > gatewayFixedPos) {
								log.error("合约{}接口{}的空头持仓量{}加开仓冻结量{}已经超过预设值{},指令忽略!", rtSymbol, gatewayID,
										gatewayShortPos, gatewayShortOpenFrozenPos, gatewayFixedPos);
								stopTrading(true);
								continue;
							} else {
								tradePos = gatewayFixedPos - (gatewayShortPos + gatewayShortOpenFrozenPos);
							}
						}

						sellShort(rtSymbol, tradePos, price, gatewayID);
					} else {
						log.error("合约{}接口{}配置中的仓位大小不正确", rtSymbol, gatewayID);
						stopTrading(true);
					}
				}
			} else {
				log.error("未找到合约{}配置中的接口配置", rtSymbol);
				stopTrading(true);
			}
		} else {
			log.error("未找到合约{}的配置", rtSymbol);
			stopTrading(true);
		}

	}

	/**
	 * 根据仓位通用买平空逻辑
	 * @param rtSymbol
	 * @param price
	 * @param offsetType
	 */
	private void commonBuyToCoverByPosition(String rtSymbol, double price, int offsetType) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);
		if (contractPositionDetail != null) {
			int shortPos = contractPositionDetail.getShortPos();
			if (shortPos == 0) {
				log.warn("合约{}的空头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (shortPos < 0) {
				log.error("合约{}的空头总持仓量{}小于0!", rtSymbol, shortPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {
					continue;
				}

				if (positionDetail.getShortPos() > 0) {
					if (offsetType >= 0) {
						if (positionDetail.getShortOpenFrozen() > 0) {
							log.warn("合约{}接口{}空头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID,
									positionDetail.getShortOpenFrozen());
						}

						if (positionDetail.getShortTd() > 0) {
							buyToCoverTd(rtSymbol, positionDetail.getShortTd(), price, gatewayID);
						}
					}
					if (offsetType <= 0) {
						if (positionDetail.getShortYd() > 0) {
							buyToCoverYd(rtSymbol, positionDetail.getShortYd(), price, gatewayID);
						}
					}

				} else {
					log.error("合约{}接口{}空头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	/**
	 * 根据仓位买平空
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToCoverByPosition(String rtSymbol, double price) {
		commonBuyToCoverByPosition(rtSymbol, price, 0);

	}

	/**
	 * 根据仓位买平今空
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToCoverTdByPosition(String rtSymbol, double price) {
		commonBuyToCoverByPosition(rtSymbol, price, 1);

	}

	/**
	 * 根据仓位买平昨空
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToCoverYdByPosition(String rtSymbol, double price) {
		commonBuyToCoverByPosition(rtSymbol, price, -1);
	}

	/**
	 * 根据仓位买多锁空
	 * @param rtSymbol
	 * @param price
	 */
	public void buyToLockByPosition(String rtSymbol, double price) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);
		if (contractPositionDetail != null) {
			int shortPos = contractPositionDetail.getShortPos();
			if (shortPos == 0) {
				log.warn("合约{}的空头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (shortPos < 0) {
				log.error("合约{}的空头总持仓量{}小于0!", rtSymbol, shortPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {
					continue;
				}
				if (positionDetail.getShortOpenFrozen() > 0) {
					log.warn("合约{}接口{}空头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID, positionDetail.getShortOpenFrozen());
				}
				if (positionDetail.getShortPos() > 0) {
					buy(rtSymbol, positionDetail.getShortPos(), price, gatewayID);
				} else {
					log.error("合约{}接口{}空头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	/**
	 * 根据仓位卖空锁多
	 * @param rtSymbol
	 * @param price
	 */
	public void sellShortToLockByPosition(String rtSymbol, double price) {
		ContractPositionDetail contractPositionDetail = contractPositionMap.get(rtSymbol);

		if (contractPositionDetail != null) {
			int longPos = contractPositionDetail.getLongPos();
			if (longPos == 0) {
				log.warn("合约{}的多头总持仓量为0,指令终止!", rtSymbol);
				return;
			} else if (longPos < 0) {
				log.error("合约{}的多头总持仓量{}小于0!", rtSymbol, longPos);
				stopTrading(true);
				return;
			}

			for (Entry<String, PositionDetail> entry : contractPositionDetail.getPositionDetailMap().entrySet()) {
				String gatewayID = entry.getKey();
				PositionDetail positionDetail = entry.getValue();
				if (positionDetail == null) {

					continue;
				}

				if (positionDetail.getLongPos() > 0) {
					if (positionDetail.getLongOpenFrozen() > 0) {
						log.warn("合约{}接口{}多头开仓冻结为{},这部分不会被处理", rtSymbol, gatewayID, positionDetail.getLongOpenFrozen());
					}
					sellShort(rtSymbol, positionDetail.getLongPos(), price, gatewayID);
				} else {
					log.error("合约{}接口{}多头持仓大小不正确", rtSymbol, gatewayID);
					stopTrading(true);
				}
			}
		} else {
			log.error("未找到合约{}的持仓信息", rtSymbol);
		}
	}

	@Override
	public void processTick(Tick tick) {
		try {
			// 处理停止单
			processStopOrder(tick);
			onTick(tick);
			// 基于合约的onBar和onMinBar
			String bgKey = tick.getRtSymbol();
			// 基于合约+接口的onBar和onMinBar,使用这个key会多次触发同一策略下同一品种的相同时间bar的事件
			// String bgKey = tick.getRtSymbol()+tick.getGatewayID();
			BarGenerator barGenerator;
			if (barGeneratorMap.containsKey(bgKey)) {
				barGenerator = barGeneratorMap.get(bgKey);
			} else {
				barGenerator = new BarGenerator(new CallBackXMinBar() {
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
				ContractPositionDetail contractPositionDetail = contractPositionMap.get(trade.getRtSymbol());
				contractPositionDetail.updateTrade(trade);
				savePosition();
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
			workingOrderMap.put(order.getRtOrderID(), order);
			if (RtConstant.STATUS_FINISHED.contains(order.getStatus())) {
				workingOrderMap.remove(order.getRtOrderID());
			}
			ContractPositionDetail contractPositionDetail = contractPositionMap.get(order.getRtSymbol());
			contractPositionDetail.updateOrder(order);
			onOrder(order);
		} catch (Exception e) {
			stopTrading(true);
			log.error("{} 调用onOrder发生异常,停止策略!!!", logStr, e);
		}
	}

	/**
	 * 处理Bar
	 * @param bar
	 */
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
				xMinBarGenerator = new XMinBarGenerator(strategySetting.getxMin(), new CallBackXMinBar() {
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

	// ##############################################################################

	/**
	 * CallBack接口,用于注册Bar生成器回调事件
	 */
	public static interface CallBackXMinBar {
		void call(Bar bar);
	}

	/**
	 * 1分钟Bar生成器
	 */
	public static class BarGenerator {

		private Bar bar = null;
		private Tick lastTick = null;
		CallBackXMinBar callBackXMinBar;

		BarGenerator(CallBackXMinBar callBackXMinBar) {
			this.callBackXMinBar = callBackXMinBar;
		}

		/**
		 * 更新Tick数据
		 * 
		 * @param tick
		 */
		public void updateTick(Tick tick) {

			boolean newMinute = false;

			if (lastTick != null) {
				// 此处过滤用于一个策略在多个接口订阅了同一个合约的情况下,Tick到达顺序和实际产生顺序不一致或者重复的情况
				if (tick.getDateTime().getMillis() <= lastTick.getDateTime().getMillis()) {
					return;
				}
			}

			if (bar == null) {
				bar = new Bar();
				newMinute = true;
			} else if (bar.getDateTime().getMinuteOfDay() != tick.getDateTime().getMinuteOfDay()) {

				bar.setDateTime(bar.getDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
				bar.setActionTime(bar.getDateTime().toString(RtConstant.T_FORMAT_WITH_MS_FORMATTER));

				// 回调OnBar方法
				callBackXMinBar.call(bar);

				bar = new Bar();
				newMinute = true;
			}

			if (newMinute) {
				bar.setGatewayID(tick.getGatewayID());
				bar.setExchange(tick.getExchange());
				bar.setRtSymbol(tick.getRtSymbol());
				bar.setSymbol(tick.getSymbol());

				bar.setTradingDay(tick.getTradingDay());
				;
				bar.setActionDay(tick.getActionDay());

				bar.setOpen(tick.getLastPrice());
				bar.setHigh(tick.getLastPrice());
				bar.setLow(tick.getLastPrice());

				bar.setDateTime(tick.getDateTime());
			} else {
				bar.setHigh(Math.max(bar.getHigh(), tick.getLastPrice()));
				bar.setLow(Math.min(bar.getLow(), tick.getLastPrice()));
			}

			bar.setClose(tick.getLastPrice());
			bar.setOpenInterest(tick.getOpenInterest());
			if (lastTick != null) {
				bar.setVolume(bar.getVolume() + (tick.getVolume() - lastTick.getVolume()));
			}

			lastTick = tick;
		}
	}

	/**
	 * X分钟Bar生成器,xMin在策略初始化时指定,当值大于1小于时生效,建议此数值不要大于120
	 */
	public static class XMinBarGenerator {

		private int xMin;
		private Bar xMinBar = null;
		CallBackXMinBar callBackXMinBar;

		XMinBarGenerator(int xMin, CallBackXMinBar callBackXMinBar) {
			this.callBackXMinBar = callBackXMinBar;
			this.xMin = xMin;
		}

		public void updateBar(Bar bar) {

			if (xMinBar == null) {
				xMinBar = new Bar();
				xMinBar.setGatewayID(bar.getGatewayID());
				xMinBar.setExchange(bar.getExchange());
				xMinBar.setRtSymbol(bar.getRtSymbol());
				xMinBar.setSymbol(bar.getSymbol());

				xMinBar.setTradingDay(bar.getTradingDay());
				xMinBar.setActionDay(bar.getActionDay());

				xMinBar.setOpen(bar.getOpen());
				xMinBar.setHigh(bar.getHigh());
				xMinBar.setLow(bar.getLow());

				xMinBar.setDateTime(bar.getDateTime());

			} else {
				xMinBar.setHigh(Math.max(xMinBar.getHigh(), bar.getHigh()));
				xMinBar.setLow(Math.min(xMinBar.getLow(), bar.getLow()));
			}

			if ((xMinBar.getDateTime().getMinuteOfDay() + 1) % xMin == 0) {
				bar.setDateTime(bar.getDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
				bar.setActionTime(bar.getDateTime().toString(RtConstant.T_FORMAT_WITH_MS_FORMATTER));

				// 回调onXMinBar方法
				callBackXMinBar.call(xMinBar);

				xMinBar = null;
			}

		}
	}

}
