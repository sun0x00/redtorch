package xyz.redtorch.master.gui.state

import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.master.db.ZookeeperService
import xyz.redtorch.master.gui.utils.FXUtils
import xyz.redtorch.master.gui.view.LoginView
import xyz.redtorch.master.gui.view.ManagementView
import xyz.redtorch.master.po.BackupPo
import xyz.redtorch.master.service.GatewaySettingService
import xyz.redtorch.master.service.SlaveNodeService
import xyz.redtorch.master.service.SystemService
import xyz.redtorch.master.service.UserService
import xyz.redtorch.master.web.socket.TradeClientWebSocketHandler


@Component
class ViewState {

    private val logger = LoggerFactory.getLogger(ViewState::class.java)

    @Autowired
    lateinit var slaveNodeService: SlaveNodeService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var gatewaySettingService: GatewaySettingService

    @Autowired
    lateinit var zookeeperService: ZookeeperService

    @Autowired
    lateinit var systemService: SystemService

    @Autowired
    lateinit var tradeClientWebSocketHandler: TradeClientWebSocketHandler

    @Value("\${rt.gui-password}")
    lateinit var guiPassword: String

    var primaryStage: Stage? = null

    fun authPassword(password: String?): Boolean {
        if (password == guiPassword) {
            showManagementView()
            return true
        }
        return false
    }

    fun showLoginView() {
        val widthHeight = getWidthHeight()
        val root = LoginView(this).view
        primaryStage!!.scene = Scene(root, widthHeight.first, widthHeight.second).apply {
            stylesheets.add("fx/style/main.css")
        }
    }

    fun showManagementView() {
        val widthHeight = getWidthHeight()
        val root = ManagementView(this).view
        primaryStage!!.scene = Scene(root, widthHeight.first, widthHeight.second).apply {
            stylesheets.add("fx/style/main.css")
        }
    }

    /**
     * 获取当前宽高，如果获取不到数据，则设置默认宽高
     */
    fun getWidthHeight(): Pair<Double, Double> {
        val width =
            if (primaryStage!!.scene == null || primaryStage!!.scene.width.isNaN()) 800.0 else primaryStage!!.scene.width
        val height =
            if (primaryStage!!.scene == null || primaryStage!!.scene.height.isNaN()) 600.0 else primaryStage!!.scene.height
        return Pair(width, height)
    }

    fun importData() {
        try {
            val fileChooser = FileChooser()
            fileChooser.title = "打开备份文件"
            fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("JSON Files", "*.json"),
            )
            val selectedFile = fileChooser.showOpenDialog(primaryStage!!.scene.window)
            if (selectedFile != null) {
                val backupPoJsonStr = selectedFile.readText()

                val backupPo = JsonUtils.readToObject(backupPoJsonStr, BackupPo::class.java)
                for (user in backupPo.userMap.values) {
                    userService.upsertUserById(user)
                }

                for (gatewaySetting in backupPo.gatewaySettingMap.values) {
                    gatewaySettingService.upsertGatewaySettingById(gatewaySetting)
                }

                for (slaveNode in backupPo.slaveNodeMap.values) {
                    slaveNodeService.upsertSlaveNodeById(slaveNode)
                }

                FXUtils.showConfirmAlert("提示", "导入数据完成", primaryStage!!.scene.window,Alert.AlertType.INFORMATION)
            }
        } catch (e: Exception) {
            logger.error("导入数据异常", e)
            FXUtils.showConfirmAlert("错误提示", "导入数据异常", primaryStage!!.scene.window)
        }

    }

    fun exportData() {
        try {
            val userList = userService.getUserList()
            val gatewaySettingList = gatewaySettingService.getGatewaySettingList()
            val slaveNodeList = slaveNodeService.getSlaveNodeList()
            val backupPo = BackupPo()
            for (user in userList) {
                backupPo.userMap[user.id!!] = user
            }
            for (gatewaySetting in gatewaySettingList) {
                backupPo.gatewaySettingMap[gatewaySetting.id!!] = gatewaySetting
            }
            for (slaveNode in slaveNodeList) {
                backupPo.slaveNodeMap[slaveNode.id!!] = slaveNode
            }
            val backupPoJsonString = JsonUtils.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(backupPo)

            val fileChooser = FileChooser()
            fileChooser.title = "保存备份文件"
            fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("JSON Files", "*.json"),
            )

            fileChooser.showSaveDialog(primaryStage!!.scene.window)?.writeText(backupPoJsonString)
            FXUtils.showConfirmAlert("提示", "导出数据完成", primaryStage!!.scene.window, Alert.AlertType.INFORMATION)
        } catch (e: Exception) {
            logger.error("导出数据异常", e)
            FXUtils.showConfirmAlert("错误提示", "导出数据异常", primaryStage!!.scene.window)
        }

    }


}