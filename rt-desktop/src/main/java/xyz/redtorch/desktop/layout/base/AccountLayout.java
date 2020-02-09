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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.AccountField;

@Component
public class AccountLayout {

	private static final Logger logger = LoggerFactory.getLogger(AccountLayout.class);

	private VBox vBox = new VBox();

	private boolean layoutCreated = false;

	private ObservableList<AccountField> accountObservableList = FXCollections.observableArrayList();

	private List<AccountField> accountList = new ArrayList<>();

	private TableView<AccountField> accountTableView = new TableView<>();

	private Set<String> selectedAccountIdSet = new HashSet<>();

	private CheckBox selectAllCheckBox = new CheckBox();

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
		if (!new HashSet<>(this.accountList).equals(new HashSet<>(accountList))) {
			this.accountList = accountList;
			fillingData();
		}
	}

	public void fillingData() {
		accountTableView.getSelectionModel().clearSelection();

		accountObservableList.clear();
		accountObservableList.addAll(accountList);
		accountTableView.sort();

		Set<String> newSelectedAccountIdSet = new HashSet<>();
		for (AccountField accountField : accountObservableList) {
			if (selectedAccountIdSet.contains(accountField.getAccountId())) {
				accountTableView.getSelectionModel().select(accountField);
				newSelectedAccountIdSet.add(accountField.getAccountId());
			}
		}
		selectedAccountIdSet.clear();
		selectedAccountIdSet.addAll(newSelectedAccountIdSet);
		guiMainService.updateSelectedAccountIdSet(selectedAccountIdSet);
	}

	public void updateSelectedAccountIdSet(Set<String> selectedAccountIdSet) {
		this.selectedAccountIdSet.clear();
		this.selectedAccountIdSet.addAll(selectedAccountIdSet);
		this.fillingData();
	}

	private void createLayout() {

		accountTableView.setTableMenuButtonVisible(true);

		TableColumn<AccountField, Boolean> checkCol = new TableColumn<>("选择");
		checkCol.setCellValueFactory(feature -> new SimpleBooleanProperty(selectedAccountIdSet.contains(feature.getValue().getAccountId())));
		checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
		checkCol.setEditable(true);
		checkCol.setMaxWidth(50);
		checkCol.setMinWidth(50);

		selectAllCheckBox.setOnMousePressed(event -> {
			accountTableView.getSelectionModel().clearSelection();
			if (selectAllCheckBox.isSelected()) {
				selectedAccountIdSet.clear();
			} else {
				Set<String> newSelectedAccountIdSet = new HashSet<>();
				for (AccountField accountField : accountObservableList) {
					accountTableView.getSelectionModel().select(accountField);
					newSelectedAccountIdSet.add(accountField.getAccountId());
				}
				selectedAccountIdSet.addAll(newSelectedAccountIdSet);
			}
			fillingData();
		});
		checkCol.setText("");
		checkCol.setGraphic(selectAllCheckBox);

		checkCol.setSortable(false);

		accountTableView.getColumns().add(checkCol);

		TableColumn<AccountField, String> accountCodeCol = new TableColumn<>("账户代码");
		accountCodeCol.setPrefWidth(80);
		accountCodeCol.setCellValueFactory(feature -> {
			String code = "";
			try {
				code = feature.getValue().getCode();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(code);
		});
		accountCodeCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		accountTableView.getColumns().add(accountCodeCol);

		TableColumn<AccountField, String> holderCol = new TableColumn<>("持有人");
		holderCol.setPrefWidth(60);
		holderCol.setCellValueFactory(feature -> {
			String holder = "";
			try {
				holder = feature.getValue().getHolder();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(holder);
		});
		holderCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		accountTableView.getColumns().add(holderCol);

		TableColumn<AccountField, String> currencyCol = new TableColumn<>("币种");
		currencyCol.setPrefWidth(40);
		currencyCol.setCellValueFactory(feature -> {
			String currency = "";
			try {
				currency = feature.getValue().getCurrency().getValueDescriptor().getName();
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(currency);
		});
		currencyCol.setComparator((String s1, String s2) -> StringUtils.compare(s1, s2));
		accountTableView.getColumns().add(currencyCol);

		TableColumn<AccountField, String> balanceCol = new TableColumn<>("权益");
		balanceCol.setPrefWidth(90);
		balanceCol.setCellValueFactory(feature -> {
			String balanceString = "";
			try {
				balanceString = String.format("%,.2f", feature.getValue().getBalance());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(balanceString);
		});
		balanceCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(balanceCol);

		TableColumn<AccountField, String> avliableCol = new TableColumn<>("可用");
		avliableCol.setPrefWidth(90);
		avliableCol.setCellValueFactory(feature -> {
			String availableString = "";
			try {
				availableString = String.format("%,.2f", feature.getValue().getAvailable());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(availableString);
		});
		avliableCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(avliableCol);

		TableColumn<AccountField, String> utilizationRateCol = new TableColumn<>("使用率");
		utilizationRateCol.setPrefWidth(90);
		utilizationRateCol.setCellValueFactory(feature -> {
			String utilizationString = "";
			try {
				utilizationString = String.format("%,.2f%%", 100 - feature.getValue().getAvailable() / feature.getValue().getBalance() * 100);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(utilizationString);
		});
		utilizationRateCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "").replaceAll("%", "")), Double.valueOf(s2.replaceAll(",", "").replaceAll("%", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(utilizationRateCol);

		TableColumn<AccountField, Text> tdProfitCol = new TableColumn<>("今日盈亏");
		tdProfitCol.setPrefWidth(90);
		tdProfitCol.setCellValueFactory(feature -> {

			Text tdProfitText = new Text();
			try {
				AccountField account = feature.getValue();
				double preBalance = account.getPreBalance();

				Double tdProfit = account.getBalance() - preBalance - account.getDeposit() + account.getWithdraw();

				tdProfitText.setText(String.format("%,.2f", tdProfit));
				if (tdProfit > 0) {
					tdProfitText.getStyleClass().add("trade-long-color");
				} else if (tdProfit < 0) {
					tdProfitText.getStyleClass().add("trade-short-color");
				}
				tdProfitText.setUserData(tdProfit);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(tdProfitText);
		});

		tdProfitCol.setComparator((Text t1, Text t2) -> {
			try {
				Double tdProfit1 = (Double) t1.getUserData();
				Double tdProfit2 = (Double) t2.getUserData();
				return Double.compare(tdProfit1, tdProfit2);
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(tdProfitCol);

		TableColumn<AccountField, Text> tdProfitRatioCol = new TableColumn<>("今日盈亏率");
		tdProfitRatioCol.setPrefWidth(75);
		tdProfitRatioCol.setCellValueFactory(feature -> {

			Text tdProfitRatioText = new Text();
			try {
				AccountField account = feature.getValue();
				double preBalance = account.getPreBalance();

				Double tdProfit = account.getBalance() - preBalance - account.getDeposit() + account.getWithdraw();
				if (preBalance == 0) {
					preBalance = account.getBalance();
				}
				tdProfitRatioText.setText(String.format("%,.2f%%", tdProfit / preBalance * 100));
				if (tdProfit > 0) {
					tdProfitRatioText.getStyleClass().add("trade-long-color");
				} else if (tdProfit < 0) {
					tdProfitRatioText.getStyleClass().add("trade-short-color");
				}
				tdProfitRatioText.setUserData(tdProfit);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(tdProfitRatioText);

		});

		tdProfitRatioCol.setComparator((Text t1, Text t2) -> {
			try {
				Double tdProfit1 = (Double) t1.getUserData();
				Double tdProfit2 = (Double) t2.getUserData();
				return Double.compare(tdProfit1, tdProfit2);
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});

		accountTableView.getColumns().add(tdProfitRatioCol);

		TableColumn<AccountField, Text> closeProfitCol = new TableColumn<>("平仓盈亏");
		closeProfitCol.setPrefWidth(90);
		closeProfitCol.setCellValueFactory(feature -> {
			Text closeProfitText = new Text();

			try {
				AccountField account = feature.getValue();
				closeProfitText.setText(String.format("%,.2f", feature.getValue().getCloseProfit()));
				if (account.getCloseProfit() > 0) {
					closeProfitText.getStyleClass().add("trade-long-color");
				} else if (account.getCloseProfit() < 0) {
					closeProfitText.getStyleClass().add("trade-short-color");
				}
				closeProfitText.setUserData(account);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(closeProfitText);

		});

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

		TableColumn<AccountField, Text> positionProfitCol = new TableColumn<>("持仓盈亏");
		positionProfitCol.setPrefWidth(90);
		positionProfitCol.setCellValueFactory(feature -> {
			Text positionProfitText = new Text();

			try {
				AccountField account = feature.getValue();
				positionProfitText.setText(String.format("%,.2f", feature.getValue().getPositionProfit()));
				if (account.getPositionProfit() > 0) {
					positionProfitText.getStyleClass().add("trade-long-color");
				} else if (account.getPositionProfit() < 0) {
					positionProfitText.getStyleClass().add("trade-short-color");
				}
				positionProfitText.setUserData(account);
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}

			return new SimpleObjectProperty<>(positionProfitText);

		});

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

		TableColumn<AccountField, String> marginCol = new TableColumn<>("保证金");
		marginCol.setPrefWidth(90);
		marginCol.setCellValueFactory(feature -> {
			String marginString = "";
			try {
				marginString = String.format("%,.2f", feature.getValue().getMargin());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(marginString);
		});
		marginCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(marginCol);

		TableColumn<AccountField, String> preBalanceCol = new TableColumn<>("昨日权益");
		preBalanceCol.setPrefWidth(90);
		preBalanceCol.setCellValueFactory(feature -> {
			String preBalanceString = "";
			try {
				preBalanceString = String.format("%,.2f", feature.getValue().getPreBalance());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(preBalanceString);
		});
		preBalanceCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(preBalanceCol);

		TableColumn<AccountField, String> commissionCol = new TableColumn<>("佣金");
		commissionCol.setPrefWidth(90);
		commissionCol.setCellValueFactory(feature -> {
			String commissionString = "";
			try {
				commissionString = String.format("%,.2f", feature.getValue().getCommission());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(commissionString);
		});
		commissionCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(commissionCol);

		TableColumn<AccountField, String> depositCol = new TableColumn<>("入金");
		depositCol.setPrefWidth(90);
		depositCol.setCellValueFactory(feature -> {
			String depositString = "";
			try {
				depositString = String.format("%,.2f", feature.getValue().getDeposit());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(depositString);
		});
		depositCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(depositCol);

		TableColumn<AccountField, String> withdrawCol = new TableColumn<>("出金");
		withdrawCol.setPrefWidth(90);
		withdrawCol.setCellValueFactory(feature -> {
			String withdrawString = "";
			try {
				withdrawString = String.format("%,.2f", feature.getValue().getWithdraw());
			} catch (Exception e) {
				logger.error("渲染错误", e);
			}
			return new SimpleStringProperty(withdrawString);
		});
		withdrawCol.setComparator((String s1, String s2) -> {
			try {
				return Double.compare(Double.valueOf(s1.replaceAll(",", "")), Double.valueOf(s2.replaceAll(",", "")));
			} catch (Exception e) {
				logger.error("排序错误", e);
			}
			return 0;
		});
		accountTableView.getColumns().add(withdrawCol);

		TableColumn<AccountField, String> accountIdCol = new TableColumn<>("账户ID");
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
		accountTableView.getColumns().add(accountIdCol);

		accountTableView.setRowFactory(tv -> {
			TableRow<AccountField> row = new TableRow<>();
			row.setOnMousePressed(event -> {
				if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
					AccountField clickedItem = row.getItem();

					ObservableList<AccountField> selectedItems = accountTableView.getSelectionModel().getSelectedItems();

					boolean selectedItemReverse = false;
					if (selectedAccountIdSet.contains(clickedItem.getAccountId())) {
						selectedItemReverse = true;
					}

					selectedAccountIdSet.clear();
					for (AccountField selectedRow : selectedItems) {
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

					fillingData();
				}
			});
			return row;
		});

		SortedList<AccountField> sortedItems = new SortedList<>(accountObservableList);
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
