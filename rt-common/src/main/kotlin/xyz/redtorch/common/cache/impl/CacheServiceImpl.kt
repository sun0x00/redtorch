package xyz.redtorch.common.cache.impl

import xyz.redtorch.common.cache.CacheService
import xyz.redtorch.common.trade.dto.*
import java.util.concurrent.locks.ReentrantLock

//@Service
class CacheServiceImpl : CacheService {

    private val accountMap: MutableMap<String, Account> = HashMap()
    private val accountMapLock = ReentrantLock()

    private val positionMap: MutableMap<String, Position> = HashMap()
    private val positionMapLock = ReentrantLock()

    private val tradeMap: MutableMap<String, Trade> = HashMap()
    private val tradeMapLock = ReentrantLock()

    private val orderMap: MutableMap<String, Order> = HashMap()
    private val orderMapLock = ReentrantLock()

    private val tickMap: MutableMap<String, Tick> = HashMap()
    private val tickMapLock = ReentrantLock()

    private val contractMap: MutableMap<String, Contract> = HashMap()
    private val contractMapLock = ReentrantLock()

    override fun getTickList(): List<Tick> {
        val tickList = ArrayList<Tick>()
        tickMapLock.lock()
        try {
            tickList.addAll(tickMap.values)
        } finally {
            tickMapLock.unlock()
        }
        return tickList
    }

    override fun getPositionList(): List<Position> {
        val positionList = ArrayList<Position>()
        positionMapLock.lock()
        try {
            positionList.addAll(positionMap.values)
        } finally {
            positionMapLock.unlock()
        }
        return positionList
    }

    override fun getAccountList(): List<Account> {
        val accountList = ArrayList<Account>()
        accountMapLock.lock()
        try {
            accountList.addAll(accountMap.values)
        } finally {
            accountMapLock.unlock()
        }
        return accountList
    }

    override fun getOrderList(): List<Order> {
        val orderList = ArrayList<Order>()
        orderMapLock.lock()
        try {
            orderList.addAll(orderMap.values)
        } finally {
            orderMapLock.unlock()
        }
        return orderList
    }

    override fun getTradeList(): List<Trade> {
        val tradeList = ArrayList<Trade>()
        tradeMapLock.lock()
        try {
            tradeList.addAll(tradeMap.values)
        } finally {
            tradeMapLock.unlock()
        }
        return tradeList
    }

    override fun getContractList(): List<Contract> {
        val contractList = ArrayList<Contract>()
        contractMapLock.lock()
        try {
            contractList.addAll(contractMap.values)
        } finally {
            contractMapLock.unlock()
        }
        return contractList
    }

    override fun updateAccountMap(accountMap: Map<String, Account>) {
        accountMapLock.lock()
        try {
            this.accountMap.clear()
            this.accountMap.putAll(accountMap)
        } finally {
            accountMapLock.unlock()
        }
    }

    override fun updatePositionMap(positionMap: Map<String, Position>) {
        positionMapLock.lock()
        try {
            this.positionMap.clear()
            this.positionMap.putAll(positionMap)
        } finally {
            positionMapLock.unlock()
        }
    }

    override fun updateContractMap(contractMap: Map<String, Contract>) {
        contractMapLock.lock()
        try {
            this.contractMap.clear()
            this.contractMap.putAll(contractMap)
        } finally {
            contractMapLock.unlock()
        }
    }

    override fun updateTickMap(tickMap: Map<String, Tick>) {
        tickMapLock.lock()
        try {
            this.tickMap.clear()
            this.tickMap.putAll(tickMap)
        } finally {
            tickMapLock.unlock()
        }
    }

    override fun updateOrderMap(orderMap: Map<String, Order>) {
        orderMapLock.lock()
        try {
            this.orderMap.clear()
            this.orderMap.putAll(orderMap)
        } finally {
            orderMapLock.unlock()
        }
    }

    override fun updateTradeMap(tradeMap: Map<String, Trade>) {
        tradeMapLock.lock()
        try {
            this.tradeMap.clear()
            this.tradeMap.putAll(tradeMap)
        } finally {
            tradeMapLock.unlock()
        }
    }

    override fun updateTick(tick: Tick) {
        tickMapLock.lock()
        try {
            this.tickMap[tick.contract.uniformSymbol] = tick
        } finally {
            tickMapLock.unlock()
        }
    }

    override fun updateTrade(trade: Trade) {
        tradeMapLock.lock()
        try {
            this.tradeMap[trade.tradeId] = trade
        } finally {
            tradeMapLock.unlock()
        }
    }

    override fun updateOrder(order: Order) {
        orderMapLock.lock()
        try {
            this.orderMap[order.orderId] = order
        } finally {
            orderMapLock.unlock()
        }
    }

    override fun queryAccountByAccountId(accountId: String): Account? {
        return accountMap[accountId]
    }

    override fun queryPositionByPositionId(positionId: String): Position? {
        return positionMap[positionId]
    }

    override fun queryTickByUniformSymbol(uniformSymbol: String): Tick? {
        return tickMap[uniformSymbol]
    }

    override fun queryContractByUniformSymbol(uniformSymbol: String): Contract? {
        return contractMap[uniformSymbol]
    }

}