package xyz.redtorch.desktop.layout.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TradeField;

@Component
public class TradeLayout {

	private static final Logger logger = LoggerFactory.getLogger(TradeLayout.class);

	public static final int SHOW_ALL = 0;
	public static final int SHOW_LONG = 1;
	public static final int SHOW_SHORT = 2;

	private VBox vBox = new VBox();

	private boolean layoutCreated = false;

	private ObservableList<TradeField> tradeObservableList = FXCollections.observableArrayList();

	private List<TradeField> tradeList = new ArrayList<>();

	private TableView<TradeField> tradeTableView = new TableView<>();

	private int showRadioValue = 0;

	private boolean showMergedChecked = false;

	private Set<String> selectedTradeIdSet = new HashSet<>();

	@Autowired
	private GuiMainService guiMainService;

	public Node getNode() {
		if (!layoutCreated) {
			createLayout();
			layoutCreated = true;
		}
		return this.vBox;
	}

	public void updateData(List<TradeField> tradeList) {
		if (!new HashSet<>(this.tradeList).equals(new HashSet<>(tradeList))) {
			this.tradeList = tradeList;
			fillingData();
		}
	}

	public void fillingData() {
		List<TradeField> newTradeList = new ArrayList<>();
		for (TradeField trade : this.tradeList) {
			if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.getSelectedAccountIdSet().contains(trade.getAccountId())) {
				if (showRadioValue == SHOW_ALL) {
					newTradeList.add(trade);
				} else if (showRadioValue == SHOW_LONG) {
					if (DirectionEnum.D_Buy_VALUE == trade.getDirectionValue()) {
						newTradeList.add(trade);
					}
				} else if (showRadioValue == SHOW_SHORT) {
					if (DirectionEnum.D_Sell_VALUE == trade.getDirectionValue()) {
						newTradeList.add(trade);
					}
				}
			}
		}

		if (!showMergedChecked) {
			tradeObservableList.clear();
			tradeObservableList.addAll(newTradeList);
		} else {
			Map<String, TradeField.Builder> mergedTradeFieldBuilderMap = new HashMap<>();

			for (TradeField trade : newTradeList) {
				String key = trade.getContract().getUnifiedSymbol() + "#" + trade.getDirectionValue() + "#" + trade.getOffsetFlagValue();

				TradeField.Builder tradeFieldBuilder;
				if (mergedTradeFieldBuilderMap.containsKey(key)) {
					tradeFieldBuilder = mergedTradeFieldBuilderMap.get(key);
					tradeFieldBuilder.setVolume(tradeFieldBuilder.getVolume() + trade.getVolume());
				} else {
					tradeFieldBuilder = TradeField.newBuilder();
					tradeFieldBuilder.setContract(trade.getContract());
					tradeFieldBuilder.setDirection(trade.getDirection());
					tradeFieldBuilder.setOffsetFlag(trade.getOffsetFlag());
					tradeFieldBuilder.setVolume(trade.getVolume());
					mergedTradeFieldBuilderMap.put(key, tradeFieldBuilder);
				}

			}

			List<TradeField> mergedTradeFieldList = new ArrayList<>();

			for (TradeField.Builder tradeFieldBuilder : mergedTradeFieldBuilderMap.values()) {
				mergedTradeFieldList.add(tradeFieldBuilder.build());
			}
			tradeObservableList.clear();
			tradeObservableList.addAll(mergedTradeFieldList);

		}

		Set<String> newSelectedTradeIdSet = new HashSet<>();
		for (TradeField trade : tradeObservableList) {
			if (selectedTradeIdSet.contains(trade.getTradeId())) {
				tradeTableView.getSelectionModel().select(trade);
				newSelectedTradeIdSet.add(trade.getTradeId());
			}
		}
		selectedTradeIdSet = newSelectedTradeIdSet;
	}

	private void createLayout() {

		tradeTableView.setTableMenuButtonVisible(true);

		TableColumn<TradeField, Pane> unifiedSymbolCol = new TableColumn<>("合约");
		unifiedSymbolCol.setPrefWidth(160);
		unifiedSymbolCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {
				TradeField trade = feature.getValue();
				Text unifiedSymbolText = new Text(trade.getContract().getUnifiedSymbol());
				Text shortNameText = new Text(trade.getContract().getName());
				vBox.getChildren().add(unifiedSymbolText);
				vBox.getChildren().add(shortNameText);

				if (guiMainService.getSelectedContract() != null && guiMainService.getSelectedContract().getUnifiedSymbol().equals(trade.getContract().getUnifiedSymbol())) {
					unifiedSymbolText.getStyleClass().add("trade-remind-color");
				}

				vBox.setUserData(trade);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleObjectProperty<>(vBox);
		});

		unifiedSymbolCol.setComparator((Pane p1, Pane p2) -> {
			try {
				TradeField trade1 = (TradeField) p1.getUserData();
				TradeField trade2 = (TradeField) p2.getUserData();
				return StringUtils.compare(trade1.getContract().getUnifiedSymbol(), trade2.getContract().getUnifiedSymbol());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		tradeTableView.getColumns().add(unifiedSymbolCol);

		TableColumn<TradeField, Text> directionCol = new TableColumn<>("方向");
		directionCol.setPrefWidth(40);
		directionCol.setCellValueFactory(feature -> {
			Text directionText = new Text("未知");

			try {
				TradeField trade = feature.getValue();

				if (trade.getDirection() == DirectionEnum.D_Buy) {
					directionText.setText("多");
					directionText.getStyleClass().add("trade-long-color");
				} else if (trade.getDirection() == DirectionEnum.D_Sell) {
					directionText.setText("空");
					directionText.getStyleClass().add("trade-short-color");
				}

				directionText.setUserData(trade);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(directionText);
		});

		directionCol.setComparator((Text t1, Text t2) -> {
			try {
				TradeField trade1 = (TradeField) t1.getUserData();
				TradeField trade2 = (TradeField) t2.getUserData();
				return Integer.compare(trade1.getDirection().getNumber(), trade2.getDirection().getNumber());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		tradeTableView.getColumns().add(directionCol);

		TableColumn<TradeField, String> offsetCol = new TableColumn<>("开平");
		offsetCol.setPrefWidth(40);
		offsetCol.setCellValueFactory(feature -> {
			String offset = "未知";

			try {
				TradeField trade = feature.getValue();

				if (trade.getOffsetFlag() == OffsetFlagEnum.OF_Close) {
					offset = "平";
				} else if (trade.getOffsetFlag() == OffsetFlagEnum.OF_CloseToday) {
					offset = "平今";
				} else if (trade.getOffsetFlag() == OffsetFlagEnum.OF_CloseYesterday) {
					offset = "平昨";
				} else if (trade.getOffsetFlag() == OffsetFlagEnum.OF_Open) {
					offset = "开";
				} else if (trade.getOffsetFlag() == OffsetFlagEnum.OF_Unkonwn) {
					offset = "未知";
				} else {
					offset = trade.getOffsetFlag().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(offset);
		});
		offsetCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		tradeTableView.getColumns().add(offsetCol);

		TableColumn<TradeField, String> hedgeFlagCol = new TableColumn<>("投机套保");
		hedgeFlagCol.setPrefWidth(60);
		hedgeFlagCol.setCellValueFactory(feature -> {
			String hedgeFlag = "未知";

			try {
				TradeField trade = feature.getValue();

				if (trade.getHedgeFlag() == HedgeFlagEnum.HF_Speculation) {
					hedgeFlag = "投机";
				} else if (trade.getHedgeFlag() == HedgeFlagEnum.HF_Hedge) {
					hedgeFlag = "套保";
				} else if (trade.getHedgeFlag() == HedgeFlagEnum.HF_Arbitrage) {
					hedgeFlag = "套利";
				} else if (trade.getHedgeFlag() == HedgeFlagEnum.HF_MarketMaker) {
					hedgeFlag = "做市商";
				} else if (trade.getHedgeFlag() == HedgeFlagEnum.HF_SpecHedge) {
					hedgeFlag = "第一条腿投机第二条腿套保 大商所专用";
				} else if (trade.getHedgeFlag() == HedgeFlagEnum.HF_HedgeSpec) {
					hedgeFlag = "第一条腿套保第二条腿投机 大商所专用";
				} else if (trade.getHedgeFlag() == HedgeFlagEnum.HF_Unknown) {
					hedgeFlag = "未知";
				} else {
					hedgeFlag = trade.getHedgeFlag().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(hedgeFlag);
		});
		hedgeFlagCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		tradeTableView.getColumns().add(hedgeFlagCol);

		TableColumn<TradeField, String> priceCol = new TableColumn<>("价格");
		priceCol.setPrefWidth(80);
		priceCol.setCellValueFactory(feature -> {
			String priceString = "";
			try {

				TradeField trade = feature.getValue();
				ContractField contract = trade.getContract();
				int dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
				if (dcimalDigits < 0) {
					dcimalDigits = 0;
				}
				String priceStringFormat = "%,." + dcimalDigits + "f";
				priceString = String.format(priceStringFormat, trade.getPrice());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(priceString);

		});
		priceCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		tradeTableView.getColumns().add(priceCol);

		TableColumn<TradeField, Integer> volumeCol = new TableColumn<>("成交量");
		volumeCol.setPrefWidth(50);
		volumeCol.setCellValueFactory(feature -> {
			Integer volume = Integer.MAX_VALUE;
			try {
				volume = feature.getValue().getVolume();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleIntegerProperty(volume).asObject();
		});
		volumeCol.setComparator((Integer i1, Integer i2) -> Integer.compare(i1, i2));
		tradeTableView.getColumns().add(volumeCol);

		TableColumn<TradeField, String> tradeTimeCol = new TableColumn<>("时间");
		tradeTimeCol.setPrefWidth(120);
		tradeTimeCol.setCellValueFactory(feature -> {
			String tradeTime = "";
			try {
				tradeTime = feature.getValue().getTradeDate() + " " + feature.getValue().getTradeTime();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(tradeTime);

		});
		tradeTimeCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		tradeTimeCol.setSortType(SortType.DESCENDING);
		tradeTableView.getColumns().add(tradeTimeCol);

		TableColumn<TradeField, String> adapterOrderIdCol = new TableColumn<>("适配器定单编号");
		adapterOrderIdCol.setPrefWidth(150);
		adapterOrderIdCol.setCellValueFactory(feature -> {
			String adapterOrderId = "";
			try {
				adapterOrderId = feature.getValue().getAdapterOrderId();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(adapterOrderId);
		});
		adapterOrderIdCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		tradeTableView.getColumns().add(adapterOrderIdCol);

		TableColumn<TradeField, String> adapterTradeIdCol = new TableColumn<>("适配器成交编号");
		adapterTradeIdCol.setPrefWidth(250);
		adapterTradeIdCol.setCellValueFactory(feature -> {
			String adapterTradeId = "";
			try {
				adapterTradeId = feature.getValue().getAdapterTradeId();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(adapterTradeId);
		});
		adapterTradeIdCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		tradeTableView.getColumns().add(adapterTradeIdCol);

		TableColumn<TradeField, String> accountIdCol = new TableColumn<>("账户ID");
		accountIdCol.setPrefWidth(350);
		accountIdCol.setCellValueFactory(feature -> {
			String accountId = "";
			try {
				accountId = feature.getValue().getAccountId();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(accountId);
		});
		accountIdCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		tradeTableView.getColumns().add(accountIdCol);

		SortedList<TradeField> sortedItems = new SortedList<>(tradeObservableList);
		tradeTableView.setItems(sortedItems);
		sortedItems.comparatorProperty().bind(tradeTableView.comparatorProperty());

		tradeTableView.getSortOrder().add(tradeTimeCol);
		tradeTableView.setFocusTraversable(false);

		tradeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		tradeTableView.setRowFactory(tv -> {
			TableRow<TradeField> row = new TableRow<>();
			row.setOnMousePressed(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
					ObservableList<TradeField> selectedItems = tradeTableView.getSelectionModel().getSelectedItems();
					selectedTradeIdSet.clear();
					for (TradeField trade : selectedItems) {
						selectedTradeIdSet.add(trade.getTradeId());
					}
					TradeField clickedItem = row.getItem();
					guiMainService.updateSelectedContarct(clickedItem.getContract());
				}
			});
			return row;
		});

		tradeTableView.setFocusTraversable(false);

		vBox.getChildren().add(tradeTableView);
		VBox.setVgrow(tradeTableView, Priority.ALWAYS);

		HBox hBox = new HBox();
		RadioButton allRadioButton = new RadioButton("全部");
		RadioButton longRadioButton = new RadioButton("做多记录");
		RadioButton shortRadioButton = new RadioButton("做空记录");
		ToggleGroup toggleGroup = new ToggleGroup();
		allRadioButton.setToggleGroup(toggleGroup);
		allRadioButton.setUserData(SHOW_ALL);
		longRadioButton.setToggleGroup(toggleGroup);
		longRadioButton.setUserData(SHOW_LONG);
		shortRadioButton.setToggleGroup(toggleGroup);
		shortRadioButton.setUserData(SHOW_SHORT);
		allRadioButton.setSelected(true);
		toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {

				showRadioValue = (int) newValue.getUserData();
				fillingData();

			};
		});

		hBox.getChildren().add(allRadioButton);
		hBox.getChildren().add(longRadioButton);
		hBox.getChildren().add(shortRadioButton);

		CheckBox showMergedCheckBox = new CheckBox("合并显示");
		showMergedCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				showMergedChecked = newValue;
				fillingData();
			}
		});

		hBox.getChildren().add(showMergedCheckBox);

		vBox.getChildren().add(hBox);

	}

}
