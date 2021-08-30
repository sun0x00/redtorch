package xyz.redtorch.master.gui.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import xyz.redtorch.master.gui.state.ViewState

class LoginView(private val viewState: ViewState) {

    private var passwordProperty = SimpleStringProperty()
    private var promptProperty = SimpleStringProperty()

    val view = BorderPane().apply {
        padding = Insets(40.0)
        center = VBox(20.0).apply {
            alignment = Pos.CENTER
            children.add(HBox(20.0).apply {
                alignment = Pos.CENTER
                children.addAll(Label("密码"), PasswordField().apply {
                    textProperty().bindBidirectional(passwordProperty)
                })
            })
            children.add(HBox().apply {
                alignment = Pos.CENTER
                children.add(Label().apply {
                    textProperty().bindBidirectional(promptProperty)
                })
            })
            children.add(HBox().apply {
                alignment = Pos.CENTER
                children.add(Button("解锁").apply {
                    setOnAction {
                        if (viewState.authPassword(passwordProperty.value)) {
                            promptProperty.set("")
                        } else {
                            promptProperty.set("验证失败")
                        }
                    }
                })
            })
        }
    }
}

