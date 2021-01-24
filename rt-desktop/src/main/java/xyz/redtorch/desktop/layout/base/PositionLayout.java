package xyz.redtorch.desktop.layout.base;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.*;
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
import xyz.redtorch.desktop.layout.base.bean.PositionFXBean;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.PositionField;

import java.util.*;

@Component
public class PositionLayout {

    private static final Logger logger = LoggerFactory.getLogger(PositionLayout.class);

    private final VBox vBox = new VBox();

    private boolean layoutCreated = false;

    private final ObservableList<PositionFXBean> positionObservableList = FXCollections.observableArrayList();

    private List<PositionField> positionList = new ArrayList<>();

    private final TableView<PositionFXBean> positionTableView = new TableView<>();

    private Map<String, PositionFXBean> positionFXBeanMap = new HashMap<>();

    private boolean showMergedFlag = false;

    private boolean showEmptyFlag = false;

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
        this.positionList = positionList;
        render();
    }

    public void render() {
        List<PositionField> filteredPositionList = new ArrayList<>();
        for (PositionField position : positionList) {
            if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.isSelectedAccountId(position.getAccountId())) {
                if (showEmptyFlag) {
                    filteredPositionList.add(position);
                } else if (position.getPosition() != 0) {
                    filteredPositionList.add(position);
                }
            }
        }
        List<PositionField> newPositionList;
        if (showMergedFlag) {
            newPositionList = new ArrayList<>();
            Map<String, PositionField.Builder> mergedPositionFieldBuilderMap = new HashMap<>();

            for (PositionField position : filteredPositionList) {

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

                        double openPriceDiff = (positionFieldBuilder.getOpenPriceDiff() * positionFieldBuilder.getPosition() + position.getOpenPriceDiff() * position.getPosition()) / positionInt;
                        positionFieldBuilder.setOpenPriceDiff(openPriceDiff);

                        double priceDiff = (positionFieldBuilder.getPriceDiff() * positionFieldBuilder.getPosition() + position.getPriceDiff() * position.getPosition()) / positionInt;
                        positionFieldBuilder.setPriceDiff(priceDiff);
                    }

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
                    positionFieldBuilder.setOpenPriceDiff(position.getOpenPriceDiff());
                    positionFieldBuilder.setPriceDiff(position.getPriceDiff());
                    positionFieldBuilder.setPositionId(key);

                    mergedPositionFieldBuilderMap.put(key, positionFieldBuilder);
                }

            }


            for (PositionField.Builder positionFieldBuilder : mergedPositionFieldBuilderMap.values()) {
                if (positionFieldBuilder.getUseMargin() != 0) {
                    positionFieldBuilder.setOpenPositionProfitRatio(positionFieldBuilder.getOpenPositionProfit() / positionFieldBuilder.getUseMargin());
                    positionFieldBuilder.setPositionProfitRatio(positionFieldBuilder.getPositionProfit() / positionFieldBuilder.getUseMargin());
                }
                newPositionList.add(positionFieldBuilder.build());
            }

        } else {
            newPositionList = filteredPositionList;
        }


        double accountBalance = 0;

        if (showMergedFlag) {
            Set<String> selectedAccountIdSet = guiMainService.getSelectedAccountIdSet();

            List<AccountField> accountList = desktopTradeCachesService.getAccountList();
            if (accountList != null) {
                for (AccountField account : accountList) {
                    if (selectedAccountIdSet.contains(account.getAccountId()) || selectedAccountIdSet.isEmpty()) {
                        accountBalance += account.getBalance();
                    }
                }
            }

        }

        Set<String> positionIdSet = new HashSet<>();

        List<PositionFXBean> newPositionFXBeanList = new ArrayList<>();
        for (PositionField position : newPositionList) {
            String positionId = position.getPositionId();

            if (!showMergedFlag) {
                AccountField account = desktopTradeCachesService.queryAccountByAccountId(position.getAccountId());
                if (account != null) {
                    accountBalance = account.getBalance();
                }
            }

            positionIdSet.add(positionId);

            if (positionFXBeanMap.containsKey(positionId)) {
                positionFXBeanMap.get(positionId).update(position, guiMainService.isSelectedContract(position.getContract()), accountBalance);
            } else {
                PositionFXBean positionFXBean = new PositionFXBean(position, guiMainService.isSelectedContract(position.getContract()), accountBalance);
                positionFXBeanMap.put(positionId, positionFXBean);
                newPositionFXBeanList.add(positionFXBean);
            }
        }

        positionObservableList.addAll(newPositionFXBeanList);

        Map<String, PositionFXBean> newPositionFXBeanMap = new HashMap<>();
        for (String positionId : positionFXBeanMap.keySet()) {
            if (positionIdSet.contains(positionId)) {
                newPositionFXBeanMap.put(positionId, positionFXBeanMap.get(positionId));
            }
        }
        positionFXBeanMap = newPositionFXBeanMap;

        positionObservableList.removeIf(positionFXBean -> !positionIdSet.contains(positionFXBean.getPositionId()));

        Set<String> newSelectedPositionIdSet = new HashSet<>();
        for (PositionFXBean positionFXBean : positionObservableList) {
            if (selectedPositionIdSet.contains(positionFXBean.getPositionId())) {
                positionTableView.getSelectionModel().select(positionFXBean);
                newSelectedPositionIdSet.add(positionFXBean.getPositionId());
            }
        }
        selectedPositionIdSet = newSelectedPositionIdSet;
    }

    private void createLayout() {

        positionTableView.setTableMenuButtonVisible(true);

        TableColumn<PositionFXBean, Pane> contractCol = new TableColumn<>("合约");
        contractCol.setPrefWidth(160);
        contractCol.setCellValueFactory(new PropertyValueFactory<>("contract"));
        contractCol.setComparator((Pane p1, Pane p2) -> {
            try {
                PositionField position1 = (PositionField) p1.getUserData();
                PositionField position2 = (PositionField) p2.getUserData();
                return StringUtils.compare(position1.getContract().getUnifiedSymbol(), position2.getContract().getUnifiedSymbol());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        positionTableView.getColumns().add(contractCol);

        TableColumn<PositionFXBean, Text> directionCol = new TableColumn<>("方向");
        directionCol.setPrefWidth(40);
        directionCol.setCellValueFactory(new PropertyValueFactory<>("direction"));
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

        TableColumn<PositionFXBean, String> hedgeFlagCol = new TableColumn<>("投机套保");
        hedgeFlagCol.setPrefWidth(60);
        hedgeFlagCol.setCellValueFactory(new PropertyValueFactory<>("hedgeFlag"));
        hedgeFlagCol.setComparator(StringUtils::compare);
        positionTableView.getColumns().add(hedgeFlagCol);

        TableColumn<PositionFXBean, Pane> positionCol = new TableColumn<>("持仓");
        positionCol.setPrefWidth(90);
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
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

        TableColumn<PositionFXBean, Pane> tdPositionCol = new TableColumn<>("今仓");
        tdPositionCol.setPrefWidth(90);
        tdPositionCol.setCellValueFactory(new PropertyValueFactory<>("todayPosition"));
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

        TableColumn<PositionFXBean, Pane> openPositionProfitCol = new TableColumn<>("逐笔浮盈");
        openPositionProfitCol.setPrefWidth(90);
        openPositionProfitCol.setCellValueFactory(new PropertyValueFactory<>("openProfit"));
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

        TableColumn<PositionFXBean, Pane> positionProfitCol = new TableColumn<>("盯市浮盈");
        positionProfitCol.setPrefWidth(90);
        positionProfitCol.setCellValueFactory(new PropertyValueFactory<>("positionProfit"));
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

        TableColumn<PositionFXBean, Pane> openPriceCol = new TableColumn<>("开仓价格");
        openPriceCol.setPrefWidth(90);
        openPriceCol.setCellValueFactory(new PropertyValueFactory<>("openPrice"));
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

        TableColumn<PositionFXBean, Pane> priceCol = new TableColumn<>("持仓价格");
        priceCol.setPrefWidth(90);
        priceCol.setCellValueFactory(new PropertyValueFactory<>("positionPrice"));
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

        TableColumn<PositionFXBean, Pane> marginCol = new TableColumn<>("保证金");
        marginCol.setPrefWidth(130);
        marginCol.setCellValueFactory(new PropertyValueFactory<>("margin"));
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

        TableColumn<PositionFXBean, String> marginRatioCol = new TableColumn<>("保证金占比");
        marginRatioCol.setPrefWidth(70);
        marginRatioCol.setCellValueFactory(new PropertyValueFactory<>("marginRatio"));
        marginRatioCol.setComparator((String s1, String s2) -> {
            try {
                return Double.valueOf(s1.replace(",", "").replace("%", "")).compareTo(Double.valueOf(s2.replace(",", "").replace("%", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        positionTableView.getColumns().add(marginRatioCol);

        TableColumn<PositionFXBean, String> contractValueCol = new TableColumn<>("合约价值");
        contractValueCol.setPrefWidth(90);
        contractValueCol.setCellValueFactory(new PropertyValueFactory<>("contractValue"));
        contractValueCol.setComparator((String s1, String s2) -> {
            try {
                return Double.valueOf(s1.replace(",", "")).compareTo(Double.valueOf(s2.replace(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        positionTableView.getColumns().add(contractValueCol);

        TableColumn<PositionFXBean, String> accountIdCol = new TableColumn<>("账户ID");
        accountIdCol.setPrefWidth(350);
        accountIdCol.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        accountIdCol.setComparator(StringUtils::compare);
        positionTableView.getColumns().add(accountIdCol);

        SortedList<PositionFXBean> sortedItems = new SortedList<>(positionObservableList);
        positionTableView.setItems(sortedItems);
        sortedItems.comparatorProperty().bind(positionTableView.comparatorProperty());

        positionTableView.setFocusTraversable(false);

        positionTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        positionTableView.setRowFactory(tv -> {
            TableRow<PositionFXBean> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    ObservableList<PositionFXBean> selectedItems = positionTableView.getSelectionModel().getSelectedItems();
                    selectedPositionIdSet.clear();
                    for (PositionFXBean positionFXBean : selectedItems) {
                        selectedPositionIdSet.add(positionFXBean.getPositionId());
                    }
                    PositionFXBean clickedItem = row.getItem();
                    guiMainService.updateSelectedContract(clickedItem.getPositionField().getContract());
                }
            });
            return row;
        });

        vBox.getChildren().add(positionTableView);
        VBox.setVgrow(positionTableView, Priority.ALWAYS);

        HBox hBox = new HBox();

        CheckBox showMergedCheckBox = new CheckBox("合并持仓");
        showMergedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showMergedFlag = newValue;
            render();
        });

        hBox.getChildren().add(showMergedCheckBox);

        CheckBox showEmptyCheckBox = new CheckBox("显示空仓");
        showEmptyCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showEmptyFlag = newValue;
            render();
        });

        hBox.getChildren().add(showEmptyCheckBox);

        vBox.getChildren().add(hBox);
    }

}
