package xyz.redtorch.startegy.backtesting;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import xyz.redtorch.core.CoreRunConfiguration;
import xyz.redtorch.core.zeus.BacktestingEngine;
import xyz.redtorch.core.zeus.ZeusDataService;
import xyz.redtorch.core.zeus.BacktestingEngine.BacktestingSection;
import xyz.redtorch.core.zeus.impl.BacktestingEngineImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreRunConfiguration.class)
@PropertySource(value = { "classpath:rt-core.properties" })
public class StrategyDemoBacktesting {

	@Autowired
	private ZeusDataService zeusDataService;

	@Value("${module.zeus.backtesting.output.dir}")
	private String backtestingOutputDir;

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testStrategy() throws Exception {

		String strategyID = "StrategyDemo01";
		boolean reloadStrategyEveryday = false;
		int backtestingDataMode = BacktestingEngine.DATA_MODE_BAR;
		List<BacktestingSection> backestingSectionList = new ArrayList<>();
		// 分段回测
		BacktestingSection backtestingSection = new BacktestingSection();
		backtestingSection.setStartDate("20180222");
		backtestingSection.setEndDate("20180319");
		backtestingSection.addAliasRtSymbol("IC", "IC1803.CFFEX");
		backtestingSection.addAliasRtSymbol("IH", "IH1803.CFFEX");

		backtestingSection.addSubscribeRtSymbol("9999.724SN02.187.10030", "IF1803.CFFEX");
		backtestingSection.addSubscribeRtSymbol("9999.724SN01.187.10030", "IC1803.CFFEX");
		backtestingSection.addSubscribeRtSymbol("9999.724SN01.187.10030", "IH1803.CFFEX");

		backestingSectionList.add(backtestingSection);

		// backtestingSection= new BacktestingSection();
		// backtestingSection.setStartDate("20180321");
		// backtestingSection.setEndDate("20180323");
		// backtestingSection.addAliasRtSymbol("IF", "1804");
		// backtestingSection.addAliasRtSymbol("IC", "1804");
		// backtestingSection.addAliasRtSymbol("IH", "1804");
		// backestingSectionList.add(backtestingSection);

		BacktestingEngine backtestingEngine = new BacktestingEngineImpl(zeusDataService, strategyID,
				backestingSectionList, backtestingDataMode, reloadStrategyEveryday, backtestingOutputDir);
		backtestingEngine.runBacktesting();
	}
}
