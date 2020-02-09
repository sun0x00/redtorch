package xyz.redtorch.desktop.layout.base;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import xyz.redtorch.desktop.service.AuthService;
import xyz.redtorch.desktop.web.socket.WebSocketClientHandler;

@Component
public class LoginLayout {

	private static final Logger logger = LoggerFactory.getLogger(LoginLayout.class);

	private VBox vBox = new VBox();

	private boolean layountCreated = false;

	@Autowired
	private AuthService authService;

	@Autowired
	private WebSocketClientHandler webSocketClientHandler;

	public Node getNode() {
		if (!layountCreated) {
			createLayout();
			layountCreated = true;
		}
		return vBox;
	}

	private void createLayout() {
		vBox.setPadding(new Insets(5));

		vBox.getChildren().add(new Label("用户名"));
		TextField usernameTextField = new TextField();
		vBox.getChildren().add(usernameTextField);

		vBox.getChildren().add(new Label("密码"));
		PasswordField passwordField = new PasswordField();
		vBox.getChildren().add(passwordField);

		Button loginButton = new Button("登录");
		loginButton.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					loginButton.setDisable(true);
					try {
						if (StringUtils.isBlank(usernameTextField.getText())) {
							Alert priceAlert = new Alert(AlertType.ERROR);
							priceAlert.setTitle("错误");
							priceAlert.setHeaderText("字段错误");
							priceAlert.setContentText("用户名不可为空!");
							priceAlert.showAndWait();
						} else if (StringUtils.isBlank(passwordField.getText())) {
							Alert priceAlert = new Alert(AlertType.ERROR);
							priceAlert.setTitle("错误");
							priceAlert.setHeaderText("字段错误");
							priceAlert.setContentText("密码不可为空!");
							priceAlert.showAndWait();
						} else {
							if (authService.login(usernameTextField.getText(), passwordField.getText())) {
								webSocketClientHandler.connectRtWebSocketClient();
								Stage stage = (Stage) vBox.getScene().getWindow();
								stage.close();
							} else {

								Alert priceAlert = new Alert(AlertType.ERROR);
								priceAlert.setTitle("错误");
								priceAlert.setHeaderText("登录错误");
								priceAlert.setContentText("服务器连接失败或用户名密码错误!");
								priceAlert.showAndWait();
							}
						}
					} catch (Exception e) {

						Alert priceAlert = new Alert(AlertType.ERROR);
						priceAlert.setTitle("错误");
						priceAlert.setHeaderText("异常");
						priceAlert.setContentText("登录异常!");
						priceAlert.showAndWait();
						logger.error("登录错误", e);
					}

					loginButton.setDisable(false);

				}
			}
		});
		vBox.getChildren().add(loginButton);
	}
}
