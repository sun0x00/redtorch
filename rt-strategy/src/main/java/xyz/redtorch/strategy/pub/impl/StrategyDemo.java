package xyz.redtorch.strategy.pub.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.entity.PositionDetail;
import xyz.redtorch.core.zeus.strategy.StrategyAbstract;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public class StrategyDemo extends StrategyAbstract{
	
	Logger log = LoggerFactory.getLogger(StrategyDemo.class);
	
	Long tickCount = 0L;
	Long barCount = 0L;

	public StrategyDemo(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
		super(zeusEngineService, strategySetting);
	}

	@Override
	public void onInit() throws Exception {
		log.info("初始化！");
		log.info("=================ParamMap=============================");
		log.info(JSON.toJSONString(strategySetting.getParamMap()));
		log.info("======================================================");
		log.info("==================VarMap==============================");
		log.info(JSON.toJSONString(strategySetting.getVarMap()));
		log.info("======================================================");
		
		if(strategySetting.getVarMap().containsKey("tickCount")) {
			tickCount = Long.valueOf(strategySetting.getVarMap().get("tickCount"));
		}
		
		if(strategySetting.getVarMap().containsKey("barCount")) {
			barCount = Long.valueOf(strategySetting.getVarMap().get("barCount"));
		}
	}

	@Override
	public void onStartTrading() throws Exception {
		log.info("开始交易！");
	}

	@Override
	public void onStopTrading(boolean finishedCorrectly) throws Exception {
		log.info("停止交易！是否正常停止-[{}]",finishedCorrectly);
	}

	@Override
	public void onTick(Tick tick) throws Exception {
		tickCount++;
		setVarValue("tickCount", tickCount+"");
//		
//		log.info("################## TICK #########################");
//		log.info(JSON.toJSONString(tick));
	}

	@Override
	public void onBar(Bar bar) throws Exception {
		
//		log.info("################## BAR #########################");
//		log.info(JSON.toJSONString(bar));
		
		barCount++;
		setVarValue("barCount", barCount+"");
		
//		if(tradingStatus) {
//			sellTd("IF1903.CFFEX", 1, bar.getClose()-1, "095076.CNY.657552c83e33496e8d675edaae9acea9");
//		}
		
		int tradeTime = bar.getDateTime().getMinuteOfHour() + bar.getDateTime().getHourOfDay()*100;
		
		String icSymbol = strategySetting.getContractByAlias("IC").getRtSymbol();
		String ihSymbol = strategySetting.getContractByAlias("IH").getRtSymbol();
		if(tradeTime == 945 && tradingStatus) {
			if(bar.getRtSymbol().equals(icSymbol)) {
				PositionDetail pd = contractPositionMap.get(icSymbol).getPositionDetailMap().get("a62c17309a8d4565a87b35792bbc1763.CNY.888888");
				int longPos = 0;
				if( pd != null ) {
					longPos = pd.getLongPos()+pd.getLongOpenFrozen();
					if(longPos<0) {
						log.error("检测到仓位异常，longPos不应小于0");
						stopTrading(false);
						return;
					}
				}
				int posDiff = 2 - longPos;
				if(posDiff<0) {
					log.error("仓位异常，应开仓数小于0");
					stopTrading(false);
					return;
				}else if(posDiff == 0) {
					log.warn("无需开仓");
				}else {
					buy(icSymbol, posDiff, bar.getClose()+1, "a62c17309a8d4565a87b35792bbc1763.CNY.888888");
				}
	
			}else if(bar.getRtSymbol().equals(ihSymbol)) {
				PositionDetail pd = contractPositionMap.get(ihSymbol).getPositionDetailMap().get("a62c17309a8d4565a87b35792bbc1763.CNY.666666");
				int shortPos = 0;
				if( pd != null ) {
					shortPos = pd.getShortPos()+pd.getShortOpenFrozen();
					if(shortPos<0) {
						log.error("检测到仓位异常，shortPos不应小于0");
						stopTrading(false);
						return;
					}
				}
				int posDiff = 2 - shortPos;
				if(posDiff<0) {
					log.error("仓位异常，应开仓数小于0");
					stopTrading(false);
					return;
				}else if(posDiff == 0) {
					log.warn("无需开仓");
				}else {
					sellShort(ihSymbol, posDiff,  bar.getClose()-1, "a62c17309a8d4565a87b35792bbc1763.CNY.666666");
				}
				
			}
		}else if(tradeTime == 1315 && tradingStatus) {
			if(bar.getRtSymbol().equals(icSymbol)) {
				PositionDetail pd = contractPositionMap.get(icSymbol).getPositionDetailMap().get("a62c17309a8d4565a87b35792bbc1763.CNY.888888");
				if( pd != null ) {
					int longPos = pd.getLongPos()-pd.getLongPosFrozen();
					if(longPos<0) {
						log.error("检测到仓位异常，longPos不应小于0");
						stopTrading(false);
						return;
					}else if(longPos == 0) {
						log.error("无有效多头持仓");
					}else {
						sell(icSymbol, longPos,  bar.getClose()-1, "a62c17309a8d4565a87b35792bbc1763.CNY.888888");
					}
				}else {
					log.warn("无持仓");
				}
			}else if(bar.getRtSymbol().equals(ihSymbol)) {
				
				PositionDetail pd = contractPositionMap.get(ihSymbol).getPositionDetailMap().get("a62c17309a8d4565a87b35792bbc1763.CNY.666666");
				if( pd != null ) {
					int shortPos = pd.getShortPos()-pd.getShortPosFrozen();
					if(shortPos<0) {
						log.error("检测到仓位异常，shortPos不应小于0");
						stopTrading(false);
						return;
					}else if(shortPos == 0) {
						log.error("无有效多头持仓");
					}else {
						buyToCover(ihSymbol, 2, bar.getClose()+1, "a62c17309a8d4565a87b35792bbc1763.CNY.666666");
					}
				}else {
					log.warn("无持仓");
				}
			}
		}else if(tradeTime == 1445 && tradingStatus) {
			if(bar.getRtSymbol().equals(icSymbol)) {
				PositionDetail pd = contractPositionMap.get(icSymbol).getPositionDetailMap().get("a62c17309a8d4565a87b35792bbc1763.CNY.888888");
				int longPos = 0;
				if( pd != null ) {
					longPos = pd.getLongPos()+pd.getLongOpenFrozen();
					if(longPos<0) {
						log.error("检测到仓位异常，longPos不应小于0");
						stopTrading(false);
						return;
					}
				}
				int posDiff = 2 - longPos;
				if(posDiff<0) {
					log.error("仓位异常，应开仓数小于0");
					stopTrading(false);
					return;
				}else if(posDiff == 0) {
					log.warn("无需开仓");
				}else {
					buy(icSymbol, posDiff, bar.getClose()+1, "a62c17309a8d4565a87b35792bbc1763.CNY.888888");
				}
	
			}else if(bar.getRtSymbol().equals(ihSymbol)) {
				PositionDetail pd = contractPositionMap.get(ihSymbol).getPositionDetailMap().get("a62c17309a8d4565a87b35792bbc1763.CNY.666666");
				int shortPos = 0;
				if( pd != null ) {
					shortPos = pd.getShortPos()+pd.getShortOpenFrozen();
					if(shortPos<0) {
						log.error("检测到仓位异常，shortPos不应小于0");
						stopTrading(false);
						return;
					}
				}
				int posDiff = 2 - shortPos;
				if(posDiff<0) {
					log.error("仓位异常，应开仓数小于0");
					stopTrading(false);
					return;
				}else if(posDiff == 0) {
					log.warn("无需开仓");
				}else {
					sellShort(ihSymbol, posDiff,  bar.getClose()-1, "a62c17309a8d4565a87b35792bbc1763.CNY.666666");
				}
				
			}
		}
		
	}

	@Override
	public void onXMinBar(Bar bar) throws Exception {
		log.info("################## X Min BAR #########################");
		log.info(JSON.toJSONString(bar));
	}

	@Override
	public void onOrder(Order order) throws Exception {
		log.info("################## ORDER #########################");
		log.info(JSON.toJSONString(order));
	}

	@Override
	public void onTrade(Trade trade) throws Exception {
		log.info("################## Trade #########################");
		log.info(JSON.toJSONString(trade));
		
	}

}
