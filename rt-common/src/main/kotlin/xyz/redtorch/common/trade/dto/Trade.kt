package xyz.redtorch.common.trade.dto

import xyz.redtorch.common.trade.enumeration.*

class Trade {
    var tradeId = "" // 成交ID,通常是<网关ID@定单编号@方向@成交编号>，加入方向是因为部分交易所发生违规自成交后,成交ID相同
    var contract = Contract() // 合约
    var adapterTradeId = "" // 适配器层成交ID
    var originOrderId = "" // 原始定单ID
    var orderId = "" // 定单ID,通常是<网关ID@定单ID>
    var adapterOrderId = "" // 适配器层定单ID
    var orderLocalId = "" // 本地报单编号
    var brokerOrderSeq = "" //经纪公司报单编号
    var orderSysId = "" // 报单编号
    var settlementId = "" // 结算编号
    var sequenceNo = "" // 序列号
    var accountId = "" // 账户ID
    var direction = DirectionEnum.Unknown // 方向
    var offsetFlag = OffsetFlagEnum.Unknown // 开平
    var hedgeFlag = HedgeFlagEnum.Unknown // 投机套保标识
    var price = 0.0 // 价格
    var volume = 0 // 数量
    var tradeType = TradeTypeEnum.Unknown // 成交类型
    var priceSource = PriceSourceEnum.Unknown // 成交价来源
    var tradingDay = "" // 交易日
    var tradeDate = "" // 成交日期
    var tradeTime = "" // 成交时间(HHmmssSSS)
    var tradeTimestamp = 0L // 成交时间戳
    var gatewayId = "" // 网关ID
}