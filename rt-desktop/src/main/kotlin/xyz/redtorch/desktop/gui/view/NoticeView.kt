package xyz.redtorch.desktop.gui.view

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.Separator
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import xyz.redtorch.common.constant.Constant
import xyz.redtorch.common.sync.dto.Notice
import xyz.redtorch.common.sync.enumeration.InfoLevelEnum
import xyz.redtorch.common.utils.CommonUtils
import xyz.redtorch.desktop.gui.state.ViewState

class NoticeView(private val viewState: ViewState) {
    val view = VBox()

    private val contentVBox = VBox()

    init {
        view.apply {
            children.add(ScrollPane().apply {
                VBox.setVgrow(this, Priority.ALWAYS)
                content = contentVBox
            })
            children.add(HBox(10.0).apply {
                children.add(Button("清除所有通知").apply {
                    onMousePressed = EventHandler { e: MouseEvent ->
                        if (e.button == MouseButton.PRIMARY) {
                            contentVBox.children.clear()
                        }
                    }
                })
            })
        }
    }

    fun addNotice(notice: Notice) {

        val datetime = CommonUtils.millsToLocalDateTime(notice.timestamp).format(Constant.DT_FORMAT_WITH_MS_FORMATTER)
        val line = Text("${datetime} >>> ${notice.info}")

        when (notice.infoLevel) {
            InfoLevelEnum.CRITICAL -> {
                line.styleClass.add("trade-long-color-background")
            }
            InfoLevelEnum.ERROR -> {

                line.styleClass.add("trade-long-color")
            }
            InfoLevelEnum.WARN -> {

                line.styleClass.add("trade-remind-color")
            }
            else -> {
                line.styleClass.add("trade-info-color")
            }
        }
        Platform.runLater {
            contentVBox.children.addAll(line)
            contentVBox.children.addAll(Separator(Orientation.HORIZONTAL))
        }
    }
}