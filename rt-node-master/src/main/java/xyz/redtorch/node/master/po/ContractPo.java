package xyz.redtorch.node.master.po;

public class ContractPo {

	String contractId;  // ID，通常是  <合约代码@交易所代码@产品类型@网关ID>
	String name;  // 简称
	String fullName;  // 全称
	String thirdPartyId ;  // 第三方ID
	String unifiedSymbol;  // 统一ID，通常是 <合约代码@交易所代码@产品类型>
	String symbol;  // 代码
	int exchange;  // 交易所
	int productClass;  // 产品类型
	int currency;  // 币种
	double multiplier;  // 合约乘数
	double priceTick;  // 最小变动价位
	double longMarginRatio;  // 多头保证金率
	double shortMarginRatio;  // 空头保证金率
	boolean  maxMarginSideAlgorithm;  // 最大单边保证金算法
	String underlyingSymbol;  // 基础商品代码
	double strikePrice;  // 执行价
	int optionsType;  // 期权类型
	double underlyingMultiplier;  // 合约基础商品乘数
	String lastTradeDateOrContractMonth;  // 最后交易日或合约月
	int maxMarketOrderVolume;  // 市价单最大下单量
	int minMarketOrderVolume;  // 市价单最小下单量
	int maxLimitOrderVolume;  // 限价单最大下单量
	int minLimitOrderVolume;  // 限价单最小下单量
	int combinationType; // 组合类型
	String gatewayId;  // 网关
	
	public String getContractId() {
		return contractId;
	}
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getThirdPartyId() {
		return thirdPartyId;
	}
	public void setThirdPartyId(String thirdPartyId) {
		this.thirdPartyId = thirdPartyId;
	}
	public String getUnifiedSymbol() {
		return unifiedSymbol;
	}
	public void setUnifiedSymbol(String unifiedSymbol) {
		this.unifiedSymbol = unifiedSymbol;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getExchange() {
		return exchange;
	}
	public void setExchange(int exchange) {
		this.exchange = exchange;
	}
	public int getProductClass() {
		return productClass;
	}
	public void setProductClass(int productClass) {
		this.productClass = productClass;
	}
	public int getCurrency() {
		return currency;
	}
	public void setCurrency(int currency) {
		this.currency = currency;
	}
	public double getMultiplier() {
		return multiplier;
	}
	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}
	public double getPriceTick() {
		return priceTick;
	}
	public void setPriceTick(double priceTick) {
		this.priceTick = priceTick;
	}
	public double getLongMarginRatio() {
		return longMarginRatio;
	}
	public void setLongMarginRatio(double longMarginRatio) {
		this.longMarginRatio = longMarginRatio;
	}
	public double getShortMarginRatio() {
		return shortMarginRatio;
	}
	public void setShortMarginRatio(double shortMarginRatio) {
		this.shortMarginRatio = shortMarginRatio;
	}
	public boolean getMaxMarginSideAlgorithm() {
		return maxMarginSideAlgorithm;
	}
	public void setMaxMarginSideAlgorithm(boolean maxMarginSideAlgorithm) {
		this.maxMarginSideAlgorithm = maxMarginSideAlgorithm;
	}
	public String getUnderlyingSymbol() {
		return underlyingSymbol;
	}
	public void setUnderlyingSymbol(String underlyingSymbol) {
		this.underlyingSymbol = underlyingSymbol;
	}
	public double getStrikePrice() {
		return strikePrice;
	}
	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}
	public int getOptionsType() {
		return optionsType;
	}
	public void setOptionsType(int optionsType) {
		this.optionsType = optionsType;
	}
	public double getUnderlyingMultiplier() {
		return underlyingMultiplier;
	}
	public void setUnderlyingMultiplier(double underlyingMultiplier) {
		this.underlyingMultiplier = underlyingMultiplier;
	}
	public String getLastTradeDateOrContractMonth() {
		return lastTradeDateOrContractMonth;
	}
	public void setLastTradeDateOrContractMonth(String lastTradeDateOrContractMonth) {
		this.lastTradeDateOrContractMonth = lastTradeDateOrContractMonth;
	}
	public int getMaxMarketOrderVolume() {
		return maxMarketOrderVolume;
	}
	public void setMaxMarketOrderVolume(int maxMarketOrderVolume) {
		this.maxMarketOrderVolume = maxMarketOrderVolume;
	}
	public int getMinMarketOrderVolume() {
		return minMarketOrderVolume;
	}
	public void setMinMarketOrderVolume(int minMarketOrderVolume) {
		this.minMarketOrderVolume = minMarketOrderVolume;
	}
	public int getMaxLimitOrderVolume() {
		return maxLimitOrderVolume;
	}
	public void setMaxLimitOrderVolume(int maxLimitOrderVolume) {
		this.maxLimitOrderVolume = maxLimitOrderVolume;
	}
	public int getMinLimitOrderVolume() {
		return minLimitOrderVolume;
	}
	public void setMinLimitOrderVolume(int minLimitOrderVolume) {
		this.minLimitOrderVolume = minLimitOrderVolume;
	}
	public int getCombinationType() {
		return combinationType;
	}
	public void setCombinationType(int combinationType) {
		this.combinationType = combinationType;
	}
	public String getGatewayId() {
		return gatewayId;
	}
	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}



}
