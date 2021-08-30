package xyz.redtorch.common.trade.dto

import xyz.redtorch.common.trade.enumeration.*

class Contract {
    var uniformSymbol = "" // 统一ID，通常是 <合约代码@交易所代码>
    var symbol = "" // 代码
    var exchange = ExchangeEnum.Unknown // 交易所
    var productClass = ProductClassEnum.Unknown // 产品类型
    var name = "" // 简称
    var fullName = "" // 全称
    var thirdPartyId = "" // 第三方ID
    var currency = CurrencyEnum.Unknown // 币种
    var multiplier = 0.0 // 合约乘数
    var priceTick = 0.0 // 最小变动价位
    var longMarginRatio = 0.0 // 多头保证金率
    var shortMarginRatio = 0.0 // 空头保证金率
    var maxMarginSideAlgorithm = false // 最大单边保证金算法
    var underlyingSymbol = "" // 基础商品代码
    var strikePrice = 0.0 // 执行价
    var optionsType = OptionsTypeEnum.Unknown // 期权类型
    var underlyingMultiplier = 0.0 // 合约基础商品乘数
    var lastTradeDateOrContractMonth = "" // 最后交易日或合约月
    var maxMarketOrderVolume = 0 // 市价单最大下单量
    var minMarketOrderVolume = 0 // 市价单最小下单量
    var maxLimitOrderVolume = 0 // 限价单最大下单量
    var minLimitOrderVolume = 0 // 限价单最小下单量
    var combinationType = CombinationTypeEnum.Unknown // 组合类型
}