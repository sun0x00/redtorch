package xyz.redtorch.desktop.gui.bean

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.common.trade.dto.Position
import xyz.redtorch.common.trade.enumeration.HedgeFlagEnum
import xyz.redtorch.common.trade.enumeration.PositionDirectionEnum
import xyz.redtorch.common.utils.CommonUtils

class PositionFXBean(position: Position, contractSelectedFlag: Boolean, accountBalance: Double) {

    private val positionId = SimpleStringProperty()
    private val contract = SimpleObjectProperty<Pane>()
    private val direction = SimpleObjectProperty<Text>()
    private val hedgeFlag = SimpleStringProperty()
    private val position = SimpleObjectProperty<Pane>()
    private val todayPosition = SimpleObjectProperty<Pane>()
    private val openProfit = SimpleObjectProperty<Pane>()
    private val positionProfit = SimpleObjectProperty<Pane>()
    private val openPrice = SimpleObjectProperty<Pane>()
    private val positionPrice = SimpleObjectProperty<Pane>()
    private val margin = SimpleObjectProperty<Pane>()
    private val marginRatio = SimpleStringProperty()
    private val contractValue = SimpleStringProperty()
    private val accountId = SimpleStringProperty()

    private var decimalDigits = 4

    private var contractSelectedFlag = false
    private var accountBalance = 0.0

    var positionDTO: Position? = null
        private set

    init {
        update(position, contractSelectedFlag, accountBalance)
    }

    fun update(newPositionDTO: Position, newContractSelectedFlag: Boolean, newAccountBalance: Double) {
        if (positionDTO == null) {
            decimalDigits = CommonUtils.getNumberDecimalDigits(newPositionDTO.contract.priceTick)

            updatePositionId(newPositionDTO)
            updateDirection(newPositionDTO)
            updateHedgeFlag(newPositionDTO)
            updateContract(newPositionDTO)
            updateAccountId(newPositionDTO)
        }
        // 更新选中合约高亮
        if (contractSelectedFlag != newContractSelectedFlag) {
            contractSelectedFlag = newContractSelectedFlag
            updateContract(newPositionDTO)
        }

        // 对比内存地址
        if (newPositionDTO !== positionDTO) {
            accountBalance = newAccountBalance
            updateChangeable(newPositionDTO)
            positionDTO = newPositionDTO
        } else if (CommonUtils.isNotEquals(accountBalance, newAccountBalance)) {
            // 如果持仓更新的对象是一条旧数据,但是账户资金发生了变化
            accountBalance = newAccountBalance
            updateMarginRatio(newPositionDTO)
        }
    }

    private fun updateChangeable(newPositionDTO: Position) {
        updatePosition(newPositionDTO)
        updateTodayPosition(newPositionDTO)
        updateOpenProfit(newPositionDTO)
        updatePositionProfit(newPositionDTO)
        updateOpenPrice(newPositionDTO)
        updatePositionPrice(newPositionDTO)
        updateMargin(newPositionDTO)
        updateMarginRatio(newPositionDTO)
        updateContractValue(newPositionDTO)
    }

    private fun updatePositionId(newPositionDTO: Position) {
        setPositionId(newPositionDTO.positionId)
    }

    private fun updateContract(newPositionDTO: Position) {
        val vBox = VBox().apply {
            children.add(Text(newPositionDTO.contract.uniformSymbol).apply {
                // 选中合约高亮
                if (contractSelectedFlag) {
                    styleClass.add("trade-remind-color")
                }
            })
            children.add(
                Text(newPositionDTO.contract.name)
            )
            userData = newPositionDTO
        }
        setContract(vBox)
    }

    private fun updateDirection(newPositionDTO: Position) {
        val directionText = Text("未知")

        when (newPositionDTO.positionDirection) {
            PositionDirectionEnum.Long -> {
                directionText.text = "多"
                directionText.styleClass.add("trade-long-color")
            }
            PositionDirectionEnum.Short -> {
                directionText.text = "空"
                directionText.styleClass.add("trade-short-color")
            }

            PositionDirectionEnum.Net -> {
                directionText.text = "净"
            }
            PositionDirectionEnum.Unknown -> {
                directionText.text = "未知"
            }
            else -> {
                directionText.text = newPositionDTO.positionDirection.toString()
            }
        }

        directionText.userData = newPositionDTO
        setDirection(directionText)
    }

    private fun updateHedgeFlag(newPositionDTO: Position) {
        val hedgeFlag = when (newPositionDTO.hedgeFlag) {
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
                newPositionDTO.hedgeFlag.toString()
            }
        }
        setHedgeFlag(hedgeFlag)
    }

    private fun updatePosition(newPositionDTO: Position) {
        if (positionDTO == null || positionDTO!!.position != newPositionDTO.position || positionDTO!!.frozen != newPositionDTO.position) {

            val vBox = VBox().apply {
                children.add(HBox().apply {
                    children.add(Text("持仓").apply {
                        wrappingWidth = 35.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(newPositionDTO.position.toString()).apply {
                        // 高亮
                        if (newPositionDTO.positionDirection == PositionDirectionEnum.Long) {
                            styleClass.add("trade-long-color")
                        } else if (newPositionDTO.positionDirection == PositionDirectionEnum.Short) {
                            styleClass.add("trade-short-color")
                        }
                    })
                })

                children.add(HBox().apply {
                    children.add(Text("冻结").apply {
                        wrappingWidth = 35.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(newPositionDTO.frozen.toString()).apply {
                        if (newPositionDTO.frozen != 0) {
                            styleClass.add("trade-info-color")
                        }
                    })
                })

                userData = newPositionDTO
            }

            setPosition(vBox)
        }
    }

    private fun updateTodayPosition(newPositionDTO: Position) {
        if (positionDTO == null || positionDTO!!.tdPosition != newPositionDTO.tdPosition || positionDTO!!.tdFrozen != newPositionDTO.tdFrozen) {

            val vBox = VBox().apply {
                children.add(HBox().apply {
                    children.add(Text("持仓").apply {
                        wrappingWidth = 35.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(newPositionDTO.tdPosition.toString()).apply {
                        // 高亮
                        if (newPositionDTO.positionDirection == PositionDirectionEnum.Long) {
                            styleClass.add("trade-long-color")
                        } else if (newPositionDTO.positionDirection == PositionDirectionEnum.Short) {
                            styleClass.add("trade-short-color")
                        }
                    })
                })

                children.add(HBox().apply {
                    children.add(Text("冻结").apply {
                        wrappingWidth = 35.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(newPositionDTO.tdFrozen.toString()).apply {
                        if (newPositionDTO.tdFrozen != 0) {
                            styleClass.add("trade-info-color")
                        }
                    })
                })

                userData = newPositionDTO
            }

            setTodayPosition(vBox)
        }
    }

    private fun updateOpenProfit(newPositionDTO: Position) {
        if (positionDTO == null || CommonUtils.isNotEquals(positionDTO!!.openPositionProfit, newPositionDTO.openPositionProfit)
            || CommonUtils.isNotEquals(positionDTO!!.openPositionProfitRatio, newPositionDTO.openPositionProfitRatio)
        ) {
            val openPositionProfitText = Text(CommonUtils.formatDouble(newPositionDTO.openPositionProfit))
            val openPositionProfitRatioText = Text(CommonUtils.formatDouble(newPositionDTO.openPositionProfitRatio * 100, "%.2f%%"))
            if (newPositionDTO.openPositionProfit > 0) {
                openPositionProfitText.styleClass.add("trade-long-color")
                openPositionProfitRatioText.styleClass.add("trade-long-color")
            } else if (newPositionDTO.openPositionProfit < 0) {
                openPositionProfitText.styleClass.add("trade-short-color")
                openPositionProfitRatioText.styleClass.add("trade-short-color")
            }
            val vBox = VBox()
            vBox.children.addAll(openPositionProfitText, openPositionProfitRatioText)
            vBox.userData = newPositionDTO
            setOpenProfit(vBox)
        }
    }

    private fun updatePositionProfit(newPositionDTO: Position) {
        if (positionDTO == null || CommonUtils.isNotEquals(positionDTO!!.positionProfit, newPositionDTO.positionProfit)
            || CommonUtils.isNotEquals(positionDTO!!.positionProfitRatio, newPositionDTO.positionProfitRatio)
        ) {
            val positionProfitText = Text(CommonUtils.formatDouble(newPositionDTO.positionProfit))
            val positionProfitRatioText = Text(CommonUtils.formatDouble(newPositionDTO.positionProfitRatio * 100, "%.2f%%"))

            if (newPositionDTO.positionProfit > 0) {
                positionProfitText.styleClass.add("trade-long-color")
                positionProfitRatioText.styleClass.add("trade-long-color")
            } else if (newPositionDTO.positionProfit < 0) {
                positionProfitText.styleClass.add("trade-short-color")
                positionProfitRatioText.styleClass.add("trade-short-color")
            }
            val vBox = VBox()
            vBox.children.addAll(positionProfitText, positionProfitRatioText)
            vBox.userData = newPositionDTO
            setPositionProfit(vBox)
        }
    }

    private fun updateOpenPrice(newPositionDTO: Position) {
        if (positionDTO == null || CommonUtils.isNotEquals(positionDTO!!.openPrice, newPositionDTO.openPrice)
            || CommonUtils.isNotEquals(positionDTO!!.openPriceDiff, newPositionDTO.openPriceDiff)
        ) {
            val openPriceText = Text(CommonUtils.formatDouble(newPositionDTO.openPrice, decimalDigits))
            val openPriceDiffText = Text(CommonUtils.formatDouble(newPositionDTO.openPriceDiff, decimalDigits))

            if (newPositionDTO.openPriceDiff > 0) {
                openPriceDiffText.styleClass.add("trade-long-color")
            } else if (newPositionDTO.openPriceDiff < 0) {
                openPriceDiffText.styleClass.add("trade-short-color")
            }

            val vBox = VBox()
            vBox.children.addAll(openPriceText, openPriceDiffText)
            vBox.userData = newPositionDTO
            setOpenPrice(vBox)
        }
    }

    private fun updatePositionPrice(newPositionDTO: Position) {
        if (positionDTO == null || CommonUtils.isNotEquals(positionDTO!!.price, newPositionDTO.price)
            || CommonUtils.isNotEquals(positionDTO!!.priceDiff, newPositionDTO.priceDiff)
        ) {
            val priceText = Text(CommonUtils.formatDouble(newPositionDTO.price, decimalDigits))
            val priceDiffText = Text(CommonUtils.formatDouble(newPositionDTO.priceDiff, decimalDigits))

            if (newPositionDTO.priceDiff > 0) {
                priceDiffText.styleClass.add("trade-long-color")
            } else if (newPositionDTO.priceDiff < 0) {
                priceDiffText.styleClass.add("trade-short-color")
            }

            val vBox = VBox()
            vBox.children.addAll(priceText, priceDiffText)
            vBox.userData = newPositionDTO
            setPositionPrice(vBox)
        }
    }

    private fun updateMargin(newPositionDTO: Position) {
        if (positionDTO == null || CommonUtils.isNotEquals(positionDTO!!.price, newPositionDTO.price)
            || CommonUtils.isNotEquals(positionDTO!!.priceDiff, newPositionDTO.priceDiff)
        ) {

            val vBox = VBox().apply {
                children.add(HBox().apply {
                    children.add(Text("经纪商").apply {
                        wrappingWidth = 45.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(CommonUtils.formatDouble(newPositionDTO.useMargin)))
                })

                children.add(HBox().apply {
                    children.add(Text("交易所").apply {
                        wrappingWidth = 45.0
                        styleClass.add("trade-label")
                    })
                    children.add(Text(CommonUtils.formatDouble(newPositionDTO.exchangeMargin)))
                })

                userData = newPositionDTO
            }

            setMargin(vBox)
        }
    }

    private fun updateMarginRatio(newPositionDTO: Position) {
        val marginRatioStr = if (accountBalance == 0.0) {
            "NA"
        } else {
            val marginRatio = newPositionDTO.useMargin / accountBalance * 100
            CommonUtils.formatDouble(marginRatio) + "%"
        }
        setMarginRatio(marginRatioStr)
    }

    private fun updateContractValue(newPositionDTO: Position) {
        if (positionDTO == null || CommonUtils.isNotEquals(positionDTO!!.contractValue, newPositionDTO.contractValue)) {
            setContractValue(CommonUtils.formatDouble(newPositionDTO.contractValue))
        }
    }

    private fun updateAccountId(newPositionDTO: Position) {
        setAccountId(newPositionDTO.accountId)
    }

    fun getPositionId(): String {
        return positionId.get()
    }

    fun positionIdProperty(): SimpleStringProperty {
        return positionId
    }

    fun setPositionId(positionId: String?) {
        this.positionId.set(positionId)
    }

    fun getContract(): Pane {
        return contract.get()
    }

    fun contractProperty(): SimpleObjectProperty<Pane> {
        return contract
    }

    fun setContract(contract: Pane) {
        this.contract.set(contract)
    }

    fun getDirection(): Text {
        return direction.get()
    }

    fun directionProperty(): SimpleObjectProperty<Text> {
        return direction
    }

    fun setDirection(direction: Text) {
        this.direction.set(direction)
    }

    fun getHedgeFlag(): String {
        return hedgeFlag.get()
    }

    fun hedgeFlagProperty(): SimpleStringProperty {
        return hedgeFlag
    }

    fun setHedgeFlag(hedgeFlag: String?) {
        this.hedgeFlag.set(hedgeFlag)
    }

    fun getPosition(): Pane {
        return position.get()
    }

    fun positionProperty(): SimpleObjectProperty<Pane> {
        return position
    }

    fun setPosition(position: Pane) {
        this.position.set(position)
    }

    fun getTodayPosition(): Pane {
        return todayPosition.get()
    }

    fun todayPositionProperty(): SimpleObjectProperty<Pane> {
        return todayPosition
    }

    fun setTodayPosition(todayPosition: Pane) {
        this.todayPosition.set(todayPosition)
    }

    fun getOpenProfit(): Pane {
        return openProfit.get()
    }

    fun openProfitProperty(): SimpleObjectProperty<Pane> {
        return openProfit
    }

    fun setOpenProfit(openProfit: Pane) {
        this.openProfit.set(openProfit)
    }

    fun getPositionProfit(): Pane {
        return positionProfit.get()
    }

    fun positionProfitProperty(): SimpleObjectProperty<Pane> {
        return positionProfit
    }

    fun setPositionProfit(positionProfit: Pane) {
        this.positionProfit.set(positionProfit)
    }

    fun getOpenPrice(): Pane {
        return openPrice.get()
    }

    fun openPriceProperty(): SimpleObjectProperty<Pane> {
        return openPrice
    }

    fun setOpenPrice(openPrice: Pane) {
        this.openPrice.set(openPrice)
    }

    fun getPositionPrice(): Pane {
        return positionPrice.get()
    }

    fun positionPriceProperty(): SimpleObjectProperty<Pane> {
        return positionPrice
    }

    fun setPositionPrice(positionPrice: Pane) {
        this.positionPrice.set(positionPrice)
    }

    fun getMargin(): Pane {
        return margin.get()
    }

    fun marginProperty(): SimpleObjectProperty<Pane> {
        return margin
    }

    fun setMargin(margin: Pane) {
        this.margin.set(margin)
    }

    fun getMarginRatio(): String {
        return marginRatio.get()
    }

    fun marginRatioProperty(): SimpleStringProperty {
        return marginRatio
    }

    fun setMarginRatio(marginRatio: String?) {
        this.marginRatio.set(marginRatio)
    }

    fun getContractValue(): String {
        return contractValue.get()
    }

    fun contractValueProperty(): SimpleStringProperty {
        return contractValue
    }

    fun setContractValue(contractValue: String?) {
        this.contractValue.set(contractValue)
    }

    fun getAccountId(): String {
        return accountId.get()
    }

    fun accountIdProperty(): SimpleStringProperty {
        return accountId
    }

    fun setAccountId(accountId: String?) {
        this.accountId.set(accountId)
    }

}