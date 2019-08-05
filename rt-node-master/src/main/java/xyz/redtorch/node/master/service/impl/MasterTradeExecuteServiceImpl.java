package xyz.redtorch.node.master.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.node.master.rpc.service.RpcServerApiService;
import xyz.redtorch.node.master.rpc.service.RpcServerProcessService;
import xyz.redtorch.node.master.service.MasterSystemService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.node.master.service.OperatorService;
import xyz.redtorch.node.master.service.MasterTradeCachesService;
import xyz.redtorch.pb.CoreEnum.CommonStatusEnum;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;

@Service
public class MasterTradeExecuteServiceImpl implements MasterTradeExecuteService {
	private static Logger logger = LoggerFactory.getLogger(MasterTradeExecuteServiceImpl.class);

	@Autowired
	private MasterSystemService masterSystemService;
	@Autowired
	private RpcServerProcessService rpcServerProcessService;
	@Autowired
	private RpcServerApiService rpcServerApiService;
	@Autowired
	private MasterTradeCachesService masterTradeCachesService;
	@Autowired
	private OperatorService operatorService;
	@Value("${rt.node.master.operatorId}")
	private String adminOperatorId;

	private Map<String, SubmitOrderReqField> orderIdSubmitOrderReqMap = new ConcurrentHashMap<>(5000);
	private Map<String, SubmitOrderReqField> originOrderIdSubmitOrderReqMap = new ConcurrentHashMap<>(5000);

	private Map<String, Integer> orderIdSourceNodeIdMap = new ConcurrentHashMap<>(5000);
	private Map<String, Integer> originOrderIdSourceNodeIdMap = new ConcurrentHashMap<>(5000);

	private Map<String, ContractField> subscribedContractMap = new HashMap<>();
	// key可能是unifiedSymbol也可能是dataSourceId
	private Map<String, Set<Integer>> subscribedNodeIdSetMap = new HashMap<>();

	private Map<String, String> subscribeKeyUnfiedSymblMap = new HashMap<>();

	private Lock subscribeLock = new ReentrantLock();

	@Override
	public Integer getNodeIdByOrderId(String orderId) {
		return orderIdSourceNodeIdMap.get(orderId);
	}

	@Override
	public Integer getNodeIdByOriginOrderId(String originOrderId) {
		return originOrderIdSourceNodeIdMap.get(originOrderId);
	}

	@Override
	public void subscribe(CommonReqField commonReq, ContractField contract, String gatewayId) {
		if (contract == null) {
			logger.error("订阅错误,参数contract为空");
			throw new IllegalArgumentException("订阅错误,参数contract为空");
		} else if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
			logger.error("订阅错误,合约统一标识为空");
			throw new IllegalArgumentException("订阅错误,合约统一标识为空");
		} else if (masterTradeCachesService.queryContractByUnifiedSymbol(adminOperatorId,
				contract.getUnifiedSymbol()) == null) {
			logger.error("订阅错误,未能找到合约,请尝试搜寻");
			throw new IllegalArgumentException("订阅错误,未能找到合约,请尝试搜寻");
		} else {
			subscribeLock.lock();
			try {

				String unifiedSymbol = contract.getUnifiedSymbol();

				// 验证权限
				String operatorId = commonReq.getOperatorId();
				boolean canSubscribe = operatorService.checkSubscribePermission(operatorId, unifiedSymbol);

				// 返回结果
				int rspSourceNodeId = 0;
				int rspTargetNodeId = commonReq.getSourceNodeId();

				CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
						.setSourceNodeId(rspSourceNodeId) //
						.setTargetNodeId(rspTargetNodeId) //
						.setReqId(commonReq.getReqId()) //
						.setRequestStatus(CommonStatusEnum.SUCCESS);

				if (canSubscribe) {
					ContractField targetContract = masterTradeCachesService
							.queryContractByUnifiedSymbol(adminOperatorId, contract.getUnifiedSymbol());
					subscribedContractMap.put(unifiedSymbol, targetContract);
					Integer nodeId = commonReq.getSourceNodeId();

					String subscribeKey = unifiedSymbol;
					if (StringUtils.isNotBlank(gatewayId)) {
						subscribeKey = subscribeKey + "@" + gatewayId;
					}

					// 记录订阅节点信息
					Set<Integer> nodeIdSet = new HashSet<>();
					if (subscribedNodeIdSetMap.containsKey(subscribeKey)) {
						nodeIdSet = subscribedNodeIdSetMap.get(subscribeKey);
					}
					nodeIdSet.add(nodeId);
					subscribedNodeIdSetMap.put(subscribeKey, nodeIdSet);
					subscribeKeyUnfiedSymblMap.put(subscribeKey, unifiedSymbol);
					logger.info("节点ID{}操作员ID{}订阅合约{}", nodeId, operatorId, subscribeKey);
				} else {
					commonRspBuilder.setRequestStatus(CommonStatusEnum.ERROR).setInfo("此操作员无权订阅合约" + unifiedSymbol);
					logger.info("节点ID{}操作员ID{}无权订阅合约{}", commonReq.getSourceNodeId(), operatorId, unifiedSymbol);
				}

				RpcSubscribeRsp.Builder rpcSubscribeRspBuilder = RpcSubscribeRsp.newBuilder()
						.setCommonRsp(commonRspBuilder);
				rpcServerProcessService.sendRoutineCoreRpc(rspTargetNodeId,
						rpcSubscribeRspBuilder.build().toByteString(), commonReq.getReqId(), RpcId.SUBSCRIBE_RSP);
			} catch (Exception e) {
				throw e;
			} finally {
				subscribeLock.unlock();
			}
		}
	}

	@Override
	public void unsubscribe(CommonReqField commonReq, ContractField contract, String gatewayId) {
		if (contract == null) {
			logger.error("订阅错误,参数contract为空");
			throw new IllegalArgumentException("订阅错误,参数contract为空");
		} else if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
			logger.error("订阅错误,合约统一标识为空");
			throw new IllegalArgumentException("订阅错误,合约统一标识为空");
		} else if (masterTradeCachesService.queryContractByUnifiedSymbol(adminOperatorId,
				contract.getUnifiedSymbol()) == null) {
			logger.error("订阅错误,未能找到合约,请尝试搜寻");
			throw new IllegalArgumentException("订阅错误,未能找到合约,请尝试搜寻");
		} else {
			subscribeLock.lock();
			try {
				String unifiedSymbol = contract.getUnifiedSymbol();
				Integer nodeId = commonReq.getSourceNodeId();

				// 返回结果
				int rspSourceNodeId = 0;
				int rspTargetNodeId = commonReq.getSourceNodeId();

				CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
						.setSourceNodeId(rspSourceNodeId) //
						.setTargetNodeId(rspTargetNodeId) //
						.setReqId(commonReq.getReqId()) //
						.setRequestStatus(CommonStatusEnum.SUCCESS);

				String subscribeKey = unifiedSymbol;
				if (StringUtils.isNotBlank(gatewayId)) {
					subscribeKey = subscribeKey + "@" + gatewayId;
				}

				if (subscribedNodeIdSetMap.containsKey(subscribeKey)) {
					Set<Integer> nodeIdSet = subscribedNodeIdSetMap.get(subscribeKey);
					if (nodeIdSet.contains(nodeId)) {
						nodeIdSet.remove(nodeId);
						if (nodeIdSet.isEmpty()) {
							logger.info("订阅键{}已经没有节点订阅,删除", subscribeKey);
							subscribedNodeIdSetMap.remove(subscribeKey);
							// 如果不存在其它key的订阅关系了,则完全删除订阅关系
							if (!subscribedNodeIdSetMap.containsKey(unifiedSymbol)) {
								logger.info("合约{}已经没有节点订阅,删除", unifiedSymbol);
								subscribedContractMap.remove(unifiedSymbol);
							}
						}
					} else {
						commonRspBuilder.setRequestStatus(CommonStatusEnum.WARN);
						commonRspBuilder.setInfo("订阅关系不存在！");
					}
				} else {
					commonRspBuilder.setRequestStatus(CommonStatusEnum.WARN);
					commonRspBuilder.setInfo("订阅关系不存在！");
				}

				RpcUnsubscribeRsp.Builder rpcUnsubscribeRspBuilder = RpcUnsubscribeRsp.newBuilder()
						.setCommonRsp(commonRspBuilder);
				rpcServerProcessService.sendRoutineCoreRpc(rspTargetNodeId,
						rpcUnsubscribeRspBuilder.build().toByteString(), commonReq.getReqId(), RpcId.UNSUBSCRIBE_RSP);
			} catch (Exception e) {
				throw e;
			} finally {
				subscribeLock.unlock();
			}
		}
	}

	@Override
	public void removeSubscribeRelationByNodeId(Integer nodeId) {
		subscribeLock.lock();
		try {
			List<String> remvoeKeyList = new ArrayList<>();
			for (Entry<String, Set<Integer>> entry : subscribedNodeIdSetMap.entrySet()) {
				String subscribeKey = entry.getKey();
				Set<Integer> nodeIdSet = entry.getValue();
				nodeIdSet.remove(nodeId);
				if (nodeIdSet.isEmpty()) {
					logger.info("订阅键{}已经没有节点订阅,删除", subscribeKey);
					remvoeKeyList.add(subscribeKey);
				}
			}
			for (String subscribeKey : remvoeKeyList) {
				subscribedNodeIdSetMap.remove(subscribeKey);
				if (subscribeKeyUnfiedSymblMap.containsKey(subscribeKey)) {
					String unifiedSymbol = subscribeKeyUnfiedSymblMap.get(subscribeKey);
					// 如果不存在其它key的订阅关系了,则完全删除订阅关系
					if (!subscribedNodeIdSetMap.containsKey(unifiedSymbol)) {
						logger.info("合约{}已经没有节点订阅,删除", unifiedSymbol);
						subscribedContractMap.remove(unifiedSymbol);
					}
				}

			}
		} catch (Exception e) {
			logger.error("根据节点ID删除订阅关系错误", e);
		} finally {
			subscribeLock.unlock();
		}
	}

	@Override
	public Set<Integer> getSubscribedNodeIdSet(String subscribeKey) {
		subscribeLock.lock();
		Set<Integer> nodeIdSet = new HashSet<>();
		try {
			if (subscribedNodeIdSetMap.containsKey(subscribeKey)) {
				nodeIdSet = new HashSet<>(subscribedNodeIdSetMap.get(subscribeKey));
			}
		} catch (Exception e) {
			logger.error("根据订阅键获取订阅节点ID列表错误", e);
		} finally {
			subscribeLock.unlock();
		}
		return nodeIdSet;
	}

	@Override
	public void submitOrder(CommonReqField commonReq, SubmitOrderReqField submitOrderReq) {

		if (submitOrderReq == null) {
			logger.error("提交定单错误，参数submitOrderReq缺失");
			throw new IllegalArgumentException("提交定单错误，参数submitOrderReq缺失");
		} else if (submitOrderReq.getContract() == null) {
			logger.error("提交定单错误，参数contract缺失");
			throw new IllegalArgumentException("提交定单错误，参数contract缺失");
		} else if (StringUtils.isBlank(submitOrderReq.getOriginOrderId())) {
			logger.error("提交定单错误,原始定单ID为空");
			throw new IllegalArgumentException("提交定单错误,原始定单ID为空");
		} else if (StringUtils.isBlank(submitOrderReq.getGatewayId())) {
			logger.error("提交定单错误,网关ID为空");
			throw new IllegalArgumentException("提交定单错误,网关ID为空");
		} else if (masterSystemService.getSlaveNodeIdByGatewayId(submitOrderReq.getGatewayId()) == null) {
			logger.error("提交定单错误,无法找到网关所在的节点ID,网关ID:{}", submitOrderReq.getGatewayId());
			throw new RuntimeException("取消订阅错误,无法找到网关所在的节点ID,网关ID:" + submitOrderReq.getGatewayId());
		} else {

			// 验证权限
			String operatorId = commonReq.getOperatorId();

			String unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
			String accountId = submitOrderReq.getAccountCode() + "@"
					+ submitOrderReq.getCurrency().getValueDescriptor().getName() + "@" + submitOrderReq.getGatewayId();

			boolean canTradeAccount = operatorService.checkTradeAccountPermission(operatorId, accountId);
			boolean canTradeContract = false;

			if (canTradeAccount) {
				canTradeContract = operatorService.checkTradeContractPermission(operatorId, unifiedSymbol);
			}

			int targetNodeId = masterSystemService.getSlaveNodeIdByGatewayId(submitOrderReq.getGatewayId());
			String reqId = commonReq.getReqId();

			int rspSourceNodeId = 0;
			int rspTargetNodeId = commonReq.getSourceNodeId();

			CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
					.setSourceNodeId(rspSourceNodeId) //
					.setTargetNodeId(rspTargetNodeId) //
					.setReqId(reqId) //
					.setRequestStatus(CommonStatusEnum.SUCCESS);

			RpcSubmitOrderRsp.Builder rpcSubmitOrderRsp = RpcSubmitOrderRsp.newBuilder();

			if (canTradeAccount && canTradeContract) {
				originOrderIdSubmitOrderReqMap.put(submitOrderReq.getOriginOrderId(), submitOrderReq);
				originOrderIdSourceNodeIdMap.put(submitOrderReq.getOriginOrderId(), commonReq.getSourceNodeId());
				String orderId = rpcServerApiService.submitOrder(submitOrderReq, targetNodeId, reqId, null);

				if (StringUtils.isBlank(orderId)) {
					originOrderIdSubmitOrderReqMap.remove(submitOrderReq.getOriginOrderId());
					originOrderIdSourceNodeIdMap.remove(submitOrderReq.getOriginOrderId());
					logger.error("提交定单错误,深度调用返回空定单ID");
					commonRspBuilder.setRequestStatus(CommonStatusEnum.ERROR).setInfo("提交定单错误,深度调用返回空定单ID");
				} else {
					orderIdSubmitOrderReqMap.put(orderId, submitOrderReq);
					orderIdSourceNodeIdMap.put(orderId, commonReq.getSourceNodeId());
					rpcSubmitOrderRsp.setOrderId(orderId);
				}

			} else if (!canTradeAccount) {
				commonRspBuilder.setRequestStatus(CommonStatusEnum.ERROR).setInfo("此操作员无权交易账户" + accountId);
				logger.info("节点ID{}操作员ID{}无权交易账户{}", commonReq.getSourceNodeId(), operatorId, unifiedSymbol);
			} else if (!canTradeContract) {
				commonRspBuilder.setRequestStatus(CommonStatusEnum.ERROR).setInfo("此操作员无权交易合约" + unifiedSymbol);
				logger.info("节点ID{}操作员ID{}无权交易合约{}", commonReq.getSourceNodeId(), operatorId, unifiedSymbol);
			}

			rpcSubmitOrderRsp.setCommonRsp(commonRspBuilder);
			rpcServerProcessService.sendRoutineCoreRpc(rspTargetNodeId, rpcSubmitOrderRsp.build().toByteString(), reqId,
					RpcId.SUBMIT_ORDER_RSP);
		}

	}

	@Override
	public void cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq) {

		if (cancelOrderReq == null) {
			logger.error("撤销定单错误，参数cancelOrderReq缺失");
			throw new IllegalArgumentException("撤销定单错误，参数cancelOrderReq缺失");
		} else if (StringUtils.isBlank(cancelOrderReq.getOrderId())
				&& StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
			logger.error("撤销定单错误，参数orderId与originOrderId同时缺失");
			throw new IllegalArgumentException("撤销定单错误，参数orderId与originOrderId同时缺失");
		} else {

			String operatorId = commonReq.getOperatorId();

			String gatewayId = null;
			String accountId = null;
			String unifiedSymbol = null;

			if (StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
				String orderId = cancelOrderReq.getOrderId();

				if (orderIdSubmitOrderReqMap.containsKey(orderId)) {
					SubmitOrderReqField submitOrderReq = orderIdSubmitOrderReqMap.get(orderId);
					gatewayId = submitOrderReq.getGatewayId();
					unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
					String currency = submitOrderReq.getContract().getCurrency().getValueDescriptor().getName();
					String accountCode = submitOrderReq.getAccountCode();
					accountId = accountCode + "@" + currency + "@" + gatewayId;
				} else {
					OrderField order = masterTradeCachesService.queryOrderByOrderId(adminOperatorId, orderId);
					if (order != null) {
						gatewayId = order.getContract().getGateway().getGatewayId();
						accountId = order.getAccountId();
						unifiedSymbol = order.getContract().getUnifiedSymbol();
					}
				}

			} else {

				String originOrderId = cancelOrderReq.getOriginOrderId();

				if (originOrderIdSubmitOrderReqMap.containsKey(originOrderId)) {
					SubmitOrderReqField submitOrderReq = originOrderIdSubmitOrderReqMap.get(originOrderId);
					gatewayId = submitOrderReq.getGatewayId();
					unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
					String currency = submitOrderReq.getContract().getCurrency().getValueDescriptor().getName();
					String accountCode = submitOrderReq.getAccountCode();
					accountId = accountCode + "@" + currency + "@" + gatewayId;
				} else {
					OrderField order = masterTradeCachesService.queryOrderByOriginOrderId(adminOperatorId,
							originOrderId);
					if (order != null) {
						gatewayId = order.getContract().getGateway().getGatewayId();
						accountId = order.getAccountId();
						unifiedSymbol = order.getContract().getUnifiedSymbol();
					}
				}
			}

			String reqId = commonReq.getReqId();
			if (StringUtils.isBlank(gatewayId)) {
				throw new RuntimeException("撤销定单错误,无法找到网关信息");
			} else if (masterSystemService.getSlaveNodeIdByGatewayId(gatewayId) == null) {
				throw new RuntimeException("撤销定单错误,无法找到网关所在的节点ID,网关ID:" + gatewayId);
			}

			boolean canTradeAccount = operatorService.checkTradeAccountPermission(operatorId, accountId);
			boolean canTradeContract = false;

			if (canTradeAccount) {
				canTradeContract = operatorService.checkTradeContractPermission(operatorId, unifiedSymbol);
			}

			int rspSourceNodeId = 0;
			int rspTargetNodeId = commonReq.getSourceNodeId();

			CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
					.setSourceNodeId(rspSourceNodeId) //
					.setTargetNodeId(rspTargetNodeId) //
					.setReqId(reqId) //
					.setRequestStatus(CommonStatusEnum.SUCCESS);

			if (canTradeAccount && canTradeContract) {
				int targetNodeId = masterSystemService.getSlaveNodeIdByGatewayId(gatewayId);

				Boolean result = rpcServerApiService.cancelOrder(cancelOrderReq, targetNodeId, reqId, null);

				if (!result) {
					logger.error("撤销定单错误,深度调用返回失败");
					commonRspBuilder.setRequestStatus(CommonStatusEnum.ERROR).setInfo("撤销定单错误,深度调用返回失败");
				}
			} else if (!canTradeAccount) {
				commonRspBuilder.setRequestStatus(CommonStatusEnum.ERROR).setInfo("此操作员无权交易账户" + accountId);
				logger.info("节点ID{}操作员ID{}无权交易账户{}", commonReq.getSourceNodeId(), operatorId, unifiedSymbol);
			} else if (!canTradeContract) {
				commonRspBuilder.setRequestStatus(CommonStatusEnum.ERROR).setInfo("此操作员无权交易合约" + unifiedSymbol);
				logger.info("节点ID{}操作员ID{}无权交易合约{}", commonReq.getSourceNodeId(), operatorId, unifiedSymbol);
			}

			RpcCancelOrderRsp.Builder rpcCancelOrderRsp = RpcCancelOrderRsp.newBuilder().setCommonRsp(commonRspBuilder);
			rpcServerProcessService.sendRoutineCoreRpc(rspTargetNodeId, rpcCancelOrderRsp.build().toByteString(), reqId,
					RpcId.CANCEL_ORDER_RSP);
		}
	}

	@Override
	public void searchContract(CommonReqField commonReq, ContractField contract) {
		if (contract == null) {
			logger.error("搜索合约错误，参数contract缺失");
			throw new IllegalArgumentException("订阅错误，参数contract缺失");
		} else {

			List<Integer> nodeIdList = masterSystemService.getSlaveNodeIdList();

			String reqId = commonReq.getReqId();

			for (Integer targetNodeId : nodeIdList) {
				try {

					Boolean result = rpcServerApiService.searchContract(contract, targetNodeId, reqId, null);
					if (!result) {
						logger.error("搜寻合约错误,深度调用返回失败,节点ID:{}", targetNodeId);
					}
				} catch (Exception e) {
					logger.error("搜寻合约异常", e);
				}
			}

			int rspSourceNodeId = 0;
			int rspTargetNodeId = commonReq.getSourceNodeId();

			CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
					.setSourceNodeId(rspSourceNodeId) //
					.setTargetNodeId(rspTargetNodeId) //
					.setReqId(reqId) //
					.setRequestStatus(CommonStatusEnum.SUCCESS);

			RpcSearchContractRsp.Builder rpcSearchContractRsp = RpcSearchContractRsp.newBuilder()
					.setCommonRsp(commonRspBuilder);
			rpcServerProcessService.sendRoutineCoreRpc(rspTargetNodeId, rpcSearchContractRsp.build().toByteString(),
					reqId, RpcId.SEARCH_CONTRACT_RSP);
		}
	}

	@Override
	public List<ContractField> getSubscribedContract() {
		subscribeLock.lock();
		try {
			return new ArrayList<>(subscribedContractMap.values());
		} catch (Exception e) {
			logger.error("获取已经订阅合约列表发生错误", e);
		} finally {
			subscribeLock.unlock();
		}
		return new ArrayList<>();
	}

}
