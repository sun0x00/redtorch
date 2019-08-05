package xyz.redtorch.node.master.rpc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.service.FastEventService;
import xyz.redtorch.node.master.rpc.service.RpcServerRtnHandlerService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.node.master.service.MasterTradeRtnRelayService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.CommonRtnField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcAccountListRtn;
import xyz.redtorch.pb.CoreRpc.RpcAccountRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractListRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractRtn;
import xyz.redtorch.pb.CoreRpc.RpcNoticeRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderListRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeRtn;

/**
 * 
 * @author sun0x00@gmail.com
 *
 */
@Service
public class RpcServerRtnHandlerServiceImpl implements RpcServerRtnHandlerService {

	@Autowired
	private MasterTradeCachesService masterTradeCachesService;
	@Autowired
	private MasterTradeRtnRelayService masterTradeRtnRelayService;
	@Autowired
	private FastEventService fastEventService;

	@Override
	public void onOrderRtn(RpcOrderRtn rpcOrderRtn) {
		CommonRtnField commonRtn = rpcOrderRtn.getCommonRtn();
		OrderField order = rpcOrderRtn.getOrder();
		fastEventService.emitOrder(order);
		masterTradeRtnRelayService.onOrder(commonRtn, order);
		masterTradeCachesService.cacheOrder(order);
	}

	@Override
	public void onTradeRtn(RpcTradeRtn rpcTradeRtn) {
		CommonRtnField commonRtn = rpcTradeRtn.getCommonRtn();
		TradeField trade = rpcTradeRtn.getTrade();
		fastEventService.emitTrade(trade);
		masterTradeRtnRelayService.onTrade(commonRtn, trade);
		masterTradeCachesService.cacheTrade(trade);
	}

	@Override
	public void onContractRtn(RpcContractRtn rpcContractRtn) {
		ContractField contract = rpcContractRtn.getContract();
		masterTradeCachesService.cacheContract(contract);
	}

	@Override
	public void onPositionRtn(RpcPositionRtn rpcPositionRtn) {
		CommonRtnField commonRtn = rpcPositionRtn.getCommonRtn();
		PositionField position = rpcPositionRtn.getPosition();
		masterTradeRtnRelayService.onPosition(commonRtn, position);
		masterTradeCachesService.cachePosition(position);
	}

	@Override
	public void onAccountRtn(RpcAccountRtn rpcAccountRtn) {
		CommonRtnField commonRtn = rpcAccountRtn.getCommonRtn();
		AccountField account = rpcAccountRtn.getAccount();
		masterTradeRtnRelayService.onAccount(commonRtn, account);
		masterTradeCachesService.cacheAccount(account);
	}

	@Override
	public void onTickRtn(RpcTickRtn rpcTickRtn) {
		CommonRtnField commonRtn = rpcTickRtn.getCommonRtn();
		TickField tick = rpcTickRtn.getTick();
		fastEventService.emitTick(tick);
		masterTradeRtnRelayService.onTick(commonRtn, tick);
		masterTradeCachesService.cacheTick(tick);

	}

	@Override
	public void onNoticeRtn(RpcNoticeRtn rpcNoticeRtn) {
		CommonRtnField commonRtn = rpcNoticeRtn.getCommonRtn();
		masterTradeRtnRelayService.onNotice(commonRtn, rpcNoticeRtn.getNotice());

	}

	@Override
	public void onOrderListRtn(RpcOrderListRtn rpcOrderListRtn) {
		List<OrderField> orderList = rpcOrderListRtn.getOrderList();
		int sourceNodeId = rpcOrderListRtn.getCommonRtn().getSourceNodeId();
		masterTradeCachesService.cacheOrderList(orderList, sourceNodeId);
	}

	@Override
	public void onTradeListRtn(RpcTradeListRtn rpcTradeListRtn) {
		List<TradeField> tradeList = rpcTradeListRtn.getTradeList();
		int sourceNodeId = rpcTradeListRtn.getCommonRtn().getSourceNodeId();
		masterTradeCachesService.cacheTradeList(tradeList, sourceNodeId);
	}

	@Override
	public void onContractListRtn(RpcContractListRtn rpcContractListRtn) {
		List<ContractField> contractList = rpcContractListRtn.getContractList();
		int sourceNodeId = rpcContractListRtn.getCommonRtn().getSourceNodeId();
		masterTradeCachesService.cacheContractList(contractList, sourceNodeId);

	}

	@Override
	public void onPositionListRtn(RpcPositionListRtn rpcPositionListRtn) {
		List<PositionField> positionList = rpcPositionListRtn.getPositionList();
		int sourceNodeId = rpcPositionListRtn.getCommonRtn().getSourceNodeId();
		masterTradeCachesService.cachePositionList(positionList, sourceNodeId);
	}

	@Override
	public void onAccountListRtn(RpcAccountListRtn rpcAccountListRtn) {
		List<AccountField> accountList = rpcAccountListRtn.getAccountList();
		int sourceNodeId = rpcAccountListRtn.getCommonRtn().getSourceNodeId();
		masterTradeCachesService.cacheAccountList(accountList, sourceNodeId);

	}

	@Override
	public void onTickListRtn(RpcTickListRtn rpcTickListRtn) {
		List<TickField> tickList = rpcTickListRtn.getTickList();
		int sourceNodeId = rpcTickListRtn.getCommonRtn().getSourceNodeId();
		masterTradeCachesService.cacheTickList(tickList, sourceNodeId);

	}

}
