package xyz.redtorch.desktop.gui.view

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.desktop.gui.state.ViewState

class DashboardView(private val viewState: ViewState) {

    val quoteTableView = QuoteTableView(viewState)
    val quoteView = QuoteView(viewState)
    val placeOrderView = PlaceOrderView(viewState)
    val orderTableView = OrderTableView(viewState)
    val tradeTableView = TradeTableView(viewState)
    val combinationView = CombinationView(viewState)
    val accountTableView = AccountTableView(viewState)
    val positionTableView = PositionTableView(viewState)
    val contractTableView = ContractTableView(viewState)
    val noticeView = NoticeView(viewState)

    private val statusBarLeftText = Text("未知")
    private val statusBarRightText = Text("---")


    val view = VBox().apply {
        // 菜单栏
        children.add(createMainMenuBar())
        // 左右切分布局
        children.add(SplitPane().apply {
            // 自动垂直高度
            VBox.setVgrow(this, Priority.ALWAYS)
            // 分隔栏位置
            setDividerPositions(0.5)

            // 左侧布局
            items.add(SplitPane().apply {
                // 上下切分
                orientation = Orientation.VERTICAL
                // 分隔栏位置
                setDividerPositions(0.4)

                // 上部布局
                items.add(SplitPane().apply {
                    // 左侧 行情表格
                    items.add(quoteTableView.view)
                    // 右侧 行情+交易面板
                    items.add(HBox().apply {
                        SplitPane.setResizableWithParent(this, false)
                        maxWidth = 680.0
                        prefWidth = 680.0

                        // 左侧 行情
                        children.add(ScrollPane().apply {
                            content = quoteView.view
                            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                            prefWidth = 253.0
                            minWidth = 253.0
                            maxWidth = 253.0
                        })

                        // 右侧 交易
                        children.add(placeOrderView.view.apply {
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                    })
                })

                // 下部布局
                items.add(TabPane().apply {
                    tabs.add(Tab("定单").apply {
                        isClosable = false
                        content = orderTableView.view
                    })
                    tabs.add(Tab("成交").apply {
                        isClosable = false
                        content = tradeTableView.view
                    })
                })

            })

            // 右侧布局
            items.add(TabPane().apply {
                // 投资组合页签
                tabs.add(Tab("投资组合").apply {
                    isClosable = false
                    // 上下布局
                    content = SplitPane().apply {
                        orientation = Orientation.VERTICAL
                        VBox.setVgrow(this, Priority.ALWAYS)
                        items.add(VBox().apply {
                            children.add(combinationView.view)
                            children.add(accountTableView.view.apply {
                                VBox.setVgrow(this, Priority.ALWAYS)
                            })
                        })
                        items.add(positionTableView.view)
                    }
                })
                // 全部合约页签
                tabs.add(Tab("全部合约").apply {
                    isClosable = false
                    content = contractTableView.view
                })

                // 通知页签
                tabs.add(Tab("通知").apply {
                    isClosable = false
                    content = noticeView.view
                })

            })
        })

        // 状态栏
        children.add(createStatusBar())
    }


    private fun createMainMenuBar(): MenuBar {
        // 暂无具体功能
        return MenuBar()
    }


    private fun createStatusBar(): HBox {
        return HBox().apply {
            padding = Insets(2.0, 5.0, 2.0, 5.0)
            children.add(statusBarLeftText)
            children.add(Pane().apply {
                HBox.setHgrow(this, Priority.ALWAYS)
            })
            children.add(statusBarRightText)
        }
    }

    fun updateStatusBar(connectionStatus: String, delay: Long) {
        this.statusBarLeftText.text = connectionStatus
        this.statusBarRightText.text = "${delay}ms"
    }
}