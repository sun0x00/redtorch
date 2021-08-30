package xyz.redtorch.desktop.gui.bean


import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import xyz.redtorch.common.trade.dto.Account
import xyz.redtorch.common.utils.CommonUtils


class AccountFXBean(account: Account, selected: Boolean) {
    companion object {
        private val logger = LoggerFactory.getLogger(AccountFXBean::class.java)
    }

    private val selected = SimpleBooleanProperty(false)
    private val accountId = SimpleStringProperty()
    private val code = SimpleStringProperty()
    private val name = SimpleStringProperty()
    private val currency = SimpleStringProperty()
    private val balance = SimpleStringProperty()
    private val marginRatio = SimpleStringProperty()
    private val available = SimpleStringProperty()
    private val todayProfit = SimpleObjectProperty<Text>()
    private val todayProfitRatio = SimpleObjectProperty<Text>()
    private val closeProfit = SimpleObjectProperty<Text>()
    private val positionProfit = SimpleObjectProperty<Text>()
    private val commission = SimpleStringProperty()
    private val margin = SimpleStringProperty()
    private val preBalance = SimpleStringProperty()
    private val deposit = SimpleStringProperty()
    private val withdraw = SimpleStringProperty()
    private val gatewayId = SimpleStringProperty()

    var accountDTO: Account? = null
        private set

    init {
        update(account, selected)
    }

    fun update(newAccountDTO: Account, newSelected: Boolean) {
        // 更新是否选中
        updateSelected(newSelected)

        if (accountDTO == null) {
            updateAccountId(newAccountDTO)
            updateCode(newAccountDTO)
            updateName(newAccountDTO)
            updateCurrency(newAccountDTO)
            updateGatewayId(newAccountDTO)
        }

        // 对比内存地址
        if (newAccountDTO !== accountDTO) {
            updateChangeable(newAccountDTO)
            accountDTO = newAccountDTO
        }
    }

    private fun updateChangeable(newAccountDTO: Account) {
        updateBalance(newAccountDTO)
        updateMarginRatio(newAccountDTO)
        updateAvailable(newAccountDTO)
        updateTodayProfit(newAccountDTO)
        updateTodayProfitRatio(newAccountDTO)
        updateCloseProfit(newAccountDTO)
        updatePositionProfit(newAccountDTO)
        updateCommission(newAccountDTO)
        updateMargin(newAccountDTO)
        updatePreBalance(newAccountDTO)
        updateDeposit(newAccountDTO)
        updateWithdraw(newAccountDTO)
    }

    private fun updateSelected(selected: Boolean) {
        setSelected(selected)
    }

    private fun updateAccountId(newAccountDTO: Account) {
        setAccountId(newAccountDTO.accountId)
    }

    private fun updateCode(newAccountDTO: Account) {
        setCode(newAccountDTO.code)
    }

    private fun updateName(newAccountDTO: Account) {
        setName(newAccountDTO.name)
    }

    private fun updateCurrency(newAccountDTO: Account) {
        setCurrency(newAccountDTO.currency.toString())
    }

    private fun updateBalance(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.balance, newAccountDTO.balance)) {
            setBalance(CommonUtils.formatDouble(newAccountDTO.balance))
        }
    }

    private fun updateMarginRatio(newAccountDTO: Account) {
        if (accountDTO == null //
            || CommonUtils.isNotEquals(accountDTO!!.balance, newAccountDTO.balance) //
            || CommonUtils.isNotEquals(accountDTO!!.margin, newAccountDTO.margin)
        ) {
            setMarginRatio(CommonUtils.formatDouble(newAccountDTO.margin / newAccountDTO.balance * 100, "%,.2f%%"))
        }
    }

    private fun updateAvailable(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.available, newAccountDTO.available)) {
            setAvailable(CommonUtils.formatDouble(newAccountDTO.available))
        }
    }

    private fun updateTodayProfit(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.balance, newAccountDTO.balance)) {
            val tdProfitText = Text("渲染错误")
            try {
                val preBalance: Double = newAccountDTO.preBalance
                val tdProfit: Double = newAccountDTO.balance - preBalance - newAccountDTO.deposit + newAccountDTO.withdraw
                tdProfitText.text = CommonUtils.formatDouble(tdProfit)
                if (tdProfit > 0) {
                    tdProfitText.styleClass.add("trade-long-color")
                } else if (tdProfit < 0) {
                    tdProfitText.styleClass.add("trade-short-color")
                }
                tdProfitText.userData = tdProfit
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            setTodayProfit(tdProfitText)
        }
    }

    private fun updateTodayProfitRatio(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.balance, newAccountDTO.balance)) {
            val tdProfitRatioText = Text("渲染异常")
            try {
                var preBalance: Double = newAccountDTO.preBalance
                val tdProfit: Double = newAccountDTO.balance - preBalance - newAccountDTO.deposit + newAccountDTO.withdraw
                if (preBalance == 0.0) {
                    preBalance = newAccountDTO.balance
                }
                tdProfitRatioText.text = String.format("%,.2f%%", tdProfit / preBalance * 100)
                if (tdProfit > 0) {
                    tdProfitRatioText.styleClass.add("trade-long-color")
                } else if (tdProfit < 0) {
                    tdProfitRatioText.styleClass.add("trade-short-color")
                }
                tdProfitRatioText.userData = tdProfit
            } catch (e: Exception) {
                logger.error("渲染异常", e)
            }
            setTodayProfitRatio(tdProfitRatioText)
        }
    }

    private fun updateCloseProfit(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.closeProfit, newAccountDTO.closeProfit)) {
            val closeProfitText = Text(CommonUtils.formatDouble(newAccountDTO.closeProfit))
            if (newAccountDTO.closeProfit > 0) {
                closeProfitText.styleClass.add("trade-long-color")
            } else if (newAccountDTO.closeProfit < 0) {
                closeProfitText.styleClass.add("trade-short-color")
            }
            closeProfitText.userData = newAccountDTO
            setCloseProfit(closeProfitText)
        }
    }

    private fun updatePositionProfit(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.positionProfit, newAccountDTO.positionProfit)) {
            val positionProfitText = Text(CommonUtils.formatDouble(newAccountDTO.positionProfit))
            if (newAccountDTO.positionProfit > 0) {
                positionProfitText.styleClass.add("trade-long-color")
            } else if (newAccountDTO.positionProfit < 0) {
                positionProfitText.styleClass.add("trade-short-color")
            }
            positionProfitText.userData = newAccountDTO
            setPositionProfit(positionProfitText)
        }
    }

    private fun updateCommission(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.commission, newAccountDTO.commission)) {
            setCommission(CommonUtils.formatDouble(newAccountDTO.commission))
        }
    }

    private fun updateMargin(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.margin, newAccountDTO.margin)) {
            setMargin(CommonUtils.formatDouble(newAccountDTO.margin))
        }
    }

    private fun updatePreBalance(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.preBalance, newAccountDTO.preBalance)) {
            setPreBalance(CommonUtils.formatDouble(newAccountDTO.preBalance))
        }
    }

    private fun updateDeposit(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.deposit, newAccountDTO.deposit)) {
            setDeposit(CommonUtils.formatDouble(newAccountDTO.deposit))
        }
    }

    private fun updateWithdraw(newAccountDTO: Account) {
        if (accountDTO == null || CommonUtils.isNotEquals(accountDTO!!.withdraw, newAccountDTO.withdraw)) {
            setWithdraw(CommonUtils.formatDouble(newAccountDTO.withdraw))
        }
    }

    private fun updateGatewayId(newAccountDTO: Account) {
        setGatewayId(newAccountDTO.gatewayId)
    }

    fun isSelected(): Boolean {
        return selected.get()
    }

    fun selectedProperty(): SimpleBooleanProperty {
        return selected
    }

    fun setSelected(selected: Boolean) {
        this.selected.set(selected)
    }

    fun getAccountId(): String {
        return accountId.get()
    }

    fun setAccountId(accountId: String) {
        this.accountId.set(accountId)
    }

    fun accountIdProperty(): SimpleStringProperty {
        return accountId
    }

    fun getCode(): String {
        return code.get()
    }

    fun setCode(code: String) {
        this.code.set(code)
    }

    fun codeProperty(): SimpleStringProperty {
        return code
    }

    fun getName(): String {
        return name.get()
    }

    fun setName(name: String) {
        this.name.set(name)
    }

    fun nameProperty(): SimpleStringProperty {
        return name
    }

    fun getCurrency(): String {
        return currency.get()
    }

    fun setCurrency(currency: String) {
        this.currency.set(currency)
    }

    fun currencyProperty(): SimpleStringProperty {
        return currency
    }

    fun getBalance(): String {
        return balance.get()
    }

    fun setBalance(balance: String) {
        this.balance.set(balance)
    }

    fun balanceProperty(): SimpleStringProperty {
        return balance
    }

    fun getMarginRatio(): String {
        return marginRatio.get()
    }

    fun setMarginRatio(marginRatio: String) {
        this.marginRatio.set(marginRatio)
    }

    fun marginRatioProperty(): SimpleStringProperty {
        return marginRatio
    }

    fun getAvailable(): String {
        return available.get()
    }

    fun setAvailable(available: String) {
        this.available.set(available)
    }

    fun availableProperty(): SimpleStringProperty {
        return available
    }

    fun getTodayProfit(): Text {
        return todayProfit.get()
    }

    fun setTodayProfit(todayProfit: Text) {
        this.todayProfit.set(todayProfit)
    }

    fun todayProfitProperty(): SimpleObjectProperty<Text> {
        return todayProfit
    }

    fun getTodayProfitRatio(): Text {
        return todayProfitRatio.get()
    }

    fun setTodayProfitRatio(todayProfitRatio: Text) {
        this.todayProfitRatio.set(todayProfitRatio)
    }

    fun todayProfitRatioProperty(): SimpleObjectProperty<Text> {
        return todayProfitRatio
    }

    fun getCloseProfit(): Text {
        return closeProfit.get()
    }

    fun setCloseProfit(closeProfit: Text) {
        this.closeProfit.set(closeProfit)
    }

    fun closeProfitProperty(): SimpleObjectProperty<Text> {
        return closeProfit
    }

    fun getPositionProfit(): Text {
        return positionProfit.get()
    }

    fun setPositionProfit(positionProfit: Text) {
        this.positionProfit.set(positionProfit)
    }

    fun positionProfitProperty(): SimpleObjectProperty<Text> {
        return positionProfit
    }

    fun getCommission(): String {
        return commission.get()
    }

    fun setCommission(commission: String) {
        this.commission.set(commission)
    }

    fun commissionProperty(): SimpleStringProperty {
        return commission
    }

    fun getMargin(): String {
        return margin.get()
    }

    fun setMargin(margin: String) {
        this.margin.set(margin)
    }

    fun marginProperty(): SimpleStringProperty {
        return margin
    }

    fun getPreBalance(): String {
        return preBalance.get()
    }

    fun setPreBalance(preBalance: String) {
        this.preBalance.set(preBalance)
    }

    fun preBalanceProperty(): SimpleStringProperty {
        return preBalance
    }

    fun getDeposit(): String {
        return deposit.get()
    }

    fun setDeposit(deposit: String) {
        this.deposit.set(deposit)
    }

    fun depositProperty(): SimpleStringProperty {
        return deposit
    }

    fun getWithdraw(): String {
        return withdraw.get()
    }

    fun setWithdraw(withdraw: String) {
        this.withdraw.set(withdraw)
    }

    fun withdrawProperty(): SimpleStringProperty {
        return withdraw
    }

    fun getGatewayId(): String {
        return gatewayId.get()
    }

    fun setGatewayId(gatewayId: String) {
        this.gatewayId.set(gatewayId)
    }

    fun gatewayIdProperty(): SimpleStringProperty {
        return gatewayId
    }
}
