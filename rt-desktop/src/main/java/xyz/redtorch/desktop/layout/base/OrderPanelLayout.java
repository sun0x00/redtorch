package xyz.redtorch.desktop.layout.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreEnum.ContingentConditionEnum;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.ForceCloseReasonEnum;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreEnum.OrderPriceTypeEnum;
import xyz.redtorch.pb.CoreEnum.TimeConditionEnum;
import xyz.redtorch.pb.CoreEnum.VolumeConditionEnum;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreField.TickField;

@Component
public class OrderPanelLayout {

	private static final String PFM_Manual = "Manual"; // 手动
	private static final String PFM_Last = "Last"; // 最新
	private static final String PFM_Best = "Best"; // 本方最优
	private static final String PFM_CounterpartyBest = "CounterpartyBest"; // 对手方最优
	private static final String PFM_UpperOrLowerLimit = "UpperOrLowerLimit";

	private boolean layoutCreated = false;
	private TabPane tabPane = new TabPane();

	private ToggleGroup priceFillMethodToggleGroup = new ToggleGroup();
	private ToggleGroup orderPriceTypeToggleGroup = new ToggleGroup();
	private TextField priceTextField = new TextField();
	private TextField volumeTextField = new TextField("0");
	private ComboBox<HedgeFlagEnum> hedgeFlagComboBox = new ComboBox<>();
	private ComboBox<TimeConditionEnum> timeConditionComboBox = new ComboBox<>();
	private ComboBox<VolumeConditionEnum> volumeConditionComboBox = new ComboBox<>();
	private TextField minVolumeTextField = new TextField("1");
	private ComboBox<ContingentConditionEnum> contingentConditionComboBox = new ComboBox<>();
	private TextField stopPriceTextField = new TextField();

	private VBox accountVolumeVBox = new VBox();

	private Insets commonInsets = new Insets(2, 0, 0, 0);

	private OrderPriceTypeEnum orderPriceType = OrderPriceTypeEnum.OPT_LimitPrice;
	private String priceFillMethod = PFM_Last;
	private Double price = null;
	private Double stopPrice = null;
	private Integer volume = 0;
	private Integer minVolume = 1;

	private Map<String, Integer> accountVolumeMap = new HashMap<>();

	private TickField tick = null;
	private int dcimalDigits = 4;

	@Autowired
	private GuiMainService guiMainService;
	@Autowired
	private RpcClientApiService rpcClientApiService;
	@Autowired
	private DesktopTradeCachesService desktopTradeCachesService;

	private boolean submitPending = false;

	public void updateAccountVolumeMap(Map<String, Integer> accountVolumeMap) {
		this.accountVolumeMap.putAll(accountVolumeMap);
		this.fillingData();
	}

	public void fillingData() {

		Set<String> selectedAccountIdSet = guiMainService.getSelectedAccountIdSet();

		accountVolumeMap = accountVolumeMap.entrySet().stream().filter(map -> selectedAccountIdSet.contains(map.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		accountVolumeVBox.getChildren().clear();
		for (String selectedAccountId : selectedAccountIdSet) {

			AccountField account = desktopTradeCachesService.queryAccountByAccountId(selectedAccountId);
			TextField holderTextField = new TextField("");
			if (account != null) {
				holderTextField.setText(account.getHolder());
			}
			holderTextField.setDisable(true);
			TextField accountIdTextField = new TextField(selectedAccountId);
			accountIdTextField.setEditable(false);
			TextField accountVolumeTextField = new TextField();

			if (accountVolumeMap.containsKey(selectedAccountId)) {
				accountVolumeTextField.setText(accountVolumeMap.get(selectedAccountId) + "");
			} else {
				accountVolumeTextField.setText(volume + "");
			}

			accountVolumeTextField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					String volumeString = newValue;
					if (!newValue.matches("\\d*")) {
						volumeString = newValue.replaceAll("[^\\d]", "");
						accountVolumeTextField.setText(volumeString);
					}

					if (!volumeString.isBlank()) {
						Integer accountVolume = Integer.valueOf(volumeString);
						accountVolumeMap.put(selectedAccountId, accountVolume);
					}

				}
			});

			Button accountVolumeIncreaseButton = new Button("+");
			accountVolumeIncreaseButton.setPrefWidth(80);
			Button accountVolumeDecreaseButton = new Button("-");
			accountVolumeDecreaseButton.setPrefWidth(80);
			HBox accountVolumeButtonHBox = new HBox();
			accountVolumeButtonHBox.getChildren().addAll(accountVolumeDecreaseButton, accountVolumeIncreaseButton);

			accountVolumeIncreaseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						Integer accountVolume = volume;
						if (accountVolumeMap.containsKey(selectedAccountId)) {
							accountVolume = accountVolumeMap.get(selectedAccountId);
						}
						if (accountVolume < 1000000) {
							accountVolume += 1;
						}
						accountVolumeMap.put(selectedAccountId, accountVolume);
						accountVolumeTextField.setText("" + accountVolume);
					}
				}
			});

			accountVolumeDecreaseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						Integer accountVolume = volume;
						if (accountVolumeMap.containsKey(selectedAccountId)) {
							accountVolume = accountVolumeMap.get(selectedAccountId);
						}
						accountVolume -= 1;
						if (accountVolume < 0) {
							accountVolume = 0;
						}
						accountVolumeMap.put(selectedAccountId, accountVolume);
						accountVolumeTextField.setText("" + accountVolume);
					}
				}
			});

			VBox lineVbox = new VBox();
			lineVbox.setPadding(commonInsets);
			lineVbox.getChildren().addAll(holderTextField, accountIdTextField, accountVolumeTextField, accountVolumeButtonHBox);

			accountVolumeVBox.getChildren().add(lineVbox);
		}

	}

	public Node getNode() {
		if (!layoutCreated) {
			createLayout();
			fillingData();
			layoutCreated = true;
		}
		return this.tabPane;
	}

	public void updateData(TickField tick) {
		if (tick == null || this.tick == null || !tick.equals(this.tick)) {
			if (this.tick != null && tick != null && !this.tick.getUnifiedSymbol().equals(tick.getUnifiedSymbol())) {
				this.price = null;
				this.priceTextField.setText("");

				this.volume = 0;
				this.volumeTextField.setText("0");

				accountVolumeMap.clear();

				fillingData();
			}

			this.tick = tick;

			if (tick != null) {
				ContractField contract = desktopTradeCachesService.queryContractByUnifiedSymbol(tick.getUnifiedSymbol());
				if (contract != null) {
					dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
					if (dcimalDigits < 0) {
						dcimalDigits = 4;
					}
				}

			} else {
				dcimalDigits = 4;
			}

			if (PFM_Last.equals(priceFillMethod)) {
				if (tick != null && tick.getLastPrice() != Double.MAX_VALUE) {
					price = tick.getLastPrice();
					priceTextField.setText(String.format("%." + dcimalDigits + "f", price));
				} else {
					price = null;
					priceTextField.setText("");
				}
			} else if (PFM_CounterpartyBest.equals(priceFillMethod)) {
				price = null;
				priceTextField.setText("对手价");
			} else if (PFM_Best.equals(priceFillMethod)) {
				price = null;
				priceTextField.setText("排队价");
			} else if (PFM_UpperOrLowerLimit.equals(priceFillMethod)) {
				price = null;
				priceTextField.setText("涨跌停");
			}
		}
	}

	private void createLayout() {

		Tab routineTab = new Tab("常规");
		routineTab.setClosable(false);
		tabPane.getTabs().add(routineTab);
		tabPane.setPrefWidth(420);
		{
			HBox contentHBox = new HBox();
			routineTab.setContent(contentHBox);
			VBox leftVBox = new VBox();
			leftVBox.setPrefWidth(220);
			leftVBox.setMinWidth(220);
			leftVBox.setStyle("-fx-border-color: rgb(220, 220, 220);-fx-border-style: dashed;-fx-border-width: 0 1 0 0;");

			ScrollPane leftVBoxScrollPane = new ScrollPane();
			leftVBoxScrollPane.setPadding(new Insets(2, 0, 2, 2));
			leftVBoxScrollPane.setContent(leftVBox);
			leftVBoxScrollPane.setPrefWidth(235);
			leftVBoxScrollPane.setMinWidth(235);

			contentHBox.getChildren().add(leftVBoxScrollPane);

			RadioButton orderPriceTypeLimitRadioButton = new RadioButton("限价");
			orderPriceTypeLimitRadioButton.setUserData(OrderPriceTypeEnum.OPT_LimitPrice);
			orderPriceTypeLimitRadioButton.setPrefWidth(60);
			RadioButton orderPriceTypeAnyRadioButton = new RadioButton("市价");
			orderPriceTypeAnyRadioButton.setUserData(OrderPriceTypeEnum.OPT_AnyPrice);
			orderPriceTypeAnyRadioButton.setPrefWidth(60);
			RadioButton orderPriceTypeFiveLevelRadioButton = new RadioButton("五档");
			orderPriceTypeFiveLevelRadioButton.setUserData(OrderPriceTypeEnum.OPT_FiveLevelPrice);
			orderPriceTypeFiveLevelRadioButton.setPrefWidth(60);
			RadioButton orderPriceTypeBestRadioButton = new RadioButton("最优");
			orderPriceTypeBestRadioButton.setUserData(OrderPriceTypeEnum.OPT_BestPrice);
			orderPriceTypeBestRadioButton.setPrefWidth(60);
			RadioButton orderPriceTypeLastRadioButton = new RadioButton("最新");
			orderPriceTypeLastRadioButton.setUserData(OrderPriceTypeEnum.OPT_LastPrice);
			orderPriceTypeLastRadioButton.setPrefWidth(60);
			RadioButton orderPriceTypeLastPricePlusOneTicksRadioButton = new RadioButton("最新价浮动上浮1个ticks");
			orderPriceTypeLastPricePlusOneTicksRadioButton.setUserData(OrderPriceTypeEnum.OPT_LastPricePlusOneTicks);
			orderPriceTypeLastPricePlusOneTicksRadioButton.setPrefWidth(200);
			RadioButton orderPriceTypeLastPricePlusThreeTicksRadioButton = new RadioButton("最新价浮动上浮3个ticks");
			orderPriceTypeLastPricePlusThreeTicksRadioButton.setUserData(OrderPriceTypeEnum.OPT_LastPricePlusThreeTicks);
			orderPriceTypeLastPricePlusThreeTicksRadioButton.setPrefWidth(200);

			orderPriceTypeLimitRadioButton.setToggleGroup(orderPriceTypeToggleGroup);
			orderPriceTypeAnyRadioButton.setToggleGroup(orderPriceTypeToggleGroup);
			orderPriceTypeFiveLevelRadioButton.setToggleGroup(orderPriceTypeToggleGroup);
			orderPriceTypeBestRadioButton.setToggleGroup(orderPriceTypeToggleGroup);
			orderPriceTypeLastRadioButton.setToggleGroup(orderPriceTypeToggleGroup);
			orderPriceTypeLastPricePlusOneTicksRadioButton.setToggleGroup(orderPriceTypeToggleGroup);
			orderPriceTypeLastPricePlusThreeTicksRadioButton.setToggleGroup(orderPriceTypeToggleGroup);

			orderPriceTypeLimitRadioButton.setSelected(true);
			orderPriceTypeToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
					orderPriceType = (OrderPriceTypeEnum) newValue.getUserData();
				}
			});

			HBox orderPriceTypeLine1HBox = new HBox();
			orderPriceTypeLine1HBox.setPadding(commonInsets);
			orderPriceTypeLine1HBox.getChildren().addAll(orderPriceTypeLimitRadioButton, orderPriceTypeAnyRadioButton, orderPriceTypeFiveLevelRadioButton);

			HBox orderPriceTypeLine2HBox = new HBox();
			orderPriceTypeLine2HBox.setPadding(commonInsets);
			orderPriceTypeLine2HBox.getChildren().addAll(orderPriceTypeBestRadioButton, orderPriceTypeLastRadioButton);

			orderPriceTypeLastPricePlusOneTicksRadioButton.setPadding(commonInsets);
			orderPriceTypeLastPricePlusThreeTicksRadioButton.setPadding(commonInsets);

			Label orderPriceTypeLabel = new Label("价格类型");
			orderPriceTypeLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(orderPriceTypeLabel, orderPriceTypeLine1HBox, orderPriceTypeLine2HBox, orderPriceTypeLastPricePlusOneTicksRadioButton,
					orderPriceTypeLastPricePlusThreeTicksRadioButton);

			RadioButton priceFillMethodBestRadioButton = new RadioButton("排队价");
			priceFillMethodBestRadioButton.setUserData(PFM_Best);
			priceFillMethodBestRadioButton.setPrefWidth(60);
			RadioButton priceFillMethodLastRadioButton = new RadioButton("最新");
			priceFillMethodLastRadioButton.setUserData(PFM_Last);
			priceFillMethodLastRadioButton.setPrefWidth(60);
			RadioButton priceFillMethodCounterpartyBestRadioButton = new RadioButton("对手");
			priceFillMethodCounterpartyBestRadioButton.setUserData(PFM_CounterpartyBest);
			priceFillMethodCounterpartyBestRadioButton.setPrefWidth(60);
			RadioButton priceFillMethodUpperOrLowerLimitRadioButton = new RadioButton("涨跌停");
			priceFillMethodUpperOrLowerLimitRadioButton.setUserData(PFM_UpperOrLowerLimit);
			priceFillMethodUpperOrLowerLimitRadioButton.setPrefWidth(60);
			RadioButton priceFillMethodManualRadioButton = new RadioButton("手动");
			priceFillMethodManualRadioButton.setUserData(PFM_Manual);
			priceFillMethodManualRadioButton.setPrefWidth(60);

			priceFillMethodBestRadioButton.setToggleGroup(priceFillMethodToggleGroup);
			priceFillMethodLastRadioButton.setToggleGroup(priceFillMethodToggleGroup);
			priceFillMethodCounterpartyBestRadioButton.setToggleGroup(priceFillMethodToggleGroup);
			priceFillMethodUpperOrLowerLimitRadioButton.setToggleGroup(priceFillMethodToggleGroup);
			priceFillMethodManualRadioButton.setToggleGroup(priceFillMethodToggleGroup);

			priceFillMethodLastRadioButton.setSelected(true);
			priceTextField.setDisable(true);

			priceFillMethodToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
					priceFillMethod = (String) newValue.getUserData();
					if (PFM_Manual.equals(priceFillMethod)) {
						priceTextField.setDisable(false);
						if (price == null) {
							if (tick != null && tick.getLastPrice() != Double.MAX_VALUE) {
								price = tick.getLastPrice();
								priceTextField.setText(String.format("%." + dcimalDigits + "f", price));
							} else {
								priceTextField.setText("");
							}
						}

					} else {
						priceTextField.setDisable(true);

						if (PFM_CounterpartyBest.equals(priceFillMethod)) {
							price = null;
							priceTextField.setText("对手价");
						} else if (PFM_Best.equals(priceFillMethod)) {
							price = null;
							priceTextField.setText("排队价");
						} else if (PFM_UpperOrLowerLimit.equals(priceFillMethod)) {
							price = null;
							priceTextField.setText("涨跌停");
						} else if (PFM_Last.equals(priceFillMethod)) {
							if (tick != null && tick.getLastPrice() != Double.MAX_VALUE) {
								price = tick.getLastPrice();
								priceTextField.setText(String.format("%." + dcimalDigits + "f", price));
							} else {
								price = null;
								priceTextField.setText("");
							}
						}
					}
					fillingData();
				}
			});

			HBox priceFillMethodLine1HBox = new HBox();
			priceFillMethodLine1HBox.setPadding(commonInsets);
			priceFillMethodLine1HBox.getChildren().addAll(priceFillMethodBestRadioButton, priceFillMethodLastRadioButton, priceFillMethodCounterpartyBestRadioButton);
			HBox priceFillMethodLine2HBox = new HBox();
			priceFillMethodLine2HBox.setPadding(commonInsets);
			priceFillMethodLine2HBox.getChildren().addAll(priceFillMethodUpperOrLowerLimitRadioButton, priceFillMethodManualRadioButton);

			Label priceFillMethodLabel = new Label("填价方式");
			priceFillMethodLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(priceFillMethodLabel, priceFillMethodLine1HBox, priceFillMethodLine2HBox);

			priceTextField.setPrefWidth(120);
			priceTextField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					Pattern pattern = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

					if (!(newValue.isEmpty() || "-".equals(newValue) || ".".equals(newValue) || "-.".equals(newValue) || pattern.matcher(newValue).matches() || "涨跌停".equals(newValue)
							|| "对手价".equals(newValue) || "排队价".equals(newValue))) {
						priceTextField.setText(oldValue);
					} else {
						if (newValue.isEmpty() || "-".equals(newValue) || ".".equals(newValue) || "-.".equals(newValue) || "涨跌停".equals(newValue) || "对手价".equals(newValue) || "排队价".equals(newValue)) {
							price = null;
						} else if (pattern.matcher(newValue).matches()) {
							price = Double.valueOf(newValue);
						}
					}

				}

			});

			Button priceIncreaseButton = new Button("+");
			priceIncreaseButton.setPrefWidth(109);
			Button priceDecreaseButton = new Button("-");
			priceDecreaseButton.setPrefWidth(109);
			HBox priceButtonHBox = new HBox();
			priceButtonHBox.getChildren().addAll(priceDecreaseButton, priceIncreaseButton);
			Label priceLabel = new Label("价格");
			priceLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(priceLabel, priceTextField, priceButtonHBox);

			priceIncreaseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						if (tick != null && guiMainService.getSelectedContract() != null) {
							if (price == null) {
								if (tick.getLastPrice() != Double.MAX_VALUE) {
									price = tick.getLastPrice() + guiMainService.getSelectedContract().getPriceTick();
								}
							} else {
								price = price + guiMainService.getSelectedContract().getPriceTick();
							}
							priceFillMethod = PFM_Manual;
							priceFillMethodManualRadioButton.setSelected(true);
							priceTextField.setText(String.format("%." + dcimalDigits + "f", price));
						}
					}
				}
			});

			priceDecreaseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						if (tick != null && guiMainService.getSelectedContract() != null) {
							if (price == null) {
								if (tick.getLastPrice() != Double.MAX_VALUE) {
									price = tick.getLastPrice() - guiMainService.getSelectedContract().getPriceTick();
								}
							} else {
								price = price - guiMainService.getSelectedContract().getPriceTick();
							}
							priceFillMethod = PFM_Manual;
							priceFillMethodManualRadioButton.setSelected(true);
							priceTextField.setText(String.format("%." + dcimalDigits + "f", price));
						}
					}
				}
			});

			volumeTextField.setPrefWidth(120);
			volumeTextField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					String volumeString = newValue;
					if (!newValue.matches("\\d*")) {
						volumeString = newValue.replaceAll("[^\\d]", "");
						volumeTextField.setText(volumeString);
					}
					if (volumeString.isBlank()) {
						volume = 0;
					} else {
						volume = Integer.valueOf(volumeString);
					}
				}
			});

			Button volumeIncreaseButton = new Button("+");
			volumeIncreaseButton.setPrefWidth(109);
			Button volumeDecreaseButton = new Button("-");
			volumeDecreaseButton.setPrefWidth(109);
			HBox volumeButtonHBox = new HBox();
			volumeButtonHBox.getChildren().addAll(volumeDecreaseButton, volumeIncreaseButton);
			Label volumeLabel = new Label("全局数量");
			volumeLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(volumeLabel, volumeTextField, volumeButtonHBox);

			volumeIncreaseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						if (volume < 1000000) {
							volume += 1;
						}
						volumeTextField.setText("" + volume);
						fillingData();
					}
				}
			});

			volumeDecreaseButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						volume -= 1;
						if (volume < 0) {
							volume = 0;
						}
						volumeTextField.setText("" + volume);
						fillingData();
					}
				}
			});

			ObservableList<HedgeFlagEnum> hedgeFlagObservableList = FXCollections.observableArrayList();
			for (int i = 0; i < HedgeFlagEnum.values().length - 1; i++) {
				HedgeFlagEnum hedgeFlag = HedgeFlagEnum.values()[i];
				hedgeFlagObservableList.add(hedgeFlag);
			}
			hedgeFlagComboBox.setItems(hedgeFlagObservableList);
			hedgeFlagComboBox.getSelectionModel().select(HedgeFlagEnum.HF_Speculation);

			Label hedgeFlagLabel = new Label("投机套保");
			hedgeFlagLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(hedgeFlagLabel, hedgeFlagComboBox);

			ObservableList<TimeConditionEnum> timeConditionObservableList = FXCollections.observableArrayList();
			timeConditionObservableList.add(TimeConditionEnum.TC_GFD);
			timeConditionObservableList.add(TimeConditionEnum.TC_GTC);
			timeConditionObservableList.add(TimeConditionEnum.TC_IOC);
			timeConditionComboBox.setItems(timeConditionObservableList);
			timeConditionComboBox.getSelectionModel().select(TimeConditionEnum.TC_GFD);

			Label timeConditionLabel = new Label("时效类型");
			timeConditionLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(timeConditionLabel, timeConditionComboBox);

			ObservableList<VolumeConditionEnum> volumeConditionObservableList = FXCollections.observableArrayList();
			for (int i = 0; i < VolumeConditionEnum.values().length - 1; i++) {
				VolumeConditionEnum volumeCondition = VolumeConditionEnum.values()[i];
				volumeConditionObservableList.add(volumeCondition);
			}
			volumeConditionComboBox.setItems(volumeConditionObservableList);
			volumeConditionComboBox.getSelectionModel().select(VolumeConditionEnum.VC_AV);

			Label volumeConditionLabel = new Label("最小成交量");
			volumeConditionLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(volumeConditionLabel, volumeConditionComboBox);

			minVolumeTextField.setPrefWidth(120);
			minVolumeTextField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					String minVolumeString = newValue;
					if (!newValue.matches("\\d*")) {
						minVolumeString = newValue.replaceAll("[^\\d]", "");
						minVolumeTextField.setText(minVolumeString);
					}
					if (minVolumeString.isBlank()) {
						minVolume = 1;
					} else {
						minVolume = Integer.valueOf(minVolumeString);
					}
				}
			});

			Label minVolumeLabel = new Label("最小成交数量");
			minVolumeLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(minVolumeLabel, minVolumeTextField);

			ObservableList<ContingentConditionEnum> contingentConditionObservableList = FXCollections.observableArrayList();
			contingentConditionObservableList.add(ContingentConditionEnum.CC_Immediately);
			contingentConditionObservableList.add(ContingentConditionEnum.CC_LastPriceGreaterEqualStopPrice);
			contingentConditionObservableList.add(ContingentConditionEnum.CC_LastPriceLesserEqualStopPrice);
			contingentConditionObservableList.add(ContingentConditionEnum.CC_LocalLastPriceGreaterEqualStopPrice);
			contingentConditionObservableList.add(ContingentConditionEnum.CC_LocalLastPriceLesserEqualStopPrice);
			contingentConditionComboBox.setItems(contingentConditionObservableList);
			contingentConditionComboBox.getSelectionModel().select(ContingentConditionEnum.CC_Immediately);

			Label contingentConditionLabel = new Label("触发条件");
			contingentConditionLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(contingentConditionLabel, contingentConditionComboBox);

			stopPriceTextField.setPrefWidth(120);
			stopPriceTextField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					Pattern pattern = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

					if (!(newValue.isEmpty() || "-".equals(newValue) || ".".equals(newValue) || "-.".equals(newValue) || pattern.matcher(newValue).matches())) {
						stopPriceTextField.setText(oldValue);
					} else {
						if (newValue.isEmpty() || "-".equals(newValue) || ".".equals(newValue) || "-.".equals(newValue)) {
							stopPrice = null;
						} else if (pattern.matcher(newValue).matches()) {
							stopPrice = Double.valueOf(newValue);
						}
					}

				}

			});

			Label stopPriceLabel = new Label("条件价格");
			stopPriceLabel.setPadding(commonInsets);
			leftVBox.getChildren().addAll(stopPriceLabel, stopPriceTextField);

			VBox rightVBox = new VBox();
			contentHBox.getChildren().add(rightVBox);
			HBox.setHgrow(rightVBox, Priority.ALWAYS);
			rightVBox.setPadding(new Insets(5));

			VBox accountVolumeWrapVBox = new VBox();
			rightVBox.getChildren().add(accountVolumeWrapVBox);
			VBox.setVgrow(accountVolumeWrapVBox, Priority.ALWAYS);
			accountVolumeWrapVBox.setStyle("-fx-border-color: rgb(220, 220, 220);-fx-border-style: dashed;-fx-border-width: 0 1 0 0;");
			accountVolumeWrapVBox.setPadding(new Insets(5, 0, 0, 0));

			ScrollPane accountVolumeScrollPane = new ScrollPane();
			accountVolumeScrollPane.setMinWidth(180);
			accountVolumeScrollPane.setMaxWidth(180);
			accountVolumeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
			accountVolumeWrapVBox.getChildren().addAll(new Text("特定账户数量"), accountVolumeScrollPane);
			VBox.setVgrow(accountVolumeScrollPane, Priority.ALWAYS);

			accountVolumeVBox.setPadding(new Insets(0, 0, 0, 2));
			accountVolumeScrollPane.setContent(accountVolumeVBox);

			Insets buttonMarginInsets = new Insets(3, 3, 0, 0);

			String buttonStyle = "-fx-light-text-color: rgb(255, 255, 255); -fx-mid-text-color: rgb(255, 255, 255); -fx-dark-text-color: rgb(255, 255, 255);";

			VBox buttonVBox = new VBox();
			rightVBox.getChildren().add(buttonVBox);

			buttonVBox.setPrefHeight(115);
			buttonVBox.setMaxHeight(115);
			buttonVBox.setMinHeight(115);

			Button buyButton = new Button("多开");
			buyButton.getStyleClass().add("trade-long-color-background");
			buyButton.setStyle(buttonStyle);
			buyButton.setPrefWidth(60);
			buyButton.setPrefHeight(25);
			HBox.setMargin(buyButton, buttonMarginInsets);
			Button shortButton = new Button("空开");
			shortButton.getStyleClass().add("trade-short-color-background");
			shortButton.setStyle(buttonStyle);
			shortButton.setPrefWidth(60);
			shortButton.setPrefHeight(25);
			HBox.setMargin(shortButton, buttonMarginInsets);

			Button coverButton = new Button("平空");
			coverButton.getStyleClass().add("trade-long-color-background");
			coverButton.setStyle(buttonStyle);
			coverButton.setPrefWidth(60);
			coverButton.setPrefHeight(25);
			HBox.setMargin(coverButton, buttonMarginInsets);
			Button sellButton = new Button("平多");
			sellButton.getStyleClass().add("trade-short-color-background");
			sellButton.setStyle(buttonStyle);
			sellButton.setPrefWidth(60);
			sellButton.setPrefHeight(25);
			HBox.setMargin(sellButton, buttonMarginInsets);

			Button coverTDButton = new Button("平今空");
			coverTDButton.getStyleClass().add("trade-long-color-background");
			coverTDButton.setStyle(buttonStyle);
			coverTDButton.setPrefWidth(60);
			coverTDButton.setPrefHeight(25);
			HBox.setMargin(coverTDButton, buttonMarginInsets);
			Button sellTDButton = new Button("平今多");
			sellTDButton.getStyleClass().add("trade-short-color-background");
			sellTDButton.setStyle(buttonStyle);
			sellTDButton.setPrefWidth(60);
			sellTDButton.setPrefHeight(25);
			HBox.setMargin(sellTDButton, buttonMarginInsets);

			Button coverYDButton = new Button("平昨空");
			coverYDButton.getStyleClass().add("trade-long-color-background");
			coverYDButton.setStyle(buttonStyle);
			coverYDButton.setPrefWidth(60);
			coverYDButton.setPrefHeight(25);
			HBox.setMargin(coverYDButton, buttonMarginInsets);
			Button sellYDButton = new Button("平昨多");
			sellYDButton.getStyleClass().add("trade-short-color-background");
			sellYDButton.setStyle(buttonStyle);
			sellYDButton.setPrefWidth(60);
			sellYDButton.setPrefHeight(25);
			HBox.setMargin(sellYDButton, buttonMarginInsets);
			buyButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Buy, OffsetFlagEnum.OF_Open);
					}
				}
			});

			shortButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Sell, OffsetFlagEnum.OF_Open);
					}
				}
			});

			coverButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Buy, OffsetFlagEnum.OF_Close);
					}
				}
			});

			sellButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Sell, OffsetFlagEnum.OF_Close);
					}
				}
			});

			coverTDButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Buy, OffsetFlagEnum.OF_CloseToday);
					}
				}
			});

			sellTDButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Sell, OffsetFlagEnum.OF_CloseToday);
					}
				}
			});
			coverYDButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Buy, OffsetFlagEnum.OF_CloseYesterday);
					}
				}
			});

			sellYDButton.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if (e.getButton() == MouseButton.PRIMARY) {
						submitOrder(DirectionEnum.D_Sell, OffsetFlagEnum.OF_CloseYesterday);
					}
				}
			});

			HBox buttonLine1HBox = new HBox();
			buttonLine1HBox.getChildren().addAll(shortButton, buyButton);
			HBox buttonLine2HBox = new HBox();
			buttonLine2HBox.getChildren().addAll(coverButton, sellButton);
			HBox buttonLine3HBox = new HBox();
			buttonLine3HBox.getChildren().addAll(coverTDButton, sellTDButton);
			HBox buttonLine4HBox = new HBox();
			buttonLine4HBox.getChildren().addAll(coverYDButton, sellYDButton);

			buttonVBox.getChildren().addAll(buttonLine1HBox, buttonLine2HBox, buttonLine3HBox, buttonLine4HBox);

		}

	}

	private void submitOrder(DirectionEnum direction, OffsetFlagEnum offsetFlag) {
		if (submitPending) {
			return;
		}

		submitPending = true;

		Set<String> finalAccountIdSet = guiMainService.getSelectedAccountIdSet();

		if (finalAccountIdSet.size() == 0) {
			Alert selectAccountAlert = new Alert(AlertType.ERROR);
			selectAccountAlert.setTitle("错误");
			selectAccountAlert.setHeaderText("提交定单错误");
			selectAccountAlert.setContentText("请至少选择一个账户!");

			selectAccountAlert.showAndWait();
			submitPending = false;
			return;
		}

		ContractField finalContract = null;
		if (tick != null) {
			finalContract = desktopTradeCachesService.queryContractByUnifiedSymbol(tick.getUnifiedSymbol());
		}

		if (finalContract == null) {

			Alert selectContractAlert = new Alert(AlertType.ERROR);
			selectContractAlert.setTitle("错误");
			selectContractAlert.setHeaderText("提交定单错误");
			selectContractAlert.setContentText("请选择合约!");

			selectContractAlert.showAndWait();

			submitPending = false;
			return;
		}

		Double finalPrice = null;

		if (PFM_Last.equals(priceFillMethod)) {
			if (tick != null) {
				finalPrice = tick.getLastPrice();
			}
		} else if (PFM_CounterpartyBest.equals(priceFillMethod)) {
			if (DirectionEnum.D_Buy.equals(direction)) {
				if (tick != null && tick.getAskPriceList().size() > 0) {
					finalPrice = tick.getAskPriceList().get(0);
				}
			} else if (DirectionEnum.D_Sell.equals(direction)) {
				if (tick != null && tick.getBidPriceList().size() > 0) {
					finalPrice = tick.getBidPriceList().get(0);
				}
			}
		} else if (PFM_Best.equals(priceFillMethod)) {
			if (DirectionEnum.D_Buy.equals(direction)) {
				if (tick != null && tick.getBidPriceList().size() > 0) {
					finalPrice = tick.getBidPriceList().get(0);
				}
			} else if (DirectionEnum.D_Sell.equals(direction)) {
				if (tick != null && tick.getAskPriceList().size() > 0) {
					finalPrice = tick.getAskPriceList().get(0);
				}
			}
		} else if (PFM_UpperOrLowerLimit.equals(priceFillMethod)) {
			if (DirectionEnum.D_Buy.equals(direction)) {
				if (tick != null) {
					finalPrice = tick.getUpperLimit();
				}
			} else if (DirectionEnum.D_Sell.equals(direction)) {
				if (tick != null) {
					finalPrice = tick.getLowerLimit();
				}
			}
		} else if (PFM_Manual.equals(priceFillMethod)) {
			finalPrice = price;
		}

		if (orderPriceType == OrderPriceTypeEnum.OPT_LimitPrice) {
			if (finalPrice == null || finalPrice == Double.MAX_VALUE) {
				Alert priceAlert = new Alert(AlertType.ERROR);
				priceAlert.setTitle("错误");
				priceAlert.setHeaderText("定单错误");
				priceAlert.setContentText("无法获取到正确的价格!");

				priceAlert.showAndWait();

				submitPending = false;
				return;
			}
		} else {
			if (finalPrice == null) {
				finalPrice = 0.0;
			}
		}

		ContingentConditionEnum contingentCondition = contingentConditionComboBox.getSelectionModel().getSelectedItem();
		if (!contingentCondition.equals(ContingentConditionEnum.CC_Immediately)) {
			if (stopPrice == null || stopPrice == Double.MAX_VALUE) {
				Alert priceAlert = new Alert(AlertType.ERROR);
				priceAlert.setTitle("错误");
				priceAlert.setHeaderText("定单错误");
				priceAlert.setContentText("无法获取到正确的条件价格!");

				priceAlert.showAndWait();

				submitPending = false;
				return;
			}
		} else if (stopPrice == null) {
			stopPrice = 0.;
		}

		SubmitOrderReqField.Builder submitOrderReqFieldBuilder = SubmitOrderReqField.newBuilder();
		submitOrderReqFieldBuilder.setContract(finalContract);
		submitOrderReqFieldBuilder.setCurrency(finalContract.getCurrency());
		submitOrderReqFieldBuilder.setDirection(direction);
		submitOrderReqFieldBuilder.setOffsetFlag(offsetFlag);
		submitOrderReqFieldBuilder.setOrderPriceType(orderPriceType);
		submitOrderReqFieldBuilder.setTimeCondition(timeConditionComboBox.getSelectionModel().getSelectedItem());
		submitOrderReqFieldBuilder.setPrice(finalPrice);
		submitOrderReqFieldBuilder.setMinVolume(minVolume);
		submitOrderReqFieldBuilder.setStopPrice(stopPrice);
		submitOrderReqFieldBuilder.setVolumeCondition(volumeConditionComboBox.getSelectionModel().getSelectedItem());
		submitOrderReqFieldBuilder.setContingentCondition(contingentCondition);
		submitOrderReqFieldBuilder.setHedgeFlag(hedgeFlagComboBox.getSelectionModel().getSelectedItem());
		submitOrderReqFieldBuilder.setForceCloseReason(ForceCloseReasonEnum.FCR_NotForceClose);
		Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "确认提交订单？", ButtonType.YES, ButtonType.NO);
		confirmAlert.showAndWait();

		if (confirmAlert.getResult() == ButtonType.YES) {
			for (String accountId : finalAccountIdSet) {
				AccountField account = desktopTradeCachesService.queryAccountByAccountId(accountId);
				if (account == null) {
					Alert accountAlert = new Alert(AlertType.ERROR);
					accountAlert.setTitle("错误");
					accountAlert.setHeaderText("定单错误");
					accountAlert.setContentText("无法找到指定账户!账户ID:" + accountId);
					accountAlert.show();
				}

				submitOrderReqFieldBuilder.setAccountCode(account.getCode());
				submitOrderReqFieldBuilder.setGatewayId(account.getGatewayId());

				if (accountVolumeMap.containsKey(accountId) && accountVolumeMap.get(accountId) != 0) {
					submitOrderReqFieldBuilder.setVolume(accountVolumeMap.get(accountId));
					submitOrderReqFieldBuilder.setOriginOrderId(UUIDStringPoolUtils.getUUIDString());
					rpcClientApiService.asyncSubmitOrder(submitOrderReqFieldBuilder.build(), UUIDStringPoolUtils.getUUIDString());
				} else if (volume != 0) {
					submitOrderReqFieldBuilder.setVolume(volume);
					submitOrderReqFieldBuilder.setOriginOrderId(UUIDStringPoolUtils.getUUIDString());
					rpcClientApiService.asyncSubmitOrder(submitOrderReqFieldBuilder.build(), UUIDStringPoolUtils.getUUIDString());
				}

			}
		}

		submitPending = false;

	}
}
