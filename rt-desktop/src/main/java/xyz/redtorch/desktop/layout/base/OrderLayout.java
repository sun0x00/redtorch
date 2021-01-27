package xyz.redtorch.desktop.layout.base;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.desktop.layout.base.bean.OrderFXBean;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreEnum.OrderStatusEnum;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.TradeField;

import java.util.*;

@Component
public class OrderLayout {

    private static final Logger logger = LoggerFactory.getLogger(OrderLayout.class);

    public static final int SHOW_ALL = 0;
    public static final int SHOW_CANCELABLE = 1;
    public static final int SHOW_CANCELLED = 2;

    private final VBox vBox = new VBox();

    private boolean layoutCreated = false;

    private final ObservableList<OrderFXBean> orderObservableList = FXCollections.observableArrayList();

    private List<OrderField> orderList = new ArrayList<>();

    private final TableView<OrderFXBean> orderTableView = new TableView<>();

    private Map<String, OrderFXBean> orderFXBeanMap = new HashMap<>();

    private int showRadioValue = 0;

    private boolean showRejectedFlag = false;

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
        this.orderList = orderList;
        render();
    }

    public void render() {
        orderTableView.getSelectionModel().clearSelection();

        List<OrderField> filteredOrderList = new ArrayList<>();
        for (OrderField order : this.orderList) {
            if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.getSelectedAccountIdSet().contains(order.getAccountId())) {
                if (showRadioValue == SHOW_ALL) {
                    if (showRejectedFlag) {
                        filteredOrderList.add(order);
                    } else if (OrderStatusEnum.OS_Rejected_VALUE != order.getOrderStatusValue()) {
                        filteredOrderList.add(order);
                    }
                } else if (showRadioValue == SHOW_CANCELABLE) {
                    if (CommonConstant.ORDER_STATUS_WORKING_SET.contains(order.getOrderStatus())) {
                        filteredOrderList.add(order);
                    }
                } else {
                    if (OrderStatusEnum.OS_Canceled_VALUE == order.getOrderStatusValue()) {
                        filteredOrderList.add(order);
                    }
                }
            }
        }

        Set<String> orderIdSet = new HashSet<>();

        List<OrderFXBean> newOrderFXBeanList = new ArrayList<>();
        for (OrderField order : filteredOrderList) {
            String orderId = order.getOrderId();
            orderIdSet.add(orderId);

            if (orderFXBeanMap.containsKey(orderId)) {
                orderFXBeanMap.get(orderId).update(order, guiMainService.isSelectedContract(order.getContract()));
            } else {
                OrderFXBean orderFXBean = new OrderFXBean(order, guiMainService.isSelectedContract(order.getContract()));
                orderFXBeanMap.put(orderId, orderFXBean);
                newOrderFXBeanList.add(orderFXBean);
            }
        }

        orderObservableList.addAll(newOrderFXBeanList);

        Map<String, OrderFXBean> newOrderFXBeanMap = new HashMap<>();
        for (String orderId : orderFXBeanMap.keySet()) {
            if (orderIdSet.contains(orderId)) {
                newOrderFXBeanMap.put(orderId, orderFXBeanMap.get(orderId));
            }
        }
        orderFXBeanMap = newOrderFXBeanMap;

        orderObservableList.removeIf(orderFXBean -> !orderIdSet.contains(orderFXBean.getOrderId()));

        Set<String> newSelectedOrderIdSet = new HashSet<>();
        for (OrderFXBean orderFXBean : orderObservableList) {
            if (selectedOrderIdSet.contains(orderFXBean.getOrderId())) {
                orderTableView.getSelectionModel().select(orderFXBean);
                newSelectedOrderIdSet.add(orderFXBean.getOrderId());
            }
        }
        selectedOrderIdSet = newSelectedOrderIdSet;

    }

    private void createLayout() {

        orderTableView.setTableMenuButtonVisible(true);

        TableColumn<OrderFXBean, Pane> contractCol = new TableColumn<>("合约");
        contractCol.setPrefWidth(160);
        contractCol.setCellValueFactory(new PropertyValueFactory<>("contract"));
        contractCol.setComparator((Pane p1, Pane p2) -> {
            try {
                TradeField trade1 = (TradeField) p1.getUserData();
                TradeField trade2 = (TradeField) p2.getUserData();
                return StringUtils.compare(trade1.getContract().getUniformSymbol(), trade2.getContract().getUniformSymbol());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        orderTableView.getColumns().add(contractCol);

        TableColumn<OrderFXBean, Text> directionCol = new TableColumn<>("方向");
        directionCol.setPrefWidth(40);
        directionCol.setCellValueFactory(new PropertyValueFactory<>("direction"));
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
        orderTableView.getColumns().add(directionCol);


        TableColumn<OrderFXBean, String> offsetFlagCol = new TableColumn<>("开平");
        offsetFlagCol.setPrefWidth(60);
        offsetFlagCol.setCellValueFactory(new PropertyValueFactory<>("offsetFlag"));
        offsetFlagCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(offsetFlagCol);

        TableColumn<OrderFXBean, String> hedgeFlagCol = new TableColumn<>("投机套保");
        hedgeFlagCol.setPrefWidth(60);
        hedgeFlagCol.setCellValueFactory(new PropertyValueFactory<>("hedgeFlag"));
        hedgeFlagCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(hedgeFlagCol);

        TableColumn<OrderFXBean, String> orderPriceTypeCol = new TableColumn<>("价格类型");
        orderPriceTypeCol.setPrefWidth(60);
        orderPriceTypeCol.setCellValueFactory(new PropertyValueFactory<>("orderPriceType"));
        orderPriceTypeCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(orderPriceTypeCol);

        TableColumn<OrderFXBean, String> priceCol = new TableColumn<>("价格");
        priceCol.setPrefWidth(80);
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        orderTableView.getColumns().add(priceCol);

        TableColumn<OrderFXBean, Pane> volumeCol = new TableColumn<>("数量");
        volumeCol.setPrefWidth(70);
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));

        volumeCol.setComparator((Pane p1, Pane p2) -> {
            try {
                OrderField order1 = (OrderField) p1.getUserData();
                OrderField order2 = (OrderField) p2.getUserData();
                return Integer.compare(order1.getTotalVolume(), order2.getTotalVolume());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });

        orderTableView.getColumns().add(volumeCol);

        TableColumn<OrderFXBean, Text> orderStatusCol = new TableColumn<>("状态");
        orderStatusCol.setPrefWidth(60);
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        orderStatusCol.setComparator((Text t1, Text t2) -> {
            try {
                OrderField order1 = (OrderField) t1.getUserData();
                OrderField order2 = (OrderField) t2.getUserData();
                return Integer.compare(order1.getOrderStatus().getNumber(), order2.getOrderStatus().getNumber());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });

        orderTableView.getColumns().add(orderStatusCol);

        TableColumn<OrderFXBean, String> statusMsgCol = new TableColumn<>("状态信息");
        statusMsgCol.setPrefWidth(130);
        statusMsgCol.setCellValueFactory(new PropertyValueFactory<>("statusMsg"));
        statusMsgCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(statusMsgCol);

        TableColumn<OrderFXBean, String> orderTimeCol = new TableColumn<>("委托时间");
        orderTimeCol.setPrefWidth(70);
        orderTimeCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        orderTimeCol.setComparator(StringUtils::compare);
        orderTimeCol.setSortType(SortType.DESCENDING);
        orderTableView.getColumns().add(orderTimeCol);

        TableColumn<OrderFXBean, String> timeConditionCol = new TableColumn<>("时效类型");
        timeConditionCol.setPrefWidth(100);
        timeConditionCol.setCellValueFactory(new PropertyValueFactory<>("timeCondition"));
        timeConditionCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(timeConditionCol);

        TableColumn<OrderFXBean, String> volumeConditionCol = new TableColumn<>("成交量类型");
        volumeConditionCol.setPrefWidth(70);
        volumeConditionCol.setCellValueFactory(new PropertyValueFactory<>("volumeCondition"));
        volumeConditionCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(volumeConditionCol);

        TableColumn<OrderFXBean, Integer> minVolumeCol = new TableColumn<>("最小数量");
        minVolumeCol.setPrefWidth(60);
        minVolumeCol.setCellValueFactory(new PropertyValueFactory<>("minVolume"));
        minVolumeCol.setComparator(Comparator.comparingInt((Integer i) -> i));
        orderTableView.getColumns().add(minVolumeCol);

        TableColumn<OrderFXBean, String> contingentConditionCol = new TableColumn<>("触发条件");
        contingentConditionCol.setPrefWidth(120);
        contingentConditionCol.setCellValueFactory(new PropertyValueFactory<>("contingentCondition"));
        contingentConditionCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(contingentConditionCol);

        TableColumn<OrderFXBean, String> stopPriceCol = new TableColumn<>("条件价格");
        stopPriceCol.setPrefWidth(80);
        stopPriceCol.setCellValueFactory(new PropertyValueFactory<>("stopPrice"));
        stopPriceCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        orderTableView.getColumns().add(stopPriceCol);

        TableColumn<OrderFXBean, String> adapterOrderIdCol = new TableColumn<>("编号");
        adapterOrderIdCol.setPrefWidth(150);
        adapterOrderIdCol.setCellValueFactory(new PropertyValueFactory<>("adapterOrderId"));
        statusMsgCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(adapterOrderIdCol);

        TableColumn<OrderFXBean, String> originOrderIdCol = new TableColumn<>("原始编号");
        originOrderIdCol.setPrefWidth(260);
        originOrderIdCol.setCellValueFactory(new PropertyValueFactory<>("originOrderId"));
        originOrderIdCol.setSortable(false);
        orderTableView.getColumns().add(originOrderIdCol);

        TableColumn<OrderFXBean, String> accountIdCol = new TableColumn<>("账户ID");
        accountIdCol.setPrefWidth(350);
        accountIdCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        accountIdCol.setComparator(StringUtils::compare);
        orderTableView.getColumns().add(accountIdCol);

        SortedList<OrderFXBean> sortedItems = new SortedList<>(orderObservableList);
        orderTableView.setItems(sortedItems);
        sortedItems.comparatorProperty().bind(orderTableView.comparatorProperty());

        orderTableView.getSortOrder().add(orderTimeCol);

        orderTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        orderTableView.setOnMousePressed(event -> {
            ObservableList<OrderFXBean> selectedItems = orderTableView.getSelectionModel().getSelectedItems();
            selectedOrderIdSet.clear();
            for (OrderFXBean row : selectedItems) {
                selectedOrderIdSet.add(row.getOrderId());
            }
        });

        orderTableView.setRowFactory(tv -> {
            TableRow<OrderFXBean> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    ObservableList<OrderFXBean> selectedItems = orderTableView.getSelectionModel().getSelectedItems();
                    selectedOrderIdSet.clear();
                    for (OrderFXBean order : selectedItems) {
                        selectedOrderIdSet.add(order.getOrderId());
                    }
                    OrderFXBean clickedItem = row.getItem();
                    guiMainService.updateSelectedContract(clickedItem.getOrderField().getContract());
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
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            showRadioValue = (int) newValue.getUserData();
            render();

        });

        hBox.getChildren().add(allRadioButton);
        hBox.getChildren().add(cancelableRadioButton);
        hBox.getChildren().add(cancelledRadioButton);

        CheckBox showRejectedCheckBox = new CheckBox("显示拒单");
        showRejectedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showRejectedFlag = newValue;
            render();
        });

        hBox.getChildren().add(showRejectedCheckBox);

        vBox.getChildren().add(hBox);

    }

}
