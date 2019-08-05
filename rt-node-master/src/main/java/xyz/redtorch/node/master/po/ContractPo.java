package xyz.redtorch.node.master.po;

public class ContractPo {
	private String contractId = "";
	private String shortName = "";
	private String fullName = "";
	private String thirdPartyId = "";
	private String unifiedSymbol = "";
	private String symbol = "";
	private int exchange = 0;
	private int productType = 0;
	private int currency = 0;
	private double multiplier = 0D;
	private double priceTick = 0D;
	private double longMarginRatio = 0D;
	private double shortMarginRatio = 0D;
	private boolean maxMarginSideAlgorithm = false;
	private String underlyingSymbol = "";
	private double strikePrice = 0D;
	private int optionType = 0;
	private double underlyingMultiplier = 0D;
	private String lastTradeDateOrContractMonth = "";
	private Integer maxMarketOrderVolume = 0;
	private Integer minMarketOrderVolume = 0;
	private Integer maxLimitOrderVolume = 0;
	private Integer minLimitOrderVolume = 0;

	public String getContractId() {
		return contractId;
	}

	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
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

	public int getProductType() {
		return productType;
	}

	public void setProductType(int productType) {
		this.productType = productType;
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

	public boolean isMaxMarginSideAlgorithm() {
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

	public int getOptionType() {
		return optionType;
	}

	public void setOptionType(int optionType) {
		this.optionType = optionType;
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

	public Integer getMaxMarketOrderVolume() {
		return maxMarketOrderVolume;
	}

	public void setMaxMarketOrderVolume(Integer maxMarketOrderVolume) {
		this.maxMarketOrderVolume = maxMarketOrderVolume;
	}

	public Integer getMinMarketOrderVolume() {
		return minMarketOrderVolume;
	}

	public void setMinMarketOrderVolume(Integer minMarketOrderVolume) {
		this.minMarketOrderVolume = minMarketOrderVolume;
	}

	public Integer getMaxLimitOrderVolume() {
		return maxLimitOrderVolume;
	}

	public void setMaxLimitOrderVolume(Integer maxLimitOrderVolume) {
		this.maxLimitOrderVolume = maxLimitOrderVolume;
	}

	public Integer getMinLimitOrderVolume() {
		return minLimitOrderVolume;
	}

	public void setMinLimitOrderVolume(Integer minLimitOrderVolume) {
		this.minLimitOrderVolume = minLimitOrderVolume;
	}

}
