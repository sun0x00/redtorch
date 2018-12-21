# encoding: UTF-8

from xyz.redtorch.client.python.base.StrategyTemplate import StrategyTemplate
from xyz.redtorch.client.python.base.Config import *
from xyz.redtorch.client.python.base.RtObject import *
from xyz.redtorch.client.python.base.RtConstant import *
import uuid


class StrategyDemo(StrategyTemplate):

    def __init__(self):
        StrategyTemplate.__init__(self)
        self.count = 1

    def onEventTick(self, tick):
        log.info("TICK")
        if tick['rtTickID'] in self.tickIDSet:
            log.info(tick)

            self.count += 1
            print self.count
            if self.count % 20 == 0:
                orderReq = OrderReq()
                orderReq.rtAccountID = '095076.CNY.614313ef70b442e985c935435aa99e0d'
                orderReq.exchange = EXCHANGE_CFFEX
                orderReq.symbol = 'T1903'
                orderReq.rtSymbol = 'T1903.CFFEX'
                orderReq.offset = OFFSET_OPEN
                orderReq.direction = DIRECTION_LONG
                orderReq.volume = 1
                orderReq.price = 96.3
                orderReq.priceType = PRICETYPE_LIMITPRICE
                orderReq.originalOrderID = str(uuid.uuid4())

                log.info(self.sendOrder(orderReq))

        # print self.getContracts()
        # print self.getTicks()
        # print self.getTrades()
        # print self.getOrders()
        # print self.getAccounts()
        # print self.getPositions()
        # print self.getGateways()

    def onEventOrder(self, order):
        log.info("ORDER")
        if 'originalOrderID' in order and order['originalOrderID'] in self.originalOrderIDSet:
            log.info(order)

    def onEventTrade(self, trade):
        log.info("TRADE")
        log.info(trade)
        # if 'originalOrderID' in order and trade['originalOrderID'] in self.originalOrderIDSet:
        #     log.info(trade)


def main():
    sd = StrategyDemo()
    # 订阅合约
    subscribeReq = SubscribeReq()
    subscribeReq.gatewayID = '614313ef70b442e985c935435aa99e0d'
    subscribeReq.exchange = EXCHANGE_CFFEX
    subscribeReq.symbol = 'T1903'

    sd.subscribe(subscribeReq)

    # # 发送委托
    # orderReq = OrderReq()
    # orderReq.rtAccountID = '094948.CNY.384c128274064e34adca69ad6799760c'
    # orderReq.exchange = EXCHANGE_CFFEX
    # orderReq.symbol = 'T1903'
    # orderReq.rtSymbol = 'T1903.CFFEX'
    # orderReq.offset = OFFSET_OPEN
    # orderReq.direction = DIRECTION_LONG
    # orderReq.volume = 199
    # orderReq.price = 95.3
    # orderReq.priceType = PRICETYPE_LIMITPRICE
    # orderReq.originalOrderID = str(uuid.uuid4())
    #
    # sd.sendOrder(orderReq)

    sd.start()


if __name__ == '__main__':
    main()

