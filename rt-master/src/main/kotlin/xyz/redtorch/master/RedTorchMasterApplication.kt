package xyz.redtorch.master

import javafx.application.Application
import javafx.scene.control.ButtonType
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import xyz.redtorch.common.cache.impl.CacheServiceImpl
import xyz.redtorch.master.gui.state.ViewState
import xyz.redtorch.master.gui.utils.FXUtils
import kotlin.system.exitProcess


@SpringBootApplication
@Import(value = [CacheServiceImpl::class])
open class RedTorchMasterApplication : Application() {

    private var springContext: ConfigurableApplicationContext? = null

    @Autowired
    private lateinit var viewState: ViewState

    override fun start(primaryStage: Stage?) {

        val stage = primaryStage!!

        // 视图状态组件设置主Stage
        viewState.primaryStage = primaryStage

        // 启动时切换到登录视图
        viewState.showLoginView()

        // 窗口标题
        stage.title = "服务器配置GUI"

        // 初始化并显示视图
        stage.apply {
            // 监听关闭按钮时间,弹框提醒
            setOnCloseRequest {
                // 获取弹框响应结果
                val res = FXUtils.showConfirmAlert("Quit Application", "确认关闭系统?", primaryStage.scene.window)

                if (!res) {
                    it.consume()
                }
            }
            // 显示
            show()
        }
    }

    @Throws(Exception::class)
    override fun init() {
        // 通过JavaFX启动Spring Boot
        springContext = SpringApplication.run(RedTorchMasterApplication::class.java)
        // 通过SpringContext获取视图状态组件
        viewState = springContext!!.getBean(ViewState::class.java)
    }

    @Throws(java.lang.Exception::class)
    override fun stop() {
        springContext!!.stop()
        exitProcess(0)
    }

}

fun main(args: Array<String>) {

    val argsSet = HashSet<String>().apply {
        addAll(args)
    }

    if (argsSet.contains("--nogui")) {
        // 无GUI启动
        // 直接启动SpringBoot
        SpringApplication.run(RedTorchMasterApplication::class.java)
    } else {
        // 启动GUI,由JavaFX启动SpringBoot
        Application.launch(RedTorchMasterApplication::class.java)
    }
}