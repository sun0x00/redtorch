package xyz.redtorch.gateway.ctp.x64v6v3v16t1v;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import xyz.redtorch.gateway.ctp.x64v6v3v16t1v.api.jctpv6v3v16t1x64apiConstants;
import xyz.redtorch.pb.CoreEnum.ContingentConditionEnum;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.ForceCloseReasonEnum;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreEnum.OptionsTypeEnum;
import xyz.redtorch.pb.CoreEnum.OrderPriceTypeEnum;
import xyz.redtorch.pb.CoreEnum.OrderStatusEnum;
import xyz.redtorch.pb.CoreEnum.PositionDirectionEnum;
import xyz.redtorch.pb.CoreEnum.PriceSourceEnum;
import xyz.redtorch.pb.CoreEnum.ProductClassEnum;
import xyz.redtorch.pb.CoreEnum.TimeConditionEnum;
import xyz.redtorch.pb.CoreEnum.TradeTypeEnum;
import xyz.redtorch.pb.CoreEnum.VolumeConditionEnum;

public class CtpConstant {

	public static Map<OrderPriceTypeEnum, Character> orderPriceTypeMap = new HashMap<>();
	public static Map<Character, OrderPriceTypeEnum> orderPriceTypeMapReverse = new HashMap<>();

	public static Map<DirectionEnum, Character> directionMap = new HashMap<>();
	public static Map<Character, DirectionEnum> directionMapReverse = new HashMap<>();

	public static Map<OffsetFlagEnum, Character> offsetFlagMap = new HashMap<>();
	public static Map<Character, OffsetFlagEnum> offsetMapReverse = new HashMap<>();

	public static Map<ExchangeEnum, String> exchangeMap = new HashMap<>();
	public static Map<String, ExchangeEnum> exchangeMapReverse = new HashMap<>();

	public static Map<PositionDirectionEnum, Character> posiDirectionMap = new HashMap<>();
	public static Map<Character, PositionDirectionEnum> posiDirectionMapReverse = new HashMap<>();

	public static Map<ProductClassEnum, Character> productTypeMap = new HashMap<>();
	public static Map<Character, ProductClassEnum> productTypeMapReverse = new HashMap<>();

	public static Map<OrderStatusEnum, Character> statusMap = new HashMap<>();
	public static Map<Character, OrderStatusEnum> statusMapReverse = new HashMap<>();

	public static Map<OptionsTypeEnum, Character> optionTypeMap = new HashMap<>();
	public static Map<Character, OptionsTypeEnum> optionTypeMapReverse = new HashMap<>();

	public static Map<HedgeFlagEnum, String> hedgeFlagMap = new HashMap<>();
	public static Map<String, HedgeFlagEnum> hedgeFlagMapReverse = new HashMap<>();

	public static Map<ContingentConditionEnum, Character> contingentConditionMap = new HashMap<>();
	public static Map<Character, ContingentConditionEnum> contingentConditionMapReverse = new HashMap<>();

	public static Map<ForceCloseReasonEnum, Character> forceCloseReasonMap = new HashMap<>();
	public static Map<Character, ForceCloseReasonEnum> forceCloseReasonMapReverse = new HashMap<>();

	public static Map<TimeConditionEnum, Character> timeConditionMap = new HashMap<>();
	public static Map<Character, TimeConditionEnum> timeConditionMapReverse = new HashMap<>();

	public static Map<VolumeConditionEnum, Character> volumeConditionMap = new HashMap<>();
	public static Map<Character, VolumeConditionEnum> volumeConditionMapReverse = new HashMap<>();

	public static Map<PriceSourceEnum, Character> priceSourceMap = new HashMap<>();
	public static Map<Character, PriceSourceEnum> priceSourceMapReverse = new HashMap<>();

	public static Map<TradeTypeEnum, Character> tradeTypeMap = new HashMap<>();
	public static Map<Character, TradeTypeEnum> tradeTypeMapReverse = new HashMap<>();

	static {

		// 价格类型映射
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AnyPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_AnyPrice);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LimitPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_LimitPrice);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BestPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_BestPrice);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_LastPrice);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPricePlusOneTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_LastPricePlusOneTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPricePlusTwoTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_LastPricePlusTwoTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPricePlusThreeTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_LastPricePlusThreeTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_AskPrice1);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1PlusOneTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_AskPrice1PlusOneTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1PlusTwoTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_AskPrice1PlusTwoTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1PlusThreeTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_AskPrice1PlusThreeTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_BidPrice1);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1PlusOneTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_BidPrice1PlusOneTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1PlusTwoTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_BidPrice1PlusTwoTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1PlusThreeTicks, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_BidPrice1PlusThreeTicks);
		orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_FiveLevelPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OPT_FiveLevelPrice);
		orderPriceTypeMapReverse = orderPriceTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 方向类型映射
		directionMap.put(DirectionEnum.D_Buy, jctpv6v3v16t1x64apiConstants.THOST_FTDC_D_Buy);
		directionMap.put(DirectionEnum.D_Sell, jctpv6v3v16t1x64apiConstants.THOST_FTDC_D_Sell);
		directionMapReverse = directionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 开平类型映射
		offsetFlagMap.put(OffsetFlagEnum.OF_Open, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_Open);
		offsetFlagMap.put(OffsetFlagEnum.OF_Close, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_Close);
		offsetFlagMap.put(OffsetFlagEnum.OF_ForceClose, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_ForceClose);
		offsetFlagMap.put(OffsetFlagEnum.OF_CloseToday, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_CloseToday);
		offsetFlagMap.put(OffsetFlagEnum.OF_CloseYesterday, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_CloseYesterday);
		offsetFlagMap.put(OffsetFlagEnum.OF_ForceOff, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_ForceOff);
		offsetFlagMap.put(OffsetFlagEnum.OF_LocalForceClose, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OF_LocalForceClose);
		offsetMapReverse = offsetFlagMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 交易所映射
		exchangeMap.put(ExchangeEnum.CFFEX, "CFFEX");
		exchangeMap.put(ExchangeEnum.SHFE, "SHFE");
		exchangeMap.put(ExchangeEnum.CZCE, "CZCE");
		exchangeMap.put(ExchangeEnum.DCE, "DCE");
		exchangeMap.put(ExchangeEnum.SSE, "SSE");
		exchangeMap.put(ExchangeEnum.SZSE, "SZSE");
		exchangeMap.put(ExchangeEnum.INE, "INE");
		exchangeMap.put(ExchangeEnum.UnknownExchange, "");
		exchangeMapReverse = exchangeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 持仓类型映射
		posiDirectionMap.put(PositionDirectionEnum.PD_Net, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PD_Net);
		posiDirectionMap.put(PositionDirectionEnum.PD_Long, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PD_Long);
		posiDirectionMap.put(PositionDirectionEnum.PD_Short, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PD_Short);
		posiDirectionMapReverse = posiDirectionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 产品类型映射
		productTypeMap.put(ProductClassEnum.FUTURES, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Futures);
		productTypeMap.put(ProductClassEnum.OPTION, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Options);
		productTypeMap.put(ProductClassEnum.COMBINATION, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Combination);
		productTypeMap.put(ProductClassEnum.SPOT, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_Spot);
		productTypeMap.put(ProductClassEnum.EFP, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_EFP);
		productTypeMap.put(ProductClassEnum.SPOTOPTION, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PC_SpotOption);
		productTypeMapReverse = productTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 委托状态映射
		statusMap.put(OrderStatusEnum.OS_Unknown, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_Unknown);
		statusMap.put(OrderStatusEnum.OS_AllTraded, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_AllTraded);
		statusMap.put(OrderStatusEnum.OS_PartTradedQueueing, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_PartTradedQueueing);
		statusMap.put(OrderStatusEnum.OS_PartTradedNotQueueing, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_PartTradedNotQueueing);
		statusMap.put(OrderStatusEnum.OS_NoTradeQueueing, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_NoTradeQueueing);
		statusMap.put(OrderStatusEnum.OS_NoTradeNotQueueing, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_NoTradeNotQueueing);
		statusMap.put(OrderStatusEnum.OS_Canceled, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_Canceled);
		statusMap.put(OrderStatusEnum.OS_NotTouched, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_NotTouched);
		statusMap.put(OrderStatusEnum.OS_Touched, jctpv6v3v16t1x64apiConstants.THOST_FTDC_OST_Touched);
		statusMapReverse = statusMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		// 期权类型映射
		optionTypeMap.put(OptionsTypeEnum.O_CallOptions, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CP_CallOptions);
		optionTypeMap.put(OptionsTypeEnum.O_PutOptions, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CP_PutOptions);
		optionTypeMapReverse = optionTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		hedgeFlagMap.put(HedgeFlagEnum.HF_Arbitrage, String.valueOf(jctpv6v3v16t1x64apiConstants.THOST_FTDC_HF_Arbitrage));
		hedgeFlagMap.put(HedgeFlagEnum.HF_Hedge, String.valueOf(jctpv6v3v16t1x64apiConstants.THOST_FTDC_HF_Hedge));
		hedgeFlagMap.put(HedgeFlagEnum.HF_MarketMaker, String.valueOf(jctpv6v3v16t1x64apiConstants.THOST_FTDC_HF_MarketMaker));
		hedgeFlagMap.put(HedgeFlagEnum.HF_Speculation, String.valueOf(jctpv6v3v16t1x64apiConstants.THOST_FTDC_HF_Speculation));
		hedgeFlagMap.put(HedgeFlagEnum.HF_SpecHedge, String.valueOf(jctpv6v3v16t1x64apiConstants.THOST_FTDC_HF_SpecHedge));
		hedgeFlagMap.put(HedgeFlagEnum.HF_HedgeSpec, String.valueOf(jctpv6v3v16t1x64apiConstants.THOST_FTDC_HF_HedgeSpec));

		hedgeFlagMapReverse = hedgeFlagMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		contingentConditionMap.put(ContingentConditionEnum.CC_Immediately, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_Immediately);
		contingentConditionMap.put(ContingentConditionEnum.CC_Touch, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_Touch);
		contingentConditionMap.put(ContingentConditionEnum.CC_TouchProfit, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_TouchProfit);
		contingentConditionMap.put(ContingentConditionEnum.CC_ParkedOrder, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_ParkedOrder);
		contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceGreaterThanStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_LastPriceGreaterThanStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceGreaterEqualStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_LastPriceGreaterEqualStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceLesserThanStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_LastPriceLesserThanStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceLesserEqualStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_LastPriceLesserEqualStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceGreaterThanStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_AskPriceGreaterThanStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceGreaterEqualStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_AskPriceGreaterEqualStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceLesserThanStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_AskPriceLesserThanStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceLesserEqualStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_AskPriceLesserEqualStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceGreaterThanStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_BidPriceGreaterThanStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceGreaterEqualStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_BidPriceGreaterEqualStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceLesserThanStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_BidPriceLesserThanStopPrice);
		contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceLesserEqualStopPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_CC_BidPriceLesserEqualStopPrice);
		contingentConditionMapReverse = contingentConditionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_NotForceClose, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_NotForceClose);
		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_LackDeposit, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_LackDeposit);
		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_ClientOverPositionLimit, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_ClientOverPositionLimit);
		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_MemberOverPositionLimit, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_MemberOverPositionLimit);
		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_NotMultiple, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_NotMultiple);
		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_Violation, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_Violation);
		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_Other, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_Other);
		forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_PersonDeliver, jctpv6v3v16t1x64apiConstants.THOST_FTDC_FCC_PersonDeliv);
		forceCloseReasonMapReverse = forceCloseReasonMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		timeConditionMap.put(TimeConditionEnum.TC_IOC, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TC_IOC);
		timeConditionMap.put(TimeConditionEnum.TC_GFS, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TC_GFS);
		timeConditionMap.put(TimeConditionEnum.TC_GFD, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TC_GFD);
		timeConditionMap.put(TimeConditionEnum.TC_GTD, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TC_GTD);
		timeConditionMap.put(TimeConditionEnum.TC_GTC, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TC_GTC);
		timeConditionMap.put(TimeConditionEnum.TC_GFA, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TC_GFA);
		timeConditionMapReverse = timeConditionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		volumeConditionMap.put(VolumeConditionEnum.VC_AV, jctpv6v3v16t1x64apiConstants.THOST_FTDC_VC_AV);
		volumeConditionMap.put(VolumeConditionEnum.VC_MV, jctpv6v3v16t1x64apiConstants.THOST_FTDC_VC_MV);
		volumeConditionMap.put(VolumeConditionEnum.VC_CV, jctpv6v3v16t1x64apiConstants.THOST_FTDC_VC_CV);
		volumeConditionMapReverse = volumeConditionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		priceSourceMap.put(PriceSourceEnum.PSRC_Buy, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PSRC_Buy);
		priceSourceMap.put(PriceSourceEnum.PSRC_LastPrice, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PSRC_LastPrice);
		priceSourceMap.put(PriceSourceEnum.PSRC_Sell, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PSRC_Sell);
		priceSourceMap.put(PriceSourceEnum.PSRC_OTC, jctpv6v3v16t1x64apiConstants.THOST_FTDC_PSRC_OTC);
		priceSourceMapReverse = priceSourceMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

		tradeTypeMap.put(TradeTypeEnum.TT_CombinationDerived, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TRDT_CombinationDerived);
		tradeTypeMap.put(TradeTypeEnum.TT_Common, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TRDT_Common);
		tradeTypeMap.put(TradeTypeEnum.TT_EFPDerived, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TRDT_EFPDerived);
		tradeTypeMap.put(TradeTypeEnum.TT_OptionsExecution, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TRDT_OptionsExecution);
		tradeTypeMap.put(TradeTypeEnum.TT_OTC, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TRDT_OTC);
		tradeTypeMap.put(TradeTypeEnum.TT_SplitCombination, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TRDT_SplitCombination);
		tradeTypeMap.put(TradeTypeEnum.TT_BlockTrade, jctpv6v3v16t1x64apiConstants.THOST_FTDC_TRDT_BlockTrade);
		tradeTypeMapReverse = tradeTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

	}
}
