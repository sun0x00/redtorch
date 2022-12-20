package xyz.redtorch.gateway.ctp.x64v6v6v9v

import xyz.redtorch.common.trade.enumeration.*
import xyz.redtorch.gateway.ctp.x64v6v6v9v.api.jctpv6v6v9x64apiConstants


object CtpConstant {
    // 价格类型映射
    val orderPriceTypeMap = HashMap<OrderPriceTypeEnum, Char>().apply {
        put(OrderPriceTypeEnum.AnyPrice, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_AnyPrice)
        put(OrderPriceTypeEnum.LimitPrice, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_LimitPrice)
        put(OrderPriceTypeEnum.BestPrice, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_BestPrice)
        put(OrderPriceTypeEnum.LastPrice, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_LastPrice)
        put(OrderPriceTypeEnum.LastPricePlusOneTicks, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_LastPricePlusOneTicks)
        put(OrderPriceTypeEnum.LastPricePlusTwoTicks, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_LastPricePlusTwoTicks)
        put(
            OrderPriceTypeEnum.LastPricePlusThreeTicks,
            jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_LastPricePlusThreeTicks
        )
        put(OrderPriceTypeEnum.AskPrice1, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_AskPrice1)
        put(OrderPriceTypeEnum.AskPrice1PlusOneTicks, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_AskPrice1PlusOneTicks)
        put(OrderPriceTypeEnum.AskPrice1PlusTwoTicks, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_AskPrice1PlusTwoTicks)
        put(
            OrderPriceTypeEnum.AskPrice1PlusThreeTicks,
            jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_AskPrice1PlusThreeTicks
        )
        put(OrderPriceTypeEnum.BidPrice1, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_BidPrice1)
        put(OrderPriceTypeEnum.BidPrice1PlusOneTicks, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_BidPrice1PlusOneTicks)
        put(OrderPriceTypeEnum.BidPrice1PlusTwoTicks, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_BidPrice1PlusTwoTicks)
        put(
            OrderPriceTypeEnum.BidPrice1PlusThreeTicks,
            jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_BidPrice1PlusThreeTicks
        )
        put(OrderPriceTypeEnum.FiveLevelPrice, jctpv6v6v9x64apiConstants.THOST_FTDC_OPT_FiveLevelPrice)
    }
    val orderPriceTypeMapReverse = orderPriceTypeMap.entries.associateBy({ it.value }) { it.key }

    // 方向类型映射
    val directionMap = HashMap<DirectionEnum, Char>().apply {
        put(DirectionEnum.Buy, jctpv6v6v9x64apiConstants.THOST_FTDC_D_Buy)
        put(DirectionEnum.Sell, jctpv6v6v9x64apiConstants.THOST_FTDC_D_Sell)
    }
    val directionMapReverse = directionMap.entries.associateBy({ it.value }) { it.key }

    // 开平类型映射
    val offsetFlagMap = HashMap<OffsetFlagEnum, Char>().apply {
        put(OffsetFlagEnum.Open, jctpv6v6v9x64apiConstants.THOST_FTDC_OF_Open)
        put(OffsetFlagEnum.Close, jctpv6v6v9x64apiConstants.THOST_FTDC_OF_Close)
        put(OffsetFlagEnum.ForceClose, jctpv6v6v9x64apiConstants.THOST_FTDC_OF_ForceClose)
        put(OffsetFlagEnum.CloseToday, jctpv6v6v9x64apiConstants.THOST_FTDC_OF_CloseToday)
        put(OffsetFlagEnum.CloseYesterday, jctpv6v6v9x64apiConstants.THOST_FTDC_OF_CloseYesterday)
        put(OffsetFlagEnum.ForceOff, jctpv6v6v9x64apiConstants.THOST_FTDC_OF_ForceOff)
        put(OffsetFlagEnum.LocalForceClose, jctpv6v6v9x64apiConstants.THOST_FTDC_OF_LocalForceClose)
    }
    val offsetMapReverse = offsetFlagMap.entries.associateBy({ it.value }) { it.key }

    // 交易所映射
    val exchangeMap = HashMap<ExchangeEnum, String>().apply {
        put(ExchangeEnum.CFFEX, "CFFEX")
        put(ExchangeEnum.SHFE, "SHFE")
        put(ExchangeEnum.CZCE, "CZCE")
        put(ExchangeEnum.DCE, "DCE")
        put(ExchangeEnum.SSE, "SSE")
        put(ExchangeEnum.SZSE, "SZSE")
        put(ExchangeEnum.INE, "INE")
        put(ExchangeEnum.GFEX, "GFEX")
        put(ExchangeEnum.Unknown, "")
    }
    val exchangeMapReverse = exchangeMap.entries.associateBy({ it.value }) { it.key }

    // 持仓类型映射
    val positionDirectionMap = HashMap<PositionDirectionEnum, Char>().apply {
        put(PositionDirectionEnum.Net, jctpv6v6v9x64apiConstants.THOST_FTDC_PD_Net)
        put(PositionDirectionEnum.Long, jctpv6v6v9x64apiConstants.THOST_FTDC_PD_Long)
        put(PositionDirectionEnum.Short, jctpv6v6v9x64apiConstants.THOST_FTDC_PD_Short)
    }
    val positionDirectionMapReverse = positionDirectionMap.entries.associateBy({ it.value }) { it.key }

    // 产品类型映射
    val productTypeMap = HashMap<ProductClassEnum, Char>().apply {
        // 产品类型映射
        put(ProductClassEnum.Futures, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_Futures)
        put(ProductClassEnum.Options, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_Options)
        put(ProductClassEnum.Combination, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_Combination)
        put(ProductClassEnum.Spot, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_Spot)
        put(ProductClassEnum.EFP, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_EFP)
        put(ProductClassEnum.SpotOption, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_SpotOption)
        put(ProductClassEnum.TAS, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_TAS)
        put(ProductClassEnum.MI, jctpv6v6v9x64apiConstants.THOST_FTDC_PC_MI)
    }
    val productTypeMapReverse = productTypeMap.entries.associateBy({ it.value }) { it.key }

    // 委托状态映射
    val statusMap = HashMap<OrderStatusEnum, Char>().apply {
        put(OrderStatusEnum.Unknown, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_Unknown)
        put(OrderStatusEnum.AllTraded, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_AllTraded)
        put(OrderStatusEnum.PartTradedQueueing, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_PartTradedQueueing)
        put(OrderStatusEnum.PartTradedNotQueueing, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_PartTradedNotQueueing)
        put(OrderStatusEnum.NoTradeQueueing, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_NoTradeQueueing)
        put(OrderStatusEnum.NoTradeNotQueueing, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_NoTradeNotQueueing)
        put(OrderStatusEnum.Canceled, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_Canceled)
        put(OrderStatusEnum.NotTouched, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_NotTouched)
        put(OrderStatusEnum.Touched, jctpv6v6v9x64apiConstants.THOST_FTDC_OST_Touched)
    }
    val statusMapReverse = statusMap.entries.associateBy({ it.value }) { it.key }

    // 期权类型映射
    val optionTypeMap = HashMap<OptionsTypeEnum, Char>().apply {
        put(OptionsTypeEnum.CallOptions, jctpv6v6v9x64apiConstants.THOST_FTDC_CP_CallOptions)
        put(OptionsTypeEnum.PutOptions, jctpv6v6v9x64apiConstants.THOST_FTDC_CP_PutOptions)
    }
    val optionTypeMapReverse = optionTypeMap.entries.associateBy({ it.value }) { it.key }

    val hedgeFlagMap = HashMap<HedgeFlagEnum, Char>().apply {
        put(HedgeFlagEnum.Arbitrage, jctpv6v6v9x64apiConstants.THOST_FTDC_HF_Arbitrage)
        put(HedgeFlagEnum.Hedge, jctpv6v6v9x64apiConstants.THOST_FTDC_HF_Hedge)
        put(HedgeFlagEnum.MarketMaker, jctpv6v6v9x64apiConstants.THOST_FTDC_HF_MarketMaker)
        put(HedgeFlagEnum.Speculation, jctpv6v6v9x64apiConstants.THOST_FTDC_HF_Speculation)
        put(HedgeFlagEnum.SpecHedge, jctpv6v6v9x64apiConstants.THOST_FTDC_HF_SpecHedge)
        put(HedgeFlagEnum.HedgeSpec, jctpv6v6v9x64apiConstants.THOST_FTDC_HF_HedgeSpec)
    }
    val hedgeFlagMapReverse = hedgeFlagMap.entries.associateBy({ it.value }) { it.key }

    val contingentConditionMap = HashMap<ContingentConditionEnum, Char>().apply {
        put(ContingentConditionEnum.Immediately, jctpv6v6v9x64apiConstants.THOST_FTDC_CC_Immediately)
        put(ContingentConditionEnum.Touch, jctpv6v6v9x64apiConstants.THOST_FTDC_CC_Touch)
        put(ContingentConditionEnum.TouchProfit, jctpv6v6v9x64apiConstants.THOST_FTDC_CC_TouchProfit)
        put(ContingentConditionEnum.ParkedOrder, jctpv6v6v9x64apiConstants.THOST_FTDC_CC_ParkedOrder)
        put(
            ContingentConditionEnum.LastPriceGreaterThanStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_LastPriceGreaterThanStopPrice
        )
        put(
            ContingentConditionEnum.LastPriceGreaterEqualStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_LastPriceGreaterEqualStopPrice
        )
        put(
            ContingentConditionEnum.LastPriceLesserThanStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_LastPriceLesserThanStopPrice
        )
        put(
            ContingentConditionEnum.LastPriceLesserEqualStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_LastPriceLesserEqualStopPrice
        )
        put(
            ContingentConditionEnum.AskPriceGreaterThanStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_AskPriceGreaterThanStopPrice
        )
        put(
            ContingentConditionEnum.AskPriceGreaterEqualStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_AskPriceGreaterEqualStopPrice
        )
        put(
            ContingentConditionEnum.AskPriceLesserThanStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_AskPriceLesserThanStopPrice
        )
        put(
            ContingentConditionEnum.AskPriceLesserEqualStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_AskPriceLesserEqualStopPrice
        )
        put(
            ContingentConditionEnum.BidPriceGreaterThanStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_BidPriceGreaterThanStopPrice
        )
        put(
            ContingentConditionEnum.BidPriceGreaterEqualStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_BidPriceGreaterEqualStopPrice
        )
        put(
            ContingentConditionEnum.BidPriceLesserThanStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_BidPriceLesserThanStopPrice
        )
        put(
            ContingentConditionEnum.BidPriceLesserEqualStopPrice,
            jctpv6v6v9x64apiConstants.THOST_FTDC_CC_BidPriceLesserEqualStopPrice
        )

    }
    val contingentConditionMapReverse = contingentConditionMap.entries.associateBy({ it.value }) { it.key }

    val forceCloseReasonMap = HashMap<ForceCloseReasonEnum, Char>().apply {
        put(ForceCloseReasonEnum.NotForceClose, jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_NotForceClose)
        put(ForceCloseReasonEnum.LackDeposit, jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_LackDeposit)
        put(
            ForceCloseReasonEnum.ClientOverPositionLimit,
            jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_ClientOverPositionLimit
        )
        put(
            ForceCloseReasonEnum.MemberOverPositionLimit,
            jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_MemberOverPositionLimit
        )
        put(ForceCloseReasonEnum.NotMultiple, jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_NotMultiple)
        put(ForceCloseReasonEnum.Violation, jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_Violation)
        put(ForceCloseReasonEnum.Other, jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_Other)
        put(ForceCloseReasonEnum.PersonDeliver, jctpv6v6v9x64apiConstants.THOST_FTDC_FCC_PersonDeliv)
    }
    val forceCloseReasonMapReverse = forceCloseReasonMap.entries.associateBy({ it.value }) { it.key }

    val timeConditionMap = HashMap<TimeConditionEnum, Char>().apply {
        put(TimeConditionEnum.IOC, jctpv6v6v9x64apiConstants.THOST_FTDC_TC_IOC)
        put(TimeConditionEnum.GFS, jctpv6v6v9x64apiConstants.THOST_FTDC_TC_GFS)
        put(TimeConditionEnum.GFD, jctpv6v6v9x64apiConstants.THOST_FTDC_TC_GFD)
        put(TimeConditionEnum.GTD, jctpv6v6v9x64apiConstants.THOST_FTDC_TC_GTD)
        put(TimeConditionEnum.GTC, jctpv6v6v9x64apiConstants.THOST_FTDC_TC_GTC)
        put(TimeConditionEnum.GFA, jctpv6v6v9x64apiConstants.THOST_FTDC_TC_GFA)
    }
    val timeConditionMapReverse = timeConditionMap.entries.associateBy({ it.value }) { it.key }

    val volumeConditionMap = HashMap<VolumeConditionEnum, Char>().apply {
        put(VolumeConditionEnum.AV, jctpv6v6v9x64apiConstants.THOST_FTDC_VC_AV)
        put(VolumeConditionEnum.MV, jctpv6v6v9x64apiConstants.THOST_FTDC_VC_MV)
        put(VolumeConditionEnum.CV, jctpv6v6v9x64apiConstants.THOST_FTDC_VC_CV)
    }
    val volumeConditionMapReverse = volumeConditionMap.entries.associateBy({ it.value }) { it.key }

    val priceSourceMap = HashMap<PriceSourceEnum, Char>().apply {
        put(PriceSourceEnum.Buy, jctpv6v6v9x64apiConstants.THOST_FTDC_PSRC_Buy)
        put(PriceSourceEnum.LastPrice, jctpv6v6v9x64apiConstants.THOST_FTDC_PSRC_LastPrice)
        put(PriceSourceEnum.Sell, jctpv6v6v9x64apiConstants.THOST_FTDC_PSRC_Sell)
        put(PriceSourceEnum.OTC, jctpv6v6v9x64apiConstants.THOST_FTDC_PSRC_OTC)
    }
    val priceSourceMapReverse = priceSourceMap.entries.associateBy({ it.value }) { it.key }

    val tradeTypeMap = HashMap<TradeTypeEnum, Char>().apply {
        put(TradeTypeEnum.CombinationDerived, jctpv6v6v9x64apiConstants.THOST_FTDC_TRDT_CombinationDerived)
        put(TradeTypeEnum.Common, jctpv6v6v9x64apiConstants.THOST_FTDC_TRDT_Common)
        put(TradeTypeEnum.EFPDerived, jctpv6v6v9x64apiConstants.THOST_FTDC_TRDT_EFPDerived)
        put(TradeTypeEnum.OptionsExecution, jctpv6v6v9x64apiConstants.THOST_FTDC_TRDT_OptionsExecution)
        put(TradeTypeEnum.OTC, jctpv6v6v9x64apiConstants.THOST_FTDC_TRDT_OTC)
        put(TradeTypeEnum.SplitCombination, jctpv6v6v9x64apiConstants.THOST_FTDC_TRDT_SplitCombination)
        put(TradeTypeEnum.BlockTrade, jctpv6v6v9x64apiConstants.THOST_FTDC_TRDT_BlockTrade)
    }
    val tradeTypeMapReverse = tradeTypeMap.entries.associateBy({ it.value }) { it.key }

    val orderSubmitStatusMap = HashMap<OrderSubmitStatusEnum, Char>().apply {
        put(OrderSubmitStatusEnum.Accepted, jctpv6v6v9x64apiConstants.THOST_FTDC_OSS_Accepted)
        put(OrderSubmitStatusEnum.CancelRejected, jctpv6v6v9x64apiConstants.THOST_FTDC_OSS_CancelRejected)
        put(OrderSubmitStatusEnum.CancelSubmitted, jctpv6v6v9x64apiConstants.THOST_FTDC_OSS_CancelSubmitted)
        put(OrderSubmitStatusEnum.InsertRejected, jctpv6v6v9x64apiConstants.THOST_FTDC_OSS_InsertRejected)
        put(OrderSubmitStatusEnum.InsertSubmitted, jctpv6v6v9x64apiConstants.THOST_FTDC_OSS_InsertSubmitted)
        put(OrderSubmitStatusEnum.ModifyRejected, jctpv6v6v9x64apiConstants.THOST_FTDC_OSS_ModifyRejected)
        put(OrderSubmitStatusEnum.ModifySubmitted, jctpv6v6v9x64apiConstants.THOST_FTDC_OSS_ModifySubmitted)
    }
    val orderSubmitStatusMapReverse = orderSubmitStatusMap.entries.associateBy({ it.value }) { it.key }


}