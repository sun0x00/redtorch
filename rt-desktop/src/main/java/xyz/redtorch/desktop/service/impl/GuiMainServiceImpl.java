package xyz.redtorch.desktop.service.impl;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.desktop.layout.base.*;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GuiMainServiceImpl implements GuiMainService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(GuiMainServiceImpl.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Set<String> selectedAccountIdSet = new HashSet<>();

    private ContractField selectedContract = null;

    @Autowired
    private DesktopTradeCachesService desktopTradeCachesService;
    @Autowired
    private PositionLayout positionLayout;
    @Autowired
    private AccountLayout accountLayout;
    @Autowired
    private TradeLayout tradeLayout;
    @Autowired
    private OrderLayout orderLayout;
    @Autowired
    private CombinationLayout combinationLayout;
    @Autowired
    private MarketDetailsLayout marketDetailsLayout;
    @Autowired
    private OrderPanelLayout orderPanelLayout;
    @Autowired
    private TickLayout tickLayout;
    @Autowired
    private ContractLayout contractLayout;
    @Autowired
    private RpcClientApiService rpcClientApiService;

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(250);
                        Platform.runLater(() -> {

                            List<OrderField> orderList = desktopTradeCachesService.getOrderList();
                            orderLayout.updateData(orderList);

                            List<TradeField> tradeList = desktopTradeCachesService.getTradeList();
                            tradeLayout.updateData(tradeList);

                            List<PositionField> positionList = desktopTradeCachesService.getPositionList();
                            positionLayout.updateData(positionList);

                            List<AccountField> accountList = desktopTradeCachesService.getAccountList();
                            accountLayout.updateData(accountList);

                            if (selectedContract != null) {
                                TickField tick = desktopTradeCachesService.queryTickByUniformSymbol(selectedContract.getUniformSymbol());
                                marketDetailsLayout.updateData(tick);
                                orderPanelLayout.updateData(tick);
                            } else {
                                marketDetailsLayout.updateData(null);
                                orderPanelLayout.updateData(null);
                            }

                            combinationLayout.updateData(positionList, accountList);

                            List<TickField> mixTickList = desktopTradeCachesService.getMixTickList();
                            tickLayout.updateData(mixTickList);

                            List<ContractField> mixContractList = desktopTradeCachesService.getMixContractList();
                            contractLayout.updateData(mixContractList);

                        });
                    } catch (Exception e) {
                        logger.error("刷新错误", e);
                    }
                }
            }
        });
    }

    @Override
    public ContractField getSelectedContract() {
        return this.selectedContract;
    }

    @Override
    public void updateSelectedContract(ContractField contract) {

        if (!(this.selectedContract != null && contract != null && this.selectedContract.getUniformSymbol().equals(contract.getUniformSymbol()))) {
            this.selectedContract = contract;
            if (contract != null) {
                // 订阅合约
                rpcClientApiService.subscribe(contract, UUIDStringPoolUtils.getUUIDString(), null);
                // 更新缓存
                RpcGetTickListRsp rpcGetTickListRsp = rpcClientApiService.getTickList(UUIDStringPoolUtils.getUUIDString(), null);
                if (rpcGetTickListRsp != null && rpcGetTickListRsp.getCommonRsp().getErrorId() == 0) {
                    List<TickField> tickList = rpcGetTickListRsp.getTickList();
                    desktopTradeCachesService.cacheTickList(tickList);
                }
                TickField tick = desktopTradeCachesService.queryTickByUniformSymbol(contract.getUniformSymbol());
                marketDetailsLayout.updateData(tick);
                orderPanelLayout.updateData(tick);
            } else {
                marketDetailsLayout.updateData(null);
                orderPanelLayout.updateData(null);
            }
            orderLayout.render();
            tradeLayout.render();
            positionLayout.render();
            tickLayout.render();
        }
    }

    @Override
    public void reloadData() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    desktopTradeCachesService.reloadData();
                } catch (Exception e) {
                    logger.error("重新加载数据异常", e);
                }
            }
        });
    }

    @Override
    public void updateSelectedAccountIdSet(Set<String> selectedAccountIdSet) {
        // 不可以在这个方法中调用accountLayout中的方法,会触发死循环
        if (!this.selectedAccountIdSet.equals(selectedAccountIdSet)) {
            this.selectedAccountIdSet.clear();
            this.selectedAccountIdSet.addAll(selectedAccountIdSet);
            orderLayout.render();
            tradeLayout.render();
            positionLayout.render();
            combinationLayout.render();
            orderPanelLayout.render();
        }

    }

    @Override
    public Set<String> getSelectedAccountIdSet() {
        return selectedAccountIdSet;
    }

    @Override
    public boolean isSelectedAccountId(String accountId) {
        if (accountId == null) {
            return false;
        }
        return selectedAccountIdSet.contains(accountId);
    }

    @Override
    public boolean isSelectedContract(ContractField contractField) {
        if (contractField == null) {
            return false;
        }
        if (selectedContract == null) {
            return false;
        }

        return selectedContract.getUniformSymbol().equals(contractField.getUniformSymbol());
    }

    @Override
    public void refreshContractData() {
        try {
            RpcGetContractListRsp rpcGetContractListRsp = rpcClientApiService.getContractList(null, null);
            if (rpcGetContractListRsp != null) {
                desktopTradeCachesService.clearAndCacheContractList(rpcGetContractListRsp.getContractList());
            }
        } catch (Exception e) {
            logger.error("更新数据错误", e);
        }
    }

    @Override
    public void writeAccountsDataToFile() {
        List<AccountField> accountList = desktopTradeCachesService.getAccountList();

        StringBuilder csvString = new StringBuilder();

        csvString.append("账户代码").append(",");
        csvString.append("持有人").append(",");
        csvString.append("权益");
        csvString.append("\r\n");

        for (AccountField account : accountList) {
            csvString.append(account.getCode()).append(",");
            csvString.append(account.getHolder()).append(",");
            csvString.append(account.getBalance());
            csvString.append("\r\n");
        }

        try (FileWriter fw = new FileWriter("rt-accounts.csv")) {
            fw.write(csvString.toString());
        } catch (IOException e) {
            logger.error("写入账户数据到文件发生错误,", e);
        }

    }
}
