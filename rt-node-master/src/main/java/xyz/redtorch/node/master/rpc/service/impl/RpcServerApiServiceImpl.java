package xyz.redtorch.node.master.rpc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.master.rpc.service.RpcServerApiService;
import xyz.redtorch.node.master.rpc.service.RpcServerProcessService;
import xyz.redtorch.node.master.rpc.service.RpcServerRspHandlerService;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractReq;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeReq;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeReq;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;

@Service
public class RpcServerApiServiceImpl implements RpcServerApiService {

	private static final Logger logger = LoggerFactory.getLogger(RpcServerApiServiceImpl.class);

	@Value("${rt.node.master.operatorId}")
	private String masterOperatorId;
	@Value("${rt.rpc.server.sync-default-timeout-seconds}")
	private int defaultRpcTimeoutSeconds;

	@Autowired
	private RpcServerProcessService rpcServerProcessService;
	@Autowired
	private RpcServerRspHandlerService rpcServerRspHandlerService;

	@Override
	public boolean asyncSubscribe(ContractField contract, Integer targetNodeId, String reqId) {

		if (StringUtils.isBlank(reqId)) {
			logger.error("订阅错误,参数reqId缺失");
			return false;
		} else if (contract == null) {
			logger.error("订阅错误,请求ID:{},参数contract缺失", reqId);
			return false;
		} else if (targetNodeId == null || targetNodeId == 0) {
			logger.error("订阅错误,请求ID{},参数targetNodeId错误", reqId);
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcSubscribeReq.Builder rpcSubscribeReqBuilder = RpcSubscribeReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setContract(contract);
			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcSubscribeReqBuilder.build().toByteString(), reqId, RpcId.SUBSCRIBE_REQ);
		}

	}

	@Override
	public boolean subscribe(ContractField contract, Integer targetNodeId, String reqId, Integer timeoutSeconds) {

		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		// 先注册请求ID避免回报先到
		rpcServerRspHandlerService.registerWaitReqId(reqId);

		if (!asyncSubscribe(contract, targetNodeId, reqId)) {
			rpcServerRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcSubscribeRsp rpcSubscribeRsp = rpcServerRspHandlerService.getAndRemoveRpcSubscribeRsp(reqId);
				if (rpcSubscribeRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (rpcExceptionRsp != null) {
						logger.error("订阅错误,请求ID:{},远程错误回报:{}", reqId, rpcExceptionRsp.getInfo());
						return false;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcSubscribeRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("订阅完成,请求ID:{}", reqId);
						return true;
					} else {
						logger.error("订阅错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return false;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("订阅错误,请求ID:{},等待回报超时", reqId);
				return false;
			}
		}

	}

	@Override
	public boolean asyncUnsubscribe(ContractField contract, Integer targetNodeId, String reqId) {

		if (StringUtils.isBlank(reqId)) {
			logger.error("取消订阅错误,参数reqId缺失");
			return false;
		} else if (contract == null) {
			logger.error("取消订阅错误,请求ID:{},参数contract缺失", reqId);
			return false;
		} else if (targetNodeId == null || targetNodeId == 0) {
			logger.error("取消订阅错误,请求ID:{},参数targetNodeId错误", reqId);
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcUnsubscribeReq.Builder rpcUnsubscribeReqBuilder = RpcUnsubscribeReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setContract(contract);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcUnsubscribeReqBuilder.build().toByteString(), reqId, RpcId.UNSUBSCRIBE_REQ);

		}

	}

	@Override
	public boolean unsubscribe(ContractField contract, Integer targetNodeId, String reqId, Integer timeoutSeconds) {

		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		// 先注册请求ID避免回报先到
		rpcServerRspHandlerService.registerWaitReqId(reqId);

		if (!asyncUnsubscribe(contract, targetNodeId, reqId)) {
			rpcServerRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcUnsubscribeRsp rpcUnsubscribeRsp = rpcServerRspHandlerService.getAndRemoveRpcUnsubscribeRsp(reqId);
				if (rpcUnsubscribeRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (rpcExceptionRsp != null) {
						logger.error("取消订阅错误,请求ID:{},远程错误回报:{}", reqId, rpcExceptionRsp.getInfo());
						return false;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcUnsubscribeRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("取消订阅完成,请求ID:{}", reqId);
						return true;
					} else {
						logger.error("取消订阅错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return false;
					}
				}
			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("取消订阅错误,请求ID:{},等待回报超时", reqId);
				return false;
			}

		}

	}

	@Override
	public boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("提交定单错误,参数reqId缺失");
			return false;
		} else if (submitOrderReq == null) {
			logger.error("提交定单错误,请求ID:{},参数submitOrderReq缺失", reqId);
			return false;
		} else if (targetNodeId == null || targetNodeId == 0) {
			logger.error("提交定单错误,请求ID:{},参数targetNodeId错误", reqId);
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcSubmitOrderReq.Builder rpcSubmitOrderReqBuilder = RpcSubmitOrderReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setSubmitOrderReq(submitOrderReq); //

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcSubmitOrderReqBuilder.build().toByteString(), reqId, RpcId.SUBMIT_ORDER_REQ);

		}
	}

	@Override
	public String submitOrder(SubmitOrderReqField submitOrderReq, Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		rpcServerRspHandlerService.registerWaitReqId(reqId);

		if (!asyncSubmitOrder(submitOrderReq, targetNodeId, reqId)) {
			rpcServerRspHandlerService.unregisterWaitReqId(reqId);
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcSubmitOrderRsp rpcSubmitOrderRsp = rpcServerRspHandlerService.getAndRemoveRpcSubmitOrderRsp(reqId);
				if (rpcSubmitOrderRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("提交定单错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcSubmitOrderRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("提交定单完成,请求ID:{}", reqId);
						return rpcSubmitOrderRsp.getOrderId();
					} else {
						logger.error("提交定单错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("提交定单错误,请求ID:{},等待回报超时", reqId);
				return null;
			}
		}

	}

	@Override
	public boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("撤销定单错误,参数reqId缺失");
			return false;
		} else if (cancelOrderReq == null) {
			logger.error("撤销定单错误,请求ID:{},参数cancelOrderReq缺失", reqId);
			return false;
		} else if (targetNodeId == null || targetNodeId == 0) {
			logger.error("撤销定单错误,请求ID:{},参数targetNodeId错误", reqId);
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcCancelOrderReq.Builder rpcCancelOrderReqBuilder = RpcCancelOrderReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setCancelOrderReq(cancelOrderReq);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcCancelOrderReqBuilder.build().toByteString(), reqId, RpcId.CANCEL_ORDER_REQ);

		}
	}

	@Override
	public boolean cancelOrder(CancelOrderReqField cancelOrderReq, Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		rpcServerRspHandlerService.registerWaitReqId(reqId);

		if (!asyncCancelOrder(cancelOrderReq, targetNodeId, reqId)) {
			rpcServerRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcCancelOrderRsp rpcCancelOrderRsp = rpcServerRspHandlerService.getAndRemoveRpcCancelOrderRsp(reqId);
				if (rpcCancelOrderRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (rpcExceptionRsp != null) {
						logger.error("撤销定单错误,请求ID:{},远程错误回报:{}", reqId, rpcExceptionRsp.getInfo());
						return false;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcCancelOrderRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("撤销定单完成,请求ID:{}", reqId);
						return true;
					} else {
						logger.error("撤销定单错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return false;
					}
				}
			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("撤销定单错误,请求ID:{},等待回报超时", reqId);
				return false;
			}

		}
	}

	@Override
	public boolean asyncSearchContract(ContractField contract, Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("搜寻合约错误,参数reqId缺失");
			return false;
		} else if (contract == null) {
			logger.error("搜寻合约错误,请求ID:{},参数contract缺失", reqId);
			return false;
		} else if (targetNodeId == null || targetNodeId == 0) {
			logger.error("搜寻合约错误,请求ID:{},参数targetNodeId错误", reqId);
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcSearchContractReq.Builder rpcSearchContractReqBuilder = RpcSearchContractReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setContract(contract);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcSearchContractReqBuilder.build().toByteString(), reqId, RpcId.SEARCH_CONTRACT_REQ);

		}
	}

	@Override
	public boolean searchContract(ContractField contract, Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		rpcServerRspHandlerService.registerWaitReqId(reqId);

		if (!asyncSearchContract(contract, targetNodeId, reqId)) {
			rpcServerRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcCancelOrderRsp rpcCancelOrderRsp = rpcServerRspHandlerService.getAndRemoveRpcCancelOrderRsp(reqId);
				if (rpcCancelOrderRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (rpcExceptionRsp != null) {
						logger.error("搜寻合约错误,请求ID:{},远程错误回报:{}", reqId, rpcExceptionRsp.getInfo());
						return false;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcCancelOrderRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("搜寻合约完成,请求ID:{}", reqId);
						return true;
					} else {
						logger.error("搜寻合约错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return false;
					}
				}
			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("搜寻合约错误,请求ID:{},等待回报超时", reqId);
				return false;
			}

		}

	}

	@Override
	public boolean asyncGetContractList(Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询合约列表错误,参数reqId缺失");
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcGetContractListReq.Builder rpcGetContractListReqBuilder = RpcGetContractListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetContractListReqBuilder.build().toByteString(), reqId, RpcId.GET_CONTRACT_LIST_REQ);

		}
	}

	@Override
	public RpcGetContractListRsp getContractList(Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetContractList(targetNodeId, reqId)) {
			rpcServerRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetContractListRsp rpcGetContractListRsp = rpcServerRspHandlerService.getAndRemoveRpcGetContractListRsp(reqId);
				if (rpcGetContractListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("查询合约列表错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcGetContractListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询合约列表完成,请求ID:{}", reqId);
						return rpcGetContractListRsp;
					} else {
						logger.error("查询合约列表错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询合约列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetTickList(Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询Tick列表错误,参数reqId缺失");
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcGetTickListReq.Builder rpcGetTickListReqBuilder = RpcGetTickListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetTickListReqBuilder.build().toByteString(), reqId, RpcId.GET_TICK_LIST_REQ);

		}
	}

	@Override
	public RpcGetTickListRsp getTickList(Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetTickList(targetNodeId, reqId)) {
			rpcServerRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetTickListRsp rpcGetTickListRsp = rpcServerRspHandlerService.getAndRemoveRpcGetTickListRsp(reqId);
				if (rpcGetTickListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("查询Tick列表错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcGetTickListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询Tick列表完成,请求ID:{}", reqId);
						return rpcGetTickListRsp;
					} else {
						logger.error("查询Tick列表错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询Tick列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetOrderList(Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询定单列表错误,参数reqId缺失");
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcGetOrderListReq.Builder rpcGetOrderListReqBuilder = RpcGetOrderListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetOrderListReqBuilder.build().toByteString(), reqId, RpcId.GET_ORDER_LIST_REQ);

		}
	}

	@Override
	public RpcGetOrderListRsp getOrderList(Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetOrderList(targetNodeId, reqId)) {
			rpcServerRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetOrderListRsp rpcGetOrderListRsp = rpcServerRspHandlerService.getAndRemoveRpcGetOrderListRsp(reqId);
				if (rpcGetOrderListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("查询定单列表错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcGetOrderListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询定单列表完成,请求ID:{}", reqId);
						return rpcGetOrderListRsp;
					} else {
						logger.error("查询定单列表错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询定单列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetPositionList(Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询持仓列表错误,参数reqId缺失");
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcGetPositionListReq.Builder rpcGetPositionListReqBuilder = RpcGetPositionListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetPositionListReqBuilder.build().toByteString(), reqId, RpcId.GET_POSITION_LIST_REQ);

		}
	}

	@Override
	public RpcGetPositionListRsp getPositionList(Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetPositionList(targetNodeId, reqId)) {
			rpcServerRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetPositionListRsp rpcGetPositionListRsp = rpcServerRspHandlerService.getAndRemoveRpcGetPositionListRsp(reqId);
				if (rpcGetPositionListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("查询持仓列表错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcGetPositionListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询持仓列表完成,请求ID:{}", reqId);
						return rpcGetPositionListRsp;
					} else {
						logger.error("查询持仓列表错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询持仓列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetTradeList(Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询成交列表错误,参数reqId缺失");
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcGetTradeListReq.Builder rpcGetTradeListReqBuilder = RpcGetTradeListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetTradeListReqBuilder.build().toByteString(), reqId, RpcId.GET_TRADE_LIST_REQ);
		}
	}

	@Override
	public RpcGetTradeListRsp getTradeList(Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetTradeList(targetNodeId, reqId)) {
			rpcServerRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetTradeListRsp rpcGetTradeListRsp = rpcServerRspHandlerService.getAndRemoveRpcGetTradeListRsp(reqId);
				if (rpcGetTradeListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("查询成交列表错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcGetTradeListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询成交列表完成,请求ID:{}", reqId);
						return rpcGetTradeListRsp;
					} else {
						logger.error("查询成交列表错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询成交列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetAccountList(Integer targetNodeId, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询账户列表错误,参数reqId缺失");
			return false;
		} else {

			Integer sourceNodeId = 0;

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(sourceNodeId) //
					.setTargetNodeId(targetNodeId) //
					.setOperatorId(masterOperatorId) //
					.setReqId(reqId);

			RpcGetAccountListReq.Builder rpcGetAccountListReqBuilder = RpcGetAccountListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcServerProcessService.sendCoreRpc(targetNodeId, rpcGetAccountListReqBuilder.build().toByteString(), reqId, RpcId.GET_ACCOUNT_LIST_REQ);
		}
	}

	@Override
	public RpcGetAccountListRsp getAccountList(Integer targetNodeId, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetAccountList(targetNodeId, reqId)) {
			rpcServerRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetAccountListRsp rpcGetAccountListRsp = rpcServerRspHandlerService.getAndRemoveRpcGetAccountListRsp(reqId);
				if (rpcGetAccountListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcServerRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("查询账户列表错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcGetAccountListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询账户列表完成,请求ID:{}", reqId);
						return rpcGetAccountListRsp;
					} else {
						logger.error("查询账户列表错误,请求ID:{},错误信息:{}", reqId, commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcServerRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询账户列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

}
