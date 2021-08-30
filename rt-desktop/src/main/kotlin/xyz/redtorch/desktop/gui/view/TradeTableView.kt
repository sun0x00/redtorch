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
import xyz.redtorch.common.trade.dto.Trade
import xyz.redtorch.common.trade.enumeration.DirectionEnum
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.desktop.gui.bean.TradeFXBean
import xyz.redtorch.desktop.gui.state.ViewState

class TradeTableView(private val viewState: ViewState) {

    companion object {
        const val SHOW_ALL = 0
        const val SHOW_LONG = 1
        const val SHOW_SHORT = 2
        private val logger = LoggerFactory.getLogger(TradeTableView::class.java)
    }

    private val tradeObservableList: ObservableList<TradeFXBean> = FXCollections.observableArrayList()
    private val tradeTableView: TableView<TradeFXBean> = TableView<TradeFXBean>()
    private var tradeList: List<Trade> = ArrayList()
    private var tradeFXBeanMap: MutableMap<String, TradeFXBean> = HashMap()
    private var showRadioValue = 0
    private var showMergedFlag = false
    private var selectedTradeIdSet: MutableSet<String> = HashSet()

    val view = VBox()

    init {
        tradeTableView.apply {
            isTableMenuButtonVisible = true
            isFocusTraversable = false
            selectionModel.selectionMode = SelectionMode.MULTIPLE

            columns.add(TableColumn<TradeFXBean, Pane>("合约").apply {
                prefWidth = 100.0
                cellValueFactory = PropertyValueFactory("contract")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val trade1 = p1.userData as Trade
                        val trade2 = p2.userData as Trade
                        return@setComparator trade1.contract.uniformSymbol.compareTo(trade2.contract.uniformSymbol)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })

            columns.add(TableColumn<TradeFXBean, Text>("方向").apply {
                prefWidth = 40.0
                cellValueFactory = PropertyValueFactory("direction")
                setComparator { t1, t2 -> t1.text.compareTo(t2.text) }
            })

            columns.add(TableColumn<TradeFXBean, String>("开平").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("offsetFlag")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<TradeFXBean, String>("投机套保").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("hedgeFlag")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<TradeFXBean, String>("价格").apply {
                prefWidth = 80.0
                cellValueFactory = PropertyValueFactory("price")
                setComparator { s1, s2 -> CommonUtils.doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<TradeFXBean, Int>("成交量").apply {
                prefWidth = 50.0
                cellValueFactory = PropertyValueFactory("volume")
                setComparator { i1, i2 -> i1.compareTo(i2) }
            })

            columns.add(TableColumn<TradeFXBean, String>("成交时间").apply {
                prefWidth = 120.0
                cellValueFactory = PropertyValueFactory("tradeTime")
                setComparator { s1, s2 -> s1.compareTo(s2) }
                // 默认降序排列
                sortType = TableColumn.SortType.DESCENDING
                // 默认排序列
                sortOrder.add(this)
            })

            columns.add(TableColumn<TradeFXBean, String>("适配器成交编号").apply {
                prefWidth = 260.0
                cellValueFactory = PropertyValueFactory("adapterTradeId")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<TradeFXBean, String>("原始定单编号").apply {
                prefWidth = 250.0
                cellValueFactory = PropertyValueFactory("originOrderId")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<TradeFXBean, String>("账户ID").apply {
                prefWidth = 300.0
                cellValueFactory = PropertyValueFactory("accountId")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            setRowFactory {
                val row: TableRow<TradeFXBean> = TableRow<TradeFXBean>()
                row.setOnMousePressed { event: MouseEvent ->
                    if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                        val selectedItems: ObservableList<TradeFXBean> = tradeTableView.selectionModel.selectedItems
                        selectedTradeIdSet.clear()
                        for (trade in selectedItems) {
                            selectedTradeIdSet.add(trade.getTradeId())
                        }
                        val clickedItem: TradeFXBean = row.item
                        viewState.updateSelectedContract(clickedItem.tradeDTO!!.contract)
                    }
                }
                row
            }
        }

        // 建立数据关系
        val sortedItems: SortedList<TradeFXBean> = SortedList(tradeObservableList)
        tradeTableView.items = sortedItems
        // 建立排序关系
        sortedItems.comparatorProperty().bind(tradeTableView.comparatorProperty())

        view.apply {
            children.add(tradeTableView.apply {
                VBox.setVgrow(this, Priority.ALWAYS)
            })

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
                children.add(RadioButton("做多记录").apply {
                    toggleGroup = radioToggleGroup
                    userData = SHOW_LONG
                })
                children.add(RadioButton("做空记录").apply {
                    toggleGroup = radioToggleGroup
                    userData = SHOW_SHORT
                })
                children.add(CheckBox("合并显示").apply {
                    selectedProperty().addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newValue: Boolean ->
                        showMergedFlag = newValue
                        render()
                    }
                })
            })
        }
    }

    fun updateData(tradeList: List<Trade>) {
        this.tradeList = tradeList
        render()
    }


    fun render() {
        val filteredTradeList: MutableList<Trade> = ArrayList()
        for (trade in tradeList) {
            if (viewState.getSelectedAccountIdSet().isEmpty() || viewState.isSelectedAccountId(trade.accountId)) {
                if (showRadioValue == SHOW_ALL) {
                    filteredTradeList.add(trade)
                } else if (showRadioValue == SHOW_LONG) {
                    if (DirectionEnum.Buy == trade.direction) {
                        filteredTradeList.add(trade)
                    }
                } else if (showRadioValue == SHOW_SHORT) {
                    if (DirectionEnum.Sell == trade.direction) {
                        filteredTradeList.add(trade)
                    }
                }
            }
        }
        val newTradeList: MutableList<Trade>
        if (showMergedFlag) {
            newTradeList = ArrayList()
            val mergedTradeMap: MutableMap<String, Trade> = HashMap()
            for (trade in filteredTradeList) {
                val key: String = trade.contract.uniformSymbol + "#" + trade.direction + "#" + trade.offsetFlag
                var mergedTrade: Trade
                if (mergedTradeMap.containsKey(key)) {
                    mergedTrade = mergedTradeMap[key]!!
                    mergedTrade.price = (mergedTrade.price * mergedTrade.volume + trade.price * trade.volume) / (mergedTrade.volume + trade.volume)
                    mergedTrade.volume = mergedTrade.volume + trade.volume
                } else {
                    mergedTrade = Trade()
                    mergedTrade.contract = trade.contract
                    mergedTrade.direction = trade.direction
                    mergedTrade.offsetFlag = trade.offsetFlag
                    mergedTrade.price = trade.price
                    mergedTrade.volume = trade.volume
                    mergedTrade.tradeId = key
                    mergedTradeMap[key] = mergedTrade
                }
            }
            newTradeList.addAll(mergedTradeMap.values)
        } else {
            newTradeList = filteredTradeList
        }
        val tradeIdSet: MutableSet<String> = HashSet()
        val newTradeFXBeanList: MutableList<TradeFXBean> = ArrayList()
        for (trade in newTradeList) {
            val tradeId: String = trade.tradeId
            tradeIdSet.add(tradeId)
            if (tradeFXBeanMap.containsKey(tradeId)) {
                tradeFXBeanMap[tradeId]!!.update(trade, viewState.isSelectedContract(trade.contract))
            } else {
                val tradeFXBean = TradeFXBean(trade, viewState.isSelectedContract(trade.contract))
                tradeFXBeanMap[tradeId] = tradeFXBean
                newTradeFXBeanList.add(tradeFXBean)
            }
        }
        tradeObservableList.addAll(newTradeFXBeanList)
        val newTradeFXBeanMap: MutableMap<String, TradeFXBean> = HashMap()
        for (tradeId in tradeFXBeanMap.keys) {
            if (tradeIdSet.contains(tradeId)) {
                newTradeFXBeanMap[tradeId] = tradeFXBeanMap[tradeId]!!
            }
        }
        tradeFXBeanMap = newTradeFXBeanMap
        tradeObservableList.removeIf { tradeFXBean: TradeFXBean -> !tradeIdSet.contains(tradeFXBean.getTradeId()) }
        val newSelectedTradeIdSet: MutableSet<String> = HashSet()
        for (tradeFXBean in tradeObservableList) {
            if (selectedTradeIdSet.contains(tradeFXBean.getTradeId())) {
                tradeTableView.selectionModel.select(tradeFXBean)
                newSelectedTradeIdSet.add(tradeFXBean.getTradeId())
            }
        }
        selectedTradeIdSet = newSelectedTradeIdSet
    }
}