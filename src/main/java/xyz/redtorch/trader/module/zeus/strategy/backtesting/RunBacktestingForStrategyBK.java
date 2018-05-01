package xyz.redtorch.trader.module.zeus.strategy.backtesting;

import java.util.ArrayList;
import java.util.List;

import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.engine.data.impl.DataEngineImpl;
import xyz.redtorch.trader.module.zeus.BacktestingEngine;
import xyz.redtorch.trader.module.zeus.BacktestingEngine.BacktestingSection;
import xyz.redtorch.trader.module.zeus.impl.BacktestingEngineImpl;


/**
 * @author sun0x00@gmail.com
 */
public class RunBacktestingForStrategyBK {
	public static void main(String[] args) throws Exception {
		String strategyID = "ID-StrategyBK01";
		boolean reloadStrategyEveryday = false;
		int backtestingDataMode = BacktestingEngine.DATA_MODE_BAR;
		List<BacktestingSection> backestingSectionList = new ArrayList<>();
		// 分段回测
		BacktestingSection backtestingSection= new BacktestingSection();
		backtestingSection.setStartDate("20180222");
		backtestingSection.setEndDate("20180319");
		backtestingSection.addAliasRtSymbol("IC", "IC1803.CFFEX");
		backtestingSection.addAliasRtSymbol("IH", "IH1803.CFFEX");
		
		backtestingSection.addSubscribeRtSymbol("9999.724SN02.187.10030", "IF1803.CFFEX");
		backtestingSection.addSubscribeRtSymbol("9999.724SN01.187.10030", "IC1803.CFFEX");
		backtestingSection.addSubscribeRtSymbol("9999.724SN01.187.10030", "IH1803.CFFEX");
		
		backestingSectionList.add(backtestingSection);
		
//		backtestingSection= new BacktestingSection();
//		backtestingSection.setStartDate("20180321");
//		backtestingSection.setEndDate("20180323");
//		backtestingSection.addPrefixSuffix("IF", "1804");
//		backtestingSection.addPrefixSuffix("IC", "1804");
//		backtestingSection.addPrefixSuffix("IH", "1804");
//		backestingSectionList.add(backtestingSection);
		
		DataEngine dataEngine = new DataEngineImpl();
		
		BacktestingEngine backtestingEngine = new BacktestingEngineImpl(dataEngine, strategyID, backestingSectionList, backtestingDataMode, reloadStrategyEveryday);
		backtestingEngine.runBacktesting();
	}
}
