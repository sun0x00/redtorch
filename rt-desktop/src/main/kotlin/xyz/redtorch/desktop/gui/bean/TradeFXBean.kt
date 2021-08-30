package xyz.redtorch.desktop.gui.bean

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.common.trade.dto.Trade
import xyz.redtorch.common.trade.enumeration.DirectionEnum
import xyz.redtorch.common.trade.enumeration.HedgeFlagEnum
import xyz.redtorch.common.trade.enumeration.OffsetFlagEnum
import xyz.redtorch.common.utils.CommonUtils

class TradeFXBean(trade: Trade, contractSelectedFlag: Boolean) {

    private val tradeId = SimpleStringProperty()
    private val contract = SimpleObjectProperty<Pane>()
    private val direction = SimpleObjectProperty<Text>()
    private val offsetFlag = SimpleStringProperty()
    private val hedgeFlag = SimpleStringProperty()
    private val price = SimpleStringProperty()
    private val volume = SimpleIntegerProperty()
    private val tradeTime = SimpleStringProperty()
    private val adapterTradeId = SimpleStringProperty()
    private val originOrderId = SimpleStringProperty()
    private val accountId = SimpleStringProperty()
    private var decimalDigits = 4
    private var contractSelectedFlag = false
    var tradeDTO: Trade? = null
        private set

    init {
        update(trade, contractSelectedFlag)
    }

    fun update(newTradeDTO: Trade, newContractSelectedFlag: Boolean) {
        if (tradeDTO == null) {
            decimalDigits = CommonUtils.getNumberDecimalDigits(newTradeDTO.contract.priceTick)
            updateTradeId(newTradeDTO)
            updateDirection(newTradeDTO)
            updateOffsetFlag(newTradeDTO)
            updateHedgeFlag(newTradeDTO)
            updateContract(newTradeDTO)
            updateTradeTime(newTradeDTO)
            updateAdapterTradeId(newTradeDTO)
            updateAccountId(newTradeDTO)
        }
        if (contractSelectedFlag != newContractSelectedFlag) {
            contractSelectedFlag = newContractSelectedFlag
            updateContract(newTradeDTO)
        }
        // 对比内存地址
        if (newTradeDTO !== tradeDTO) {
            updateChangeable(newTradeDTO)
            tradeDTO = newTradeDTO
        }
    }

    private fun updateChangeable(newTradeDTO: Trade) {
        updatePrice(newTradeDTO)
        updateVolume(newTradeDTO)
        updateOriginOrderId(newTradeDTO)
    }

    private fun updateTradeId(newTradeDTO: Trade) {
        setTradeId(newTradeDTO.tradeId)
    }

    private fun updateContract(newTradeDTO: Trade) {
        val vBox = VBox().apply {
            children.add(Text(newTradeDTO.contract.uniformSymbol).apply {
                // 选中合约高亮
                if (contractSelectedFlag) {
                    styleClass.add("trade-remind-color")
                }
            })
            children.add(
                Text(newTradeDTO.contract.name)
            )
            userData = newTradeDTO
        }
        setContract(vBox)
    }

    private fun updateDirection(newTradeDTO: Trade) {
        val directionText = Text()
        when (newTradeDTO.direction) {
            DirectionEnum.Buy -> {
                directionText.text = "多"
                directionText.styleClass.add("trade-long-color")
            }
            DirectionEnum.Sell -> {
                directionText.text = "空"
                directionText.styleClass.add("trade-short-color")
            }
            DirectionEnum.Unknown -> {
                directionText.text = "未知"
            }
            else -> {
                directionText.text = newTradeDTO.direction.toString()
            }
        }

        directionText.userData = newTradeDTO
        setDirection(directionText)
    }

    private fun updateOffsetFlag(newTradeDTO: Trade) {
        val offsetFlag = when (newTradeDTO.offsetFlag) {
            OffsetFlagEnum.Close -> {
                "平"
            }
            OffsetFlagEnum.CloseToday -> {
                "平今"
            }
            OffsetFlagEnum.CloseYesterday -> {
                "平昨"
            }
            OffsetFlagEnum.Open -> {
                "开"
            }
            OffsetFlagEnum.Unknown -> {
                "未知"
            }
            else -> {
                newTradeDTO.offsetFlag.toString()
            }
        }
        setOffsetFlag(offsetFlag)
    }

    private fun updateHedgeFlag(newTradeDTO: Trade) {
        val hedgeFlag = when (newTradeDTO.hedgeFlag) {
            HedgeFlagEnum.Speculation -> {
                "投机"
            }
            HedgeFlagEnum.Hedge -> {
                "套保"
            }
            HedgeFlagEnum.Arbitrage -> {
                "套利"
            }
            HedgeFlagEnum.MarketMaker -> {
                "做市商"
            }
            HedgeFlagEnum.SpecHedge -> {
                "第一条腿投机第二条腿套保 大商所专用"
            }
            HedgeFlagEnum.HedgeSpec -> {
                "第一条腿套保第二条腿投机 大商所专用"
            }
            HedgeFlagEnum.Unknown -> {
                "未知"
            }
            else -> {
                newTradeDTO.hedgeFlag.toString()
            }
        }
        setHedgeFlag(hedgeFlag)
    }

    private fun updatePrice(newTradeDTO: Trade) {
        if (tradeDTO == null || CommonUtils.isNotEquals(tradeDTO!!.price, newTradeDTO.price)) {
            setPrice(CommonUtils.formatDouble(newTradeDTO.price, decimalDigits))
        }
    }

    private fun updateVolume(newTradeDTO: Trade) {
        if (tradeDTO == null || tradeDTO!!.volume != newTradeDTO.volume) {
            setVolume(newTradeDTO.volume)
        }
    }

    private fun updateTradeTime(newTradeDTO: Trade) {
        setTradeTime(newTradeDTO.tradeTime)
    }

    private fun updateOriginOrderId(newTradeDTO: Trade) {
        if (tradeDTO == null || tradeDTO!!.originOrderId != newTradeDTO.originOrderId) {
            setOriginOrderId(newTradeDTO.originOrderId)
        }
    }

    private fun updateAdapterTradeId(newTradeDTO: Trade) {
        setAdapterTradeId(newTradeDTO.adapterTradeId)
    }

    private fun updateAccountId(newTradeDTO: Trade) {
        setAccountId(newTradeDTO.accountId)
    }

    fun getTradeId(): String {
        return tradeId.get()
    }

    fun setTradeId(tradeId: String?) {
        this.tradeId.set(tradeId)
    }

    fun tradeIdProperty(): SimpleStringProperty {
        return tradeId
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

    fun getDirection(): Text {
        return direction.get()
    }

    fun setDirection(direction: Text) {
        this.direction.set(direction)
    }

    fun directionProperty(): SimpleObjectProperty<Text> {
        return direction
    }

    fun getOffsetFlag(): String {
        return offsetFlag.get()
    }

    fun setOffsetFlag(offsetFlag: String?) {
        this.offsetFlag.set(offsetFlag)
    }

    fun offsetFlagProperty(): SimpleStringProperty {
        return offsetFlag
    }

    fun getHedgeFlag(): String {
        return hedgeFlag.get()
    }

    fun setHedgeFlag(hedgeFlag: String?) {
        this.hedgeFlag.set(hedgeFlag)
    }

    fun hedgeFlagProperty(): SimpleStringProperty {
        return hedgeFlag
    }

    fun getPrice(): String {
        return price.get()
    }

    fun setPrice(price: String?) {
        this.price.set(price)
    }

    fun priceProperty(): SimpleStringProperty {
        return price
    }

    fun getVolume(): Int {
        return volume.get()
    }

    fun setVolume(volume: Int) {
        this.volume.set(volume)
    }

    fun volumeProperty(): SimpleIntegerProperty {
        return volume
    }

    fun getTradeTime(): String {
        return tradeTime.get()
    }

    fun setTradeTime(tradeTime: String?) {
        this.tradeTime.set(tradeTime)
    }

    fun tradeTimeProperty(): SimpleStringProperty {
        return tradeTime
    }

    fun getAdapterTradeId(): String {
        return adapterTradeId.get()
    }

    fun setAdapterTradeId(adapterTradeId: String?) {
        this.adapterTradeId.set(adapterTradeId)
    }

    fun adapterTradeIdProperty(): SimpleStringProperty {
        return adapterTradeId
    }

    fun getOriginOrderId(): String {
        return originOrderId.get()
    }

    fun setOriginOrderId(originOrderId: String?) {
        this.originOrderId.set(originOrderId)
    }

    fun originOrderIdProperty(): SimpleStringProperty {
        return originOrderId
    }

    fun getAccountId(): String {
        return accountId.get()
    }

    fun setAccountId(accountId: String?) {
        this.accountId.set(accountId)
    }

    fun accountIdProperty(): SimpleStringProperty {
        return accountId
    }
}