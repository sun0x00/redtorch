package xyz.redtorch.desktop.layout.base;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreEnum.CurrencyEnum;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.ProductClassEnum;
import xyz.redtorch.pb.CoreField.ContractField;

import java.util.*;

@Component
public class ContractLayout {

    private static final Logger logger = LoggerFactory.getLogger(ContractLayout.class);

    private final VBox vBox = new VBox();

    private boolean layoutCreated = false;

    private final ObservableList<ContractField> contractObservableList = FXCollections.observableArrayList();

    private List<ContractField> contractList = new ArrayList<>();

    private final TableView<ContractField> contractTableView = new TableView<>();

    private Set<String> selectedContractUnifiedSymbolSet = new HashSet<>();

    private String filterExchange = "全部";
    private String filterProductType = "全部";
    private String filterCurrency = "全部";
    private String filterFuzzySymbol = "";
    private String filterLastTradeDateOrContractMonth = "";
    private String filterThirdPartyId = "";
    private String filterUnderlyingSymbol = "";
    private String filterName = "";

    @Autowired
    private GuiMainService guiMainService;

    public Node getNode() {
        if (!layoutCreated) {
            createLayout();
            layoutCreated = true;
        }
        return this.vBox;
    }

    public void updateData(List<ContractField> contractList) {
        if (!new HashSet<>(this.contractList).equals(new HashSet<>(contractList))) {
            this.contractList = contractList;
            render();
        }
    }

    public void render() {

        List<ContractField> newContractList = new ArrayList<>();

        if ((this.filterCurrency.equals("全部")) && (this.filterExchange.equals("全部")) && (this.filterProductType.equals("全部")) && (this.filterFuzzySymbol.equals(""))
                && (this.filterLastTradeDateOrContractMonth.isEmpty()) && (this.filterThirdPartyId.isEmpty()) && (this.filterName.isEmpty()) && (this.filterUnderlyingSymbol.isEmpty())) {
            newContractList = contractList;
        } else {

            for (ContractField contract : this.contractList) {

                boolean flag;
                flag = this.filterCurrency.equals("全部") || contract.getCurrency().getValueDescriptor().getName().equals(filterCurrency);

                flag = flag && (this.filterExchange.equals("全部") || contract.getExchange().getValueDescriptor().getName().equals(filterExchange));

                flag = flag && (this.filterProductType.equals("全部") || contract.getProductClass().getValueDescriptor().getName().equals(filterProductType));

                flag = flag && (this.filterFuzzySymbol.isEmpty() || contract.getSymbol().toLowerCase().contains(this.filterFuzzySymbol.toLowerCase()));

                flag = flag && (this.filterLastTradeDateOrContractMonth.isEmpty() || contract.getLastTradeDateOrContractMonth().contains(this.filterLastTradeDateOrContractMonth));

                flag = flag && (this.filterUnderlyingSymbol.isEmpty() || contract.getUnderlyingSymbol().toLowerCase().contains(this.filterUnderlyingSymbol.toLowerCase()));

                flag = flag && (this.filterThirdPartyId.isEmpty() || contract.getThirdPartyId().toLowerCase().contains(this.filterThirdPartyId.toLowerCase()));

                flag = flag && (this.filterName.isEmpty() || contract.getFullName().toLowerCase().contains(this.filterName.toLowerCase())
                        || contract.getName().toLowerCase().contains(this.filterName.toLowerCase()));

                if (flag) {
                    newContractList.add(contract);
                }
            }
        }

        contractTableView.getSelectionModel().clearSelection();

        contractObservableList.clear();
        contractObservableList.addAll(newContractList);
        Set<String> newSelectedContractIdSet = new HashSet<>();
        for (ContractField contract : contractObservableList) {
            if (selectedContractUnifiedSymbolSet.contains(contract.getUnifiedSymbol())) {
                contractTableView.getSelectionModel().select(contract);
                newSelectedContractIdSet.add(contract.getUnifiedSymbol());
            }
        }
        selectedContractUnifiedSymbolSet = newSelectedContractIdSet;
    }

    private void createLayout() {

        contractTableView.setTableMenuButtonVisible(true);

        TableColumn<ContractField, String> unifiedSymbolCol = new TableColumn<>("统一标识");
        unifiedSymbolCol.setPrefWidth(160);
        unifiedSymbolCol.setCellValueFactory(feature -> {
            String unifiedSymbol = "";
            try {
                unifiedSymbol = feature.getValue().getUnifiedSymbol();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(unifiedSymbol);
        });
        unifiedSymbolCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(unifiedSymbolCol);

        TableColumn<ContractField, String> shortNameCol = new TableColumn<>("简称");
        shortNameCol.setPrefWidth(80);
        shortNameCol.setCellValueFactory(feature -> {
            String shortName = "";
            try {
                shortName = feature.getValue().getName();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(shortName);
        });
        shortNameCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(shortNameCol);

        TableColumn<ContractField, String> fullNameCol = new TableColumn<>("完整名称");
        fullNameCol.setPrefWidth(100);
        fullNameCol.setCellValueFactory(feature -> {
            String fullName = "";
            try {
                fullName = feature.getValue().getFullName();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(fullName);
        });
        fullNameCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(fullNameCol);

        TableColumn<ContractField, String> symbolCol = new TableColumn<>("代码");
        symbolCol.setPrefWidth(60);
        symbolCol.setCellValueFactory(feature -> {
            String symbol = "";
            try {
                symbol = feature.getValue().getSymbol();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(symbol);
        });
        symbolCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(symbolCol);

        TableColumn<ContractField, String> exchangeCol = new TableColumn<>("交易所");
        exchangeCol.setPrefWidth(60);
        exchangeCol.setCellValueFactory(feature -> {
            String exchange = "";
            try {
                exchange = feature.getValue().getExchange().getValueDescriptor().getName();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(exchange);
        });
        exchangeCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(exchangeCol);

        TableColumn<ContractField, String> productTypeCol = new TableColumn<>("产品类型");
        productTypeCol.setPrefWidth(70);
        productTypeCol.setCellValueFactory(feature -> {
            String productType = "";
            try {
                productType = feature.getValue().getProductClass().getValueDescriptor().getName();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(productType);
        });
        productTypeCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(productTypeCol);

        TableColumn<ContractField, String> lastTradeDateOrContractMonthCol = new TableColumn<>("合约月或最后交易日");
        lastTradeDateOrContractMonthCol.setPrefWidth(100);
        lastTradeDateOrContractMonthCol.setCellValueFactory(feature -> {
            String lastTradeDateOrContractMonth = "";
            try {
                lastTradeDateOrContractMonth = feature.getValue().getLastTradeDateOrContractMonth();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(lastTradeDateOrContractMonth);
        });
        lastTradeDateOrContractMonthCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(lastTradeDateOrContractMonthCol);

        TableColumn<ContractField, String> currencyCol = new TableColumn<>("币种");
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
        currencyCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(currencyCol);

        TableColumn<ContractField, String> thirdPartyIdCol = new TableColumn<>("第三方ID");
        thirdPartyIdCol.setPrefWidth(100);
        thirdPartyIdCol.setCellValueFactory(feature -> {
            String thirdPartyId = "";
            try {
                thirdPartyId = feature.getValue().getThirdPartyId();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(thirdPartyId);
        });
        thirdPartyIdCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(thirdPartyIdCol);

        TableColumn<ContractField, Double> multiplierCol = new TableColumn<>("合约乘数");
        multiplierCol.setPrefWidth(40);
        multiplierCol.setCellValueFactory(feature -> {
            double multiplier = 1.0;
            try {
                multiplier = feature.getValue().getMultiplier();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleDoubleProperty(multiplier).asObject();
        });
        multiplierCol.setComparator(Comparator.comparingDouble((Double d) -> d));
        contractTableView.getColumns().add(multiplierCol);

        TableColumn<ContractField, Double> priceTickCol = new TableColumn<>("最小变动价位");
        priceTickCol.setPrefWidth(40);
        priceTickCol.setCellValueFactory(feature -> {
            double priceTick = 0.001;
            try {
                priceTick = feature.getValue().getPriceTick();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleDoubleProperty(priceTick).asObject();
        });
        priceTickCol.setComparator(Double::compare);
        contractTableView.getColumns().add(priceTickCol);

        TableColumn<ContractField, String> optionTypeCol = new TableColumn<>("期权类型");
        optionTypeCol.setPrefWidth(100);
        optionTypeCol.setCellValueFactory(feature -> {
            String optionType = "";
            try {
                optionType = feature.getValue().getOptionsType().getValueDescriptor().getName();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(optionType);
        });
        optionTypeCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(optionTypeCol);

        TableColumn<ContractField, String> underlyingSymbolCol = new TableColumn<>("基础商品代码");
        underlyingSymbolCol.setPrefWidth(80);
        underlyingSymbolCol.setCellValueFactory(feature -> {
            String underlyingSymbol = "";
            try {
                underlyingSymbol = feature.getValue().getUnderlyingSymbol();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleStringProperty(underlyingSymbol);
        });
        underlyingSymbolCol.setComparator(StringUtils::compare);
        contractTableView.getColumns().add(underlyingSymbolCol);

        TableColumn<ContractField, Double> underlyingMultiplierCol = new TableColumn<>("基础商品乘数");
        underlyingMultiplierCol.setPrefWidth(40);
        underlyingMultiplierCol.setCellValueFactory(feature -> {
            double underlyingMultiplier = 1.0;
            try {
                underlyingMultiplier = feature.getValue().getUnderlyingMultiplier();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleDoubleProperty(underlyingMultiplier).asObject();
        });
        underlyingMultiplierCol.setComparator(Double::compare);
        contractTableView.getColumns().add(underlyingMultiplierCol);

        TableColumn<ContractField, Double> strikePriceCol = new TableColumn<>("执行价");
        strikePriceCol.setPrefWidth(80);
        strikePriceCol.setCellValueFactory(feature -> {
            double strikePrice = 1.0;
            try {
                strikePrice = feature.getValue().getStrikePrice();
            } catch (Exception e) {
                logger.error("渲染错误", e);
            }
            return new SimpleDoubleProperty(strikePrice).asObject();
        });
        strikePriceCol.setComparator(Double::compare);
        contractTableView.getColumns().add(strikePriceCol);

        SortedList<ContractField> sortedItems = new SortedList<>(contractObservableList);
        contractTableView.setItems(sortedItems);
        sortedItems.comparatorProperty().bind(contractTableView.comparatorProperty());

        contractTableView.getSortOrder().add(unifiedSymbolCol);

        contractTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        contractTableView.setOnMousePressed(event -> {
            ObservableList<ContractField> selectedItems = contractTableView.getSelectionModel().getSelectedItems();
            selectedContractUnifiedSymbolSet.clear();
            for (ContractField row : selectedItems) {
                selectedContractUnifiedSymbolSet.add(row.getUnifiedSymbol());
            }
        });

        contractTableView.setRowFactory(tv -> {
            TableRow<ContractField> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    ObservableList<ContractField> selectedItems = contractTableView.getSelectionModel().getSelectedItems();
                    selectedContractUnifiedSymbolSet.clear();
                    for (ContractField contract : selectedItems) {
                        selectedContractUnifiedSymbolSet.add(contract.getUnifiedSymbol());
                    }
                    ContractField clickedItem = row.getItem();
                    guiMainService.updateSelectedContract(clickedItem);
                }
            });
            return row;
        });

        contractTableView.setFocusTraversable(false);

        HBox filterHBox = new HBox();
        VBox filterCol1VBox = new VBox();
        VBox filterCol2VBox = new VBox();
        VBox filterCol3VBox = new VBox();
        Insets commonInsets = new Insets(5);
        filterCol1VBox.setPadding(commonInsets);
        filterCol2VBox.setPadding(commonInsets);
        filterCol3VBox.setPadding(commonInsets);
        filterHBox.getChildren().addAll(filterCol1VBox, filterCol2VBox, filterCol3VBox);

        ComboBox<String> exchangeComboBox = new ComboBox<>();
        exchangeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterExchange = newValue;
            render();
        });
        ObservableList<String> exchangeObservableList = FXCollections.observableArrayList();
        exchangeObservableList.add("全部");
        for (int i = 0; i < ExchangeEnum.values().length - 1; i++) {
            ExchangeEnum exchange = ExchangeEnum.values()[i];
            exchangeObservableList.add(exchange.getValueDescriptor().getName());
        }
        exchangeComboBox.setItems(exchangeObservableList);
        exchangeComboBox.getSelectionModel().selectFirst();
        filterCol1VBox.getChildren().addAll(new Label("交易所"), exchangeComboBox);

        TextField fuzzySymbolTextField = new TextField();
        fuzzySymbolTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFuzzySymbol = newValue;
            render();
        });
        filterCol1VBox.getChildren().addAll(new Label("代码"), fuzzySymbolTextField);

        TextField underlyingSymbolTextField = new TextField();
        underlyingSymbolTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUnderlyingSymbol = newValue;
            render();
        });
        filterCol1VBox.getChildren().addAll(new Label("基础商品代码"), underlyingSymbolTextField);

        ComboBox<String> productTypeComboBox = new ComboBox<>();
        productTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterProductType = newValue;
            render();
        });
        ObservableList<String> productTypeObservableList = FXCollections.observableArrayList();
        productTypeObservableList.add("全部");
        for (int i = 0; i < ProductClassEnum.values().length - 1; i++) {
            ProductClassEnum productType = ProductClassEnum.values()[i];
            productTypeObservableList.add(productType.getValueDescriptor().getName());
        }
        productTypeComboBox.setItems(productTypeObservableList);
        productTypeComboBox.getSelectionModel().selectFirst();
        filterCol2VBox.getChildren().addAll(new Label("产品类型"), productTypeComboBox);

        TextField lastTradeDateOrContractMonthTextField = new TextField();
        lastTradeDateOrContractMonthTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLastTradeDateOrContractMonth = newValue;
            render();
        });
        filterCol2VBox.getChildren().addAll(new Label("最后交易日或合约月"), lastTradeDateOrContractMonthTextField);

        TextField nameTextField = new TextField();
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterName = newValue;
            render();
        });
        filterCol2VBox.getChildren().addAll(new Label("名称"), nameTextField);

        ComboBox<String> currencyComboBox = new ComboBox<>();
        currencyComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterCurrency = newValue;
            render();
        });
        ObservableList<String> currencyObservableList = FXCollections.observableArrayList();
        currencyObservableList.add("全部");
        for (int i = 0; i < CurrencyEnum.values().length - 1; i++) {
            CurrencyEnum currency = CurrencyEnum.values()[i];
            currencyObservableList.add(currency.getValueDescriptor().getName());
        }
        currencyComboBox.setItems(currencyObservableList);
        currencyComboBox.getSelectionModel().selectFirst();
        filterCol3VBox.getChildren().addAll(new Label("币种"), currencyComboBox);

        TextField thirdPartyIdField = new TextField();
        thirdPartyIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterThirdPartyId = newValue;
            render();
        });
        filterCol3VBox.getChildren().addAll(new Label("第三方ID"), thirdPartyIdField);

        Button searchButton = new Button("搜寻");
        Button refreshButton = new Button("刷新");
        refreshButton.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                guiMainService.refreshContractData();
            }
        });

        filterCol3VBox.getChildren().addAll(searchButton, refreshButton);

        vBox.getChildren().add(filterHBox);
        vBox.getChildren().add(contractTableView);
        VBox.setVgrow(contractTableView, Priority.ALWAYS);
        vBox.setMinWidth(1);

//        HBox hBox = new HBox();
//        vBox.getChildren().add(hBox);

    }

}
