package xyz.redtorch.desktop.layout.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

@Component
public class TickLayout {

	private static final Logger logger = LoggerFactory.getLogger(TickLayout.class);

	private VBox vBox = new VBox();

	private boolean layoutCreated = false;

	private ObservableList<TickField> tickObservableList = FXCollections.observableArrayList();

	private List<TickField> tickList = new ArrayList<>();

	private TableView<TickField> tickTableView = new TableView<>();

	private Set<String> selectedTickUnifiedSymbolSet = new HashSet<>();

	@Autowired
	private GuiMainService guiMainService;

	@Autowired
	private DesktopTradeCachesService desktopTradeCachesService;

	public Node getNode() {
		if (!layoutCreated) {
			createLayout();
			layoutCreated = true;
		}
		return this.vBox;
	}

	public void updateData(List<TickField> tickList) {
		if (!new HashSet<>(this.tickList).equals(new HashSet<>(tickList))) {
			this.tickList = tickList;
			fillingData();
		}
	}

	public void fillingData() {
		tickTableView.getSelectionModel().clearSelection();

		tickObservableList.clear();
		tickObservableList.addAll(this.tickList);
		Set<String> newSelectedTickIdSet = new HashSet<>();
		for (TickField tick : tickObservableList) {
			if (selectedTickUnifiedSymbolSet.contains(tick.getUnifiedSymbol())) {
				tickTableView.getSelectionModel().select(tick);
				newSelectedTickIdSet.add(tick.getUnifiedSymbol());
			}
		}
		selectedTickUnifiedSymbolSet = newSelectedTickIdSet;
	}

	private void createLayout() {

		tickTableView.setTableMenuButtonVisible(true);

		TableColumn<TickField, Pane> unifiedSymbolCol = new TableColumn<>("合约");
		unifiedSymbolCol.setPrefWidth(160);
		unifiedSymbolCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {

				TickField tick = feature.getValue();
				ContractField contract = desktopTradeCachesService.queryContractByUnifiedSymbol(tick.getUnifiedSymbol());

				String contractName = tick.getUnifiedSymbol();
				if (contract != null) {
					contractName = contract.getName();
				}
				Text unifiedSymbolText = new Text(tick.getUnifiedSymbol());
				Text nameText = new Text(contractName);
				vBox.getChildren().add(unifiedSymbolText);
				vBox.getChildren().add(nameText);

				if (guiMainService.getSelectedContract() != null && guiMainService.getSelectedContract().getUnifiedSymbol().equals(tick.getUnifiedSymbol())) {
					unifiedSymbolText.getStyleClass().add("trade-remind-color");
				}

				vBox.setUserData(tick);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleObjectProperty<>(vBox);
		});

		unifiedSymbolCol.setComparator((Pane p1, Pane p2) -> {
			try {
				TickField tick1 = (TickField) p1.getUserData();
				TickField tick2 = (TickField) p2.getUserData();
				return StringUtils.compare(tick1.getUnifiedSymbol(), tick2.getUnifiedSymbol());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		tickTableView.getColumns().add(unifiedSymbolCol);

		TableColumn<TickField, Pane> lastPriceCol = new TableColumn<>("最新价格");
		lastPriceCol.setPrefWidth(120);
		lastPriceCol.setCellValueFactory(feature -> {

			VBox vBox = new VBox();

			try {
				TickField tick = feature.getValue();

				ContractField contract = desktopTradeCachesService.queryContractByUnifiedSymbol(tick.getUnifiedSymbol());

				int dcimalDigits = 4;
				if (contract != null) {
					dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
					if (dcimalDigits < 0) {
						dcimalDigits = 4;
					}
				}

				String priceStringFormat = "%,." + dcimalDigits + "f";

				Double basePrice = tick.getPreSettlePrice();
				if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
					basePrice = tick.getPreClosePrice();
				}
				if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
					basePrice = tick.getOpenPrice();
				}

				Double lastPrice = tick.getLastPrice();

				Double priceDiff = 0d;

				String lastPriceStr = "-";
				String pctChangeStr = "-";
				String colorStyleClass = "";

				if (lastPrice == Double.MAX_VALUE || basePrice == 0 || basePrice == Double.MAX_VALUE) {
					lastPriceStr = "-";
				} else {
					priceDiff = lastPrice - basePrice;
					Double pctChange = priceDiff / basePrice;
					pctChangeStr = String.format("%,.2f%%", pctChange * 100);
					lastPriceStr = String.format(priceStringFormat, lastPrice);
					if (priceDiff > 0) {
						colorStyleClass = "trade-long-color";
					}
					if (priceDiff < 0) {
						colorStyleClass = "trade-short-color";
					}
				}

				HBox lastPriceHBox = new HBox();
				Text lastPriceLabelText = new Text("最新");
				lastPriceLabelText.setWrappingWidth(35);
				lastPriceLabelText.getStyleClass().add("trade-label");
				lastPriceHBox.getChildren().add(lastPriceLabelText);

				Text lastPriceText = new Text(lastPriceStr);
				lastPriceHBox.getChildren().add(lastPriceText);
				lastPriceText.getStyleClass().add(colorStyleClass);
				vBox.getChildren().add(lastPriceHBox);

				HBox pctChangeHBox = new HBox();
				Text pctChangeLabelText = new Text("涨跌");
				pctChangeLabelText.setWrappingWidth(35);
				pctChangeLabelText.getStyleClass().add("trade-label");
				pctChangeHBox.getChildren().add(pctChangeLabelText);

				Text pctChangeText = new Text(pctChangeStr);
				pctChangeText.getStyleClass().add(colorStyleClass);
				pctChangeHBox.getChildren().add(pctChangeText);
				vBox.getChildren().add(pctChangeHBox);

				vBox.setUserData(tick);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		lastPriceCol.setComparator((Pane p1, Pane p2) -> {
			try {
				TickField tick1 = (TickField) p1.getUserData();
				TickField tick2 = (TickField) p2.getUserData();
				return Double.compare(tick1.getLastPrice(), tick2.getLastPrice());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		tickTableView.getColumns().add(lastPriceCol);

		TableColumn<TickField, Pane> askPrice1BidPrice1Col = new TableColumn<>("买卖一价");
		askPrice1BidPrice1Col.setPrefWidth(70);
		askPrice1BidPrice1Col.setCellValueFactory(feature -> {

			VBox vBox = new VBox();

			try {
				TickField tick = feature.getValue();

				ContractField contract = desktopTradeCachesService.queryContractByUnifiedSymbol(tick.getUnifiedSymbol());

				int dcimalDigits = 4;
				if (contract != null) {
					dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
					if (dcimalDigits < 0) {
						dcimalDigits = 4;
					}
				}

				String priceStringFormat = "%,." + dcimalDigits + "f";

				Double basePrice = tick.getPreSettlePrice();
				if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
					basePrice = tick.getPreClosePrice();
				}
				if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
					basePrice = tick.getOpenPrice();
				}

				String askPrice1Str = "-";
				String bidPrice1Str = "-";
				String askPrice1ColorStyleClass = "";
				String bidPrice1ColorStyleClass = "";

				if (tick.getAskPriceList().size() > 0 && tick.getAskPriceList().get(0) != Double.MAX_VALUE) {
					askPrice1Str = String.format(priceStringFormat, tick.getAskPriceList().get(0));
					if (tick.getAskPriceList().get(0) > basePrice) {
						askPrice1ColorStyleClass = "trade-long-color";
					} else if (tick.getAskPriceList().get(0) < basePrice) {
						askPrice1ColorStyleClass = "trade-short-color";
					}
				}

				if (tick.getBidPriceList().size() > 0 && tick.getBidPriceList().get(0) != Double.MAX_VALUE) {
					bidPrice1Str = String.format(priceStringFormat, tick.getBidPriceList().get(0));
					if (tick.getBidPriceList().get(0) > basePrice) {
						bidPrice1ColorStyleClass = "trade-long-color";
					} else if (tick.getBidPriceList().get(0) < basePrice) {
						bidPrice1ColorStyleClass = "trade-short-color";
					}
				}
				Text askPrice1Text = new Text(askPrice1Str);
				askPrice1Text.getStyleClass().add(askPrice1ColorStyleClass);
				Text bidPrice1Text = new Text(bidPrice1Str);
				bidPrice1Text.getStyleClass().add(bidPrice1ColorStyleClass);
				vBox.getChildren().addAll(askPrice1Text, bidPrice1Text);

				vBox.setUserData(tick);

			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		askPrice1BidPrice1Col.setSortable(false);
		tickTableView.getColumns().add(askPrice1BidPrice1Col);

		TableColumn<TickField, Pane> askVolume1BidVolume1Col = new TableColumn<>("买卖一量");
		askVolume1BidVolume1Col.setPrefWidth(70);
		askVolume1BidVolume1Col.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {
				TickField tick = feature.getValue();

				String askVolume1Str = "-";
				String bidVolume1Str = "-";
				String askVolume1ColorStyleClass = "trade-remind-color";
				String bidVolume1ColorStyleClass = "trade-remind-color";

				if (tick.getAskVolumeList().size() > 0) {
					askVolume1Str = "" + tick.getAskVolumeList().get(0);
				}

				if (tick.getBidVolumeList().size() > 0) {
					bidVolume1Str = "" + tick.getBidVolumeList().get(0);
				}
				Text askVolume1Text = new Text(askVolume1Str);
				askVolume1Text.getStyleClass().add(askVolume1ColorStyleClass);
				Text bidVolume1Text = new Text(bidVolume1Str);
				bidVolume1Text.getStyleClass().add(bidVolume1ColorStyleClass);
				vBox.getChildren().addAll(askVolume1Text, bidVolume1Text);
				vBox.setUserData(tick);

			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleObjectProperty<>(vBox);
		});

		askVolume1BidVolume1Col.setSortable(false);
		tickTableView.getColumns().add(askVolume1BidVolume1Col);

		TableColumn<TickField, Pane> limitPriceCol = new TableColumn<>("涨跌停");
		limitPriceCol.setPrefWidth(70);
		limitPriceCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();

			try {
				TickField tick = feature.getValue();

				ContractField contract = desktopTradeCachesService.queryContractByUnifiedSymbol(tick.getUnifiedSymbol());

				int dcimalDigits = 4;
				if (contract != null) {
					dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
					if (dcimalDigits < 0) {
						dcimalDigits = 4;
					}
				}

				String priceStringFormat = "%,." + dcimalDigits + "f";

				String upperLimitStr = "-";
				String lowerLimitStr = "-";
				String upperLimitColorStyleClass = "trade-long-color";
				String lowerLimitColorStyleClass = "trade-short-color";

				if (tick.getUpperLimit() != Double.MAX_VALUE) {
					upperLimitStr = String.format(priceStringFormat, tick.getUpperLimit());
				}

				if (tick.getLowerLimit() != Double.MAX_VALUE) {
					lowerLimitStr = String.format(priceStringFormat, tick.getLowerLimit());
				}
				Text upperLimitText = new Text(upperLimitStr);
				upperLimitText.getStyleClass().add(upperLimitColorStyleClass);
				Text lowerLimitText = new Text(lowerLimitStr);
				lowerLimitText.getStyleClass().add(lowerLimitColorStyleClass);
				vBox.getChildren().addAll(upperLimitText, lowerLimitText);
				vBox.setUserData(tick);

			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		limitPriceCol.setSortable(false);
		tickTableView.getColumns().add(limitPriceCol);

		SortedList<TickField> sortedItems = new SortedList<>(tickObservableList);
		tickTableView.setItems(sortedItems);
		sortedItems.comparatorProperty().bind(tickTableView.comparatorProperty());

		tickTableView.getSortOrder().add(unifiedSymbolCol);

		tickTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		tickTableView.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				ObservableList<TickField> selectedItems = tickTableView.getSelectionModel().getSelectedItems();
				selectedTickUnifiedSymbolSet.clear();
				for (TickField row : selectedItems) {
					selectedTickUnifiedSymbolSet.add(row.getUnifiedSymbol());
				}
			}
		});

		tickTableView.setRowFactory(tv -> {
			TableRow<TickField> row = new TableRow<>();
			row.setOnMousePressed(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
					ObservableList<TickField> selectedItems = tickTableView.getSelectionModel().getSelectedItems();
					selectedTickUnifiedSymbolSet.clear();
					for (TickField tick : selectedItems) {
						selectedTickUnifiedSymbolSet.add(tick.getUnifiedSymbol());
					}
					TickField clickedItem = row.getItem();

					ContractField contract = desktopTradeCachesService.queryContractByUnifiedSymbol(clickedItem.getUnifiedSymbol());

					guiMainService.updateSelectedContarct(contract);
				}
			});
			return row;
		});

		tickTableView.setFocusTraversable(false);

		vBox.getChildren().add(tickTableView);
		VBox.setVgrow(tickTableView, Priority.ALWAYS);
		vBox.setMinWidth(1);

//        HBox hBox = new HBox();
//        vBox.getChildren().add(hBox);

	}

}
