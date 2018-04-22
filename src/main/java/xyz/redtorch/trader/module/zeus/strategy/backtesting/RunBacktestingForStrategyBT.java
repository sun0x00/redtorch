package xyz.redtorch.trader.module.zeus.strategy.backtesting;

import java.util.ArrayList;
import java.util.List;

import xyz.redtorch.trader.engine.data.DataEngine;
import xyz.redtorch.trader.engine.data.impl.DataEngineImpl;
import xyz.redtorch.trader.module.zeus.BacktestingEngine;
import xyz.redtorch.trader.module.zeus.BacktestingEngine.BacktestingSection;
import xyz.redtorch.trader.module.zeus.BacktestingUtil;
import xyz.redtorch.trader.module.zeus.impl.BacktestingEngineImpl;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;


/**
 * @author sun0x00@gmail.com
 */
public class RunBacktestingForStrategyBT {
	public static void main(String[] args) throws Exception {
		String strategyClassName = "xyz.redtorch.trader.module.zeus.strategy.impl.StrategyBT";
		StrategySetting strategySetting = BacktestingUtil.getConfigFileByClassName(strategyClassName);
		boolean reloadStrategyEveryday = false;
		int backtestingDataMode = BacktestingEngine.DATA_MODE_BAR;
		List<BacktestingSection> backestingSectionList = new ArrayList<>();
		// 分段回测
		BacktestingSection backtestingSection= new BacktestingSection();
		backtestingSection.setStartDate("20180222");
		backtestingSection.setEndDate("20180319");
		backtestingSection.addPrefixSuffix("IF", "1803");
		backtestingSection.addPrefixSuffix("IC", "1803");
		backtestingSection.addPrefixSuffix("IH", "1803");
		backestingSectionList.add(backtestingSection);
		
//		backtestingSection= new BacktestingSection();
//		backtestingSection.setStartDate("20180321");
//		backtestingSection.setEndDate("20180323");
//		backtestingSection.addPrefixSuffix("IF", "1804");
//		backtestingSection.addPrefixSuffix("IC", "1804");
//		backtestingSection.addPrefixSuffix("IH", "1804");
//		backestingSectionList.add(backtestingSection);
		
		DataEngine dataEngine = new DataEngineImpl();
		
		BacktestingEngine backtestingEngine = new BacktestingEngineImpl(dataEngine, strategyClassName, strategySetting, backestingSectionList, backtestingDataMode, reloadStrategyEveryday);
		backtestingEngine.runBacktesting();
	}
}
