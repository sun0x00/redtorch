package xyz.redtorch.desktop.layout.base.bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.HedgeFlagEnum;
import xyz.redtorch.pb.CoreEnum.OffsetFlagEnum;
import xyz.redtorch.pb.CoreField.TradeField;

public class TradeFXBean {

    private final static Logger logger = LoggerFactory.getLogger(TradeFXBean.class);

    private final SimpleStringProperty tradeId = new SimpleStringProperty();
    private final SimpleObjectProperty<Pane> contract = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Text> direction = new SimpleObjectProperty<>();
    private final SimpleStringProperty offsetFlag = new SimpleStringProperty();
    private final SimpleStringProperty hedgeFlag = new SimpleStringProperty();
    private final SimpleStringProperty price = new SimpleStringProperty();
    private final SimpleIntegerProperty volume = new SimpleIntegerProperty();
    private final SimpleStringProperty tradeTime = new SimpleStringProperty();
    private final SimpleStringProperty adapterTradeId = new SimpleStringProperty();
    private final SimpleStringProperty originOrderId = new SimpleStringProperty();
    private final SimpleStringProperty accountId = new SimpleStringProperty();

    private TradeField tradeField;
    private boolean contractSelectedFlag = false;

    public TradeFXBean(TradeField tradeField, boolean contractSelectedFlag) {
        update(tradeField, contractSelectedFlag);
    }

    public TradeField getTradeField() {
        return tradeField;
    }

    public void update(TradeField newTradeField, boolean newContractSelectedFlag) {
        if (newTradeField == null) {
            return;
        }

        if (tradeField == null) {
            updateTradeId(newTradeField);
            updateDirection(newTradeField);
            updateOffsetFlag(newTradeField);
            updateHedgeFlag(newTradeField);
            updateContract(newTradeField);
            updateTradeTime(newTradeField);
            updateAdapterTradeId(newTradeField);
            updateAccountId(newTradeField);
        }
        if (contractSelectedFlag != newContractSelectedFlag) {
            contractSelectedFlag = newContractSelectedFlag;
            updateContract(newTradeField);
        }

        if (newTradeField != tradeField) {
            updateChangeable(newTradeField);
            tradeField = newTradeField;
        }
    }

    private void updateChangeable(TradeField newTradeField) {
        updatePrice(newTradeField);
        updateVolume(newTradeField);
        updateOriginOrderId(newTradeField);
    }

    private void updateTradeId(TradeField newTradeField) {
        setTradeId(newTradeField.getTradeId());
    }

    private void updateContract(TradeField newTradeField) {
        VBox vBox = new VBox();
        Text unifiedSymbolText = new Text(newTradeField.getContract().getUnifiedSymbol());
        Text shortNameText = new Text(newTradeField.getContract().getName());
        vBox.getChildren().add(unifiedSymbolText);
        vBox.getChildren().add(shortNameText);
        if (contractSelectedFlag) {
            unifiedSymbolText.getStyleClass().add("trade-remind-color");
        }
        vBox.setUserData(newTradeField);
        setContract(vBox);
    }

    private void updateDirection(TradeField newTradeField) {
        Text directionText = new Text();

        if (newTradeField.getDirection() == DirectionEnum.D_Buy) {
            directionText.setText("多");
            directionText.getStyleClass().add("trade-long-color");
        } else if (newTradeField.getDirection() == DirectionEnum.D_Sell) {
            directionText.setText("空");
            directionText.getStyleClass().add("trade-short-color");
        } else if (newTradeField.getDirection() == DirectionEnum.D_Unknown) {
            directionText.setText("未知");
        } else {
            directionText.setText(newTradeField.getDirection().getValueDescriptor().getName());
        }

        directionText.setUserData(newTradeField);

        setDirection(directionText);
    }

    private void updateOffsetFlag(TradeField newTradeField) {
        String offsetFlag;

        if (newTradeField.getOffsetFlag() == OffsetFlagEnum.OF_Close) {
            offsetFlag = "平";
        } else if (newTradeField.getOffsetFlag() == OffsetFlagEnum.OF_CloseToday) {
            offsetFlag = "平今";
        } else if (newTradeField.getOffsetFlag() == OffsetFlagEnum.OF_CloseYesterday) {
            offsetFlag = "平昨";
        } else if (newTradeField.getOffsetFlag() == OffsetFlagEnum.OF_Open) {
            offsetFlag = "开";
        } else if (newTradeField.getOffsetFlag() == OffsetFlagEnum.OF_Unknown) {
            offsetFlag = "未知";
        } else {
            offsetFlag = newTradeField.getOffsetFlag().getValueDescriptor().getName();
        }

        setOffsetFlag(offsetFlag);
    }

    private void updateHedgeFlag(TradeField newTradeField) {
        String hedgeFlag;

        if (newTradeField.getHedgeFlag() == HedgeFlagEnum.HF_Speculation) {
            hedgeFlag = "投机";
        } else if (newTradeField.getHedgeFlag() == HedgeFlagEnum.HF_Hedge) {
            hedgeFlag = "套保";
        } else if (newTradeField.getHedgeFlag() == HedgeFlagEnum.HF_Arbitrage) {
            hedgeFlag = "套利";
        } else if (newTradeField.getHedgeFlag() == HedgeFlagEnum.HF_MarketMaker) {
            hedgeFlag = "做市商";
        } else if (newTradeField.getHedgeFlag() == HedgeFlagEnum.HF_SpecHedge) {
            hedgeFlag = "第一条腿投机第二条腿套保 大商所专用";
        } else if (newTradeField.getHedgeFlag() == HedgeFlagEnum.HF_HedgeSpec) {
            hedgeFlag = "第一条腿套保第二条腿投机 大商所专用";
        } else if (newTradeField.getHedgeFlag() == HedgeFlagEnum.HF_Unknown) {
            hedgeFlag = "未知";
        } else {
            hedgeFlag = newTradeField.getHedgeFlag().getValueDescriptor().getName();
        }

        setHedgeFlag(hedgeFlag);
    }

    private void updatePrice(TradeField newTradeField) {
        if (tradeField == null || !CommonUtils.isEquals(tradeField.getPrice(),newTradeField.getPrice())) {
            String price = "渲染错误";
            try {
                int decimalDigits = CommonUtils.getNumberDecimalDigits(newTradeField.getContract().getPriceTick());
                if (decimalDigits < 0) {
                    decimalDigits = 0;
                }
                String priceStringFormat = "%,." + decimalDigits + "f";

                price = String.format(priceStringFormat, newTradeField.getPrice());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setPrice(price);
        }
    }

    private void updateVolume(TradeField newTradeField) {
        if(tradeField == null || tradeField.getVolume() != newTradeField.getVolume()){
            setVolume(newTradeField.getVolume());
        }
    }

    private void updateTradeTime(TradeField newTradeField) {
        setTradeTime(newTradeField.getTradeTime());
    }

    private void updateOriginOrderId(TradeField newTradeField) {
        if (tradeField == null || !this.tradeField.getOriginOrderId().equals(newTradeField.getOriginOrderId())) {
            setOriginOrderId(newTradeField.getOriginOrderId());
        }
    }

    private void updateAdapterTradeId(TradeField newTradeField) {
        setAdapterTradeId(newTradeField.getAdapterTradeId());
    }

    private void updateAccountId(TradeField newTradeField) {
        setAccountId(newTradeField.getAccountId());
    }

    public String getTradeId() {
        return tradeId.get();
    }

    public void setTradeId(String tradeId) {
        this.tradeId.set(tradeId);
    }

    public SimpleStringProperty tradeIdProperty() {
        return tradeId;
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

    public Text getDirection() {
        return direction.get();
    }

    public void setDirection(Text direction) {
        this.direction.set(direction);
    }

    public SimpleObjectProperty<Text> directionProperty() {
        return direction;
    }

    public String getOffsetFlag() {
        return offsetFlag.get();
    }

    public void setOffsetFlag(String offsetFlag) {
        this.offsetFlag.set(offsetFlag);
    }

    public SimpleStringProperty offsetFlagProperty() {
        return offsetFlag;
    }

    public String getHedgeFlag() {
        return hedgeFlag.get();
    }

    public void setHedgeFlag(String hedgeFlag) {
        this.hedgeFlag.set(hedgeFlag);
    }

    public SimpleStringProperty hedgeFlagProperty() {
        return hedgeFlag;
    }

    public String getPrice() {
        return price.get();
    }

    public void setPrice(String price) {
        this.price.set(price);
    }

    public SimpleStringProperty priceProperty() {
        return price;
    }

    public int getVolume() {
        return volume.get();
    }

    public void setVolume(int volume) {
        this.volume.set(volume);
    }

    public SimpleIntegerProperty volumeProperty() {
        return volume;
    }

    public String getTradeTime() {
        return tradeTime.get();
    }

    public void setTradeTime(String tradeTime) {
        this.tradeTime.set(tradeTime);
    }

    public SimpleStringProperty tradeTimeProperty() {
        return tradeTime;
    }

    public String getAdapterTradeId() {
        return adapterTradeId.get();
    }

    public void setAdapterTradeId(String adapterTradeId) {
        this.adapterTradeId.set(adapterTradeId);
    }

    public SimpleStringProperty adapterTradeIdProperty() {
        return adapterTradeId;
    }

    public String getOriginOrderId() {
        return originOrderId.get();
    }

    public void setOriginOrderId(String originOrderId) {
        this.originOrderId.set(originOrderId);
    }

    public SimpleStringProperty originOrderIdProperty() {
        return originOrderId;
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
}
