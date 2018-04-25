package xyz.redtorch.trader.module.zeus.strategy;

import java.util.List;
import java.util.Map;

/**
 * @author sun0x00@gmail.com
 */
public class StrategySetting {
	private String name; // 策略名称
	private String id; // 策略ID
	private String className;
	private int xMin = 0; //x分钟Bar生成器参数
	private String tradingDay; //交易日
	private String preTradingDay; //前一个交易日
	private boolean lastTradingDay; //最后一个交易日
	
	private Map<String,String> paramMap; // 运行时不可变参数列表
	private Map<String,String> varMap; //运行时可变参数字典
	private List<String> syncVarList; // 数据库存储可变参数列表
	
	private List<gatewaySetting> gateways; //接口设置,回测,订阅相关
	private List<ContractSetting> contracts; //合约设置,回测,交易仓位相关
	
	/**
	 * 在使用前务必执行此方法,校正相关数据项
	 */
	public void fixSetting() {
		
		for(ContractSetting contractSetting:contracts) {
				String[] rtSymbolArr = contractSetting.getRtSymbol().split("\\.");
				
				contractSetting.setExchange(rtSymbolArr[1]);
				
				int tradeFixedPos = 0;
				for(TradeGatewaySetting tradeGatewaySetting: contractSetting.getTradeGateways()) {
					tradeFixedPos+=tradeGatewaySetting.getTradeFixedPos();
				}
				contractSetting.setTradeFixedPos(tradeFixedPos);
		}
	}
	
	public gatewaySetting getGatewaySetting(String gatewayID) {
		for(gatewaySetting gatewaySetting:gateways) {
			
			if(gatewaySetting.getGatewayID().equals(gatewayID)) {
				return gatewaySetting;
			}
		}
		return null;
	}
	
	public ContractSetting getContractSetting(String rtSymbol) {
		for(ContractSetting contractSetting:contracts) {
			if(contractSetting.getRtSymbol().equals(rtSymbol)) {
				return contractSetting;
			}
		}
		return null;
	}
	
	public ContractSetting getContractByAlias(String alias) {
		for(ContractSetting contractSetting:contracts) {
			if(contractSetting.getAlias().equals(alias)) {
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
	public List<gatewaySetting> getGateways() {
		return gateways;
	}
	public void setGateways(List<gatewaySetting> gateways) {
		this.gateways = gateways;
	}
	public List<ContractSetting> getContracts() {
		return contracts;
	}
	public void setContracts(List<ContractSetting> contracts) {
		this.contracts = contracts;
	}

	public static class gatewaySetting{
		private String gatewayID; // 接口ID
		private double backtestingCapital; // 回测账户资本设置
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
		public List<String> getSubscribeRtSymbols() {
			return subscribeRtSymbols;
		}
		public void setSubscribeRtSymbols(List<String> subscribeRtSymbols) {
			this.subscribeRtSymbols = subscribeRtSymbols;
		}
	}
	
	public static class ContractSetting{
		private String alias;  // 合约前缀
		private String rtSymbol; // 合约唯一标识
		private String symbol; // 合约唯一标识
		private String exchange;  //合约交易所
		private double backtestingSlippage; //回测滑点设置
		private double backtestingPriceTick; //回测合约最小变动价位
		private int size;  // 合约大小
		private int tradeFixedPos;  // 合约固定仓位大小
		private List<TradeGatewaySetting> tradeGateways;
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
		public List<TradeGatewaySetting> getTradeGateways() {
			return tradeGateways;
		}
		public void setTradeGateways(List<TradeGatewaySetting> tradeGateways) {
			this.tradeGateways = tradeGateways;
		}
	}
	
	public static class TradeGatewaySetting{
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
