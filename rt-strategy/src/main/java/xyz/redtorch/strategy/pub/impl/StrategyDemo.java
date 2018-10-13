package xyz.redtorch.strategy.pub.impl;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.zeus.ZeusEngineService;
import xyz.redtorch.core.zeus.entity.StopOrder;
import xyz.redtorch.core.zeus.strategy.StrategyAbstract;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public class StrategyDemo extends StrategyAbstract{

	public StrategyDemo(ZeusEngineService zeusEngineService, StrategySetting strategySetting) {
		super(zeusEngineService, strategySetting);
	}

	@Override
	public void onInit() throws Exception {
		
	}

	@Override
	public void onStartTrading() throws Exception {
		
	}

	@Override
	public void onStopTrading(boolean isException) throws Exception {
		
	}

	@Override
	public void onTick(Tick tick) throws Exception {
		Integer i = 0;
		if(strategySetting.getVarMap().containsKey("testTick")) {
			i = Integer.valueOf(strategySetting.getVarMap().get("testTick"));
		}
		i++;
		setVarValue("testTick", i+"");
		System.out.println(JSON.toJSONString(tick));
	}

	@Override
	public void onBar(Bar bar) throws Exception {

		Integer i = 0;
		if(strategySetting.getVarMap().containsKey("testBar")) {
			i = Integer.valueOf(strategySetting.getVarMap().get("testBar"));
		}
		i++;
		setVarValue("testBar", i+"");
		
//		int timeTest = bar.getDateTime().getMinuteOfHour() + bar.getDateTime().getHourOfDay()*100;
//		
//		String icSymbol = strategySetting.getContractByAlias("IC").getRtSymbol();
//		String ihSymbol = strategySetting.getContractByAlias("IH").getRtSymbol();
//		if(timeTest == 945 && trading) {
//			if(bar.getRtSymbol().equals(icSymbol)) {
//				buyByPreset(icSymbol, bar.getClose()+1);
//			}else if(bar.getRtSymbol().equals(ihSymbol)) {
//				sellShortByPreset(ihSymbol, bar.getClose()-1);
//			}
//		}else if(timeTest == 1315 && trading) {
//			if(bar.getRtSymbol().equals(icSymbol)) {
//				sellByPosition(icSymbol, bar.getClose()-1);
//			}else if(bar.getRtSymbol().equals(ihSymbol)) {
//				buyToCoverByPosition(ihSymbol, bar.getClose()+1);
//			}
//		}else if(timeTest == 1445 && trading) {
//			if(bar.getRtSymbol().equals(icSymbol)) {
//				buyByPreset(icSymbol, bar.getClose()+1);
//			}else if(bar.getRtSymbol().equals(ihSymbol)) {
//				sellShortByPreset(ihSymbol, bar.getClose()-1);
//			}
//		}
		
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

	@Override
	public void onStopOrder(StopOrder StopOrder) throws Exception {
		
	}

}
