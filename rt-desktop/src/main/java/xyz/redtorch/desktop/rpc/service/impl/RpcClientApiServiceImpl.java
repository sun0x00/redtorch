package xyz.redtorch.desktop.rpc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.rpc.service.RpcClientRspHandlerService;
import xyz.redtorch.desktop.service.AuthService;
import xyz.redtorch.pb.CoreEnum.BarCycleEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
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
import xyz.redtorch.pb.CoreRpc.RpcGetMixContractListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetMixContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBBarListReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBTickListReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryVolumeBarListReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryVolumeBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractReq;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeReq;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeReq;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;

@Service
public class RpcClientApiServiceImpl implements RpcClientApiService {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientApiServiceImpl.class);

	@Value("${rt.rpc.client.sync-default-timeout-seconds}")
	private int defaultRpcTimeoutSeconds;
	@Autowired
	private RpcClientProcessService rpcClientProcessService;
	@Autowired
	private RpcClientRspHandlerService rpcClientRspHandlerService;
	@Autowired
	private AuthService authService;

	@Override
	public boolean asyncSubscribe(ContractField contract, String reqId) {

		if (contract == null) {
			logger.error("订阅错误,参数contract缺失");
			return false;
		} else if (StringUtils.isBlank(reqId)) {
			logger.error("订阅错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcSubscribeReq.Builder rpcSubscribeReqBuilder = RpcSubscribeReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setContract(contract);

			return rpcClientProcessService.sendCoreRpc(0, rpcSubscribeReqBuilder.build().toByteString(), reqId, RpcId.SUBSCRIBE_REQ);
		}

	}

	@Override
	public boolean subscribe(ContractField contract, String reqId, Integer timeoutSeconds) {

		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		// 先注册请求ID避免回报先到
		rpcClientRspHandlerService.registerWaitReqId(reqId);

		if (!asyncSubscribe(contract, reqId)) {
			rpcClientRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcSubscribeRsp rpcSubscribeRsp = rpcClientRspHandlerService.getAndRemoveRpcSubscribeRsp(reqId);
				if (rpcSubscribeRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("订阅完成");
						return true;
					} else {
						logger.error("订阅错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return false;
					}
				}
			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("订阅错误,请求ID:{},等待回报超时", reqId);
				return false;
			}
		}

	}

	@Override
	public boolean asyncUnsubscribe(ContractField contract, String reqId) {

		if (contract == null) {
			logger.error("取消订阅错误,参数contract缺失");
			return false;
		} else if (StringUtils.isBlank(reqId)) {
			logger.error("取消订阅错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcUnsubscribeReq.Builder rpcUnsubscribeReqBuilder = RpcUnsubscribeReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setContract(contract);

			return rpcClientProcessService.sendCoreRpc(0, rpcUnsubscribeReqBuilder.build().toByteString(), reqId, RpcId.UNSUBSCRIBE_REQ);

		}

	}

	@Override
	public boolean unsubscribe(ContractField contract, String reqId, Integer timeoutSeconds) {

		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		// 先注册请求ID避免回报先到
		rpcClientRspHandlerService.registerWaitReqId(reqId);

		if (!asyncUnsubscribe(contract, reqId)) {
			rpcClientRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcUnsubscribeRsp rpcUnsubscribeRsp = rpcClientRspHandlerService.getAndRemoveRpcUnsubscribeRsp(reqId);
				if (rpcUnsubscribeRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("取消订阅完成");
						return true;
					} else {
						logger.error("取消订阅错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return false;
					}
				}
			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("取消订阅错误,等待回报超时", reqId);
				return false;
			}

		}

	}

	@Override
	public boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, String reqId) {
		if (submitOrderReq == null) {
			logger.error("提交定单错误,参数submitOrderReq缺失");
			return false;
		} else if (StringUtils.isBlank(reqId)) {
			logger.error("提交定单错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcSubmitOrderReq.Builder rpcSubmitOrderReqBuilder = RpcSubmitOrderReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setSubmitOrderReq(submitOrderReq); //

			return rpcClientProcessService.sendCoreRpc(0, rpcSubmitOrderReqBuilder.build().toByteString(), reqId, RpcId.SUBMIT_ORDER_REQ);

		}
	}

	@Override
	public String submitOrder(SubmitOrderReqField submitOrderReq, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		rpcClientRspHandlerService.registerWaitReqId(reqId);

		if (!asyncSubmitOrder(submitOrderReq, reqId)) {
			rpcClientRspHandlerService.unregisterWaitReqId(reqId);
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcSubmitOrderRsp rpcSubmitOrderRsp = rpcClientRspHandlerService.getAndRemoveRpcSubmitOrderRsp(reqId);
				if (rpcSubmitOrderRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("提交定单完成");
						return rpcSubmitOrderRsp.getOrderId();
					} else {
						logger.error("提交定单错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("提交定单错误,请求ID:{},等待回报超时", reqId);
				return null;
			}
		}

	}

	@Override
	public boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, String reqId) {
		if (cancelOrderReq == null) {
			logger.error("撤销定单错误,参数cancelOrderReq缺失");
			return false;
		} else if (StringUtils.isBlank(reqId)) {
			logger.error("撤销定单错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcCancelOrderReq.Builder rpcCancelOrderReqBuilder = RpcCancelOrderReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setCancelOrderReq(cancelOrderReq);

			return rpcClientProcessService.sendCoreRpc(0, rpcCancelOrderReqBuilder.build().toByteString(), reqId, RpcId.CANCEL_ORDER_REQ);

		}
	}

	@Override
	public boolean cancelOrder(CancelOrderReqField cancelOrderReq, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		rpcClientRspHandlerService.registerWaitReqId(reqId);

		if (!asyncCancelOrder(cancelOrderReq, reqId)) {
			rpcClientRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcCancelOrderRsp rpcCancelOrderRsp = rpcClientRspHandlerService.getAndRemoveRpcCancelOrderRsp(reqId);
				if (rpcCancelOrderRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("撤销定单完成");
						return true;
					} else {
						logger.error("撤销定单错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return false;
					}
				}
			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("撤销定单错误,请求ID:{},等待回报超时", reqId);
				return false;
			}

		}
	}

	@Override
	public boolean asyncSearchContract(ContractField contract, String reqId) {
		if (contract == null) {
			logger.error("搜寻合约错误,参数contract缺失");
			return false;
		} else if (StringUtils.isBlank(reqId)) {
			logger.error("搜寻合约错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcSearchContractReq.Builder rpcSearchContractReqBuilder = RpcSearchContractReq.newBuilder() //
					.setCommonReq(commonReqBuilder) //
					.setContract(contract);

			return rpcClientProcessService.sendCoreRpc(0, rpcSearchContractReqBuilder.build().toByteString(), reqId, RpcId.SEARCH_CONTRACT_REQ);

		}
	}

	@Override
	public boolean searchContract(ContractField contract, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {

			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		rpcClientRspHandlerService.registerWaitReqId(reqId);

		if (!asyncSearchContract(contract, reqId)) {
			rpcClientRspHandlerService.unregisterWaitReqId(reqId);
			return false;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcCancelOrderRsp rpcCancelOrderRsp = rpcClientRspHandlerService.getAndRemoveRpcCancelOrderRsp(reqId);
				if (rpcCancelOrderRsp == null) {
					RpcExceptionRsp rpcExceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("搜寻合约完成");
						return true;
					} else {
						logger.error("搜寻合约错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return false;
					}
				}
			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("搜寻合约错误,请求ID:{},等待回报超时", reqId);
				return false;
			}

		}

	}

	@Override
	public boolean asyncGetContractList(String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询合约列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcGetContractListReq.Builder rpcGetContractListReqBuilder = RpcGetContractListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcClientProcessService.sendCoreRpc(0, rpcGetContractListReqBuilder.build().toByteString(), reqId, RpcId.GET_CONTRACT_LIST_REQ);

		}
	}

	@Override
	public RpcGetContractListRsp getContractList(String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetContractList(reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetContractListRsp rpcGetContractListRsp = rpcClientRspHandlerService.getAndRemoveRpcGetContractListRsp(reqId);
				if (rpcGetContractListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("查询合约列表完成");
						return rpcGetContractListRsp;
					} else {
						logger.error("查询合约列表完成,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询合约列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetMixContractList(String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询混合合约列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcGetMixContractListReq.Builder rpcGetMixContractListReqBuilder = RpcGetMixContractListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcClientProcessService.sendCoreRpc(0, rpcGetMixContractListReqBuilder.build().toByteString(), reqId, RpcId.GET_MIX_CONTRACT_LIST_REQ);

		}
	}

	@Override
	public RpcGetMixContractListRsp getMixContractList(String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetMixContractList(reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetMixContractListRsp rpcGetMixContractListRsp = rpcClientRspHandlerService.getAndRemoveRpcGetMixContractListRsp(reqId);
				if (rpcGetMixContractListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
					if (exceptionRsp != null) {
						logger.error("查询混合合约列表错误,请求ID:{},远程错误回报:{}", reqId, exceptionRsp.getInfo());
						return null;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// NOP
					}
				} else {
					CommonRspField commonRsp = rpcGetMixContractListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询混合合约列表完成");
						return rpcGetMixContractListRsp;
					} else {
						logger.error("查询混合合约列表完成,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询混合合约列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetTickList(String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询Tick列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcGetTickListReq.Builder rpcGetTickListReqBuilder = RpcGetTickListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcClientProcessService.sendCoreRpc(0, rpcGetTickListReqBuilder.build().toByteString(), reqId, RpcId.GET_TICK_LIST_REQ);

		}
	}

	@Override
	public RpcGetTickListRsp getTickList(String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetTickList(reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetTickListRsp rpcGetTickListRsp = rpcClientRspHandlerService.getAndRemoveRpcGetTickListRsp(reqId);
				if (rpcGetTickListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("查询Tick列表完成");
						return rpcGetTickListRsp;
					} else {
						logger.error("查询Tick列表完成,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询Tick列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetOrderList(String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询定单列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcGetOrderListReq.Builder rpcGetOrderListReqBuilder = RpcGetOrderListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcClientProcessService.sendCoreRpc(0, rpcGetOrderListReqBuilder.build().toByteString(), reqId, RpcId.GET_ORDER_LIST_REQ);

		}
	}

	@Override
	public RpcGetOrderListRsp getOrderList(String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetOrderList(reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetOrderListRsp rpcGetOrderListRsp = rpcClientRspHandlerService.getAndRemoveRpcGetOrderListRsp(reqId);
				if (rpcGetOrderListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("查询定单列表完成");
						return rpcGetOrderListRsp;
					} else {
						logger.error("查询定单列表完成,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询定单列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetPositionList(String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询持仓列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcGetPositionListReq.Builder rpcGetPositionListReqBuilder = RpcGetPositionListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcClientProcessService.sendCoreRpc(0, rpcGetPositionListReqBuilder.build().toByteString(), reqId, RpcId.GET_POSITION_LIST_REQ);

		}
	}

	@Override
	public RpcGetPositionListRsp getPositionList(String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetPositionList(reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetPositionListRsp rpcGetPositionListRsp = rpcClientRspHandlerService.getAndRemoveRpcGetPositionListRsp(reqId);
				if (rpcGetPositionListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("查询持仓列表完成");
						return rpcGetPositionListRsp;
					} else {
						logger.error("查询持仓列表错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询持仓列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetTradeList(String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询成交列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcGetTradeListReq.Builder rpcGetTradeListReqBuilder = RpcGetTradeListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcClientProcessService.sendCoreRpc(0, rpcGetTradeListReqBuilder.build().toByteString(), reqId, RpcId.GET_TRADE_LIST_REQ);
		}
	}

	@Override
	public RpcGetTradeListRsp getTradeList(String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetTradeList(reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetTradeListRsp rpcGetTradeListRsp = rpcClientRspHandlerService.getAndRemoveRpcGetTradeListRsp(reqId);
				if (rpcGetTradeListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("查询成交列表完成");
						return rpcGetTradeListRsp;
					} else {
						logger.error("查询成交列表错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询成交列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncGetAccountList(String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询账户列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcGetAccountListReq.Builder rpcGetAccountListReqBuilder = RpcGetAccountListReq.newBuilder() //
					.setCommonReq(commonReqBuilder);

			return rpcClientProcessService.sendCoreRpc(0, rpcGetAccountListReqBuilder.build().toByteString(), reqId, RpcId.GET_ACCOUNT_LIST_REQ);
		}
	}

	@Override
	public RpcGetAccountListRsp getAccountList(String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncGetAccountList(reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcGetAccountListRsp rpcGetAccountListRsp = rpcClientRspHandlerService.getAndRemoveRpcGetAccountListRsp(reqId);
				if (rpcGetAccountListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
						logger.info("查询账户列表完成");
						return rpcGetAccountListRsp;
					} else {
						logger.error("查询账户列表错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询账户列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncQueryDBBarList(long startTimestamp, long endTimestamp, String unifiedSymbol, BarCycleEnum barCycle, MarketDataDBTypeEnum marketDataDBType, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询历史Bar列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcQueryDBBarListReq.Builder rpcQueryDBBarListReqBuilder = RpcQueryDBBarListReq.newBuilder() //
					.setCommonReq(commonReqBuilder).setStartTimestamp(startTimestamp).setEndTimestamp(endTimestamp).setUnifiedSymbol(unifiedSymbol).setBarCycle(barCycle)
					.setMarketDataDBType(marketDataDBType);

			return rpcClientProcessService.sendCoreRpc(0, rpcQueryDBBarListReqBuilder.build().toByteString(), reqId, RpcId.QUERY_DB_BAR_LIST_REQ);

		}
	}

	@Override
	public RpcQueryDBBarListRsp queryDBBarList(long startTimestamp, long endTimestamp, String unifiedSymbol, BarCycleEnum barCycle, MarketDataDBTypeEnum marketDataDBType, String reqId,
			Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncQueryDBBarList(startTimestamp, endTimestamp, unifiedSymbol, barCycle, marketDataDBType, reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcQueryDBBarListRsp rpcQueryDBBarListRsp = rpcClientRspHandlerService.getAndRemoveRpcQueryDBBarListRsp(reqId);
				if (rpcQueryDBBarListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
					CommonRspField commonRsp = rpcQueryDBBarListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询历史Bar列表完成");
						return rpcQueryDBBarListRsp;
					} else {
						logger.error("查询历史Bar列表错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询历史Bar列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncQueryDBTickList(long startTimestamp, long endTimestamp, String unifiedSymbol, MarketDataDBTypeEnum marketDataDBType, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询历史Tick列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcQueryDBTickListReq.Builder rpcQueryDBTickListReqBuilder = RpcQueryDBTickListReq.newBuilder() //
					.setCommonReq(commonReqBuilder).setStartTimestamp(startTimestamp).setEndTimestamp(endTimestamp).setUnifiedSymbol(unifiedSymbol).setMarketDataDBType(marketDataDBType);

			return rpcClientProcessService.sendCoreRpc(0, rpcQueryDBTickListReqBuilder.build().toByteString(), reqId, RpcId.QUERY_DB_TICK_LIST_REQ);

		}
	}

	@Override
	public RpcQueryDBTickListRsp queryDBTickList(long startTimestamp, long endTimestamp, String unifiedSymbol, MarketDataDBTypeEnum marketDataDBType, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncQueryDBTickList(startTimestamp, endTimestamp, unifiedSymbol, marketDataDBType, reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcQueryDBTickListRsp rpcQueryDBTickListRsp = rpcClientRspHandlerService.getAndRemoveRpcQueryDBTickListRsp(reqId);
				if (rpcQueryDBTickListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
					CommonRspField commonRsp = rpcQueryDBTickListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询历史Tick列表完成");
						return rpcQueryDBTickListRsp;
					} else {
						logger.error("查询历史Tick列表错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询历史Tick列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}

	@Override
	public boolean asyncQueryVolumeBarList(long startTimestamp, long endTimestamp, String unifiedSymbol, int volume, String reqId) {
		if (StringUtils.isBlank(reqId)) {
			logger.error("查询历史Bar列表错误,参数reqId缺失");
			return false;
		} else {

			CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
					.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
					.setTargetNodeId(0) //
					.setOperatorId(authService.getOperatorId() == null ? "" : authService.getOperatorId()) //
					.setReqId(reqId);

			RpcQueryVolumeBarListReq.Builder rpcQueryVolumeBarListReqBuilder = RpcQueryVolumeBarListReq.newBuilder() //
					.setCommonReq(commonReqBuilder).setStartTimestamp(startTimestamp).setEndTimestamp(endTimestamp).setUnifiedSymbol(unifiedSymbol).setVolume(volume);

			return rpcClientProcessService.sendCoreRpc(0, rpcQueryVolumeBarListReqBuilder.build().toByteString(), reqId, RpcId.QUERY_VOLUME_BAR_LIST_REQ);

		}
	}

	@Override
	public RpcQueryVolumeBarListRsp queryVolumeBarList(long startTimestamp, long endTimestamp, String unifiedSymbol, int volume, String reqId, Integer timeoutSeconds) {
		Integer rpcTimeoutSeconds = timeoutSeconds;
		if (timeoutSeconds == null || timeoutSeconds < 1 || timeoutSeconds > 300) {
			rpcTimeoutSeconds = defaultRpcTimeoutSeconds;
		}

		if (StringUtils.isBlank(reqId)) {
			reqId = UUIDStringPoolUtils.getUUIDString();
		}

		long startTime = System.currentTimeMillis();

		if (asyncQueryVolumeBarList(startTimestamp, endTimestamp, unifiedSymbol, volume, reqId)) {
			rpcClientRspHandlerService.registerWaitReqId(reqId);
		} else {
			return null;
		}

		while (true) {
			if ((System.currentTimeMillis() - startTime) <= rpcTimeoutSeconds * 1000) {
				RpcQueryVolumeBarListRsp rpcQueryVolumeBarListRsp = rpcClientRspHandlerService.getAndRemoveRpcQueryVolumeBarListRsp(reqId);
				if (rpcQueryVolumeBarListRsp == null) {
					RpcExceptionRsp exceptionRsp = rpcClientRspHandlerService.getAndRemoveRpcExceptionRsp(reqId);
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
					CommonRspField commonRsp = rpcQueryVolumeBarListRsp.getCommonRsp();
					if (commonRsp.getErrorId() == 0) {
						logger.info("查询VolBar列表完成");
						return rpcQueryVolumeBarListRsp;
					} else {
						logger.error("查询VolBar列表错误,请求ID:{},错误ID:{},错误信息{}", commonRsp.getErrorId(), commonRsp.getErrorMsg());
						return null;
					}
				}

			} else {
				rpcClientRspHandlerService.unregisterWaitReqId(reqId);
				logger.error("查询历史Bar列表错误,请求ID:{},等待回报超时", reqId);
				return null;
			}

		}
	}
}
