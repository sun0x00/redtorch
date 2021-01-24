package xyz.redtorch.desktop.layout.base;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

import java.util.List;

@Component
public class MarketDetailsLayout {

    @Autowired
    private DesktopTradeCachesService desktopTradeCachesService;

    private final VBox vBox = new VBox();

    private boolean layoutCreated = false;

    private final Text contractSymbolText = new Text("--");
    private final Text contractNameText = new Text("--");

    private final Text ask5PriceText = new Text("--");
    private final Text ask5VolumeText = new Text("-");
    private final Text ask4PriceText = new Text("--");
    private final Text ask4VolumeText = new Text("-");
    private final Text ask3PriceText = new Text("--");
    private final Text ask3VolumeText = new Text("-");
    private final Text ask2PriceText = new Text("--");
    private final Text ask2VolumeText = new Text("-");
    private final Text ask1PriceText = new Text("--");
    private final Text ask1VolumeText = new Text("-");
    private final Text bid1PriceText = new Text("--");
    private final Text bid1VolumeText = new Text("-");
    private final Text bid2PriceText = new Text("--");
    private final Text bid2VolumeText = new Text("-");
    private final Text bid3PriceText = new Text("--");
    private final Text bid3VolumeText = new Text("-");
    private final Text bid4PriceText = new Text("--");
    private final Text bid4VolumeText = new Text("-");
    private final Text bid5PriceText = new Text("--");
    private final Text bid5VolumeText = new Text("-");

    private final Text lastPriceText = new Text("-");
    private final Text pctChangeText = new Text("-");
    private final Text volumeDeltaText = new Text("-");
    private final Text priceDiffText = new Text("-");
    private final Text volumeText = new Text("-");
    private final Text openPriceText = new Text("-");
    private final Text openInterestText = new Text("-");
    private final Text highPriceText = new Text("-");
    private final Text dayOpenInterestDeltaText = new Text("-");
    private final Text lowPriceText = new Text("-");
    private final Text preClosePriceText = new Text("-");
    private final Text upperLimitPriceText = new Text("-");
    private final Text preSettlePriceText = new Text("-");
    private final Text lowerLimitPriceText = new Text("-");
    private final Text settlePriceText = new Text("-");
    private final Text timeText = new Text("-");

    private final double labelWidth = 35;
    private final double priceWidth = 100;
    private final double volumeWidth = 100;
    private final double valueWidth = 82;

    private TickField tick = null;

    public Node getNode() {
        if (!layoutCreated) {
            createLayout();
            layoutCreated = true;
        }
        return this.vBox;
    }

    public void updateData(TickField tick) {
        if (tick == null || !tick.equals(this.tick)) {
            this.tick = tick;
            render();
        }
    }

    public void render() {
        this.ask5PriceText.getStyleClass().clear();
        this.ask5VolumeText.getStyleClass().clear();
        this.ask4PriceText.getStyleClass().clear();
        this.ask4VolumeText.getStyleClass().clear();
        this.ask3PriceText.getStyleClass().clear();
        this.ask3VolumeText.getStyleClass().clear();
        this.ask2PriceText.getStyleClass().clear();
        this.ask2VolumeText.getStyleClass().clear();
        this.ask1PriceText.getStyleClass().clear();
        this.ask1VolumeText.getStyleClass().clear();
        this.bid1PriceText.getStyleClass().clear();
        this.bid1VolumeText.getStyleClass().clear();
        this.bid2PriceText.getStyleClass().clear();
        this.bid2VolumeText.getStyleClass().clear();
        this.bid3PriceText.getStyleClass().clear();
        this.bid3VolumeText.getStyleClass().clear();
        this.bid4PriceText.getStyleClass().clear();
        this.bid4VolumeText.getStyleClass().clear();
        this.bid5PriceText.getStyleClass().clear();
        this.bid5VolumeText.getStyleClass().clear();
        if (this.tick == null) {

            contractSymbolText.setText("--");
            contractNameText.setText("--");

            this.ask5PriceText.setText("--");
            this.ask5VolumeText.setText("-");
            this.ask4PriceText.setText("--");
            this.ask4VolumeText.setText("-");
            this.ask3PriceText.setText("--");
            this.ask3VolumeText.setText("-");
            this.ask2PriceText.setText("--");
            this.ask2VolumeText.setText("-");
            this.ask1PriceText.setText("--");
            this.ask1VolumeText.setText("-");
            this.bid1PriceText.setText("--");
            this.bid1VolumeText.setText("-");
            this.bid2PriceText.setText("--");
            this.bid2VolumeText.setText("-");
            this.bid3PriceText.setText("--");
            this.bid3VolumeText.setText("-");
            this.bid4PriceText.setText("--");
            this.bid4VolumeText.setText("-");
            this.bid5PriceText.setText("--");
            this.bid5VolumeText.setText("-");

            this.lastPriceText.setText("-");
            this.pctChangeText.setText("-");
            this.volumeDeltaText.setText("-");
            this.priceDiffText.setText("-");
            this.volumeText.setText("-");
            this.openPriceText.setText("-");
            this.openInterestText.setText("-");
            this.highPriceText.setText("-");
            this.dayOpenInterestDeltaText.setText("-");
            this.lowPriceText.setText("-");
            this.preClosePriceText.setText("-");
            this.upperLimitPriceText.setText("-");
            this.preSettlePriceText.setText("-");
            this.lowerLimitPriceText.setText("-");
            this.settlePriceText.setText("-");
            this.timeText.setText("-");

        } else {
            ContractField contract = desktopTradeCachesService.queryContractByUnifiedSymbol(tick.getUnifiedSymbol());

            String symbol = tick.getUnifiedSymbol();
            String name = tick.getUnifiedSymbol();
            int decimalDigits = 4;
            if (contract != null) {
                decimalDigits = CommonUtils.getNumberDecimalDigits(contract.getPriceTick());
                if (decimalDigits < 0) {
                    decimalDigits = 4;
                }

                symbol = contract.getSymbol();
                name = contract.getName();
            }

            String priceStringFormat = "%,." + decimalDigits + "f";

            contractSymbolText.setText(symbol);
            contractNameText.setText(name);

            double basePrice = tick.getPreSettlePrice();
            if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
                basePrice = tick.getPreClosePrice();
            }
            if (basePrice == 0 || basePrice == Double.MAX_VALUE) {
                basePrice = tick.getOpenPrice();
            }

            List<Double> askPriceList = this.tick.getAskPriceList();
            List<Integer> askVolumeList = this.tick.getAskVolumeList();

            if (askPriceList.size() >= 5) {
                Double price = askPriceList.get(5 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.ask5PriceText.setText(String.format(priceStringFormat, price));
                    this.ask5VolumeText.setText("" + askVolumeList.get(5 - 1));
                    this.ask5VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.ask5PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.ask5PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.ask5PriceText.setText("--");
                    this.ask5VolumeText.setText("-");
                }

            }

            if (askPriceList.size() >= 4) {
                Double price = askPriceList.get(4 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.ask4PriceText.setText(String.format(priceStringFormat, price));
                    this.ask4VolumeText.setText("" + askVolumeList.get(4 - 1));
                    this.ask4VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.ask4PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.ask4PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.ask4PriceText.setText("--");
                    this.ask4VolumeText.setText("-");
                }

            }

            if (askPriceList.size() >= 3) {
                Double price = askPriceList.get(3 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.ask3PriceText.setText(String.format(priceStringFormat, price));
                    this.ask3VolumeText.setText("" + askVolumeList.get(3 - 1));
                    this.ask3VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.ask3PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.ask3PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.ask3PriceText.setText("--");
                    this.ask3VolumeText.setText("-");
                }

            }

            if (askPriceList.size() >= 2) {
                Double price = askPriceList.get(2 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.ask2PriceText.setText(String.format(priceStringFormat, price));
                    this.ask2VolumeText.setText("" + askVolumeList.get(2 - 1));
                    this.ask2VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.ask2PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.ask2PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.ask2PriceText.setText("--");
                    this.ask2VolumeText.setText("-");
                }

            }

            if (askPriceList.size() >= 1) {
                Double price = askPriceList.get(1 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.ask1PriceText.setText(String.format(priceStringFormat, price));
                    this.ask1VolumeText.setText("" + askVolumeList.get(1 - 1));
                    this.ask1VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.ask1PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.ask1PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.ask1PriceText.setText("--");
                    this.ask1VolumeText.setText("-");
                }

            }

            List<Double> bidPriceList = this.tick.getBidPriceList();
            List<Integer> bidVolumeList = this.tick.getBidVolumeList();

            if (bidPriceList.size() >= 5) {
                Double price = bidPriceList.get(5 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.bid5PriceText.setText(String.format(priceStringFormat, price));
                    this.bid5VolumeText.setText("" + bidVolumeList.get(5 - 1));
                    this.bid5VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.bid5PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.bid5PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.bid5PriceText.setText("--");
                    this.bid5VolumeText.setText("-");
                }

            }

            if (bidPriceList.size() >= 4) {
                Double price = bidPriceList.get(4 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.bid4PriceText.setText(String.format(priceStringFormat, price));
                    this.bid4VolumeText.setText("" + bidVolumeList.get(4 - 1));
                    this.bid4VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.bid4PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.bid4PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.bid4PriceText.setText("--");
                    this.bid4VolumeText.setText("-");
                }

            }

            if (bidPriceList.size() >= 3) {
                Double price = bidPriceList.get(3 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.bid3PriceText.setText(String.format(priceStringFormat, price));
                    this.bid3VolumeText.setText("" + bidVolumeList.get(3 - 1));
                    this.bid3VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.bid3PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.bid3PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.bid3PriceText.setText("--");
                    this.bid3VolumeText.setText("-");
                }

            }

            if (bidPriceList.size() >= 2) {
                Double price = bidPriceList.get(2 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.bid2PriceText.setText(String.format(priceStringFormat, price));
                    this.bid2VolumeText.setText("" + bidVolumeList.get(2 - 1));
                    this.bid2VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.bid2PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.bid2PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.bid2PriceText.setText("--");
                    this.bid2VolumeText.setText("-");
                }

            }

            if (bidPriceList.size() >= 1) {
                Double price = bidPriceList.get(1 - 1);
                if (price != 0 && price != Double.MAX_VALUE) {
                    this.bid1PriceText.setText(String.format(priceStringFormat, price));
                    this.bid1VolumeText.setText("" + bidVolumeList.get(1 - 1));
                    this.bid1VolumeText.getStyleClass().add("trade-remind-color");
                    if (price > basePrice) {
                        this.bid1PriceText.getStyleClass().add("trade-long-color");
                    } else if (price < basePrice) {
                        this.bid1PriceText.getStyleClass().add("trade-short-color");
                    }
                } else {
                    this.bid1PriceText.setText("--");
                    this.bid1VolumeText.setText("-");
                }

            }

            double lastPrice = tick.getLastPrice();
            this.lastPriceText.getStyleClass().clear();
            if (lastPrice == Double.MAX_VALUE) {
                this.lastPriceText.setText("-");
            } else {
                this.lastPriceText.setText(String.format(priceStringFormat, lastPrice));
                if (lastPrice > basePrice) {
                    this.lastPriceText.getStyleClass().add("trade-long-color");
                } else if (lastPrice < basePrice) {
                    this.lastPriceText.getStyleClass().add("trade-short-color");
                }
            }

            this.pctChangeText.getStyleClass().clear();
            double priceDiff = 0d;
            if (lastPrice == Double.MAX_VALUE || basePrice == 0 || basePrice == Double.MAX_VALUE) {
                this.pctChangeText.setText("-");
            } else {
                priceDiff = lastPrice - basePrice;
                double pctChange = priceDiff / basePrice;
                this.pctChangeText.setText(String.format("%,.2f%%", pctChange * 100));

                if (priceDiff > 0) {
                    this.pctChangeText.getStyleClass().add("trade-long-color");
                }
                if (priceDiff < 0) {
                    this.pctChangeText.getStyleClass().add("trade-short-color");
                }
            }

            Long volumeDelta = tick.getVolumeDelta();
            this.volumeDeltaText.setText(String.format("%,d", volumeDelta));

            this.priceDiffText.setText(String.format(priceStringFormat, priceDiff));
            this.priceDiffText.getStyleClass().clear();
            if (priceDiff > 0) {
                this.priceDiffText.getStyleClass().add("trade-long-color");
            }
            if (priceDiff < 0) {
                this.priceDiffText.getStyleClass().add("trade-short-color");
            }

            this.volumeText.setText(String.format("%,d", tick.getVolume()));

            double openPrice = tick.getOpenPrice();
            this.openPriceText.getStyleClass().clear();
            if (openPrice == Double.MAX_VALUE) {
                this.openPriceText.setText("-");
            } else {
                this.openPriceText.setText(String.format(priceStringFormat, openPrice));
                if (openPrice > basePrice) {
                    this.openPriceText.getStyleClass().add("trade-long-color");
                } else if (openPrice < basePrice) {
                    this.openPriceText.getStyleClass().add("trade-short-color");
                }
            }

            if (tick.getOpenInterest() == Double.MAX_VALUE) {
                this.openInterestText.setText("-");
            } else {
                this.openInterestText.setText(String.format("%,.0f", tick.getOpenInterest()));
            }

            double highPrice = tick.getHighPrice();
            this.highPriceText.getStyleClass().clear();
            if (highPrice == Double.MAX_VALUE) {
                this.highPriceText.setText("-");
            } else {
                this.highPriceText.setText(String.format(priceStringFormat, highPrice));
                if (highPrice > basePrice) {
                    this.highPriceText.getStyleClass().add("trade-long-color");
                } else if (highPrice < basePrice) {
                    this.highPriceText.getStyleClass().add("trade-short-color");
                }
            }

            if (tick.getOpenInterest() == Double.MAX_VALUE || tick.getPreOpenInterest() == Double.MAX_VALUE) {
                this.dayOpenInterestDeltaText.setText("-");
            } else {
                this.dayOpenInterestDeltaText.setText(String.format("%,.0f", tick.getOpenInterest() - tick.getPreOpenInterest()));
            }

            double lowPrice = tick.getLowPrice();
            this.lowPriceText.getStyleClass().clear();
            if (lowPrice == Double.MAX_VALUE) {
                this.lowPriceText.setText("-");
            } else {
                this.lowPriceText.setText(String.format(priceStringFormat, lowPrice));
                if (lowPrice > basePrice) {
                    this.lowPriceText.getStyleClass().add("trade-long-color");
                } else if (lowPrice < basePrice) {
                    this.lowPriceText.getStyleClass().add("trade-short-color");
                }
            }

            if (tick.getPreClosePrice() == Double.MAX_VALUE) {
                this.preClosePriceText.setText("-");
            } else {
                this.preClosePriceText.setText(String.format(priceStringFormat, tick.getPreClosePrice()));
            }

            if (tick.getUpperLimit() == Double.MAX_VALUE) {
                this.upperLimitPriceText.setText("-");
            } else {
                this.upperLimitPriceText.setText(String.format(priceStringFormat, tick.getUpperLimit()));
            }

            if (tick.getPreSettlePrice() == Double.MAX_VALUE) {
                this.preSettlePriceText.setText("-");
            } else {
                this.preSettlePriceText.setText(String.format(priceStringFormat, tick.getPreSettlePrice()));
            }

            if (tick.getLowerLimit() == Double.MAX_VALUE) {
                this.lowerLimitPriceText.setText("-");
            } else {
                this.lowerLimitPriceText.setText(String.format(priceStringFormat, tick.getLowerLimit()));
            }

            if (tick.getSettlePrice() == Double.MAX_VALUE) {
                this.settlePriceText.setText("-");
            } else {
                this.settlePriceText.setText(String.format(priceStringFormat, tick.getSettlePrice()));
            }

            this.timeText.setText(CommonUtils.millsToLocalDateTime(tick.getActionTimestamp()).format(CommonUtils.T_FORMAT_WITH_MS_FORMATTER));

        }
    }

    private void createLayout() {

        double vBoxWidth = 240;
        vBox.setPrefWidth(vBoxWidth);
        vBox.setMinWidth(vBoxWidth);
        vBox.setStyle("-fx-border-color: rgb(200, 200, 200);-fx-border-style: solid;-fx-border-width: 0 0 0 0;");

        contractSymbolText.setStyle("-fx-font-size: 16;");
        contractNameText.setStyle("-fx-font-size: 16;");
        contractSymbolText.getStyleClass().add("trade-remind-color");
        contractNameText.getStyleClass().add("trade-info-color");
        contractSymbolText.setWrappingWidth(vBoxWidth);
        contractNameText.setWrappingWidth(vBoxWidth);
        contractSymbolText.setTextAlignment(TextAlignment.CENTER);
        contractNameText.setTextAlignment(TextAlignment.CENTER);

        vBox.getChildren().add(contractSymbolText);
        vBox.getChildren().add(contractNameText);

        Insets rightInsets = new Insets(0, 2, 0, 0);
        Insets leftInsets = new Insets(0, 0, 0, 2);

        HBox ask5HBox = new HBox();
        ask5HBox.setStyle("-fx-border-color: rgb(200, 200, 200);-fx-border-style: dashed;-fx-border-width: 2 0 0 0;");
        Label ask5Label = new Label("卖五");
        ask5Label.getStyleClass().add("trade-label");
        ask5Label.setPrefWidth(labelWidth);
        ask5PriceText.setWrappingWidth(priceWidth);
        ask5PriceText.setTextAlignment(TextAlignment.RIGHT);
        ask5VolumeText.setTextAlignment(TextAlignment.RIGHT);
        ask5VolumeText.setWrappingWidth(volumeWidth);
        ask5HBox.getChildren().addAll(ask5Label, ask5PriceText, ask5VolumeText);
        HBox.setMargin(ask5Label, leftInsets);
        vBox.getChildren().add(ask5HBox);

        HBox ask4HBox = new HBox();
        Label ask4Label = new Label("卖四");
        ask4Label.getStyleClass().add("trade-label");
        ask4Label.setPrefWidth(labelWidth);
        ask4PriceText.setWrappingWidth(priceWidth);
        ask4PriceText.setTextAlignment(TextAlignment.RIGHT);
        ask4VolumeText.setWrappingWidth(volumeWidth);
        ask4VolumeText.setTextAlignment(TextAlignment.RIGHT);
        ask4HBox.getChildren().addAll(ask4Label, ask4PriceText, ask4VolumeText);
        HBox.setMargin(ask4Label, leftInsets);
        vBox.getChildren().add(ask4HBox);

        HBox ask3HBox = new HBox();
        Label ask3Label = new Label("卖三");
        ask3Label.getStyleClass().add("trade-label");
        ask3Label.setPrefWidth(labelWidth);
        ask3PriceText.setWrappingWidth(priceWidth);
        ask3PriceText.setTextAlignment(TextAlignment.RIGHT);
        ask3VolumeText.setWrappingWidth(volumeWidth);
        ask3VolumeText.setTextAlignment(TextAlignment.RIGHT);
        ask3HBox.getChildren().addAll(ask3Label, ask3PriceText, ask3VolumeText);
        HBox.setMargin(ask3Label, leftInsets);
        vBox.getChildren().add(ask3HBox);

        HBox ask2HBox = new HBox();
        Label ask2Label = new Label("卖二");
        ask2Label.getStyleClass().add("trade-label");
        ask2Label.setPrefWidth(labelWidth);
        ask2PriceText.setWrappingWidth(priceWidth);
        ask2PriceText.setTextAlignment(TextAlignment.RIGHT);
        ask2VolumeText.setWrappingWidth(volumeWidth);
        ask2VolumeText.setTextAlignment(TextAlignment.RIGHT);
        ask2HBox.getChildren().addAll(ask2Label, ask2PriceText, ask2VolumeText);
        HBox.setMargin(ask2Label, leftInsets);
        vBox.getChildren().add(ask2HBox);

        HBox ask1HBox = new HBox();
        Label ask1Label = new Label("卖一");
        ask1Label.getStyleClass().add("trade-label");
        ask1Label.setPrefWidth(labelWidth);
        ask1PriceText.setWrappingWidth(priceWidth);
        ask1PriceText.setTextAlignment(TextAlignment.RIGHT);
        ask1PriceText.setStyle("-fx-font-size: 14;");
        ask1VolumeText.setWrappingWidth(volumeWidth);
        ask1VolumeText.setTextAlignment(TextAlignment.RIGHT);
        ask1VolumeText.setStyle("-fx-font-size: 14;");
        ask1HBox.getChildren().addAll(ask1Label, ask1PriceText, ask1VolumeText);
        HBox.setMargin(ask1Label, leftInsets);
        vBox.getChildren().add(ask1HBox);

        HBox bid1HBox = new HBox();
        bid1HBox.setStyle("-fx-border-color: rgb(200, 200, 200);-fx-border-style: dashed;-fx-border-width: 2 0 0 0;");
        Label bid1Label = new Label("买一");
        bid1Label.getStyleClass().add("trade-label");
        bid1Label.setPrefWidth(labelWidth);
        bid1PriceText.setWrappingWidth(priceWidth);
        bid1PriceText.setTextAlignment(TextAlignment.RIGHT);
        bid1PriceText.setStyle("-fx-font-size: 14;");
        bid1VolumeText.setWrappingWidth(volumeWidth);
        bid1VolumeText.setTextAlignment(TextAlignment.RIGHT);
        bid1VolumeText.setStyle("-fx-font-size: 14;");
        bid1HBox.getChildren().addAll(bid1Label, bid1PriceText, bid1VolumeText);
        HBox.setMargin(bid1Label, leftInsets);
        vBox.getChildren().add(bid1HBox);

        HBox bid2HBox = new HBox();
        Label bid2Label = new Label("买二");
        bid2Label.getStyleClass().add("trade-label");
        bid2Label.setPrefWidth(labelWidth);
        bid2PriceText.setWrappingWidth(priceWidth);
        bid2PriceText.setTextAlignment(TextAlignment.RIGHT);
        bid2VolumeText.setWrappingWidth(volumeWidth);
        bid2VolumeText.setTextAlignment(TextAlignment.RIGHT);
        bid2HBox.getChildren().addAll(bid2Label, bid2PriceText, bid2VolumeText);
        HBox.setMargin(bid2Label, leftInsets);
        vBox.getChildren().add(bid2HBox);

        HBox bid3HBox = new HBox();
        Label bid3Label = new Label("买三");
        bid3Label.getStyleClass().add("trade-label");
        bid3Label.setPrefWidth(labelWidth);
        bid3PriceText.setWrappingWidth(priceWidth);
        bid3PriceText.setTextAlignment(TextAlignment.RIGHT);
        bid3VolumeText.setWrappingWidth(volumeWidth);
        bid3VolumeText.setTextAlignment(TextAlignment.RIGHT);
        bid3HBox.getChildren().addAll(bid3Label, bid3PriceText, bid3VolumeText);
        HBox.setMargin(bid3Label, leftInsets);
        vBox.getChildren().add(bid3HBox);

        HBox bid4HBox = new HBox();
        Label bid4Label = new Label("买四");
        bid4Label.getStyleClass().add("trade-label");
        bid4Label.setPrefWidth(labelWidth);
        bid4PriceText.setWrappingWidth(priceWidth);
        bid4PriceText.setTextAlignment(TextAlignment.RIGHT);
        bid4VolumeText.setWrappingWidth(volumeWidth);
        bid4VolumeText.setTextAlignment(TextAlignment.RIGHT);
        bid4HBox.getChildren().addAll(bid4Label, bid4PriceText, bid4VolumeText);
        HBox.setMargin(bid4Label, leftInsets);
        vBox.getChildren().add(bid4HBox);

        HBox bid5HBox = new HBox();
        Label bid5Label = new Label("买五");
        bid5Label.getStyleClass().add("trade-label");
        bid5Label.setPrefWidth(labelWidth);
        bid5PriceText.setWrappingWidth(priceWidth);
        bid5PriceText.setTextAlignment(TextAlignment.RIGHT);
        bid5VolumeText.setWrappingWidth(volumeWidth);
        bid5VolumeText.setTextAlignment(TextAlignment.RIGHT);
        bid5HBox.getChildren().addAll(bid5Label, bid5PriceText, bid5VolumeText);
        HBox.setMargin(bid5Label, leftInsets);
        vBox.getChildren().add(bid5HBox);

        String labelStyle = "-fx-border-color: rgb(200, 200, 200);-fx-border-style: solid;-fx-border-width: 0 0 0 1;";
        HBox line1HBox = new HBox();
        line1HBox.setStyle("-fx-border-color: rgb(200, 200, 200);-fx-border-style: solid;-fx-border-width: 1 0 0 0;");
        Label lastPriceLabel = new Label("最新");
        lastPriceLabel.getStyleClass().add("trade-label");
        lastPriceLabel.setPrefWidth(labelWidth);
        lastPriceText.setWrappingWidth(valueWidth);
        lastPriceText.setTextAlignment(TextAlignment.RIGHT);
        Label pctChangeLabel = new Label("涨跌");
        pctChangeLabel.setStyle(labelStyle);
        pctChangeLabel.getStyleClass().add("trade-label");
        pctChangeLabel.setPrefWidth(labelWidth);
        pctChangeText.setWrappingWidth(valueWidth);
        pctChangeText.setTextAlignment(TextAlignment.RIGHT);
        line1HBox.getChildren().addAll(lastPriceLabel, lastPriceText, pctChangeLabel, pctChangeText);
        HBox.setMargin(lastPriceText, rightInsets);
        HBox.setMargin(lastPriceLabel, leftInsets);
        vBox.getChildren().add(line1HBox);

        HBox line2HBox = new HBox();
        Label volumeDeltaLabel = new Label("现手");
        volumeDeltaLabel.getStyleClass().add("trade-label");
        volumeDeltaLabel.setPrefWidth(labelWidth);
        volumeDeltaText.setWrappingWidth(valueWidth);
        volumeDeltaText.setTextAlignment(TextAlignment.RIGHT);
        volumeDeltaText.getStyleClass().add("trade-remind-color");
        Label priceDiffLabel = new Label("价差");
        priceDiffLabel.setStyle(labelStyle);
        priceDiffLabel.getStyleClass().add("trade-label");
        priceDiffLabel.setPrefWidth(labelWidth);
        priceDiffText.setWrappingWidth(valueWidth);
        priceDiffText.setTextAlignment(TextAlignment.RIGHT);
        line2HBox.getChildren().addAll(volumeDeltaLabel, volumeDeltaText, priceDiffLabel, priceDiffText);
        HBox.setMargin(volumeDeltaText, rightInsets);
        HBox.setMargin(volumeDeltaLabel, leftInsets);
        vBox.getChildren().add(line2HBox);

        HBox line3HBox = new HBox();
        Label volumeLabel = new Label("总手");
        volumeLabel.getStyleClass().add("trade-label");
        volumeLabel.setPrefWidth(labelWidth);
        volumeText.setWrappingWidth(valueWidth);
        volumeText.setTextAlignment(TextAlignment.RIGHT);
        volumeText.getStyleClass().add("trade-remind-color");
        Label openPriceLabel = new Label("开盘");
        openPriceLabel.setStyle(labelStyle);
        openPriceLabel.getStyleClass().add("trade-label");
        openPriceLabel.setPrefWidth(labelWidth);
        openPriceText.setWrappingWidth(valueWidth);
        openPriceText.setTextAlignment(TextAlignment.RIGHT);
        line3HBox.getChildren().addAll(volumeLabel, volumeText, openPriceLabel, openPriceText);
        HBox.setMargin(volumeText, rightInsets);
        HBox.setMargin(volumeLabel, leftInsets);
        vBox.getChildren().add(line3HBox);

        HBox line4HBox = new HBox();
        Label openInterestLabel = new Label("持仓");
        openInterestLabel.getStyleClass().add("trade-label");
        openInterestLabel.setPrefWidth(labelWidth);
        openInterestText.setWrappingWidth(valueWidth);
        openInterestText.setTextAlignment(TextAlignment.RIGHT);
        openInterestText.getStyleClass().add("trade-remind-color");
        Label highPriceLabel = new Label("最高");
        highPriceLabel.setStyle(labelStyle);
        highPriceLabel.getStyleClass().add("trade-label");
        highPriceLabel.setPrefWidth(labelWidth);
        highPriceText.setWrappingWidth(valueWidth);
        highPriceText.setTextAlignment(TextAlignment.RIGHT);
        line4HBox.getChildren().addAll(openInterestLabel, openInterestText, highPriceLabel, highPriceText);
        HBox.setMargin(openInterestText, rightInsets);
        HBox.setMargin(openInterestLabel, leftInsets);
        vBox.getChildren().add(line4HBox);

        HBox line5HBox = new HBox();
        Label dayOpenInterestDeltaLabel = new Label("日增");
        dayOpenInterestDeltaLabel.getStyleClass().add("trade-label");
        dayOpenInterestDeltaLabel.setPrefWidth(labelWidth);
        dayOpenInterestDeltaText.setWrappingWidth(valueWidth);
        dayOpenInterestDeltaText.setTextAlignment(TextAlignment.RIGHT);
        dayOpenInterestDeltaText.getStyleClass().add("trade-remind-color");
        Label lowPriceLabel = new Label("最低");
        lowPriceLabel.setStyle(labelStyle);
        lowPriceLabel.getStyleClass().add("trade-label");
        lowPriceLabel.setPrefWidth(labelWidth);
        lowPriceText.setWrappingWidth(valueWidth);
        lowPriceText.setTextAlignment(TextAlignment.RIGHT);
        line5HBox.getChildren().addAll(dayOpenInterestDeltaLabel, dayOpenInterestDeltaText, lowPriceLabel, lowPriceText);
        HBox.setMargin(dayOpenInterestDeltaText, rightInsets);
        HBox.setMargin(dayOpenInterestDeltaLabel, leftInsets);
        vBox.getChildren().add(line5HBox);

        HBox line6HBox = new HBox();
        Label preClosePriceLabel = new Label("昨收");
        preClosePriceLabel.getStyleClass().add("trade-label");
        preClosePriceLabel.setPrefWidth(labelWidth);
        preClosePriceText.setWrappingWidth(valueWidth);
        preClosePriceText.setTextAlignment(TextAlignment.RIGHT);
        Label upperLimitPriceLabel = new Label("涨停");
        upperLimitPriceLabel.setStyle(labelStyle);
        upperLimitPriceLabel.getStyleClass().add("trade-label");
        upperLimitPriceLabel.setPrefWidth(labelWidth);
        upperLimitPriceText.setWrappingWidth(valueWidth);
        upperLimitPriceText.setTextAlignment(TextAlignment.RIGHT);
        upperLimitPriceText.getStyleClass().add("trade-long-color");
        line6HBox.getChildren().addAll(preClosePriceLabel, preClosePriceText, upperLimitPriceLabel, upperLimitPriceText);
        HBox.setMargin(preClosePriceText, rightInsets);
        HBox.setMargin(preClosePriceLabel, leftInsets);
        vBox.getChildren().add(line6HBox);

        HBox line7HBox = new HBox();
        Label preSettlePriceLabel = new Label("昨结");
        preSettlePriceLabel.getStyleClass().add("trade-label");
        preSettlePriceLabel.setPrefWidth(labelWidth);
        preSettlePriceText.setWrappingWidth(valueWidth);
        preSettlePriceText.setTextAlignment(TextAlignment.RIGHT);
        Label lowerLimitPriceLabel = new Label("跌停");
        lowerLimitPriceLabel.setStyle(labelStyle);
        lowerLimitPriceLabel.getStyleClass().add("trade-label");
        lowerLimitPriceLabel.setPrefWidth(labelWidth);
        lowerLimitPriceText.setWrappingWidth(valueWidth);
        lowerLimitPriceText.setTextAlignment(TextAlignment.RIGHT);
        lowerLimitPriceText.getStyleClass().add("trade-short-color");
        line7HBox.getChildren().addAll(preSettlePriceLabel, preSettlePriceText, lowerLimitPriceLabel, lowerLimitPriceText);
        HBox.setMargin(preSettlePriceText, rightInsets);
        HBox.setMargin(preSettlePriceLabel, leftInsets);
        vBox.getChildren().add(line7HBox);

        HBox line8HBox = new HBox();
        Label settlePriceLabel = new Label("结算");
        settlePriceLabel.getStyleClass().add("trade-label");
        settlePriceLabel.setPrefWidth(labelWidth);
        settlePriceText.setWrappingWidth(valueWidth);
        settlePriceText.setTextAlignment(TextAlignment.RIGHT);
        Label timeLabel = new Label("时间");
        timeLabel.setStyle(labelStyle);
        timeLabel.getStyleClass().add("trade-label");
        timeLabel.setPrefWidth(labelWidth);
        timeText.setWrappingWidth(valueWidth);
        timeText.setTextAlignment(TextAlignment.RIGHT);
        line8HBox.getChildren().addAll(settlePriceLabel, settlePriceText, timeLabel, timeText);
        HBox.setMargin(settlePriceText, rightInsets);
        HBox.setMargin(settlePriceLabel, leftInsets);
        vBox.getChildren().add(line8HBox);

    }
}
