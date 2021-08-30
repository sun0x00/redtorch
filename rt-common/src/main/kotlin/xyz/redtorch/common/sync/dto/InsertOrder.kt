package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.enumeration.*


class InsertOrder {
    var originOrderId: String = "" //  原始定单ID
    var accountCode: String = "" // 账户代码
    var currency: CurrencyEnum = CurrencyEnum.Unknown // 币种
    var contract: Contract? = null //  合约
    var gatewayId: String = "" // 网关ID
    var volume: Int = 0 // 数量
    var price = 0.0 // 价格
    var orderPriceType: OrderPriceTypeEnum = OrderPriceTypeEnum.Unknown // 价格类型
    var direction: DirectionEnum = DirectionEnum.Unknown // 方向
    var offsetFlag: OffsetFlagEnum = OffsetFlagEnum.Unknown // 组合开平标志
    var hedgeFlag: HedgeFlagEnum = HedgeFlagEnum.Unknown // 组合投机套保标志
    var timeCondition: TimeConditionEnum = TimeConditionEnum.Unknown // 时效
    var gtdDate: String = "" // GTD日期
    var volumeCondition: VolumeConditionEnum = VolumeConditionEnum.Unknown // 成交量类型
    var minVolume: Int = 0 // 最小成交量
    var contingentCondition: ContingentConditionEnum = ContingentConditionEnum.Unknown // 触发条件
    var stopPrice = 0.0 // 止损价
    var forceCloseReason: ForceCloseReasonEnum = ForceCloseReasonEnum.Unknown // 强平原因
    var autoSuspend: Int = 0 // 自动挂起标志
    var userForceClose: Int = 0 // 用户强评标志
    var swapOrder: Int = 0 // 互换单标志
}