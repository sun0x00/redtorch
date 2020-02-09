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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.PositionDirectionEnum;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.PositionField;

@Component
public class PositionLayout {

	private static final Logger logger = LoggerFactory.getLogger(PositionLayout.class);

	private VBox vBox = new VBox();

	private boolean layoutCreated = false;

	private ObservableList<PositionField> positionObservableList = FXCollections.observableArrayList();

	private List<PositionField> positionList = new ArrayList<>();

	private TableView<PositionField> positionTableView = new TableView<>();

	private boolean showMergedPosition = false;
	private boolean showEmptyPosition = false;

	private Set<String> selectedPositionIdSet = new HashSet<>();

	@Autowired
	private DesktopTradeCachesService desktopTradeCachesService;
	@Autowired
	private GuiMainService guiMainService;

	public Node getNode() {
		if (!layoutCreated) {
			createLayout();
			layoutCreated = true;
		}
		return this.vBox;
	}

	public void updateData(List<PositionField> positionList) {
		if (!new HashSet<>(this.positionList).equals(new HashSet<>(positionList))) {
			this.positionList = positionList;
			fillingData();
		}
	}

	public void fillingData() {
		List<PositionField> newPositionList = new ArrayList<>();
		for (PositionField position : positionList) {
			if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.getSelectedAccountIdSet().contains(position.getAccountId())) {
				if (!showEmptyPosition) {
					if (position.getPosition() != 0) {
						newPositionList.add(position);
					}
				} else {
					newPositionList.add(position);
				}
			}
		}

		if (!showMergedPosition) {
			positionObservableList.clear();
			positionObservableList.addAll(newPositionList);
		} else {

			Map<String, PositionField.Builder> mergedPositionFieldBuilderMap = new HashMap<>();

			for (PositionField position : newPositionList) {

				if (!(guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.getSelectedAccountIdSet().contains(position.getAccountId()))) {
					continue;
				}

				String key = position.getContract().getUnifiedSymbol() + "#" + position.getPositionDirectionValue();

				PositionField.Builder positionFieldBuilder;
				if (mergedPositionFieldBuilderMap.containsKey(key)) {
					positionFieldBuilder = mergedPositionFieldBuilderMap.get(key);

					int positionInt = positionFieldBuilder.getPosition() + position.getPosition();
					if (positionInt != 0) {
						double openPrice = (positionFieldBuilder.getOpenPrice() * positionFieldBuilder.getPosition() + position.getOpenPrice() * position.getPosition()) / positionInt;
						positionFieldBuilder.setOpenPrice(openPrice);

						double price = (positionFieldBuilder.getPrice() * positionFieldBuilder.getPosition() + position.getPrice() * position.getPosition()) / positionInt;
						positionFieldBuilder.setPrice(price);
					}

					positionFieldBuilder.setPrice(position.getPrice());
					positionFieldBuilder.setPosition(positionInt);
					positionFieldBuilder.setFrozen(positionFieldBuilder.getFrozen() + position.getFrozen());
					positionFieldBuilder.setTdPosition(positionFieldBuilder.getTdPosition() + position.getTdPosition());
					positionFieldBuilder.setTdFrozen(positionFieldBuilder.getTdFrozen() + position.getTdFrozen());
					positionFieldBuilder.setYdPosition(positionFieldBuilder.getYdPosition() + position.getYdPosition());
					positionFieldBuilder.setYdFrozen(positionFieldBuilder.getYdFrozen() + position.getYdFrozen());

					positionFieldBuilder.setContractValue(positionFieldBuilder.getContractValue() + position.getContractValue());
					positionFieldBuilder.setExchangeMargin(positionFieldBuilder.getExchangeMargin() + position.getExchangeMargin());
					positionFieldBuilder.setUseMargin(positionFieldBuilder.getUseMargin() + position.getUseMargin());
					positionFieldBuilder.setOpenPositionProfit(positionFieldBuilder.getOpenPositionProfit() + position.getOpenPositionProfit());
					positionFieldBuilder.setPositionProfit(positionFieldBuilder.getPositionProfit() + position.getPositionProfit());

				} else {
					positionFieldBuilder = PositionField.newBuilder();
					positionFieldBuilder.setContract(position.getContract());
					positionFieldBuilder.setPositionDirection(position.getPositionDirection());
					positionFieldBuilder.setPosition(position.getPosition());
					positionFieldBuilder.setFrozen(position.getFrozen());
					positionFieldBuilder.setTdPosition(position.getTdPosition());
					positionFieldBuilder.setTdFrozen(position.getTdFrozen());
					positionFieldBuilder.setYdPosition(position.getYdPosition());
					positionFieldBuilder.setYdFrozen(position.getYdFrozen());

					positionFieldBuilder.setContractValue(position.getContractValue());
					positionFieldBuilder.setExchangeMargin(position.getExchangeMargin());
					positionFieldBuilder.setUseMargin(position.getUseMargin());
					positionFieldBuilder.setOpenPositionProfit(position.getOpenPositionProfit());
					positionFieldBuilder.setPositionProfit(position.getPositionProfit());
					positionFieldBuilder.setOpenPrice(position.getOpenPrice());
					positionFieldBuilder.setPrice(position.getPrice());
					positionFieldBuilder.setPositionId(key);
					mergedPositionFieldBuilderMap.put(key, positionFieldBuilder);
				}

			}

			List<PositionField> mergedPositionFieldList = new ArrayList<>();

			for (PositionField.Builder positionFieldBuilder : mergedPositionFieldBuilderMap.values()) {
				if (positionFieldBuilder.getUseMargin() != 0) {
					positionFieldBuilder.setOpenPositionProfitRatio(positionFieldBuilder.getOpenPositionProfit() / positionFieldBuilder.getUseMargin());
					positionFieldBuilder.setPositionProfitRatio(positionFieldBuilder.getPositionProfit() / positionFieldBuilder.getUseMargin());
				}
				mergedPositionFieldList.add(positionFieldBuilder.build());
			}
			positionObservableList.clear();
			positionObservableList.addAll(mergedPositionFieldList);

		}

		Set<String> newSelectedPositionIdSet = new HashSet<>();
		for (PositionField position : positionObservableList) {
			if (selectedPositionIdSet.contains(position.getPositionId())) {
				positionTableView.getSelectionModel().select(position);
				newSelectedPositionIdSet.add(position.getPositionId());
			}
		}
		selectedPositionIdSet = newSelectedPositionIdSet;
	}

	private void createLayout() {

		positionTableView.setTableMenuButtonVisible(true);

		TableColumn<PositionField, Pane> unifiedSymbolCol = new TableColumn<>("合约");
		unifiedSymbolCol.setPrefWidth(160);
		unifiedSymbolCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {
				PositionField position = feature.getValue();
				Text unifiedSymbolText = new Text(position.getContract().getUnifiedSymbol());
				Text shortNameText = new Text(position.getContract().getName());
				vBox.getChildren().add(unifiedSymbolText);
				vBox.getChildren().add(shortNameText);

				if (guiMainService.getSelectedContract() != null && guiMainService.getSelectedContract().getUnifiedSymbol().equals(position.getContract().getUnifiedSymbol())) {
					unifiedSymbolText.getStyleClass().add("trade-remind-color");
				}

				vBox.setUserData(position);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleObjectProperty<>(vBox);
		});

		unifiedSymbolCol.setComparator((Pane p1, Pane p2) -> {
			try {
				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return StringUtils.compare(position1.getContract().getUnifiedSymbol(), position2.getContract().getUnifiedSymbol());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(unifiedSymbolCol);

		TableColumn<PositionField, Text> directionCol = new TableColumn<>("方向");
		directionCol.setPrefWidth(40);
		directionCol.setCellValueFactory(feature -> {
			Text directionText = new Text("未知");

			try {
				PositionField position = feature.getValue();

				if (position.getPositionDirection() == PositionDirectionEnum.PD_Long) {
					directionText.setText("多");
					directionText.getStyleClass().add("trade-long-color");
				} else if (position.getPositionDirection() == PositionDirectionEnum.PD_Short) {
					directionText.setText("空");
					directionText.getStyleClass().add("trade-short-color");
				} else if (position.getPositionDirection() == PositionDirectionEnum.PD_Net) {
					directionText.setText("净");
				} else if (position.getPositionDirection() == PositionDirectionEnum.PD_Unknown) {
					directionText.setText("未知");
				} else {
					directionText.setText(position.getPositionDirection().getValueDescriptor().getName());
				}

				directionText.setUserData(position);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(directionText);
		});

		directionCol.setComparator((Text t1, Text t2) -> {
			try {
				PositionField position1 = (PositionField) t1.getUserData();
				PositionField position2 = (PositionField) t2.getUserData();
				return Integer.compare(position1.getPositionDirection().getNumber(), position2.getPositionDirection().getNumber());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(directionCol);

		TableColumn<PositionField, String> hedgeFlagCol = new TableColumn<>("投机套保");
		hedgeFlagCol.setPrefWidth(60);
		hedgeFlagCol.setCellValueFactory(feature -> {
			String hedgeFlag = "未知";

			try {
				PositionField position = feature.getValue();

				if (position.getHedgeFlag() == HedgeFlagEnum.HF_Speculation) {
					hedgeFlag = "投机";
				} else if (position.getHedgeFlag() == HedgeFlagEnum.HF_Hedge) {
					hedgeFlag = "套保";
				} else if (position.getHedgeFlag() == HedgeFlagEnum.HF_Arbitrage) {
					hedgeFlag = "套利";
				} else if (position.getHedgeFlag() == HedgeFlagEnum.HF_MarketMaker) {
					hedgeFlag = "做市商";
				} else if (position.getHedgeFlag() == HedgeFlagEnum.HF_SpecHedge) {
					hedgeFlag = "第一条腿投机第二条腿套保 大商所专用";
				} else if (position.getHedgeFlag() == HedgeFlagEnum.HF_HedgeSpec) {
					hedgeFlag = "第一条腿套保第二条腿投机 大商所专用";
				} else if (position.getHedgeFlag() == HedgeFlagEnum.HF_Unknown) {
					hedgeFlag = "未知";
				} else {
					hedgeFlag = position.getHedgeFlag().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(hedgeFlag);
		});
		hedgeFlagCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		positionTableView.getColumns().add(hedgeFlagCol);

		TableColumn<PositionField, Pane> positionCol = new TableColumn<>("持仓");
		positionCol.setPrefWidth(90);
		positionCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();

			try {
				HBox positionHBox = new HBox();
				Text positionLabelText = new Text("持仓");
				positionLabelText.setWrappingWidth(35);
				positionHBox.getChildren().add(positionLabelText);

				PositionField position = feature.getValue();

				Text positionText = new Text("" + position.getPosition());
				if (position.getPositionDirection() == PositionDirectionEnum.PD_Long) {
					positionText.getStyleClass().add("trade-long-color");
				} else if (position.getPositionDirection() == PositionDirectionEnum.PD_Short) {
					positionText.getStyleClass().add("trade-short-color");
				}
				positionHBox.getChildren().add(positionText);
				vBox.getChildren().add(positionHBox);

				HBox frozenHBox = new HBox();
				Text frozenLabelText = new Text("冻结");
				frozenLabelText.setWrappingWidth(35);
				frozenHBox.getChildren().add(frozenLabelText);

				Text frozenText = new Text("" + position.getFrozen());
				if (position.getFrozen() != 0) {
					frozenText.getStyleClass().add("trade-info-color");
				}
				frozenHBox.getChildren().add(frozenText);
				vBox.getChildren().add(frozenHBox);

				vBox.setUserData(position);

			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		positionCol.setComparator((Pane p1, Pane p2) -> {
			try {
				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return Integer.compare(position1.getPosition(), position2.getPosition());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(positionCol);

		TableColumn<PositionField, Pane> tdPositionCol = new TableColumn<>("今仓");
		tdPositionCol.setPrefWidth(90);
		tdPositionCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();

			try {
				HBox tdPositionHBox = new HBox();
				Text tdPositionLabelText = new Text("持仓");
				tdPositionLabelText.setWrappingWidth(35);
				tdPositionHBox.getChildren().add(tdPositionLabelText);

				PositionField position = feature.getValue();

				Text tdPositionText = new Text("" + position.getTdPosition());
				if (position.getPositionDirection() == PositionDirectionEnum.PD_Long) {
					tdPositionText.getStyleClass().add("trade-long-color");
				} else if (position.getPositionDirection() == PositionDirectionEnum.PD_Short) {
					tdPositionText.getStyleClass().add("trade-short-color");
				}
				tdPositionHBox.getChildren().add(tdPositionText);
				vBox.getChildren().add(tdPositionHBox);

				HBox frozenHBox = new HBox();
				Text frozenLabelText = new Text("冻结");
				frozenLabelText.setWrappingWidth(35);
				frozenHBox.getChildren().add(frozenLabelText);

				Text frozenText = new Text("" + position.getTdFrozen());
				if (position.getTdFrozen() != 0) {
					frozenText.getStyleClass().add("trade-info-color");
				}
				frozenHBox.getChildren().add(frozenText);
				vBox.getChildren().add(frozenHBox);

				vBox.setUserData(position);

			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleObjectProperty<>(vBox);
		});

		tdPositionCol.setComparator((Pane p1, Pane p2) -> {
			try {

				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return Integer.compare(position1.getPosition(), position2.getPosition());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(tdPositionCol);

		TableColumn<PositionField, Pane> openPositionProfitCol = new TableColumn<>("逐笔浮盈");
		openPositionProfitCol.setPrefWidth(90);
		openPositionProfitCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {
				PositionField position = feature.getValue();
				Text openPositionProfitText = new Text(String.format("%,.2f", position.getOpenPositionProfit()));
				Text openPositionProfitRatioText = new Text(String.format("%.2f%%", position.getOpenPositionProfitRatio() * 100));
				if (position.getOpenPositionProfit() > 0) {
					openPositionProfitText.getStyleClass().add("trade-long-color");
					openPositionProfitRatioText.getStyleClass().add("trade-long-color");
				} else if (position.getOpenPositionProfit() < 0) {
					openPositionProfitText.getStyleClass().add("trade-short-color");
					openPositionProfitRatioText.getStyleClass().add("trade-short-color");
				}
				vBox.getChildren().addAll(openPositionProfitText, openPositionProfitRatioText);
				vBox.setUserData(position);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		openPositionProfitCol.setComparator((Pane p1, Pane p2) -> {
			try {
				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return Double.compare(position1.getOpenPositionProfit(), position2.getOpenPositionProfit());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;

		});
		positionTableView.getColumns().add(openPositionProfitCol);

		TableColumn<PositionField, Pane> positionProfitCol = new TableColumn<>("盯市浮盈");
		positionProfitCol.setPrefWidth(90);
		positionProfitCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {
				PositionField position = feature.getValue();
				Text positionProfitText = new Text(String.format("%,.2f", position.getPositionProfit()));
				Text positionProfitRatioText = new Text(String.format("%.2f%%", position.getPositionProfitRatio() * 100));
				if (position.getPositionProfit() > 0) {
					positionProfitText.getStyleClass().add("trade-long-color");
					positionProfitRatioText.getStyleClass().add("trade-long-color");
				} else if (position.getPositionProfit() < 0) {
					positionProfitText.getStyleClass().add("trade-short-color");
					positionProfitRatioText.getStyleClass().add("trade-short-color");
				}
				vBox.getChildren().addAll(positionProfitText, positionProfitRatioText);
				vBox.setUserData(position);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		positionProfitCol.setComparator((Pane p1, Pane p2) -> {
			try {
				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return Double.compare(position1.getPositionProfit(), position2.getPositionProfit());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;

		});
		positionTableView.getColumns().add(positionProfitCol);

		TableColumn<PositionField, Pane> openPriceCol = new TableColumn<>("开仓价格");
		openPriceCol.setPrefWidth(90);
		openPriceCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();

			try {
				PositionField position = feature.getValue();

				ContractField contract = position.getContract();

				int dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
				if (dcimalDigits < 0) {
					dcimalDigits = 0;
				}
				String priceStringFormat = "%,." + dcimalDigits + "f";

				Text openPriceText = new Text(String.format(priceStringFormat, position.getOpenPrice()));
				vBox.getChildren().add(openPriceText);

				Text openPriceDiffText = new Text(String.format(priceStringFormat, position.getOpenPriceDiff()));
				if (position.getOpenPriceDiff() > 0) {
					openPriceDiffText.getStyleClass().add("trade-long-color");
				} else if (position.getOpenPriceDiff() < 0) {
					openPriceDiffText.getStyleClass().add("trade-short-color");
				}
				vBox.getChildren().add(openPriceDiffText);

				vBox.setUserData(position);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		openPriceCol.setComparator((Pane p1, Pane p2) -> {
			try {
				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return Double.compare(position1.getOpenPrice(), position2.getOpenPrice());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;

		});

		positionTableView.getColumns().add(openPriceCol);

		TableColumn<PositionField, Pane> priceCol = new TableColumn<>("持仓价格");
		priceCol.setPrefWidth(90);
		priceCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();

			try {

				PositionField position = feature.getValue();

				ContractField contract = position.getContract();
				int dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
				if (dcimalDigits < 0) {
					dcimalDigits = 0;
				}
				String priceStringFormat = "%,." + dcimalDigits + "f";

				Text priceText = new Text(String.format(priceStringFormat, position.getPrice()));
				vBox.getChildren().add(priceText);

				Text priceDiffText = new Text(String.format(priceStringFormat, position.getPriceDiff()));
				if (position.getPriceDiff() > 0) {
					priceDiffText.getStyleClass().add("trade-long-color");
				} else if (position.getPriceDiff() < 0) {
					priceDiffText.getStyleClass().add("trade-short-color");
				}
				vBox.getChildren().add(priceDiffText);

				vBox.setUserData(position);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		priceCol.setComparator((Pane p1, Pane p2) -> {
			try {
				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return Double.compare(position1.getPrice(), position2.getPrice());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(priceCol);

		TableColumn<PositionField, Pane> marginCol = new TableColumn<>("保证金");
		marginCol.setPrefWidth(130);
		marginCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();

			try {
				HBox useMarginHBox = new HBox();
				Text useMarginLabelText = new Text("经纪商");
				useMarginLabelText.setWrappingWidth(45);
				useMarginHBox.getChildren().add(useMarginLabelText);

				PositionField position = feature.getValue();

				Text useMarginText = new Text(String.format("%,.2f", position.getUseMargin()));
				useMarginHBox.getChildren().add(useMarginText);
				vBox.getChildren().add(useMarginHBox);

				HBox exchangeMarginHBox = new HBox();
				Text exchangeMarginLabelText = new Text("交易所");
				exchangeMarginLabelText.setWrappingWidth(45);
				exchangeMarginHBox.getChildren().add(exchangeMarginLabelText);

				Text exchangeMarginText = new Text(String.format("%,.2f", position.getExchangeMargin()));
				exchangeMarginHBox.getChildren().add(exchangeMarginText);
				vBox.getChildren().add(exchangeMarginHBox);

				vBox.setUserData(position);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		marginCol.setComparator((Pane p1, Pane p2) -> {
			try {
				PositionField position1 = (PositionField) p1.getUserData();
				PositionField position2 = (PositionField) p2.getUserData();
				return Double.compare(position1.getUseMargin(), position2.getUseMargin());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(marginCol);

		TableColumn<PositionField, String> marginRatioCol = new TableColumn<>("保证金占比");
		marginRatioCol.setPrefWidth(70);
		marginRatioCol.setCellValueFactory(feature -> {

			String marginRatioStr = "";
			try {
				PositionField position = feature.getValue();

				if (showMergedPosition) {
					double allBalance = 0.;

					Set<String> selectedAccountIdSet = guiMainService.getSelectedAccountIdSet();

					List<AccountField> accountList = desktopTradeCachesService.getAccountList();
					if (accountList == null || accountList.isEmpty()) {
						marginRatioStr = "NA";
					} else {
						for (AccountField account : accountList) {
							if (selectedAccountIdSet.contains(account.getAccountId())) {
								allBalance += account.getBalance();
							}
						}

						if (allBalance == 0) {
							marginRatioStr = "NA";
						} else {
							double marginRatio = position.getUseMargin() / allBalance * 100;
							marginRatioStr = String.format("%,.2f", marginRatio) + "%";
						}
					}

				} else {
					AccountField account = desktopTradeCachesService.queryAccountByAccountId(position.getAccountId());

					if (account == null) {
						marginRatioStr = "NA";
					} else if (account.getBalance() == 0) {
						marginRatioStr = "NA";
					} else {
						double marginRatio = position.getUseMargin() / account.getBalance() * 100;
						marginRatioStr = String.format("%,.2f", marginRatio) + "%";
					}

				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(marginRatioStr);
		});

		marginRatioCol.setComparator((String s1, String s2) -> {
			try {
				return Double.valueOf(s1.replace(",", "")).compareTo(Double.valueOf(s2.replace(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(marginRatioCol);

		TableColumn<PositionField, String> contractValueCol = new TableColumn<>("合约价值");
		contractValueCol.setPrefWidth(90);
		contractValueCol.setCellValueFactory(feature -> {
			String contractValue = "";
			try {
				contractValue = String.format("%,.0f", feature.getValue().getContractValue());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(contractValue);
		});

		contractValueCol.setComparator((String s1, String s2) -> {
			try {
				return Double.valueOf(s1.replace(",", "")).compareTo(Double.valueOf(s2.replace(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		positionTableView.getColumns().add(contractValueCol);

		TableColumn<PositionField, String> accountIdCol = new TableColumn<>("账户ID");
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
		positionTableView.getColumns().add(accountIdCol);

		SortedList<PositionField> sortedItems = new SortedList<>(positionObservableList);
		positionTableView.setItems(sortedItems);
		sortedItems.comparatorProperty().bind(positionTableView.comparatorProperty());

		positionTableView.setFocusTraversable(false);

		positionTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		positionTableView.setRowFactory(tv -> {
			TableRow<PositionField> row = new TableRow<>();
			row.setOnMousePressed(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
					ObservableList<PositionField> selectedItems = positionTableView.getSelectionModel().getSelectedItems();
					selectedPositionIdSet.clear();
					for (PositionField position : selectedItems) {
						selectedPositionIdSet.add(position.getPositionId());
					}
					PositionField clickedItem = row.getItem();
					guiMainService.updateSelectedContarct(clickedItem.getContract());
				}
			});
			return row;
		});

		vBox.getChildren().add(positionTableView);
		VBox.setVgrow(positionTableView, Priority.ALWAYS);

		HBox hBox = new HBox();

		CheckBox showMergedCheckBox = new CheckBox("合并持仓");
		showMergedCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				showMergedPosition = newValue;
				fillingData();
			}
		});

		hBox.getChildren().add(showMergedCheckBox);

		CheckBox showEmptyCheckBox = new CheckBox("显示空仓");
		showEmptyCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				showEmptyPosition = newValue;
				fillingData();
			}
		});

		hBox.getChildren().add(showEmptyCheckBox);

		vBox.getChildren().add(hBox);
	}

}
