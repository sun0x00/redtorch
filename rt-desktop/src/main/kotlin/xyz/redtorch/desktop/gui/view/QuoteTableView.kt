package xyz.redtorch.desktop.gui.view

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.desktop.gui.bean.TickFXBean
import xyz.redtorch.desktop.gui.state.ViewState

class QuoteTableView(private val viewState: ViewState) {
    companion object {
        private val logger = LoggerFactory.getLogger(QuoteTableView::class.java)
    }

    private val tickObservableList: ObservableList<TickFXBean> = FXCollections.observableArrayList()
    private var tickList: List<Tick> = ArrayList()
    private val tickTableView = TableView<TickFXBean>()
    private var tickFXBeanMap: MutableMap<String, TickFXBean> = HashMap()
    private var selectedTickUniformSymbolSet: MutableSet<String> = HashSet()
    val view = VBox()

    init {

        tickTableView.apply {
            isTableMenuButtonVisible = true
            isFocusTraversable = false
            selectionModel.selectionMode = SelectionMode.MULTIPLE

            columns.add(TableColumn<TickFXBean, Pane>("合约").apply {
                prefWidth = 100.0
                cellValueFactory = PropertyValueFactory("contract")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val tick1: Tick = p1.userData as Tick
                        val tick2: Tick = p2.userData as Tick
                        return@setComparator tick1.contract.uniformSymbol.compareTo(tick2.contract.uniformSymbol)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
                // 默认排序列
                sortOrder.add(this)
            })

            columns.add(TableColumn<TickFXBean, Pane>("最新价格").apply {
                prefWidth = 120.0
                cellValueFactory = PropertyValueFactory("lastPrice")
                setComparator { p1: Pane, p2: Pane ->
                    try {
                        val tick1: Tick = p1.userData as Tick
                        val tick2: Tick = p2.userData as Tick
                        return@setComparator tick1.lastPrice.compareTo(tick2.lastPrice)
                    } catch (e: Exception) {
                        logger.error("排序异常", e)
                    }
                    0
                }
            })

            columns.add(TableColumn<TickFXBean, Pane>("买卖一价").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("abPrice")
                isSortable = false
            })

            columns.add(TableColumn<TickFXBean, Pane>("买卖一量").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("abVolume")
                isSortable = false
            })

            columns.add(TableColumn<TickFXBean, Pane>("成交量").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("volume")
                isSortable = false
            })

            columns.add(TableColumn<TickFXBean, Pane>("持仓量").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("openInterest")
                isSortable = false
            })

            columns.add(TableColumn<TickFXBean, Pane>("涨跌停").apply {
                prefWidth = 70.0
                cellValueFactory = PropertyValueFactory("limit")
                isSortable = false
            })

            columns.add(TableColumn<TickFXBean, Pane>("时间").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("actionTime")
                isSortable = false
            })


            tickTableView.onMousePressed = EventHandler {
                val selectedItems = tickTableView.selectionModel.selectedItems
                selectedTickUniformSymbolSet.clear()
                for (row in selectedItems) {
                    selectedTickUniformSymbolSet.add(row.tickDTO!!.contract.uniformSymbol)
                }
            }

            tickTableView.setRowFactory {
                val row = TableRow<TickFXBean>()
                row.onMousePressed = EventHandler { event: MouseEvent ->
                    if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                        val selectedItems = tickTableView.selectionModel.selectedItems
                        selectedTickUniformSymbolSet.clear()
                        for (tick in selectedItems) {
                            selectedTickUniformSymbolSet.add(tick.tickDTO!!.contract.uniformSymbol)
                        }
                        viewState.updateSelectedContract(row.item.tickDTO!!.contract)
                    } else if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                        viewState.unsubscribe(row.item.tickDTO!!.contract)
                    }
                }
                row
            }
        }

        // 建立数据关系
        val sortedItems = SortedList(tickObservableList)
        tickTableView.items = sortedItems
        // 建立排序关系
        sortedItems.comparatorProperty().bind(tickTableView.comparatorProperty())

        view.apply {
            minWidth = 1.0
            children.add(tickTableView.apply {
                VBox.setVgrow(this, Priority.ALWAYS)
            })
        }
    }

    fun updateData(tickList: List<Tick>) {
        this.tickList = tickList
        render()
    }

    fun render() {

        tickTableView.selectionModel.clearSelection()
        val uniformSymbolSet: MutableSet<String> = HashSet()
        val newTickFXBeanList: MutableList<TickFXBean> = ArrayList()
        for (tick in tickList) {
            val uniformSymbol: String = tick.contract.uniformSymbol
            uniformSymbolSet.add(uniformSymbol)
            val contract: Contract = tick.contract
            if (tickFXBeanMap.containsKey(uniformSymbol)) {
                tickFXBeanMap[uniformSymbol]!!.update(tick, viewState.isSelectedContract(contract))
            } else {
                val tickFXBean = TickFXBean(tick, viewState.isSelectedContract(contract))
                tickFXBeanMap[uniformSymbol] = tickFXBean
                newTickFXBeanList.add(tickFXBean)
            }
        }
        tickObservableList.addAll(newTickFXBeanList)
        val newTickFXBeanMap: MutableMap<String, TickFXBean> = HashMap()
        for (uniformSymbol in tickFXBeanMap.keys) {
            if (uniformSymbolSet.contains(uniformSymbol)) {
                newTickFXBeanMap[uniformSymbol] = tickFXBeanMap[uniformSymbol]!!
            }
        }
        tickFXBeanMap = newTickFXBeanMap
        tickObservableList.removeIf { tickFXBean -> !uniformSymbolSet.contains(tickFXBean.tickDTO!!.contract.uniformSymbol) }
        tickTableView.sort()

        val newSelectedTickIdSet: MutableSet<String> = HashSet()
        for (tick in tickObservableList) {
            if (selectedTickUniformSymbolSet.contains(tick.tickDTO!!.contract.uniformSymbol)) {
                tickTableView.selectionModel.select(tick)
                newSelectedTickIdSet.add(tick.tickDTO!!.contract.uniformSymbol)
            }
        }
        selectedTickUniformSymbolSet = newSelectedTickIdSet
    }

}