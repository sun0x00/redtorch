package xyz.redtorch.master.gui.utils

import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.stage.Window
import java.util.*

object FXUtils {
    fun showConfirmAlert(titleStr: String, contentStr: String, ownerWindow: Window?,alertType: Alert.AlertType = Alert.AlertType.WARNING): Boolean {
        val res = Alert(alertType).apply {
            title = titleStr
            contentText = contentStr
            buttonTypes.remove(ButtonType.OK)
            buttonTypes.add(ButtonType.CANCEL)
            buttonTypes.add(ButtonType.YES)
            initOwner(ownerWindow)
        }.showAndWait()
        return res.isPresent && res.get() == ButtonType.YES
    }

    fun showInputDialog(titleStr: String, contentNode: Node): Optional<ButtonType> {
        return Dialog<ButtonType>().apply {
            isResizable = true
            title = titleStr
            // Set the button types.
            dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
            dialogPane.content = contentNode
            // 此处可选择替换返回类型
            setResultConverter { dialogButton -> dialogButton }
        }.showAndWait()
    }

}