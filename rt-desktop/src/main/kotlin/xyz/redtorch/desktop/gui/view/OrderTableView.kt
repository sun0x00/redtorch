package xyz.redtorch.desktop.gui.view

import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.trade.dto.Order
import xyz.redtorch.common.utils.CommonUtils.doubleStringCompare
import xyz.redtorch.desktop.gui.bean.OrderFXBean
import xyz.redtorch.desktop.gui.state.ViewState

class OrderTableView(private val viewState: ViewState) {

    companion object {
        const val SHOW_ALL = 0
        const val SHOW_CANCELABLE = 1
        const val SHOW_CANCELLED = 2
        private val logger = LoggerFactory.getLogger(OrderTableView::class.java)
    }

    private val orderObservableList: ObservableList<OrderFXBean> = FXCollections.observableArrayList()
    private var orderList: List<Order> = ArrayList()
    private val orderTableView = TableView<OrderFXBean>()
    private var orderFXBeanMap: MutableMap<String, OrderFXBean> = HashMap()
    private var showRadioValue = 0
    private var showRejectedFlag = false
    private var selectedOrderIdSet: MutableSet<String> = HashSet()

    val view = VBox()

    init {
        orderTableView.apply {
            isTableMenuButtonVisible = true
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            isFocusTraversable = false

            columns.add(TableColumn<OrderFXBean, Pane>("合约").apply {
                prefWidth = 100.0
                cellValueFactory = PropertyValueFactory("contract")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val order1 = p1.userData as Order
                        val order2 = p2.userData as Order
                        return@setComparator order1.contract.uniformSymbol.compareTo(order2.contract.uniformSymbol)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })

            columns.add(TableColumn<OrderFXBean, Text>("方向").apply {
                prefWidth = 40.0
                cellValueFactory = PropertyValueFactory("direction")
                setComparator { t1, t2 -> t1.text.compareTo(t2.text) }
            })

            columns.add(TableColumn<OrderFXBean, String>("开平").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("offsetFlag")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("投机套保").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("hedgeFlag")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("价格类型").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("orderPriceType")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("价格").apply {
                prefWidth = 80.0
                cellValueFactory = PropertyValueFactory("price")
                setComparator { s1, s2 -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<OrderFXBean, Pane>("数量").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("volume")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val order1 = p1.userData as Order
                        val order2 = p2.userData as Order
                        return@setComparator order1.totalVolume.compareTo(order2.totalVolume)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })

            columns.add(TableColumn<OrderFXBean, Text>("状态").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("orderStatus")
                setComparator { t1, t2 -> t1.text.compareTo(t2.text) }
            })

            columns.add(TableColumn<OrderFXBean, String>("状态信息").apply {
                prefWidth = 130.0
                cellValueFactory = PropertyValueFactory("statusMsg")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("委托时间").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("orderTime")
                setComparator { s1, s2 -> s1.compareTo(s2) }
                // 默认降序排列
                sortType = TableColumn.SortType.DESCENDING
                // 默认排序列
                sortOrder.add(this)
            })

            columns.add(TableColumn<OrderFXBean, String>("时效类型").apply {
                prefWidth = 100.0
                cellValueFactory = PropertyValueFactory("timeCondition")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("成交量类型").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("volumeCondition")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, Int>("最小数量").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("minVolume")
                setComparator { i1, i2 -> i1.compareTo(i2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("触发条件").apply {
                prefWidth = 120.0
                cellValueFactory = PropertyValueFactory("contingentCondition")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("条件价格").apply {
                prefWidth = 80.0
                cellValueFactory = PropertyValueFactory("stopPrice")
                setComparator { s1, s2 -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("适配器定单编号").apply {
                prefWidth = 230.0
                cellValueFactory = PropertyValueFactory("adapterOrderId")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("原始定单编号").apply {
                prefWidth = 250.0
                cellValueFactory = PropertyValueFactory("originOrderId")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<OrderFXBean, String>("账户ID").apply {
                prefWidth = 300.0
                cellValueFactory = PropertyValueFactory("accountId")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            setOnMousePressed {
                val selectedItems = this.selectionModel.selectedItems
                selectedOrderIdSet.clear()
                for (row in selectedItems) {
                    selectedOrderIdSet.add(row.getOrderId())
                }
            }
            setRowFactory {
                val row: TableRow<OrderFXBean> = TableRow<OrderFXBean>()
                row.setOnMousePressed { event: MouseEvent ->
                    if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                        val selectedItems = orderTableView.selectionModel.selectedItems
                        selectedOrderIdSet.clear()
                        for (order in selectedItems) {
                            selectedOrderIdSet.add(order.getOrderId())
                        }
                        val clickedItem = row.item
                        // 更新选中合约
                        viewState.updateSelectedContract(clickedItem.orderDTO!!.contract)
                    } else if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                        // 双击撤单
                        viewState.cancelOrder(row.item.orderDTO!!.gatewayId, row.item.orderDTO!!.orderId, row.item.orderDTO!!.originOrderId)
                    }
                }
                row
            }

        }

        // 建立数据关联
        val sortedItems = SortedList(orderObservableList)
        orderTableView.items = sortedItems
        // 建立排序关联
        sortedItems.comparatorProperty().bind(orderTableView.comparatorProperty())


        view.apply {
            children.add(orderTableView.apply {
                VBox.setVgrow(this, Priority.ALWAYS)
            })

            // 设置过滤选项
            val radioToggleGroup = ToggleGroup().apply {
                selectedToggleProperty().addListener { _: ObservableValue<out Toggle>?, _: Toggle?, newValue: Toggle ->
                    showRadioValue = newValue.userData as Int
                    render()
                }
            }

            children.add(HBox().apply {
                children.add(RadioButton("全部").apply {
                    toggleGroup = radioToggleGroup
                    userData = SHOW_ALL
                    isSelected = true
                })
                children.add(RadioButton("可撤销").apply {
                    toggleGroup = radioToggleGroup
                    userData = SHOW_CANCELABLE
                })
                children.add(RadioButton("已撤销").apply {
                    toggleGroup = radioToggleGroup
                    userData = SHOW_CANCELLED
                })
                children.add(CheckBox("显示拒单").apply {
                    selectedProperty().addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newValue: Boolean ->
                        showRejectedFlag = newValue
                        render()
                    }
                })
            })
        }
    }

    fun updateData(orderList: List<Order>) {
        this.orderList = orderList
        render()
    }


    fun render() {
        orderTableView.selectionModel.clearSelection()
        val filteredOrderList: MutableList<Order> = ArrayList()
        for (order in orderList) {
            if (viewState.getSelectedAccountIdSet().isEmpty() || viewState.getSelectedAccountIdSet().contains(order.accountId)) {
                if (showRadioValue == SHOW_ALL) {
                    if (showRejectedFlag) {
                        filteredOrderList.add(order)
                    } else if (xyz.redtorch.common.trade.enumeration.OrderStatusEnum.Rejected != order.orderStatus) {
                        filteredOrderList.add(order)
                    }
                } else if (showRadioValue == SHOW_CANCELABLE) {
                    if (Constant.ORDER_STATUS_WORKING_SET.contains(order.orderStatus)) {
                        filteredOrderList.add(order)
                    }
                } else {
                    if (xyz.redtorch.common.trade.enumeration.OrderStatusEnum.Canceled == order.orderStatus) {
                        filteredOrderList.add(order)
                    }
                }
            }
        }
        val orderIdSet: MutableSet<String> = HashSet()
        val newOrderFXBeanList: MutableList<OrderFXBean> = ArrayList()
        for (order in filteredOrderList) {
            val orderId: String = order.orderId
            orderIdSet.add(orderId)
            if (orderFXBeanMap.containsKey(orderId)) {
                orderFXBeanMap[orderId]!!.update(order, viewState.isSelectedContract(order.contract))
            } else {
                val orderFXBean = OrderFXBean(order, viewState.isSelectedContract(order.contract))
                orderFXBeanMap[orderId] = orderFXBean
                newOrderFXBeanList.add(orderFXBean)
            }
        }
        orderObservableList.addAll(newOrderFXBeanList)
        val newOrderFXBeanMap: MutableMap<String, OrderFXBean> = HashMap()
        for (orderId in orderFXBeanMap.keys) {
            if (orderIdSet.contains(orderId)) {
                newOrderFXBeanMap[orderId] = orderFXBeanMap[orderId]!!
            }
        }
        orderFXBeanMap = newOrderFXBeanMap
        orderObservableList.removeIf { orderFXBean: OrderFXBean -> !orderIdSet.contains(orderFXBean.getOrderId()) }
        val newSelectedOrderIdSet: MutableSet<String> = HashSet()
        for (orderFXBean in orderObservableList) {
            if (selectedOrderIdSet.contains(orderFXBean.getOrderId())) {
                orderTableView.selectionModel.select(orderFXBean)
                newSelectedOrderIdSet.add(orderFXBean.getOrderId())
            }
        }
        selectedOrderIdSet = newSelectedOrderIdSet
    }
}