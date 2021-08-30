package xyz.redtorch.common.event.service.impl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import xyz.redtorch.common.event.Event
import xyz.redtorch.common.event.EventObserver
import xyz.redtorch.common.event.EventTypeEnum
import xyz.redtorch.common.event.service.EventService
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.dto.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

//@Service
class EventServiceImpl : EventService, InitializingBean {

    private val logger = LoggerFactory.getLogger(EventServiceImpl::class.java)

    private val transactionQueue = LinkedBlockingQueue<Event>()
    private val quoteQueue = LinkedBlockingQueue<Event>()
    private val commonQueue = LinkedBlockingQueue<Event>()

    private val eventObserverSet = HashSet<EventObserver>()

    private val executor = Executors.newCachedThreadPool()


    override fun emit(account: Account) {
        val event = Event()
        event.eventType = EventTypeEnum.Account
        event.account = account
        commonQueue.add(event)
    }

    override fun emit(position: Position) {
        val event = Event()
        event.eventType = EventTypeEnum.Position
        event.position = position
        commonQueue.add(event)
    }

    override fun emit(order: Order) {
        val event = Event()
        event.eventType = EventTypeEnum.Order
        event.order = order
        transactionQueue.add(event)
    }

    override fun emit(trade: Trade) {
        val event = Event()
        event.eventType = EventTypeEnum.Trade
        event.trade = trade
        transactionQueue.add(event)
    }

    override fun emit(tick: Tick) {
        val event = Event()
        event.eventType = EventTypeEnum.Tick
        event.tick = tick
        quoteQueue.add(event)
    }

    override fun emit(contract: Contract) {
        val event = Event()
        event.eventType = EventTypeEnum.Contract
        event.contract = contract
        commonQueue.add(event)
    }

    override fun emit(notice: Notice) {
        val event = Event()
        event.eventType = EventTypeEnum.Notice
        event.notice = notice
        commonQueue.add(event)
    }

    override fun emitContractList(contractList: List<Contract>) {
        val event = Event()
        event.eventType = EventTypeEnum.ContractList
        event.contractList = contractList
        commonQueue.add(event)
    }

    override fun emitTradeList(tradeList: List<Trade>) {
        val event = Event()
        event.eventType = EventTypeEnum.TradeList
        event.tradeList = tradeList
        transactionQueue.add(event)
    }

    override fun emitOrderList(orderList: List<Order>) {
        val event = Event()
        event.eventType = EventTypeEnum.OrderList
        event.orderList = orderList
        transactionQueue.add(event)
    }

    override fun register(eventObserver: EventObserver) {
        eventObserverSet.add(eventObserver)
    }

    override fun afterPropertiesSet() {
        executor.execute {
            Thread.currentThread().name = "RT-EventServiceImpl-Transaction"
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val event = transactionQueue.take()
                    for (eventObserver in eventObserverSet) {
                        try {
                            eventObserver.handleTransactionEvent(event)
                        } catch (e: Exception) {
                            logger.error("处理Transaction事件异常", e)
                        }
                    }
                }catch (ie:InterruptedException){
                    break
                }
            }
        }

        executor.execute {
            Thread.currentThread().name = "RT-EventServiceImpl-Quote"
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val event = quoteQueue.take()
                    for (eventObserver in eventObserverSet) {
                        try {
                            eventObserver.handleQuoteEvent(event)
                        } catch (e: Exception) {
                            logger.error("处理Quote事件异常", e)
                        }
                    }
                }catch (ie:InterruptedException){
                    break
                }
            }
        }

        executor.execute {
            Thread.currentThread().name = "RT-EventServiceImpl-Common"
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val event = commonQueue.take()
                    for (eventObserver in eventObserverSet) {
                        try {
                            eventObserver.handleCommonEvent(event)
                        } catch (e: Exception) {
                            logger.error("处理Common事件异常", e)
                        }
                    }
                }catch (ie:InterruptedException){
                    break
                }
            }
        }
    }
}