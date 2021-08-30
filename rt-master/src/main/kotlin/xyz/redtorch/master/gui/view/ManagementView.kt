package xyz.redtorch.master.gui.view

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import xyz.redtorch.master.gui.state.ViewState

class ManagementView(private val viewState: ViewState) {
    private val container = HBox()

    val view = VBox().apply {
        children.add(createMenuBar())
        children.add(container)
        VBox.setVgrow(container, Priority.ALWAYS)
    }

    init {
        showUserSessionView()
    }

    private fun createMenuBar(): MenuBar {
        val securityMenu = Menu("安全").apply {
            items.add(MenuItem("锁定管理端").apply {
                setOnAction {
                    viewState.showLoginView()
                }
            })
            items.add(MenuItem("会话列表").apply {
                setOnAction {
                    showUserSessionView()
                }
            })
        }

        val manageMenu = Menu("管理").apply {
            items.add(MenuItem("用户").apply {
                setOnAction {
                    showUserView()
                }
            })
            items.add(MenuItem("节点").apply {
                setOnAction {
                    showSlaveNodeView()
                }
            })
            items.add(MenuItem("网关").apply {
                setOnAction {
                    showGatewayView()
                }
            })
        }

        val dataMenu = Menu("数据").apply {
            items.add(MenuItem("导出备份配置数据").apply {
                setOnAction {
                    viewState.exportData()
                }
            })
            items.add(MenuItem("导入覆盖配置数据").apply {
                setOnAction {
                    viewState.importData()
                }
            })
            items.add(MenuItem("Zookeeper数据管理").apply {
                setOnAction {
                    showZookeeperView()
                }
            })
        }

        return MenuBar().apply {
            menus.add(securityMenu)
            menus.add(manageMenu)
            menus.add(dataMenu)
        }
    }

    private fun showZookeeperView() {
        container.children.clear()
        container.children.add(ZookeeperView(viewState).view.apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        })
    }

    private fun showGatewayView() {
        container.children.clear()
        container.children.add(GatewaySettingView(viewState).view.apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        })
    }

    private fun showSlaveNodeView() {
        container.children.clear()
        container.children.add(SlaveNodeView(viewState).view.apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        })
    }

    private fun showUserView() {
        container.children.clear()
        container.children.add(UserView(viewState).view.apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        })
    }

    private fun showUserSessionView() {
        container.children.clear()
        container.children.add(UserSessionView(viewState).view.apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        })
    }

}