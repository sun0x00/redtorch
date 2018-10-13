# encoding: UTF-8


class SubscribeReq(object):
    """订阅行情时传入的对象类"""

    def __init__(self):
        """Constructor"""
        self.symbol = None
        self.exchange = None
        self.productClass = None
        self.currency = None
        self.gatewayID = None


class OrderReq(object):
    """发单时传入的对象类"""

    def __init__(self):
        """Constructor"""
        self.gatewayID = None  # 网关
        self.gatewayDisplayName = None  # 网关
        self.accountID = None  # 账户代码
        self.rtAccountID = None  # 系统中的唯一账户代码

        # 代码编号相关
        self.symbol = None  # 代码
        self.exchange = None  # 交易所代码
        self.rtSymbol = None  # 系统中的唯一代码, 通常是 合约代码.交易所代码
        self.originalOrderID = None  # 自定义OrderID,用于异步操作

        # 报单相关
        self.price = None  # 报单价格
        self.volume = None  # 报单总数量
        self.direction = None  # 报单方向
        self.offset = None  # 报单开平仓
        self.priceType = None  # 报单成交数量

        originalOrderID = None  # 发起ID

        # IB预留
        self.productClass = None  # 合约类型
        self.currency = None  # 合约货币
        self.expiry = None  # 到期日
        self.strikePrice = None  # 行权价
        self.optionType = None  # 期权类型
        self.lastTradeDateOrContractMonth = None  # 合约月, IB专用
        self.multiplier = None  # 乘数, IB专用

