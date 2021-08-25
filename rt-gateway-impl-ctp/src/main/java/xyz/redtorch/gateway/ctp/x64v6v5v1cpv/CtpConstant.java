package xyz.redtorch.gateway.ctp.x64v6v5v1cpv;

import xyz.redtorch.gateway.ctp.x64v6v5v1cpv.api.jctpv6v5v1cpx64apiConstants;
import xyz.redtorch.pb.CoreEnum.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CtpConstant {

    public static Map<OrderPriceTypeEnum, Character> orderPriceTypeMap = new HashMap<>();
    public static Map<Character, OrderPriceTypeEnum> orderPriceTypeMapReverse;

    public static Map<DirectionEnum, Character> directionMap = new HashMap<>();
    public static Map<Character, DirectionEnum> directionMapReverse;

    public static Map<OffsetFlagEnum, Character> offsetFlagMap = new HashMap<>();
    public static Map<Character, OffsetFlagEnum> offsetMapReverse;

    public static Map<ExchangeEnum, String> exchangeMap = new HashMap<>();
    public static Map<String, ExchangeEnum> exchangeMapReverse;

    public static Map<PositionDirectionEnum, Character> positionDirectionMap = new HashMap<>();
    public static Map<Character, PositionDirectionEnum> positionDirectionMapReverse;

    public static Map<ProductClassEnum, Character> productTypeMap = new HashMap<>();
    public static Map<Character, ProductClassEnum> productTypeMapReverse;

    public static Map<OrderStatusEnum, Character> statusMap = new HashMap<>();
    public static Map<Character, OrderStatusEnum> statusMapReverse;

    public static Map<OptionsTypeEnum, Character> optionTypeMap = new HashMap<>();
    public static Map<Character, OptionsTypeEnum> optionTypeMapReverse;

    public static Map<HedgeFlagEnum, String> hedgeFlagMap = new HashMap<>();
    public static Map<String, HedgeFlagEnum> hedgeFlagMapReverse;

    public static Map<ContingentConditionEnum, Character> contingentConditionMap = new HashMap<>();
    public static Map<Character, ContingentConditionEnum> contingentConditionMapReverse;

    public static Map<ForceCloseReasonEnum, Character> forceCloseReasonMap = new HashMap<>();
    public static Map<Character, ForceCloseReasonEnum> forceCloseReasonMapReverse;

    public static Map<TimeConditionEnum, Character> timeConditionMap = new HashMap<>();
    public static Map<Character, TimeConditionEnum> timeConditionMapReverse;

    public static Map<VolumeConditionEnum, Character> volumeConditionMap = new HashMap<>();
    public static Map<Character, VolumeConditionEnum> volumeConditionMapReverse;

    public static Map<PriceSourceEnum, Character> priceSourceMap = new HashMap<>();
    public static Map<Character, PriceSourceEnum> priceSourceMapReverse;

    public static Map<TradeTypeEnum, Character> tradeTypeMap = new HashMap<>();
    public static Map<Character, TradeTypeEnum> tradeTypeMapReverse;

    public static Map<OrderSubmitStatusEnum, Character> orderSubmitStatusMap = new HashMap<>();
    public static Map<Character, OrderSubmitStatusEnum> orderSubmitStatusMapReverse;

    static {

        // 价格类型映射
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AnyPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_AnyPrice);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LimitPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_LimitPrice);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BestPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_BestPrice);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_LastPrice);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPricePlusOneTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_LastPricePlusOneTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPricePlusTwoTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_LastPricePlusTwoTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_LastPricePlusThreeTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_LastPricePlusThreeTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_AskPrice1);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1PlusOneTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_AskPrice1PlusOneTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1PlusTwoTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_AskPrice1PlusTwoTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_AskPrice1PlusThreeTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_AskPrice1PlusThreeTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_BidPrice1);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1PlusOneTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_BidPrice1PlusOneTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1PlusTwoTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_BidPrice1PlusTwoTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_BidPrice1PlusThreeTicks, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_BidPrice1PlusThreeTicks);
        orderPriceTypeMap.put(OrderPriceTypeEnum.OPT_FiveLevelPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OPT_FiveLevelPrice);
        orderPriceTypeMapReverse = orderPriceTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        // 方向类型映射
        directionMap.put(DirectionEnum.D_Buy, jctpv6v5v1cpx64apiConstants.THOST_FTDC_D_Buy);
        directionMap.put(DirectionEnum.D_Sell, jctpv6v5v1cpx64apiConstants.THOST_FTDC_D_Sell);
        directionMapReverse = directionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        // 开平类型映射
        offsetFlagMap.put(OffsetFlagEnum.OF_Open, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OF_Open);
        offsetFlagMap.put(OffsetFlagEnum.OF_Close, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OF_Close);
        offsetFlagMap.put(OffsetFlagEnum.OF_ForceClose, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OF_ForceClose);
        offsetFlagMap.put(OffsetFlagEnum.OF_CloseToday, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OF_CloseToday);
        offsetFlagMap.put(OffsetFlagEnum.OF_CloseYesterday, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OF_CloseYesterday);
        offsetFlagMap.put(OffsetFlagEnum.OF_ForceOff, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OF_ForceOff);
        offsetFlagMap.put(OffsetFlagEnum.OF_LocalForceClose, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OF_LocalForceClose);
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
        positionDirectionMap.put(PositionDirectionEnum.PD_Net, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PD_Net);
        positionDirectionMap.put(PositionDirectionEnum.PD_Long, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PD_Long);
        positionDirectionMap.put(PositionDirectionEnum.PD_Short, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PD_Short);
        positionDirectionMapReverse = positionDirectionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        // 产品类型映射
        productTypeMap.put(ProductClassEnum.FUTURES, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_Futures);
        productTypeMap.put(ProductClassEnum.OPTION, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_Options);
        productTypeMap.put(ProductClassEnum.COMBINATION, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_Combination);
        productTypeMap.put(ProductClassEnum.SPOT, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_Spot);
        productTypeMap.put(ProductClassEnum.EFP, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_EFP);
        productTypeMap.put(ProductClassEnum.SPOTOPTION, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_SpotOption);
        productTypeMap.put(ProductClassEnum.TAS, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_TAS);
        productTypeMap.put(ProductClassEnum.MI, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PC_MI);
        productTypeMapReverse = productTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        // 委托状态映射
        statusMap.put(OrderStatusEnum.OS_Unknown, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_Unknown);
        statusMap.put(OrderStatusEnum.OS_AllTraded, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_AllTraded);
        statusMap.put(OrderStatusEnum.OS_PartTradedQueueing, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_PartTradedQueueing);
        statusMap.put(OrderStatusEnum.OS_PartTradedNotQueueing, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_PartTradedNotQueueing);
        statusMap.put(OrderStatusEnum.OS_NoTradeQueueing, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_NoTradeQueueing);
        statusMap.put(OrderStatusEnum.OS_NoTradeNotQueueing, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_NoTradeNotQueueing);
        statusMap.put(OrderStatusEnum.OS_Canceled, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_Canceled);
        statusMap.put(OrderStatusEnum.OS_NotTouched, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_NotTouched);
        statusMap.put(OrderStatusEnum.OS_Touched, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OST_Touched);
        statusMapReverse = statusMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        // 期权类型映射
        optionTypeMap.put(OptionsTypeEnum.O_CallOptions, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CP_CallOptions);
        optionTypeMap.put(OptionsTypeEnum.O_PutOptions, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CP_PutOptions);
        optionTypeMapReverse = optionTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        hedgeFlagMap.put(HedgeFlagEnum.HF_Arbitrage, String.valueOf(jctpv6v5v1cpx64apiConstants.THOST_FTDC_HF_Arbitrage));
        hedgeFlagMap.put(HedgeFlagEnum.HF_Hedge, String.valueOf(jctpv6v5v1cpx64apiConstants.THOST_FTDC_HF_Hedge));
        hedgeFlagMap.put(HedgeFlagEnum.HF_MarketMaker, String.valueOf(jctpv6v5v1cpx64apiConstants.THOST_FTDC_HF_MarketMaker));
        hedgeFlagMap.put(HedgeFlagEnum.HF_Speculation, String.valueOf(jctpv6v5v1cpx64apiConstants.THOST_FTDC_HF_Speculation));
        hedgeFlagMap.put(HedgeFlagEnum.HF_SpecHedge, String.valueOf(jctpv6v5v1cpx64apiConstants.THOST_FTDC_HF_SpecHedge));
        hedgeFlagMap.put(HedgeFlagEnum.HF_HedgeSpec, String.valueOf(jctpv6v5v1cpx64apiConstants.THOST_FTDC_HF_HedgeSpec));

        hedgeFlagMapReverse = hedgeFlagMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        contingentConditionMap.put(ContingentConditionEnum.CC_Immediately, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_Immediately);
        contingentConditionMap.put(ContingentConditionEnum.CC_Touch, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_Touch);
        contingentConditionMap.put(ContingentConditionEnum.CC_TouchProfit, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_TouchProfit);
        contingentConditionMap.put(ContingentConditionEnum.CC_ParkedOrder, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_ParkedOrder);
        contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceGreaterThanStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_LastPriceGreaterThanStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceGreaterEqualStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_LastPriceGreaterEqualStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceLesserThanStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_LastPriceLesserThanStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_LastPriceLesserEqualStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_LastPriceLesserEqualStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceGreaterThanStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_AskPriceGreaterThanStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceGreaterEqualStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_AskPriceGreaterEqualStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceLesserThanStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_AskPriceLesserThanStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_AskPriceLesserEqualStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_AskPriceLesserEqualStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceGreaterThanStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_BidPriceGreaterThanStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceGreaterEqualStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_BidPriceGreaterEqualStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceLesserThanStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_BidPriceLesserThanStopPrice);
        contingentConditionMap.put(ContingentConditionEnum.CC_BidPriceLesserEqualStopPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_CC_BidPriceLesserEqualStopPrice);
        contingentConditionMapReverse = contingentConditionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_NotForceClose, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_NotForceClose);
        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_LackDeposit, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_LackDeposit);
        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_ClientOverPositionLimit, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_ClientOverPositionLimit);
        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_MemberOverPositionLimit, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_MemberOverPositionLimit);
        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_NotMultiple, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_NotMultiple);
        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_Violation, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_Violation);
        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_Other, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_Other);
        forceCloseReasonMap.put(ForceCloseReasonEnum.FCR_PersonDeliver, jctpv6v5v1cpx64apiConstants.THOST_FTDC_FCC_PersonDeliv);
        forceCloseReasonMapReverse = forceCloseReasonMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        timeConditionMap.put(TimeConditionEnum.TC_IOC, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TC_IOC);
        timeConditionMap.put(TimeConditionEnum.TC_GFS, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TC_GFS);
        timeConditionMap.put(TimeConditionEnum.TC_GFD, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TC_GFD);
        timeConditionMap.put(TimeConditionEnum.TC_GTD, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TC_GTD);
        timeConditionMap.put(TimeConditionEnum.TC_GTC, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TC_GTC);
        timeConditionMap.put(TimeConditionEnum.TC_GFA, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TC_GFA);
        timeConditionMapReverse = timeConditionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        volumeConditionMap.put(VolumeConditionEnum.VC_AV, jctpv6v5v1cpx64apiConstants.THOST_FTDC_VC_AV);
        volumeConditionMap.put(VolumeConditionEnum.VC_MV, jctpv6v5v1cpx64apiConstants.THOST_FTDC_VC_MV);
        volumeConditionMap.put(VolumeConditionEnum.VC_CV, jctpv6v5v1cpx64apiConstants.THOST_FTDC_VC_CV);
        volumeConditionMapReverse = volumeConditionMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        priceSourceMap.put(PriceSourceEnum.PSRC_Buy, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PSRC_Buy);
        priceSourceMap.put(PriceSourceEnum.PSRC_LastPrice, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PSRC_LastPrice);
        priceSourceMap.put(PriceSourceEnum.PSRC_Sell, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PSRC_Sell);
        priceSourceMap.put(PriceSourceEnum.PSRC_OTC, jctpv6v5v1cpx64apiConstants.THOST_FTDC_PSRC_OTC);
        priceSourceMapReverse = priceSourceMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        tradeTypeMap.put(TradeTypeEnum.TT_CombinationDerived, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TRDT_CombinationDerived);
        tradeTypeMap.put(TradeTypeEnum.TT_Common, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TRDT_Common);
        tradeTypeMap.put(TradeTypeEnum.TT_EFPDerived, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TRDT_EFPDerived);
        tradeTypeMap.put(TradeTypeEnum.TT_OptionsExecution, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TRDT_OptionsExecution);
        tradeTypeMap.put(TradeTypeEnum.TT_OTC, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TRDT_OTC);
        tradeTypeMap.put(TradeTypeEnum.TT_SplitCombination, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TRDT_SplitCombination);
        tradeTypeMap.put(TradeTypeEnum.TT_BlockTrade, jctpv6v5v1cpx64apiConstants.THOST_FTDC_TRDT_BlockTrade);
        tradeTypeMapReverse = tradeTypeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        orderSubmitStatusMap.put(OrderSubmitStatusEnum.OSS_Accepted, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OSS_Accepted);
        orderSubmitStatusMap.put(OrderSubmitStatusEnum.OSS_CancelRejected, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OSS_CancelRejected);
        orderSubmitStatusMap.put(OrderSubmitStatusEnum.OSS_CancelSubmitted, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OSS_CancelSubmitted);
        orderSubmitStatusMap.put(OrderSubmitStatusEnum.OSS_InsertRejected, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OSS_InsertRejected);
        orderSubmitStatusMap.put(OrderSubmitStatusEnum.OSS_InsertSubmitted, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OSS_InsertSubmitted);
        orderSubmitStatusMap.put(OrderSubmitStatusEnum.OSS_ModifyRejected, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OSS_ModifyRejected);
        orderSubmitStatusMap.put(OrderSubmitStatusEnum.OSS_ModifySubmitted, jctpv6v5v1cpx64apiConstants.THOST_FTDC_OSS_ModifySubmitted);
        orderSubmitStatusMapReverse = orderSubmitStatusMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    }
}
