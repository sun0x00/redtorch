package xyz.redtorch.common.trade.dto

import xyz.redtorch.common.trade.enumeration.*

class Order {
    var contract = Contract() // 合约
    var originOrderId: String = ""  // 原始定单ID
    var orderId: String = ""  // 定单ID,通常是<网关ID@适配器定单ID>
    var adapterOrderId: String = ""  // 适配器层定单ID
    var accountId: String = ""  // 账户ID
    var orderLocalId: String = "" // 本地报单编号
    var brokerOrderSeq: String = "" // 经纪公司报单编号
    var orderSysId: String = "" // 报单编号
    var relativeOrderSysId: String = "" // 相关报单编号
    var sequenceNo: String = "" // 序号
    var direction: DirectionEnum = DirectionEnum.Unknown  // 方向
    var offsetFlag: OffsetFlagEnum = OffsetFlagEnum.Unknown  // 开平
    var hedgeFlag: HedgeFlagEnum = HedgeFlagEnum.Unknown // 投机套保标识
    var orderPriceType: OrderPriceTypeEnum = OrderPriceTypeEnum.Unknown // 定单价格类型
    var orderStatus: OrderStatusEnum = OrderStatusEnum.Unknown  // 状态
    var price: Double = 0.0 // 价格
    var totalVolume: Int = 0  // 数量
    var tradedVolume: Int = 0  // 已成交数量
    var timeCondition: TimeConditionEnum = TimeConditionEnum.Unknown  // 时效
    var gtdDate: String = "" // GTD日期
    var volumeCondition: VolumeConditionEnum = VolumeConditionEnum.Unknown // 成交量类型
    var minVolume: Int = 0 // 最小成交量
    var contingentCondition: ContingentConditionEnum = ContingentConditionEnum.Unknown // 触发条件
    var stopPrice: Double = 0.0 // 止损价
    var forceCloseReason: ForceCloseReasonEnum = ForceCloseReasonEnum.Unknown// 强平原因
    var autoSuspend: Int = 0 // 自动挂起标志
    var userForceClose: Int = 0 // 用户强平标志
    var swapOrder: Int = 0 // 互换单标志
    var tradingDay: String = ""  // 交易日
    var orderDate: String = ""  // 定单日期
    var orderTime: String = ""  // 定单时间
    var activeTime: String = ""  // 激活时间
    var suspendTime: String = ""  // 挂起时间
    var cancelTime: String = ""  // 撤销时间
    var updateTime: String = ""  // 最后修改时间
    var statusMsg: String = ""  // 状态信息
    var frontId: Int = 0  // 前置机编号(CTP/LTS)
    var sessionId: Int = 0  // 连接编号(CTP/LTS)
    var gatewayId: String = ""  // 网关ID
    var orderSubmitStatus: OrderSubmitStatusEnum = OrderSubmitStatusEnum.Unknown // 定单委托状态
}