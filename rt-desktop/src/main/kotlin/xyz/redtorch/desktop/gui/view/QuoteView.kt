package xyz.redtorch.desktop.gui.view

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.trade.dto.Contract
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.desktop.gui.state.ViewState

class QuoteView(private val viewState: ViewState) {

    private val contractSymbolText = Text("--")
    private val contractNameText = Text("--")
    private val ask5PriceText = Text("--")
    private val ask5VolumeText = Text("-")
    private val ask4PriceText = Text("--")
    private val ask4VolumeText = Text("-")
    private val ask3PriceText = Text("--")
    private val ask3VolumeText = Text("-")
    private val ask2PriceText = Text("--")
    private val ask2VolumeText = Text("-")
    private val ask1PriceText = Text("--")
    private val ask1VolumeText = Text("-")
    private val bid1PriceText = Text("--")
    private val bid1VolumeText = Text("-")
    private val bid2PriceText = Text("--")
    private val bid2VolumeText = Text("-")
    private val bid3PriceText = Text("--")
    private val bid3VolumeText = Text("-")
    private val bid4PriceText = Text("--")
    private val bid4VolumeText = Text("-")
    private val bid5PriceText = Text("--")
    private val bid5VolumeText = Text("-")
    private val lastPriceText = Text("-")
    private val pctChangeText = Text("-")
    private val volumeDeltaText = Text("-")
    private val priceDiffText = Text("-")
    private val volumeText = Text("-")
    private val openPriceText = Text("-")
    private val openInterestText = Text("-")
    private val highPriceText = Text("-")
    private val dayOpenInterestDeltaText = Text("-")
    private val lowPriceText = Text("-")
    private val preClosePriceText = Text("-")
    private val upperLimitPriceText = Text("-")
    private val preSettlePriceText = Text("-")
    private val lowerLimitPriceText = Text("-")
    private val settlePriceText = Text("-")
    private val timeText = Text("-")
    private val labelWidth = 35.0
    private val priceWidth = 100.0
    private val volumeWidth = 100.0
    private val valueWidth = 82.0
    private var tick: Tick? = null

    val view = VBox()

    init {

        val viewWidth = 240.0


        view.apply {
            prefWidth = viewWidth
            minWidth = viewWidth
            style = "-fx-border-color: rgb(200, 200, 200);-fx-border-style: solid;-fx-border-width: 0 0 0 0;"

            children.add(contractSymbolText.apply {
                style = "-fx-font-size: 16;"
                styleClass.add("trade-remind-color")
                wrappingWidth = viewWidth
                textAlignment = TextAlignment.CENTER
            })

            children.add(contractNameText.apply {
                style = "-fx-font-size: 16;"
                styleClass.add("trade-info-color")
                wrappingWidth = viewWidth
                textAlignment = TextAlignment.CENTER
            })

            val rightInsets = Insets(0.0, 2.0, 0.0, 0.0)
            val leftInsets = Insets(0.0, 0.0, 0.0, 2.0)

            children.add(HBox().apply {
                style = "-fx-border-color: rgb(200, 200, 200);-fx-border-style: dashed;-fx-border-width: 2 0 0 0;"
                children.add(Label("卖五").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(ask5PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(ask5VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("卖四").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(ask4PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(ask4VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("卖三").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(ask3PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(ask3VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("卖二").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(ask2PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(ask2VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("卖一").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(ask1PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                    style = "-fx-font-size: 14;"
                })

                children.add(ask1VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                    style = "-fx-font-size: 14;"
                })
            })

            children.add(HBox().apply {

                style = "-fx-border-color: rgb(200, 200, 200);-fx-border-style: dashed;-fx-border-width: 2 0 0 0;"

                children.add(Label("买一").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(bid1PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                    style = "-fx-font-size: 14;"
                })

                children.add(bid1VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                    style = "-fx-font-size: 14;"
                })
            })

            children.add(HBox().apply {
                children.add(Label("买二").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(bid2PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(bid2VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })


            children.add(HBox().apply {
                children.add(Label("买三").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(bid3PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(bid3VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("买四").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(bid4PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(bid4VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })


            children.add(HBox().apply {
                children.add(Label("买五").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })

                children.add(bid5PriceText.apply {
                    wrappingWidth = priceWidth
                    textAlignment = TextAlignment.RIGHT
                })

                children.add(bid5VolumeText.apply {
                    wrappingWidth = volumeWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            val labelStyle = "-fx-border-color: rgb(200, 200, 200);-fx-border-style: solid;-fx-border-width: 0 0 0 1;"

            children.add(HBox().apply {
                style = "-fx-border-color: rgb(200, 200, 200);-fx-border-style: solid;-fx-border-width: 1 0 0 0;"
                children.add(Label("最新").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(lastPriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("涨跌").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(pctChangeText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("现手").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(volumeDeltaText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("涨跌").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(priceDiffText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("总手").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(volumeText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("开盘").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(openPriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("持仓").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(openInterestText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("最高").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(highPriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("日增").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(dayOpenInterestDeltaText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("最低").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(lowPriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("昨收").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(preClosePriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("涨停").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(upperLimitPriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

            children.add(HBox().apply {
                children.add(Label("昨结").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(preSettlePriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("跌停").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(lowerLimitPriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })
            children.add(HBox().apply {
                children.add(Label("结算").apply {
                    styleClass.add("trade-label")
                    prefWidth = labelWidth
                    HBox.setMargin(this, leftInsets)
                })
                children.add(settlePriceText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                    HBox.setMargin(this, rightInsets)
                })

                children.add(Label("时间").apply {
                    styleClass.add("trade-label")
                    style = labelStyle
                    prefWidth = labelWidth
                })
                children.add(timeText.apply {
                    wrappingWidth = valueWidth
                    textAlignment = TextAlignment.RIGHT
                })
            })

        }
    }

    fun updateData(tick: Tick?) {
        // 对比内存地址
        if (tick !== this.tick || (tick == null && this.tick == null)) {
            this.tick = tick
            render()
        }
    }

    private fun render() {
        ask5PriceText.styleClass.clear()
        ask5VolumeText.styleClass.clear()
        ask4PriceText.styleClass.clear()
        ask4VolumeText.styleClass.clear()
        ask3PriceText.styleClass.clear()
        ask3VolumeText.styleClass.clear()
        ask2PriceText.styleClass.clear()
        ask2VolumeText.styleClass.clear()
        ask1PriceText.styleClass.clear()
        ask1VolumeText.styleClass.clear()
        bid1PriceText.styleClass.clear()
        bid1VolumeText.styleClass.clear()
        bid2PriceText.styleClass.clear()
        bid2VolumeText.styleClass.clear()
        bid3PriceText.styleClass.clear()
        bid3VolumeText.styleClass.clear()
        bid4PriceText.styleClass.clear()
        bid4VolumeText.styleClass.clear()
        bid5PriceText.styleClass.clear()
        bid5VolumeText.styleClass.clear()

        viewState.getSelectedContract()?.let {
            tick?.let { tk ->
                if (it.uniformSymbol != tk.contract.uniformSymbol) {
                    this.tick = null
                }
            }
        }

        if (tick == null) {
            contractSymbolText.text = "--"
            contractNameText.text = "--"

            viewState.getSelectedContract()?.let {
                contractSymbolText.text = it.symbol
                contractNameText.text = it.name
            }

            ask5PriceText.text = "--"
            ask5VolumeText.text = "-"
            ask4PriceText.text = "--"
            ask4VolumeText.text = "-"
            ask3PriceText.text = "--"
            ask3VolumeText.text = "-"
            ask2PriceText.text = "--"
            ask2VolumeText.text = "-"
            ask1PriceText.text = "--"
            ask1VolumeText.text = "-"
            bid1PriceText.text = "--"
            bid1VolumeText.text = "-"
            bid2PriceText.text = "--"
            bid2VolumeText.text = "-"
            bid3PriceText.text = "--"
            bid3VolumeText.text = "-"
            bid4PriceText.text = "--"
            bid4VolumeText.text = "-"
            bid5PriceText.text = "--"
            bid5VolumeText.text = "-"
            lastPriceText.text = "-"
            pctChangeText.text = "-"
            volumeDeltaText.text = "-"
            priceDiffText.text = "-"
            volumeText.text = "-"
            openPriceText.text = "-"
            openInterestText.text = "-"
            highPriceText.text = "-"
            dayOpenInterestDeltaText.text = "-"
            lowPriceText.text = "-"
            preClosePriceText.text = "-"
            upperLimitPriceText.text = "-"
            preSettlePriceText.text = "-"
            lowerLimitPriceText.text = "-"
            settlePriceText.text = "-"
            timeText.text = "-"
        } else {

            val validTick = tick!!

            val contract: Contract = validTick.contract
            val symbol = contract.symbol
            val name = contract.name

            var decimalDigits = CommonUtils.getNumberDecimalDigits(contract.priceTick)
            if (decimalDigits < 0) {
                decimalDigits = 4
            }

            val priceStringFormat = "%,." + decimalDigits + "f"

            contractSymbolText.text = symbol
            contractNameText.text = name

            var basePrice = validTick.preSettlePrice
            if (basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                basePrice = validTick.preClosePrice
            }
            if (basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                basePrice = validTick.openPrice
            }

            val askPriceMap: Map<String, Double> = validTick.askPriceMap
            val askVolumeMap: Map<String, Int> = validTick.askVolumeMap

            if (askPriceMap.containsKey("5") && askVolumeMap.containsKey("5")) {
                val price = askPriceMap["5"]!!
                val volume = askVolumeMap["5"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    ask5PriceText.text = String.format(priceStringFormat, price)
                    ask5VolumeText.text = "" + volume
                    ask5VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        ask5PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        ask5PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    ask5PriceText.text = "--"
                    ask5VolumeText.text = "-"
                }
            }

            if (askPriceMap.containsKey("4") && askVolumeMap.containsKey("4")) {
                val price = askPriceMap["4"]!!
                val volume = askVolumeMap["4"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    ask4PriceText.text = String.format(priceStringFormat, price)
                    ask4VolumeText.text = "" + volume
                    ask4VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        ask4PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        ask4PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    ask4PriceText.text = "--"
                    ask4VolumeText.text = "-"
                }
            }

            if (askPriceMap.containsKey("3") && askVolumeMap.containsKey("3")) {
                val price = askPriceMap["3"]!!
                val volume = askVolumeMap["3"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    ask3PriceText.text = String.format(priceStringFormat, price)
                    ask3VolumeText.text = "" + volume
                    ask3VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        ask3PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        ask3PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    ask3PriceText.text = "--"
                    ask3VolumeText.text = "-"
                }
            }

            if (askPriceMap.containsKey("2") && askVolumeMap.containsKey("2")) {
                val price = askPriceMap["2"]!!
                val volume = askVolumeMap["2"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    ask2PriceText.text = String.format(priceStringFormat, price)
                    ask2VolumeText.text = "" + volume
                    ask2VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        ask2PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        ask2PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    ask2PriceText.text = "--"
                    ask2VolumeText.text = "-"
                }
            }

            if (askPriceMap.containsKey("1") && askVolumeMap.containsKey("1")) {
                val price = askPriceMap["1"]!!
                val volume = askVolumeMap["1"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    ask1PriceText.text = String.format(priceStringFormat, price)
                    ask1VolumeText.text = "" + volume
                    ask1VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        ask1PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        ask1PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    ask1PriceText.text = "--"
                    ask1VolumeText.text = "-"
                }
            }

            val bidPriceMap: Map<String, Double> = validTick.bidPriceMap
            val bidVolumeMap: Map<String, Int> = validTick.bidVolumeMap

            if (bidPriceMap.containsKey("5") && bidVolumeMap.containsKey("5")) {
                val price = bidPriceMap["5"]!!
                val volume = bidVolumeMap["5"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    bid5PriceText.text = String.format(priceStringFormat, price)
                    bid5VolumeText.text = "" + volume
                    bid5VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        bid5PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        bid5PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    bid5PriceText.text = "--"
                    bid5VolumeText.text = "-"
                }
            }


            if (bidPriceMap.containsKey("4") && bidVolumeMap.containsKey("4")) {
                val price = bidPriceMap["4"]!!
                val volume = bidVolumeMap["4"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    bid4PriceText.text = String.format(priceStringFormat, price)
                    bid4VolumeText.text = "" + volume
                    bid4VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        bid4PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        bid4PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    bid4PriceText.text = "--"
                    bid4VolumeText.text = "-"
                }
            }


            if (bidPriceMap.containsKey("3") && bidVolumeMap.containsKey("3")) {
                val price = bidPriceMap["3"]!!
                val volume = bidVolumeMap["3"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    bid3PriceText.text = String.format(priceStringFormat, price)
                    bid3VolumeText.text = "" + volume
                    bid3VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        bid3PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        bid3PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    bid3PriceText.text = "--"
                    bid3VolumeText.text = "-"
                }
            }

            if (bidPriceMap.containsKey("2") && bidVolumeMap.containsKey("2")) {
                val price = bidPriceMap["2"]!!
                val volume = bidVolumeMap["2"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    bid2PriceText.text = String.format(priceStringFormat, price)
                    bid2VolumeText.text = "" + volume
                    bid2VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        bid2PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        bid2PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    bid2PriceText.text = "--"
                    bid2VolumeText.text = "-"
                }
            }

            if (bidPriceMap.containsKey("1") && bidVolumeMap.containsKey("1")) {
                val price = bidPriceMap["1"]!!
                val volume = bidVolumeMap["1"]!!
                if (price != 0.0 && price != Double.MAX_VALUE) {
                    bid1PriceText.text = String.format(priceStringFormat, price)
                    bid1VolumeText.text = "" + volume
                    bid1VolumeText.styleClass.add("trade-remind-color")
                    if (price > basePrice) {
                        bid1PriceText.styleClass.add("trade-long-color")
                    } else if (price < basePrice) {
                        bid1PriceText.styleClass.add("trade-short-color")
                    }
                } else {
                    bid1PriceText.text = "--"
                    bid1VolumeText.text = "-"
                }
            }


            val lastPrice = validTick.lastPrice
            lastPriceText.styleClass.clear()
            if (lastPrice == Double.MAX_VALUE) {
                lastPriceText.text = "-"
            } else {
                lastPriceText.text = String.format(priceStringFormat, lastPrice)
                if (lastPrice > basePrice) {
                    lastPriceText.styleClass.add("trade-long-color")
                } else if (lastPrice < basePrice) {
                    lastPriceText.styleClass.add("trade-short-color")
                }
            }
            pctChangeText.styleClass.clear()
            var priceDiff = 0.0
            if (lastPrice == Double.MAX_VALUE || basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                pctChangeText.text = "-"
            } else {
                priceDiff = lastPrice - basePrice
                val pctChange = priceDiff / basePrice
                pctChangeText.text = String.format("%,.2f%%", pctChange * 100)
                if (priceDiff > 0) {
                    pctChangeText.styleClass.add("trade-long-color")
                }
                if (priceDiff < 0) {
                    pctChangeText.styleClass.add("trade-short-color")
                }
            }
            val volumeDelta: Long = validTick.volumeDelta
            volumeDeltaText.text = String.format("%,d", volumeDelta)
            priceDiffText.text = String.format(priceStringFormat, priceDiff)
            priceDiffText.styleClass.clear()
            if (priceDiff > 0) {
                priceDiffText.styleClass.add("trade-long-color")
            }
            if (priceDiff < 0) {
                priceDiffText.styleClass.add("trade-short-color")
            }
            volumeText.text = String.format("%,d", validTick.volume)
            val openPrice: Double = validTick.openPrice
            openPriceText.styleClass.clear()
            if (openPrice == Double.MAX_VALUE) {
                openPriceText.text = "-"
            } else {
                openPriceText.text = String.format(priceStringFormat, openPrice)
                if (openPrice > basePrice) {
                    openPriceText.styleClass.add("trade-long-color")
                } else if (openPrice < basePrice) {
                    openPriceText.styleClass.add("trade-short-color")
                }
            }
            if (validTick.openInterest == Double.MAX_VALUE) {
                openInterestText.text = "-"
            } else {
                openInterestText.text = String.format("%,.0f", validTick.openInterest)
            }
            val highPrice: Double = validTick.highPrice
            highPriceText.styleClass.clear()
            if (highPrice == Double.MAX_VALUE) {
                highPriceText.text = "-"
            } else {
                highPriceText.text = String.format(priceStringFormat, highPrice)
                if (highPrice > basePrice) {
                    highPriceText.styleClass.add("trade-long-color")
                } else if (highPrice < basePrice) {
                    highPriceText.styleClass.add("trade-short-color")
                }
            }
            if (validTick.openInterest == Double.MAX_VALUE || validTick.preOpenInterest == Double.MAX_VALUE) {
                dayOpenInterestDeltaText.text = "-"
            } else {
                dayOpenInterestDeltaText.text = String.format("%,.0f", validTick.openInterest - validTick.preOpenInterest)
            }
            val lowPrice: Double = validTick.lowPrice
            lowPriceText.styleClass.clear()
            if (lowPrice == Double.MAX_VALUE) {
                lowPriceText.text = "-"
            } else {
                lowPriceText.text = String.format(priceStringFormat, lowPrice)
                if (lowPrice > basePrice) {
                    lowPriceText.styleClass.add("trade-long-color")
                } else if (lowPrice < basePrice) {
                    lowPriceText.styleClass.add("trade-short-color")
                }
            }
            if (validTick.preClosePrice == Double.MAX_VALUE) {
                preClosePriceText.text = "-"
            } else {
                preClosePriceText.text = String.format(priceStringFormat, validTick.preClosePrice)
            }
            if (validTick.upperLimit == Double.MAX_VALUE) {
                upperLimitPriceText.text = "-"
            } else {
                upperLimitPriceText.text = String.format(priceStringFormat, validTick.upperLimit)
            }
            if (validTick.preSettlePrice == Double.MAX_VALUE) {
                preSettlePriceText.text = "-"
            } else {
                preSettlePriceText.text = String.format(priceStringFormat, validTick.preSettlePrice)
            }
            if (validTick.lowerLimit == Double.MAX_VALUE) {
                lowerLimitPriceText.text = "-"
            } else {
                lowerLimitPriceText.text = String.format(priceStringFormat, validTick.lowerLimit)
            }
            if (validTick.settlePrice == Double.MAX_VALUE) {
                settlePriceText.text = "-"
            } else {
                settlePriceText.text = String.format(priceStringFormat, validTick.settlePrice)
            }
            timeText.text = CommonUtils.millsToLocalDateTime(validTick.actionTimestamp).format(Constant.T_FORMAT_WITH_MS_FORMATTER)
        }
    }
}