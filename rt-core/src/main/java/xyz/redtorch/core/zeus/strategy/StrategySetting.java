package xyz.redtorch.core.zeus.strategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.redtorch.core.entity.SubscribeReq;

/**
 * @author sun0x00@gmail.com
 */
public class StrategySetting implements Serializable {

	private static final long serialVersionUID = 4037995985601670824L;

	private String strategyName; // 策略名称
	private String strategyID; // 策略ID
	private String className;
	private int xMin = 0; // x分钟Bar生成器参数
	private String tradingDay; // 交易日
	private String preTradingDay; // 前一个交易日
	private boolean lastTradingDay; // 最后一个交易日

	private Map<String, String> paramMap = new HashMap<>(); // 运行时不可变参数列表
	private Map<String, String> varMap = new HashMap<>(); // 运行时可变参数字典

	private List<SubscribeReq> subscribeReqList = new ArrayList<>(); // 完整的合约List
	private List<ContractSetting> contracts = new ArrayList<>(); // 合约设置,回测

	private String version;

	/**
	 * 在使用前务必执行此方法,校正相关数据项
	 */
	public void fixSetting() {
		// 不允许修改(通过setParamMap仍可修改，但不建议这么做)
		paramMap = Collections.unmodifiableMap(paramMap);
	}

	public ContractSetting getContractSetting(String rtSymbol) {
		for (ContractSetting contractSetting : contracts) {
			if (contractSetting.getRtSymbol().equals(rtSymbol)) {
				return contractSetting;
			}
		}
		return null;
	}

	public ContractSetting getContractByAlias(String alias) {
		for (ContractSetting contractSetting : contracts) {
			if (contractSetting.getAlias().equals(alias)) {
				return contractSetting;
			}
		}
		return null;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getStrategyName() {
		return strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public String getStrategyID() {
		return strategyID;
	}

	public void setStrategyID(String strategyID) {
		this.strategyID = strategyID;
	}

	public int getxMin() {
		return xMin;
	}

	public void setxMin(int xMin) {
		this.xMin = xMin;
	}

	public String getTradingDay() {
		return tradingDay;
	}

	public void setTradingDay(String tradingDay) {
		this.tradingDay = tradingDay;
	}

	public String getPreTradingDay() {
		return preTradingDay;
	}

	public void setPreTradingDay(String preTradingDay) {
		this.preTradingDay = preTradingDay;
	}

	public boolean isLastTradingDay() {
		return lastTradingDay;
	}

	public void setLastTradingDay(boolean lastTradingDay) {
		this.lastTradingDay = lastTradingDay;
	}

	public Map<String, String> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, String> paramMap) {
		this.paramMap = paramMap;
	}

	public Map<String, String> getVarMap() {
		return varMap;
	}

	public void setVarMap(Map<String, String> varMap) {
		this.varMap = varMap;
	}

	public List<ContractSetting> getContracts() {
		return contracts;
	}

	public void setContracts(List<ContractSetting> contracts) {
		this.contracts = contracts;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public List<SubscribeReq> getSubscribeReqList() {
		return subscribeReqList;
	}

	public void setSubscribeReqList(List<SubscribeReq> subscribeReqList) {
		this.subscribeReqList = subscribeReqList;
	}




	public static class ContractSetting implements Serializable {

		private static final long serialVersionUID = 4564498502584597194L;

		private String alias; // 合约前缀
		private String rtSymbol; // 合约唯一标识
		private String symbol; // 合约标识
		private String exchange; // 合约交易所
		private int size; // 合约大小
		private double backtestingSlippage; // 回测滑点设置
		private double backtestingPriceTick; // 回测合约最小变动价位
		private double backtestingRate;
		
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
		public String getRtSymbol() {
			return rtSymbol;
		}
		public void setRtSymbol(String rtSymbol) {
			this.rtSymbol = rtSymbol;
		}
		public String getSymbol() {
			return symbol;
		}
		public void setSymbol(String symbol) {
			this.symbol = symbol;
		}
		public String getExchange() {
			return exchange;
		}
		public void setExchange(String exchange) {
			this.exchange = exchange;
		}
		public double getBacktestingSlippage() {
			return backtestingSlippage;
		}
		public void setBacktestingSlippage(double backtestingSlippage) {
			this.backtestingSlippage = backtestingSlippage;
		}
		public double getBacktestingPriceTick() {
			return backtestingPriceTick;
		}
		public void setBacktestingPriceTick(double backtestingPriceTick) {
			this.backtestingPriceTick = backtestingPriceTick;
		}
		public double getBacktestingRate() {
			return backtestingRate;
		}
		public void setBacktestingRate(double backtestingRate) {
			this.backtestingRate = backtestingRate;
		}
		public int getSize() {
			return size;
		}
		public void setSize(int size) {
			this.size = size;
		}
	}
}
