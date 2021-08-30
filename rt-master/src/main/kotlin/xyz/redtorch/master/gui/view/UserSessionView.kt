package xyz.redtorch.master.gui.view

import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback
import xyz.redtorch.master.gui.bean.UserSessionFXBean
import xyz.redtorch.master.gui.state.ViewState
import xyz.redtorch.master.gui.utils.FXUtils

class UserSessionView(private val viewState: ViewState) {

    private val userSessionFXBeanList = FXCollections.observableArrayList<UserSessionFXBean>()

    private val tableView = TableView<UserSessionFXBean>().apply {
        // 纵向自适应
        VBox.setVgrow(this, Priority.ALWAYS)
    }
    val view = VBox().apply {
        // 增加表格
        children.add(tableView)
        // 增加操作栏
        children.add(HBox(5.0).apply {
            children.add(Label("      会话列表      ").apply {
                padding = Insets(4.0, 0.0, 0.0, 0.0)
            })
            children.add(Button("刷新").apply {
                setOnAction {
                    refreshData()
                }
            })
        })
    }

    init {
        val sessionIdColumn = TableColumn<UserSessionFXBean, String>("会话ID").apply {
            prefWidth = 250.0
            cellValueFactory = PropertyValueFactory("sessionId")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(sessionIdColumn)
        val addressColumn = TableColumn<UserSessionFXBean, String>("地址").apply {
            prefWidth = 250.0
            cellValueFactory = PropertyValueFactory("address")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(addressColumn)

        val userIdColumn = TableColumn<UserSessionFXBean, String>("用户ID").apply {
            prefWidth = 120.0
            cellValueFactory = PropertyValueFactory("userId")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(userIdColumn)

        val delayColumn = TableColumn<UserSessionFXBean, String>("通信延迟").apply {
            prefWidth = 80.0
            cellValueFactory = PropertyValueFactory("delay")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(delayColumn)

        val timeOfDurationColumn = TableColumn<UserSessionFXBean, String>("持续时间").apply {
            prefWidth = 80.0
            cellValueFactory = PropertyValueFactory("timeOfDuration")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(timeOfDurationColumn)


        val operateColumn = TableColumn<UserSessionFXBean, Pane>("操作").apply {
            prefWidth = 120.0
            // 关闭这一列的排序功能
            isSortable = false
            cellFactory = Callback<TableColumn<UserSessionFXBean, Pane>, TableCell<UserSessionFXBean, Pane>> {
                object : TableCell<UserSessionFXBean, Pane>() {
                    // 采用HBox作为横向容器
                    private val container = HBox(5.0).apply {

                        children.add(Hyperlink("封禁用户").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)

                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false

                                // 弹框获取响应结果
                                val res =
                                    FXUtils.showConfirmAlert("封禁确认", "确认封禁用户${tableView.items[index].getUserId()}？", viewState.primaryStage!!.scene.window)

                                // 根据弹框结果决定是否重置
                                if (res) {
                                    viewState.userService.banUserById(tableView.items[index].getUserId())
                                }

                                refreshData()
                            }
                        })

                        children.add(Hyperlink("断开连接").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)

                            // 设置事件响应
                            setOnAction {
                                isVisited = false  // 禁止应用超链接已被访问过的样式

                                // 弹框获取响应结果
                                val res =
                                    FXUtils.showConfirmAlert(
                                        "断开确认",
                                        "确认断开用户${tableView.items[index].getUserId()}的会话${tableView.items[index].getSessionId()}？",
                                        viewState.primaryStage!!.scene.window
                                    )

                                // 根据弹框结果决定是否重置
                                if (res) {
                                    viewState.tradeClientWebSocketHandler.closeBySessionId(tableView.items[index].getSessionId())
                                }

                                refreshData()
                            }
                        })

                    }

                    override fun updateItem(item: Pane?, empty: Boolean) {
                        super.updateItem(item, empty)
                        graphic = if (empty) null else container
                    }
                }
            }
        }
        tableView.columns.add(operateColumn)

        // 允许排序
        val sortedItems: SortedList<UserSessionFXBean> = SortedList(userSessionFXBeanList)
        tableView.items = sortedItems
        sortedItems.comparatorProperty().bind(tableView.comparatorProperty())
        // 默认排序
        tableView.sortOrder.add(userIdColumn)
        tableView.selectionModel.selectionMode = SelectionMode.SINGLE
        refreshData()
    }

    private fun refreshData() {
        userSessionFXBeanList.clear()
        userSessionFXBeanList.addAll(viewState.tradeClientWebSocketHandler.getUserSessionFXBeanList())
    }

}