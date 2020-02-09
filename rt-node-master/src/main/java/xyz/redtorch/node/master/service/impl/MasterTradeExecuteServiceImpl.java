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
	private static final Logger logger = LoggerFactory.getLogger(MasterTradeExecuteServiceImpl.class);

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

	private Set<String> originOrderIdSet = new HashSet<>(5000);

	private Map<String, SubmitOrderReqField> orderIdToSubmitOrderReqMap = new ConcurrentHashMap<>(5000);
	private Map<String, SubmitOrderReqField> originOrderIdToSubmitOrderReqMap = new ConcurrentHashMap<>(5000);

	private Map<String, Integer> orderIdToSourceNodeIdMap = new ConcurrentHashMap<>(5000);
	private Map<String, Integer> originOrderIdToSourceNodeIdMap = new ConcurrentHashMap<>(5000);
	
	private Map<String, ContractField> unifiedSymbolToSubscribedContractMap = new HashMap<>();
	// key可能是unifiedSymbol也可能是dataSourceId
	private Map<String, Set<Integer>> subscribeKeyToSubscribedNodeIdSetMap = new HashMap<>();
	private Map<Integer, Set<String>> subscribedNodeIdToSubscribeKeySetMap = new HashMap<>();

	private Map<String, String> subscribeKeyToUnfiedSymbolMap = new HashMap<>();

	private Lock subscribeLock = new ReentrantLock();

	@Override
	public Integer getNodeIdByOrderId(String orderId) {
		return orderIdToSourceNodeIdMap.get(orderId);
	}

	@Override
	public Integer getNodeIdByOriginOrderId(String originOrderId) {
		return originOrderIdToSourceNodeIdMap.get(originOrderId);
	}

	@Override
	public void subscribe(CommonReqField commonReq, ContractField contract) {

		String operatorId = commonReq.getOperatorId();
		String reqId = commonReq.getReqId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId); //

		int errorId = 0;
		String errorMsg = "";

		if (contract == null) {
			logger.error("订阅错误,请求ID:{},参数contract为空", reqId);
			errorId = 1;
			errorMsg = "订阅错误,参数contract为空";
		} else if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
			logger.error("订阅错误,请求ID:{},合约统一标识为空", reqId);
			errorId = 1;
			errorMsg = "订阅错误,参数contract为空";
		} else if (masterTradeCachesService.queryContractByUnifiedSymbol(adminOperatorId, contract.getUnifiedSymbol()) == null) {
			logger.error("订阅错误,请求ID:{},未能找到合约:{},请尝试搜寻", reqId, contract.getUnifiedSymbol());
			errorId = 1;
			errorMsg = "订阅错误未能找到合约,请尝试搜寻";
		} else {

			String unifiedSymbol = contract.getUnifiedSymbol();
			boolean canSubscribe = operatorService.checkSubscribePermission(operatorId, unifiedSymbol);

			if (canSubscribe) {
				subscribeLock.lock();
				try {
					ContractField targetContract = masterTradeCachesService.queryContractByUnifiedSymbol(adminOperatorId, contract.getUnifiedSymbol());
					unifiedSymbolToSubscribedContractMap.put(unifiedSymbol, targetContract);
					Integer nodeId = commonReq.getSourceNodeId();

					String subscribeKey = unifiedSymbol;
					if (StringUtils.isNotBlank(contract.getGatewayId())) {
						subscribeKey = subscribeKey + "@" + contract.getGatewayId();
					}

					// 记录订阅节点信息
					Set<Integer> nodeIdSet = new HashSet<>();
					if (subscribeKeyToSubscribedNodeIdSetMap.containsKey(subscribeKey)) {
						nodeIdSet = subscribeKeyToSubscribedNodeIdSetMap.get(subscribeKey);
					}
					nodeIdSet.add(nodeId);
					subscribeKeyToSubscribedNodeIdSetMap.put(subscribeKey, nodeIdSet);
					subscribeKeyToUnfiedSymbolMap.put(subscribeKey, unifiedSymbol);

					Set<String> subscribeKeySet = subscribedNodeIdToSubscribeKeySetMap.get(nodeId);
					if (subscribeKeySet == null) {
						subscribeKeySet = new HashSet<>();
						subscribedNodeIdToSubscribeKeySetMap.put(nodeId, subscribeKeySet);
					}
					subscribeKeySet.add(subscribeKey);

					logger.info("请求ID:{},节点ID:{},操作员ID:{},订阅合约:{}", reqId, nodeId, operatorId, subscribeKey);

				} catch (Exception e) {
					throw e;
				} finally {
					subscribeLock.unlock();
				}
			} else {
				logger.info("订阅错误,请求ID:{},操作员ID:{},无权订阅合约:{}", reqId, operatorId, unifiedSymbol);
			}

		}

		commonRspBuilder.setErrorId(errorId);
		commonRspBuilder.setErrorMsg(errorMsg);
		RpcSubscribeRsp.Builder rpcSubscribeRspBuilder = RpcSubscribeRsp.newBuilder().setCommonRsp(commonRspBuilder);
		rpcServerProcessService.sendCoreRpc(commonReq.getSourceNodeId(), rpcSubscribeRspBuilder.build().toByteString(), commonReq.getReqId(), RpcId.SUBSCRIBE_RSP);
	}

	@Override
	public void unsubscribe(CommonReqField commonReq, ContractField contract) {

		String reqId = commonReq.getReqId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId); //

		int errorId = 0;
		String errorMsg = "";

		if (contract == null) {
			logger.error("取消订阅错误,请求ID:{},参数contract为空", reqId);
			errorId = 1;
			errorMsg = "取消订阅错误,参数contract为空";
		} else if (StringUtils.isBlank(contract.getUnifiedSymbol())) {
			logger.error("取消订阅错误,请求ID:{},合约统一标识为空", reqId);
			errorId = 1;
			errorMsg = "取消订阅错误,参数contract为空";
		} else if (masterTradeCachesService.queryContractByUnifiedSymbol(adminOperatorId, contract.getUnifiedSymbol()) == null) {
			logger.error("取消订阅错误,请求ID:{},未能找到合约:{},请尝试搜寻", reqId, contract.getUnifiedSymbol());
			errorId = 1;
			errorMsg = "取消订阅错误未能找到合约,请尝试搜寻";
		} else {

			subscribeLock.lock();
			try {
				String unifiedSymbol = contract.getUnifiedSymbol();
				Integer nodeId = commonReq.getSourceNodeId();

				String subscribeKey = unifiedSymbol;
				if (StringUtils.isNotBlank(contract.getGatewayId())) {
					subscribeKey = subscribeKey + "@" + contract.getGatewayId();
				}

				Set<String> subscribeKeySet = subscribedNodeIdToSubscribeKeySetMap.get(nodeId);
				if (subscribeKeySet != null) {
					subscribeKeySet.remove(subscribeKey);
				}

				if (subscribeKeyToSubscribedNodeIdSetMap.containsKey(subscribeKey)) {
					Set<Integer> nodeIdSet = subscribeKeyToSubscribedNodeIdSetMap.get(subscribeKey);
					if (nodeIdSet.contains(nodeId)) {
						nodeIdSet.remove(nodeId);
						if (nodeIdSet.isEmpty()) {
							logger.info("订阅键{}已经没有节点订阅,删除", subscribeKey);
							subscribeKeyToSubscribedNodeIdSetMap.remove(subscribeKey);
							// 如果不存在其它key的订阅关系了,则完全删除订阅关系
							if (!subscribeKeyToSubscribedNodeIdSetMap.containsKey(unifiedSymbol)) {
								logger.info("合约{}已经没有节点订阅,删除", unifiedSymbol);
								unifiedSymbolToSubscribedContractMap.remove(unifiedSymbol);
							}
						}
					} else {
						errorId = 1;
						errorMsg = "订阅关系不存在";
					}
				} else {
					errorId = 1;
					errorMsg = "订阅关系不存在";
				}

			} catch (Exception e) {
				throw e;
			} finally {
				subscribeLock.unlock();
			}
		}

		commonRspBuilder.setErrorId(errorId);
		commonRspBuilder.setErrorMsg(errorMsg);

		RpcUnsubscribeRsp.Builder rpcUnsubscribeRspBuilder = RpcUnsubscribeRsp.newBuilder().setCommonRsp(commonRspBuilder);
		rpcServerProcessService.sendCoreRpc(commonReq.getSourceNodeId(), rpcUnsubscribeRspBuilder.build().toByteString(), commonReq.getReqId(), RpcId.UNSUBSCRIBE_RSP);
	}

	@Override
	public void removeSubscribeRelationByNodeId(Integer nodeId) {
		subscribeLock.lock();
		try {
			subscribedNodeIdToSubscribeKeySetMap.remove(nodeId);
			List<String> remvoeKeyList = new ArrayList<>();
			for (Entry<String, Set<Integer>> entry : subscribeKeyToSubscribedNodeIdSetMap.entrySet()) {
				String subscribeKey = entry.getKey();
				Set<Integer> nodeIdSet = entry.getValue();
				nodeIdSet.remove(nodeId);
				if (nodeIdSet.isEmpty()) {
					logger.info("订阅键{}已经没有节点订阅,删除", subscribeKey);
					remvoeKeyList.add(subscribeKey);
				}
			}
			for (String subscribeKey : remvoeKeyList) {
				subscribeKeyToSubscribedNodeIdSetMap.remove(subscribeKey);
				if (subscribeKeyToUnfiedSymbolMap.containsKey(subscribeKey)) {
					String unifiedSymbol = subscribeKeyToUnfiedSymbolMap.get(subscribeKey);
					// 如果不存在其它key的订阅关系了,则完全删除订阅关系
					if (!subscribeKeyToSubscribedNodeIdSetMap.containsKey(unifiedSymbol)) {
						logger.info("合约{}已经没有节点订阅,删除", unifiedSymbol);
						unifiedSymbolToSubscribedContractMap.remove(unifiedSymbol);
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
			if (subscribeKeyToSubscribedNodeIdSetMap.containsKey(subscribeKey)) {
				nodeIdSet = new HashSet<>(subscribeKeyToSubscribedNodeIdSetMap.get(subscribeKey));
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

		String operatorId = commonReq.getOperatorId();
		String reqId = commonReq.getReqId();

		RpcSubmitOrderRsp.Builder rpcSubmitOrderRsp = RpcSubmitOrderRsp.newBuilder();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId); //

		int errorId = 0;
		String errorMsg = "";

		if (submitOrderReq == null) {
			logger.error("提交定单错误,请求ID:{},参数submitOrderReq缺失", reqId);
			errorId = 1;
			errorMsg = "提交定单错误,参数submitOrderReq缺失";
		} else if (submitOrderReq.getContract() == null) {
			logger.error("提交定单错误,请求ID:{},参数contract缺失", reqId);
			errorId = 1;
			errorMsg = "提交定单错误,参数contract缺失";
		} else if (StringUtils.isBlank(submitOrderReq.getOriginOrderId())) {
			logger.error("提交定单错误,请求ID:{},原始定单ID为空", reqId);
			errorId = 1;
			errorMsg = "提交定单错误,原始定单ID为空";
		} else if (StringUtils.isBlank(submitOrderReq.getGatewayId())) {
			logger.error("提交定单错误,请求ID:{},网关ID为空", reqId);
			errorMsg = "提交定单错误,网关ID为空";
		} else if (masterSystemService.getSlaveNodeIdByGatewayId(submitOrderReq.getGatewayId()) == null) {
			logger.error("提交定单错误,请求ID:{},无法找到网关所在的节点ID,网关ID:{}", reqId, submitOrderReq.getGatewayId());
			errorId = 1;
			errorMsg = "提交定单错误，无法找到网关所在的节点ID";
		} else if (originOrderIdSet.contains(submitOrderReq.getOriginOrderId())) {
			logger.error("提交定单错误,请求ID:{},原始定单ID重复,原始订单ID{}", reqId, submitOrderReq.getOriginOrderId());
			errorId = 1;
			errorMsg = "提交定单错误,原始定单ID重复";
		} else {
			originOrderIdSet.add(submitOrderReq.getOriginOrderId());

			// 验证权限
			String unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
			String accountId = submitOrderReq.getAccountCode() + "@" + submitOrderReq.getCurrency().getValueDescriptor().getName() + "@" + submitOrderReq.getGatewayId();

			boolean canTradeAccount = operatorService.checkTradeAccountPermission(operatorId, accountId);
			boolean canTradeContract = false;

			if (canTradeAccount) {
				canTradeContract = operatorService.checkTradeContractPermission(operatorId, unifiedSymbol);
			}

			int targetNodeId = masterSystemService.getSlaveNodeIdByGatewayId(submitOrderReq.getGatewayId());

			if (canTradeAccount && canTradeContract) {
				originOrderIdToSubmitOrderReqMap.put(submitOrderReq.getOriginOrderId(), submitOrderReq);
				originOrderIdToSourceNodeIdMap.put(submitOrderReq.getOriginOrderId(), commonReq.getSourceNodeId());
				String orderId = rpcServerApiService.submitOrder(submitOrderReq, targetNodeId, reqId, null);

				if (StringUtils.isBlank(orderId)) {
					originOrderIdToSubmitOrderReqMap.remove(submitOrderReq.getOriginOrderId());
					originOrderIdToSourceNodeIdMap.remove(submitOrderReq.getOriginOrderId());
					logger.error("提交定单错误,请求ID:{},深度调用返回空定单ID", reqId);
					errorId = 1;
					errorMsg = "提交定单错误,深度调用返回空定单ID";
				} else {
					orderIdToSubmitOrderReqMap.put(orderId, submitOrderReq);
					orderIdToSourceNodeIdMap.put(orderId, commonReq.getSourceNodeId());
					rpcSubmitOrderRsp.setOrderId(orderId);
				}

			} else if (!canTradeAccount) {
				commonRspBuilder.setErrorId(1).setErrorMsg("" + accountId);
				logger.warn("请求ID:{},节点ID:{},操作员ID:{},无权交易账户:{}", reqId, commonReq.getSourceNodeId(), operatorId, unifiedSymbol);

				errorId = 1;
				errorMsg = "此操作员无权交易此账户";

			} else if (!canTradeContract) {
				logger.info("请求ID:{},节点ID:{},操作员ID:{},无权交易合约:{}", reqId, commonReq.getSourceNodeId(), operatorId, unifiedSymbol);

				errorId = 1;
				errorMsg = "此操作员无权交易此合约";
			}

		}

		commonRspBuilder.setErrorId(errorId);
		commonRspBuilder.setErrorMsg(errorMsg);
		rpcSubmitOrderRsp.setCommonRsp(commonRspBuilder);
		rpcServerProcessService.sendCoreRpc(commonReq.getSourceNodeId(), rpcSubmitOrderRsp.build().toByteString(), reqId, RpcId.SUBMIT_ORDER_RSP);

	}

	@Override
	public void cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq) {

		String operatorId = commonReq.getOperatorId();
		String reqId = commonReq.getReqId();

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId); //

		int errorId = 0;
		String errorMsg = "";

		if (cancelOrderReq == null) {
			logger.error("撤销定单错误,请求ID:{},参数cancelOrderReq缺失", reqId);
			errorId = 1;
			errorMsg = "撤销定单错误，参数cancelOrderReq缺失";
		} else if (StringUtils.isBlank(cancelOrderReq.getOrderId()) && StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
			logger.error("撤销定单错误,请求ID:{},参数orderId与originOrderId同时缺失", reqId);
			errorId = 1;
			errorMsg = "撤销定单错误，参数orderId与originOrderId同时缺失";
		} else {

			String gatewayId = null;
			String accountId = null;
			String unifiedSymbol = null;

			if (StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
				String orderId = cancelOrderReq.getOrderId();

				if (orderIdToSubmitOrderReqMap.containsKey(orderId)) {
					SubmitOrderReqField submitOrderReq = orderIdToSubmitOrderReqMap.get(orderId);
					gatewayId = submitOrderReq.getGatewayId();
					unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
					String currency = submitOrderReq.getContract().getCurrency().getValueDescriptor().getName();
					String accountCode = submitOrderReq.getAccountCode();
					accountId = accountCode + "@" + currency + "@" + gatewayId;
				} else {
					OrderField order = masterTradeCachesService.queryOrderByOrderId(adminOperatorId, orderId);
					if (order != null) {
						gatewayId = order.getContract().getGatewayId();
						accountId = order.getAccountId();
						unifiedSymbol = order.getContract().getUnifiedSymbol();
					}
				}

			} else {

				String originOrderId = cancelOrderReq.getOriginOrderId();

				if (originOrderIdToSubmitOrderReqMap.containsKey(originOrderId)) {
					SubmitOrderReqField submitOrderReq = originOrderIdToSubmitOrderReqMap.get(originOrderId);
					gatewayId = submitOrderReq.getGatewayId();
					unifiedSymbol = submitOrderReq.getContract().getUnifiedSymbol();
					String currency = submitOrderReq.getContract().getCurrency().getValueDescriptor().getName();
					String accountCode = submitOrderReq.getAccountCode();
					accountId = accountCode + "@" + currency + "@" + gatewayId;
				} else {
					OrderField order = masterTradeCachesService.queryOrderByOriginOrderId(adminOperatorId, originOrderId);
					if (order != null) {
						gatewayId = order.getContract().getGatewayId();
						accountId = order.getAccountId();
						unifiedSymbol = order.getContract().getUnifiedSymbol();
					}
				}
			}

			if (StringUtils.isBlank(gatewayId)) {
				logger.error("撤销定单错误,请求ID:{},无法找到网关信息,网关ID:{}", reqId, gatewayId);
				errorId = 1;
				errorMsg = "撤销定单错误,无法找到网关信息";
			} else if (masterSystemService.getSlaveNodeIdByGatewayId(gatewayId) == null) {
				logger.error("撤销定单错误,请求ID:{},无法找到网关所在的节点ID,网关ID:{}", reqId, gatewayId);
				errorId = 1;
				errorMsg = "撤销定单错误,无法找到网关所在的节点ID";
			} else {
				boolean canTradeAccount = operatorService.checkTradeAccountPermission(operatorId, accountId);
				boolean canTradeContract = false;

				if (canTradeAccount) {
					canTradeContract = operatorService.checkTradeContractPermission(operatorId, unifiedSymbol);
				}

				if (canTradeAccount && canTradeContract) {
					int targetNodeId = masterSystemService.getSlaveNodeIdByGatewayId(gatewayId);

					Boolean result = rpcServerApiService.cancelOrder(cancelOrderReq, targetNodeId, reqId, null);

					if (!result) {
						logger.error("撤销定单错误,请求ID{},深度调用返回失败", reqId);
						errorId = 1;
						errorMsg = "撤销定单错误,深度调用返回失败";
					}
				} else if (!canTradeAccount) {
					commonRspBuilder.setErrorId(1).setErrorMsg("" + accountId);
					logger.warn("请求ID:{},节点ID:{},操作员ID:{},无权交易账户:{}", reqId, commonReq.getSourceNodeId(), operatorId, unifiedSymbol);

					errorId = 1;
					errorMsg = "此操作员无权交易此账户";

				} else if (!canTradeContract) {
					logger.info("请求ID:{},节点ID:{},操作员ID:{},无权交易合约:{}", reqId, commonReq.getSourceNodeId(), operatorId, unifiedSymbol);

					errorId = 1;
					errorMsg = "此操作员无权交易此合约";
				}

				commonRspBuilder.setErrorId(errorId);
				commonRspBuilder.setErrorMsg(errorMsg);
				RpcCancelOrderRsp.Builder rpcCancelOrderRsp = RpcCancelOrderRsp.newBuilder().setCommonRsp(commonRspBuilder);
				rpcServerProcessService.sendCoreRpc(commonReq.getSourceNodeId(), rpcCancelOrderRsp.build().toByteString(), reqId, RpcId.CANCEL_ORDER_RSP);
			}
		}
	}

	@Override
	public void searchContract(CommonReqField commonReq, ContractField contract) {
		String reqId = commonReq.getReqId();
		int errorId = 0;
		String errorMsg = "";

		if (contract == null) {
			logger.error("搜索合约错误,请求ID:{},参数contract缺失", reqId);
			errorId = 1;
			errorMsg = "搜索合约错误,参数contract缺失";
		} else {

			List<Integer> nodeIdList = masterSystemService.getSlaveNodeIdList();

			for (Integer targetNodeId : nodeIdList) {
				try {

					Boolean result = rpcServerApiService.searchContract(contract, targetNodeId, reqId, null);
					if (!result) {
						logger.error("搜寻合约错误,请求ID:{},深度调用返回失败,节点ID:{}", reqId, targetNodeId);
					}
				} catch (Exception e) {
					logger.error("搜寻合约异常,请求ID:{}", reqId, e);
				}
			}
		}

		CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
				.setReqId(reqId) //
				.setErrorId(errorId)//
				.setErrorMsg(errorMsg);

		RpcSearchContractRsp.Builder rpcSearchContractRsp = RpcSearchContractRsp.newBuilder().setCommonRsp(commonRspBuilder);
		rpcServerProcessService.sendCoreRpc(commonReq.getSourceNodeId(), rpcSearchContractRsp.build().toByteString(), reqId, RpcId.SEARCH_CONTRACT_RSP);
	}

	@Override
	public List<ContractField> getSubscribedContract() {
		subscribeLock.lock();
		try {
			return new ArrayList<>(unifiedSymbolToSubscribedContractMap.values());
		} catch (Exception e) {
			logger.error("获取已经订阅合约列表发生错误", e);
		} finally {
			subscribeLock.unlock();
		}
		return new ArrayList<>();
	}

	@Override
	public Set<String> getSubscribKeySet(int nodeId) {
		return subscribedNodeIdToSubscribeKeySetMap.get(nodeId);
	}

	@Override
	public SubmitOrderReqField getSubmitOrderReqByOrderId(String orderId) {
		return orderIdToSubmitOrderReqMap.get(orderId);
	}

}
