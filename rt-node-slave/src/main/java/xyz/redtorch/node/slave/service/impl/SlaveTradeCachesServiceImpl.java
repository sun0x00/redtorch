package xyz.redtorch.node.slave.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.service.FastEventService;
import xyz.redtorch.common.service.FastEventService.FastEvent;
import xyz.redtorch.common.service.FastEventService.FastEventDynamicHandlerAbstract;
import xyz.redtorch.common.service.FastEventService.FastEventType;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.node.slave.service.SlaveSystemService;
import xyz.redtorch.node.slave.service.SlaveTradeCachesService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.NoticeField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcAccountRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractListRtn;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcNoticeRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeRtn;

@Service
public class SlaveTradeCachesServiceImpl extends FastEventDynamicHandlerAbstract implements SlaveTradeCachesService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(SlaveTradeCachesServiceImpl.class);

	@Autowired
	private FastEventService fastEventService;
	@Autowired
	private SlaveSystemService slaveSystemService;

	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private Map<String, ContractField> contractMap = new HashMap<>(10000);
	private Map<String, TickField> tickMap = new HashMap<>(1000);
	private Map<String, OrderField> orderMap = new HashMap<>(50000);
	private Map<String, TradeField> tradeMap = new HashMap<>(100000);
	private Map<String, AccountField> accountMap = new HashMap<>(500);
	private Map<String, PositionField> positionMap = new HashMap<>(5000);

	private Lock contractMapLock = new ReentrantLock();
	private Lock tickMapLock = new ReentrantLock();
	private Lock orderMapLock = new ReentrantLock();
	private Lock tradeMapLock = new ReentrantLock();
	private Lock accountMapLock = new ReentrantLock();
	private Lock positionMapLock = new ReentrantLock();

	// 对时效性要求不高的数据使用Queue减少发送次数,减轻中心节点压力
	private Queue<PositionField> positionQueue = new ConcurrentLinkedQueue<>();
	private Queue<ContractField> contractQueue = new ConcurrentLinkedQueue<>();

	@Autowired
	private RpcClientProcessService rpcClientProcessService;

	@Override
	public void afterPropertiesSet() throws Exception {
		fastEventService.addHandler(this);

		subscribeFastEventType(FastEventType.ACCOUNT);
		subscribeFastEventType(FastEventType.CONTRACT);
		subscribeFastEventType(FastEventType.ORDER);
		subscribeFastEventType(FastEventType.POSITION);
		subscribeFastEventType(FastEventType.TICK);
		subscribeFastEventType(FastEventType.TRADE);
		subscribeFastEventType(FastEventType.LOG);
		subscribeFastEventType(FastEventType.NOTICE);

		// 定时清理缓存任务
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {

				List<String> gatewayIdList = slaveSystemService.getConnectedGatewayIdList();
				Set<String> connectedGatewayIdSet = new HashSet<>(gatewayIdList);

				accountMapLock.lock();
				try {
					// 删除账户缓存
					accountMap = accountMap.entrySet().stream().filter(map -> connectedGatewayIdSet.contains(map.getValue().getGatewayId()))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				} catch (Exception e) {
					logger.error("删除账户缓存异常", e);
				} finally {
					accountMapLock.unlock();
				}

				positionMapLock.lock();
				try {
					// 删除持仓缓存
					positionMap = positionMap.entrySet().stream().filter(map -> connectedGatewayIdSet.contains(map.getValue().getGatewayId()))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				} catch (Exception e) {
					logger.error("删除持仓缓存异常", e);
				} finally {
					positionMapLock.unlock();
				}

				orderMapLock.lock();
				try {
					// 删除定单缓存
					orderMap = orderMap.entrySet().stream().filter(map -> connectedGatewayIdSet.contains(map.getValue().getGatewayId()))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				} catch (Exception e) {
					logger.error("删除定单缓存异常", e);
				} finally {
					orderMapLock.unlock();
				}

				tradeMapLock.lock();
				try {
					// 删除成交缓存
					tradeMap = tradeMap.entrySet().stream().filter(map -> connectedGatewayIdSet.contains(map.getValue().getGatewayId()))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				} catch (Exception e) {
					logger.error("删除成交缓存异常", e);
				} finally {
					tradeMapLock.unlock();
				}

				tickMapLock.lock();
				try {
					// 删除Tick缓存
					tickMap = tickMap.entrySet().stream().filter(map -> connectedGatewayIdSet.contains(map.getValue().getGatewayId()))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				} catch (Exception e) {
					logger.error("删除Tick缓存异常", e);
				} finally {
					tickMapLock.unlock();
				}

				contractMapLock.lock();
				try {
					// 删除Contract缓存
					contractMap = contractMap.entrySet().stream().filter(map -> connectedGatewayIdSet.contains(map.getValue().getGatewayId()))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				} catch (Exception e) {
					logger.error("删除合约缓存异常", e);
				} finally {
					contractMapLock.unlock();
				}

			} catch (Exception e) {
				logger.error("定时清理数据异常", e);
			}
		}, 3, 3, TimeUnit.SECONDS);

		executorService.execute(new Runnable() {
			long lastTimestamp = System.currentTimeMillis();
			List<ContractField> contractFieldList = new ArrayList<>();

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if (contractFieldList.size() > 10000 || (System.currentTimeMillis() - lastTimestamp > 1000 && !contractFieldList.isEmpty())) {

							RpcContractListRtn.Builder rpcContractListRtnBuilder = RpcContractListRtn.newBuilder();
							rpcContractListRtnBuilder.addAllContract(contractFieldList);
							rpcClientProcessService.sendCoreRpc(0, rpcContractListRtnBuilder.build().toByteString(), "", RpcId.CONTRACT_LIST_RTN);
							lastTimestamp = System.currentTimeMillis();
							contractFieldList = new ArrayList<>();
						}

						if (contractQueue.peek() != null) {
							contractFieldList.add(contractQueue.poll());
						} else {
							Thread.sleep(5);
						}

					} catch (Exception e) {
						logger.error("定时发送合约列表线程异常", e);
					}
				}
			}
		});

		executorService.execute(new Runnable() {
			long lastTimestamp = System.currentTimeMillis();
			List<PositionField> positionList = new ArrayList<>();

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if (positionList.size() > 10 || (System.currentTimeMillis() - lastTimestamp > 100 && !positionList.isEmpty())) {

							RpcPositionListRtn.Builder rpcPositionListRtnBuilder = RpcPositionListRtn.newBuilder();
							rpcPositionListRtnBuilder.addAllPosition(positionList);
							rpcClientProcessService.sendCoreRpc(0, rpcPositionListRtnBuilder.build().toByteString(), "", RpcId.POSITION_LIST_RTN);
							lastTimestamp = System.currentTimeMillis();
							positionList = new ArrayList<>();
						}

						if (positionQueue.peek() != null) {
							positionList.add(positionQueue.poll());
						} else {
							Thread.sleep(5);
						}

					} catch (Exception e) {
						logger.error("定时发送持仓列表线程异常", e);
					}
				}
			}
		});

	}

	@Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {

		if (!subscribedFastEventTypeSet.contains(fastEvent.getFastEventType())) {
			return;
		}

		// 判断消息类型
		if (FastEventType.TICK.equals(fastEvent.getFastEventType())) {
			try {
				TickField tick = (TickField) fastEvent.getObj();
				onTick(tick);
			} catch (Exception e) {
				logger.error("onTick发生异常", e);
			}
		} else if (FastEventType.TRADE.equals(fastEvent.getFastEventType())) {
			try {
				TradeField trade = (TradeField) fastEvent.getObj();
				onTrade(trade);
			} catch (Exception e) {
				logger.error("onTrade发生异常", e);
			}
		} else if (FastEventType.ORDER.equals(fastEvent.getFastEventType())) {
			try {
				OrderField order = (OrderField) fastEvent.getObj();
				onOrder(order);
			} catch (Exception e) {
				logger.error("onOrder发生异常", e);
			}
		} else if (FastEventType.CONTRACT.equals(fastEvent.getFastEventType())) {
			try {
				ContractField contract = (ContractField) fastEvent.getObj();
				onContract(contract);
			} catch (Exception e) {
				logger.error("onContract发生异常", e);
			}
		} else if (FastEventType.POSITION.equals(fastEvent.getFastEventType())) {
			try {
				PositionField position = (PositionField) fastEvent.getObj();
				onPosition(position);
			} catch (Exception e) {
				logger.error("onPosition发生异常", e);
			}
		} else if (FastEventType.ACCOUNT.equals(fastEvent.getFastEventType())) {
			try {
				AccountField account = (AccountField) fastEvent.getObj();
				onAccount(account);
			} catch (Exception e) {
				logger.error("onAccount发生异常", e);
			}
		} else if (FastEventType.LOG.equals(fastEvent.getFastEventType())) {
			// NOP
		} else if (FastEventType.NOTICE.equals(fastEvent.getFastEventType())) {
			try {
				NoticeField notice = (NoticeField) fastEvent.getObj();
				onNotice(notice);
			} catch (Exception e) {
				logger.error("onNotice发生异常", e);
			}
		} else {
			logger.warn("未能识别的事件数据类型{}", fastEvent.getEvent());
		}
	}

	private void onNotice(NoticeField notice) {
		RpcNoticeRtn.Builder rpcNoticeRtnBuilder = RpcNoticeRtn.newBuilder();
		rpcNoticeRtnBuilder.setNotice(notice);
		rpcClientProcessService.sendCoreRpc(0, rpcNoticeRtnBuilder.build().toByteString(), "", RpcId.NOTICE_RTN);
	}

	private void onAccount(AccountField account) {

		RpcAccountRtn.Builder rpcAccountRtnBuilder = RpcAccountRtn.newBuilder();
		rpcAccountRtnBuilder.setAccount(account);
		rpcClientProcessService.sendCoreRpc(0, rpcAccountRtnBuilder.build().toByteString(), "", RpcId.ACCOUNT_RTN);

		accountMapLock.lock();
		try {
			accountMap.put(account.getAccountId(), account);
		} catch (Exception e) {
			logger.error("存储账户异常", e);
		} finally {
			accountMapLock.unlock();
		}
	}

	private void onOrder(OrderField order) {

		RpcOrderRtn.Builder rpcOrderRtnBuilder = RpcOrderRtn.newBuilder();
		rpcOrderRtnBuilder.setOrder(order);
		rpcClientProcessService.sendCoreRpc(0, rpcOrderRtnBuilder.build().toByteString(), "", RpcId.ORDER_RTN);

		orderMapLock.lock();
		try {
			String orderId = order.getOrderId();
			if (!orderMap.containsKey(orderId) || !CommonConstant.ORDER_STATUS_FINISHED_SET.contains(orderMap.get(orderId).getOrderStatus())) {
				orderMap.put(order.getOrderId(), order);
			}
		} catch (Exception e) {
			logger.error("存储定单异常", e);
		} finally {
			orderMapLock.unlock();
		}
	}

	private void onTrade(TradeField trade) {

		RpcTradeRtn.Builder rpcTradeRtnBuilder = RpcTradeRtn.newBuilder();
		rpcTradeRtnBuilder.setTrade(trade);
		rpcClientProcessService.sendCoreRpc(0, rpcTradeRtnBuilder.build().toByteString(), "", RpcId.TRADE_RTN);

		tradeMapLock.lock();
		try {
			tradeMap.put(trade.getTradeId(), trade);
		} catch (Exception e) {
			logger.error("存储成交异常", e);
		} finally {
			tradeMapLock.unlock();
		}
	}

	private void onPosition(PositionField position) {

//		RpcPositionRtn.Builder rpcPositionRtnBuilder = RpcPositionRtn.newBuilder();
//		rpcPositionRtnBuilder.setCommonRtn(commonRtn);
//		rpcPositionRtnBuilder.setPosition(position);
//		rpcClientProcessService.sendCoreRpc(0, rpcPositionRtnBuilder.build().toByteString(), "",
//				RpcId.POSITION_RTN);

		positionQueue.add(position);

		positionMapLock.lock();
		try {
			positionMap.put(position.getPositionId(), position);
		} catch (Exception e) {
			logger.error("存储持仓异常", e);
		} finally {
			positionMapLock.unlock();
		}
	}

	private void onTick(TickField tick) {

		RpcTickRtn.Builder rpcTickRtnBuilder = RpcTickRtn.newBuilder();
		rpcTickRtnBuilder.setTick(tick);
		rpcClientProcessService.sendCoreRpc(0, rpcTickRtnBuilder.build().toByteString(), "", RpcId.TICK_RTN);

		tickMapLock.lock();
		try {
			tickMap.put(tick.getUnifiedSymbol() + "@" + tick.getGatewayId(), tick);
		} catch (Exception e) {
			logger.error("存储Tick异常", e);
		} finally {
			tickMapLock.unlock();
		}
	}

	private void onContract(ContractField contract) {

//		RpcContractRtn.Builder rpcContractRtnBuilder = RpcContractRtn.newBuilder();
//		rpcContractRtnBuilder.setCommonRtn(commonRtn);
//		rpcContractRtnBuilder.setContract(contract);
//		rpcClientProcessService.sendCoreRpc(0, rpcContractRtnBuilder.build().toByteString(), "",
//				RpcId.CONTRACT_RTN);

		contractQueue.add(contract);

		contractMapLock.lock();
		try {
			contractMap.put(contract.getContractId(), contract);
		} catch (Exception e) {
			logger.error("存储合约异常", e);
		} finally {
			contractMapLock.unlock();
		}
	}

	@Override
	public void removeAllCachesByGatewayId(String gatewayId) {
		accountMapLock.lock();
		try {
			// 删除账户缓存
			accountMap = accountMap.entrySet().stream().filter(map -> !map.getValue().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} catch (Exception e) {
			logger.error("删除账户缓存异常", e);
		} finally {
			accountMapLock.unlock();
		}

		positionMapLock.lock();
		try {
			// 删除持仓缓存
			positionMap = positionMap.entrySet().stream().filter(map -> !map.getValue().getContract().getGatewayId().equals(gatewayId))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} catch (Exception e) {
			logger.error("删除持仓缓存异常", e);
		} finally {
			positionMapLock.unlock();
		}

		orderMapLock.lock();
		try {
			// 删除定单缓存
			orderMap = orderMap.entrySet().stream().filter(map -> !map.getValue().getContract().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} catch (Exception e) {
			logger.error("删除定单缓存异常", e);
		} finally {
			orderMapLock.unlock();
		}

		tradeMapLock.lock();
		try {
			// 删除成交缓存
			tradeMap = tradeMap.entrySet().stream().filter(map -> !map.getValue().getContract().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} catch (Exception e) {
			logger.error("删除成交缓存异常", e);
		} finally {
			tradeMapLock.unlock();
		}

		tickMapLock.lock();
		try {
			// 删除Tick缓存
			tickMap = tickMap.entrySet().stream().filter(map -> !map.getValue().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} catch (Exception e) {
			logger.error("删除Tick缓存异常", e);
		} finally {
			tickMapLock.unlock();
		}

		contractMapLock.lock();
		try {
			// 删除Contract缓存
			contractMap = contractMap.entrySet().stream().filter(map -> !map.getValue().getGatewayId().equals(gatewayId)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} catch (Exception e) {
			logger.error("删除合约缓存异常", e);
		} finally {
			contractMapLock.unlock();
		}
	}

	@Override
	public OrderField queryOrderByOrderId(String orderId) {
		return orderMap.get(orderId);
	}

	@Override
	public List<OrderField> getOrderList() {
		List<OrderField> orderList = new ArrayList<>();
		orderMapLock.lock();
		try {
			orderList = new ArrayList<OrderField>(orderMap.values());
		} catch (Exception e) {
			logger.error("获取委托列表异常", e);
		} finally {
			orderMapLock.unlock();
		}
		return orderList;
	}

	@Override
	public List<TradeField> getTradeList() {
		List<TradeField> tradeList = new ArrayList<>();
		tradeMapLock.lock();
		try {
			tradeList = new ArrayList<TradeField>(tradeMap.values());
		} catch (Exception e) {
			logger.error("获取成交列表异常", e);
		} finally {
			tradeMapLock.unlock();
		}
		return tradeList;
	}

	@Override
	public List<PositionField> getPositionList() {
		List<PositionField> positionList = new ArrayList<>();
		positionMapLock.lock();
		try {
			positionList = new ArrayList<PositionField>(positionMap.values());
		} catch (Exception e) {
			logger.error("获取持仓列表异常", e);
		} finally {
			positionMapLock.unlock();
		}
		return positionList;
	}

	@Override
	public List<AccountField> getAccountList() {
		List<AccountField> accountList = new ArrayList<>();
		accountMapLock.lock();
		try {
			accountList = new ArrayList<AccountField>(accountMap.values());
		} catch (Exception e) {
			logger.error("获取账户列表异常", e);
		} finally {
			accountMapLock.unlock();
		}
		return accountList;
	}

	@Override
	public List<ContractField> getContractList() {
		List<ContractField> contractList = new ArrayList<>();
		contractMapLock.lock();
		try {
			contractList = new ArrayList<ContractField>(contractMap.values());
		} catch (Exception e) {
			logger.error("获取合约列表异常", e);
		} finally {
			contractMapLock.unlock();
		}
		return contractList;
	}

	@Override
	public List<TickField> getTickList() {
		List<TickField> tickList = new ArrayList<>();
		tickMapLock.lock();
		try {
			tickList = new ArrayList<TickField>(tickMap.values());
		} catch (Exception e) {
			logger.error("获取委托列表异常", e);
		} finally {
			tickMapLock.unlock();
		}
		return tickList;
	}

}
