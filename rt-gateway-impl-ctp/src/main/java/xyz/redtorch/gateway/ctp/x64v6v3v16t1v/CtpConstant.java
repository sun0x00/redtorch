package xyz.redtorch.gateway.ctp.x64v6v3v16t1v;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import xyz.redtorch.gateway.ctp.x64v6v3v16t1v.api.jctpv6v3v16t1x64apiConstants;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.OffsetEnum;
import xyz.redtorch.pb.CoreEnum.OptionTypeEnum;
import xyz.redtorch.pb.CoreEnum.OrderStatusEnum;
import xyz.redtorch.pb.CoreEnum.PriceTypeEnum;
import xyz.redtorch.pb.CoreEnum.ProductTypeEnum;


/**
 * @author sun0x00@gmail.com
 */
public class CtpConstant {
	
	public static Map<PriceTypeEnum,Character> priceTypeMap = new HashMap<>();
	public static Map<Character,PriceTypeEnum> priceTypeMapReverse = new HashMap<>();
	
	public static Map<DirectionEnum,Character> directionMap = new HashMap<>();
	public static Map<Character,DirectionEnum> directionMapReverse = new HashMap<>();
	
	public static Map<OffsetEnum,Character> offsetMap = new HashMap<>();
	public static Map<Character,OffsetEnum> offsetMapReverse = new HashMap<>();
	
	public static Map<ExchangeEnum,String> exchangeMap = new HashMap<>();
	public static Map<String,ExchangeEnum> exchangeMapReverse = new HashMap<>();
	

	public static Map<DirectionEnum,Character> posiDirectionMap = new HashMap<>();
	public static Map<Character,DirectionEnum> posiDirectionMapReverse = new HashMap<>();
	
	public static Map<ProductTypeEnum,Character> productTypeMap = new HashMap<>();
	public static Map<Character,ProductTypeEnum> productTypeMapReverse = new HashMap<>();
	

	public static Map<OrderStatusEnum,Character> statusMap = new HashMap<>();
	public static Map<Character,OrderStatusEnum> statusMapReverse = new HashMap<>();

	public static Map<OptionTypeEnum,Character> optionTypeMap = new HashMap<>();
	public static Map<Character,OptionTypeEnum> optionTypeMapReverse = new HashMap<>();
	
	static {
		
		// 价格类型映射
		priceTypeMap.put(PriceTypeEnum.LIMIT, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_LimitPrice);
		priceTypeMap.put(PriceTypeEnum.MARKET, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_AnyPrice);
		priceTypeMapReverse = priceTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		// 方向类型映射
		directionMap.put(DirectionEnum.LONG, jctpv6v3v16t1x64apiConstants.THOST_FTDC_D_Buy);
		directionMap.put(DirectionEnum.SHORT, jctpv6v3v16t1x64apiConstants.THOST_FTDC_D_Sell);
		directionMapReverse = directionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		// 开平类型映射
		offsetMap.put(OffsetEnum.OPEN, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_Open);
		offsetMap.put(OffsetEnum.CLOSE, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_Close);
		offsetMap.put(OffsetEnum.CLOSE_TODAY, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_CloseToday);
		offsetMap.put(OffsetEnum.CLOSE_YESTERDAY, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_CloseYesterday);
		offsetMapReverse = offsetMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		// 交易所映射
		exchangeMap.put(ExchangeEnum.CFFEX, "CFFEX");
		exchangeMap.put(ExchangeEnum.SHFE, "SHFE");
		exchangeMap.put(ExchangeEnum.CZCE, "CZCE");
		exchangeMap.put(ExchangeEnum.DCE, "DCE");
		exchangeMap.put(ExchangeEnum.SSE, "SSE");
		exchangeMap.put(ExchangeEnum.SZSE, "SZSE");
		exchangeMap.put(ExchangeEnum.INE, "INE");
		exchangeMap.put(ExchangeEnum.UNKNOWN_EXCHANGE, "");
		exchangeMapReverse = exchangeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		//持仓类型映射
		posiDirectionMap.put(DirectionEnum.NET, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PD_Net);
		posiDirectionMap.put(DirectionEnum.LONG, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PD_Long);
		posiDirectionMap.put(DirectionEnum.SHORT, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PD_Short);
		posiDirectionMapReverse = posiDirectionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		

		// 产品类型映射
		productTypeMap.put(ProductTypeEnum.FUTURES, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Futures);
		productTypeMap.put(ProductTypeEnum.OPTION, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Options);
		productTypeMap.put(ProductTypeEnum.COMBINATION, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Combination);
		productTypeMap.put(ProductTypeEnum.SPOT, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Spot);
		productTypeMap.put(ProductTypeEnum.EFP, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_EFP);
		productTypeMap.put(ProductTypeEnum.SPOT_OPTION, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_SpotOption);
		productTypeMapReverse = productTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		// 委托状态映射
		statusMap.put(OrderStatusEnum.ALL_TRADED, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_AllTraded);
		statusMap.put(OrderStatusEnum.PART_TRADED, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_PartTradedQueueing);
		statusMap.put(OrderStatusEnum.NOT_TRADED, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_NoTradeQueueing);
		statusMap.put(OrderStatusEnum.CANCELLED, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_Canceled);
		statusMap.put(OrderStatusEnum.UNKNOWN_ORDER_STATUS, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_Unknown);
		statusMapReverse = statusMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 期权类型映射
		optionTypeMap.put(OptionTypeEnum.CALL, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CP_CallOptions);
		optionTypeMap.put(OptionTypeEnum.PUT, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CP_PutOptions);
		optionTypeMapReverse = optionTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		
		
	}
}
