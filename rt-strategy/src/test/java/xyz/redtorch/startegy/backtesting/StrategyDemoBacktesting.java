package xyz.redtorch.startegy.backtesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import xyz.redtorch.core.CoreRunConfiguration;
import xyz.redtorch.core.zeus.ZeusBacktestingEngine;
import xyz.redtorch.core.zeus.ZeusDataService;
import xyz.redtorch.core.zeus.ZeusBacktestingEngine.BacktestingSection;
import xyz.redtorch.core.zeus.ZeusBacktestingEngine.OptimizationSetting;
import xyz.redtorch.core.zeus.impl.ZeusBacktestingEngineImpl;
import xyz.redtorch.core.zeus.strategy.StrategySetting;

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

		String strategyID = "DEMO00";
		boolean reloadStrategyEveryday = false;
		int backtestingDataMode = ZeusBacktestingEngine.DATA_MODE_BAR;
		List<BacktestingSection> backestingSectionList = new ArrayList<>();
		// 分段回测
		BacktestingSection backtestingSection = new BacktestingSection();
		backtestingSection.setStartDate("20180222");
		backtestingSection.setEndDate("20180319");
		backtestingSection.addAliasRtSymbol("IC", "IC1803.CFFEX");
		backtestingSection.addAliasRtSymbol("IH", "IH1803.CFFEX");

		backtestingSection.addSubscribeReq("7acc51e2434841eaa0970b4ce5cf89e8", "IF1803.CFFEX");
		backtestingSection.addSubscribeReq("7acc51e2434841eaa0970b4ce5cf89e8", "IC1803.CFFEX");
		backtestingSection.addSubscribeReq("e6527bd9ca074f48be400119e4f9a10c", "IH1803.CFFEX");

		backestingSectionList.add(backtestingSection);

		backtestingSection= new BacktestingSection();
		backtestingSection.setStartDate("20180321");
		backtestingSection.setEndDate("20180323");
		backtestingSection.addAliasRtSymbol("IC", "IC1804.CFFEX");
		backtestingSection.addAliasRtSymbol("IH", "IH1804.CFFEX");

		backtestingSection.addSubscribeReq("7acc51e2434841eaa0970b4ce5cf89e8", "IF1804.CFFEX");
		backtestingSection.addSubscribeReq("7acc51e2434841eaa0970b4ce5cf89e8", "IC1804.CFFEX");
		backtestingSection.addSubscribeReq("e6527bd9ca074f48be400119e4f9a10c", "IH1804.CFFEX"); 
		backestingSectionList.add(backtestingSection);
		
		/////////////////////////////普通回测/////////////////////////////////////////////
		StrategySetting strategySetting = zeusDataService.loadStrategySetting(strategyID);
		ZeusBacktestingEngine backtestingEngine = new ZeusBacktestingEngineImpl(zeusDataService, strategySetting,
				backestingSectionList, backtestingDataMode, reloadStrategyEveryday, backtestingOutputDir);
		backtestingEngine.runBacktesting();
		///////////////////////////////////////////////////////////////////////////////
		
		
//		//////////////////////////////尝试寻参的回测///////////////////////////////////////
//		// 创建参数生成器
//		OptimizationSetting optimizationSetting = new OptimizationSetting();
//		// 加入参数生成规则
//		optimizationSetting.addParameter(parameterName, start, end, step);
//		optimizationSetting.addStrParameter(parameterName, parameterSet);
//		//生成参数集合
//		List<Map<String, String>> paramMapList = optimizationSetting.generateSetting();
//		 
//		// 记录ID列表（可以根据这个列表去输出文件夹定位结果文件）
//		List<String> optimizationStrategyIDList = new ArrayList<>();
//		 
//		
//		// ===========此处可修改为多线程=======================
//		for(Map<String, String> paramMap:paramMapList) {
//			String optimizationStrategyID = UUID.randomUUID().toString().replace("-", "").toLowerCase();
//			optimizationStrategyIDList.add(optimizationStrategyID);
//			strategySetting.setParamMap(paramMap);
//			strategySetting.setStrategyID(optimizationStrategyID);
//			
//			ZeusBacktestingEngine backtestingEngine = new ZeusBacktestingEngineImpl(zeusDataService, strategySetting,
//					backestingSectionList, backtestingDataMode, reloadStrategyEveryday, backtestingOutputDir);
//			backtestingEngine.runBacktesting();
//		}
//		// ==============================================
//		/////////////////////////////////////////////////////////////////////////////


		
	}
}
