# encoding: UTF-8

from socketIO_client import SocketIO
from EventConstant import *
from Config import *
from RtConstant import *
from RtObject import *
import requests
import json

basetPath = "http://" + host + ":" + str(httpPort) + "/api"


class StrategyTemplate:
    def __init__(self):
        self.tickIDSet = set()
        self.originalOrderIDSet = set()

    def getData(self, uri):
        try:
            payload = {'token': token}
            response = requests.get(basetPath + uri, params=payload)

            responseJson = json.loads(response.content)

            if 200 <= response.status_code < 300 and responseJson['status'] == 'success':
                return responseJson['data']
            else:
                log.error(response)
                return []
        except Exception, e:
            log.error(e)
            return []

    def sendOrder(self, orderReq):

        self.originalOrderIDSet.add(orderReq.originalOrderID)

        try:
            postData = {'token': token}
            postData.update(orderReq.__dict__)
            response = requests.post(basetPath + '/core/sendOrder', json=postData)

            responseJson = json.loads(response.content)

            if 200 <= response.status_code < 300 and responseJson['status'] == 'success':
                return responseJson['data']
            else:
                log.error(response)
                return None
        except Exception, e:
            log.error(e)
            return None

    def buy(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_OPEN
        orderReq.direction = DIRECTION_LONG
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def sell(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_CLOSE
        orderReq.direction = DIRECTION_SHORT
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def sellTd(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_CLOSETODAY
        orderReq.direction = DIRECTION_SHORT
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def sellYd(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_CLOSEYESTERDAY
        orderReq.direction = DIRECTION_SHORT
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def sellShort(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_OPEN
        orderReq.direction = DIRECTION_SHORT
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def buyToCover(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_CLOSE
        orderReq.direction = DIRECTION_LONG
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def buyToCoverTd(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_CLOSETODAY
        orderReq.direction = DIRECTION_LONG
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def buyToCoverYd(self, symbol, volume, price, exchange, rtAccountID):
        orderReq = OrderReq()
        orderReq.rtAccountID = rtAccountID
        orderReq.exchange = exchange
        orderReq.volume = volume
        orderReq.price = price
        orderReq.symbol = symbol
        orderReq.rtSymbol = symbol+"."+exchange
        orderReq.offset = OFFSET_CLOSEYESTERDAY
        orderReq.direction = DIRECTION_LONG
        orderReq.priceType = PRICETYPE_LIMITPRICE
        self.sendOrder(orderReq)

    def cancelOrder(self, rtOrderID):
        try:
            response = requests.post(basetPath + '/core/cancelOrder', json={
                'rtOrderID': rtOrderID
            })

            responseJson = json.loads(response.content)

            if 200 <= response.status_code < 300 and responseJson['status'] == 'success':
                return responseJson['data']
            else:
                log.error(response)
                return None
        except Exception, e:
            log.error(e)
            return None

    def subscribe(self, subscribeReq):

        self.tickIDSet.add(subscribeReq.symbol + "." + subscribeReq.exchange + "." + subscribeReq.gatewayID)

        try:
            postData = {'token': token}
            postData.update(subscribeReq.__dict__)
            # headers = {'content-type': 'application/json'}
            response = requests.post(basetPath + '/core/subscribe', json=postData)

            responseJson = json.loads(response.content)

            if 200 <= response.status_code < 300 and responseJson['status'] == 'success':
                return responseJson['data']
            else:
                log.error(response)
                return None
        except Exception, e:
            log.error(e)
            return None

    def getContracts(self):
        return self.getData("/core/contracts")

    def getTicks(self):
        return self.getData("/core/ticks")

    def getTrades(self):
        return self.getData("/core/trades")

    def getOrders(self):
        return self.getData("/core/orders")

    def getAccounts(self):
        return self.getData("/core/accounts")

    def getPositions(self):
        return self.getData("/core/positions")

    def getGateways(self):
        return self.getData("/core/gateways")

    def onEventTick(self, tick):
        if debug:
            log.info(tick)

    def onEventTicksChanged(self, *args):
        if debug:
            log.info(args)

    def onEventTicks(self, *args):
        if debug:
            log.info(self, args)

    def onEventTrade(self, trade):
        if debug:
            log.info(trade)

    def onEventTrades(self, trades):
        if debug:
            log.info(trades)

    def onEventOrder(self, order):
        if debug:
            log.info(order)

    def onEventOrders(self, orders):
        if debug:
            log.info(orders)

    def onEventPosition(self, position):
        if debug:
            log.info(position)

    def onEventPositions(self, positions):
        if debug:
            log.info(positions)

    def onEventAccount(self, account):
        if debug:
            log.info(account)

    def onEventAccounts(self, accounts):
        if debug:
            log.info(accounts)

    def onEventContract(self, contract):
        if debug:
            log.info(contract)

    def onEventContracts(self, contracts):
        if debug:
            log.info(contracts)

    def onEventError(self, error):
        if debug:
            log.info(error)

    def onEventErrors(self, errors):
        if debug:
            log.info(self, errors)

    def onEventGateway(self, *args):
        if debug:
            log.info(args)

    def onEventGateways(self, *args):
        if debug:
            log.info(args)

    def onEventLog(self, log):
        if debug:
            log.info(log)

    def onEventLogs(self, logs):
        if debug:
            log.info(self, logs)

    def onEventThreadStop(*args):
        if debug:
            log.info(args)

    def start(self):
        io = SocketIO('http://'+host, port=socketIOPort, params={'token': token})  # create connection with params
        io.on(EVENT_TICK, self.onEventTick)
        io.on(EVENT_TICKS_CHANGED, self.onEventTicksChanged)
        io.on(EVENT_TICKS, self.onEventTicks)
        io.on(EVENT_TRADE, self.onEventTrade)
        io.on(EVENT_TRADES, self.onEventTrades)
        io.on(EVENT_ORDER, self.onEventOrder)
        io.on(EVENT_ORDERS, self.onEventOrders)
        io.on(EVENT_POSITION, self.onEventPosition)
        io.on(EVENT_POSITIONS, self.onEventPositions)
        io.on(EVENT_ACCOUNT, self.onEventAccount)
        io.on(EVENT_ACCOUNTS, self.onEventAccounts)
        io.on(EVENT_CONTRACT, self.onEventContract)
        io.on(EVENT_CONTRACTS, self.onEventContracts)
        io.on(EVENT_ERROR, self.onEventError)
        io.on(EVENT_ERRORS, self.onEventErrors)
        io.on(EVENT_GATEWAY, self.onEventGateway)
        io.on(EVENT_GATEWAYS, self.onEventGateways)
        io.on(EVENT_LOG, self.onEventLog)
        io.on(EVENT_LOGS, self.onEventLogs)
        io.on(EVENT_THREAD_STOP, self.onEventThreadStop)
        io.wait()
