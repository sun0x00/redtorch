package xyz.redtorch.desktop.gui.view

import javafx.beans.value.ChangeListener
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
import xyz.redtorch.common.trade.dto.Account
import xyz.redtorch.common.trade.dto.Position
import xyz.redtorch.common.utils.CommonUtils.doubleStringCompare
import xyz.redtorch.desktop.gui.bean.PositionFXBean
import xyz.redtorch.desktop.gui.state.ViewState

class PositionTableView(private val viewState: ViewState) {
    companion object {
        private val logger = LoggerFactory.getLogger(PositionTableView::class.java)
    }

    private val positionObservableList: ObservableList<PositionFXBean> = FXCollections.observableArrayList()
    private var positionList: List<Position> = ArrayList()
    private val positionTableView: TableView<PositionFXBean> = TableView()
    private var positionFXBeanMap: MutableMap<String, PositionFXBean> = HashMap()
    private var showMergedFlag = false
    private var showEmptyFlag = false
    private var selectedPositionIdSet: MutableSet<String> = HashSet()


    private var accountList: List<Account> = ArrayList()

    val view = VBox()

    init {
        positionTableView.apply {
            isTableMenuButtonVisible = true
            isFocusTraversable = false
            selectionModel.selectionMode = SelectionMode.MULTIPLE

            columns.add(TableColumn<PositionFXBean, Pane>("合约").apply {
                prefWidth = 100.0
                cellValueFactory = PropertyValueFactory("contract")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.contract.uniformSymbol.compareTo(position2.contract.uniformSymbol)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
                // 默认排序列
                sortOrder.add(this)
            })

            columns.add(TableColumn<PositionFXBean, Text>("方向").apply {
                prefWidth = 40.0
                cellValueFactory = PropertyValueFactory("direction")
                setComparator { t1, t2 -> t1.text.compareTo(t2.text) }
            })

            columns.add(TableColumn<PositionFXBean, String>("投机套保").apply {
                prefWidth = 60.0
                cellValueFactory = PropertyValueFactory("hedgeFlag")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<PositionFXBean, Pane>("持仓").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("position")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.position.compareTo(position2.position)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })

            columns.add(TableColumn<PositionFXBean, Pane>("今仓").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("todayPosition")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.tdPosition.compareTo(position2.tdPosition)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })

            columns.add(TableColumn<PositionFXBean, Pane>("逐笔浮盈").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("openProfit")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.openPositionProfit.compareTo(position2.openPositionProfit)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })


            columns.add(TableColumn<PositionFXBean, Pane>("盯市浮盈").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("positionProfit")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.positionProfit.compareTo(position2.positionProfit)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })


            columns.add(TableColumn<PositionFXBean, Pane>("开仓价格").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("openPrice")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.openPrice.compareTo(position2.openPrice)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })


            columns.add(TableColumn<PositionFXBean, Pane>("持仓价格").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("positionPrice")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.price.compareTo(position2.price)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })


            columns.add(TableColumn<PositionFXBean, Pane>("保证金").apply {
                prefWidth = 130.0
                cellValueFactory = PropertyValueFactory("margin")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val position1 = p1.userData as Position
                        val position2 = p2.userData as Position
                        return@setComparator position1.useMargin.compareTo(position2.useMargin)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })

            columns.add(TableColumn<PositionFXBean, String>("保证金占比").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("marginRatio")
                setComparator { s1, s2 -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<PositionFXBean, String>("合约价值").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("contractValue")
                setComparator { s1, s2 -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<PositionFXBean, String>("账户ID").apply {
                prefWidth = 300.0
                cellValueFactory = PropertyValueFactory("accountId")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            setRowFactory {
                val row: TableRow<PositionFXBean> = TableRow<PositionFXBean>()
                row.setOnMousePressed { event: MouseEvent ->
                    if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                        val selectedItems: ObservableList<PositionFXBean> = selectionModel.selectedItems
                        selectedPositionIdSet.clear()
                        for (positionFXBean in selectedItems) {
                            selectedPositionIdSet.add(positionFXBean.getPositionId())
                        }
                        val clickedItem: PositionFXBean = row.item
                        viewState.updateSelectedContract(clickedItem.positionDTO!!.contract)
                    }
                }
                row
            }

        }

        // 建立数据关联
        val sortedItems: SortedList<PositionFXBean> = SortedList(positionObservableList)
        positionTableView.items = sortedItems
        // 建立排序关联
        sortedItems.comparatorProperty().bind(positionTableView.comparatorProperty())

        view.apply {

            children.add(positionTableView.apply {
                VBox.setVgrow(this, Priority.ALWAYS)
            })

            children.add(HBox().apply {
                children.add(CheckBox("合并持仓").apply {
                    selectedProperty().addListener(ChangeListener { _: ObservableValue<out Boolean>?, _: Boolean?, newValue: Boolean ->
                        showMergedFlag = newValue
                        render()
                    })
                })

                children.add(CheckBox("显示空仓").apply {
                    selectedProperty().addListener(ChangeListener { _: ObservableValue<out Boolean>?, _: Boolean?, newValue: Boolean ->
                        showEmptyFlag = newValue
                        render()
                    })
                })
            })
        }
    }

    fun updateData(positionList: List<Position>, accountList: List<Account>) {
        this.positionList = positionList
        this.accountList = accountList
        render()
    }

    fun render() {

        val filteredPositionList: MutableList<Position> = ArrayList()
        for (position in positionList) {
            if (viewState.getSelectedAccountIdSet().isEmpty() || viewState.isSelectedAccountId(position.accountId)) {
                if (showEmptyFlag) {
                    filteredPositionList.add(position)
                } else if (position.position != 0) {
                    filteredPositionList.add(position)
                }
            }
        }
        val newPositionList: MutableList<Position>
        if (showMergedFlag) {
            newPositionList = ArrayList()
            val mergedPositionMap: MutableMap<String, Position> = HashMap()
            for (position in filteredPositionList) {
                val key: String = position.contract.uniformSymbol + "#" + position.positionDirection
                var mergedPosition: Position
                if (mergedPositionMap.containsKey(key)) {
                    mergedPosition = mergedPositionMap[key]!!
                    val positionInt: Int = mergedPosition.position + position.position
                    if (positionInt != 0) {
                        val openPrice = (mergedPosition.openPrice * mergedPosition.position + position.openPrice * position.position) / positionInt
                        mergedPosition.openPrice = openPrice
                        val price = (mergedPosition.price * mergedPosition.position + position.price * position.position) / positionInt
                        mergedPosition.price = price
                        val openPriceDiff = (mergedPosition.openPriceDiff * mergedPosition.position + position.openPriceDiff * position.position) / positionInt
                        mergedPosition.openPriceDiff = openPriceDiff
                        val priceDiff = (mergedPosition.priceDiff * mergedPosition.position + position.priceDiff * position.position) / positionInt
                        mergedPosition.priceDiff = priceDiff
                    }
                    mergedPosition.position = positionInt
                    mergedPosition.frozen = mergedPosition.frozen + position.frozen
                    mergedPosition.tdPosition = mergedPosition.tdPosition + position.tdPosition
                    mergedPosition.tdFrozen = mergedPosition.tdFrozen + position.tdFrozen
                    mergedPosition.ydPosition = mergedPosition.ydPosition + position.ydPosition
                    mergedPosition.ydFrozen = mergedPosition.ydFrozen + position.ydFrozen
                    mergedPosition.contractValue = mergedPosition.contractValue + position.contractValue
                    mergedPosition.exchangeMargin = mergedPosition.exchangeMargin + position.exchangeMargin
                    mergedPosition.useMargin = mergedPosition.useMargin + position.useMargin
                    mergedPosition.openPositionProfit = mergedPosition.openPositionProfit + position.openPositionProfit
                    mergedPosition.positionProfit = mergedPosition.positionProfit + position.positionProfit
                } else {
                    mergedPosition = Position()
                    mergedPosition.contract = position.contract
                    mergedPosition.positionDirection = position.positionDirection
                    mergedPosition.position = position.position
                    mergedPosition.frozen = position.frozen
                    mergedPosition.tdPosition = position.tdPosition
                    mergedPosition.tdFrozen = position.tdFrozen
                    mergedPosition.ydPosition = position.ydPosition
                    mergedPosition.ydFrozen = position.ydFrozen
                    mergedPosition.contractValue = position.contractValue
                    mergedPosition.exchangeMargin = position.exchangeMargin
                    mergedPosition.useMargin = position.useMargin
                    mergedPosition.openPositionProfit = position.openPositionProfit
                    mergedPosition.positionProfit = position.positionProfit
                    mergedPosition.openPrice = position.openPrice
                    mergedPosition.price = position.price
                    mergedPosition.openPriceDiff = position.openPriceDiff
                    mergedPosition.priceDiff = position.priceDiff
                    mergedPosition.positionId = key
                    mergedPositionMap[key] = mergedPosition
                }
            }
            for (positionBuilder in mergedPositionMap.values) {
                if (positionBuilder.useMargin != 0.0) {
                    positionBuilder.openPositionProfitRatio = positionBuilder.openPositionProfit / positionBuilder.useMargin
                    positionBuilder.positionProfitRatio = positionBuilder.positionProfit / positionBuilder.useMargin
                }
                newPositionList.add(positionBuilder)
            }
        } else {
            newPositionList = filteredPositionList
        }
        var accountBalance = 0.0
        if (showMergedFlag) {
            val selectedAccountIdSet: Set<String> = viewState.getSelectedAccountIdSet()
            for (account in accountList) {
                if (selectedAccountIdSet.contains(account.accountId) || selectedAccountIdSet.isEmpty()) {
                    accountBalance += account.balance
                }
            }
        }
        val positionIdSet: MutableSet<String> = HashSet()
        val newPositionFXBeanList: MutableList<PositionFXBean> = ArrayList()
        for (position in newPositionList) {
            val positionId: String = position.positionId
            if (!showMergedFlag) {
                val account: Account? = viewState.queryAccountByAccountId(position.accountId)
                if (account != null) {
                    accountBalance = account.balance
                }
            }
            positionIdSet.add(positionId)
            if (positionFXBeanMap.containsKey(positionId)) {
                positionFXBeanMap[positionId]!!.update(position, viewState.isSelectedContract(position.contract), accountBalance)
            } else {
                val positionFXBean = PositionFXBean(position, viewState.isSelectedContract(position.contract), accountBalance)
                positionFXBeanMap[positionId] = positionFXBean
                newPositionFXBeanList.add(positionFXBean)
            }
        }
        positionObservableList.addAll(newPositionFXBeanList)
        val newPositionFXBeanMap: MutableMap<String, PositionFXBean> = HashMap()
        for (positionId in positionFXBeanMap.keys) {
            if (positionIdSet.contains(positionId)) {
                newPositionFXBeanMap[positionId] = positionFXBeanMap[positionId]!!
            }
        }
        positionFXBeanMap = newPositionFXBeanMap
        positionObservableList.removeIf { positionFXBean: PositionFXBean -> !positionIdSet.contains(positionFXBean.getPositionId()) }
        val newSelectedPositionIdSet: MutableSet<String> = HashSet()
        for (positionFXBean in positionObservableList) {
            if (selectedPositionIdSet.contains(positionFXBean.getPositionId())) {
                positionTableView.selectionModel.select(positionFXBean)
                newSelectedPositionIdSet.add(positionFXBean.getPositionId())
            }
        }
        selectedPositionIdSet = newSelectedPositionIdSet

    }
}