package xyz.redtorch.master.gui.view

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import xyz.redtorch.common.storage.po.User
import xyz.redtorch.master.gui.bean.UserFXBean
import xyz.redtorch.master.gui.state.ViewState
import xyz.redtorch.master.gui.utils.FXUtils

class UserView(private val viewState: ViewState) {

    private val userFXBeanList = FXCollections.observableArrayList<UserFXBean>()

    private val tableView = TableView<UserFXBean>().apply {
        // 纵向自适应
        VBox.setVgrow(this, Priority.ALWAYS)
    }
    val view = VBox().apply {
        // 增加表格
        children.add(tableView)
        // 增加操作栏
        children.add(HBox(5.0).apply {
            children.add(Label("      用户管理      ").apply {
                padding = Insets(4.0, 0.0, 0.0, 0.0)
            })
            children.add(Button("刷新").apply {
                setOnAction {
                    refreshData()
                }
            })
            children.add(Button("增加").apply {
                setOnAction {
                    // 禁止应用超链接已被访问过的样式

                    val userIdProperty = SimpleStringProperty("")
                    val descriptionProperty = SimpleStringProperty("")

                    val userIdTextField = TextField().apply {
                        HBox.setHgrow(this, Priority.ALWAYS)
                        textProperty().bindBidirectional(userIdProperty)
                    }

                    val descriptionTextField = TextArea().apply {
                        HBox.setHgrow(this, Priority.ALWAYS)
                        textProperty().bindBidirectional(descriptionProperty)
                    }

                    val contentPane = VBox(5.0).apply {
                        children.add(HBox(5.0).apply {
                            children.add(Label("用户ID").apply { prefWidth = 40.0 })
                            children.add(userIdTextField)
                        })
                        children.add(HBox(5.0).apply {
                            children.add(Label("描述").apply { prefWidth = 40.0 })
                            children.add(descriptionTextField)
                            VBox.setVgrow(this, Priority.ALWAYS)
                        })
                    }

                    Platform.runLater { userIdTextField.requestFocus() }

                    val res = FXUtils.showInputDialog("新增用户", contentPane)

                    res.ifPresent { buttonType ->
                        if (buttonType == ButtonType.OK && userIdProperty.get() != null && "" != userIdProperty.get()) {
                            val user = viewState.userService.addUser(userIdProperty.get(), descriptionProperty.get() ?: "")
                            if (user == null) {
                                Alert(Alert.AlertType.WARNING).apply {
                                    contentText = "新增用户失败"
                                }.showAndWait()
                            }
                        } else if (buttonType == ButtonType.OK && (userIdProperty.get() == null || "" == userIdProperty.get())) {
                            Alert(Alert.AlertType.WARNING).apply {
                                contentText = "用户ID不可为空"
                            }.showAndWait()
                        }
                    }
                    refreshData()
                }
            })
        })
    }

    init {
        val idColumn = TableColumn<UserFXBean, String>("ID").apply {
            prefWidth = 150.0
            cellValueFactory = PropertyValueFactory("id")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(idColumn)

        val bannedColumn = TableColumn<UserFXBean, String>("是否封禁").apply {
            prefWidth = 80.0
            cellValueFactory = PropertyValueFactory("banned")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(bannedColumn)

        val operateColumn = TableColumn<UserFXBean, Pane>("操作").apply {
            prefWidth = 310.0
            // 关闭这一列的排序功能
            isSortable = false
            cellFactory = Callback<TableColumn<UserFXBean, Pane>, TableCell<UserFXBean, Pane>> {
                object : TableCell<UserFXBean, Pane>() {
                    // 采用HBox作为横向容器
                    private val container = HBox(5.0).apply {

                        children.add(Hyperlink("显示令牌").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)

                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false

                                // 显示令牌弹框
                                Stage().apply {
                                    scene = Scene(VBox().apply {
                                        prefWidth = 280.0 // 给scene初始宽度

                                        children.add(TextField(tableView.items[index].getToken()).apply {
                                            isEditable = false // 禁止编辑，只允许查看复制
                                        })
                                    })
                                    maxHeight = 65.0 // 限制stage高度
                                    maxWidth = 300.0 // 限制stage高度
                                    title = "用户${tableView.items[index].getId()}令牌"

                                    // 模态
                                    initModality(Modality.APPLICATION_MODAL)
                                    initOwner(viewState.primaryStage!!.scene.window)
                                }.showAndWait()
                            }
                        })

                        children.add(Hyperlink("重置令牌").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)

                            // 设置事件响应
                            setOnAction {
                                isVisited = false  // 禁止应用超链接已被访问过的样式

                                // 弹框获取响应结果
                                val res =
                                    FXUtils.showConfirmAlert("重置令牌确认", "确认重置用户${tableView.items[index].getId()}令牌？", viewState.primaryStage!!.scene.window)

                                // 根据弹框结果决定是否重置
                                if (res) {
                                    viewState.userService.resetUserTokenById(tableView.items[index].getId())
                                }

                                refreshData()
                            }
                        })

                        children.add(Hyperlink("删除").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)
                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false

                                // 弹框获取响应结果
                                val res = FXUtils.showConfirmAlert("删除确认", "确认删除用户${tableView.items[index].getId()}？", viewState.primaryStage!!.scene.window)

                                if (res) {
                                    viewState.userService.deleteUserById(tableView.items[index].getId())
                                }

                                refreshData()
                            }
                        })

                        children.add(Hyperlink("更改状态").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)
                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false
                                if (tableView.items[index].getUser().banned) {
                                    // 弹框获取响应结果
                                    val res =
                                        FXUtils.showConfirmAlert("启用确认", "确认启用用户${tableView.items[index].getId()}？", viewState.primaryStage!!.scene.window)

                                    if (res) {
                                        viewState.userService.enableUserById(tableView.items[index].getId())
                                    }
                                } else {
                                    // 弹框获取响应结果
                                    val res =
                                        FXUtils.showConfirmAlert("封禁确认", "确认封禁用户${tableView.items[index].getId()}？", viewState.primaryStage!!.scene.window)

                                    if (res) {
                                        viewState.userService.banUserById(tableView.items[index].getId())
                                    }
                                }


                                refreshData()
                            }
                        })


                        children.add(Hyperlink("更新描述").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)
                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false

                                val descriptionProperty = SimpleStringProperty(tableView.items[index].getDescription())

                                val textArea = TextArea().apply {
                                    HBox.setHgrow(this, Priority.ALWAYS)
                                    textProperty().bindBidirectional(descriptionProperty)
                                }

                                val contentPane = VBox().apply {
                                    children.add(HBox().apply {
                                        children.add(textArea)
                                        VBox.setVgrow(this, Priority.ALWAYS)
                                    })
                                }

                                Platform.runLater { textArea.requestFocus() }

                                val res = FXUtils.showInputDialog("更新用户${tableView.items[index].getId()}描述", contentPane)

                                res.ifPresent { buttonType ->
                                    val newDescription = if (descriptionProperty.get() == null) {
                                        ""
                                    } else {
                                        descriptionProperty.get()
                                    }
                                    if (buttonType == ButtonType.OK && !newDescription.equals(tableView.items[index].getDescription())) {
                                        viewState.userService.updateUserDescriptionById(tableView.items[index].getId(), newDescription)
                                    }
                                }
                                refreshData()
                            }
                        })

                        children.add(Hyperlink("权限管理").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)
                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false

                                val user = tableView.items[index].getUser()
                                showAclDialog(user)
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

        val descriptionColumn = TableColumn<UserFXBean, String>("描述").apply {
            prefWidth = 250.0
            cellValueFactory = PropertyValueFactory("description")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(descriptionColumn)

        // 允许排序
        val sortedItems: SortedList<UserFXBean> = SortedList(userFXBeanList)
        tableView.items = sortedItems
        sortedItems.comparatorProperty().bind(tableView.comparatorProperty())
        // 默认排序
        tableView.sortOrder.add(idColumn)
        tableView.selectionModel.selectionMode = SelectionMode.SINGLE
        refreshData()
    }

    private fun refreshData() {
        val userList = viewState.userService.getUserList()
        userFXBeanList.clear()
        for (user in userList) {
            userFXBeanList.add(UserFXBean(user))
        }
    }

    private fun showAclDialog(user: User) {

        ////////////////////////////////////////////////////////////
        // 读取账户权限
        val permitReadAllAccountsProperty = SimpleBooleanProperty(user.permitReadAllAccounts)
        val permitReadAllAccountsCheckBox = CheckBox().apply {
            selectedProperty().bindBidirectional(permitReadAllAccountsProperty)
        }

        var acceptReadAccountIdSetString = ""
        for (accountId in user.acceptReadAccountIdSet) {
            acceptReadAccountIdSetString += (accountId + "\n")
        }

        val acceptReadAccountIdSetProperty = SimpleStringProperty(acceptReadAccountIdSetString)
        val acceptReadAccountIdSetTextArea = TextArea().apply {
            prefWidth = 250.0
            textProperty().bindBidirectional(acceptReadAccountIdSetProperty)
        }

        var denyReadAccountIdSetString = ""
        for (accountId in user.denyReadAccountIdSet) {
            denyReadAccountIdSetString += (accountId + "\n")
        }

        val denyReadAccountIdSetProperty = SimpleStringProperty(denyReadAccountIdSetString)
        val denyReadAccountIdSetTextArea = TextArea().apply {
            prefWidth = 250.0
            textProperty().bindBidirectional(denyReadAccountIdSetProperty)
        }
        ////////////////////////////////////////////////////////////
        // 交易账户权限
        val permitTradeAllAccountsProperty = SimpleBooleanProperty(user.permitTradeAllAccounts)
        val permitTradeAllAccountsCheckBox = CheckBox().apply {
            selectedProperty().bindBidirectional(permitTradeAllAccountsProperty)
        }

        var acceptTradeAccountIdSetString = ""
        for (accountId in user.acceptTradeAccountIdSet) {
            acceptTradeAccountIdSetString += (accountId + "\n")
        }

        val acceptTradeAccountIdSetProperty = SimpleStringProperty(acceptTradeAccountIdSetString)
        val acceptTradeAccountIdSetTextArea = TextArea().apply {
            prefWidth = 250.0
            textProperty().bindBidirectional(acceptTradeAccountIdSetProperty)
        }


        var denyTradeAccountIdSetString = ""
        for (accountId in user.denyTradeAccountIdSet) {
            denyTradeAccountIdSetString += (accountId + "\n")
        }

        val denyTradeAccountIdSetProperty = SimpleStringProperty(denyTradeAccountIdSetString)
        val denyTradeAccountIdSetTextArea = TextArea().apply {
            prefWidth = 250.0
            textProperty().bindBidirectional(denyTradeAccountIdSetProperty)
        }
        ////////////////////////////////////////////////////////////
        // 交易合约权限
        val permitTradeAllContractsProperty = SimpleBooleanProperty(user.permitTradeAllContracts)
        val permitTradeAllContractsCheckBox = CheckBox().apply {
            selectedProperty().bindBidirectional(permitTradeAllContractsProperty)
        }

        var acceptTradeUniformSymbolSetString = ""
        for (uniformSymbol in user.acceptTradeUniformSymbolSet) {
            acceptTradeUniformSymbolSetString += (uniformSymbol + "\n")
        }

        val acceptTradeUniformSymbolSetProperty = SimpleStringProperty(acceptTradeUniformSymbolSetString)
        val acceptTradeUniformSymbolSetTextArea = TextArea().apply {
            prefWidth = 250.0
            textProperty().bindBidirectional(acceptTradeUniformSymbolSetProperty)
        }


        var denyTradeUniformSymbolSetString = ""
        for (uniformSymbol in user.denyTradeUniformSymbolSet) {
            denyTradeUniformSymbolSetString += (uniformSymbol + "\n")
        }

        val denyTradeUniformSymbolSetProperty = SimpleStringProperty(denyTradeUniformSymbolSetString)
        val denyTradeUniformSymbolSetTextArea = TextArea().apply {
            prefWidth = 250.0
            textProperty().bindBidirectional(denyTradeUniformSymbolSetProperty)
        }
        ////////////////////////////////////////////////////////////


        val contentPane = ScrollPane().apply {
            isFitToWidth = true
            prefHeight = 400.0
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
            content = VBox(10.0).apply {
                children.add(VBox(10.0).apply {
                    children.add(Label("提示:列表按行输入"))
                    children.add(Separator(Orientation.HORIZONTAL))
                    children.add(HBox(3.0).apply {
                        children.add(Label("允许读取全部账户"))
                        children.add(permitReadAllAccountsCheckBox)
                    })
                    children.add(HBox(3.0).apply {
                        children.add(VBox().apply {
                            children.add(Label("允许读取账户列表"))
                            children.add(acceptReadAccountIdSetTextArea)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                        children.add(VBox().apply {
                            children.add(Label("拒绝读取账户列表"))
                            children.add(denyReadAccountIdSetTextArea)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                    })
                })

                children.add(VBox(10.0).apply {
                    VBox.setVgrow(this, Priority.ALWAYS)
                    children.add(Separator(Orientation.HORIZONTAL))

                    children.add(HBox(3.0).apply {
                        children.add(Label("允许交易全部账户"))
                        children.add(permitTradeAllAccountsCheckBox)
                    })
                    children.add(HBox(3.0).apply {
                        children.add(VBox().apply {
                            children.add(Label("允许交易账户列表"))
                            children.add(acceptTradeAccountIdSetTextArea)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                        children.add(VBox().apply {
                            children.add(Label("拒绝交易账户列表"))
                            children.add(denyTradeAccountIdSetTextArea)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })

                    })
                })

                children.add(VBox(10.0).apply {
                    children.add(Separator(Orientation.HORIZONTAL))
                    children.add(HBox(3.0).apply {
                        children.add(Label("允许交易全部合约"))
                        children.add(permitTradeAllContractsCheckBox)
                    })

                    children.add(HBox(3.0).apply {
                        children.add(VBox().apply {
                            children.add(Label("允许交易合约列表"))
                            children.add(acceptTradeUniformSymbolSetTextArea)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                        children.add(VBox().apply {
                            children.add(Label("拒绝交易账户列表"))
                            children.add(denyTradeUniformSymbolSetTextArea)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })

                    })
                })


            }

        }

        Platform.runLater { acceptReadAccountIdSetTextArea.requestFocus() }

        val res = FXUtils.showInputDialog("更新用户${user.id}权限", contentPane)

        res.ifPresent { buttonType ->

            if (buttonType == ButtonType.OK) {
                ////////////////////////////
                val permitReadAllAccounts = permitReadAllAccountsProperty.get()
                val acceptReadAccountIdSet = HashSet<String>()
                for (accountIdString in acceptReadAccountIdSetProperty.get().split("\n")) {
                    val accountId = accountIdString.trim()
                    if (accountId.isNotBlank()) {
                        acceptReadAccountIdSet.add(accountId)
                    }
                }
                val denyReadAccountIdSet = HashSet<String>()
                for (accountIdString in denyReadAccountIdSetProperty.get().split("\n")) {
                    val accountId = accountIdString.trim()
                    if (accountId.isNotBlank()) {
                        denyReadAccountIdSet.add(accountId)
                    }
                }
                /////////////////////////////
                val permitTradeAllAccounts = permitTradeAllAccountsProperty.get()
                val acceptTradeAccountIdSet = HashSet<String>()
                for (accountIdString in acceptTradeAccountIdSetProperty.get().split("\n")) {
                    val accountId = accountIdString.trim()
                    if (accountId.isNotBlank()) {
                        acceptTradeAccountIdSet.add(accountId)
                    }
                }
                val denyTradeAccountIdSet = HashSet<String>()
                for (accountIdString in denyTradeAccountIdSetProperty.get().split("\n")) {
                    val accountId = accountIdString.trim()
                    if (accountId.isNotBlank()) {
                        denyTradeAccountIdSet.add(accountId)
                    }
                }
                /////////////////////////////
                val permitTradeAllContracts = permitTradeAllContractsProperty.get()
                val acceptTradeUniformSymbolSet = HashSet<String>()
                for (uniformSymbolString in acceptTradeUniformSymbolSetProperty.get().split("\n")) {
                    val uniformSymbol = uniformSymbolString.trim()
                    if (uniformSymbol.isNotBlank()) {
                        acceptTradeUniformSymbolSet.add(uniformSymbol)
                    }
                }
                val denyTradeUniformSymbolSet = HashSet<String>()
                for (uniformSymbolString in denyTradeUniformSymbolSetProperty.get().split("\n")) {
                    val uniformSymbol = uniformSymbolString.trim()
                    if (uniformSymbol.isNotBlank()) {
                        denyTradeUniformSymbolSet.add(uniformSymbol)
                    }
                }
                //////////////////////////////
                user.permitReadAllAccounts = permitReadAllAccounts
                user.acceptReadAccountIdSet = acceptReadAccountIdSet
                user.denyReadAccountIdSet = denyReadAccountIdSet

                user.permitTradeAllAccounts = permitTradeAllAccounts
                user.acceptTradeAccountIdSet = acceptTradeAccountIdSet
                user.denyTradeAccountIdSet = denyTradeAccountIdSet

                user.permitTradeAllContracts = permitTradeAllContracts
                user.acceptTradeUniformSymbolSet = acceptTradeUniformSymbolSet
                user.denyTradeUniformSymbolSet = denyTradeUniformSymbolSet

                viewState.userService.upsertUserById(user)
            }
        }
    }
}