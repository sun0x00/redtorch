package xyz.redtorch.desktop.gui.view

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.enumeration.CurrencyEnum
import xyz.redtorch.common.trade.enumeration.ExchangeEnum
import xyz.redtorch.common.trade.enumeration.ProductClassEnum
import xyz.redtorch.desktop.gui.state.ViewState
import java.util.*

class ContractTableView(private val viewState: ViewState) {

    companion object {
        private val logger = LoggerFactory.getLogger(ContractTableView::class.java)
    }

    private val contractObservableList = FXCollections.observableArrayList<Contract>()
    private var contractList: List<Contract> = ArrayList()
    private val contractTableView = TableView<Contract>()
    private var selectedContractUniformSymbolSet: MutableSet<String> = HashSet()
    private var filterExchange = "全部"
    private var filterProductType = "全部"
    private var filterCurrency = "全部"
    private var filterFuzzySymbol = ""
    private var filterLastTradeDateOrContractMonth = ""
    private var filterThirdPartyId = ""
    private var filterUnderlyingSymbol = ""
    private var filterName = ""

    val view = VBox()

    init {
        contractTableView.isTableMenuButtonVisible = true
        val uniformSymbolCol = TableColumn<Contract, String>("统一标识")
        uniformSymbolCol.prefWidth = 100.0
        uniformSymbolCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var uniformSymbol = ""
            try {
                uniformSymbol = feature.value.uniformSymbol
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(uniformSymbol)
        }
        uniformSymbolCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(uniformSymbolCol)
        val shortNameCol = TableColumn<Contract, String>("简称")
        shortNameCol.prefWidth = 80.0
        shortNameCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var shortName = ""
            try {
                shortName = feature.value.name
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(shortName)
        }
        shortNameCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(shortNameCol)
        val fullNameCol = TableColumn<Contract, String>("完整名称")
        fullNameCol.prefWidth = 100.0
        fullNameCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var fullName = ""
            try {
                fullName = feature.value.fullName
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(fullName)
        }
        fullNameCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(fullNameCol)

        val productTypeCol = TableColumn<Contract, String>("产品类型")
        productTypeCol.prefWidth = 70.0
        productTypeCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var productType = ""
            try {
                productType = feature.value.productClass.toString()
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(productType)
        }
        productTypeCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(productTypeCol)

        val multiplierCol = TableColumn<Contract, Double>("合约乘数")
        multiplierCol.prefWidth = 70.0
        multiplierCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, Double> ->
            var multiplier = 1.0
            try {
                multiplier = feature.value.multiplier
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleDoubleProperty(multiplier).asObject()
        }
        multiplierCol.comparator = Comparator.comparingDouble { d: Double? -> d!! }
        contractTableView.columns.add(multiplierCol)
        val priceTickCol = TableColumn<Contract, Double>("最小变动价位")
        priceTickCol.prefWidth = 90.0
        priceTickCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, Double> ->
            var priceTick = 0.001
            try {
                priceTick = feature.value.priceTick
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleDoubleProperty(priceTick).asObject()
        }
        priceTickCol.setComparator { d1: Double?, d2: Double? -> d1!!.compareTo(d2!!) }
        contractTableView.columns.add(priceTickCol)
        val maxLimitOrderVolumeCol = TableColumn<Contract, Int>("最大限价报单手数")
        maxLimitOrderVolumeCol.prefWidth = 100.0
        maxLimitOrderVolumeCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, Int> ->
            var maxLimitOrderVolume = 0
            try {
                maxLimitOrderVolume = feature.value.maxLimitOrderVolume
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleIntegerProperty(maxLimitOrderVolume).asObject()
        }
        maxLimitOrderVolumeCol.comparator = Comparator.comparingInt { i: Int? -> i!! }
        contractTableView.columns.add(maxLimitOrderVolumeCol)
        val maxMarketOrderVolumeCol = TableColumn<Contract, Int>("最大市价报单手数")
        maxMarketOrderVolumeCol.prefWidth = 100.0
        maxMarketOrderVolumeCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, Int> ->
            var maxMarketOrderVolume = 0
            try {
                maxMarketOrderVolume = feature.value.maxMarketOrderVolume
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleIntegerProperty(maxMarketOrderVolume).asObject()
        }
        maxMarketOrderVolumeCol.comparator = Comparator.comparingInt { i: Int? -> i!! }
        contractTableView.columns.add(maxMarketOrderVolumeCol)

        val lastTradeDateOrContractMonthCol = TableColumn<Contract, String>("合约月或最后交易日")
        lastTradeDateOrContractMonthCol.prefWidth = 120.0
        lastTradeDateOrContractMonthCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var lastTradeDateOrContractMonth = ""
            try {
                lastTradeDateOrContractMonth = feature.value.lastTradeDateOrContractMonth
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(lastTradeDateOrContractMonth)
        }
        lastTradeDateOrContractMonthCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(lastTradeDateOrContractMonthCol)
        val currencyCol = TableColumn<Contract, String>("币种")
        currencyCol.prefWidth = 40.0
        currencyCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var currency = ""
            try {
                currency = feature.value.currency.toString()
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(currency)
        }
        currencyCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(currencyCol)
        val thirdPartyIdCol = TableColumn<Contract, String>("第三方ID")
        thirdPartyIdCol.prefWidth = 100.0
        thirdPartyIdCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var thirdPartyId = ""
            try {
                thirdPartyId = feature.value.thirdPartyId
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(thirdPartyId)
        }
        thirdPartyIdCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(thirdPartyIdCol)

        val optionTypeCol = TableColumn<Contract, String>("期权类型")
        optionTypeCol.prefWidth = 100.0
        optionTypeCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var optionType = ""
            try {
                optionType = feature.value.optionsType.toString()
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(optionType)
        }
        optionTypeCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(optionTypeCol)
        val underlyingSymbolCol = TableColumn<Contract, String>("基础商品代码")
        underlyingSymbolCol.prefWidth = 80.0
        underlyingSymbolCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, String> ->
            var underlyingSymbol = ""
            try {
                underlyingSymbol = feature.value.underlyingSymbol
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleStringProperty(underlyingSymbol)
        }
        underlyingSymbolCol.setComparator { s1, s2 -> s1.compareTo(s2) }
        contractTableView.columns.add(underlyingSymbolCol)
        val underlyingMultiplierCol = TableColumn<Contract, Double>("基础商品乘数")
        underlyingMultiplierCol.prefWidth = 80.0
        underlyingMultiplierCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, Double> ->
            var underlyingMultiplier = 1.0
            try {
                underlyingMultiplier = feature.value.underlyingMultiplier
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleDoubleProperty(underlyingMultiplier).asObject()
        }
        underlyingMultiplierCol.setComparator { d1: Double?, d2: Double? -> d1!!.compareTo(d2!!) }
        contractTableView.columns.add(underlyingMultiplierCol)
        val strikePriceCol = TableColumn<Contract, Double>("执行价")
        strikePriceCol.prefWidth = 80.0
        strikePriceCol.setCellValueFactory { feature: TableColumn.CellDataFeatures<Contract, Double> ->
            var strikePrice = 1.0
            try {
                strikePrice = feature.value.strikePrice
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            SimpleDoubleProperty(strikePrice).asObject()
        }
        strikePriceCol.setComparator { d1: Double?, d2: Double? -> d1!!.compareTo(d2!!) }
        contractTableView.columns.add(strikePriceCol)
        val sortedItems = SortedList(contractObservableList)
        contractTableView.items = sortedItems
        sortedItems.comparatorProperty().bind(contractTableView.comparatorProperty())
        contractTableView.sortOrder.add(uniformSymbolCol)
        contractTableView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        contractTableView.onMousePressed = EventHandler {
            val selectedItems = contractTableView.selectionModel.selectedItems
            selectedContractUniformSymbolSet.clear()
            for (row in selectedItems) {
                selectedContractUniformSymbolSet.add(row.uniformSymbol)
            }
        }
        contractTableView.setRowFactory {
            val row = TableRow<Contract>()
            row.onMousePressed = EventHandler { event: MouseEvent ->
                if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                    val selectedItems = contractTableView.selectionModel.selectedItems
                    selectedContractUniformSymbolSet.clear()
                    for (contract in selectedItems) {
                        selectedContractUniformSymbolSet.add(contract.uniformSymbol)
                    }
                    val clickedItem = row.item
                    viewState.updateSelectedContract(clickedItem)
                }
            }
            row
        }
        contractTableView.isFocusTraversable = false
        val filterHBox = HBox()
        val filterCol1VBox = VBox()
        val filterCol2VBox = VBox()
        val filterCol3VBox = VBox()
        val commonInsets = Insets(5.0)
        filterCol1VBox.padding = commonInsets
        filterCol2VBox.padding = commonInsets
        filterCol3VBox.padding = commonInsets
        filterHBox.children.addAll(filterCol1VBox, filterCol2VBox, filterCol3VBox)
        val exchangeComboBox = ComboBox<String>()
        exchangeComboBox.valueProperty().addListener { _: ObservableValue<out String>?, _: String?, newValue: String ->
            filterExchange = newValue
            render()
        }
        val exchangeObservableList = FXCollections.observableArrayList<String>()
        exchangeObservableList.add("全部")
        for (i in 0 until ExchangeEnum.values().size) {
            val exchange: ExchangeEnum = ExchangeEnum.values()[i]
            exchangeObservableList.add(exchange.toString())
        }
        exchangeComboBox.items = exchangeObservableList
        exchangeComboBox.selectionModel.selectFirst()
        exchangeComboBox.maxWidth = Double.MAX_VALUE
        filterCol1VBox.children.addAll(Label("交易所"), exchangeComboBox)
        val fuzzySymbolTextField = TextField()
        fuzzySymbolTextField.textProperty().addListener { _, _, newValue ->
            filterFuzzySymbol = newValue
            render()
        }
        filterCol1VBox.children.addAll(Label("代码"), fuzzySymbolTextField)
        val underlyingSymbolTextField = TextField()
        underlyingSymbolTextField.textProperty().addListener { _, _, newValue ->
            filterUnderlyingSymbol = newValue
            render()
        }
        filterCol1VBox.children.addAll(Label("基础商品代码"), underlyingSymbolTextField)
        val productTypeComboBox = ComboBox<String>()
        productTypeComboBox.valueProperty().addListener { _: ObservableValue<out String>?, _: String?, newValue: String ->
            filterProductType = newValue
            render()
        }
        val productTypeObservableList = FXCollections.observableArrayList<String>()
        productTypeObservableList.add("全部")
        for (i in 0 until ProductClassEnum.values().size) {
            val productType: ProductClassEnum = ProductClassEnum.values()[i]
            productTypeObservableList.add(productType.toString())
        }
        productTypeComboBox.items = productTypeObservableList
        productTypeComboBox.selectionModel.selectFirst()
        productTypeComboBox.maxWidth = Double.MAX_VALUE

        filterCol2VBox.children.addAll(Label("产品类型"), productTypeComboBox)
        val lastTradeDateOrContractMonthTextField = TextField()
        lastTradeDateOrContractMonthTextField.textProperty().addListener { _, _, newValue ->
            filterLastTradeDateOrContractMonth = newValue
            render()
        }
        filterCol2VBox.children.addAll(Label("最后交易日或合约月"), lastTradeDateOrContractMonthTextField)
        val nameTextField = TextField()
        nameTextField.textProperty().addListener { _, _, newValue ->
            filterName = newValue
            render()
        }
        filterCol2VBox.children.addAll(Label("名称"), nameTextField)
        val currencyComboBox = ComboBox<String>()
        currencyComboBox.valueProperty().addListener { _: ObservableValue<out String>?, _: String?, newValue: String ->
            filterCurrency = newValue
            render()
        }
        val currencyObservableList = FXCollections.observableArrayList<String>()
        currencyObservableList.add("全部")
        for (i in 0 until CurrencyEnum.values().size) {
            val currency: CurrencyEnum = CurrencyEnum.values()[i]
            currencyObservableList.add(currency.toString())
        }
        currencyComboBox.items = currencyObservableList
        currencyComboBox.selectionModel.selectFirst()
        currencyComboBox.maxWidth = Double.MAX_VALUE

        filterCol3VBox.children.addAll(Label("币种"), currencyComboBox)
        val thirdPartyIdTextField = TextField()
        thirdPartyIdTextField.textProperty().addListener { _, _, newValue ->
            filterThirdPartyId = newValue
            render()
        }
        filterCol3VBox.children.addAll(Label("第三方ID"), thirdPartyIdTextField)
        val searchButton = Button("搜寻")
        filterCol3VBox.children.addAll(Label(""), searchButton)
        view.children.add(filterHBox)
        view.children.add(contractTableView)
        VBox.setVgrow(contractTableView, Priority.ALWAYS)
        view.minWidth = 1.0
    }


    fun updateData(contractList: List<Contract>) {
        if (HashSet(this.contractList) != HashSet(contractList)) {
            this.contractList = contractList
            render()
        }
    }

    fun render() {
        var newContractList: MutableList<Contract> = ArrayList()
        if (filterCurrency == "全部" && filterExchange == "全部" && filterProductType == "全部" && filterFuzzySymbol == ""
            && filterLastTradeDateOrContractMonth.isEmpty() && filterThirdPartyId.isEmpty() && filterName.isEmpty() && filterUnderlyingSymbol.isEmpty()
        ) {
            newContractList = contractList.toMutableList()
        } else {
            for (contract in contractList) {
                var flag: Boolean = filterCurrency == "全部" || contract.currency.toString() == filterCurrency
                flag = flag && (filterExchange == "全部" || contract.exchange.toString() == filterExchange)
                flag = flag && (filterProductType == "全部" || contract.productClass.toString() == filterProductType)
                flag = flag && (filterFuzzySymbol.isEmpty() || contract.symbol.lowercase(Locale.getDefault())
                    .contains(filterFuzzySymbol.lowercase(Locale.getDefault())))
                flag = flag && (filterLastTradeDateOrContractMonth.isEmpty() || contract.lastTradeDateOrContractMonth.contains(
                    filterLastTradeDateOrContractMonth
                ))
                flag = flag && (filterUnderlyingSymbol.isEmpty() || contract.underlyingSymbol.lowercase(Locale.getDefault())
                    .contains(filterUnderlyingSymbol.lowercase(Locale.getDefault())))
                flag = flag && (filterThirdPartyId.isEmpty() || contract.thirdPartyId.lowercase(Locale.getDefault())
                    .contains(filterThirdPartyId.lowercase(Locale.getDefault())))
                flag = flag && (filterName.isEmpty() || contract.fullName.lowercase(Locale.getDefault()).contains(filterName.lowercase(Locale.getDefault()))
                        || contract.name.lowercase(Locale.getDefault()).contains(filterName.lowercase(Locale.getDefault())))
                if (flag) {
                    newContractList.add(contract)
                }
            }
        }
        contractTableView.selectionModel.clearSelection()
        contractObservableList.clear()
        contractObservableList.addAll(newContractList)
        val newSelectedContractIdSet: MutableSet<String> = HashSet()
        for (contract in contractObservableList) {
            if (selectedContractUniformSymbolSet.contains(contract.uniformSymbol)) {
                contractTableView.selectionModel.select(contract)
                newSelectedContractIdSet.add(contract.uniformSymbol)
            }
        }
        selectedContractUniformSymbolSet = newSelectedContractIdSet
    }
}