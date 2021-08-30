package xyz.redtorch.desktop.gui.view

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.common.trade.dto.Account
import xyz.redtorch.common.trade.dto.Position
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.desktop.gui.state.ViewState

class CombinationView(private val viewState: ViewState) {

    private val todayProfitSumText = Text("N/A")
    private val balanceSumText = Text("N/A")
    private val openPositionProfitSumText = Text("N/A")
    private val positionProfitSumText = Text("N/A")
    private val marginSumText = Text("N/A")
    private val commissionSumText = Text("N/A")
    private val closeProfitSumText = Text("N/A")
    private val contractValueSumText = Text("N/A")
    private val depositAndWithdrawSumText = Text("N/A")
    private var positionList: List<Position> = ArrayList()
    private var accountList: List<Account> = ArrayList()

    val view = HBox()

    init {

        val labelWidth = 95.0
        view.apply {
            children.add(VBox().apply {
                HBox.setHgrow(this, Priority.ALWAYS)

                children.add(HBox().apply {
                    children.add(Label("今日盈亏（率）：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(todayProfitSumText)
                })

                children.add(HBox().apply {
                    children.add(Label("资金：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(balanceSumText)
                })

                children.add(HBox().apply {
                    children.add(Label("持仓盈亏：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(openPositionProfitSumText)
                })

            })


            children.add(VBox().apply {
                HBox.setHgrow(this, Priority.ALWAYS)

                children.add(HBox().apply {
                    children.add(Label("盯市持仓盈亏：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(positionProfitSumText)
                })

                children.add(HBox().apply {
                    children.add(Label("保证金（率）：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(marginSumText)
                })

                children.add(HBox().apply {
                    children.add(Label("佣金：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(commissionSumText)
                })

            })



            children.add(VBox().apply {
                HBox.setHgrow(this, Priority.ALWAYS)

                children.add(HBox().apply {
                    children.add(Label("盯市平仓盈亏：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(closeProfitSumText)
                })

                children.add(HBox().apply {
                    children.add(Label("合约价值：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(contractValueSumText)
                })

                children.add(HBox().apply {
                    children.add(Label("出入金：").apply {
                        minWidth = labelWidth
                        alignment = Pos.CENTER_RIGHT
                        styleClass.add("trade-label")
                    })
                    children.add(depositAndWithdrawSumText)
                })

            })

        }
    }


    fun updateData(positionList: List<Position>, accountList: List<Account>) {
        if (HashSet<Position>(this.positionList) != HashSet<Position>(positionList) || HashSet<Account>(this.accountList) != HashSet<Account>(accountList)) {
            this.positionList = positionList
            this.accountList = accountList
            render()
        }
    }

    fun render() {
        var todayProfitSum: Double? = null
        var todayProfitSumRatio: Double? = null
        var balanceSum: Double? = null
        var openPositionProfitSum: Double? = null
        var positionProfitSum: Double? = null
        var marginSum: Double? = null
        var marginSumRatio: Double? = null
        var commissionSum: Double? = null
        var closeProfitSum: Double? = null
        var contractValueSum: Double? = null
        var depositAndWithdrawSum: Double? = null
        var preBalanceSum = 0.0
        for (account in accountList) {
            if (viewState.getSelectedAccountIdSet().isEmpty() || viewState.getSelectedAccountIdSet().contains(account.accountId)) {
                todayProfitSum = todayProfitSum ?: 0.0
                balanceSum = balanceSum ?: 0.0
                positionProfitSum = positionProfitSum ?: 0.0
                marginSum = marginSum ?: 0.0
                commissionSum = commissionSum ?: 0.0
                closeProfitSum = closeProfitSum ?: 0.0
                depositAndWithdrawSum = depositAndWithdrawSum ?: 0.0
                var preBalance: Double = account.preBalance
                val tdProfit: Double = account.balance - preBalance - account.deposit + account.withdraw
                todayProfitSum += tdProfit
                balanceSum += account.balance
                if (preBalance == 0.0) {
                    preBalance = account.balance
                }
                preBalanceSum += preBalance
                positionProfitSum += account.positionProfit
                marginSum += account.margin
                commissionSum += account.commission
                closeProfitSum += account.closeProfit
                depositAndWithdrawSum += account.deposit - account.withdraw
            }
        }
        if (preBalanceSum != 0.0) {
            todayProfitSumRatio = todayProfitSum!! / preBalanceSum
        }
        if (marginSum != null && balanceSum != 0.0) {
            marginSumRatio = marginSum / balanceSum!!
        }
        for (position in positionList) {
            if (viewState.getSelectedAccountIdSet().isEmpty() || viewState.getSelectedAccountIdSet().contains(position.accountId)) {
                openPositionProfitSum = openPositionProfitSum ?: 0.0
                contractValueSum = contractValueSum ?: 0.0
                openPositionProfitSum += if (position.openPositionProfit.isNaN()) 0.0 else position.openPositionProfit
                contractValueSum += if (position.contractValue.isNaN()) 0.0 else position.contractValue
            }
        }
        todayProfitSumText.styleClass.clear()
        if (todayProfitSum != null) {
            todayProfitSumText.text = String.format("%,.2f(%,.2f%%)", todayProfitSum, todayProfitSumRatio!! * 100)
            if (todayProfitSum > 0) {
                todayProfitSumText.styleClass.add("trade-long-color")
            } else if (todayProfitSum < 0) {
                todayProfitSumText.styleClass.add("trade-short-color")
            }
        } else {
            todayProfitSumText.text = "N/A"
        }
        if (balanceSum != null) {
            balanceSumText.text = CommonUtils.formatDouble(balanceSum, "%,.2f")
        } else {
            balanceSumText.text = "N/A"
        }
        openPositionProfitSumText.styleClass.clear()
        if (openPositionProfitSum != null) {
            openPositionProfitSumText.text = CommonUtils.formatDouble(openPositionProfitSum, "%,.2f")
            if (openPositionProfitSum > 0) {
                openPositionProfitSumText.styleClass.add("trade-long-color")
            } else if (openPositionProfitSum < 0) {
                openPositionProfitSumText.styleClass.add("trade-short-color")
            }
        } else {
            openPositionProfitSumText.text = "N/A"
        }
        positionProfitSumText.styleClass.clear()
        if (positionProfitSum != null) {
            positionProfitSumText.text = CommonUtils.formatDouble(positionProfitSum, "%,.2f")
            if (positionProfitSum > 0) {
                positionProfitSumText.styleClass.add("trade-long-color")
            } else if (positionProfitSum < 0) {
                positionProfitSumText.styleClass.add("trade-short-color")
            }
        } else {
            positionProfitSumText.text = "N/A"
        }
        if (marginSum != null) {
            marginSumText.text = String.format("%,.2f(%,.2f%%)", marginSum, marginSumRatio!! * 100)
        } else {
            marginSumText.text = "N/A"
        }
        if (commissionSum != null) {
            commissionSumText.text = CommonUtils.formatDouble(commissionSum, "%,.2f")
        } else {
            commissionSumText.text = "N/A"
        }
        closeProfitSumText.styleClass.clear()
        if (closeProfitSum != null) {
            closeProfitSumText.text = CommonUtils.formatDouble(closeProfitSum, "%,.2f")
            if (closeProfitSum > 0) {
                closeProfitSumText.styleClass.add("trade-long-color")
            } else if (closeProfitSum < 0) {
                closeProfitSumText.styleClass.add("trade-short-color")
            }
        } else {
            closeProfitSumText.text = "N/A"
        }
        if (contractValueSum != null) {
            contractValueSumText.text = CommonUtils.formatDouble(contractValueSum, "%,.2f")
        } else {
            contractValueSumText.text = "N/A"
        }
        depositAndWithdrawSumText.styleClass.clear()
        if (depositAndWithdrawSum != null) {
            depositAndWithdrawSumText.text = CommonUtils.formatDouble(depositAndWithdrawSum, "%,.2f")
            if (depositAndWithdrawSum > 0) {
                depositAndWithdrawSumText.styleClass.add("trade-long-color")
            } else if (depositAndWithdrawSum < 0) {
                depositAndWithdrawSumText.styleClass.add("trade-short-color")
            }
        } else {
            depositAndWithdrawSumText.text = "N/A"
        }
    }
}