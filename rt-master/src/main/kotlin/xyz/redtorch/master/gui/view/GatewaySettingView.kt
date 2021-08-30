package xyz.redtorch.master.gui.view

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
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
import xyz.redtorch.common.enumeration.ConnectionStatusEnum
import xyz.redtorch.common.storage.enumeration.GatewayTypeEnum
import xyz.redtorch.common.storage.po.GatewayAdapterCtpSetting
import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.master.gui.bean.GatewaySettingFXBean
import xyz.redtorch.master.gui.state.ViewState
import xyz.redtorch.master.gui.utils.FXUtils

class GatewaySettingView(private val viewState: ViewState) {
    private val gatewaySettingFXBeanList = FXCollections.observableArrayList<GatewaySettingFXBean>()

    private val tableView = TableView<GatewaySettingFXBean>().apply {
        // 纵向自适应
        VBox.setVgrow(this, Priority.ALWAYS)
    }

    val view = VBox().apply {
        // 增加表格
        children.add(tableView)
        // 增加操作栏
        children.add(HBox(5.0).apply {
            children.add(Label("      网关管理      ").apply {
                padding = Insets(4.0, 0.0, 0.0, 0.0)
            })
            children.add(Button("刷新").apply {
                setOnAction {
                    refreshData()
                }
            })
            children.add(Button("增加").apply {
                setOnAction {
                    showAddOrEditGatewaySettingDialog(null)
                    refreshData()
                }
            })

            children.add(Button("全部连接").apply {
                setOnAction {
                    // 弹框获取响应结果
                    val res = FXUtils.showConfirmAlert("连接确认", "确认连接全部网关？", viewState.primaryStage!!.scene.window)

                    if (res) {
                        viewState.gatewaySettingService.connectAllGateways()
                        // 立即同步配置到子节点
                        viewState.systemService.syncSlaveNodeSettingMirror()
                    }
                    refreshData()
                }
            })

            children.add(Button("全部断开").apply {
                setOnAction {
                    // 弹框获取响应结果
                    val res = FXUtils.showConfirmAlert("断开确认", "确认断开全部网关？", viewState.primaryStage!!.scene.window)

                    if (res) {
                        viewState.gatewaySettingService.disconnectAllGateways()
                        // 立即同步配置到子节点
                        viewState.systemService.syncSlaveNodeSettingMirror()
                    }
                    refreshData()
                }
            })

        })
    }

    init {
        val idColumn = TableColumn<GatewaySettingFXBean, String>("ID").apply {
            prefWidth = 60.0
            cellValueFactory = PropertyValueFactory("id")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(idColumn)

        val nameColumn = TableColumn<GatewaySettingFXBean, String>("名称").apply {
            prefWidth = 160.0
            cellValueFactory = PropertyValueFactory("name")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(nameColumn)

        val targetSlaveNodeIdColumn = TableColumn<GatewaySettingFXBean, String>("目标节点").apply {
            prefWidth = 60.0
            cellValueFactory = PropertyValueFactory("targetSlaveNodeId")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(targetSlaveNodeIdColumn)

        val connectionStatusColumn = TableColumn<GatewaySettingFXBean, String>("状态").apply {
            prefWidth = 80.0
            cellValueFactory = PropertyValueFactory("connectionStatus")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(connectionStatusColumn)


        val operateColumn = TableColumn<GatewaySettingFXBean, Pane>("操作").apply {
            prefWidth = 240.0
            // 关闭这一列的排序功能
            isSortable = false
            cellFactory = Callback<TableColumn<GatewaySettingFXBean, Pane>, TableCell<GatewaySettingFXBean, Pane>> {
                object : TableCell<GatewaySettingFXBean, Pane>() {
                    // 采用HBox作为横向容器
                    private val container = HBox(5.0).apply {

                        children.add(Hyperlink("复制").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)
                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false

                                // 弹框获取响应结果
                                val res = FXUtils.showConfirmAlert(
                                    "复制确认",
                                    "确认复制网关${tableView.items[index].getId()}？",
                                    viewState.primaryStage!!.scene.window
                                )

                                if (res) {
                                    val gatewaySetting = tableView.items[index].getGatewaySetting()

                                    gatewaySetting.id = ""
                                    gatewaySetting.name = "CP-${gatewaySetting.name}"
                                    gatewaySetting.connectionStatus = ConnectionStatusEnum.Disconnected

                                    viewState.gatewaySettingService.upsertGatewaySettingById(gatewaySetting)
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
                                val res = FXUtils.showConfirmAlert("删除确认", "确认删除网关${tableView.items[index].getId()}？", viewState.primaryStage!!.scene.window)

                                if (res) {
                                    viewState.gatewaySettingService.deleteGatewaySettingById(tableView.items[index].getId())
                                    // 立即同步配置到子节点
                                    viewState.systemService.syncSlaveNodeSettingMirror()
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

                                val gatewaySetting = tableView.items[index].getGatewaySetting()
                                if (gatewaySetting.connectionStatus == ConnectionStatusEnum.Unknown
                                    || gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnected
                                    || gatewaySetting.connectionStatus == ConnectionStatusEnum.Disconnecting
                                ) {
                                    viewState.gatewaySettingService.connectGatewayById(gatewaySetting.id!!)
                                } else {
                                    val res = FXUtils.showConfirmAlert("断开确认", "确认断开网关${gatewaySetting.id}？", viewState.primaryStage!!.scene.window)
                                    if (res) {
                                        viewState.gatewaySettingService.disconnectGatewayById(gatewaySetting.id!!)
                                    }
                                }
                                // 立即同步配置到子节点
                                viewState.systemService.syncSlaveNodeSettingMirror()
                                refreshData()
                            }
                        })

                        children.add(Hyperlink("修改配置").apply {
                            // 减小对周围空间的占用
                            padding = Insets(0.0)
                            setOnAction {
                                // 禁止应用超链接已被访问过的样式
                                isVisited = false

                                showAddOrEditGatewaySettingDialog(tableView.items[index].getGatewaySetting())

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

                                val res = FXUtils.showInputDialog("更新网关${tableView.items[index].getId()}描述", contentPane)

                                res.ifPresent { buttonType ->
                                    val newDescription = if (descriptionProperty.get() == null) {
                                        ""
                                    } else {
                                        descriptionProperty.get()
                                    }
                                    if (buttonType == ButtonType.OK && !newDescription.equals(tableView.items[index].getDescription())) {
                                        viewState.gatewaySettingService.updateGatewaySettingDescriptionById(tableView.items[index].getId(), newDescription)
                                        // 立即同步配置到子节点
                                        viewState.systemService.syncSlaveNodeSettingMirror()
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

        val gatewayTypeColumn = TableColumn<GatewaySettingFXBean, String>("网关类型").apply {
            prefWidth = 120.0
            cellValueFactory = PropertyValueFactory("gatewayType")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(gatewayTypeColumn)

        val implementClassNameColumn = TableColumn<GatewaySettingFXBean, String>("实现类").apply {
            prefWidth = 250.0
            cellValueFactory = PropertyValueFactory("implementClassName")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(implementClassNameColumn)

        val descriptionColumn = TableColumn<GatewaySettingFXBean, String>("描述").apply {
            prefWidth = 250.0
            cellValueFactory = PropertyValueFactory("description")
            comparator = Comparator { a: String, b: String -> a.compareTo(b) }
        }
        tableView.columns.add(descriptionColumn)

        val versionColumn = TableColumn<GatewaySettingFXBean, Long>("版本").apply {
            prefWidth = 120.0
            cellValueFactory = PropertyValueFactory("version")
            comparator = Comparator { a: Long, b: Long -> a.compareTo(b) }
        }
        tableView.columns.add(versionColumn)


        // 允许排序
        val sortedItems: SortedList<GatewaySettingFXBean> = SortedList(gatewaySettingFXBeanList)
        tableView.items = sortedItems
        sortedItems.comparatorProperty().bind(tableView.comparatorProperty())
        // 默认排序
        tableView.sortOrder.add(idColumn)
        // 禁止选中
        tableView.selectionModel.selectionMode = SelectionMode.SINGLE
        refreshData()
    }

    private fun refreshData() {
        val gatewaySettingList = viewState.gatewaySettingService.getGatewaySettingList()
        gatewaySettingFXBeanList.clear()
        for (gatewaySetting in gatewaySettingList) {
            gatewaySettingFXBeanList.add(GatewaySettingFXBean(gatewaySetting))
        }
    }

    private fun showAddOrEditGatewaySettingDialog(gatewaySetting: GatewaySetting?) {

        val isNew = gatewaySetting == null

        val editGatewaySetting = if (isNew) {
            GatewaySetting()
        } else {
            gatewaySetting!!
        }

        // ID
        val gatewayIdProperty = SimpleStringProperty(editGatewaySetting.id ?: "")
        val gatewayIdTextField = TextField().apply {
            isEditable = false
            textProperty().bindBidirectional(gatewayIdProperty)
        }

        // 目标节点
        val targetSlaveNodeIdProperty = SimpleStringProperty(editGatewaySetting.targetSlaveNodeId)
        val targetSlaveNodeIdTextField = TextField().apply {
            textProperty().bindBidirectional(targetSlaveNodeIdProperty)
        }

        // 网关名称
        val nameProperty = SimpleStringProperty(editGatewaySetting.name ?: "")
        val nameTextField = TextField().apply {
            textProperty().bindBidirectional(nameProperty)
        }
        // 网关描述
        val descriptionProperty = SimpleStringProperty(editGatewaySetting.description ?: "")
        val descriptionTextField = TextField().apply {
            textProperty().bindBidirectional(descriptionProperty)
        }

        // 网关类型
        val gatewayTypeProperty = SimpleObjectProperty(editGatewaySetting.gatewayType)
        val gatewayTypeComboBox = ComboBox<GatewayTypeEnum>().apply {
            maxWidth = Double.MAX_VALUE
            items = FXCollections.observableArrayList<GatewayTypeEnum>().apply {
                addAll(GatewayTypeEnum.values())
                valueProperty().bindBidirectional(gatewayTypeProperty)
            }
//          selectionModel.select(GatewayTypeEnum.TradeAndQuote)
        }

        // 实现类
        val implementClassNameProperty = SimpleStringProperty(editGatewaySetting.implementClassName ?: "")
        val implementClassNameTextField = TextField().apply {
            textProperty().bindBidirectional(implementClassNameProperty)
        }

        // 855-1555#2033-2359#0-300
        val autoConnectTimeRangesProperty = SimpleStringProperty(editGatewaySetting.autoConnectTimeRanges ?: "")
        val autoConnectTimeRangesField = TextField().apply {
            textProperty().bindBidirectional(autoConnectTimeRangesProperty)
        }
        // 适配器配置JSON字符串
        val adapterSettingJsonString = if (isNew) {
            JsonUtils.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(GatewayAdapterCtpSetting())
        } else {
            editGatewaySetting.adapterSettingJsonString ?: ""
        }
        val adapterSettingJsonStringProperty = SimpleStringProperty(adapterSettingJsonString)
        val adapterSettingJsonStringTextArea = TextArea().apply {
            prefHeight = 350.0
            textProperty().bindBidirectional(adapterSettingJsonStringProperty)
        }

        val contentPane = ScrollPane().apply {
            isFitToWidth = true
            prefHeight = 400.0
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
            content = VBox(10.0).apply {
                children.add(VBox(10.0).apply {
                    children.add(HBox(3.0).apply {
                        children.add(VBox().apply {
                            children.add(Label("ID(自动)"))
                            children.add(gatewayIdTextField)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                        children.add(VBox().apply {
                            children.add(Label("目标节点(SlaveNodeID)"))
                            children.add(targetSlaveNodeIdTextField)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                    })
                })

                children.add(VBox(10.0).apply {
                    children.add(HBox(3.0).apply {
                        children.add(VBox().apply {
                            children.add(Label("名称"))
                            children.add(nameTextField)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                        children.add(VBox().apply {
                            children.add(Label("描述"))
                            children.add(descriptionTextField)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                    })
                })

                children.add(Label("网关类型"))
                children.add(gatewayTypeComboBox)

                children.add(VBox(10.0).apply {
                    children.add(HBox(3.0).apply {
                        children.add(VBox().apply {
                            children.add(Label("实现类"))
                            children.add(implementClassNameTextField)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                        children.add(VBox().apply {
                            children.add(Label("自动连接时间段"))
                            children.add(autoConnectTimeRangesField)
                            HBox.setHgrow(this, Priority.ALWAYS)
                        })
                    })
                })

                children.add(Label("适配器配置JSON字符串"))
                children.add(adapterSettingJsonStringTextArea)

            }

        }

        Platform.runLater { targetSlaveNodeIdTextField.requestFocus() }

        val title = if (isNew) {
            "新增网关"
        } else {
            "修改网关${editGatewaySetting.id}"
        }

        val res = FXUtils.showInputDialog(title, contentPane)

        res.ifPresent { buttonType ->

            if (buttonType == ButtonType.OK) {
                editGatewaySetting.targetSlaveNodeId = targetSlaveNodeIdProperty.get()
                editGatewaySetting.name = nameProperty.get()
                editGatewaySetting.gatewayType = gatewayTypeProperty.get()
                editGatewaySetting.description = descriptionProperty.get()
                editGatewaySetting.autoConnectTimeRanges = autoConnectTimeRangesProperty.get()
                editGatewaySetting.implementClassName = implementClassNameProperty.get()
                editGatewaySetting.adapterSettingJsonString = adapterSettingJsonStringProperty.get()
                editGatewaySetting.connectionStatus = ConnectionStatusEnum.Disconnected
                editGatewaySetting.version = System.currentTimeMillis()
                viewState.gatewaySettingService.upsertGatewaySettingById(editGatewaySetting)
            }
        }


    }
}