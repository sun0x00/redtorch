package xyz.redtorch.desktop;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.service.impl.FastEventServiceImpl;
import xyz.redtorch.desktop.layout.base.MainLayout;

@SpringBootApplication
@Import({ FastEventServiceImpl.class })
public class RedTorchApp extends Application {

	private static final Logger logger = LoggerFactory.getLogger(RedTorchApp.class);

	private static ConfigurableApplicationContext springContext;

	private MainLayout mainLayout;

	@Override
	public void start(Stage stage) {
		try {
			System.setProperty("prism.lcdtext", "false");

			Scene scene = new Scene((Parent) mainLayout.getNode());
			scene.getStylesheets().add("main.css");
			stage.setHeight(1040);
			stage.setWidth(1920);
			stage.setTitle("RedTorch");
			stage.setScene(scene);
			stage.show();

		} catch (Exception e) {
			logger.error("启动失败", e);
		}
	}

	@Override
	public void init() throws Exception {
		springContext = SpringApplication.run(RedTorchApp.class);
		mainLayout = springContext.getBean(MainLayout.class);
	}

	public static void main(String[] args) {
		System.setProperty("program.start.timestamp", LocalDateTime.now().format(CommonConstant.DT_FORMAT_WITH_MS_INT_FORMATTER));
		launch();
	}

	@Override
	public void stop() throws Exception {
		springContext.stop();
		System.exit(0);
	}

}