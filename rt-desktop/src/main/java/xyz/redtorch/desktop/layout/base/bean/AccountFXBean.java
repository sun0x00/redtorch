package xyz.redtorch.desktop.layout.base.bean;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreField.AccountField;

public class AccountFXBean {

    private final static Logger logger = LoggerFactory.getLogger(AccountFXBean.class);

    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    private final SimpleStringProperty accountId = new SimpleStringProperty();
    private final SimpleStringProperty code = new SimpleStringProperty();
    private final SimpleStringProperty holder = new SimpleStringProperty();
    private final SimpleStringProperty currency = new SimpleStringProperty();
    private final SimpleStringProperty balance = new SimpleStringProperty();
    private final SimpleStringProperty marginRatio = new SimpleStringProperty();
    private final SimpleStringProperty available = new SimpleStringProperty();
    private final SimpleObjectProperty<Text> todayProfit = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Text> todayProfitRatio = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Text> closeProfit = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Text> positionProfit = new SimpleObjectProperty<>();
    private final SimpleStringProperty commission = new SimpleStringProperty();
    private final SimpleStringProperty margin = new SimpleStringProperty();
    private final SimpleStringProperty preBalance = new SimpleStringProperty();
    private final SimpleStringProperty deposit = new SimpleStringProperty();
    private final SimpleStringProperty withdraw = new SimpleStringProperty();
    private final SimpleStringProperty gatewayId = new SimpleStringProperty();

    private AccountField accountField;

    public AccountFXBean(AccountField accountField, boolean selected) {
        update(accountField, selected);
    }

    public AccountField getAccountField() {
        return accountField;
    }

    public void update(AccountField newAccountField, boolean newSelected) {
        if (newAccountField == null) {
            return;
        }

        updateSelected(newSelected);

        if (accountField == null) {
            updateAccountId(newAccountField);
            updateCode(newAccountField);
            updateHolder(newAccountField);
            updateCurrency(newAccountField);
            updateGatewayId(newAccountField);
        }

        if (newAccountField != accountField) {
            updateChangeable(newAccountField);
            accountField = newAccountField;
        }
    }

    private void updateChangeable(AccountField newAccountField) {


        updateBalance(newAccountField);
        updateMarginRatio(newAccountField);
        updateAvailable(newAccountField);
        updateTodayProfit(newAccountField);
        updateTodayProfitRatio(newAccountField);
        updateCloseProfit(newAccountField);
        updatePositionProfit(newAccountField);
        updateCommission(newAccountField);
        updateMargin(newAccountField);
        updatePreBalance(newAccountField);
        updateDeposit(newAccountField);
        updateWithdraw(newAccountField);
    }

    private void updateSelected(boolean selected){
        setSelected(selected);
    }

    private void updateAccountId(AccountField newAccountField) {
        setAccountId(newAccountField.getAccountId());
    }

    private void updateCode(AccountField newAccountField) {
        setCode(newAccountField.getCode());
    }

    private void updateHolder(AccountField newAccountField) {
        setHolder(newAccountField.getHolder());
    }

    private void updateCurrency(AccountField newAccountField) {
        setCurrency(newAccountField.getCurrency().getValueDescriptor().getName());
    }

    private void updateBalance(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getBalance(), newAccountField.getBalance())) {
            String balance = "渲染错误";
            try {
                balance = String.format("%,.2f", newAccountField.getBalance());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setBalance(balance);
        }
    }

    private void updateMarginRatio(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getBalance(), newAccountField.getBalance()) || !CommonUtils.isEquals(accountField.getMargin(), newAccountField.getMargin())) {
            String marginRatio = "渲染错误";
            try {
                marginRatio = String.format("%,.2f%%", newAccountField.getMargin() / newAccountField.getBalance() * 100);
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setMarginRatio(marginRatio);
        }
    }

    private void updateAvailable(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getAvailable(), newAccountField.getAvailable())) {
            String available = "渲染错误";
            try {
                available = String.format("%,.2f", newAccountField.getAvailable());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setAvailable(available);
        }
    }

    private void updateTodayProfit(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getBalance(), newAccountField.getBalance())) {
            Text tdProfitText = new Text("渲染错误");
            try {
                double preBalance = newAccountField.getPreBalance();

                double tdProfit = newAccountField.getBalance() - preBalance - newAccountField.getDeposit() + newAccountField.getWithdraw();

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
            setTodayProfit(tdProfitText);
        }
    }

    private void updateTodayProfitRatio(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getBalance(), newAccountField.getBalance())) {
            Text tdProfitRatioText = new Text("渲染错误");
            try {
                double preBalance = newAccountField.getPreBalance();

                double tdProfit = newAccountField.getBalance() - preBalance - newAccountField.getDeposit() + newAccountField.getWithdraw();
                if (preBalance == 0) {
                    preBalance = newAccountField.getBalance();
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
            setTodayProfitRatio(tdProfitRatioText);

        }
    }

    private void updateCloseProfit(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getCloseProfit(), newAccountField.getCloseProfit())) {
            Text closeProfitText = new Text("渲染错误");

            try {
                closeProfitText.setText(String.format("%,.2f", newAccountField.getCloseProfit()));
                if (newAccountField.getCloseProfit() > 0) {
                    closeProfitText.getStyleClass().add("trade-long-color");
                } else if (newAccountField.getCloseProfit() < 0) {
                    closeProfitText.getStyleClass().add("trade-short-color");
                }
                closeProfitText.setUserData(newAccountField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }

            setCloseProfit(closeProfitText);
        }
    }

    private void updatePositionProfit(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getPositionProfit(), newAccountField.getPositionProfit())) {
            Text positionProfitText = new Text("渲染错误");

            try {
                positionProfitText.setText(String.format("%,.2f", newAccountField.getPositionProfit()));
                if (newAccountField.getPositionProfit() > 0) {
                    positionProfitText.getStyleClass().add("trade-long-color");
                } else if (newAccountField.getPositionProfit() < 0) {
                    positionProfitText.getStyleClass().add("trade-short-color");
                }
                positionProfitText.setUserData(newAccountField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setPositionProfit(positionProfitText);
        }
    }

    private void updateCommission(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getCommission(), newAccountField.getCommission())) {
            String commission = "渲染错误";
            try {
                commission = String.format("%,.2f", newAccountField.getCommission());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setCommission(commission);
        }
    }

    private void updateMargin(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getMargin(), newAccountField.getMargin())) {
            String margin = "渲染错误";
            try {
                margin = String.format("%,.2f", newAccountField.getMargin());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setMargin(margin);
        }
    }

    private void updatePreBalance(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getPreBalance(), newAccountField.getPreBalance())) {
            String preBalance = "渲染错误";
            try {
                preBalance = String.format("%,.2f", newAccountField.getPreBalance());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setPreBalance(preBalance);
        }
    }

    private void updateDeposit(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getDeposit(), newAccountField.getDeposit())) {
            String deposit = "渲染错误";
            try {
                deposit = String.format("%,.2f", newAccountField.getDeposit());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setDeposit(deposit);
        }
    }

    private void updateWithdraw(AccountField newAccountField) {
        if (accountField == null || !CommonUtils.isEquals(accountField.getWithdraw(), newAccountField.getWithdraw())) {
            String withdraw = "渲染错误";
            try {
                withdraw = String.format("%,.2f", newAccountField.getWithdraw());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setWithdraw(withdraw);
        }
    }

    private void updateGatewayId(AccountField newAccountField) {
        setGatewayId(newAccountField.getGatewayId());
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public String getAccountId() {
        return accountId.get();
    }

    public void setAccountId(String accountId) {
        this.accountId.set(accountId);
    }

    public SimpleStringProperty accountIdProperty() {
        return accountId;
    }

    public String getCode() {
        return code.get();
    }

    public void setCode(String code) {
        this.code.set(code);
    }

    public SimpleStringProperty codeProperty() {
        return code;
    }

    public String getHolder() {
        return holder.get();
    }

    public void setHolder(String holder) {
        this.holder.set(holder);
    }

    public SimpleStringProperty holderProperty() {
        return holder;
    }

    public String getCurrency() {
        return currency.get();
    }

    public void setCurrency(String currency) {
        this.currency.set(currency);
    }

    public SimpleStringProperty currencyProperty() {
        return currency;
    }

    public String getBalance() {
        return balance.get();
    }

    public void setBalance(String balance) {
        this.balance.set(balance);
    }

    public SimpleStringProperty balanceProperty() {
        return balance;
    }

    public String getMarginRatio() {
        return marginRatio.get();
    }

    public void setMarginRatio(String marginRatio) {
        this.marginRatio.set(marginRatio);
    }

    public SimpleStringProperty marginRatioProperty() {
        return marginRatio;
    }

    public String getAvailable() {
        return available.get();
    }

    public void setAvailable(String available) {
        this.available.set(available);
    }

    public SimpleStringProperty availableProperty() {
        return available;
    }

    public Text getTodayProfit() {
        return todayProfit.get();
    }

    public void setTodayProfit(Text todayProfit) {
        this.todayProfit.set(todayProfit);
    }

    public SimpleObjectProperty<Text> todayProfitProperty() {
        return todayProfit;
    }

    public Text getTodayProfitRatio() {
        return todayProfitRatio.get();
    }

    public void setTodayProfitRatio(Text todayProfitRatio) {
        this.todayProfitRatio.set(todayProfitRatio);
    }

    public SimpleObjectProperty<Text> todayProfitRatioProperty() {
        return todayProfitRatio;
    }

    public Text getCloseProfit() {
        return closeProfit.get();
    }

    public void setCloseProfit(Text closeProfit) {
        this.closeProfit.set(closeProfit);
    }

    public SimpleObjectProperty<Text> closeProfitProperty() {
        return closeProfit;
    }

    public Text getPositionProfit() {
        return positionProfit.get();
    }

    public void setPositionProfit(Text positionProfit) {
        this.positionProfit.set(positionProfit);
    }

    public SimpleObjectProperty<Text> positionProfitProperty() {
        return positionProfit;
    }

    public String getCommission() {
        return commission.get();
    }

    public void setCommission(String commission) {
        this.commission.set(commission);
    }

    public SimpleStringProperty commissionProperty() {
        return commission;
    }

    public String getMargin() {
        return margin.get();
    }

    public void setMargin(String margin) {
        this.margin.set(margin);
    }

    public SimpleStringProperty marginProperty() {
        return margin;
    }

    public String getPreBalance() {
        return preBalance.get();
    }

    public void setPreBalance(String preBalance) {
        this.preBalance.set(preBalance);
    }

    public SimpleStringProperty preBalanceProperty() {
        return preBalance;
    }

    public String getDeposit() {
        return deposit.get();
    }

    public void setDeposit(String deposit) {
        this.deposit.set(deposit);
    }

    public SimpleStringProperty depositProperty() {
        return deposit;
    }

    public String getWithdraw() {
        return withdraw.get();
    }

    public void setWithdraw(String withdraw) {
        this.withdraw.set(withdraw);
    }

    public SimpleStringProperty withdrawProperty() {
        return withdraw;
    }

    public String getGatewayId() {
        return gatewayId.get();
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId.set(gatewayId);
    }

    public SimpleStringProperty gatewayIdProperty() {
        return gatewayId;
    }
}
