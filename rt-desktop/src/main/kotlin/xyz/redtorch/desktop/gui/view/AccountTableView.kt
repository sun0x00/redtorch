package xyz.redtorch.desktop.gui.view

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.control.cell.CheckBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.common.trade.dto.Account
import xyz.redtorch.common.utils.CommonUtils.doubleStringCompare
import xyz.redtorch.desktop.gui.bean.AccountFXBean
import xyz.redtorch.desktop.gui.state.ViewState

class AccountTableView(private val viewState: ViewState) {

    private val accountObservableList = FXCollections.observableArrayList<AccountFXBean>()
    private var accountList: List<Account> = ArrayList()
    private var accountFXBeanMap = HashMap<String, AccountFXBean>()
    private val selectedAccountIdSet: MutableSet<String> = HashSet()
    private val selectAllCheckBox = CheckBox()
    private val accountTableView = TableView<AccountFXBean>()
    val view = VBox()

    init {
        // 设置全选按钮鼠标事件
        selectAllCheckBox.apply {
            // 单击事件
            onMouseClicked = EventHandler {
                // 账户表格清除所有选择
                accountTableView.selectionModel.clearSelection()

                if (this.isSelected) {
                    // 当前得到的状态如果是已经全选，则说明过去曾是半选或者未选中
                    // 选中所有账户
                    val newSelectedAccountIdSet: MutableSet<String> = HashSet()
                    for (accountFXBean in accountObservableList) {
                        newSelectedAccountIdSet.add(accountFXBean.getAccountId())
                    }
                    selectedAccountIdSet.addAll(newSelectedAccountIdSet)
                } else {
                    // 当前得到的状态如果是未选中，则说明过去是全选
                    // 清除所有已选择账户
                    selectedAccountIdSet.clear()
                }
                // 重新渲染
                render()
            }
        }

        accountTableView.apply {
            isTableMenuButtonVisible = true
            isFocusTraversable = false
            // 允许CTRL+Click多选
            selectionModel.selectionMode = SelectionMode.MULTIPLE

            columns.add(TableColumn<AccountFXBean, Boolean>("选择").apply {
                cellValueFactory = PropertyValueFactory("selected")
                cellFactory = CheckBoxTableCell.forTableColumn(this)
                isSortable = false
                isEditable = false
                maxWidth = 50.0
                minWidth = 50.0
                text = ""
                graphic = selectAllCheckBox
            })

            columns.add(TableColumn<AccountFXBean, String>("账户代码").apply {
                prefWidth = 80.0
                cellValueFactory = PropertyValueFactory("code")
                setComparator { s1, s2 -> s1.compareTo(s2) }
                // 作为默认排序列
                sortOrder.add(this)
            })

            columns.add(TableColumn<AccountFXBean, String>("名称").apply {
                prefWidth = 150.0
                cellValueFactory = PropertyValueFactory("name")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("币种").apply {
                prefWidth = 40.0
                cellValueFactory = PropertyValueFactory("currency")
                setComparator { s1, s2 -> s1.compareTo(s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("权益").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("balance")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("使用率").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("marginRatio")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("可用").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("available")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, Text>("今日盈亏").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("todayProfit")
                setComparator { t1: Text, t2: Text -> doubleStringCompare(t1.text, t2.text) }
            })

            columns.add(TableColumn<AccountFXBean, Text>("今日盈亏率").apply {
                prefWidth = 75.0
                cellValueFactory = PropertyValueFactory("todayProfitRatio")
                setComparator { t1: Text, t2: Text -> doubleStringCompare(t1.text, t2.text) }
            })

            columns.add(TableColumn<AccountFXBean, Text>("平仓盈亏").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("closeProfit")
                setComparator { t1: Text, t2: Text -> doubleStringCompare(t1.text, t2.text) }
            })

            columns.add(TableColumn<AccountFXBean, Text>("持仓盈亏").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("positionProfit")
                setComparator { t1: Text, t2: Text -> doubleStringCompare(t1.text, t2.text) }
            })

            columns.add(TableColumn<AccountFXBean, String>("保证金").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("margin")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("昨日权益").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("preBalance")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("佣金").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("commission")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("入金").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("deposit")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("出金").apply {
                prefWidth = 90.0
                cellValueFactory = PropertyValueFactory("withdraw")
                setComparator { s1: String, s2: String -> doubleStringCompare(s1, s2) }
            })

            columns.add(TableColumn<AccountFXBean, String>("账户ID").apply {
                prefWidth = 300.0
                cellValueFactory = PropertyValueFactory("accountId")
                setComparator { s1: String, s2: String -> s1.compareTo(s2) }
            })

            // 行渲染设置
            setRowFactory {
                val row = TableRow<AccountFXBean>()
                // 如果行被单击
                row.setOnMousePressed { event: MouseEvent ->
                    if (!row.isEmpty && event.button == MouseButton.PRIMARY && event.clickCount == 1) {
                        // 获取当当前被点击的行数据
                        val clickedItem: AccountFXBean = row.item
                        // 获取当前已被选中的项的列表
                        val selectedItems: ObservableList<AccountFXBean> = this.selectionModel.selectedItems
                        // 是否反向操作取消选择
                        var selectedItemReverse = false
                        // 如果选中账户ID集合包含这个账户ID,则反向操作,取消选择
                        if (selectedAccountIdSet.contains(clickedItem.getAccountId())) {
                            selectedItemReverse = true
                        }
                        // 清除已选择的账户ID的集合
                        selectedAccountIdSet.clear()
                        // 遍历所有被选中的项,更新已选择的账户ID的集合
                        for (selectedRow in selectedItems) {
                            if (selectedRow.getAccountId() == clickedItem.getAccountId()) {
                                if (!selectedItemReverse) {
                                    selectedAccountIdSet.add(selectedRow.getAccountId())
                                }
                            } else {
                                selectedAccountIdSet.add(selectedRow.getAccountId())
                            }
                        }
                        // 更新全选按钮的状态
                        if (selectedAccountIdSet.size == 0) {
                            selectAllCheckBox.isSelected = false
                            selectAllCheckBox.isIndeterminate = false
                        } else if (selectedAccountIdSet.size < accountObservableList.size) {
                            selectAllCheckBox.isSelected = false
                            selectAllCheckBox.isIndeterminate = true
                        } else if (selectedAccountIdSet.size == accountObservableList.size) {
                            selectAllCheckBox.isSelected = true
                            selectAllCheckBox.isIndeterminate = false
                        }
                        render()
                    }
                }
                row
            }
        }

        // 建立数据关联
        val sortedItems = SortedList(accountObservableList)
        accountTableView.items = sortedItems
        // 建立排序关联
        sortedItems.comparatorProperty().bind(accountTableView.comparatorProperty())

        // 表格加入容器,设置表格自动垂直高度
        view.children.add(accountTableView.apply {
            VBox.setVgrow(this, Priority.ALWAYS)
        })
    }

    fun updateData(accountList: List<Account>) {
        this.accountList = accountList
        render()
    }

    fun render() {
        // 清空所有选择
        accountTableView.selectionModel.clearSelection()
        // 建立账户ID集合,用于后续筛选流程
        val accountIdSet: MutableSet<String> = HashSet()
        // 新增账户FXBeanList
        val newAccountFXBeanList: MutableList<AccountFXBean> = ArrayList()
        // 遍历账户数据列表
        for (account in accountList) {
            val accountId = account.accountId
            accountIdSet.add(accountId)
            if (accountFXBeanMap.containsKey(accountId)) {
                // 如果当前账户已经在FXBeanMap中,则走更新流程
                accountFXBeanMap[accountId]!!.update(account, viewState.isSelectedAccountId(accountId))
            } else {
                // 如果当前账户不在FXBeanMap中,则新建
                val accountFXBean = AccountFXBean(account, viewState.isSelectedAccountId(accountId))
                accountFXBeanMap[accountId] = accountFXBean
                newAccountFXBeanList.add(accountFXBean)
            }
        }
        // 在ObservableList中增加新建的账户FXBean
        accountObservableList.addAll(newAccountFXBeanList)

        // 根据账户ID集合对数据进行过滤
        val newAccountFXBeanMap = HashMap<String, AccountFXBean>()
        for (accountId in accountFXBeanMap.keys) {
            // 在新的账户FXBeanMap中只加入账户数据列表中存在的账户
            if (accountIdSet.contains(accountId)) {
                newAccountFXBeanMap[accountId] = accountFXBeanMap[accountId]!!
            }
        }
        // 替换旧数据
        accountFXBeanMap = newAccountFXBeanMap
        // 从ObservableList中删除已经不需要的账户FXBean
        accountObservableList.removeIf { accountFXBean: AccountFXBean -> !accountIdSet.contains(accountFXBean.getAccountId()) }
        // 重新排序
        accountTableView.sort()

        // 遍历账户ObservableList,更新已选中账户
        val newSelectedAccountIdSet: MutableSet<String> = HashSet()
        for (account in accountObservableList) {
            if (selectedAccountIdSet.contains(account.getAccountId())) {
                accountTableView.selectionModel.select(account)
                newSelectedAccountIdSet.add(account.getAccountId())
            }
        }
        // 清除旧数据
        selectedAccountIdSet.clear()
        selectedAccountIdSet.addAll(newSelectedAccountIdSet)

        // 向state更新已选中账户ID集合
        viewState.updateSelectedAccountIdSet(newSelectedAccountIdSet)
    }
}