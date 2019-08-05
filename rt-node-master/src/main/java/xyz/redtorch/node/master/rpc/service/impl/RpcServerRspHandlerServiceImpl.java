package xyz.redtorch.node.master.rpc.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import xyz.redtorch.node.master.rpc.service.RpcServerRspHandlerService;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;

/**
 * 
 * @author sun0x00@gmail.com
 *
 */
@Service
public class RpcServerRspHandlerServiceImpl implements RpcServerRspHandlerService {

	private Logger logger = LoggerFactory.getLogger(RpcServerRspHandlerServiceImpl.class);

	private Set<String> waitReqIdSet = ConcurrentHashMap.newKeySet();

	private Map<String, RpcSubscribeRsp> rpcSubscribeRspMap = new ConcurrentHashMap<>(100000);
	private Map<String, RpcUnsubscribeRsp> rpcUnsubscribeRspMap = new ConcurrentHashMap<>(100000);
	private Map<String, RpcSubmitOrderRsp> rpcSubmitOrderRspMap = new ConcurrentHashMap<>(100000);
	private Map<String, RpcCancelOrderRsp> rpcCancelOrderRspMap = new ConcurrentHashMap<>(100000);
	private Map<String, RpcSearchContractRsp> rpcSearchContractRspMap = new ConcurrentHashMap<>(100000);
	private Map<String, RpcExceptionRsp> rpcExceptionRspMap = new ConcurrentHashMap<>(100000);

	@Override
	public RpcSubscribeRsp getAndRemoveRpcSubscribeRsp(String reqId) {
		RpcSubscribeRsp rpcSubscribeRsp = rpcSubscribeRspMap.remove(reqId);
		if (rpcSubscribeRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcSubscribeRsp;
	}

	@Override
	public RpcUnsubscribeRsp getAndRemoveRpcUnsubscribeRsp(String reqId) {
		RpcUnsubscribeRsp rpcUnsubscribeRsp = rpcUnsubscribeRspMap.remove(reqId);
		if (rpcUnsubscribeRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcUnsubscribeRsp;
	}

	@Override
	public RpcSubmitOrderRsp getAndRemoveRpcSubmitOrderRsp(String reqId) {
		RpcSubmitOrderRsp rpcSubmitOrderRsp = rpcSubmitOrderRspMap.remove(reqId);
		if (rpcSubmitOrderRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcSubmitOrderRsp;
	}

	@Override
	public RpcCancelOrderRsp getAndRemoveRpcCancelOrderRsp(String reqId) {
		RpcCancelOrderRsp rpcCancelOrderRsp = rpcCancelOrderRspMap.remove(reqId);
		if (rpcCancelOrderRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcCancelOrderRsp;
	}

	@Override
	public RpcSearchContractRsp getAndRemoveRpcSearchContractRsp(String reqId) {
		RpcSearchContractRsp rpcSearchContractRsp = rpcSearchContractRspMap.remove(reqId);
		if (rpcSearchContractRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcSearchContractRsp;
	}

	@Override
	public RpcExceptionRsp getAndRemoveRpcExceptionRsp(String reqId) {
		RpcExceptionRsp rpcExceptionRsp = rpcExceptionRspMap.remove(reqId);
		if (rpcExceptionRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcExceptionRsp;
	}

	@Override
	public void onSubscribeRsp(RpcSubscribeRsp rpcSubscribeRsp) {
		String reqId = rpcSubscribeRsp.getCommonRsp().getReqId();
		if (waitReqIdSet.contains(reqId)) {
			rpcSubscribeRspMap.put(reqId, rpcSubscribeRsp);
		} else {
			logger.info("直接丢弃的回报,请求ID:{}", reqId);
		}
	}

	@Override
	public void onUnsubscribeRsp(RpcUnsubscribeRsp rpcUnsubscribeRsp) {
		String reqId = rpcUnsubscribeRsp.getCommonRsp().getReqId();
		if (waitReqIdSet.contains(reqId)) {
			rpcUnsubscribeRspMap.put(reqId, rpcUnsubscribeRsp);
		} else {
			logger.info("直接丢弃的回报,请求ID:{}", reqId);
		}
	}

	@Override
	public void onSubmitOrderRsp(RpcSubmitOrderRsp rpcSubmitOrderRsp) {
		String reqId = rpcSubmitOrderRsp.getCommonRsp().getReqId();
		if (waitReqIdSet.contains(reqId)) {
			rpcSubmitOrderRspMap.put(reqId, rpcSubmitOrderRsp);
		} else {
			logger.info("直接丢弃的回报,请求ID:{}", reqId);
		}
	}

	@Override
	public void onCancelOrderRsp(RpcCancelOrderRsp rpcCancelOrderRsp) {
		String reqId = rpcCancelOrderRsp.getCommonRsp().getReqId();
		if (waitReqIdSet.contains(reqId)) {
			rpcCancelOrderRspMap.put(reqId, rpcCancelOrderRsp);
		} else {
			logger.info("直接丢弃的回报,请求ID:{}", reqId);
		}
	}

	@Override
	public void onSearchContractRsp(RpcSearchContractRsp rpcSearchContractRsp) {
		String reqId = rpcSearchContractRsp.getCommonRsp().getReqId();
		if (waitReqIdSet.contains(reqId)) {
			rpcSearchContractRspMap.put(reqId, rpcSearchContractRsp);
		} else {
			logger.info("直接丢弃的回报,请求ID:{}", reqId);
		}

	}

	@Override
	public void onExceptionRsp(RpcExceptionRsp rpcExceptionRsp) {
		if (StringUtils.isNotBlank(rpcExceptionRsp.getOriginalReqId())) {
			logger.error("接收到异常回报,请求ID:{},异常信息:{}", rpcExceptionRsp.getOriginalReqId(), rpcExceptionRsp.getInfo());
			if (waitReqIdSet.contains(rpcExceptionRsp.getOriginalReqId())) {
				rpcExceptionRspMap.put(rpcExceptionRsp.getOriginalReqId(), rpcExceptionRsp);
			}

		} else {
			logger.error("接收到未知请求ID的异常回报,异常信息:{}", rpcExceptionRsp.getInfo());
		}
	}

	@Override
	public void registerWaitReqId(String reqId) {
		waitReqIdSet.add(reqId);
	}

	@Override
	public void unregisterWaitReqId(String reqId) {
		waitReqIdSet.remove(reqId);
	}

}
