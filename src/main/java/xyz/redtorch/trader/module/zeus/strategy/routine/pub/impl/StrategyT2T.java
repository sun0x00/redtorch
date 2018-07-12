package xyz.redtorch.trader.module.zeus.strategy.routine.pub.impl;

import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.entity.StopOrder;
import xyz.redtorch.trader.module.zeus.strategy.StrategyAbstract;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public class StrategyT2T extends StrategyAbstract{

	public StrategyT2T(ZeusEngine zeusEngine, StrategySetting strategySetting) {
		super(zeusEngine, strategySetting);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onInit() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrading() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrading(boolean isException) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTick(Tick tick) throws Exception {
		Integer i = 0;
		if(strategySetting.getVarMap().containsKey("testTick")) {
			i = Integer.valueOf(strategySetting.getVarMap().get("testTick"));
		}
		i++;
		setVarValue("testTick", i+"");
		// TODO Auto-generated method stub
		if(trading) {
			if("IH1805".equals(tick.getSymbol())) {
				//sendOrder(tick.getRtSymbol(), ZeusConstant.ORDER_BUY, RtConstant.PRICETYPE_LIMITPRICE, tick.getBidPrice1(), 1, "9999.724SN01.187.10030");
			}
		}
		
	}

	@Override
	public void onBar(Bar bar) throws Exception {
		Integer i = 0;
		if(strategySetting.getVarMap().containsKey("testBar")) {
			i = Integer.valueOf(strategySetting.getVarMap().get("testBar"));
		}
		i++;
		setVarValue("testBar", i+"");
		
	}

	@Override
	public void onXMinBar(Bar bar) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOrder(Order order) throws Exception {
	}

	@Override
	public void onTrade(Trade trade) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopOrder(StopOrder StopOrder) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
