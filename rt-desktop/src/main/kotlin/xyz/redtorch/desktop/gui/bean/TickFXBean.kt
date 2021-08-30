package xyz.redtorch.desktop.gui.bean

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.trade.dto.Tick
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.common.utils.CommonUtils.millsToLocalDateTime

class TickFXBean(tickDTO: Tick, contractSelectedFlag: Boolean) {

    private val contract = SimpleObjectProperty<Pane>()
    private val lastPrice = SimpleObjectProperty<Pane>()
    private val abPrice = SimpleObjectProperty<Pane>()
    private val abVolume = SimpleObjectProperty<Pane>()
    private val volume = SimpleObjectProperty<Pane>()
    private val openInterest = SimpleObjectProperty<Pane>()
    private val limit = SimpleObjectProperty<Pane>()
    private val actionTime = SimpleStringProperty()

    private var decimalDigits = 4

    var tickDTO: Tick? = null
        private set
    private var contractSelectedFlag = false

    init {
        update(tickDTO, contractSelectedFlag)
    }

    fun update(newTickDTO: Tick, newContractSelectedFlag: Boolean) {

        if (tickDTO == null) {
            updateContract(newTickDTO)
        }

        if (contractSelectedFlag != newContractSelectedFlag) {
            contractSelectedFlag = newContractSelectedFlag
            updateContract(newTickDTO)
        }

        // 对比内存地址
        if (newTickDTO !== tickDTO) {
            updateLastPrice(newTickDTO)
            updateAbPrice(newTickDTO)
            updateAbVolume(newTickDTO)
            updateVolume(newTickDTO)
            updateOpenInterest(newTickDTO)
            updateLimit(newTickDTO)
            updateActionTime(newTickDTO)
            tickDTO = newTickDTO
        }
    }

    private fun updateContract(newTickDTO: Tick) {
        val vBox = VBox().apply {
            children.add(Text(newTickDTO.contract.uniformSymbol).apply {
                // 选中合约高亮
                if (contractSelectedFlag) {
                    styleClass.add("trade-remind-color")
                }
            })
            children.add(
                Text(newTickDTO.contract.name)
            )
            userData = newTickDTO
        }
        setContract(vBox)
    }

    private fun updateLastPrice(newTickDTO: Tick) {
        if (tickDTO == null || CommonUtils.isNotEquals(newTickDTO.lastPrice, tickDTO!!.lastPrice)) {

            if (tickDTO == null) {
                decimalDigits = CommonUtils.getNumberDecimalDigits(newTickDTO.contract.priceTick)
            }

            var basePrice = newTickDTO.preSettlePrice
            if (basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                basePrice = newTickDTO.preClosePrice
            }
            if (basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                basePrice = newTickDTO.openPrice
            }
            val lastPrice = newTickDTO.lastPrice
            val priceDiff: Double
            val lastPriceStr: String
            var pctChangeStr = "-"
            var colorStyleClass = ""
            if (lastPrice == Double.MAX_VALUE || basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                lastPriceStr = "-"
            } else {
                priceDiff = lastPrice - basePrice
                val pctChange = priceDiff / basePrice
                pctChangeStr = CommonUtils.formatDouble(pctChange * 100, "%,.2f%%")
                lastPriceStr = CommonUtils.formatDouble(lastPrice, decimalDigits)
                if (priceDiff > 0) {
                    colorStyleClass = "trade-long-color"
                }
                if (priceDiff < 0) {
                    colorStyleClass = "trade-short-color"
                }
            }

            val vBox = VBox().apply {
                children.add(HBox().apply {
                    children.add(Text("最新").apply {
                        wrappingWidth = 35.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(lastPriceStr).apply {
                        styleClass.add(colorStyleClass)
                    })
                })

                children.add(HBox().apply {
                    children.add(Text("涨跌").apply {
                        wrappingWidth = 35.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(pctChangeStr).apply {
                        styleClass.add(colorStyleClass)
                    })
                })

                userData = newTickDTO
            }

            setLastPrice(vBox)
        }
    }

    private fun updateAbPrice(newTickDTO: Tick) {
        if (tickDTO == null || tickDTO!!.actionTime != newTickDTO.actionTime || tickDTO!!.volume != newTickDTO.volume) {

            var basePrice = newTickDTO.preSettlePrice
            if (basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                basePrice = newTickDTO.preClosePrice
            }
            if (basePrice == 0.0 || basePrice == Double.MAX_VALUE) {
                basePrice = newTickDTO.openPrice
            }
            var askPrice1Str = "-"
            var bidPrice1Str = "-"
            var askPrice1ColorStyleClass = ""
            var bidPrice1ColorStyleClass = ""

            if (newTickDTO.askPriceMap["1"] != null && newTickDTO.askPriceMap["1"] != Double.MAX_VALUE) {
                askPrice1Str = CommonUtils.formatDouble(newTickDTO.askPriceMap["1"]!!, decimalDigits)
                if (newTickDTO.askPriceMap["1"]!! > basePrice) {
                    askPrice1ColorStyleClass = "trade-long-color"
                } else if (newTickDTO.askPriceMap["1"]!! < basePrice) {
                    askPrice1ColorStyleClass = "trade-short-color"
                }
            }
            if (newTickDTO.bidPriceMap["1"] != null && newTickDTO.bidPriceMap["1"] != Double.MAX_VALUE) {
                bidPrice1Str = CommonUtils.formatDouble(newTickDTO.bidPriceMap["1"]!!, decimalDigits)
                if (newTickDTO.bidPriceMap["1"]!! > basePrice) {
                    bidPrice1ColorStyleClass = "trade-long-color"
                } else if (newTickDTO.bidPriceMap["1"]!! < basePrice) {
                    bidPrice1ColorStyleClass = "trade-short-color"
                }
            }

            val vBox = VBox().apply {
                children.add(Text(askPrice1Str).apply {
                    styleClass.add(askPrice1ColorStyleClass)
                })
                children.add(Text(bidPrice1Str).apply {
                    styleClass.add(bidPrice1ColorStyleClass)
                })
                userData = newTickDTO
            }
            setAbPrice(vBox)
        }
    }

    fun updateAbVolume(newTickDTO: Tick) {
        if (tickDTO == null || tickDTO!!.actionTime != newTickDTO.actionTime || tickDTO!!.volume != newTickDTO.volume) {

            var askVolume1Str = "-"
            var bidVolume1Str = "-"
            if (newTickDTO.askVolumeMap["1"] != null) {
                askVolume1Str = "" + newTickDTO.askVolumeMap["1"]
            }
            if (newTickDTO.bidVolumeMap["1"] != null) {
                bidVolume1Str = "" + newTickDTO.bidVolumeMap["1"]
            }

            val vBox = VBox().apply {
                children.add(Text(askVolume1Str).apply {
                    styleClass.add("trade-remind-color")
                })
                children.add(Text(bidVolume1Str).apply {
                    styleClass.add("trade-remind-color")
                })
                userData = newTickDTO
            }
            setAbVolume(vBox)
        }
    }

    fun updateVolume(newTickDTO: Tick) {
        if (tickDTO == null || tickDTO!!.volume != newTickDTO.volume) {
            val vBox = VBox()
            vBox.children.addAll(Text(newTickDTO.volume.toString()), Text(newTickDTO.volumeDelta.toString()))
            vBox.userData = newTickDTO
            setVolume(vBox)
        }
    }

    fun updateOpenInterest(newTickDTO: Tick) {
        if (tickDTO == null || CommonUtils.isNotEquals(tickDTO!!.openInterest, newTickDTO.openInterest)) {
            val vBox = VBox()
            vBox.children.addAll(Text(newTickDTO.openInterest.toString()), Text(newTickDTO.openInterestDelta.toString()))
            vBox.userData = newTickDTO
            setOpenInterest(vBox)
        }
    }

    fun updateLimit(newTickDTO: Tick) {
        if (tickDTO == null || CommonUtils.isNotEquals(tickDTO!!.upperLimit, newTickDTO.upperLimit)
            || CommonUtils.isNotEquals(tickDTO!!.lowerLimit, newTickDTO.lowerLimit)
        ) {

            var upperLimitStr = "-"
            var lowerLimitStr = "-"
            if (newTickDTO.upperLimit != Double.MAX_VALUE) {
                upperLimitStr = CommonUtils.formatDouble(newTickDTO.upperLimit, decimalDigits)
            }
            if (newTickDTO.lowerLimit != Double.MAX_VALUE) {
                lowerLimitStr = CommonUtils.formatDouble(newTickDTO.lowerLimit, decimalDigits)
            }

            val vBox = VBox().apply {
                children.add(Text(upperLimitStr).apply {
                    styleClass.add("trade-long-color")
                })
                children.add(Text(lowerLimitStr).apply {
                    styleClass.add("trade-short-color")
                })
                userData = newTickDTO
            }

            setLimit(vBox)
        }
    }

    fun updateActionTime(newTickDTO: Tick) {
        if (tickDTO == null || tickDTO!!.actionTime != newTickDTO.actionTime) {
            setActionTime(millsToLocalDateTime(newTickDTO.actionTimestamp).format(Constant.T_FORMAT_WITH_MS_FORMATTER))
        }
    }

    fun getContract(): Pane {
        return contract.get()
    }

    fun setContract(contract: Pane) {
        this.contract.set(contract)
    }

    fun contractProperty(): SimpleObjectProperty<Pane> {
        return contract
    }

    fun getLastPrice(): Pane {
        return lastPrice.get()
    }

    fun setLastPrice(lastPrice: Pane) {
        this.lastPrice.set(lastPrice)
    }

    fun lastPriceProperty(): SimpleObjectProperty<Pane> {
        return lastPrice
    }

    fun getAbPrice(): Pane {
        return abPrice.get()
    }

    fun setAbPrice(abPrice: Pane) {
        this.abPrice.set(abPrice)
    }

    fun abPriceProperty(): SimpleObjectProperty<Pane> {
        return abPrice
    }

    fun getAbVolume(): Pane {
        return abVolume.get()
    }

    fun setAbVolume(abVolume: Pane) {
        this.abVolume.set(abVolume)
    }

    fun abVolumeProperty(): SimpleObjectProperty<Pane> {
        return abVolume
    }

    fun getVolume(): Pane {
        return volume.get()
    }

    fun setVolume(volume: Pane) {
        this.volume.set(volume)
    }

    fun volumeProperty(): SimpleObjectProperty<Pane> {
        return volume
    }

    fun getOpenInterest(): Pane {
        return openInterest.get()
    }

    fun setOpenInterest(openInterest: Pane) {
        this.openInterest.set(openInterest)
    }

    fun openInterestProperty(): SimpleObjectProperty<Pane> {
        return openInterest
    }

    fun getLimit(): Pane {
        return limit.get()
    }

    fun setLimit(limit: Pane) {
        this.limit.set(limit)
    }

    fun limitProperty(): SimpleObjectProperty<Pane> {
        return limit
    }

    fun getActionTime(): String {
        return actionTime.get()
    }

    fun setActionTime(actionTime: String?) {
        this.actionTime.set(actionTime)
    }

    fun actionTimeProperty(): SimpleStringProperty {
        return actionTime
    }

}