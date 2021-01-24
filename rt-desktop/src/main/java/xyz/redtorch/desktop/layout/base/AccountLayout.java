package xyz.redtorch.desktop.layout.base;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.redtorch.desktop.layout.base.bean.AccountFXBean;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.AccountField;

import java.util.*;

@Component
public class AccountLayout {

    private static final Logger logger = LoggerFactory.getLogger(AccountLayout.class);

    private final VBox vBox = new VBox();

    private boolean layoutCreated = false;

    private final ObservableList<AccountFXBean> accountObservableList = FXCollections.observableArrayList();

    private List<AccountField> accountList = new ArrayList<>();

    private final TableView<AccountFXBean> accountTableView = new TableView<>();

    private Map<String, AccountFXBean> accountFXBeanMap = new HashMap<>();

    private final Set<String> selectedAccountIdSet = new HashSet<>();

    private final CheckBox selectAllCheckBox = new CheckBox();

    @Autowired
    private GuiMainService guiMainService;

    public Node getNode() {
        if (!layoutCreated) {
            createLayout();
            layoutCreated = true;
        }
        return this.vBox;
    }

    public void updateData(List<AccountField> accountList) {
        this.accountList = accountList;
        render();
    }

    public void render() {
        accountTableView.getSelectionModel().clearSelection();

        Set<String> accountIdSet = new HashSet<>();

        List<AccountFXBean> newAccountFXBeanList = new ArrayList<>();
        for (AccountField account : accountList) {
            String accountId = account.getAccountId();
            accountIdSet.add(accountId);

            if (accountFXBeanMap.containsKey(accountId)) {
                accountFXBeanMap.get(accountId).update(account, guiMainService.isSelectedAccountId(accountId));
            } else {
                AccountFXBean accountFXBean = new AccountFXBean(account, guiMainService.isSelectedAccountId(accountId));
                accountFXBeanMap.put(accountId, accountFXBean);
                newAccountFXBeanList.add(accountFXBean);
            }
        }

        accountObservableList.addAll(newAccountFXBeanList);

        Map<String, AccountFXBean> newAccountFXBeanMap = new HashMap<>();
        for (String accountId : accountFXBeanMap.keySet()) {
            if (accountIdSet.contains(accountId)) {
                newAccountFXBeanMap.put(accountId, accountFXBeanMap.get(accountId));
            }
        }
        accountFXBeanMap = newAccountFXBeanMap;

        accountObservableList.removeIf(accountFXBean -> !accountIdSet.contains(accountFXBean.getAccountId()));

        accountTableView.sort();

        Set<String> newSelectedAccountIdSet = new HashSet<>();
        for (AccountFXBean account : accountObservableList) {
            if (selectedAccountIdSet.contains(account.getAccountId())) {
                accountTableView.getSelectionModel().select(account);
                newSelectedAccountIdSet.add(account.getAccountId());
            }
        }
        selectedAccountIdSet.clear();
        selectedAccountIdSet.addAll(newSelectedAccountIdSet);
        guiMainService.updateSelectedAccountIdSet(selectedAccountIdSet);
    }

    public void updateSelectedAccountIdSet(Set<String> selectedAccountIdSet) {
        this.selectedAccountIdSet.clear();
        this.selectedAccountIdSet.addAll(selectedAccountIdSet);
        this.render();
    }

    private void createLayout() {

        accountTableView.setTableMenuButtonVisible(true);

        TableColumn<AccountFXBean, Boolean> checkCol = new TableColumn<>("选择");
        checkCol.setCellValueFactory(new PropertyValueFactory<>("selected"));
        checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
        checkCol.setEditable(false);
        checkCol.setMaxWidth(50);
        checkCol.setMinWidth(50);

        selectAllCheckBox.setOnMousePressed(event -> {
            accountTableView.getSelectionModel().clearSelection();
            if (selectAllCheckBox.isSelected()) {
                selectedAccountIdSet.clear();
            } else {
                Set<String> newSelectedAccountIdSet = new HashSet<>();
                for (AccountFXBean accountFXBean : accountObservableList) {
                    accountTableView.getSelectionModel().select(accountFXBean);
                    newSelectedAccountIdSet.add(accountFXBean.getAccountId());
                }
                selectedAccountIdSet.addAll(newSelectedAccountIdSet);
            }
            render();
        });
        checkCol.setText("");
        checkCol.setGraphic(selectAllCheckBox);
        checkCol.setSortable(false);
        accountTableView.getColumns().add(checkCol);

        TableColumn<AccountFXBean, String> accountCodeCol = new TableColumn<>("账户代码");
        accountCodeCol.setPrefWidth(80);
        accountCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        accountCodeCol.setComparator(StringUtils::compare);
        accountTableView.getColumns().add(accountCodeCol);

        TableColumn<AccountFXBean, String> holderCol = new TableColumn<>("持有人");
        holderCol.setPrefWidth(60);
        holderCol.setCellValueFactory(new PropertyValueFactory<>("holder"));
        holderCol.setComparator(StringUtils::compare);
        accountTableView.getColumns().add(holderCol);

        TableColumn<AccountFXBean, String> currencyCol = new TableColumn<>("币种");
        currencyCol.setPrefWidth(40);
        currencyCol.setCellValueFactory(new PropertyValueFactory<>("currency"));
        currencyCol.setComparator(StringUtils::compare);
        accountTableView.getColumns().add(currencyCol);

        TableColumn<AccountFXBean, String> balanceCol = new TableColumn<>("权益");
        balanceCol.setPrefWidth(90);
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balanceCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(balanceCol);

        TableColumn<AccountFXBean, String> marginRatioCol = new TableColumn<>("使用率");
        marginRatioCol.setPrefWidth(90);
        marginRatioCol.setCellValueFactory(new PropertyValueFactory<>("marginRatio"));
        marginRatioCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "").replaceAll("%", "")), Double.parseDouble(s2.replaceAll(",", "").replaceAll("%", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(marginRatioCol);

        TableColumn<AccountFXBean, String> avliableCol = new TableColumn<>("可用");
        avliableCol.setPrefWidth(90);
        avliableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        avliableCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(avliableCol);

        TableColumn<AccountFXBean, Text> todayProfitCol = new TableColumn<>("今日盈亏");
        todayProfitCol.setPrefWidth(90);
        todayProfitCol.setCellValueFactory(new PropertyValueFactory<>("todayProfit"));

        todayProfitCol.setComparator((Text t1, Text t2) -> {
            try {
                Double tdProfit1 = (Double) t1.getUserData();
                Double tdProfit2 = (Double) t2.getUserData();
                return Double.compare(tdProfit1, tdProfit2);
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(todayProfitCol);

        TableColumn<AccountFXBean, Text> todayProfitRatioCol = new TableColumn<>("今日盈亏率");
        todayProfitRatioCol.setPrefWidth(75);
        todayProfitRatioCol.setCellValueFactory(new PropertyValueFactory<>("todayProfitRatio"));
        todayProfitRatioCol.setComparator((Text t1, Text t2) -> {
            try {
                String s1 = t1.getText();
                String s2 = t2.getText();
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "").replaceAll("%", "")), Double.parseDouble(s2.replaceAll(",", "").replaceAll("%", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(todayProfitRatioCol);

        TableColumn<AccountFXBean, Text> closeProfitCol = new TableColumn<>("平仓盈亏");
        closeProfitCol.setPrefWidth(90);
        closeProfitCol.setCellValueFactory(new PropertyValueFactory<>("closeProfit"));
        closeProfitCol.setComparator((Text t1, Text t2) -> {
            try {
                AccountField account1 = (AccountField) t1.getUserData();
                AccountField account2 = (AccountField) t2.getUserData();
                return Double.compare(account1.getCloseProfit(), account2.getCloseProfit());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(closeProfitCol);

        TableColumn<AccountFXBean, Text> positionProfitCol = new TableColumn<>("持仓盈亏");
        positionProfitCol.setPrefWidth(90);
        positionProfitCol.setCellValueFactory(new PropertyValueFactory<>("positionProfit"));
        positionProfitCol.setComparator((Text t1, Text t2) -> {
            try {
                AccountField account1 = (AccountField) t1.getUserData();
                AccountField account2 = (AccountField) t2.getUserData();
                return Double.compare(account1.getPositionProfit(), account2.getPositionProfit());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(positionProfitCol);

        TableColumn<AccountFXBean, String> marginCol = new TableColumn<>("保证金");
        marginCol.setPrefWidth(90);
        marginCol.setCellValueFactory(new PropertyValueFactory<>("margin"));
        marginCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(marginCol);

        TableColumn<AccountFXBean, String> preBalanceCol = new TableColumn<>("昨日权益");
        preBalanceCol.setPrefWidth(90);
        preBalanceCol.setCellValueFactory(new PropertyValueFactory<>("preBalance"));
        preBalanceCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(preBalanceCol);

        TableColumn<AccountFXBean, String> commissionCol = new TableColumn<>("佣金");
        commissionCol.setPrefWidth(90);
        commissionCol.setCellValueFactory(new PropertyValueFactory<>("commission"));
        commissionCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(commissionCol);

        TableColumn<AccountFXBean, String> depositCol = new TableColumn<>("入金");
        depositCol.setPrefWidth(90);
        depositCol.setCellValueFactory(new PropertyValueFactory<>("deposit"));
        depositCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(depositCol);

        TableColumn<AccountFXBean, String> withdrawCol = new TableColumn<>("出金");
        withdrawCol.setPrefWidth(90);
        withdrawCol.setCellValueFactory(new PropertyValueFactory<>("withdraw"));
        withdrawCol.setComparator((String s1, String s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1.replaceAll(",", "")), Double.parseDouble(s2.replaceAll(",", "")));
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });
        accountTableView.getColumns().add(withdrawCol);

        TableColumn<AccountFXBean, String> accountIdCol = new TableColumn<>("账户ID");
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
        accountIdCol.setComparator(StringUtils::compare);
        accountTableView.getColumns().add(accountIdCol);

        accountTableView.setRowFactory(tv -> {
            TableRow<AccountFXBean> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    AccountFXBean clickedItem = row.getItem();

                    ObservableList<AccountFXBean> selectedItems = accountTableView.getSelectionModel().getSelectedItems();

                    boolean selectedItemReverse = false;
                    if (selectedAccountIdSet.contains(clickedItem.getAccountId())) {
                        selectedItemReverse = true;
                    }

                    selectedAccountIdSet.clear();
                    for (AccountFXBean selectedRow : selectedItems) {
                        if (selectedRow.getAccountId().equals(clickedItem.getAccountId())) {
                            if (!selectedItemReverse) {
                                selectedAccountIdSet.add(selectedRow.getAccountId());
                            }
                        } else {
                            selectedAccountIdSet.add(selectedRow.getAccountId());
                        }
                    }

                    if (selectedAccountIdSet.size() == 0) {
                        selectAllCheckBox.setSelected(false);
                        selectAllCheckBox.setIndeterminate(false);
                    } else if (selectedAccountIdSet.size() < accountObservableList.size()) {
                        selectAllCheckBox.setSelected(false);
                        selectAllCheckBox.setIndeterminate(true);
                    } else if (selectedAccountIdSet.size() == accountObservableList.size()) {
                        selectAllCheckBox.setSelected(true);
                        selectAllCheckBox.setIndeterminate(false);
                    }

                    render();
                }
            });
            return row;
        });

        SortedList<AccountFXBean> sortedItems = new SortedList<>(accountObservableList);
        accountTableView.setItems(sortedItems);
        sortedItems.comparatorProperty().bind(accountTableView.comparatorProperty());
        accountTableView.getSortOrder().add(accountCodeCol);

        accountTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        accountTableView.setFocusTraversable(false);

        vBox.getChildren().add(accountTableView);
        VBox.setVgrow(accountTableView, Priority.ALWAYS);

//        HBox hBox = new HBox();
//        hBox.getChildren().add(new CheckBox("合并显示"));
//        vBox.getChildren().add(hBox);
    }

}
