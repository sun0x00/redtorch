package xyz.redtorch.desktop.gui.view

import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.common.sync.dto.InsertOrder
import xyz.redtorch.common.trade.dto.Account
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.trade.enumeration.*
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.desktop.gui.state.ViewState
import xyz.redtorch.desktop.gui.utils.FXUtils
import java.util.*
import java.util.regex.Pattern

class PlaceOrderView(private val viewState: ViewState) {
    companion object {
        private const val PFM_Manual = "Manual" // 手动
        private const val PFM_Last = "Last" // 最新
        private const val PFM_Best = "Best" // 本方最优
        private const val PFM_CounterpartyBest = "CounterpartyBest" // 对手方最优
        private const val PFM_UpperOrLowerLimit = "UpperOrLowerLimit"
    }

    private val priceFillMethodManualRadioButton = RadioButton("手动")
    private val priceFillMethodToggleGroup = ToggleGroup()
    private val orderPriceTypeToggleGroup = ToggleGroup()
    private val priceTextField = TextField()
    private val hedgeFlagComboBox = ComboBox<HedgeFlagEnum>()
    private val timeConditionComboBox = ComboBox<TimeConditionEnum>()
    private val volumeConditionComboBox = ComboBox<VolumeConditionEnum>()
    private val minVolumeTextField = TextField("1")
    private val contingentConditionComboBox = ComboBox<ContingentConditionEnum>()
    private val stopPriceTextField = TextField()
    private val accountVolumeVBox = VBox()

    private val commonInsets = Insets(2.0, 0.0, 0.0, 0.0)

    private var orderPriceType = OrderPriceTypeEnum.LimitPrice
    private var priceFillMethod = PFM_Last
    private var price: Double? = null
    private var stopPrice: Double? = null
    private var minVolume = 1
    private var accountVolumeMap: SortedMap<String, Int> = TreeMap()
    private var tick: Tick? = null
    private var decimalDigits = 4
    private var insertPending = false

    val view = TabPane()

    init {

        priceFillMethodToggleGroup.apply {
            selectedToggleProperty().addListener { _: ObservableValue<out Toggle>?, _: Toggle?, newValue: Toggle ->
                priceFillMethod = newValue.userData as String
                if (PFM_Manual == priceFillMethod) {
                    priceTextField.isDisable = false
                    if (price == null) {
                        priceTextField.text = ""
                        tick?.let {
                            if (it.lastPrice != Double.MAX_VALUE) {
                                price = it.lastPrice
                                priceTextField.text = String.format("%." + decimalDigits + "f", price)
                            }
                        }
                    }
                } else {
                    priceTextField.isDisable = true
                    if (PFM_CounterpartyBest == priceFillMethod) {
                        price = null
                        priceTextField.text = "对手价"
                    } else if (PFM_Best == priceFillMethod) {
                        price = null
                        priceTextField.text = "排队价"
                    } else if (PFM_UpperOrLowerLimit == priceFillMethod) {
                        price = null
                        priceTextField.text = "涨跌停"
                    } else if (PFM_Last == priceFillMethod) {
                        price = null
                        priceTextField.text = ""
                        tick?.let {
                            if (it.lastPrice != Double.MAX_VALUE) {
                                price = it.lastPrice
                                priceTextField.text = String.format("%." + decimalDigits + "f", price)
                            }
                        }
                    }
                }
                render()
            }
        }

        orderPriceTypeToggleGroup.apply {
            selectedToggleProperty().addListener { _: ObservableValue<out Toggle>?, _: Toggle?, newValue: Toggle ->
                orderPriceType = newValue.userData as OrderPriceTypeEnum
            }
        }

        priceTextField.apply {
            prefWidth = 120.0
            isDisable = true
            textProperty().addListener { _: ObservableValue<out String>?, oldValue: String?, newValue: String ->
                val pattern = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?")
                if (!(newValue.isEmpty() || "-" == newValue || "." == newValue || "-." == newValue || pattern.matcher(newValue)
                        .matches() || "涨跌停" == newValue || "对手价" == newValue || "排队价" == newValue)
                ) {
                    this.text = oldValue
                } else {
                    if (newValue.isEmpty() || "-" == newValue || "." == newValue || "-." == newValue || "涨跌停" == newValue || "对手价" == newValue || "排队价" == newValue) {
                        price = null
                    } else if (pattern.matcher(newValue).matches()) {
                        price = newValue.toDouble()
                    }
                }
            }
        }

        minVolumeTextField.apply {
            prefWidth = 120.0
            textProperty().addListener { _: ObservableValue<out String>?, _: String?, newValue: String ->
                var minVolumeString = newValue
                if (!newValue.matches("\\d*".toRegex())) {
                    minVolumeString = newValue.replace("[^\\d]".toRegex(), "")
                    minVolumeTextField.text = minVolumeString
                }
                minVolume = if (minVolumeString.isBlank()) {
                    1
                } else {
                    Integer.valueOf(minVolumeString)
                }
            }
        }

        stopPriceTextField.apply {
            prefWidth = 120.0
            textProperty().addListener { _: ObservableValue<out String>?, oldValue: String?, newValue: String ->
                val pattern = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?")
                if (!(newValue.isEmpty() || "-" == newValue || "." == newValue || "-." == newValue || pattern.matcher(newValue).matches())) {
                    stopPriceTextField.text = oldValue
                } else {
                    if (newValue.isEmpty() || "-" == newValue || "." == newValue || "-." == newValue) {
                        stopPrice = null
                    } else if (pattern.matcher(newValue).matches()) {
                        stopPrice = newValue.toDouble()
                    }
                }
            }
        }


        view.apply {
            prefWidth = 420.0

            tabs.add(Tab("常规").apply {
                isClosable = false

                content = HBox().apply {

                    //下单视图左侧区域
                    children.add(ScrollPane().apply {
                        padding = Insets(2.0, 0.0, 2.0, 2.0)
                        prefWidth = 235.0
                        minWidth = 235.0
                        content = VBox().apply {
                            prefWidth = 220.0
                            minWidth = 220.0
                            style = "-fx-border-color: rgb(220, 220, 220);-fx-border-style: dashed;-fx-border-width: 0 1 0 0;"

                            /////////////////////价格类型////////////////
                            children.add(Label("价格类型").apply {
                                padding = commonInsets
                            })

                            children.add(HBox().apply {
                                padding = commonInsets
                                children.add(RadioButton("限价").apply {
                                    userData = OrderPriceTypeEnum.LimitPrice
                                    prefWidth = 60.0
                                    toggleGroup = orderPriceTypeToggleGroup
                                    isSelected = true
                                })

                                children.add(RadioButton("市价").apply {
                                    userData = OrderPriceTypeEnum.AnyPrice
                                    prefWidth = 60.0
                                    toggleGroup = orderPriceTypeToggleGroup
                                })

                                children.add(RadioButton("五档").apply {
                                    userData = OrderPriceTypeEnum.FiveLevelPrice
                                    prefWidth = 60.0
                                    toggleGroup = orderPriceTypeToggleGroup
                                })
                            })

                            children.add(HBox().apply {
                                padding = commonInsets
                                children.add(RadioButton("最优").apply {
                                    userData = OrderPriceTypeEnum.BestPrice
                                    prefWidth = 60.0
                                    toggleGroup = orderPriceTypeToggleGroup
                                })

                                children.add(RadioButton("最新").apply {
                                    userData = OrderPriceTypeEnum.LastPrice
                                    prefWidth = 60.0
                                    toggleGroup = orderPriceTypeToggleGroup
                                })

                            })

                            children.add(RadioButton("最新价浮动上浮1个ticks").apply {
                                userData = OrderPriceTypeEnum.LastPricePlusOneTicks
                                prefWidth = 200.0
                                toggleGroup = orderPriceTypeToggleGroup
                                padding = commonInsets
                            })

                            children.add(RadioButton("最新价浮动上浮3个ticks").apply {
                                userData = OrderPriceTypeEnum.LastPricePlusThreeTicks
                                prefWidth = 200.0
                                toggleGroup = orderPriceTypeToggleGroup
                                padding = commonInsets
                            })

                            /////////////////////填价方式////////////////
                            children.add(Label("填价方式").apply {
                                padding = commonInsets
                            })

                            children.add(HBox().apply {
                                padding = commonInsets
                                children.add(RadioButton("排队价").apply {
                                    userData = PFM_Best
                                    prefWidth = 60.0
                                    toggleGroup = priceFillMethodToggleGroup
                                    isSelected = true
                                })

                                children.add(RadioButton("最新").apply {
                                    userData = PFM_Last
                                    prefWidth = 60.0
                                    toggleGroup = priceFillMethodToggleGroup
                                    isSelected = true
                                })

                                children.add(RadioButton("对手").apply {
                                    userData = PFM_CounterpartyBest
                                    prefWidth = 60.0
                                    toggleGroup = priceFillMethodToggleGroup
                                })
                            })

                            children.add(HBox().apply {
                                padding = commonInsets
                                children.add(RadioButton("涨跌停").apply {
                                    userData = PFM_UpperOrLowerLimit
                                    prefWidth = 60.0
                                    toggleGroup = priceFillMethodToggleGroup
                                    isSelected = true
                                })

                                children.add(priceFillMethodManualRadioButton.apply {
                                    userData = PFM_Manual
                                    prefWidth = 60.0
                                    toggleGroup = priceFillMethodToggleGroup
                                    isSelected = true
                                })
                            })

                            /////////////////////价格////////////////
                            children.add(Label("价格").apply {
                                padding = commonInsets
                            })
                            children.add(priceTextField)
                            children.add(HBox().apply {
                                children.add(Button("-").apply {
                                    prefWidth = 109.0

                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            if (viewState.getSelectedContract() != null) {
                                                if (price == null) {
                                                    if (tick != null && tick!!.lastPrice != Double.MAX_VALUE) {
                                                        price = tick!!.lastPrice - viewState.getSelectedContract()!!.priceTick
                                                    }
                                                } else {
                                                    price = price!! - viewState.getSelectedContract()!!.priceTick
                                                }
                                                priceFillMethod = PFM_Manual
                                                priceFillMethodManualRadioButton.isSelected = true
                                                priceTextField.text = String.format("%." + decimalDigits + "f", price)
                                            }
                                        }
                                    }

                                })
                                children.add(Button("+").apply {
                                    prefWidth = 109.0

                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            if (viewState.getSelectedContract() != null) {
                                                if (price == null) {
                                                    if (tick != null && tick!!.lastPrice != Double.MAX_VALUE) {
                                                        price = tick!!.lastPrice + viewState.getSelectedContract()!!.priceTick
                                                    }
                                                } else {
                                                    price = price!! + viewState.getSelectedContract()!!.priceTick
                                                }
                                                priceFillMethod = PFM_Manual
                                                priceFillMethodManualRadioButton.isSelected = true
                                                priceTextField.text = String.format("%." + decimalDigits + "f", price)
                                            }
                                        }
                                    }

                                })
                            })

                            /////////////////////投机套保////////////////
                            children.add(Label("投机套保").apply {
                                padding = commonInsets
                            })

                            children.add(hedgeFlagComboBox.apply {
                                maxWidth = Double.MAX_VALUE
                                items = FXCollections.observableArrayList<HedgeFlagEnum>().apply {
                                    addAll(listOf(*HedgeFlagEnum.values()))
                                }
                                selectionModel.select(HedgeFlagEnum.Speculation)
                            })

                            /////////////////////时效类型////////////////
                            children.add(Label("时效类型").apply {
                                padding = commonInsets
                            })

                            children.add(timeConditionComboBox.apply {
                                maxWidth = Double.MAX_VALUE
                                items = FXCollections.observableArrayList<TimeConditionEnum>().apply {
                                    addAll(listOf(*TimeConditionEnum.values()))
                                }
                                selectionModel.select(TimeConditionEnum.GFD)
                            })


                            /////////////////////数量条件////////////////
                            children.add(Label("数量条件").apply {
                                padding = commonInsets
                            })

                            children.add(volumeConditionComboBox.apply {
                                maxWidth = Double.MAX_VALUE
                                items = FXCollections.observableArrayList<VolumeConditionEnum>().apply {
                                    addAll(listOf(*VolumeConditionEnum.values()))
                                }
                                selectionModel.select(VolumeConditionEnum.AV)
                            })

                            /////////////////////最小成交量////////////////
                            children.add(Label("最小成交量").apply {
                                padding = commonInsets
                            })

                            children.add(minVolumeTextField)

                            /////////////////////触发条件////////////////
                            children.add(Label("触发条件").apply {
                                padding = commonInsets
                            })

                            children.add(contingentConditionComboBox.apply {
                                maxWidth = Double.MAX_VALUE
                                items = FXCollections.observableArrayList<ContingentConditionEnum>().apply {
                                    addAll(listOf(*ContingentConditionEnum.values()))
                                }
                                selectionModel.select(ContingentConditionEnum.Immediately)
                            })

                            /////////////////////条件价格////////////////
                            children.add(Label("条件价格").apply {
                                padding = commonInsets
                            })

                            children.add(stopPriceTextField)

                        }
                    })

                    // 下单视图右侧,数量和下单按钮
                    children.add(VBox().apply {
                        HBox.setHgrow(this, Priority.ALWAYS)
                        padding = Insets(2.0)

                        // 账户-数量显示区域封装
                        children.add(VBox().apply {
                            VBox.setVgrow(this, Priority.ALWAYS)
                            style = "-fx-border-color: rgb(220, 220, 220);-fx-border-style: dashed;-fx-border-width: 0 1 0 0;"
                            padding = Insets(5.0, 0.0, 0.0, 0.0)
                            children.add(Text("账户数量"))
                            children.add(ScrollPane().apply {
                                minWidth = 190.0
                                maxWidth = 190.0
                                vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
                                VBox.setVgrow(this, Priority.ALWAYS)
                                content = accountVolumeVBox.apply {
                                    padding = Insets(0.0, 0.0, 0.0, 2.0)
                                }
                            })
                        })


                        val buttonStyle =
                            "-fx-light-text-color: rgb(255, 255, 255); -fx-mid-text-color: rgb(255, 255, 255); -fx-dark-text-color: rgb(255, 255, 255);"

                        children.add(
                            GridPane().apply {

                                padding = Insets(3.0, 0.0, 0.0, 5.0)
                                prefHeight = 115.0

                                maxHeight = 115.0
                                minHeight = 115.0

                                vgap = 3.0
                                hgap = 10.0

                                add(Button("多开").apply {
                                    styleClass.add("trade-long-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Buy, OffsetFlagEnum.Open)
                                        }
                                    }
                                }, 0, 0)

                                add(Button("空开").apply {
                                    styleClass.add("trade-short-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Sell, OffsetFlagEnum.Open)
                                        }
                                    }
                                }, 1, 0)


                                add(Button("平多").apply {
                                    styleClass.add("trade-short-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Sell, OffsetFlagEnum.Close)
                                        }
                                    }
                                }, 0, 1)


                                add(Button("平空").apply {
                                    styleClass.add("trade-long-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Buy, OffsetFlagEnum.Close)
                                        }
                                    }
                                }, 1, 1)


                                add(Button("平今多").apply {
                                    styleClass.add("trade-short-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Sell, OffsetFlagEnum.CloseToday)
                                        }
                                    }
                                }, 0, 2)


                                add(Button("平今空").apply {
                                    styleClass.add("trade-long-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Buy, OffsetFlagEnum.CloseToday)
                                        }
                                    }
                                }, 1, 2)

                                add(Button("平昨多").apply {
                                    styleClass.add("trade-short-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Sell, OffsetFlagEnum.CloseYesterday)
                                        }
                                    }
                                }, 0, 3)

                                add(Button("平昨空").apply {
                                    styleClass.add("trade-long-color-background")
                                    style = buttonStyle
                                    prefWidth = 70.0
                                    prefHeight = 25.0
                                    onMousePressed = EventHandler { e: MouseEvent ->
                                        if (e.button == MouseButton.PRIMARY) {
                                            submitOrder(DirectionEnum.Buy, OffsetFlagEnum.CloseYesterday)
                                        }
                                    }
                                }, 1, 3)
                            }
                        )
                    })
                }
            })
        }
    }

    fun updateAccountVolumeMap(accountVolumeMap: Map<String, Int>) {
        this.accountVolumeMap.putAll(accountVolumeMap)
        render()
    }

    fun updateData(tick: Tick?) {

        // 过滤Tick
        val contract = viewState.getSelectedContract()
        contract?.let {
            decimalDigits = CommonUtils.getNumberDecimalDigits(it.priceTick)
            tick?.let { tk ->
                if (it.uniformSymbol != tk.contract.uniformSymbol) {
                    return
                }
            }
        }

        // 如果新的数据为null或者tick新旧地址不同
        if (tick == null || tick !== this.tick) {
            this.tick = tick
            updatePrice()
        }

    }

    private fun updatePrice() {

        val contract = viewState.getSelectedContract()

        if (this.tick == null || contract == null || this.tick!!.contract.uniformSymbol != contract.uniformSymbol) {
            this.tick = null
            price = null
            priceTextField.text = ""
        }

        tick?.let {
            if (PFM_Last == priceFillMethod) {
                if (it.lastPrice != Double.MAX_VALUE) {
                    price = it.lastPrice
                    priceTextField.text = String.format("%." + decimalDigits + "f", price)
                } else {
                    price = null
                    priceTextField.text = ""
                }
            } else if (PFM_CounterpartyBest == priceFillMethod) {
                price = null
                priceTextField.text = "对手价"
            } else if (PFM_Best == priceFillMethod) {
                price = null
                priceTextField.text = "排队价"
            } else if (PFM_UpperOrLowerLimit == priceFillMethod) {
                price = null
                priceTextField.text = "涨跌停"
            }
        }

    }

    fun render() {

        val selectedAccountIdSet: Set<String> = viewState.getSelectedAccountIdSet()

        accountVolumeMap = accountVolumeMap.filterKeys { selectedAccountIdSet.contains(it) }.toSortedMap()

        accountVolumeVBox.children.clear()

        for (selectedAccountId in selectedAccountIdSet) {
            val account: Account? = viewState.queryAccountByAccountId(selectedAccountId)
            account?.let {
                val accountNameText = Text(account.name)
                val accountIdText = TextField(selectedAccountId)
                accountIdText.isEditable = false
                val accountVolumeTextField = TextField()
                if (accountVolumeMap.containsKey(selectedAccountId)) {
                    accountVolumeTextField.text = accountVolumeMap[selectedAccountId].toString() + ""
                } else {
                    accountVolumeTextField.text = 0.toString()
                }
                accountVolumeTextField.textProperty().addListener { _: ObservableValue<out String>?, _: String?, newValue: String ->
                    var volumeString = newValue
                    if (!newValue.matches("\\d*".toRegex())) {
                        volumeString = newValue.replace("[^\\d]".toRegex(), "")
                        accountVolumeTextField.text = volumeString
                    }
                    if (volumeString.isNotBlank()) {
                        val accountVolume = volumeString.toInt()
                        accountVolumeMap[selectedAccountId] = accountVolume
                    }
                }

                val accountVolumeIncreaseButton = Button("+")
                accountVolumeIncreaseButton.prefWidth = 80.0
                val accountVolumeDecreaseButton = Button("-")
                accountVolumeDecreaseButton.prefWidth = 80.0
                val accountVolumeButtonHBox = HBox()
                accountVolumeButtonHBox.children.addAll(accountVolumeDecreaseButton, accountVolumeIncreaseButton)
                accountVolumeIncreaseButton.onMousePressed = EventHandler { e: MouseEvent ->
                    if (e.button == MouseButton.PRIMARY) {
                        var accountVolume = 0
                        accountVolumeMap[selectedAccountId]?.let {
                            accountVolume = it
                        }
                        if (accountVolume < 1000000) {
                            accountVolume += 1
                        }
                        accountVolumeMap[selectedAccountId] = accountVolume
                        accountVolumeTextField.text = "" + accountVolume
                    }
                }
                accountVolumeDecreaseButton.onMousePressed = EventHandler { e: MouseEvent ->
                    if (e.button == MouseButton.PRIMARY) {
                        var accountVolume = 0
                        accountVolumeMap[selectedAccountId]?.let {
                            accountVolume = it
                        }
                        accountVolume -= 1
                        if (accountVolume < 0) {
                            accountVolume = 0
                        }
                        accountVolumeMap[selectedAccountId] = accountVolume
                        accountVolumeTextField.text = "" + accountVolume
                    }
                }
                val lineVbox = VBox()
                lineVbox.padding = commonInsets
                lineVbox.children.addAll(accountNameText, accountIdText, accountVolumeTextField, accountVolumeButtonHBox)
                accountVolumeVBox.children.add(lineVbox)
            }
        }

    }

    private fun submitOrder(direction: DirectionEnum, offsetFlag: OffsetFlagEnum) {
        if (insertPending) {
            return
        }
        insertPending = true
        val finalAccountIdSet: Set<String> = viewState.getSelectedAccountIdSet()
        if (finalAccountIdSet.isEmpty()) {
            FXUtils.showConfirmAlert("提交定单错误", "请至少选择一个账户", viewState.primaryStage!!.scene.window)
            insertPending = false
            return
        }
        val finalContract: Contract? = viewState.getSelectedContract()
        if (finalContract == null) {
            FXUtils.showConfirmAlert("提交定单错误", "请选择合约", viewState.primaryStage!!.scene.window)
            insertPending = false
            return
        }


        if (tick != null && finalContract.uniformSymbol != tick!!.contract.uniformSymbol) {
            tick = null
        }

        var finalPrice: Double? = null

        if (PFM_Manual == priceFillMethod) {
            finalPrice = price
        } else {
            tick?.let {
                if (PFM_Last == priceFillMethod) {
                    if (it.lastPrice != Double.MAX_VALUE) {
                        finalPrice = it.lastPrice
                    }
                } else if (PFM_CounterpartyBest == priceFillMethod) {
                    if (DirectionEnum.Buy == direction) {
                        if (it.askPriceMap.containsKey("1") && it.askPriceMap["1"] != Double.MAX_VALUE) {
                            finalPrice = it.askPriceMap["1"]
                        }
                    } else if (DirectionEnum.Sell == direction) {
                        if (it.bidPriceMap.containsKey("1") && it.bidPriceMap["1"] != Double.MAX_VALUE) {
                            finalPrice = it.bidPriceMap["1"]
                        }
                    }
                } else if (PFM_Best == priceFillMethod) {
                    if (DirectionEnum.Buy == direction) {
                        if (it.bidPriceMap.containsKey("1") && it.bidPriceMap["1"] != Double.MAX_VALUE) {
                            finalPrice = it.bidPriceMap["1"]
                        }
                    } else if (DirectionEnum.Sell == direction) {
                        if (it.askPriceMap.containsKey("1") && it.askPriceMap["1"] != Double.MAX_VALUE) {
                            finalPrice = it.askPriceMap["1"]
                        }
                    }
                } else if (PFM_UpperOrLowerLimit == priceFillMethod) {
                    if (DirectionEnum.Buy == direction) {
                        if (it.upperLimit != Double.MAX_VALUE) {
                            finalPrice = it.upperLimit
                        }
                    } else if (DirectionEnum.Sell == direction) {
                        if (it.lowerLimit != Double.MAX_VALUE) {
                            finalPrice = it.lowerLimit
                        }
                    }
                }
            }
        }


        if (orderPriceType == OrderPriceTypeEnum.LimitPrice) {
            if (finalPrice == null || finalPrice == Double.MAX_VALUE) {
                FXUtils.showConfirmAlert("提交定单错误", "无法获取到正确的价格!", viewState.primaryStage!!.scene.window)
                insertPending = false
                return
            }
        } else {
            if (finalPrice == null) {
                finalPrice = 0.0
            }
        }
        val contingentCondition = contingentConditionComboBox.selectionModel.selectedItem
        if (contingentCondition != ContingentConditionEnum.Immediately) {
            if (stopPrice == null || stopPrice == Double.MAX_VALUE) {
                FXUtils.showConfirmAlert("提交定单错误", "无法获取到正确的条件价格!", viewState.primaryStage!!.scene.window)
                insertPending = false
                return
            }
        } else if (stopPrice == null) {
            stopPrice = 0.0
        }
        val insertOrder = InsertOrder()
        insertOrder.contract = finalContract
        insertOrder.currency = finalContract.currency
        insertOrder.direction = direction
        insertOrder.offsetFlag = offsetFlag
        insertOrder.orderPriceType = orderPriceType
        insertOrder.timeCondition = timeConditionComboBox.selectionModel.selectedItem
        insertOrder.price = finalPrice!!
        insertOrder.minVolume = minVolume
        insertOrder.stopPrice = stopPrice ?: 0.0
        insertOrder.volumeCondition = volumeConditionComboBox.selectionModel.selectedItem
        insertOrder.contingentCondition = contingentCondition
        insertOrder.hedgeFlag = hedgeFlagComboBox.selectionModel.selectedItem
        insertOrder.forceCloseReason = ForceCloseReasonEnum.NotForceClose
        val res = FXUtils.showConfirmAlert("提交定单确认", "合约=${finalContract.uniformSymbol},方向=${direction},价格${finalPrice}", viewState.primaryStage!!.scene.window)
        if (res) {
            for (accountId in finalAccountIdSet) {
                val account: Account? = viewState.queryAccountByAccountId(accountId)
                if (account == null) {
                    val accountAlert = Alert(Alert.AlertType.ERROR)
                    accountAlert.title = "错误"
                    accountAlert.headerText = "提交定单错误"
                    accountAlert.contentText = "无法找到指定账户!账户ID=$accountId"
                    accountAlert.show()
                } else {
                    insertOrder.accountCode = account.code
                    insertOrder.gatewayId = account.gatewayId
                    accountVolumeMap[accountId]?.let {
                        if (it != 0) {
                            insertOrder.volume = it
                            insertOrder.originOrderId = UUID.randomUUID().toString()
                            viewState.submitOrder(insertOrder)
                        }
                    }
                }
            }
        }
        insertPending = false
    }

}