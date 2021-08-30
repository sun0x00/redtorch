package xyz.redtorch.master.gui.view

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.geometry.Insets
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
import xyz.redtorch.master.gui.bean.SlaveNodeFXBean
import xyz.redtorch.master.gui.state.ViewState
import xyz.redtorch.master.gui.utils.FXUtils

class SlaveNodeView(private val viewState: ViewState) {

    private val slaveNodeFXBeanList = FXCollections.observableArrayList<SlaveNodeFXBean>()

    private val tableView = TableView<SlaveNodeFXBean>().apply {
        // 纵向自适应
        VBox.setVgrow(this, Priority.ALWAYS)
    }
    val view = VBox().apply {
        // 增加表格
        children.add(tableView)
        // 增加操作栏
        children.add(HBox(5.0).apply {
            children.add(Label("      节点管理      ").apply {
                padding = Insets(4.0, 0.0, 0.0, 0.0)
            })
            children.add(Button("刷新").apply {
                setOnAction {
                    refreshData()
                }
            })
            children.add(Button("创建").apply {
                setOnAction {
                    viewState.slaveNodeService.createSlaveNode()
                    refreshData()
                }
            })
        })
    }

    init {
        val idColumn = TableColumn<SlaveNodeFXBean, String>("ID").apply {
            prefWidth = 60.0
            cellValueFactory = PropertyValueFactory("id")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(idColumn)

        val connectionStatusColumn = TableColumn<SlaveNodeFXBean, String>("状态").apply {
            prefWidth = 80.0
            cellValueFactory = PropertyValueFactory("connectionStatus")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(connectionStatusColumn)

        val operateColumn = TableColumn<SlaveNodeFXBean, Pane>("操作").apply {
            prefWidth = 200.0
            // 关闭这一列的排序功能
            isSortable = false
            cellFactory = Callback<TableColumn<SlaveNodeFXBean, Pane>, TableCell<SlaveNodeFXBean, Pane>> {
                object : TableCell<SlaveNodeFXBean, Pane>() {
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
                                    title = "节点${tableView.items[index].getId()}令牌"

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
                                    FXUtils.showConfirmAlert("重置令牌确认", "确认重置节点${tableView.items[index].getId()}令牌？", viewState.primaryStage!!.scene.window)

                                // 根据弹框结果决定是否重置
                                if (res) {
                                    viewState.slaveNodeService.resetSlaveNodeTokenById(tableView.items[index].getId())
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
                                val res = FXUtils.showConfirmAlert("删除确认", "确认删除节点${tableView.items[index].getId()}？", viewState.primaryStage!!.scene.window)

                                if (res) {
                                    viewState.slaveNodeService.deleteSlaveNodeById(tableView.items[index].getId())
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

                                val res = FXUtils.showInputDialog("更新节点${tableView.items[index].getId()}描述", contentPane)

                                res.ifPresent { buttonType ->
                                    val newDescription = if (descriptionProperty.get() == null) {
                                        ""
                                    } else {
                                        descriptionProperty.get()
                                    }
                                    if (buttonType == ButtonType.OK && !newDescription.equals(tableView.items[index].getDescription())) {
                                        viewState.slaveNodeService.updateSlaveNodeDescriptionById(tableView.items[index].getId(), newDescription)
                                    }
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

        val descriptionColumn = TableColumn<SlaveNodeFXBean, String>("描述").apply {
            prefWidth = 250.0
            cellValueFactory = PropertyValueFactory("description")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(descriptionColumn)

        // 允许排序
        val sortedItems: SortedList<SlaveNodeFXBean> = SortedList(slaveNodeFXBeanList)
        tableView.items = sortedItems
        sortedItems.comparatorProperty().bind(tableView.comparatorProperty())
        // 默认排序
        tableView.sortOrder.add(idColumn)
        // 禁止选中
        tableView.selectionModel.selectionMode = SelectionMode.SINGLE
        refreshData()
    }

    private fun refreshData() {
        val slaveNodeList = viewState.slaveNodeService.getSlaveNodeList()
        slaveNodeFXBeanList.clear()
        for (slaveNode in slaveNodeList) {
            slaveNodeFXBeanList.add(SlaveNodeFXBean(slaveNode))
        }
    }
}