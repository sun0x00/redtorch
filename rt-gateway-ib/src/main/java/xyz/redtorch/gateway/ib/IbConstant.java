package xyz.redtorch.gateway.ib;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import xyz.redtorch.core.base.RtConstant;

/**
 * @author sun0x00@gmail.com
 */
public class IbConstant {
	public static Map<String,String> priceTypeMap = new HashMap<>();
	public static Map<String,String> priceTypeMapReverse = new HashMap<>();
	
	public static Map<String,String> directionMap = new HashMap<>();
	public static Map<String,String> directionMapReverse = new HashMap<>();
	
	public static Map<String,String> exchangeMap = new HashMap<>();
	public static Map<String,String> exchangeMapReverse = new HashMap<>();
	
	public static Map<String,String> productClassMap = new HashMap<>();
	public static Map<String,String> productClassMapReverse = new HashMap<>();
	

	public static Map<String,String> statusMap = new HashMap<>();
	public static Map<String,String> statusMapReverse = new HashMap<>();
	
	
	public static Map<String,String> optionTypeMap = new HashMap<>();
	public static Map<String,String> optionTypeMapReverse = new HashMap<>();
	

	public static Map<String,String> currencyMap = new HashMap<>();
	public static Map<String,String> currencyMapReverse = new HashMap<>();
	


	public static Map<Integer,String> tickFieldMap = new HashMap<>();
	
	public static Map<String,String> accountKeyMap = new HashMap<>();
	
	
	
	static {
		
		// 价格类型映射
		priceTypeMap.put(RtConstant.PRICETYPE_LIMITPRICE, "LMT");
		priceTypeMap.put(RtConstant.PRICETYPE_MARKETPRICE, "MKT");
		priceTypeMapReverse = priceTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		
		// 方向类型映射
		directionMap.put(RtConstant.DIRECTION_LONG, "BUY");
		directionMap.put(RtConstant.DIRECTION_SHORT, "SSHORT"); // SSHORT在IB系统中代表对股票的融券做空（而不是国内常见的卖出）
		directionMap.put(RtConstant.DIRECTION_SHORT, "SELL");     // 出于和国内的统一性考虑，这里选择把IB里的SELL印射为SHORT
		directionMapReverse = directionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		directionMapReverse.put("BOT", RtConstant.DIRECTION_LONG);
		directionMapReverse.put("SLD", RtConstant.DIRECTION_SHORT);		
		
		
		// 交易所映射
		exchangeMap.put(RtConstant.EXCHANGE_SMART, "SMART");
		exchangeMap.put(RtConstant.EXCHANGE_NYMEX, "NYMEX");
		exchangeMap.put(RtConstant.EXCHANGE_GLOBEX, "GLOBEX");
		exchangeMap.put(RtConstant.EXCHANGE_IDEALPRO, "IDEALPRO");
		exchangeMap.put(RtConstant.EXCHANGE_SEHK, "SEHK");
		exchangeMap.put(RtConstant.EXCHANGE_HKFE, "HKFE");
		exchangeMapReverse = exchangeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		
		// 委托状态映射
		statusMap.put(RtConstant.STATUS_NOTTRADED, "Submitted");
		statusMap.put(RtConstant.STATUS_ALLTRADED, "Filled");
		statusMap.put(RtConstant.STATUS_CANCELLED, "Cancelled");
		statusMapReverse = statusMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		statusMapReverse.put("PendingSubmit", RtConstant.STATUS_UNKNOWN); // TODO 此处应考虑拓展rt订单的状态类型
		statusMapReverse.put("PendingCancel", RtConstant.STATUS_UNKNOWN);
		statusMapReverse.put("PreSubmitted", RtConstant.STATUS_UNKNOWN);
		statusMapReverse.put("Inactive", RtConstant.STATUS_UNKNOWN);
		
		
		// 产品类型映射
		productClassMap.put(RtConstant.PRODUCT_EQUITY, "STK");
		productClassMap.put(RtConstant.PRODUCT_FUTURES, "FUT");
		productClassMap.put(RtConstant.PRODUCT_OPTION, "OPT");
		productClassMap.put(RtConstant.PRODUCT_FOREX, "CASH");
		productClassMap.put(RtConstant.PRODUCT_INDEX, "IND");
		productClassMap.put(RtConstant.PRODUCT_SPOT, "CMDTY");
		productClassMapReverse = productClassMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 期权类型映射
		
		optionTypeMap.put(RtConstant.OPTION_CALL, "CALL");
		optionTypeMap.put(RtConstant.OPTION_CALL, "PUT");
        optionTypeMapReverse = optionTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        currencyMap.put(RtConstant.CURRENCY_USD, "USD");
        currencyMap.put(RtConstant.CURRENCY_CNY, "CNY");
        currencyMap.put(RtConstant.CURRENCY_HKD, "HKD");
        currencyMapReverse = currencyMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        
		// Tick数据的Field和名称映射
        tickFieldMap.put(0, "bidVolume1");
        tickFieldMap.put(1, "bidPrice1");
        tickFieldMap.put(2, "askPrice1");
        tickFieldMap.put(3, "askVolume1");
        tickFieldMap.put(4, "lastPrice");
        tickFieldMap.put(5, "lastVolume");
        tickFieldMap.put(6, "highPrice");
        tickFieldMap.put(7, "lowPrice");
        tickFieldMap.put(8, "volume");
        tickFieldMap.put(9, "preClosePrice");
        tickFieldMap.put(14, "openPrice");
        tickFieldMap.put(22, "openInterest");
        

		// Account数据Key和名称的映射
        accountKeyMap.put("NetLiquidationByCurrency", "balance");
        accountKeyMap.put("NetLiquidation", "balance");
        accountKeyMap.put("UnrealizedPnL", "positionProfit");
        accountKeyMap.put("AvailableFunds", "available");
        accountKeyMap.put("MaintMarginReq", "margin");
		
	}
}
