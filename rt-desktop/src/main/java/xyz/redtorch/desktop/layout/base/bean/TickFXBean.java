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
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

public class TickFXBean {

    private final static Logger logger = LoggerFactory.getLogger(TickFXBean.class);

    private final SimpleObjectProperty<Pane> contract = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> lastPrice = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> abPrice = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> abVolume = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> volume = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> openInterest = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Pane> limit = new SimpleObjectProperty<>();
    private final SimpleStringProperty actionTime = new SimpleStringProperty();

    private ContractField contractField;
    private TickField tickField;
    private boolean contractSelectedFlag = false;

    private String priceStringFormat = "%,.4f";

    public TickFXBean(TickField tradeField, boolean contractSelectedFlag, ContractField contractField) {
        update(tradeField, contractSelectedFlag, contractField);
    }

    public TickField getTickField() {
        return tickField;
    }

    public void update(TickField newTickField, boolean newContractSelectedFlag, ContractField newContractField) {
        if (contractField == null && newContractField != null) {
            if (tickField==null || newContractField.getUnifiedSymbol().equals(tickField.getUnifiedSymbol())) {
                contractField = newContractField;
                int decimalDigits = CommonUtils.getNumberDecimalDigits(contractField.getPriceTick());

                if (decimalDigits < 0) {
                    decimalDigits = 4;
                }

                priceStringFormat = "%,." + decimalDigits + "f";
            }
        }

        if (newTickField == null) {
            return;
        }

        if (newTickField != tickField) {
            updateContract(newTickField);
            updateLastPrice(newTickField);
            updateAbPrice(newTickField);
            updateAbVolume(newTickField);
            updateVolume(newTickField);
            updateOpenInterest(newTickField);
            updateLimit(newTickField);
            updateActionTime(newTickField);

            tickField = newTickField;
        } else {
            if (contractSelectedFlag != newContractSelectedFlag) {
                contractSelectedFlag = newContractSelectedFlag;
                updateContract(newTickField);
            }
        }
    }


    private void updateContract(TickField newTickField) {
        VBox vBox = new VBox();
        Text unifiedSymbolText = new Text(newTickField.getUnifiedSymbol());
        Text shortNameText = new Text();
        if (contractField != null) {
            shortNameText.setText(contractField.getName());
        }
        vBox.getChildren().add(unifiedSymbolText);
        vBox.getChildren().add(shortNameText);
        if (contractSelectedFlag) {
            unifiedSymbolText.getStyleClass().add("trade-remind-color");
        }
        vBox.setUserData(newTickField);
        setContract(vBox);
    }


    private void updateLastPrice(TickField newTickField) {

        if (tickField == null || !CommonUtils.isEquals(newTickField.getLastPrice(), tickField.getLastPrice())) {

            VBox vBox = new VBox();

            try {

                double basePrice = newTickField.getPreSettlePrice();
                if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
                    basePrice = newTickField.getPreClosePrice();
                }
                if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
                    basePrice = newTickField.getOpenPrice();
                }

                double lastPrice = newTickField.getLastPrice();

                double priceDiff;

                String lastPriceStr;
                String pctChangeStr = "-";
                String colorStyleClass = "";

                if (lastPrice == Double.MAX_VALUE || basePrice == 0 || basePrice == Double.MAX_VALUE) {
                    lastPriceStr = "-";
                } else {
                    priceDiff = lastPrice - basePrice;
                    double pctChange = priceDiff / basePrice;
                    pctChangeStr = String.format("%,.2f%%", pctChange * 100);
                    lastPriceStr = String.format(priceStringFormat, lastPrice);
                    if (priceDiff > 0) {
                        colorStyleClass = "trade-long-color";
                    }
                    if (priceDiff < 0) {
                        colorStyleClass = "trade-short-color";
                    }
                }

                HBox lastPriceHBox = new HBox();
                Text lastPriceLabelText = new Text("最新");
                lastPriceLabelText.setWrappingWidth(35);
                lastPriceLabelText.getStyleClass().add("trade-label");
                lastPriceHBox.getChildren().add(lastPriceLabelText);

                Text lastPriceText = new Text(lastPriceStr);
                lastPriceHBox.getChildren().add(lastPriceText);
                lastPriceText.getStyleClass().add(colorStyleClass);
                vBox.getChildren().add(lastPriceHBox);

                HBox pctChangeHBox = new HBox();
                Text pctChangeLabelText = new Text("涨跌");
                pctChangeLabelText.setWrappingWidth(35);
                pctChangeLabelText.getStyleClass().add("trade-label");
                pctChangeHBox.getChildren().add(pctChangeLabelText);

                Text pctChangeText = new Text(pctChangeStr);
                pctChangeText.getStyleClass().add(colorStyleClass);
                pctChangeHBox.getChildren().add(pctChangeText);
                vBox.getChildren().add(pctChangeHBox);

                vBox.setUserData(newTickField);
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }

            setLastPrice(vBox);
        }
    }

    private void updateAbPrice(TickField newTickField) {
        if (tickField == null || !tickField.getActionTime().equals(newTickField.getActionTime()) || tickField.getVolume() != newTickField.getVolume()) {
            VBox vBox = new VBox();

            try {

                Double basePrice = newTickField.getPreSettlePrice();
                if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
                    basePrice = newTickField.getPreClosePrice();
                }
                if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
                    basePrice = newTickField.getOpenPrice();
                }

                String askPrice1Str = "-";
                String bidPrice1Str = "-";
                String askPrice1ColorStyleClass = "";
                String bidPrice1ColorStyleClass = "";

                if (newTickField.getAskPriceList().size() > 0 && newTickField.getAskPriceList().get(0) != Double.MAX_VALUE) {
                    askPrice1Str = String.format(priceStringFormat, newTickField.getAskPriceList().get(0));
                    if (newTickField.getAskPriceList().get(0) > basePrice) {
                        askPrice1ColorStyleClass = "trade-long-color";
                    } else if (newTickField.getAskPriceList().get(0) < basePrice) {
                        askPrice1ColorStyleClass = "trade-short-color";
                    }
                }

                if (newTickField.getBidPriceList().size() > 0 && newTickField.getBidPriceList().get(0) != Double.MAX_VALUE) {
                    bidPrice1Str = String.format(priceStringFormat, newTickField.getBidPriceList().get(0));
                    if (newTickField.getBidPriceList().get(0) > basePrice) {
                        bidPrice1ColorStyleClass = "trade-long-color";
                    } else if (newTickField.getBidPriceList().get(0) < basePrice) {
                        bidPrice1ColorStyleClass = "trade-short-color";
                    }
                }
                Text askPrice1Text = new Text(askPrice1Str);
                askPrice1Text.getStyleClass().add(askPrice1ColorStyleClass);
                Text bidPrice1Text = new Text(bidPrice1Str);
                bidPrice1Text.getStyleClass().add(bidPrice1ColorStyleClass);
                vBox.getChildren().addAll(askPrice1Text, bidPrice1Text);

                vBox.setUserData(newTickField);

            } catch (Exception e) {
                logger.error("渲染错误", e);
            }

            setAbPrice(vBox);
        }

    }

    public void updateAbVolume(TickField newTickField) {

        if (tickField == null || !tickField.getActionTime().equals(newTickField.getActionTime()) || tickField.getVolume() != newTickField.getVolume()) {
            VBox vBox = new VBox();
            try {

                String askVolume1Str = "-";
                String bidVolume1Str = "-";
                if (newTickField.getAskVolumeList().size() > 0) {
                    askVolume1Str = "" + newTickField.getAskVolumeList().get(0);
                }

                if (newTickField.getBidVolumeList().size() > 0) {
                    bidVolume1Str = "" + newTickField.getBidVolumeList().get(0);
                }
                Text askVolume1Text = new Text(askVolume1Str);
                askVolume1Text.getStyleClass().add("trade-remind-color");
                Text bidVolume1Text = new Text(bidVolume1Str);
                bidVolume1Text.getStyleClass().add("trade-remind-color");
                vBox.getChildren().addAll(askVolume1Text, bidVolume1Text);
                vBox.setUserData(newTickField);

            } catch (Exception e) {
                logger.error("渲染错误", e);
            }

            setAbVolume(vBox);
        }

    }


    public void updateVolume(TickField newTickField) {
        if (tickField == null || tickField.getVolume() != newTickField.getVolume()) {
            VBox vBox = new VBox();
            vBox.getChildren().addAll(new Text(newTickField.getVolume() + ""), new Text(newTickField.getVolumeDelta() + ""));
            vBox.setUserData(newTickField);
            setVolume(vBox);
        }
    }

    public void updateOpenInterest(TickField newTickField) {
        if (tickField == null || !CommonUtils.isEquals(tickField.getOpenInterest(), newTickField.getOpenInterest())) {
            VBox vBox = new VBox();
            vBox.getChildren().addAll(new Text(newTickField.getOpenInterest() + ""), new Text(newTickField.getOpenInterestDelta() + ""));
            vBox.setUserData(newTickField);
            setOpenInterest(vBox);
        }
    }

    public void updateLimit(TickField newTickField) {
        if (tickField == null || !CommonUtils.isEquals(tickField.getUpperLimit(), newTickField.getUpperLimit())
                || !CommonUtils.isEquals(tickField.getLowerLimit(), newTickField.getLowerLimit())) {
            VBox vBox = new VBox();

            try {
                String upperLimitStr = "-";
                String lowerLimitStr = "-";

                if (newTickField.getUpperLimit() != Double.MAX_VALUE) {
                    upperLimitStr = String.format(priceStringFormat, newTickField.getUpperLimit());
                }

                if (newTickField.getLowerLimit() != Double.MAX_VALUE) {
                    lowerLimitStr = String.format(priceStringFormat, newTickField.getLowerLimit());
                }
                Text upperLimitText = new Text(upperLimitStr);
                upperLimitText.getStyleClass().add("trade-long-color");
                Text lowerLimitText = new Text(lowerLimitStr);
                lowerLimitText.getStyleClass().add("trade-short-color");
                vBox.getChildren().addAll(upperLimitText, lowerLimitText);
                vBox.setUserData(newTickField);

            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setLimit(vBox);
        }

    }

    public void updateActionTime(TickField newTickField) {
        if (tickField == null || !tickField.getActionTime().equals(newTickField.getActionTime())) {
            setActionTime(CommonUtils.millsToLocalDateTime(newTickField.getActionTimestamp()).format(CommonUtils.T_FORMAT_WITH_MS_FORMATTER));
        }
    }

    public Pane getContract() {
        return contract.get();
    }

    public void setContract(Pane contract) {
        this.contract.set(contract);
    }

    public SimpleObjectProperty<Pane> contractProperty() {
        return contract;
    }

    public Pane getLastPrice() {
        return lastPrice.get();
    }

    public void setLastPrice(Pane lastPrice) {
        this.lastPrice.set(lastPrice);
    }

    public SimpleObjectProperty<Pane> lastPriceProperty() {
        return lastPrice;
    }

    public Pane getAbPrice() {
        return abPrice.get();
    }

    public void setAbPrice(Pane abPrice) {
        this.abPrice.set(abPrice);
    }

    public SimpleObjectProperty<Pane> abPriceProperty() {
        return abPrice;
    }

    public Pane getAbVolume() {
        return abVolume.get();
    }

    public void setAbVolume(Pane abVolume) {
        this.abVolume.set(abVolume);
    }

    public SimpleObjectProperty<Pane> abVolumeProperty() {
        return abVolume;
    }

    public Pane getVolume() {
        return volume.get();
    }

    public void setVolume(Pane volume) {
        this.volume.set(volume);
    }

    public SimpleObjectProperty<Pane> volumeProperty() {
        return volume;
    }

    public Pane getOpenInterest() {
        return openInterest.get();
    }

    public void setOpenInterest(Pane openInterest) {
        this.openInterest.set(openInterest);
    }

    public SimpleObjectProperty<Pane> openInterestProperty() {
        return openInterest;
    }

    public Pane getLimit() {
        return limit.get();
    }

    public void setLimit(Pane limit) {
        this.limit.set(limit);
    }

    public SimpleObjectProperty<Pane> limitProperty() {
        return limit;
    }

    public String getActionTime() {
        return actionTime.get();
    }

    public void setActionTime(String actionTime) {
        this.actionTime.set(actionTime);
    }

    public SimpleStringProperty actionTimeProperty() {
        return actionTime;
    }
}
