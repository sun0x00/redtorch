package xyz.redtorch.desktop.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javafx.application.Platform;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.desktop.layout.base.AccountLayout;
import xyz.redtorch.desktop.layout.base.CombinationLayout;
import xyz.redtorch.desktop.layout.base.ContractLayout;
import xyz.redtorch.desktop.layout.base.MarketDetailsLayout;
import xyz.redtorch.desktop.layout.base.OrderLayout;
import xyz.redtorch.desktop.layout.base.OrderPanelLayout;
import xyz.redtorch.desktop.layout.base.PositionLayout;
import xyz.redtorch.desktop.layout.base.TickLayout;
import xyz.redtorch.desktop.layout.base.TradeLayout;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.GuiMainService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;

@Service
public class GuiMainServiceImpl implements GuiMainService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(GuiMainServiceImpl.class);

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private Set<String> selectedAccountIdSet = new HashSet<>();

	private ContractField selectedContract = null;

	@Autowired
	private DesktopTradeCachesService desktopTradeCachesService;
//	@Autowired
//	private MainLayout mainLayout;
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
						Thread.sleep(200);
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
								TickField tick = desktopTradeCachesService.queryTickByUnifiedSymbol(selectedContract.getUnifiedSymbol());
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
	public void updateSelectedContarct(ContractField contract) {

		if (!(this.selectedContract != null && contract != null && this.selectedContract.getUnifiedSymbol().equals(contract.getUnifiedSymbol()))) {
			this.selectedContract = contract;
			if (contract != null) {
				// 订阅合约
				rpcClientApiService.subscribe(contract, UUIDStringPoolUtils.getUUIDString(), null);
				// 更新缓存
				RpcGetTickListRsp rpcGetTickListRsp = rpcClientApiService.getTickList(UUIDStringPoolUtils.getUUIDString(), null);
				if (rpcGetTickListRsp != null && rpcGetTickListRsp.getCommonRsp() != null && rpcGetTickListRsp.getCommonRsp().getErrorId() == 0) {
					List<TickField> tickList = rpcGetTickListRsp.getTickList();
					if (tickList != null) {
						desktopTradeCachesService.cacheTickList(tickList);
					}
				}
				TickField tick = desktopTradeCachesService.queryTickByUnifiedSymbol(contract.getUnifiedSymbol());
				marketDetailsLayout.updateData(tick);
				orderPanelLayout.updateData(tick);
			} else {
				marketDetailsLayout.updateData(null);
				orderPanelLayout.updateData(null);
			}
			orderLayout.fillingData();
			tradeLayout.fillingData();
			positionLayout.fillingData();
			tickLayout.fillingData();
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
			orderLayout.fillingData();
			tradeLayout.fillingData();
			positionLayout.fillingData();
			combinationLayout.fillingData();
			orderPanelLayout.fillingData();
		}

	}

	@Override
	public Set<String> getSelectedAccountIdSet() {
		return selectedAccountIdSet;
	}

	@Override
	public void refreshContractData() {
		try {
			RpcGetContractListRsp rpcGetContractListRsp = rpcClientApiService.getContractList(null, null);
			if (rpcGetContractListRsp != null && rpcGetContractListRsp.getContractList() != null) {
				desktopTradeCachesService.clearAndCacheContractList(rpcGetContractListRsp.getContractList());
			}
		} catch (Exception e) {
			logger.error("更新数据错误", e);
		}
	}
}
