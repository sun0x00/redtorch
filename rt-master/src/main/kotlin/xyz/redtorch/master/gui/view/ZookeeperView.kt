package xyz.redtorch.master.gui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.common.utils.STD3DesUtils
import xyz.redtorch.master.gui.state.ViewState
import xyz.redtorch.master.gui.utils.FXUtils
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime


class ZookeeperView(private val viewState: ViewState) {

    companion object {
        private val logger = LoggerFactory.getLogger(ZookeeperView::class.java)
    }

    val view = VBox(10.0)

    private val pathProperty = SimpleStringProperty("")
    private val encryptedProperty = SimpleBooleanProperty(true)
    private val jsonFormatProperty = SimpleBooleanProperty(true)
    private val textAreaProperty = SimpleStringProperty("")
    private val promptProperty = SimpleStringProperty("")


    init {
        view.apply {
            padding = Insets(5.0)
            children.add(Label("路径"))
            children.add(TextField().apply {
                textProperty().bindBidirectional(pathProperty)
            })
            children.add(Separator(Orientation.HORIZONTAL))
            children.add(HBox(10.0).apply {
                children.add(Button("删除").apply {
                    setOnAction {
                        setPrompt("开始删除")
                        val path = pathProperty.get()
                        var res = FXUtils.showConfirmAlert("删除确认", "确认删除 $path ?", viewState.primaryStage!!.scene.window)
                        if (res) {
                            res = FXUtils.showConfirmAlert("二次确认", "确认删除 $path ?", viewState.primaryStage!!.scene.window)

                            if(res){
                                try {
                                    val text = if (path.isNullOrBlank()) {
                                        "删除错误,路径不可为空"
                                    } else if (!path.startsWith("/")) {
                                        "删除错误,路径必须以/开头"
                                    } else if (path.length > 1 && path.endsWith("/")) {
                                        "删除错误,路径不能以/结尾"
                                    } else {
                                        val zkUtils = viewState.zookeeperService.getZkUtils()

                                        if (zkUtils.deleteNode(path)) {
                                            "删除成功"
                                        } else {
                                            "删除返回失败"
                                        }
                                    }
                                    textAreaProperty.set(text)
                                } catch (e: Exception) {
                                    logger.error("删除异常", e)

                                    val sw = StringWriter()
                                    e.printStackTrace(PrintWriter(sw))
                                    val exceptionAsString = sw.toString()
                                    textAreaProperty.set("删除异常\n$exceptionAsString")
                                }
                            }
                        }
                        setPrompt("删除完成")
                    }
                })
                children.add(Button("递归删除").apply {
                    setOnAction {
                        setPrompt("开始递归删除")
                        val path = pathProperty.get()

                        var res = FXUtils.showConfirmAlert("递归删除确认", "确认递归删除 $path ?此操作将删除此节点及子节点!", viewState.primaryStage!!.scene.window)
                        if (res) {

                            res = FXUtils.showConfirmAlert("二次确认", "确认递归删除 $path ?此操作将删除此节点及子节点!", viewState.primaryStage!!.scene.window)

                            if(res){
                                try {
                                    val text = if (path.isNullOrBlank()) {
                                        "递归删除错误,路径不可为空"
                                    } else if (!path.startsWith("/")) {
                                        "递归删除错误,路径必须以/开头"
                                    } else if (path.length > 1 && path.endsWith("/")) {
                                        "递归删除错误,路径不能以/结尾"
                                    } else {
                                        val zkUtils = viewState.zookeeperService.getZkUtils()

                                        if (zkUtils.recursiveDeleteNode(path)) {
                                            "递归删除成功"
                                        } else {
                                            "递归删除返回失败"
                                        }
                                    }
                                    textAreaProperty.set(text)
                                } catch (e: Exception) {
                                    logger.error("递归删除异常", e)

                                    val sw = StringWriter()
                                    e.printStackTrace(PrintWriter(sw))
                                    val exceptionAsString = sw.toString()
                                    textAreaProperty.set("递归删除异常\n$exceptionAsString")
                                }
                            }
                        }
                        setPrompt("递归删除完成")
                    }
                })
                children.add(Separator(Orientation.VERTICAL))
                children.add(Button("列出子节点").apply {
                    setOnAction {
                        setPrompt("开始列出子节点")
                        try {
                            val path = pathProperty.get()
                            var text = "未查出数据"
                            if (path.isNullOrBlank()) {
                                text = "列出子节点错误,路径不可为空"
                            } else if (!path.startsWith("/")) {
                                text = "列出子节点错误,路径必须以/开头"
                            } else if (path.length > 1 && path.endsWith("/")) {
                                text = "列出子节点错误,路径不能以/结尾"
                            } else {
                                val zkUtils = viewState.zookeeperService.getZkUtils()
                                val nodeList = zkUtils.getChildrenNodeList(path)
                                nodeList?.let {
                                    if (it.isNotEmpty()) {
                                        text = ""
                                        for (node in nodeList) {
                                            text = text + node + "\n"
                                        }
                                        text.trimEnd()
                                    }
                                }
                            }
                            textAreaProperty.set(text)
                        } catch (e: Exception) {
                            logger.error("列出子节点异常", e)

                            val sw = StringWriter()
                            e.printStackTrace(PrintWriter(sw))
                            val exceptionAsString = sw.toString()
                            textAreaProperty.set("列出子节点异常\n$exceptionAsString")
                        }
                        setPrompt("列出子节点完成")
                    }
                })
                children.add(Separator(Orientation.VERTICAL))
                children.add(CheckBox("是否加解密").apply {
                    selectedProperty().bindBidirectional(encryptedProperty)
                })
                children.add(CheckBox("作为JSON格式化").apply {
                    selectedProperty().bindBidirectional(jsonFormatProperty)
                })
                children.add(Button("读取数据").apply {
                    setOnAction {
                        setPrompt("开始读取数据")
                        try {
                            val path = pathProperty.get()
                            logger.info("读取数据,path={}", path)
                            if (path.isNullOrBlank() || !path.startsWith("/")) {
                                logger.error("读取数据错误,路径不符合要求,path={}", path)
                            }
                            val zkUtils = viewState.zookeeperService.getZkUtils()
                            val data = zkUtils.getNodeData(path)
                            var text = "未查出数据"
                            data?.let {
                                if (encryptedProperty.get() && it.isNotBlank()) {
                                    try {
                                        text = STD3DesUtils.des3DecodeECBBase64String(viewState.zookeeperService.get3DesKey(), it)
                                    } catch (e: Exception) {
                                        logger.error("解密异常", e)

                                        val sw = StringWriter()
                                        e.printStackTrace(PrintWriter(sw))
                                        val exceptionAsString = sw.toString()
                                        textAreaProperty.set("列出子节点异常\n$exceptionAsString")

                                        text = "原文\n$it\n\n解密异常\n$exceptionAsString"
                                        return@let
                                    }
                                } else {
                                    text = data
                                }

                                if (jsonFormatProperty.get() && text.isNotBlank()) {
                                    text = try {
                                        JsonUtils.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(JsonUtils.readToJsonNode(text))
                                    } catch (e: Exception) {
                                        logger.error("JSON格式化异常", e)

                                        val sw = StringWriter()
                                        e.printStackTrace(PrintWriter(sw))
                                        val exceptionAsString = sw.toString()
                                        textAreaProperty.set("列出子节点异常\n$exceptionAsString")

                                        "原文\n$text,\n\nJSON格式化异常\n$exceptionAsString"
                                    }
                                }
                            }
                            textAreaProperty.set(text)

                        } catch (e: Exception) {
                            logger.error("读取数据异常", e)

                            val sw = StringWriter()
                            e.printStackTrace(PrintWriter(sw))
                            val exceptionAsString = sw.toString()
                            textAreaProperty.set("读取数据异常\n$exceptionAsString")
                        }

                        setPrompt("读取数据完成")
                    }
                })
                children.add(Button("写入或更新数据").apply {
                    setOnAction {
                        setPrompt("开始写入或更新数据")
                        val path = pathProperty.get()

                        val alertContent = if (encryptedProperty.get()) {
                            "确认将数据加密写入或更新 $path ？"
                        } else {
                            "确认将数据明文写入或更新 $path ？"
                        }

                        var res = FXUtils.showConfirmAlert("写入或更新确认", alertContent, viewState.primaryStage!!.scene.window)

                        try {
                            if (res) {
                                res = FXUtils.showConfirmAlert("二次确认", alertContent, viewState.primaryStage!!.scene.window)

                                if(res){
                                    logger.info("写入或更新数据,path={}", path)
                                    if (path.isNullOrBlank() || !path.startsWith("/")) {
                                        logger.error("写入或更新数据错误,路径不符合要求,path={}", path)
                                    }
                                    val zkUtils = viewState.zookeeperService.getZkUtils()
                                    var data = textAreaProperty.get()

                                    if (encryptedProperty.get() && data.isNotBlank()) {
                                        data = STD3DesUtils.des3EncodeECBBase64String(viewState.zookeeperService.get3DesKey(), data)
                                    }

                                    if (zkUtils.exists(path)) {
                                        zkUtils.updateNode(path, data)
                                    } else {
                                        zkUtils.addPersistentNode(path, data)
                                    }
                                }
                            }
                            setPrompt("写入或更新数据完成")
                        } catch (e: Exception) {
                            logger.error("写入或更新数据异常", e)
                            setPrompt("写入或更新数据异常!!! ${e.message}")
                        }
                    }
                })

            })
            children.add(Separator(Orientation.HORIZONTAL))
            children.add(TextArea().apply {
                VBox.setVgrow(this, Priority.ALWAYS)
                textProperty().bindBidirectional(textAreaProperty)
            })
            children.add(Text().apply {
                textProperty().bindBidirectional(promptProperty)
            })
        }
    }

    private fun setPrompt(str: String) {
        promptProperty.set(LocalDateTime.now().format(Constant.DT_FORMAT_WITH_MS_FORMATTER) + ">>>" + str)
    }

}