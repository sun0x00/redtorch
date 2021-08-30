package xyz.redtorch.desktop.gui.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import xyz.redtorch.desktop.gui.state.ViewState

class LoginView(private val viewState: ViewState) {

    companion object {
        private val logger = LoggerFactory.getLogger(LoginView::class.java)
    }

    private var userIdProperty = SimpleStringProperty()
    private var authTokenProperty = SimpleStringProperty()
    private var promptProperty = SimpleStringProperty()
    val view = BorderPane().apply {
        padding = Insets(40.0)
        center = VBox(20.0).apply {
            alignment = Pos.CENTER
            children.add(HBox(20.0).apply {
                alignment = Pos.CENTER
                children.addAll(Label("ID").apply { prefWidth = 40.0 }, TextField().apply {
                    textProperty().bindBidirectional(userIdProperty)
                })
            })
            children.add(HBox(20.0).apply {
                alignment = Pos.CENTER
                children.addAll(Label("Token").apply { prefWidth = 40.0 }, PasswordField().apply {
                    textProperty().bindBidirectional(authTokenProperty)
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
                children.add(Button("登录").apply {
                    setOnAction {
                        this.isDisable = true
                        try {
                            if (viewState.auth(userIdProperty.value, authTokenProperty.value)) {
                                promptProperty.set("")
                            } else {
                                promptProperty.set("验证失败")
                            }
                        } catch (e: Exception) {
                            logger.error("登录异常", e)
                        } finally {
                            this.isDisable = false
                        }
                    }
                })
            })
        }
    }
}

