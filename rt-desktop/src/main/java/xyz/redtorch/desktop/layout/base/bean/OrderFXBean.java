package xyz.redtorch.desktop.layout.base.bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreEnum.*;
import xyz.redtorch.pb.CoreField.OrderField;

public class OrderFXBean {

    private final static Logger logger = LoggerFactory.getLogger(OrderFXBean.class);

    private final SimpleStringProperty orderId = new SimpleStringProperty();
    private final SimpleObjectProperty<Pane> contract = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Text> direction = new SimpleObjectProperty<>();
    private final SimpleStringProperty offsetFlag = new SimpleStringProperty();
    private final SimpleStringProperty hedgeFlag = new SimpleStringProperty();
    private final SimpleStringProperty orderPriceType = new SimpleStringProperty();
    private final SimpleStringProperty price = new SimpleStringProperty();
    private final SimpleObjectProperty<Pane> volume = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Text> orderStatus = new SimpleObjectProperty<>();
    private final SimpleStringProperty statusMsg = new SimpleStringProperty();
    private final SimpleStringProperty orderTime = new SimpleStringProperty();
    private final SimpleStringProperty timeCondition = new SimpleStringProperty();
    private final SimpleStringProperty volumeCondition = new SimpleStringProperty();
    private final SimpleIntegerProperty minVolume = new SimpleIntegerProperty();
    private final SimpleStringProperty contingentCondition = new SimpleStringProperty();
    private final SimpleStringProperty stopPrice = new SimpleStringProperty();
    private final SimpleStringProperty adapterOrderId = new SimpleStringProperty();
    private final SimpleStringProperty originOrderId = new SimpleStringProperty();
    private final SimpleStringProperty accountId = new SimpleStringProperty();

    private OrderField orderField;
    private boolean contractSelectedFlag = false;

    public OrderFXBean(OrderField orderField, boolean contractSelectedFlag) {
        update(orderField, contractSelectedFlag);
    }

    public OrderField getOrderField() {
        return orderField;
    }

    public void update(OrderField newOrderField, boolean newContractSelectedFlag) {

        if (newOrderField == null) {
            return;
        }

        if (orderField == null) {
            updateOrderId(newOrderField);
            updateDirection(newOrderField);
            updateOffsetFlag(newOrderField);
            updateHedgeFlag(newOrderField);
            updateOrderPriceType(newOrderField);
            updateContract(newOrderField);
            updatePrice(newOrderField);
            updateOrderTime(newOrderField);
            updateTimeCondition(newOrderField);
            updateVolumeCondition(newOrderField);
            updateMinVolume(newOrderField);
            updateContingentCondition(newOrderField);
            updateStopPrice(newOrderField);

            updateAdapterOrderId(newOrderField);
            updateAccountId(newOrderField);
        }
        if (contractSelectedFlag != newContractSelectedFlag) {
            contractSelectedFlag = newContractSelectedFlag;
            updateContract(newOrderField);
        }

        if (newOrderField != orderField) {
            updateChangeable(newOrderField);
            orderField = newOrderField;
        }
    }

    private void updateChangeable(OrderField newOrderField) {
        updateOrderStatus(newOrderField);
        updateStatusMsg(newOrderField);
        updateVolume(newOrderField);
        updateOriginOrderId(newOrderField);
    }

    private void updateOrderId(OrderField newOrderField) {
        setOrderId(newOrderField.getOrderId());
    }

    private void updateContract(OrderField newOrderField) {
        VBox vBox = new VBox();
        Text uniformSymbolText = new Text(newOrderField.getContract().getUniformSymbol());
        Text shortNameText = new Text(newOrderField.getContract().getName());
        vBox.getChildren().add(uniformSymbolText);
        vBox.getChildren().add(shortNameText);
        if (contractSelectedFlag) {
            uniformSymbolText.getStyleClass().add("trade-remind-color");
        }
        vBox.setUserData(newOrderField);
        setContract(vBox);
    }

    private void updateDirection(OrderField newOrderField) {
        Text directionText = new Text();

        if (newOrderField.getDirection() == DirectionEnum.D_Buy) {
            directionText.setText("多");
            directionText.getStyleClass().add("trade-long-color");
        } else if (newOrderField.getDirection() == DirectionEnum.D_Sell) {
            directionText.setText("空");
            directionText.getStyleClass().add("trade-short-color");
        } else if (newOrderField.getDirection() == DirectionEnum.D_Unknown) {
            directionText.setText("未知");
        } else {
            directionText.setText(newOrderField.getDirection().getValueDescriptor().getName());
        }

        directionText.setUserData(newOrderField);

        setDirection(directionText);
    }

    private void updateOffsetFlag(OrderField newOrderField) {
        String offsetFlag;

        if (newOrderField.getOffsetFlag() == OffsetFlagEnum.OF_Close) {
            offsetFlag = "平";
        } else if (newOrderField.getOffsetFlag() == OffsetFlagEnum.OF_CloseToday) {
            offsetFlag = "平今";
        } else if (newOrderField.getOffsetFlag() == OffsetFlagEnum.OF_CloseYesterday) {
            offsetFlag = "平昨";
        } else if (newOrderField.getOffsetFlag() == OffsetFlagEnum.OF_Open) {
            offsetFlag = "开";
        } else if (newOrderField.getOffsetFlag() == OffsetFlagEnum.OF_Unknown) {
            offsetFlag = "未知";
        } else {
            offsetFlag = newOrderField.getOffsetFlag().getValueDescriptor().getName();
        }

        setOffsetFlag(offsetFlag);
    }

    private void updateHedgeFlag(OrderField newOrderField) {
        String hedgeFlag;

        if (newOrderField.getHedgeFlag() == HedgeFlagEnum.HF_Speculation) {
            hedgeFlag = "投机";
        } else if (newOrderField.getHedgeFlag() == HedgeFlagEnum.HF_Hedge) {
            hedgeFlag = "套保";
        } else if (newOrderField.getHedgeFlag() == HedgeFlagEnum.HF_Arbitrage) {
            hedgeFlag = "套利";
        } else if (newOrderField.getHedgeFlag() == HedgeFlagEnum.HF_MarketMaker) {
            hedgeFlag = "做市商";
        } else if (newOrderField.getHedgeFlag() == HedgeFlagEnum.HF_SpecHedge) {
            hedgeFlag = "第一条腿投机第二条腿套保 大商所专用";
        } else if (newOrderField.getHedgeFlag() == HedgeFlagEnum.HF_HedgeSpec) {
            hedgeFlag = "第一条腿套保第二条腿投机 大商所专用";
        } else if (newOrderField.getHedgeFlag() == HedgeFlagEnum.HF_Unknown) {
            hedgeFlag = "未知";
        } else {
            hedgeFlag = newOrderField.getHedgeFlag().getValueDescriptor().getName();
        }

        setHedgeFlag(hedgeFlag);
    }

    private void updateOrderPriceType(OrderField newOrderField) {
        String orderPriceType;

        if (newOrderField.getOrderPriceType() == OrderPriceTypeEnum.OPT_LimitPrice) {
            orderPriceType = "限价";
        } else if (newOrderField.getOrderPriceType() == OrderPriceTypeEnum.OPT_AnyPrice) {
            orderPriceType = "市价";
        } else if (newOrderField.getOrderPriceType() == OrderPriceTypeEnum.OPT_BestPrice) {
            orderPriceType = "最优价";
        } else if (newOrderField.getOrderPriceType() == OrderPriceTypeEnum.OPT_LastPrice) {
            orderPriceType = "最新价";
        } else if (newOrderField.getOrderPriceType() == OrderPriceTypeEnum.OPT_LastPricePlusOneTicks) {
            orderPriceType = "最新价浮动上浮1个ticks";
        } else if (newOrderField.getOrderPriceType() == OrderPriceTypeEnum.OPT_LastPricePlusThreeTicks) {
            orderPriceType = "最新价浮动上浮3个ticks";
        } else if (newOrderField.getOrderPriceType() == OrderPriceTypeEnum.OPT_Unknown) {
            orderPriceType = "未知";
        } else {
            orderPriceType = newOrderField.getOrderPriceType().getValueDescriptor().getName();
        }

        setOrderPriceType(orderPriceType);
    }


    private void updatePrice(OrderField newOrderField) {
            String price = "渲染错误";
            try {
                int decimalDigits = CommonUtils.getNumberDecimalDigits(newOrderField.getContract().getPriceTick());
                if (decimalDigits < 0) {
                    decimalDigits = 0;
                }
                String priceStringFormat = "%,." + decimalDigits + "f";

                price = String.format(priceStringFormat, newOrderField.getPrice());
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            setPrice(price);
    }

    private void updateVolume(OrderField newOrderField) {
        if (orderField == null  || orderField.getOrderStatusValue()!=newOrderField.getOrderStatusValue() || orderField.getTradedVolume()!=newOrderField.getTradedVolume()) {
            VBox vBox = new VBox();

                HBox totalVolumeHBox = new HBox();
                Text totalVolumeLabelText = new Text("总计");
                totalVolumeLabelText.setWrappingWidth(35);
                totalVolumeLabelText.getStyleClass().add("trade-label");
                totalVolumeHBox.getChildren().add(totalVolumeLabelText);

                Text totalVolumeText = new Text("" + newOrderField.getTotalVolume());
                totalVolumeHBox.getChildren().add(totalVolumeText);
                vBox.getChildren().add(totalVolumeHBox);

                HBox tradedVolumeHBox = new HBox();
                Text tradedVolumeLabelText = new Text("成交");
                tradedVolumeLabelText.setWrappingWidth(35);
                tradedVolumeLabelText.getStyleClass().add("trade-label");
                tradedVolumeHBox.getChildren().add(tradedVolumeLabelText);

                Text tradedVolumeText = new Text("" + newOrderField.getTradedVolume());
                tradedVolumeHBox.getChildren().add(tradedVolumeText);
                vBox.getChildren().add(tradedVolumeHBox);

                vBox.setUserData(newOrderField);
                if (CommonConstant.ORDER_STATUS_WORKING_SET.contains(newOrderField.getOrderStatus())) {
                    if (newOrderField.getDirection() == DirectionEnum.D_Buy) {
                        totalVolumeText.getStyleClass().add("trade-long-color");
                    } else if (newOrderField.getDirection() == DirectionEnum.D_Sell) {
                        totalVolumeText.getStyleClass().add("trade-short-color");
                    }
                }
            setVolume(vBox);
        }

    }

    private void updateOrderStatus(OrderField newOrderField) {
        if(orderField == null || orderField.getOrderStatusValue() != newOrderField.getOrderStatusValue()){

            Text orderStatusText = new Text();

            if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_AllTraded) {
                orderStatusText.setText("全部成交");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_Canceled) {
                orderStatusText.setText("已撤销");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_NoTradeQueueing) {
                orderStatusText.setText("未成交还在队列中");
                orderStatusText.getStyleClass().add("trade-remind-color");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_NoTradeNotQueueing) {
                orderStatusText.setText("未成交不在队列中");
                orderStatusText.getStyleClass().add("trade-remind-color");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_PartTradedQueueing) {
                orderStatusText.setText("部分成交还在队列中");
                orderStatusText.getStyleClass().add("trade-remind-color");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_PartTradedNotQueueing) {
                orderStatusText.setText("部分成交不在队列中");
                orderStatusText.getStyleClass().add("trade-remind-color");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_Rejected) {
                orderStatusText.setText("拒单");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_NotTouched) {
                orderStatusText.setText("未触发");
                orderStatusText.getStyleClass().add("trade-remind-color");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_Touched) {
                orderStatusText.setText("已触发");
            } else if (newOrderField.getOrderStatus() == OrderStatusEnum.OS_Unknown) {
                orderStatusText.setText("未知");
            } else {
                orderStatusText.setText(newOrderField.getOrderStatus().getValueDescriptor().getName());
            }

            orderStatusText.setUserData(newOrderField);

            setOrderStatus(orderStatusText);
        }

    }


    private void updateStatusMsg(OrderField newOrderField) {
        if(orderField == null || !orderField.getStatusMsg().equals(newOrderField.getStatusMsg())){
            setStatusMsg(newOrderField.getStatusMsg());
        }
    }


    private void updateOrderTime(OrderField newOrderField) {
        setOrderTime(newOrderField.getOrderTime());
    }

    private void updateTimeCondition(OrderField newOrderField){
        String timeCondition;

            if (newOrderField.getTimeCondition() == TimeConditionEnum.TC_GFA) {
                timeCondition = "(GFA)集合竞价有效";
            } else if (newOrderField.getTimeCondition() == TimeConditionEnum.TC_GFD) {
                timeCondition = "(GFD)当日有效";
            } else if (newOrderField.getTimeCondition() == TimeConditionEnum.TC_GFS) {
                timeCondition = "(GFS)本节有效";
            } else if (newOrderField.getTimeCondition() == TimeConditionEnum.TC_GTC) {
                timeCondition = "(GTC)撤销前有效";
            } else if (newOrderField.getTimeCondition() == TimeConditionEnum.TC_GTD) {
                timeCondition = "(GTD)指定日期前有效";
            } else if (newOrderField.getTimeCondition() == TimeConditionEnum.TC_IOC) {
                timeCondition = "(IOC)立即完成,否则撤销";
            } else if (newOrderField.getTimeCondition() == TimeConditionEnum.TC_Unknown) {
                timeCondition = "未知";
            } else {
                timeCondition = newOrderField.getTimeCondition().getValueDescriptor().getName();
            }
            setTimeCondition(timeCondition);
    }
    private void updateVolumeCondition(OrderField newOrderField){
        String volumeCondition;

            if (newOrderField.getVolumeCondition() == VolumeConditionEnum.VC_AV) {
                volumeCondition = "任何数量";
            } else if (newOrderField.getVolumeCondition() == VolumeConditionEnum.VC_CV) {
                volumeCondition = "全部数量";
            } else if (newOrderField.getVolumeCondition() == VolumeConditionEnum.VC_MV) {
                volumeCondition = "最小数量";
            } else if (newOrderField.getVolumeCondition() == VolumeConditionEnum.VC_Unknown) {
                volumeCondition = "未知";
            } else {
                volumeCondition = newOrderField.getVolumeCondition().getValueDescriptor().getName();
            }
            setVolumeCondition(volumeCondition);

    }
    private void updateMinVolume(OrderField newOrderField){
        setMinVolume(newOrderField.getMinVolume());
    }
    private void updateContingentCondition(OrderField newOrderField){
        String contingentCondition;

            if (newOrderField.getContingentCondition() == ContingentConditionEnum.CC_Immediately) {
                contingentCondition = "立即";
            } else if (newOrderField.getContingentCondition() == ContingentConditionEnum.CC_LocalLastPriceGreaterEqualStopPrice) {
                contingentCondition = "(本地)最新价大于等于条件价";
            } else if (newOrderField.getContingentCondition() == ContingentConditionEnum.CC_LocalLastPriceLesserEqualStopPrice) {
                contingentCondition = "(本地)最新价小于等于条件价";
            } else if (newOrderField.getContingentCondition() == ContingentConditionEnum.CC_LastPriceGreaterEqualStopPrice) {
                contingentCondition = "最新价大于等于条件价";
            } else if (newOrderField.getContingentCondition() == ContingentConditionEnum.CC_LastPriceLesserEqualStopPrice) {
                contingentCondition = "最新价小于等于条件价";
            } else if (newOrderField.getContingentCondition() == ContingentConditionEnum.CC_Unknown) {
                contingentCondition = "未知";
            } else {
                contingentCondition = newOrderField.getContingentCondition().getValueDescriptor().getName();
            }

            setContingentCondition(contingentCondition);
    }
    private void updateStopPrice(OrderField newOrderField){
        String stopPrice = "渲染错误";
        try {
            int decimalDigits = CommonUtils.getNumberDecimalDigits(newOrderField.getContract().getPriceTick());
            if (decimalDigits < 0) {
                decimalDigits = 0;
            }
            String priceStringFormat = "%,." + decimalDigits + "f";

            stopPrice = String.format(priceStringFormat, newOrderField.getStopPrice());
        } catch (Exception e) {
            logger.error("渲染错误", e);
        }
        setStopPrice(stopPrice);
    }

    private void updateOriginOrderId(OrderField newOrderField) {
        if (orderField == null || !orderField.getOriginOrderId().equals(newOrderField.getOriginOrderId())) {
            setOriginOrderId(newOrderField.getOriginOrderId());
        }
    }

    private void updateAdapterOrderId(OrderField newOrderField) {
        setAdapterOrderId(newOrderField.getAdapterOrderId());
    }

    private void updateAccountId(OrderField newOrderField) {
        setAccountId(newOrderField.getAccountId());
    }

    public String getOrderId() {
        return orderId.get();
    }

    public SimpleStringProperty orderIdProperty() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId.set(orderId);
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

    public String getOffsetFlag() {
        return offsetFlag.get();
    }

    public SimpleStringProperty offsetFlagProperty() {
        return offsetFlag;
    }

    public void setOffsetFlag(String offsetFlag) {
        this.offsetFlag.set(offsetFlag);
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

    public String getOrderPriceType() {
        return orderPriceType.get();
    }

    public SimpleStringProperty orderPriceTypeProperty() {
        return orderPriceType;
    }

    public void setOrderPriceType(String orderPriceType) {
        this.orderPriceType.set(orderPriceType);
    }

    public String getPrice() {
        return price.get();
    }

    public SimpleStringProperty priceProperty() {
        return price;
    }

    public void setPrice(String price) {
        this.price.set(price);
    }

    public Pane getVolume() {
        return volume.get();
    }

    public SimpleObjectProperty<Pane> volumeProperty() {
        return volume;
    }

    public void setVolume(Pane volume) {
        this.volume.set(volume);
    }

    public Text getOrderStatus() {
        return orderStatus.get();
    }

    public SimpleObjectProperty<Text> orderStatusProperty() {
        return orderStatus;
    }

    public void setOrderStatus(Text orderStatus) {
        this.orderStatus.set(orderStatus);
    }

    public String getStatusMsg() {
        return statusMsg.get();
    }

    public SimpleStringProperty statusMsgProperty() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg.set(statusMsg);
    }

    public String getOrderTime() {
        return orderTime.get();
    }

    public SimpleStringProperty orderTimeProperty() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime.set(orderTime);
    }

    public String getTimeCondition() {
        return timeCondition.get();
    }

    public SimpleStringProperty timeConditionProperty() {
        return timeCondition;
    }

    public void setTimeCondition(String timeCondition) {
        this.timeCondition.set(timeCondition);
    }

    public String getVolumeCondition() {
        return volumeCondition.get();
    }

    public SimpleStringProperty volumeConditionProperty() {
        return volumeCondition;
    }

    public void setVolumeCondition(String volumeCondition) {
        this.volumeCondition.set(volumeCondition);
    }

    public int getMinVolume() {
        return minVolume.get();
    }

    public SimpleIntegerProperty minVolumeProperty() {
        return minVolume;
    }

    public void setMinVolume(int minVolume) {
        this.minVolume.set(minVolume);
    }

    public String getContingentCondition() {
        return contingentCondition.get();
    }

    public SimpleStringProperty contingentConditionProperty() {
        return contingentCondition;
    }

    public void setContingentCondition(String contingentCondition) {
        this.contingentCondition.set(contingentCondition);
    }

    public String getStopPrice() {
        return stopPrice.get();
    }

    public SimpleStringProperty stopPriceProperty() {
        return stopPrice;
    }

    public void setStopPrice(String stopPrice) {
        this.stopPrice.set(stopPrice);
    }

    public String getAdapterOrderId() {
        return adapterOrderId.get();
    }

    public SimpleStringProperty adapterOrderIdProperty() {
        return adapterOrderId;
    }

    public void setAdapterOrderId(String adapterOrderId) {
        this.adapterOrderId.set(adapterOrderId);
    }

    public String getOriginOrderId() {
        return originOrderId.get();
    }

    public SimpleStringProperty originOrderIdProperty() {
        return originOrderId;
    }

    public void setOriginOrderId(String originOrderId) {
        this.originOrderId.set(originOrderId);
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
