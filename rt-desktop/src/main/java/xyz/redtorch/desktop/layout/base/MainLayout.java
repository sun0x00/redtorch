package xyz.redtorch.desktop.layout.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.redtorch.desktop.layout.charts.BasicMarketDataChartLayout;
import xyz.redtorch.desktop.service.GuiMainService;

@Component
public class MainLayout {

	private VBox vBox = new VBox();

	private boolean layoutCreated = false;

	@Autowired
	private PositionLayout positionLayout;
	@Autowired
	private AccountLayout accountLayout;
	@Autowired
	private TradeLayout tradeLayout;
	@Autowired
	private OrderLayout orderLayout;
	@Autowired
	private CombinationLayout combinationLayout;
	@Autowired
	private MarketDetailsLayout marketDetailsLayout;
	@Autowired
	private OrderPanelLayout orderPanelLayout;
	@Autowired
	private TickLayout tickLayout;
	@Autowired
	private ContractLayout contractLayout;
	@Autowired
	private LoginLayout loginLayout;
	@Autowired
	private BasicMarketDataChartLayout basicMarketDataChartLayout;
	@Autowired
	private GuiMainService guiMainService;

	private MenuItem loginMenuItem = new MenuItem("登录");
	Text statusBarLeftText = new Text("未知");
	Text statusBarRightText = new Text("---");

	public void onDisconnected() {
		Platform.runLater(() -> {
			loginMenuItem.setDisable(false);
			statusBarLeftText.setText("已经断开");
			statusBarLeftText.getStyleClass().clear();
			statusBarLeftText.getStyleClass().add("trade-long-color");
		});
	}

	public void onConnected() {
		Platform.runLater(() -> {
			loginMenuItem.setDisable(true);
			statusBarLeftText.setText("已经连接");
			statusBarLeftText.getStyleClass().clear();
			statusBarLeftText.getStyleClass().add("trade-short-color");
		});
	}

	public void onHeartbeat(String result) {
		Platform.runLater(() -> {
			statusBarRightText.setText(result);
		});
	}

	public Node getNode() {
		if (!layoutCreated) {
			createLayout();
			layoutCreated = true;
		}
		return this.vBox;
	}

	private void createLayout() {

		vBox.getChildren().add(crearteMainMenuBar());
		// 左右切分布局------------------------
		SplitPane horizontalSplitPane = new SplitPane();
		horizontalSplitPane.setDividerPositions(0.5);
		vBox.getChildren().add(horizontalSplitPane);
		VBox.setVgrow(horizontalSplitPane, Priority.ALWAYS);

		// 左右切分布局----左侧上下切分布局---------
		SplitPane leftVerticalSplitPane = new SplitPane();
		leftVerticalSplitPane.setDividerPositions(0.4);
		horizontalSplitPane.getItems().add(leftVerticalSplitPane);
		leftVerticalSplitPane.setOrientation(Orientation.VERTICAL);
		// 左右切分布局----左侧上下切分布局----上布局-----
		SplitPane leftTopHorizontalSplitPane = new SplitPane();
		leftVerticalSplitPane.getItems().add(leftTopHorizontalSplitPane);

		HBox leftTopRithtPane = new HBox();
		Node orderPanelLayoutNode = orderPanelLayout.getNode();
		HBox.setHgrow(orderPanelLayoutNode, Priority.ALWAYS);

		ScrollPane marketDetailsScrollPane = new ScrollPane();
		Node marketDetailsNode = marketDetailsLayout.getNode();
		marketDetailsScrollPane.setContent(marketDetailsNode);
		marketDetailsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		marketDetailsScrollPane.setPrefWidth(253);
		marketDetailsScrollPane.setMinWidth(253);
		marketDetailsScrollPane.setMaxWidth(253);
		leftTopRithtPane.getChildren().addAll(marketDetailsScrollPane, orderPanelLayoutNode);
		leftTopRithtPane.setMaxWidth(680);
		leftTopRithtPane.setPrefWidth(680);

		leftTopHorizontalSplitPane.getItems().addAll(tickLayout.getNode(), leftTopRithtPane);
		SplitPane.setResizableWithParent(leftTopRithtPane, false);

		// 左右切分布局----左侧上下切分布局----下布局-----
		TabPane leftBootomTabPane = new TabPane();
		leftVerticalSplitPane.getItems().add(leftBootomTabPane);

		Tab orderTab = new Tab("定单");
		leftBootomTabPane.getTabs().add(orderTab);
		orderTab.setClosable(false);

		orderTab.setContent(orderLayout.getNode());

		Tab tradeTab = new Tab("成交");
		leftBootomTabPane.getTabs().add(tradeTab);
		tradeTab.setClosable(false);

		tradeTab.setContent(tradeLayout.getNode());

		// 左右切分布局----右侧TAB布局-------------
		TabPane rightTabPane = new TabPane();
		horizontalSplitPane.getItems().add(rightTabPane);

		Tab portfolioInvestmentTab = new Tab("投资组合");
		rightTabPane.getTabs().add(portfolioInvestmentTab);
		portfolioInvestmentTab.setClosable(false);

		SplitPane portfolioVerticalSplitPane = new SplitPane();
		portfolioInvestmentTab.setContent(portfolioVerticalSplitPane);
		portfolioVerticalSplitPane.setOrientation(Orientation.VERTICAL);
		VBox.setVgrow(portfolioVerticalSplitPane, Priority.ALWAYS);

		VBox portfolioVBox = new VBox();
		portfolioVBox.getChildren().add(combinationLayout.getNode());

		portfolioVBox.getChildren().add(accountLayout.getNode());

		VBox.setVgrow(accountLayout.getNode(), Priority.ALWAYS);

		portfolioVerticalSplitPane.getItems().add(portfolioVBox);

		portfolioVerticalSplitPane.getItems().add(positionLayout.getNode());

		Tab allContractTab = new Tab("全部合约");
		allContractTab.setContent(contractLayout.getNode());
		rightTabPane.getTabs().add(allContractTab);
		allContractTab.setClosable(false);

		// 状态栏------------------------------
		vBox.getChildren().add(createStatusBar());
	}

	private HBox createStatusBar() {
		Pane statusBarCenterPane = new Pane();
		HBox statusBarHBox = new HBox();
		statusBarHBox.setPadding(new Insets(2, 5, 2, 5));
		statusBarHBox.getChildren().add(statusBarLeftText);
		statusBarHBox.getChildren().add(statusBarCenterPane);
		statusBarHBox.getChildren().add(statusBarRightText);
		HBox.setHgrow(statusBarCenterPane, Priority.ALWAYS);
		return statusBarHBox;
	}

	private MenuBar crearteMainMenuBar() {
		Menu sessionMenu = new Menu("会话");
		loginMenuItem.setOnAction(event -> {
			if (loginMenuItem.getUserData() == null || !(boolean) loginMenuItem.getUserData()) {
				loginMenuItem.setUserData(true);
				Stage loginStage = new Stage();
				VBox loginRootVBox = new VBox();
				loginStage.setScene(new Scene(loginRootVBox, 240, 120));
				loginRootVBox.getChildren().add(loginLayout.getNode());
				VBox.setVgrow(loginLayout.getNode(), Priority.ALWAYS);
				loginStage.setTitle("登录");
				loginStage.initModality(Modality.APPLICATION_MODAL);
				loginStage.initOwner(vBox.getScene().getWindow());
				loginStage.showAndWait();
				loginMenuItem.setUserData(false);
				loginRootVBox.getChildren().remove(loginLayout.getNode());
			}

		});
		sessionMenu.getItems().add(loginMenuItem);
		MenuItem reloadDataMenuItem = new MenuItem("重新加载数据");
		reloadDataMenuItem.setOnAction(event -> {
			guiMainService.reloadData();
		});
		sessionMenu.getItems().add(reloadDataMenuItem);

		Menu chartsGroupMenu = new Menu("图表");
		MenuItem basicMarketDataChartItem = new MenuItem("新建通用图表");
		basicMarketDataChartItem.setOnAction(event -> {
			basicMarketDataChartLayout.openBasicMarketDataChartWindow(vBox.getScene().getWindow());
		});
		chartsGroupMenu.getItems().addAll(basicMarketDataChartItem);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(sessionMenu);
		menuBar.getMenus().add(chartsGroupMenu);

		return menuBar;
	}

}
