# encoding: UTF-8

from xyz.redtorch.client.python.base.StrategyTemplate import StrategyTemplate
from xyz.redtorch.client.python.base.Config import *
from xyz.redtorch.client.python.base.RtObject import *
from xyz.redtorch.client.python.base.RtConstant import *
import uuid

class StrategyDemo(StrategyTemplate):

    def onEventTick(self, tick):
        log.info("TICK")
        log.info(tick)
        # print self.getContracts()
        # print self.getTicks()
        # print self.getTrades()
        # print self.getOrders()
        # print self.getAccounts()
        # print self.getPositions()
        # print self.getGateways()


    def onEventOrder(self, order):
        log.info("ORDER")
        log.info(order)

    def onEventTrade(self, trade):
        log.info("TRADE")
        log.info(trade)


def main():
    sd = StrategyDemo()
    # 订阅合约
    subscribeReq = SubscribeReq()
    subscribeReq.gatewayID = '384c128274064e34adca69ad6799760c'
    subscribeReq.exchange = EXCHANGE_CFFEX
    subscribeReq.symbol = 'T1903'

    sd.subscribe(subscribeReq)

    # 发送委托
    orderReq = OrderReq()
    orderReq.rtAccountID = '094948.CNY.384c128274064e34adca69ad6799760c'
    orderReq.exchange = EXCHANGE_CFFEX
    orderReq.symbol = 'T1903'
    orderReq.rtSymbol = 'T1903.CFFEX'
    orderReq.offset = OFFSET_OPEN
    orderReq.direction = DIRECTION_LONG
    orderReq.volume = 199
    orderReq.price = 95.3
    orderReq.priceType = PRICETYPE_LIMITPRICE
    orderReq.originalOrderID = str(uuid.uuid4())
    uuid.uuid4()

    sd.sendOrder(orderReq)

    sd.start()


if __name__ == '__main__':
    main()

