package xyz.redtorch.desktop.layout.base.bean;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.PositionDirectionEnum;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.PositionField;

public class PositionFXBean {

    private final static Logger logger = LoggerFactory.getLogger(PositionFXBean.class);

    private final SimpleStringProperty positionId = new SimpleStringProperty();
    private final SimpleObjectProperty<Pane> contract = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Text> direction = new SimpleObjectProperty<>();
    private final SimpleStringProperty hedgeFlag = new SimpleStringProperty();
    private final SimpleObjectProperty<Pane> position = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> todayPosition = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> openProfit = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> positionProfit = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> openPrice = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> positionPrice = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> margin = new SimpleObjectProperty<>();
    private final SimpleStringProperty marginRatio = new SimpleStringProperty();
    private final SimpleStringProperty contractValue = new SimpleStringProperty();
    private final SimpleStringProperty accountId = new SimpleStringProperty();

    private PositionField positionField;
    private boolean contractSelectedFlag = false;
    private double accountBalance;

    public PositionFXBean(PositionField positionField, boolean contractSelectedFlag, double accountBalance) {
        update(positionField, contractSelectedFlag, accountBalance);
    }

    public PositionField getPositionField() {
        return positionField;
    }

    public void update(PositionField newPositionField, boolean newContractSelectedFlag, double newAccountBalance) {
        if (newPositionField == null) {
            return;
        }

        if (positionField == null) {
            updatePositionId(newPositionField);
            updateDirection(newPositionField);
            updateHedgeFlag(newPositionField);
            updateContract(newPositionField);
            updateAccountId(newPositionField);
        }
        if (contractSelectedFlag != newContractSelectedFlag) {
            contractSelectedFlag = newContractSelectedFlag;
            updateContract(newPositionField);
        }

        if (newPositionField != positionField) {
            // 如果持仓更新的数据对象地址不相同，则不是同一条新数据
            accountBalance = newAccountBalance;
            updateChangeable(newPositionField);
            positionField = newPositionField;
        } else if (!CommonUtils.isEquals(accountBalance, newAccountBalance)) {
            // 如果持仓更新的对象是一条旧数据,但是账户资金发生了变化
            accountBalance = newAccountBalance;
            updateMarginRatio(newPositionField);
        }
    }

    private void updateChangeable(PositionField newPositionField) {
        updatePosition(newPositionField);
        updateTodayPosition(newPositionField);
        updateOpenProfit(newPositionField);
        updatePositionProfit(newPositionField);
        updateOpenPrice(newPositionField);
        updatePositionPrice(newPositionField);
        updateMargin(newPositionField);
        updateMarginRatio(newPositionField);
        updateContractValue(newPositionField);
    }

    private void updatePositionId(PositionField newPositionField) {
        setPositionId(newPositionField.getPositionId());
    }

    private void updateContract(PositionField newPositionField) {
        VBox vBox = new VBox();
        Text uniformSymbolText = new Text(newPositionField.getContract().getUniformSymbol());
        Text shortNameText = new Text(newPositionField.getContract().getName());
        vBox.getChildren().add(uniformSymbolText);
        vBox.getChildren().add(shortNameText);
        if (contractSelectedFlag) {
            uniformSymbolText.getStyleClass().add("trade-remind-color");
        }
        vBox.setUserData(newPositionField);
        setContract(vBox);
    }

    private void updateDirection(PositionField newPositionField) {
        Text directionText = new Text("未知");

        if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Long) {
            directionText.setText("多");
            directionText.getStyleClass().add("trade-long-color");
        } else if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Short) {
            directionText.setText("空");
            directionText.getStyleClass().add("trade-short-color");
        } else if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Net) {
            directionText.setText("净");
        } else if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Unknown) {
            directionText.setText("未知");
        } else {
            directionText.setText(newPositionField.getPositionDirection().getValueDescriptor().getName());
        }

        directionText.setUserData(newPositionField);

        setDirection(directionText);
    }

    private void updateHedgeFlag(PositionField newPositionField) {
        String hedgeFlag;

        if (newPositionField.getHedgeFlag() == HedgeFlagEnum.HF_Speculation) {
            hedgeFlag = "投机";
        } else if (newPositionField.getHedgeFlag() == HedgeFlagEnum.HF_Hedge) {
            hedgeFlag = "套保";
        } else if (newPositionField.getHedgeFlag() == HedgeFlagEnum.HF_Arbitrage) {
            hedgeFlag = "套利";
        } else if (newPositionField.getHedgeFlag() == HedgeFlagEnum.HF_MarketMaker) {
            hedgeFlag = "做市商";
        } else if (newPositionField.getHedgeFlag() == HedgeFlagEnum.HF_SpecHedge) {
            hedgeFlag = "第一条腿投机第二条腿套保 大商所专用";
        } else if (newPositionField.getHedgeFlag() == HedgeFlagEnum.HF_HedgeSpec) {
            hedgeFlag = "第一条腿套保第二条腿投机 大商所专用";
        } else if (newPositionField.getHedgeFlag() == HedgeFlagEnum.HF_Unknown) {
            hedgeFlag = "未知";
        } else {
            hedgeFlag = newPositionField.getHedgeFlag().getValueDescriptor().getName();
        }

        setHedgeFlag(hedgeFlag);
    }

    private void updatePosition(PositionField newPositionField) {

        if (positionField == null || positionField.getPosition() != newPositionField.getPosition() || positionField.getFrozen() != newPositionField.getPosition()) {
            VBox vBox = new VBox();

            HBox positionHBox = new HBox();
            Text positionLabelText = new Text("持仓");
            positionLabelText.setWrappingWidth(35);
            positionHBox.getChildren().add(positionLabelText);

            Text positionText = new Text("" + newPositionField.getPosition());
            if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Long) {
                positionText.getStyleClass().add("trade-long-color");
            } else if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Short) {
                positionText.getStyleClass().add("trade-short-color");
            }
            positionHBox.getChildren().add(positionText);
            vBox.getChildren().add(positionHBox);

            HBox frozenHBox = new HBox();
            Text frozenLabelText = new Text("冻结");
            frozenLabelText.setWrappingWidth(35);
            frozenHBox.getChildren().add(frozenLabelText);

            Text frozenText = new Text("" + newPositionField.getFrozen());
            if (newPositionField.getFrozen() != 0) {
                frozenText.getStyleClass().add("trade-info-color");
            }
            frozenHBox.getChildren().add(frozenText);
            vBox.getChildren().add(frozenHBox);

            vBox.setUserData(newPositionField);

            setPosition(vBox);
        }


    }

    private void updateTodayPosition(PositionField newPositionField) {
        if (positionField == null || positionField.getTdPosition() != newPositionField.getTdPosition() || positionField.getTdFrozen() != newPositionField.getTdFrozen()) {
            VBox vBox = new VBox();

            HBox tdPositionHBox = new HBox();
            Text tdPositionLabelText = new Text("持仓");
            tdPositionLabelText.setWrappingWidth(35);
            tdPositionHBox.getChildren().add(tdPositionLabelText);

            Text tdPositionText = new Text("" + newPositionField.getTdPosition());
            if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Long) {
                tdPositionText.getStyleClass().add("trade-long-color");
            } else if (newPositionField.getPositionDirection() == PositionDirectionEnum.PD_Short) {
                tdPositionText.getStyleClass().add("trade-short-color");
            }
            tdPositionHBox.getChildren().add(tdPositionText);
            vBox.getChildren().add(tdPositionHBox);

            HBox frozenHBox = new HBox();
            Text frozenLabelText = new Text("冻结");
            frozenLabelText.setWrappingWidth(35);
            frozenHBox.getChildren().add(frozenLabelText);

            Text frozenText = new Text("" + newPositionField.getTdFrozen());
            if (newPositionField.getTdFrozen() != 0) {
                frozenText.getStyleClass().add("trade-info-color");
            }
            frozenHBox.getChildren().add(frozenText);
            vBox.getChildren().add(frozenHBox);

            vBox.setUserData(newPositionField);

            setTodayPosition(vBox);
        }

    }

    private void updateOpenProfit(PositionField newPositionField) {

        if (positionField == null || !CommonUtils.isEquals(positionField.getOpenPositionProfit(), newPositionField.getOpenPositionProfit())
                || !CommonUtils.isEquals(positionField.getOpenPositionProfitRatio(), newPositionField.getOpenPositionProfitRatio())) {
            VBox vBox = new VBox();
            try {
                Text openPositionProfitText = new Text(String.format("%,.2f", newPositionField.getOpenPositionProfit()));
                Text openPositionProfitRatioText = new Text(String.format("%.2f%%", newPositionField.getOpenPositionProfitRatio() * 100));
                if (newPositionField.getOpenPositionProfit() > 0) {
                    openPositionProfitText.getStyleClass().add("trade-long-color");
                    openPositionProfitRatioText.getStyleClass().add("trade-long-color");
                } else if (newPositionField.getOpenPositionProfit() < 0) {
                    openPositionProfitText.getStyleClass().add("trade-short-color");
                    openPositionProfitRatioText.getStyleClass().add("trade-short-color");
                }
                vBox.getChildren().addAll(openPositionProfitText, openPositionProfitRatioText);
                vBox.setUserData(newPositionField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
                vBox.getChildren().add(new Text("渲染错误"));
            }
            setOpenProfit(vBox);
        }


    }

    private void updatePositionProfit(PositionField newPositionField) {

        if (positionField == null || !CommonUtils.isEquals(positionField.getPositionProfit(), newPositionField.getPositionProfit())
                || !CommonUtils.isEquals(positionField.getPositionProfitRatio(), newPositionField.getPositionProfitRatio())) {
            VBox vBox = new VBox();
            try {
                Text openPositionProfitText = new Text(String.format("%,.2f", newPositionField.getPositionProfit()));
                Text openPositionProfitRatioText = new Text(String.format("%.2f%%", newPositionField.getPositionProfitRatio() * 100));
                if (newPositionField.getPositionProfit() > 0) {
                    openPositionProfitText.getStyleClass().add("trade-long-color");
                    openPositionProfitRatioText.getStyleClass().add("trade-long-color");
                } else if (newPositionField.getPositionProfit() < 0) {
                    openPositionProfitText.getStyleClass().add("trade-short-color");
                    openPositionProfitRatioText.getStyleClass().add("trade-short-color");
                }
                vBox.getChildren().addAll(openPositionProfitText, openPositionProfitRatioText);
                vBox.setUserData(newPositionField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
                vBox.getChildren().add(new Text("渲染错误"));
            }
            setPositionProfit(vBox);
        }
    }

    private void updateOpenPrice(PositionField newPositionField) {

        if (positionField == null || !CommonUtils.isEquals(positionField.getOpenPrice(), newPositionField.getOpenPrice())
                || !CommonUtils.isEquals(positionField.getOpenPriceDiff(), newPositionField.getOpenPriceDiff())) {
            VBox vBox = new VBox();
            try {
                ContractField contract = newPositionField.getContract();

                int decimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
                if (decimalDigits < 0) {
                    decimalDigits = 0;
                }
                String priceStringFormat = "%,." + decimalDigits + "f";

                Text openPriceText = new Text(String.format(priceStringFormat, newPositionField.getOpenPrice()));
                vBox.getChildren().add(openPriceText);

                Text openPriceDiffText = new Text(String.format(priceStringFormat, newPositionField.getOpenPriceDiff()));
                if (newPositionField.getOpenPriceDiff() > 0) {
                    openPriceDiffText.getStyleClass().add("trade-long-color");
                } else if (newPositionField.getOpenPriceDiff() < 0) {
                    openPriceDiffText.getStyleClass().add("trade-short-color");
                }
                vBox.getChildren().add(openPriceDiffText);

                vBox.setUserData(newPositionField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
                vBox.getChildren().add(new Text("渲染错误"));
            }

            setOpenPrice(vBox);
        }

    }

    private void updatePositionPrice(PositionField newPositionField) {

        if (positionField == null || !CommonUtils.isEquals(positionField.getPrice(), newPositionField.getPrice())
                || !CommonUtils.isEquals(positionField.getPriceDiff(), newPositionField.getPriceDiff())) {
            VBox vBox = new VBox();
            try {
                ContractField contract = newPositionField.getContract();
                int decimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
                if (decimalDigits < 0) {
                    decimalDigits = 0;
                }
                String priceStringFormat = "%,." + decimalDigits + "f";

                Text priceText = new Text(String.format(priceStringFormat, newPositionField.getPrice()));
                vBox.getChildren().add(priceText);

                Text priceDiffText = new Text(String.format(priceStringFormat, newPositionField.getPriceDiff()));
                if (newPositionField.getPriceDiff() > 0) {
                    priceDiffText.getStyleClass().add("trade-long-color");
                } else if (newPositionField.getPriceDiff() < 0) {
                    priceDiffText.getStyleClass().add("trade-short-color");
                }
                vBox.getChildren().add(priceDiffText);

                vBox.setUserData(newPositionField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
                vBox.getChildren().add(new Text("渲染错误"));
            }
            setPositionPrice(vBox);
        }

    }

    private void updateMargin(PositionField newPositionField) {

        if (positionField == null || !CommonUtils.isEquals(positionField.getPrice(), newPositionField.getPrice())
                || !CommonUtils.isEquals(positionField.getPriceDiff(), newPositionField.getPriceDiff())) {
            VBox vBox = new VBox();
            try {
                HBox useMarginHBox = new HBox();
                Text useMarginLabelText = new Text("经纪商");
                useMarginLabelText.setWrappingWidth(45);
                useMarginHBox.getChildren().add(useMarginLabelText);

                Text useMarginText = new Text(String.format("%,.2f", newPositionField.getUseMargin()));
                useMarginHBox.getChildren().add(useMarginText);
                vBox.getChildren().add(useMarginHBox);

                HBox exchangeMarginHBox = new HBox();
                Text exchangeMarginLabelText = new Text("交易所");
                exchangeMarginLabelText.setWrappingWidth(45);
                exchangeMarginHBox.getChildren().add(exchangeMarginLabelText);

                Text exchangeMarginText = new Text(String.format("%,.2f", newPositionField.getExchangeMargin()));
                exchangeMarginHBox.getChildren().add(exchangeMarginText);
                vBox.getChildren().add(exchangeMarginHBox);

                vBox.setUserData(newPositionField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
                vBox.getChildren().add(new Text("渲染错误"));
            }
            setMargin(vBox);
        }
    }

    private void updateMarginRatio(PositionField newPositionField) {

        String marginRatioStr = "渲染错误";
        try {
            if (accountBalance == 0) {
                marginRatioStr = "NA";
            } else {
                double marginRatio = newPositionField.getUseMargin() / accountBalance * 100;
                marginRatioStr = String.format("%,.2f", marginRatio) + "%";
            }
        } catch (Exception e) {
            logger.error("渲染错误", e);
        }

        setMarginRatio(marginRatioStr);
    }

    private void updateContractValue(PositionField newPositionField) {
        if (positionField == null || !CommonUtils.isEquals(positionField.getContractValue(), newPositionField.getContractValue())) {
            String contractValue = "渲染错误";
            try {
                contractValue = String.format("%,.0f", newPositionField.getContractValue());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setContractValue(contractValue);
        }

    }

    private void updateAccountId(PositionField newPositionField) {
        setAccountId(newPositionField.getAccountId());
    }


    public String getPositionId() {
        return positionId.get();
    }

    public SimpleStringProperty positionIdProperty() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId.set(positionId);
    }

    public Pane getContract() {
        return contract.get();
    }

    public SimpleObjectProperty<Pane> contractProperty() {
        return contract;
    }

    public void setContract(Pane contract) {
        this.contract.set(contract);
    }

    public Text getDirection() {
        return direction.get();
    }

    public SimpleObjectProperty<Text> directionProperty() {
        return direction;
    }

    public void setDirection(Text direction) {
        this.direction.set(direction);
    }

    public String getHedgeFlag() {
        return hedgeFlag.get();
    }

    public SimpleStringProperty hedgeFlagProperty() {
        return hedgeFlag;
    }

    public void setHedgeFlag(String hedgeFlag) {
        this.hedgeFlag.set(hedgeFlag);
    }

    public Pane getPosition() {
        return position.get();
    }

    public SimpleObjectProperty<Pane> positionProperty() {
        return position;
    }

    public void setPosition(Pane position) {
        this.position.set(position);
    }

    public Pane getTodayPosition() {
        return todayPosition.get();
    }

    public SimpleObjectProperty<Pane> todayPositionProperty() {
        return todayPosition;
    }

    public void setTodayPosition(Pane todayPosition) {
        this.todayPosition.set(todayPosition);
    }

    public Pane getOpenProfit() {
        return openProfit.get();
    }

    public SimpleObjectProperty<Pane> openProfitProperty() {
        return openProfit;
    }

    public void setOpenProfit(Pane openProfit) {
        this.openProfit.set(openProfit);
    }

    public Pane getPositionProfit() {
        return positionProfit.get();
    }

    public SimpleObjectProperty<Pane> positionProfitProperty() {
        return positionProfit;
    }

    public void setPositionProfit(Pane positionProfit) {
        this.positionProfit.set(positionProfit);
    }

    public Pane getOpenPrice() {
        return openPrice.get();
    }

    public SimpleObjectProperty<Pane> openPriceProperty() {
        return openPrice;
    }

    public void setOpenPrice(Pane openPrice) {
        this.openPrice.set(openPrice);
    }

    public Pane getPositionPrice() {
        return positionPrice.get();
    }

    public SimpleObjectProperty<Pane> positionPriceProperty() {
        return positionPrice;
    }

    public void setPositionPrice(Pane positionPrice) {
        this.positionPrice.set(positionPrice);
    }

    public Pane getMargin() {
        return margin.get();
    }

    public SimpleObjectProperty<Pane> marginProperty() {
        return margin;
    }

    public void setMargin(Pane margin) {
        this.margin.set(margin);
    }

    public String getMarginRatio() {
        return marginRatio.get();
    }

    public SimpleStringProperty marginRatioProperty() {
        return marginRatio;
    }

    public void setMarginRatio(String marginRatio) {
        this.marginRatio.set(marginRatio);
    }

    public String getContractValue() {
        return contractValue.get();
    }

    public SimpleStringProperty contractValueProperty() {
        return contractValue;
    }

    public void setContractValue(String contractValue) {
        this.contractValue.set(contractValue);
    }

    public String getAccountId() {
        return accountId.get();
    }

    public SimpleStringProperty accountIdProperty() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId.set(accountId);
    }
}
