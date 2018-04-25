package xyz.redtorch.trader.module.zeus.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author sun0x00@gmail.com
 */
public class StrategySetting {
	private String name; // 策略名称
	private String id; // 策略ID
	private int xMin = 0; //x分钟Bar生成器参数
	private String tradingDay; //交易日
	private String preTradingDay; //前一个交易日
	private boolean lastTradingDay; //最后一个交易日
	
	private Map<String,String> paramMap; // 运行时不可变参数列表
	private Map<String,String> varMap; //运行时可变参数字典
	private List<String> syncVarList; // 数据库存储可变参数列表
	
	private List<TradeGatewaySetting> gateways; //接口设置,回测,订阅相关
	private List<TradeContractSetting> contracts; //合约设置,回测,交易仓位相关
	
	/**
	 * 在使用前务必执行此方法,校正相关数据项
	 */
	public void fixSetting() {
		for(TradeGatewaySetting tradeGatewaySetting:gateways) {
			List<String> subscribeRtSymbols = new ArrayList<>();
			
			for(Entry<String,String> entry:tradeGatewaySetting.getSubscribeContractPrefixSuffixMap().entrySet()) {
				String prefix = entry.getKey();
				String suffix = entry.getValue();
				String exchange = tradeGatewaySetting.getSubscribeContractPrefixExchangeMap().get(prefix);
				if(exchange == null) {
					continue;
				}
				String rtSymbol = prefix + suffix + "." + exchange;
				subscribeRtSymbols.add(rtSymbol);
			}
			
			tradeGatewaySetting.setSubscribeRtSymbols(subscribeRtSymbols);
		}
		
		for(TradeContractSetting tradeContractSetting:contracts) {
				String rtSymbol = tradeContractSetting.getPrefix() + tradeContractSetting.getSuffix()+"." +tradeContractSetting.getExchange();
				tradeContractSetting.setRtSymbol(rtSymbol);
				
				int tradeFixedPos = 0;
				for(ContractTradeGatewaySetting contractTradeGatewaySetting: tradeContractSetting.getTradeGateways()) {
					tradeFixedPos+=contractTradeGatewaySetting.getTradeFixedPos();
				}
				tradeContractSetting.setTradeFixedPos(tradeFixedPos);
		}
	}
	
	public TradeGatewaySetting getGateway(String gatewayID) {
		for(TradeGatewaySetting tradeGatewaySetting:gateways) {
			
			if(tradeGatewaySetting.getGatewayID().equals(gatewayID)) {
				return tradeGatewaySetting;
			}
		}
		return null;
	}
	
	public TradeContractSetting getContract(String rtSymbol) {
		for(TradeContractSetting tradeContractSetting:contracts) {
			if(tradeContractSetting.getRtSymbol().equals(rtSymbol)) {
				return tradeContractSetting;
			}
		}
		return null;
	}
	
	public TradeContractSetting getContractByPrefix(String contractPrefix) {
		for(TradeContractSetting tradeContractSetting:contracts) {
			if(tradeContractSetting.getPrefix().equals(contractPrefix)) {
				return tradeContractSetting;
			}
		}
		return null;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public List<String> getSyncVarList() {
		return syncVarList;
	}

	public void setSyncVarList(List<String> syncVarList) {
		this.syncVarList = syncVarList;
	}

	public List<TradeGatewaySetting> getGateways() {
		return gateways;
	}

	public void setGateways(List<TradeGatewaySetting> gateways) {
		this.gateways = gateways;
	}

	public List<TradeContractSetting> getContracts() {
		return contracts;
	}

	public void setContracts(List<TradeContractSetting> contracts) {
		this.contracts = contracts;
	}


	public static class TradeGatewaySetting{
		private String gatewayID; // 接口ID
		private double backtestingCapital; // 回测账户资本设置
		private Map<String,String> subscribeContractPrefixSuffixMap; // 合约前后缀映射 
		private Map<String,String> subscribeContractPrefixExchangeMap; // 合约前缀交易所映射
		private List<String> subscribeRtSymbols; // 完整的合约List
		public String getGatewayID() {
			return gatewayID;
		}
		public void setGatewayID(String gatewayID) {
			this.gatewayID = gatewayID;
		}
		public double getBacktestingCapital() {
			return backtestingCapital;
		}
		public void setBacktestingCapital(double backtestingCapital) {
			this.backtestingCapital = backtestingCapital;
		}
		public Map<String, String> getSubscribeContractPrefixSuffixMap() {
			return subscribeContractPrefixSuffixMap;
		}
		public void setSubscribeContractPrefixSuffixMap(Map<String, String> subscribeContractPrefixSuffixMap) {
			this.subscribeContractPrefixSuffixMap = subscribeContractPrefixSuffixMap;
		}
		public Map<String, String> getSubscribeContractPrefixExchangeMap() {
			return subscribeContractPrefixExchangeMap;
		}
		public void setSubscribeContractPrefixExchangeMap(Map<String, String> subscribeContractPrefixExchangeMap) {
			this.subscribeContractPrefixExchangeMap = subscribeContractPrefixExchangeMap;
		}
		public List<String> getSubscribeRtSymbols() {
			return subscribeRtSymbols;
		}
		public void setSubscribeRtSymbols(List<String> subscribeRtSymbols) {
			this.subscribeRtSymbols = subscribeRtSymbols;
		}
	}
	
	public static class TradeContractSetting{
		private String prefix;  // 合约前缀
		private String suffix;  //合约后缀
		private String RtSymbol; // 合约唯一标识
		private String exchange;  //合约交易所
		private double backtestingSlippage; //回测滑点设置
		private double backtestingPriceTick; //回测合约最小变动价位
		private int size;  // 合约大小
		private int tradeFixedPos;  // 合约固定仓位大小
		private List<ContractTradeGatewaySetting> tradeGateways;
		private boolean useCommonContractSuffixSetting = true;  //是否使用通用后缀合成设置
		public String getPrefix() {
			return prefix;
		}
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
		public String getSuffix() {
			return suffix;
		}
		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}
		public String getRtSymbol() {
			return RtSymbol;
		}
		public void setRtSymbol(String rtSymbol) {
			RtSymbol = rtSymbol;
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
		public int getSize() {
			return size;
		}
		public void setSize(int size) {
			this.size = size;
		}
		public int getTradeFixedPos() {
			return tradeFixedPos;
		}
		public void setTradeFixedPos(int tradeFixedPos) {
			this.tradeFixedPos = tradeFixedPos;
		}
		public List<ContractTradeGatewaySetting> getTradeGateways() {
			return tradeGateways;
		}
		public void setTradeGateways(List<ContractTradeGatewaySetting> tradeGateways) {
			this.tradeGateways = tradeGateways;
		}
		public boolean isUseCommonContractSuffixSetting() {
			return useCommonContractSuffixSetting;
		}
		public void setUseCommonContractSuffixSetting(boolean useCommonContractSuffixSetting) {
			this.useCommonContractSuffixSetting = useCommonContractSuffixSetting;
		}

	}
	
	public static class ContractTradeGatewaySetting{
		private String gatewayID;
		private int tradeFixedPos; // 某一个合约的某一个接口的固定仓位值
		private double backtestingRate; // 此合约在回测中的佣金比例
		public String getGatewayID() {
			return gatewayID;
		}
		public void setGatewayID(String gatewayID) {
			this.gatewayID = gatewayID;
		}
		public int getTradeFixedPos() {
			return tradeFixedPos;
		}
		public void setTradeFixedPos(int tradeFixedPos) {
			this.tradeFixedPos = tradeFixedPos;
		}
		public double getBacktestingRate() {
			return backtestingRate;
		}
		public void setBacktestingRate(double backtestingRate) {
			this.backtestingRate = backtestingRate;
		}
	}
	
	
}
