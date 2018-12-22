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
		log.info("==============================================");
		log.info(JSON.toJSONString(strategySetting.getParamMap()));
		log.info("==============================================");
		
		if(strategySetting.getVarMap().containsKey("tickCount")) {
			tickCount = Long.valueOf(strategySetting.getVarMap().get("tickCount"));
		}
		
		if(strategySetting.getVarMap().containsKey("barCount")) {
			barCount = Long.valueOf(strategySetting.getVarMap().get("barCount"));
		}
	}

	@Override
	public void onStartTrading() throws Exception {
		
	}

	@Override
	public void onStopTrading(boolean isException) throws Exception {
		
	}

	@Override
	public void onTick(Tick tick) throws Exception {
		tickCount++;
		setVarValue("tickCount", tickCount+"");
	}

	@Override
	public void onBar(Bar bar) throws Exception {
		
		barCount++;
		setVarValue("barCount", barCount+"");
		
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
		
	}

	@Override
	public void onOrder(Order order) throws Exception {
	}

	@Override
	public void onTrade(Trade trade) throws Exception {
		
	}

}
