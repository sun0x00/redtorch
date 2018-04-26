package xyz.redtorch.trader.module.zeus.strategy.impl;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Order;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.trader.module.zeus.ZeusConstant;
import xyz.redtorch.trader.module.zeus.ZeusEngine;
import xyz.redtorch.trader.module.zeus.entity.StopOrder;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;
import xyz.redtorch.trader.module.zeus.strategy.StrategyTemplate;

/**
 * @author sun0x00@gmail.com
 */
public class StrategyT2T extends StrategyTemplate{

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
		// TODO Auto-generated method stub
		if(trading) {
			if("IH1805".equals(tick.getSymbol())) {
				sendOrder(tick.getRtSymbol(), ZeusConstant.ORDER_BUY, RtConstant.PRICETYPE_LIMITPRICE, tick.getBidPrice1(), 1, "9999.724SN01.187.10030");
			}
		}
		
	}

	@Override
	public void onBar(Bar bar) throws Exception {
		// TODO Auto-generated method stub
		
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
