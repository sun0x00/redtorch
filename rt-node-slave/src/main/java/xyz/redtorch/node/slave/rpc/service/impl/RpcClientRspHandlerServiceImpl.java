package xyz.redtorch.node.slave.rpc.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import xyz.redtorch.node.slave.rpc.service.RpcClientRspHandlerService;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;

@Service
public class RpcClientRspHandlerServiceImpl implements RpcClientRspHandlerService {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientRspHandlerServiceImpl.class);

	private Map<String, RpcSyncSlaveNodeRuntimeDataRsp> rpcSyncSlaveNodeRuntimeDataRspMap = new ConcurrentHashMap<>(100000);

	private Map<String, RpcExceptionRsp> rpcExceptionRspMap = new ConcurrentHashMap<>(100000);

	private Set<String> waitReqIdSet = ConcurrentHashMap.newKeySet();

	@Override
	public void onSyncSlaveNodeRuntimeDataRsp(RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp) {
		String reqId = rpcSyncSlaveNodeRuntimeDataRsp.getCommonRsp().getReqId();
		if (waitReqIdSet.contains(reqId)) {
			rpcSyncSlaveNodeRuntimeDataRspMap.put(reqId, rpcSyncSlaveNodeRuntimeDataRsp);
		} else {
			logger.info("直接丢弃的回报,请求ID:{}", reqId);
		}
	}

	@Override
	public RpcSyncSlaveNodeRuntimeDataRsp getAndRemoveSyncSlaveNodeRuntimeDataResult(String reqId) {
		RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp = rpcSyncSlaveNodeRuntimeDataRspMap.remove(reqId);
		if (rpcSyncSlaveNodeRuntimeDataRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcSyncSlaveNodeRuntimeDataRsp;
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
	public RpcExceptionRsp getAndRemoveRpcExceptionRsp(String reqId) {
		RpcExceptionRsp rpcExceptionRsp = rpcExceptionRspMap.remove(reqId);
		if (rpcExceptionRsp != null) {
			waitReqIdSet.remove(reqId);
		}
		return rpcExceptionRsp;
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
