package xyz.redtorch.common.trade.dto

import xyz.redtorch.common.trade.enumeration.CurrencyEnum

class Account {
    var accountId: String = ""// ID，通常是 <账户代码@币种@网关>
    var code: String = ""// 代码
    var name: String = ""// 名称
    var currency: CurrencyEnum = CurrencyEnum.Unknown // 币种
    var preBalance: Double = 0.0// 昨日权益
    var balance: Double = 0.0 // 权益
    var available: Double = 0.0 // 可用资金
    var commission: Double = 0.0 // 手续费
    var margin: Double = 0.0// 保证金占用
    var closeProfit: Double = 0.0// 平仓盈亏
    var positionProfit: Double = 0.0 // 持仓盈亏
    var deposit: Double = 0.0// 入金
    var withdraw: Double = 0.0// 出金
    var gatewayId: String = "" // 网关ID
    var localCreatedTimestamp: Long = 0 // 本地创建时间
}