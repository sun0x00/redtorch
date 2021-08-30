package xyz.redtorch.desktop.gui.state

import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import xyz.redtorch.common.cache.CacheService
import xyz.redtorch.common.sync.dto.CancelOrder
import xyz.redtorch.common.sync.dto.InsertOrder
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.trade.client.service.TradeClientSyncService
import xyz.redtorch.common.trade.client.service.TradeClientSyncServiceCallBack
import xyz.redtorch.common.trade.dto.*
import xyz.redtorch.desktop.gui.view.DashboardView
import xyz.redtorch.desktop.gui.view.LoginView

@Component
class ViewState : InitializingBean, TradeClientSyncServiceCallBack {
    companion object {
        private val logger = LoggerFactory.getLogger(ViewState::class.java)
    }

    @Autowired
    private lateinit var cacheService: CacheService

    @Autowired
    private lateinit var tradeClientSyncService: TradeClientSyncService

    var primaryStage: Stage? = null

    private var dashboardView: DashboardView? = null
    private var selectedAccountIdSet: MutableSet<String> = HashSet()
    private var selectedContract: Contract? = null


    override fun afterPropertiesSet() {

        // 设置ViewState实例为交易客户端同步回调接口的实现
        tradeClientSyncService.setCallBack(this)

        Thread {
            Thread.currentThread().name = "RT-ViewState-UpdateData"

            // 统计循环次数
            var count = 0
            while (!Thread.currentThread().isInterrupted) {
                try {
                    // 间隔一段时间再刷新UI
                    Thread.sleep(500)

                    // 如果当前处于看板视图,则执行更新操作
                    dashboardView?.let {
                        val tickList = cacheService.getTickList()
                        val positionList = cacheService.getPositionList()
                        val accountList = cacheService.getAccountList()
                        val orderList = cacheService.getOrderList()
                        val tradeList = cacheService.getTradeList()

                        val delay = tradeClientSyncService.getDelay()
                        val connectionStatus = if (tradeClientSyncService.isConnected()) {
                            "已连接"
                        } else {
                            if (tradeClientSyncService.isAutoReconnect()) {
                                "重连中"
                            } else {
                                "已断开"
                            }
                        }

                        Platform.runLater {
                            it.quoteTableView.updateData(tickList)
                            it.combinationView.updateData(positionList, accountList)
                            it.accountTableView.updateData(accountList)
                            it.positionTableView.updateData(positionList, accountList)
                            it.orderTableView.updateData(orderList)
                            it.tradeTableView.updateData(tradeList)
                            it.updateStatusBar(connectionStatus, delay)
                        }

                        if (count % 20 == 0) {
                            // 40*250ms=10s
                            // 降低contractTableView的刷新频率
                            val contractList = cacheService.getContractList()
                            it.contractTableView.updateData(contractList)
                        }

                        // 递增
                        count++
                        // 重置为0
                        if (count == Int.MAX_VALUE) {
                            count = 0
                        }
                    }
                } catch (ie: InterruptedException) {
                    break
                } catch (e: Exception) {
                    logger.error("定时刷新视图线程异常", e)
                }
            }

        }.start()
    }

    // 登录认证
    fun auth(userId: String?, authToken: String?): Boolean {
        if (userId.isNullOrBlank() || authToken.isNullOrBlank()) {
            return false
        }
        val res = tradeClientSyncService.auth(userId, authToken)
        return if (res) {
            showDashboardView()
            true
        } else {
            false
        }
    }

    // 切换到登录视图
    fun showLoginView() {
        dashboardView = null
        val widthHeight = getWidthHeight()
        val root = LoginView(this).view
        primaryStage!!.scene = Scene(root, widthHeight.first, widthHeight.second).apply {
            stylesheets.add("fx/style/main.css")
        }
    }

    // 切换到看板视图
    private fun showDashboardView() {
        val widthHeight = getWidthHeight()
        dashboardView = DashboardView(this)
        val root = dashboardView!!.view
        primaryStage!!.scene = Scene(root, widthHeight.first, widthHeight.second).apply {
            stylesheets.add("fx/style/main.css")
        }
    }

    // 获取当前GUI的宽和高，如果获取不到数据，则可能尚未初始化，设置默认宽和高
    private fun getWidthHeight(): Pair<Double, Double> {
        val width =
            if (primaryStage!!.scene == null || primaryStage!!.scene.width.isNaN()) 1024.0 else primaryStage!!.scene.width
        val height =
            if (primaryStage!!.scene == null || primaryStage!!.scene.height.isNaN()) 630.0 else primaryStage!!.scene.height
        return Pair(width, height)
    }

    // 判断账户是否已选中
    fun isSelectedAccountId(accountId: String): Boolean {
        return selectedAccountIdSet.contains(accountId)
    }

    // 更新已选中账户ID集合
    fun updateSelectedAccountIdSet(selectedAccountIdSet: MutableSet<String>) {
        // 如果新集合和旧集合不相等再更新
        if (this.selectedAccountIdSet != selectedAccountIdSet) {
            // 新建HashSet实例,避免引用共享导致修改冲突
            this.selectedAccountIdSet = selectedAccountIdSet

            // 更新视图
            dashboardView?.let {
                Platform.runLater {
                    it.placeOrderView.render()
                    it.combinationView.render()
                    it.orderTableView.render()
                    it.tradeTableView.render()
                }
            }
        }
        // WARN
        // 警告：AccountTableView render之后会调用此方法，因此不要在此处调用accountTableView.render()，避免循环调用
        // WARN
    }

    // 获取已选中账户ID集合
    fun getSelectedAccountIdSet(): Set<String> {
        return HashSet<String>().apply {
            addAll(selectedAccountIdSet)
        }
    }

    // 判断合约是否已选中
    fun isSelectedContract(contract: Contract): Boolean {
        selectedContract?.let {
            return it.uniformSymbol == contract.uniformSymbol
        }
        return false
    }

    // 获取已选择的合约
    fun getSelectedContract(): Contract? {
        return selectedContract
    }

    // 更新选中合约
    fun updateSelectedContract(contract: Contract) {
        this.selectedContract = contract

        // 订阅选中合约
        tradeClientSyncService.subscribeContract(contract)

        // 选中合约后,更新视图
        dashboardView?.let {
            // 尝试获取Tick
            val tick = cacheService.queryTickByUniformSymbol(contract.uniformSymbol)

            Platform.runLater {
                it.quoteTableView.render()
                it.positionTableView.render()
                it.orderTableView.render()
                it.tradeTableView.render()
                it.quoteView.updateData(tick)
                it.placeOrderView.updateData(tick)
            }
        }
    }

    // 取消订阅合约
    fun unsubscribe(contract: Contract) {
        selectedContract?.let { sc ->
            // 如果已经选择的合约和当前取消订阅的合约相同，则重置视图数据
            if (sc.uniformSymbol == contract.uniformSymbol) {
                this.selectedContract = null
                // 更新视图
                dashboardView?.let {
                    Platform.runLater {
                        it.quoteTableView.render()
                        it.positionTableView.render()
                        it.orderTableView.render()
                        it.tradeTableView.render()
                        it.quoteView.updateData(null)
                        it.placeOrderView.updateData(null)
                    }
                }
            }
        }

        tradeClientSyncService.unsubscribeContract(contract)
    }

    // 提交定单
    fun submitOrder(insertOrder: InsertOrder) {
        tradeClientSyncService.submitOrder(insertOrder)
    }

    // 撤销定单
    fun cancelOrder(gatewayId: String, orderId: String, originOrderId: String) {
        tradeClientSyncService.cancelOrder(CancelOrder().apply {
            this.gatewayId = gatewayId
            this.orderId = orderId
            this.originOrderId = originOrderId
        })
    }

    // 根据accountID从缓存查询账户
    fun queryAccountByAccountId(accountId: String): Account? {
        return cacheService.queryAccountByAccountId(accountId)
    }

    // 处理成交回报
    override fun handleTradeRtn(trade: Trade) {
    }

    // 处理定单回报
    override fun handleOrderRtn(order: Order) {
    }

    // 处理Tick回报
    override fun handleTickRtn(tick: Tick) {
        // 如果合约已被选中,则更新视图
        selectedContract?.let { sc ->
            if (tick.contract.uniformSymbol == sc.uniformSymbol) {
                dashboardView?.let {
                    Platform.runLater {
                        it.quoteView.updateData(tick)
                        it.placeOrderView.updateData(tick)
                    }
                }
            }
        }
    }

    // 处理通知回报
    override fun handleNoticeRtn(notice: Notice) {
        dashboardView?.noticeView?.addNotice(notice)
    }

    override fun handleAuthFailed() {
        dashboardView?.let {
            Platform.runLater {
                showLoginView()
            }
        }
    }

}