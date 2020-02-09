package xyz.redtorch.desktop.layout.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.PositionField;

@Component
public class CombinationLayout {

	private HBox hBox = new HBox();

	private boolean layoutCreated = false;

	private Text allTodayProfitText = new Text("N/A");
	private Text allBalanceText = new Text("N/A");
	private Text allOpenPositionProfitText = new Text("N/A");
	private Text allPositionProfitText = new Text("N/A");
	private Text allMarginText = new Text("N/A");
	private Text allCommissionText = new Text("N/A");
	private Text allCloseProfitText = new Text("N/A");
	private Text allContractValueText = new Text("N/A");
	private Text allDepositAndWithdrawText = new Text("N/A");

	private List<PositionField> positionList = new ArrayList<>();
	private List<AccountField> accountList = new ArrayList<>();

	private double labelWidth = 95;

	@Autowired
	private GuiMainService guiMainService;

	public void updateData(List<PositionField> positionList, List<AccountField> accountList) {
		if (!new HashSet<>(this.positionList).equals(new HashSet<>(positionList)) || !new HashSet<>(this.accountList).equals(new HashSet<>(accountList))) {
			this.positionList = positionList;
			this.accountList = accountList;
			fillingData();
		}
	}

	public void fillingData() {

		Double allTodayProfit = null;
		Double allTodayProfitRatio = null;
		Double allBalance = null;
		Double allOpenPositionProfit = null; // TO
		Double allPositionProfit = null;
		Double allMargin = null;
		Double allMarginRatio = null;
		Double allCommission = null;
		Double allCloseProfit = null;
		Double allContractValue = null; // TO
		Double allDepositAndWithdraw = null;

		double allPreBalance = 0;

		if (accountList != null) {
			for (AccountField account : accountList) {
				if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.getSelectedAccountIdSet().contains(account.getAccountId())) {
					allTodayProfit = allTodayProfit == null ? 0 : allTodayProfit;
					allBalance = allBalance == null ? 0 : allBalance;
					allPositionProfit = allPositionProfit == null ? 0 : allPositionProfit;
					allMargin = allMargin == null ? 0 : allMargin;
					allCommission = allCommission == null ? 0 : allCommission;
					allCloseProfit = allCloseProfit == null ? 0 : allCloseProfit;
					allDepositAndWithdraw = allDepositAndWithdraw == null ? 0 : allDepositAndWithdraw;

					double preBalance = account.getPreBalance();
					double tdProfit = account.getBalance() - preBalance - account.getDeposit() + account.getWithdraw();

					allTodayProfit += tdProfit;
					allBalance += account.getBalance();
					if (preBalance == 0) {
						preBalance = account.getBalance();
					}
					allPreBalance += preBalance;
					allPositionProfit += account.getPositionProfit();
					allMargin += account.getMargin();
					allCommission += account.getCommission();
					allCloseProfit += account.getCloseProfit();
					allDepositAndWithdraw += (account.getDeposit() - account.getWithdraw());

				}
			}
		}

		if (allPreBalance != 0) {
			allTodayProfitRatio = allTodayProfit / allPreBalance;
		}
		if (allMargin != null && allBalance != null && allBalance != 0) {
			allMarginRatio = allMargin / allBalance;
		}

		if (positionList != null) {

			for (PositionField position : positionList) {
				if (guiMainService.getSelectedAccountIdSet().isEmpty() || guiMainService.getSelectedAccountIdSet().contains(position.getAccountId())) {
					allOpenPositionProfit = (allOpenPositionProfit == null ? 0 : allOpenPositionProfit);
					allContractValue = (allContractValue == null ? 0 : allContractValue);
					allOpenPositionProfit += (Double.isNaN(position.getOpenPositionProfit()) ? 0d : position.getOpenPositionProfit());
					allContractValue += (Double.isNaN(position.getContractValue()) ? 0d : position.getContractValue());
				}
			}

		}

		allTodayProfitText.getStyleClass().clear();
		if (allTodayProfit != null) {
			allTodayProfitText.setText(String.format("%,.2f(%,.2f%%)", allTodayProfit, allTodayProfitRatio * 100));
			if (allTodayProfit > 0) {
				allTodayProfitText.getStyleClass().add("trade-long-color");
			} else if (allTodayProfit < 0) {
				allTodayProfitText.getStyleClass().add("trade-short-color");
			}
		} else {
			allTodayProfitText.setText("N/A");
		}

		if (allBalance != null) {
			allBalanceText.setText(String.format("%,.2f", allBalance));
		} else {
			allBalanceText.setText("N/A");
		}

		allOpenPositionProfitText.getStyleClass().clear();
		if (allOpenPositionProfit != null) {
			allOpenPositionProfitText.setText(String.format("%,.2f", allOpenPositionProfit));
			if (allOpenPositionProfit > 0) {
				allOpenPositionProfitText.getStyleClass().add("trade-long-color");
			} else if (allOpenPositionProfit < 0) {
				allOpenPositionProfitText.getStyleClass().add("trade-short-color");
			}
		} else {
			allOpenPositionProfitText.setText("N/A");
		}

		allPositionProfitText.getStyleClass().clear();
		if (allPositionProfit != null) {
			allPositionProfitText.setText(String.format("%,.2f", allPositionProfit));
			if (allPositionProfit > 0) {
				allPositionProfitText.getStyleClass().add("trade-long-color");
			} else if (allPositionProfit < 0) {
				allPositionProfitText.getStyleClass().add("trade-short-color");
			}
		} else {
			allPositionProfitText.setText("N/A");
		}

		if (allMargin != null) {
			allMarginText.setText(String.format("%,.2f(%,.2f%%)", allMargin, allMarginRatio * 100));
		} else {
			allMarginText.setText("N/A");
		}

		if (allCommission != null) {
			allCommissionText.setText(String.format("%,.2f", allCommission));
		} else {
			allCommissionText.setText("N/A");
		}

		allCloseProfitText.getStyleClass().clear();
		if (allCloseProfit != null) {
			allCloseProfitText.setText(String.format("%,.2f", allCloseProfit));
			if (allCloseProfit > 0) {
				allCloseProfitText.getStyleClass().add("trade-long-color");
			} else if (allCloseProfit < 0) {
				allCloseProfitText.getStyleClass().add("trade-short-color");
			}
		} else {
			allCloseProfitText.setText("N/A");
		}

		if (allContractValue != null) {
			allContractValueText.setText(String.format("%,.2f", allContractValue));
		} else {
			allContractValueText.setText("N/A");
		}

		allDepositAndWithdrawText.getStyleClass().clear();
		if (allDepositAndWithdraw != null) {
			allDepositAndWithdrawText.setText(String.format("%,.2f", allDepositAndWithdraw));
			if (allDepositAndWithdraw > 0) {
				allDepositAndWithdrawText.getStyleClass().add("trade-long-color");
			} else if (allDepositAndWithdraw < 0) {
				allDepositAndWithdrawText.getStyleClass().add("trade-short-color");
			}
		} else {
			allDepositAndWithdrawText.setText("N/A");
		}

	}

	public Node getNode() {
		if (!layoutCreated) {
			createLayout();
			layoutCreated = true;
		}
		return this.hBox;
	}

	private void createLayout() {
		hBox.getChildren().clear();

		VBox leftVbox = new VBox();

		Label allTodayProfitLabel = new Label("今日盈亏（率）：");
		allTodayProfitLabel.setMinWidth(labelWidth);
		allTodayProfitLabel.setAlignment(Pos.CENTER_RIGHT);
		allTodayProfitLabel.getStyleClass().add("trade-label");

		HBox allTodayProfitHBox = new HBox();
		allTodayProfitHBox.getChildren().add(allTodayProfitLabel);
		allTodayProfitHBox.getChildren().add(allTodayProfitText);
		leftVbox.getChildren().add(allTodayProfitHBox);

		Label allBalanceLabel = new Label("资金：");
		allBalanceLabel.setMinWidth(labelWidth);
		allBalanceLabel.setAlignment(Pos.CENTER_RIGHT);
		allBalanceLabel.getStyleClass().add("trade-label");
		HBox allBalanceHBox = new HBox();
		allBalanceHBox.getChildren().add(allBalanceLabel);
		allBalanceHBox.getChildren().add(allBalanceText);
		leftVbox.getChildren().add(allBalanceHBox);

		Label allOpenPositionProfitLabel = new Label("持仓盈亏：");
		allOpenPositionProfitLabel.setMinWidth(labelWidth);
		allOpenPositionProfitLabel.setAlignment(Pos.CENTER_RIGHT);
		allOpenPositionProfitLabel.getStyleClass().add("trade-label");
		HBox allOpenPositionProfitHBox = new HBox();
		allOpenPositionProfitHBox.getChildren().add(allOpenPositionProfitLabel);
		allOpenPositionProfitHBox.getChildren().add(allOpenPositionProfitText);
		leftVbox.getChildren().add(allOpenPositionProfitHBox);

		hBox.getChildren().add(leftVbox);
		HBox.setHgrow(leftVbox, Priority.ALWAYS);

		VBox centerVbox = new VBox();

		Label allPositionProfitLabel = new Label("盯市持仓盈亏：");
		allPositionProfitLabel.setMinWidth(labelWidth);
		allPositionProfitLabel.setAlignment(Pos.CENTER_RIGHT);
		allPositionProfitLabel.getStyleClass().add("trade-label");
		HBox allPositionProfitHBox = new HBox();
		allPositionProfitHBox.getChildren().add(allPositionProfitLabel);
		allPositionProfitHBox.getChildren().add(allPositionProfitText);
		centerVbox.getChildren().add(allPositionProfitHBox);

		Label allMarginLabel = new Label("保证金（率）：");
		allMarginLabel.setMinWidth(labelWidth);
		allMarginLabel.setAlignment(Pos.CENTER_RIGHT);
		allMarginLabel.getStyleClass().add("trade-label");
		HBox allMarginHBox = new HBox();
		allMarginHBox.getChildren().add(allMarginLabel);
		allMarginHBox.getChildren().add(allMarginText);
		centerVbox.getChildren().add(allMarginHBox);

		Label allCommissionLabel = new Label("佣金：");
		allCommissionLabel.setMinWidth(labelWidth);
		allCommissionLabel.setAlignment(Pos.CENTER_RIGHT);
		allCommissionLabel.getStyleClass().add("trade-label");
		HBox allCommissionHBox = new HBox();
		allCommissionHBox.getChildren().add(allCommissionLabel);
		allCommissionHBox.getChildren().add(allCommissionText);
		centerVbox.getChildren().add(allCommissionHBox);

		hBox.getChildren().add(centerVbox);
		HBox.setHgrow(centerVbox, Priority.ALWAYS);

		VBox rightVbox = new VBox();

		Label allCloseProfitLabel = new Label("盯市平仓盈亏：");
		allCloseProfitLabel.setMinWidth(labelWidth);
		allCloseProfitLabel.setAlignment(Pos.CENTER_RIGHT);
		allCloseProfitLabel.getStyleClass().add("trade-label");
		HBox allCloseProfitHBox = new HBox();
		allCloseProfitHBox.getChildren().add(allCloseProfitLabel);
		allCloseProfitHBox.getChildren().add(allCloseProfitText);
		rightVbox.getChildren().add(allCloseProfitHBox);

		Label allContractValueLabel = new Label("合约价值：");
		allContractValueLabel.setMinWidth(labelWidth);
		allContractValueLabel.setAlignment(Pos.CENTER_RIGHT);
		allContractValueLabel.getStyleClass().add("trade-label");
		HBox allContractValueHBox = new HBox();
		allContractValueHBox.getChildren().add(allContractValueLabel);
		allContractValueHBox.getChildren().add(allContractValueText);
		rightVbox.getChildren().add(allContractValueHBox);

		Label allDepositAndWithdrawLabel = new Label("出入金：");
		allDepositAndWithdrawLabel.setMinWidth(labelWidth);
		allDepositAndWithdrawLabel.setAlignment(Pos.CENTER_RIGHT);
		allDepositAndWithdrawLabel.getStyleClass().add("trade-label");
		HBox allDepositAndWithdrawHBox = new HBox();
		allDepositAndWithdrawHBox.getChildren().add(allDepositAndWithdrawLabel);
		allDepositAndWithdrawHBox.getChildren().add(allDepositAndWithdrawText);
		rightVbox.getChildren().add(allDepositAndWithdrawHBox);

		hBox.getChildren().add(rightVbox);
		HBox.setHgrow(rightVbox, Priority.ALWAYS);

	}

}
