package xyz.redtorch.startegy.backtesting;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.zeus.strategy.StrategySetting;
import xyz.redtorch.core.zeus.strategy.StrategySetting.ContractSetting;

/**
 * 生成策略配置的例子
 * 
 * @author sun0x00@gmail.com
 *
 */
public class StrategySettingTest {
	public static void main(String[] args) {
		StrategySetting ss = new StrategySetting();
		ss.setStrategyID("DEMO00");
		ss.setStrategyName("DEMO00");
		ss.setClassName("xyz.redtorch.strategy.pub.impl.StrategyDemo");
		ss.setPreTradingDay("20180314");
		ss.setTradingDay("20180315");
		/////////////////////////////////////////////////////////////
		List<ContractSetting> contracts = new ArrayList<>();
		ContractSetting cs = new ContractSetting();
		cs.setAlias("IH");
		cs.setSymbol("IH1903");
		cs.setExchange("CFFEX");
		cs.setRtSymbol("IH1903.CFFEX");
		cs.setSize(300);
		cs.setBacktestingPriceTick(0.2);
		cs.setBacktestingRate(0.001);
		cs.setBacktestingSlippage(0.4);
		contracts.add(cs);
		
		cs = new ContractSetting();
		cs.setAlias("IC");
		cs.setSymbol("IC1903");
		cs.setExchange("CFFEX");
		cs.setRtSymbol("IC1903.CFFEX");
		cs.setSize(200);
		cs.setBacktestingPriceTick(0.2);
		cs.setBacktestingRate(0.001);
		cs.setBacktestingSlippage(0.4);
		contracts.add(cs);
		
		cs = new ContractSetting();
		cs.setAlias("IF");
		cs.setSymbol("IF1903");
		cs.setExchange("CFFEX");
		cs.setRtSymbol("IF1903.CFFEX");
		cs.setSize(300);
		cs.setBacktestingPriceTick(0.2);
		cs.setBacktestingRate(0.001);
		cs.setBacktestingSlippage(0.4);
		contracts.add(cs);
		
		ss.setContracts(contracts);
		/////////////////////////////////////////////////////////////
		ss.setLastTradingDay(false);
		
		ss.getParamMap().put("P0", "0");
		ss.getParamMap().put("P1", "1");
		
		ss.getVarMap().put("V0", "0");
		ss.getVarMap().put("V1", "1");
		
		SubscribeReq sr = new SubscribeReq();
		sr.setSymbol("IH1903");
		sr.setExchange("CFFEX");
		sr.setRtSymbol("IH1903.CFFEX");
		sr.setGatewayID("657552c83e33496e8d675edaae9acea9");
		sr.setProductClass("");
		ss.getSubscribeReqList().add(sr);
		
		sr = new SubscribeReq();
		sr.setSymbol("IF1903");
		sr.setExchange("CFFEX");
		sr.setRtSymbol("IF1903.CFFEX");
		sr.setGatewayID("657552c83e33496e8d675edaae9acea9");
		sr.setProductClass("");
		ss.getSubscribeReqList().add(sr);
		
		sr = new SubscribeReq();
		sr.setSymbol("IC1903");
		sr.setExchange("CFFEX");
		sr.setRtSymbol("IC1903.CFFEX");
		sr.setGatewayID("657552c83e33496e8d675edaae9acea9");
		sr.setProductClass("");
		ss.getSubscribeReqList().add(sr);
		
		
		ss.setVersion("a62c17309a8d4565a87b35792bbc1763");
		
		ss.setxMin(15);
		
		System.out.println(JSON.toJSONString(ss));
	}
}
