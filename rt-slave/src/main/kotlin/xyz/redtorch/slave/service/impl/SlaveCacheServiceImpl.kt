package xyz.redtorch.slave.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.redtorch.common.event.Event
import xyz.redtorch.common.event.EventObserver
import xyz.redtorch.common.event.EventTypeEnum
import xyz.redtorch.common.event.service.EventService
import xyz.redtorch.common.trade.dto.*
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.slave.service.SlaveCacheService
import xyz.redtorch.slave.service.SlaveSyncService
import java.util.concurrent.locks.ReentrantLock

@Service
class SlaveCacheServiceImpl : SlaveCacheService, EventObserver, InitializingBean {

    private val logger = LoggerFactory.getLogger(SlaveCacheServiceImpl::class.java)

    private var accountMap: MutableMap<String, Account> = HashMap()
    private val accountMapLock = ReentrantLock()
    private var positionMap: MutableMap<String, Position> = HashMap()
    private val positionMapLock = ReentrantLock()
    private var tradeMap: MutableMap<String, Trade> = HashMap()
    private val tradeMapLock = ReentrantLock()
    private var orderMap: MutableMap<String, Order> = HashMap()
    private val orderMapLock = ReentrantLock()
    private var tickMap: MutableMap<String, Tick> = HashMap()
    private val tickMapLock = ReentrantLock()
    private val contractMap: MutableMap<String, Contract> = HashMap()
    private val contractMapLock = ReentrantLock()

    @Autowired
    private lateinit var slaveSyncService: SlaveSyncService

    @Autowired
    private lateinit var eventService: EventService

    override fun afterPropertiesSet() {
        eventService.register(this)
    }

    override fun filterTickMapByUniformSymbolSet(uniformSymbolSet: Set<String>) {
        tickMapLock.lock()
        try {
            tickMap = tickMap.filterKeys { uniformSymbolSet.contains(it) }.toMutableMap()
        } finally {
            tickMapLock.unlock()
        }
    }

    override fun clearAllCachesByGatewayId(gatewayId: String) {
        accountMapLock.lock()
        try {
            accountMap = accountMap.filterValues { it.gatewayId != gatewayId }.toMutableMap()
        } finally {
            accountMapLock.unlock()
        }

        positionMapLock.lock()
        try {
            positionMap = positionMap.filterValues { it.gatewayId != gatewayId }.toMutableMap()
        } finally {
            positionMapLock.unlock()
        }

        tradeMapLock.lock()
        try {
            tradeMap = tradeMap.filterValues { it.gatewayId != gatewayId }.toMutableMap()
        } finally {
            tradeMapLock.unlock()
        }

        orderMapLock.lock()
        try {
            orderMap = orderMap.filterValues { it.gatewayId != gatewayId }.toMutableMap()
        } finally {
            orderMapLock.unlock()
        }
    }

    override fun clearContractCaches() {
        contractMapLock.lock()
        try {
            contractMap.clear()
        } finally {
            contractMapLock.unlock()
        }
    }

    override fun getOrderByOrderId(orderId: String): Order? {
        orderMapLock.lock()
        try {
            return orderMap[orderId]
        } finally {
            orderMapLock.unlock()
        }
    }

    override fun syncQuoteMirror() {
        tickMapLock.lock()
        try {
            slaveSyncService.updateQuoteMirror(tickMap)
        } finally {
            tickMapLock.unlock()
        }
    }

    override fun syncTransactionMirror() {
        orderMapLock.lock()
        tradeMapLock.lock()
        try {
            slaveSyncService.updateTransactionMirror(orderMap, tradeMap)
        } finally {
            orderMapLock.unlock()
            tradeMapLock.unlock()
        }

    }

    override fun syncBaseMirror() {
        contractMapLock.lock()
        try {
            slaveSyncService.updateBaseMirror(contractMap)
        } finally {
            contractMapLock.unlock()
        }
    }

    override fun syncPortfolioMirror() {
        accountMapLock.lock()
        positionMapLock.lock()
        try {
            slaveSyncService.updatePortfolioMirror(accountMap, positionMap)
        } finally {
            accountMapLock.unlock()
            positionMapLock.unlock()
        }
    }

    override fun handleTransactionEvent(event: Event) {
        when (event.eventType) {
            EventTypeEnum.Order -> {
                val order = event.order!!
                orderMapLock.lock()
                try {
                    orderMap[order.orderId] = order
                } finally {
                    orderMapLock.unlock()
                }
                slaveSyncService.sendOrder(order)
            }
            EventTypeEnum.Trade -> {
                val trade = event.trade!!
                tradeMapLock.lock()
                try {
                    tradeMap[trade.tradeId] = trade
                } finally {
                    tradeMapLock.unlock()
                }
                slaveSyncService.sendTrade(trade)
            }
            EventTypeEnum.TradeList -> {
                val tradeList = event.tradeList!!

                tradeMapLock.lock()
                try {
                    for (trade in tradeList) {
                        tradeMap[trade.tradeId] = trade
                    }
                } finally {
                    tradeMapLock.unlock()
                }

                syncTransactionMirror()
            }
            EventTypeEnum.OrderList -> {
                val orderList = event.orderList!!
                orderMapLock.lock()
                try {
                    for (order in orderList) {
                        orderMap[order.orderId] = order
                    }
                } finally {
                    orderMapLock.unlock()
                }

                syncTransactionMirror()
            }
            else -> {
                logger.error("未能识别的事件,Event={}", JsonUtils.writeToJsonString(event))
            }
        }
    }

    override fun handleQuoteEvent(event: Event) {
        if (event.eventType == EventTypeEnum.Tick) {
            val tick = event.tick!!
            tickMapLock.lock()
            try {
                tickMap[tick.contract.uniformSymbol] = tick
            } finally {
                tickMapLock.unlock()
            }
            slaveSyncService.sendTick(tick)
        } else {
            logger.error("未能识别的事件,Event={}", JsonUtils.writeToJsonString(event))
        }
    }

    override fun handleCommonEvent(event: Event) {
        when (event.eventType) {
            EventTypeEnum.Account -> {
                val account = event.account!!
                accountMapLock.lock()
                try {
                    accountMap[account.accountId] = account
                } finally {
                    accountMapLock.unlock()
                }
            }
            EventTypeEnum.Position -> {
                val position = event.position!!

                positionMapLock.lock()
                try {
                    positionMap[position.positionId] = position
                } finally {
                    positionMapLock.unlock()
                }
            }
            EventTypeEnum.Contract -> {
                val contract = event.contract!!
                contractMapLock.lock()
                try {
                    contractMap[contract.uniformSymbol] = contract
                } finally {
                    contractMapLock.unlock()
                }
            }
            EventTypeEnum.ContractList -> {
                val contractList = event.contractList!!

                contractMapLock.lock()
                try {
                    for (contract in contractList) {
                        contractMap[contract.uniformSymbol] = contract
                    }
                } finally {
                    contractMapLock.unlock()
                }

            }
            EventTypeEnum.Notice -> {
                val notice = event.notice!!
                slaveSyncService.sendNotice(notice)
            }
            else -> {
                logger.error("未能识别的事件,Event={}", JsonUtils.writeToJsonString(event))
            }
        }
    }

}