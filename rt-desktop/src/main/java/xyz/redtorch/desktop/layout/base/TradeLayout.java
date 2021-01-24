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
import xyz.redtorch.desktop.layout.base.bean.TradeFXBean;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreField.TradeField;

import java.util.*;

@Component
public class TradeLayout {

    public static final int SHOW_ALL = 0;
    public static final int SHOW_LONG = 1;
    public static final int SHOW_SHORT = 2;
    private static final Logger logger = LoggerFactory.getLogger(TradeLayout.class);
    private final VBox vBox = new VBox();
    private final ObservableList<TradeFXBean> tradeObservableList = FXCollections.observableArrayList();
    private final TableView<TradeFXBean> tradeTableView = new TableView<>();
    private boolean layoutCreated = false;
    private List<TradeField> tradeList = new ArrayList<>();
    private Map<String, TradeFXBean> tradeFXBeanMap = new HashMap<>();

    private int showRadioValue = 0;

    private boolean showMergedFlag = false;

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
        this.tradeList = tradeList;
        render();
    }

    public void render() {
        List<TradeField> filteredTradeList = new ArrayList<>();
        for (TradeField trade : this.tradeList) {
            if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.isSelectedAccountId(trade.getAccountId())) {
                if (showRadioValue == SHOW_ALL) {
                    filteredTradeList.add(trade);
                } else if (showRadioValue == SHOW_LONG) {
                    if (DirectionEnum.D_Buy_VALUE == trade.getDirectionValue()) {
                        filteredTradeList.add(trade);
                    }
                } else if (showRadioValue == SHOW_SHORT) {
                    if (DirectionEnum.D_Sell_VALUE == trade.getDirectionValue()) {
                        filteredTradeList.add(trade);
                    }
                }
            }
        }

        List<TradeField> newTradeList;
        if (showMergedFlag) {
            newTradeList = new ArrayList<>();
            Map<String, TradeField.Builder> mergedTradeFieldBuilderMap = new HashMap<>();

            for (TradeField trade : filteredTradeList) {

                String key = trade.getContract().getUnifiedSymbol() + "#" + trade.getDirection().getValueDescriptor() + "#" + trade.getOffsetFlagValue();

                TradeField.Builder tradeFieldBuilder;
                if (mergedTradeFieldBuilderMap.containsKey(key)) {
                    tradeFieldBuilder = mergedTradeFieldBuilderMap.get(key);
                    tradeFieldBuilder.setPrice((tradeFieldBuilder.getPrice() * tradeFieldBuilder.getVolume() + trade.getPrice() * trade.getVolume()) / (tradeFieldBuilder.getVolume() + trade.getVolume()));
                    tradeFieldBuilder.setVolume(tradeFieldBuilder.getVolume() + trade.getVolume());
                } else {
                    tradeFieldBuilder = TradeField.newBuilder();
                    tradeFieldBuilder.setContract(trade.getContract());
                    tradeFieldBuilder.setDirection(trade.getDirection());
                    tradeFieldBuilder.setOffsetFlag(trade.getOffsetFlag());
                    tradeFieldBuilder.setPrice(trade.getPrice());
                    tradeFieldBuilder.setVolume(trade.getVolume());
                    tradeFieldBuilder.setTradeId(key);
                    mergedTradeFieldBuilderMap.put(key, tradeFieldBuilder);
                }
            }

            for (TradeField.Builder tradeFieldBuilder : mergedTradeFieldBuilderMap.values()) {
                newTradeList.add(tradeFieldBuilder.build());
            }
        } else {
            newTradeList = filteredTradeList;
        }

        Set<String> tradeIdSet = new HashSet<>();

        List<TradeFXBean> newTradeFXBeanList = new ArrayList<>();
        for (TradeField trade : newTradeList) {
            String tradeId = trade.getTradeId();
            tradeIdSet.add(tradeId);

            if (tradeFXBeanMap.containsKey(tradeId)) {
                tradeFXBeanMap.get(tradeId).update(trade, guiMainService.isSelectedContract(trade.getContract()));
            } else {
                TradeFXBean tradeFXBean = new TradeFXBean(trade, guiMainService.isSelectedContract(trade.getContract()));
                tradeFXBeanMap.put(tradeId, tradeFXBean);
                newTradeFXBeanList.add(tradeFXBean);
            }
        }

        tradeObservableList.addAll(newTradeFXBeanList);

        Map<String, TradeFXBean> newTradeFXBeanMap = new HashMap<>();
        for (String tradeId : tradeFXBeanMap.keySet()) {
            if (tradeIdSet.contains(tradeId)) {
                newTradeFXBeanMap.put(tradeId, tradeFXBeanMap.get(tradeId));
            }
        }
        tradeFXBeanMap = newTradeFXBeanMap;

        tradeObservableList.removeIf(tradeFXBean -> !tradeIdSet.contains(tradeFXBean.getTradeId()));

        Set<String> newSelectedTradeIdSet = new HashSet<>();
        for (TradeFXBean tradeFXBean : tradeObservableList) {
            if (selectedTradeIdSet.contains(tradeFXBean.getTradeId())) {
                tradeTableView.getSelectionModel().select(tradeFXBean);
                newSelectedTradeIdSet.add(tradeFXBean.getTradeId());
            }
        }
        selectedTradeIdSet = newSelectedTradeIdSet;
    }

    private void createLayout() {

        tradeTableView.setTableMenuButtonVisible(true);

        TableColumn<TradeFXBean, Pane> contractCol = new TableColumn<>("合约");
        contractCol.setPrefWidth(160);
        contractCol.setCellValueFactory(new PropertyValueFactory<>("contract"));
        contractCol.setComparator((Pane p1, Pane p2) -> {
            try {
                TradeField trade1 = (TradeField) p1.getUserData();
                TradeField trade2 = (TradeField) p2.getUserData();
                return StringUtils.compare(trade1.getContract().getUnifiedSymbol(), trade2.getContract().getUnifiedSymbol());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        tradeTableView.getColumns().add(contractCol);

        TableColumn<TradeFXBean, Text> directionCol = new TableColumn<>("方向");
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
        tradeTableView.getColumns().add(directionCol);


        TableColumn<TradeFXBean, String> offsetFlagCol = new TableColumn<>("开平");
        offsetFlagCol.setPrefWidth(60);
        offsetFlagCol.setCellValueFactory(new PropertyValueFactory<>("offsetFlag"));
        offsetFlagCol.setComparator(StringUtils::compare);
        tradeTableView.getColumns().add(offsetFlagCol);

        TableColumn<TradeFXBean, String> hedgeFlagCol = new TableColumn<>("投机套保");
        hedgeFlagCol.setPrefWidth(60);
        hedgeFlagCol.setCellValueFactory(new PropertyValueFactory<>("hedgeFlag"));
        hedgeFlagCol.setComparator(StringUtils::compare);
        tradeTableView.getColumns().add(hedgeFlagCol);

        TableColumn<TradeFXBean, String> priceCol = new TableColumn<>("价格");
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
        tradeTableView.getColumns().add(priceCol);

        TableColumn<TradeFXBean, Integer> volumeCol = new TableColumn<>("成交量");
        volumeCol.setPrefWidth(50);
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));
        volumeCol.setComparator(Comparator.comparingInt((Integer i) -> i));
        tradeTableView.getColumns().add(volumeCol);

        TableColumn<TradeFXBean, String> tradeTimeCol = new TableColumn<>("成交时间");
        tradeTimeCol.setPrefWidth(120);
        tradeTimeCol.setCellValueFactory(new PropertyValueFactory<>("tradeTime"));
        tradeTimeCol.setComparator(StringUtils::compare);
        tradeTimeCol.setSortType(SortType.DESCENDING);
        tradeTableView.getColumns().add(tradeTimeCol);

        TableColumn<TradeFXBean, String> adapterTradeIdCol = new TableColumn<>("适配器成交编号");
        adapterTradeIdCol.setPrefWidth(250);
        adapterTradeIdCol.setCellValueFactory(new PropertyValueFactory<>("adapterTradeId"));
        adapterTradeIdCol.setComparator(StringUtils::compare);
        tradeTableView.getColumns().add(adapterTradeIdCol);

        TableColumn<TradeFXBean, String> originOrderIdCol = new TableColumn<>("原始定单编号");
        originOrderIdCol.setPrefWidth(120);
        originOrderIdCol.setCellValueFactory(new PropertyValueFactory<>("originOrderId"));
        originOrderIdCol.setComparator(StringUtils::compare);
        tradeTableView.getColumns().add(originOrderIdCol);

        TableColumn<TradeFXBean, String> accountIdCol = new TableColumn<>("账户ID");
        accountIdCol.setPrefWidth(350);
        accountIdCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        accountIdCol.setComparator(StringUtils::compare);
        tradeTableView.getColumns().add(accountIdCol);

        SortedList<TradeFXBean> sortedItems = new SortedList<>(tradeObservableList);
        tradeTableView.setItems(sortedItems);
        sortedItems.comparatorProperty().bind(tradeTableView.comparatorProperty());

        tradeTableView.getSortOrder().add(tradeTimeCol);
        tradeTableView.setFocusTraversable(false);

        tradeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tradeTableView.setRowFactory(tv -> {
            TableRow<TradeFXBean> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    ObservableList<TradeFXBean> selectedItems = tradeTableView.getSelectionModel().getSelectedItems();
                    selectedTradeIdSet.clear();
                    for (TradeFXBean trade : selectedItems) {
                        selectedTradeIdSet.add(trade.getTradeId());
                    }
                    TradeFXBean clickedItem = row.getItem();
                    guiMainService.updateSelectedContract(clickedItem.getTradeField().getContract());
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
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {

            showRadioValue = (int) newValue.getUserData();
            render();

        });

        hBox.getChildren().add(allRadioButton);
        hBox.getChildren().add(longRadioButton);
        hBox.getChildren().add(shortRadioButton);

        CheckBox showMergedCheckBox = new CheckBox("合并显示");
        showMergedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showMergedFlag = newValue;
            render();
        });

        hBox.getChildren().add(showMergedCheckBox);

        vBox.getChildren().add(hBox);

    }

}
