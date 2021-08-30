package xyz.redtorch.common.trade.dto

import xyz.redtorch.common.trade.enumeration.HedgeFlagEnum
import xyz.redtorch.common.trade.enumeration.PositionDirectionEnum

class Position {
    var contract = Contract() // 合约
    var positionId = "" // 持仓在系统中的唯一代码,通常是<合约代码@交易所代码@方向@投机套保标志@账户@币种@网关>
    var accountId = "" // 账户ID
    var positionDirection = PositionDirectionEnum.Unknown // 持仓方向
    var position = 0 // 持仓量
    var frozen = 0 // 冻结数量
    var ydPosition = 0 // 昨持仓
    var ydFrozen = 0 // 冻结数量
    var tdPosition = 0 // 今持仓
    var tdFrozen = 0 // 冻结数量
    var lastPrice = 0.0 // 计算盈亏使用的行情最后价格
    var price = 0.0 // 持仓均价
    var priceDiff = 0.0 // 持仓价格差
    var openPrice = 0.0 // 开仓均价
    var openPriceDiff = 0.0 // 开仓价格差
    var positionProfit = 0.0 // 持仓盈亏
    var positionProfitRatio = 0.0 // 持仓盈亏率
    var openPositionProfit = 0.0 // 开仓盈亏
    var openPositionProfitRatio = 0.0 // 开仓盈亏率
    var useMargin = 0.0 // 占用的保证金
    var exchangeMargin = 0.0 // 交易所的保证金
    var contractValue = 0.0 // 最新合约价值
    var hedgeFlag = HedgeFlagEnum.Unknown // 投机套保标识
    var gatewayId = "" // 网关ID
    var localCreatedTimestamp: Long = 0 // 本地创建时间
}