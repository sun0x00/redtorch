package xyz.redtorch.common.trade.dto

class Tick {
    var contract = Contract() // 合约
    var gatewayId = "" // 网关ID
    var tradingDay = 0 // 交易日
    var actionDay = 0 // 业务发生日
    var actionTime = 0 // 时间(HHmmssSSS)
    var actionTimestamp = 0L // 时间戳
    var lastPrice = 0.0 // 最新成交价
    var avgPrice = 0.0 // 均价
    var volumeDelta = 0L // 成交量变化
    var volume = 0L // 总成交量
    var turnover = 0.0 // 成交总额
    var turnoverDelta = 0.0 // 成交总额变化
    var openInterest = 0.0 // 持仓量
    var openInterestDelta = 0.0 // 持仓量变化
    var preOpenInterest = 0.0 // 昨持仓
    var preClosePrice = 0.0 // 前收盘价
    var settlePrice = 0.0 // 结算价
    var preSettlePrice = 0.0 // 昨结算价
    var openPrice = 0.0 // 开盘价
    var highPrice = 0.0 // 最高价
    var lowPrice = 0.0 // 最低价
    var upperLimit = 0.0 // 涨停价
    var lowerLimit = 0.0 // 跌停价

    var bidPriceMap = HashMap<String, Double>() // 买价
    var askPriceMap = HashMap<String, Double>() // 卖价
    var bidVolumeMap = HashMap<String, Int>() // 买量
    var askVolumeMap = HashMap<String, Int>() // 卖量
}