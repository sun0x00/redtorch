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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreEnum.ContingentConditionEnum;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreEnum.OrderPriceTypeEnum;
import xyz.redtorch.pb.CoreEnum.OrderStatusEnum;
import xyz.redtorch.pb.CoreEnum.TimeConditionEnum;
import xyz.redtorch.pb.CoreEnum.VolumeConditionEnum;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;

@Component
public class OrderLayout {

	private static final Logger logger = LoggerFactory.getLogger(OrderLayout.class);

	public static final int SHOW_ALL = 0;
	public static final int SHOW_CANCELABLE = 1;
	public static final int SHOW_CANCELLED = 2;

	private VBox vBox = new VBox();

	private boolean layoutCreated = false;

	private ObservableList<OrderField> orderObservableList = FXCollections.observableArrayList();

	private List<OrderField> orderList = new ArrayList<>();

	private TableView<OrderField> orderTableView = new TableView<>();

	private int showRadioValue = 0;

	private boolean showRejectedChecked = false;

	private Set<String> selectedOrderIdSet = new HashSet<>();

	@Autowired
	private GuiMainService guiMainService;
	@Autowired
	private RpcClientApiService rpcClientApiService;

	public Node getNode() {
		if (!layoutCreated) {
			createLayout();
			layoutCreated = true;
		}
		return this.vBox;
	}

	public void updateData(List<OrderField> orderList) {
		if (!new HashSet<>(this.orderList).equals(new HashSet<>(orderList))) {
			this.orderList = orderList;
			fillingData();
		}
	}

	public void fillingData() {
		orderTableView.getSelectionModel().clearSelection();

		List<OrderField> newOrderList = new ArrayList<>();
		for (OrderField order : this.orderList) {
			if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.getSelectedAccountIdSet().contains(order.getAccountId())) {
				if (showRadioValue == SHOW_ALL) {
					if (showRejectedChecked) {
						newOrderList.add(order);
					} else if (OrderStatusEnum.OS_Rejected_VALUE != order.getOrderStatusValue()) {
						newOrderList.add(order);
					}
				} else if (showRadioValue == SHOW_CANCELABLE) {
					if (CommonConstant.ORDER_STATUS_WORKING_SET.contains(order.getOrderStatus())) {
						newOrderList.add(order);
					}
				} else {
					if (OrderStatusEnum.OS_Canceled_VALUE == order.getOrderStatusValue()) {
						newOrderList.add(order);
					}
				}
			}
		}

		orderObservableList.clear();
		orderObservableList.addAll(newOrderList);
		Set<String> newSelectedOrderIdSet = new HashSet<>();
		for (OrderField order : orderObservableList) {
			if (selectedOrderIdSet.contains(order.getOrderId())) {
				orderTableView.getSelectionModel().select(order);
				newSelectedOrderIdSet.add(order.getOrderId());
			}
		}
		selectedOrderIdSet = newSelectedOrderIdSet;

	}

	private void createLayout() {

		orderTableView.setTableMenuButtonVisible(true);

		TableColumn<OrderField, Pane> unifiedSymbolCol = new TableColumn<>("合约");
		unifiedSymbolCol.setPrefWidth(160);
		unifiedSymbolCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {
				OrderField order = feature.getValue();
				Text unifiedSymbolText = new Text(order.getContract().getUnifiedSymbol());
				Text shortNameText = new Text(order.getContract().getName());
				vBox.getChildren().add(unifiedSymbolText);
				vBox.getChildren().add(shortNameText);

				if (guiMainService.getSelectedContract() != null && guiMainService.getSelectedContract().getUnifiedSymbol().equals(order.getContract().getUnifiedSymbol())) {
					unifiedSymbolText.getStyleClass().add("trade-remind-color");
				}

				vBox.setUserData(order);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleObjectProperty<>(vBox);
		});

		unifiedSymbolCol.setComparator((Pane p1, Pane p2) -> {
			try {
				OrderField order1 = (OrderField) p1.getUserData();
				OrderField order2 = (OrderField) p2.getUserData();
				return StringUtils.compare(order1.getContract().getUnifiedSymbol(), order2.getContract().getUnifiedSymbol());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		orderTableView.getColumns().add(unifiedSymbolCol);

		TableColumn<OrderField, Text> directionCol = new TableColumn<>("方向");
		directionCol.setPrefWidth(40);
		directionCol.setCellValueFactory(feature -> {
			Text directionText = new Text("未知");

			try {
				OrderField order = feature.getValue();

				if (order.getDirection() == DirectionEnum.D_Buy) {
					directionText.setText("多");
					directionText.getStyleClass().add("trade-long-color");
				} else if (order.getDirection() == DirectionEnum.D_Sell) {
					directionText.setText("空");
					directionText.getStyleClass().add("trade-short-color");
				} else if (order.getDirection() == DirectionEnum.D_Unknown) {
					directionText.setText("未知");
				}

				directionText.setUserData(order);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(directionText);
		});

		directionCol.setComparator((Text t1, Text t2) -> {
			try {
				OrderField order1 = (OrderField) t1.getUserData();
				OrderField order2 = (OrderField) t2.getUserData();
				return Integer.compare(order1.getDirection().getNumber(), order2.getDirection().getNumber());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		orderTableView.getColumns().add(directionCol);

		TableColumn<OrderField, String> offsetCol = new TableColumn<>("开平");
		offsetCol.setPrefWidth(40);
		offsetCol.setCellValueFactory(feature -> {
			String offset = "未知";

			try {
				OrderField order = feature.getValue();

				if (order.getOffsetFlag() == OffsetFlagEnum.OF_Close) {
					offset = "平";
				} else if (order.getOffsetFlag() == OffsetFlagEnum.OF_CloseToday) {
					offset = "平今";
				} else if (order.getOffsetFlag() == OffsetFlagEnum.OF_CloseYesterday) {

					offset = "平昨";
					;
				} else if (order.getOffsetFlag() == OffsetFlagEnum.OF_Open) {
					offset = "开";
				} else if (order.getOffsetFlag() == OffsetFlagEnum.OF_Unkonwn) {
					offset = "未知";
				} else {
					offset = order.getOffsetFlag().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(offset);
		});
		offsetCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(offsetCol);

		TableColumn<OrderField, String> hedgeFlagCol = new TableColumn<>("投机套保");
		hedgeFlagCol.setPrefWidth(60);
		hedgeFlagCol.setCellValueFactory(feature -> {
			String hedgeFlag = "未知";

			try {
				OrderField order = feature.getValue();

				if (order.getHedgeFlag() == HedgeFlagEnum.HF_Speculation) {
					hedgeFlag = "投机";
				} else if (order.getHedgeFlag() == HedgeFlagEnum.HF_Hedge) {
					hedgeFlag = "套保";
				} else if (order.getHedgeFlag() == HedgeFlagEnum.HF_Arbitrage) {
					hedgeFlag = "套利";
				} else if (order.getHedgeFlag() == HedgeFlagEnum.HF_MarketMaker) {
					hedgeFlag = "做市商";
				} else if (order.getHedgeFlag() == HedgeFlagEnum.HF_SpecHedge) {
					hedgeFlag = "第一条腿投机第二条腿套保 大商所专用";
				} else if (order.getHedgeFlag() == HedgeFlagEnum.HF_HedgeSpec) {
					hedgeFlag = "第一条腿套保第二条腿投机 大商所专用";
				} else if (order.getHedgeFlag() == HedgeFlagEnum.HF_Unknown) {
					hedgeFlag = "未知";
				} else {
					hedgeFlag = order.getHedgeFlag().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(hedgeFlag);
		});
		hedgeFlagCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(hedgeFlagCol);

		TableColumn<OrderField, String> orderPriceTypeCol = new TableColumn<>("价格类型");
		orderPriceTypeCol.setPrefWidth(60);
		orderPriceTypeCol.setCellValueFactory(feature -> {
			String orderPriceType = "未知";
			try {
				OrderField order = feature.getValue();

				if (order.getOrderPriceType() == OrderPriceTypeEnum.OPT_LimitPrice) {
					orderPriceType = "限价";
				} else if (order.getOrderPriceType() == OrderPriceTypeEnum.OPT_AnyPrice) {
					orderPriceType = "市价";
				} else if (order.getOrderPriceType() == OrderPriceTypeEnum.OPT_BestPrice) {
					orderPriceType = "最优价";
				} else if (order.getOrderPriceType() == OrderPriceTypeEnum.OPT_LastPrice) {
					orderPriceType = "最新价";
				} else if (order.getOrderPriceType() == OrderPriceTypeEnum.OPT_LastPricePlusOneTicks) {
					orderPriceType = "最新价浮动上浮1个ticks";
				} else if (order.getOrderPriceType() == OrderPriceTypeEnum.OPT_LastPricePlusThreeTicks) {
					orderPriceType = "最新价浮动上浮3个ticks";
				} else if (order.getOrderPriceType() == OrderPriceTypeEnum.OPT_Unknown) {
					orderPriceType = "未知";
				} else {
					orderPriceType = order.getOrderPriceType().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(orderPriceType);
		});
		orderPriceTypeCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(orderPriceTypeCol);

		TableColumn<OrderField, String> priceCol = new TableColumn<>("价格");
		priceCol.setPrefWidth(80);
		priceCol.setCellValueFactory(feature -> {
			String priceString = "";
			try {

				OrderField order = feature.getValue();
				ContractField contract = order.getContract();
				int dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
				if (dcimalDigits < 0) {
					dcimalDigits = 0;
				}
				String priceStringFormat = "%,." + dcimalDigits + "f";
				priceString = String.format(priceStringFormat, order.getPrice());
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
		orderTableView.getColumns().add(priceCol);

		TableColumn<OrderField, Pane> totalVolumeCol = new TableColumn<>("数量");
		totalVolumeCol.setPrefWidth(70);
		totalVolumeCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {

				OrderField order = feature.getValue();

				HBox totalVolumeHBox = new HBox();
				Text totalVolumeLabelText = new Text("总计");
				totalVolumeLabelText.setWrappingWidth(35);
				totalVolumeLabelText.getStyleClass().add("trade-label");
				totalVolumeHBox.getChildren().add(totalVolumeLabelText);

				Text totalVolumeText = new Text("" + order.getTotalVolume());
				totalVolumeHBox.getChildren().add(totalVolumeText);
				vBox.getChildren().add(totalVolumeHBox);

				HBox tradedVolumeHBox = new HBox();
				Text tradedVolumeLabelText = new Text("成交");
				tradedVolumeLabelText.setWrappingWidth(35);
				tradedVolumeLabelText.getStyleClass().add("trade-label");
				tradedVolumeHBox.getChildren().add(tradedVolumeLabelText);

				Text tradedVolumeText = new Text("" + order.getTradedVolume());
				tradedVolumeHBox.getChildren().add(tradedVolumeText);
				vBox.getChildren().add(tradedVolumeHBox);

				vBox.setUserData(order);
				if (CommonConstant.ORDER_STATUS_WORKING_SET.contains(order.getOrderStatus())) {
					if (order.getDirection() == DirectionEnum.D_Buy) {
						totalVolumeText.getStyleClass().add("trade-long-color");
					} else if (order.getDirection() == DirectionEnum.D_Sell) {
						totalVolumeText.getStyleClass().add("trade-short-color");
					}
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(vBox);
		});

		totalVolumeCol.setComparator((Pane p1, Pane p2) -> {
			try {
				OrderField order1 = (OrderField) p1.getUserData();
				OrderField order2 = (OrderField) p2.getUserData();
				return Integer.compare(order1.getTotalVolume(), order2.getTotalVolume());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		orderTableView.getColumns().add(totalVolumeCol);

		TableColumn<OrderField, Text> statusCol = new TableColumn<>("状态");
		statusCol.setPrefWidth(60);
		statusCol.setCellValueFactory(feature -> {

			Text statusText = new Text("未知");
			try {
				OrderField order = feature.getValue();

				if (order.getOrderStatus() == OrderStatusEnum.OS_AllTraded) {
					statusText.setText("全部成交");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_Canceled) {
					statusText.setText("已撤销");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_NoTradeQueueing) {
					statusText.setText("未成交还在队列中");
					statusText.getStyleClass().add("trade-remind-color");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_NoTradeNotQueueing) {
					statusText.setText("未成交不在队列中");
					statusText.getStyleClass().add("trade-remind-color");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_PartTradedQueueing) {
					statusText.setText("部分成交还在队列中");
					statusText.getStyleClass().add("trade-remind-color");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_PartTradedNotQueueing) {
					statusText.setText("部分成交不在队列中");
					statusText.getStyleClass().add("trade-remind-color");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_Rejected) {
					statusText.setText("拒单");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_NotTouched) {
					statusText.setText("未触发");
					statusText.getStyleClass().add("trade-remind-color");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_Touched) {
					statusText.setText("已触发");
				} else if (order.getOrderStatus() == OrderStatusEnum.OS_Unknown) {
					statusText.setText("未知");
				} else {
					statusText.setText(order.getOrderStatus().getValueDescriptor().getName());
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(statusText);
		});
		statusCol.setComparator((Text t1, Text t2) -> {
			try {
				OrderField order1 = (OrderField) t1.getUserData();
				OrderField order2 = (OrderField) t2.getUserData();
				return Integer.compare(order1.getOrderStatus().getNumber(), order2.getOrderStatus().getNumber());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		orderTableView.getColumns().add(statusCol);

		TableColumn<OrderField, String> statusInfoCol = new TableColumn<>("状态信息");
		statusInfoCol.setPrefWidth(130);
		statusInfoCol.setCellValueFactory(feature -> {
			String statusInfo = "";
			try {
				statusInfo = feature.getValue().getStatusMsg();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(statusInfo);
		});
		statusInfoCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(statusInfoCol);

		TableColumn<OrderField, Pane> timeCol = new TableColumn<>("时间");
		timeCol.setPrefWidth(90);
		timeCol.setCellValueFactory(feature -> {
			VBox vBox = new VBox();
			try {
				OrderField order = feature.getValue();

				HBox orderTimeHBox = new HBox();
				Text orderTimeLabelText = new Text("定单");
				orderTimeLabelText.setWrappingWidth(35);
				orderTimeLabelText.getStyleClass().add("trade-label");
				orderTimeHBox.getChildren().add(orderTimeLabelText);

				Text totalVolumeText = new Text("" + order.getOrderTime());
				orderTimeHBox.getChildren().add(totalVolumeText);
				vBox.getChildren().add(orderTimeHBox);

				HBox updateTimeHBox = new HBox();
				Text updateTimeLabelText = new Text("更新");
				updateTimeLabelText.setWrappingWidth(35);
				updateTimeLabelText.getStyleClass().add("trade-label");
				updateTimeHBox.getChildren().add(updateTimeLabelText);

				Text updateTimeText = new Text("" + order.getUpdateTime());
				updateTimeHBox.getChildren().add(updateTimeText);
				vBox.getChildren().add(updateTimeHBox);

				vBox.setUserData(order);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleObjectProperty<>(vBox);
		});

		timeCol.setComparator((Pane p1, Pane p2) -> {
			try {
				OrderField order1 = (OrderField) p1.getUserData();
				OrderField order2 = (OrderField) p2.getUserData();
				return StringUtils.compare(order1.getOrderDate()+order1.getOrderTime(), order2.getOrderDate()+order2.getOrderTime());
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		timeCol.setSortType(SortType.DESCENDING);

		orderTableView.getColumns().add(timeCol);

		TableColumn<OrderField, String> timeConditionCol = new TableColumn<>("时效类型");
		timeConditionCol.setPrefWidth(100);
		timeConditionCol.setCellValueFactory(feature -> {
			String timeCondition = "未知";

			try {
				OrderField order = feature.getValue();

				if (order.getTimeCondition() == TimeConditionEnum.TC_GFA) {
					timeCondition = "(GFA)集合竞价有效";
				} else if (order.getTimeCondition() == TimeConditionEnum.TC_GFD) {
					timeCondition = "(GFD)当日有效";
				} else if (order.getTimeCondition() == TimeConditionEnum.TC_GFS) {
					timeCondition = "(GFS)本节有效";
				} else if (order.getTimeCondition() == TimeConditionEnum.TC_GTC) {
					timeCondition = "(GTC)撤销前有效";
				} else if (order.getTimeCondition() == TimeConditionEnum.TC_GTD) {
					timeCondition = "(GTD)指定日期前有效";
				} else if (order.getTimeCondition() == TimeConditionEnum.TC_IOC) {
					timeCondition = "(IOC)立即完成,否则撤销";
				} else if (order.getTimeCondition() == TimeConditionEnum.TC_Unkonwn) {
					timeCondition = "未知";
				} else {
					timeCondition = order.getTimeCondition().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(timeCondition);
		});
		timeConditionCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(timeConditionCol);

		TableColumn<OrderField, String> volumeConditionCol = new TableColumn<>("成交量类型");
		volumeConditionCol.setPrefWidth(70);
		volumeConditionCol.setCellValueFactory(feature -> {
			String volumeCondition = "未知";

			try {
				OrderField order = feature.getValue();

				if (order.getVolumeCondition() == VolumeConditionEnum.VC_AV) {
					volumeCondition = "任何数量";
				} else if (order.getVolumeCondition() == VolumeConditionEnum.VC_CV) {
					volumeCondition = "全部数量";
				} else if (order.getVolumeCondition() == VolumeConditionEnum.VC_MV) {
					volumeCondition = "最小数量";
				} else if (order.getVolumeCondition() == VolumeConditionEnum.VC_Unkonwn) {
					volumeCondition = "未知";
				} else {
					volumeCondition = order.getVolumeCondition().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(volumeCondition);
		});
		volumeConditionCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(volumeConditionCol);

		TableColumn<OrderField, Integer> minVolumeCol = new TableColumn<>("最小数量");
		minVolumeCol.setPrefWidth(60);
		minVolumeCol.setCellValueFactory(feature -> {
			Integer minVolume = Integer.MAX_VALUE;
			try {
				minVolume = feature.getValue().getMinVolume();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleIntegerProperty(minVolume).asObject();
		});
		minVolumeCol.setComparator((Integer i1, Integer i2) -> Integer.compare(i1, i2));
		orderTableView.getColumns().add(minVolumeCol);

		TableColumn<OrderField, String> contingentConditionCol = new TableColumn<>("触发条件");
		contingentConditionCol.setPrefWidth(120);
		contingentConditionCol.setCellValueFactory(feature -> {
			String contingentCondition = "未知";

			try {
				OrderField order = feature.getValue();

				if (order.getContingentCondition() == ContingentConditionEnum.CC_Immediately) {
					contingentCondition = "立即";
				} else if (order.getContingentCondition() == ContingentConditionEnum.CC_LocalLastPriceGreaterEqualStopPrice) {
					contingentCondition = "(本地)最新价大于等于条件价";
				} else if (order.getContingentCondition() == ContingentConditionEnum.CC_LocalLastPriceLesserEqualStopPrice) {
					contingentCondition = "(本地)最新价小于等于条件价";
				} else if (order.getContingentCondition() == ContingentConditionEnum.CC_LastPriceGreaterEqualStopPrice) {
					contingentCondition = "最新价大于等于条件价";
				} else if (order.getContingentCondition() == ContingentConditionEnum.CC_LastPriceLesserEqualStopPrice) {
					contingentCondition = "最新价小于等于条件价";
				} else if (order.getContingentCondition() == ContingentConditionEnum.CC_Unkonwn) {
					contingentCondition = "未知";
				} else {
					contingentCondition = order.getContingentCondition().getValueDescriptor().getName();
				}
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleStringProperty(contingentCondition);
		});
		contingentConditionCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(contingentConditionCol);

		TableColumn<OrderField, String> stopPriceCol = new TableColumn<>("条件价格");
		stopPriceCol.setPrefWidth(80);
		stopPriceCol.setCellValueFactory(feature -> {
			String stopPriceString = "";
			try {

				OrderField order = feature.getValue();
				ContractField contract = order.getContract();
				int dcimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
				if (dcimalDigits < 0) {
					dcimalDigits = 0;
				}
				String stopPriceStringFormat = "%,." + dcimalDigits + "f";
				stopPriceString = String.format(stopPriceStringFormat, order.getStopPrice());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(stopPriceString);

		});
		stopPriceCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		orderTableView.getColumns().add(stopPriceCol);

		TableColumn<OrderField, String> adapterOrderIdCol = new TableColumn<>("编号");
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
		statusInfoCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		orderTableView.getColumns().add(adapterOrderIdCol);

		TableColumn<OrderField, String> accountIdCol = new TableColumn<>("账户ID");
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
		orderTableView.getColumns().add(accountIdCol);

		TableColumn<OrderField, String> originOrderIdCol = new TableColumn<>("原始编号");
		originOrderIdCol.setPrefWidth(260);
		originOrderIdCol.setCellValueFactory(feature -> {
			String originOrderId = "";
			try {
				originOrderId = feature.getValue().getOriginOrderId();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(originOrderId);
		});
		originOrderIdCol.setSortable(false);
		orderTableView.getColumns().add(originOrderIdCol);

		SortedList<OrderField> sortedItems = new SortedList<>(orderObservableList);
		orderTableView.setItems(sortedItems);
		sortedItems.comparatorProperty().bind(orderTableView.comparatorProperty());

		orderTableView.getSortOrder().add(timeCol);

		orderTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		orderTableView.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				ObservableList<OrderField> selectedItems = orderTableView.getSelectionModel().getSelectedItems();
				selectedOrderIdSet.clear();
				for (OrderField row : selectedItems) {
					selectedOrderIdSet.add(row.getOrderId());
				}
			}
		});

		orderTableView.setRowFactory(tv -> {
			TableRow<OrderField> row = new TableRow<>();
			row.setOnMousePressed(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
					ObservableList<OrderField> selectedItems = orderTableView.getSelectionModel().getSelectedItems();
					selectedOrderIdSet.clear();
					for (OrderField order : selectedItems) {
						selectedOrderIdSet.add(order.getOrderId());
					}
					OrderField clickedItem = row.getItem();
					guiMainService.updateSelectedContarct(clickedItem.getContract());
				} else if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {

					CancelOrderReqField.Builder cancelOrderReqFieldBuilder = CancelOrderReqField.newBuilder();
					cancelOrderReqFieldBuilder.setOrderId(row.getItem().getOrderId());
					cancelOrderReqFieldBuilder.setOriginOrderId(row.getItem().getOriginOrderId());
					rpcClientApiService.asyncCancelOrder(cancelOrderReqFieldBuilder.build(), UUIDStringPoolUtils.getUUIDString());
				}
			});
			return row;
		});

		orderTableView.setFocusTraversable(false);

		vBox.getChildren().add(orderTableView);
		VBox.setVgrow(orderTableView, Priority.ALWAYS);

		HBox hBox = new HBox();
		RadioButton allRadioButton = new RadioButton("全部");
		RadioButton cancelableRadioButton = new RadioButton("可撤销");
		RadioButton cancelledRadioButton = new RadioButton("已撤销");
		ToggleGroup toggleGroup = new ToggleGroup();
		allRadioButton.setToggleGroup(toggleGroup);
		allRadioButton.setUserData(SHOW_ALL);
		cancelableRadioButton.setToggleGroup(toggleGroup);
		cancelableRadioButton.setUserData(SHOW_CANCELABLE);
		cancelledRadioButton.setToggleGroup(toggleGroup);
		cancelledRadioButton.setUserData(SHOW_CANCELLED);
		allRadioButton.setSelected(true);
		toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				showRadioValue = (int) newValue.getUserData();
				fillingData();

			};
		});

		hBox.getChildren().add(allRadioButton);
		hBox.getChildren().add(cancelableRadioButton);
		hBox.getChildren().add(cancelledRadioButton);

		CheckBox showRejectedCheckBox = new CheckBox("显示拒单");
		showRejectedCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				showRejectedChecked = newValue;
				fillingData();
			}
		});

		hBox.getChildren().add(showRejectedCheckBox);

		vBox.getChildren().add(hBox);

	}

}
