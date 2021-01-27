package xyz.redtorch.desktop.layout.base;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.redtorch.desktop.layout.base.bean.TickFXBean;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

import java.util.*;

@Component
public class TickLayout {

    private static final Logger logger = LoggerFactory.getLogger(TickLayout.class);

    private final VBox vBox = new VBox();

    private boolean layoutCreated = false;

    private final ObservableList<TickFXBean> tickObservableList = FXCollections.observableArrayList();

    private List<TickField> tickList = new ArrayList<>();

    private final TableView<TickFXBean> tickTableView = new TableView<>();

    private Map<String, TickFXBean> tickFXBeanMap = new HashMap<>();

    private Set<String> selectedTickUniformSymbolSet = new HashSet<>();

    @Autowired
    private GuiMainService guiMainService;

    @Autowired
    private DesktopTradeCachesService desktopTradeCachesService;

    public Node getNode() {
        if (!layoutCreated) {
            createLayout();
            layoutCreated = true;
        }
        return this.vBox;
    }

    public void updateData(List<TickField> tickList) {
        this.tickList = tickList;
        render();
    }

    public void render() {
        tickTableView.getSelectionModel().clearSelection();

        ///
        tickTableView.getSelectionModel().clearSelection();

        Set<String> uniformSymbolSet = new HashSet<>();

        List<TickFXBean> newTickFXBeanList = new ArrayList<>();
        for (TickField tick : tickList) {
            String uniformSymbol = tick.getUniformSymbol();
            uniformSymbolSet.add(uniformSymbol);

            ContractField contractField = desktopTradeCachesService.queryContractByUniformSymbol(uniformSymbol);

            if (tickFXBeanMap.containsKey(uniformSymbol)) {
                tickFXBeanMap.get(uniformSymbol).update(tick, guiMainService.isSelectedContract(contractField), contractField);
            } else {
                TickFXBean tickFXBean = new TickFXBean(tick, guiMainService.isSelectedContract(contractField), contractField);
                tickFXBeanMap.put(uniformSymbol, tickFXBean);
                newTickFXBeanList.add(tickFXBean);
            }
        }

        tickObservableList.addAll(newTickFXBeanList);

        Map<String, TickFXBean> newTickFXBeanMap = new HashMap<>();
        for (String uniformSymbol : tickFXBeanMap.keySet()) {
            if (uniformSymbolSet.contains(uniformSymbol)) {
                newTickFXBeanMap.put(uniformSymbol, tickFXBeanMap.get(uniformSymbol));
            }
        }
        tickFXBeanMap = newTickFXBeanMap;

        tickObservableList.removeIf(tickFXBean -> !uniformSymbolSet.contains(tickFXBean.getTickField().getUniformSymbol()));

        tickTableView.sort();

        ///

        Set<String> newSelectedTickIdSet = new HashSet<>();
        for (TickFXBean tick : tickObservableList) {
            if (selectedTickUniformSymbolSet.contains(tick.getTickField().getUniformSymbol())) {
                tickTableView.getSelectionModel().select(tick);
                newSelectedTickIdSet.add(tick.getTickField().getUniformSymbol());
            }
        }
        selectedTickUniformSymbolSet = newSelectedTickIdSet;
    }

    private void createLayout() {

        tickTableView.setTableMenuButtonVisible(true);

        TableColumn<TickFXBean, Pane> contractCol = new TableColumn<>("合约");
        contractCol.setPrefWidth(160);
        contractCol.setCellValueFactory(new PropertyValueFactory<>("contract"));

        contractCol.setComparator((Pane p1, Pane p2) -> {
            try {
                TickField tick1 = (TickField) p1.getUserData();
                TickField tick2 = (TickField) p2.getUserData();
                return StringUtils.compare(tick1.getUniformSymbol(), tick2.getUniformSymbol());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });

        tickTableView.getColumns().add(contractCol);

        TableColumn<TickFXBean, Pane> lastPriceCol = new TableColumn<>("最新价格");
        lastPriceCol.setPrefWidth(120);
        lastPriceCol.setCellValueFactory(new PropertyValueFactory<>("lastPrice"));

        lastPriceCol.setComparator((Pane p1, Pane p2) -> {
            try {
                TickField tick1 = (TickField) p1.getUserData();
                TickField tick2 = (TickField) p2.getUserData();
                return Double.compare(tick1.getLastPrice(), tick2.getLastPrice());
            } catch (Exception e) {
                logger.error("排序错误", e);
            }
            return 0;
        });

        tickTableView.getColumns().add(lastPriceCol);

        TableColumn<TickFXBean, Pane> abPriceCol = new TableColumn<>("买卖一价");
        abPriceCol.setPrefWidth(70);
        abPriceCol.setCellValueFactory(new PropertyValueFactory<>("abPrice"));
        abPriceCol.setSortable(false);
        tickTableView.getColumns().add(abPriceCol);

        TableColumn<TickFXBean, Pane> abVolumeCol = new TableColumn<>("买卖一量");
        abVolumeCol.setPrefWidth(70);
        abVolumeCol.setCellValueFactory(new PropertyValueFactory<>("abVolume"));
        abVolumeCol.setSortable(false);
        tickTableView.getColumns().add(abVolumeCol);

        TableColumn<TickFXBean, Pane> volumeCol = new TableColumn<>("成交量");
        volumeCol.setPrefWidth(70);
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));
        volumeCol.setSortable(false);
        tickTableView.getColumns().add(volumeCol);

        TableColumn<TickFXBean, Pane> openInterestCol = new TableColumn<>("持仓量");
        openInterestCol.setPrefWidth(70);
        openInterestCol.setCellValueFactory(new PropertyValueFactory<>("openInterest"));
        openInterestCol.setSortable(false);
        tickTableView.getColumns().add(openInterestCol);

        TableColumn<TickFXBean, Pane> limitPriceCol = new TableColumn<>("涨跌停");
        limitPriceCol.setPrefWidth(70);
        limitPriceCol.setCellValueFactory(new PropertyValueFactory<>("limit"));
        limitPriceCol.setSortable(false);
        tickTableView.getColumns().add(limitPriceCol);

        TableColumn<TickFXBean, Pane> actionTimeCol = new TableColumn<>("时间");
        actionTimeCol.setPrefWidth(90);
        actionTimeCol.setCellValueFactory(new PropertyValueFactory<>("actionTime"));
        actionTimeCol.setSortable(false);
        tickTableView.getColumns().add(actionTimeCol);

        SortedList<TickFXBean> sortedItems = new SortedList<>(tickObservableList);
        tickTableView.setItems(sortedItems);
        sortedItems.comparatorProperty().bind(tickTableView.comparatorProperty());

        tickTableView.getSortOrder().add(contractCol);

        tickTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tickTableView.setOnMousePressed(event -> {
            ObservableList<TickFXBean> selectedItems = tickTableView.getSelectionModel().getSelectedItems();
            selectedTickUniformSymbolSet.clear();
            for (TickFXBean row : selectedItems) {
                selectedTickUniformSymbolSet.add(row.getTickField().getUniformSymbol());
            }
        });

        tickTableView.setRowFactory(tv -> {
            TableRow<TickFXBean> row = new TableRow<>();
            row.setOnMousePressed(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    ObservableList<TickFXBean> selectedItems = tickTableView.getSelectionModel().getSelectedItems();
                    selectedTickUniformSymbolSet.clear();
                    for (TickFXBean tick : selectedItems) {
                        selectedTickUniformSymbolSet.add(tick.getTickField().getUniformSymbol());
                    }
                    TickFXBean clickedItem = row.getItem();

                    ContractField contract = desktopTradeCachesService.queryContractByUniformSymbol(clickedItem.getTickField().getUniformSymbol());

                    guiMainService.updateSelectedContract(contract);
                }
            });
            return row;
        });

        tickTableView.setFocusTraversable(false);

        vBox.getChildren().add(tickTableView);
        VBox.setVgrow(tickTableView, Priority.ALWAYS);
        vBox.setMinWidth(1);

//        HBox hBox = new HBox();
//        vBox.getChildren().add(hBox);

    }

}
