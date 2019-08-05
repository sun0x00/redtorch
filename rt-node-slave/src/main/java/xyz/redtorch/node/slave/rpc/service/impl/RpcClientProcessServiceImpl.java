package xyz.redtorch.node.slave.rpc.service.impl;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.web.socket.WebSocketClientHandler;
import xyz.redtorch.node.slave.rpc.service.RpcClientReqHandlerService;
import xyz.redtorch.node.slave.rpc.service.RpcClientRspHandlerService;
import xyz.redtorch.node.slave.service.SlaveTradeCachesService;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreField.CommonRtnField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;
import xyz.redtorch.pb.CoreRpc.RpcAccountListRtn;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcContractListRtn;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcOrderListRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractReq;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;
import xyz.redtorch.pb.CoreRpc.RpcTickListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeListRtn;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.ContentType;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.RpcType;

@Service
public class RpcClientProcessServiceImpl implements RpcClientProcessService, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(RpcClientProcessServiceImpl.class);

	@Value("${rt.rpc.client.node-id}")
	private int nodeId;
	@Autowired
	private RpcClientReqHandlerService rpcClientReqHandlerService;
	@Autowired
	private RpcClientRspHandlerService rpcClientRspHandlerService;
	@Autowired
	private WebSocketClientHandler webSocketClientHandler;
	@Autowired
	private SlaveTradeCachesService slaveTradeCachesService;

	private ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();

	private ExecutorService executorService = Executors.newCachedThreadPool();

	@Override
	public void processData(byte[] data) {
		DataExchangeProtocol dep = null;
		try {
			dep = DataExchangeProtocol.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			logger.error("处理DEP错误,PB解析数据发生错误", e);
			logger.error("处理DEP错误,PB解析数据发生错误,原始数据HEX:{}", Hex.encodeHexString(data));
			return;
		}

		int sourceNodeId = dep.getSourceNodeId();
		int targetNodeId = dep.getTargetNodeId();

		if (targetNodeId != nodeId) {
			logger.error("处理DEP错误,目标节点ID不匹配当前节点ID:{},目标节点ID:{}", nodeId, targetNodeId);
			return;
		}

		int rpcId = dep.getRpcId();
		long timestamp = dep.getTimestamp();
		DataExchangeProtocol.ContentType contentType = dep.getContentType();
		String contentTypeValueName = contentType.getValueDescriptor().getName();
		RpcType rpcType = dep.getRpcType();
		String rpcTypeValueName = rpcType.getValueDescriptor().getName();
		String reqId = dep.getReqId();

		logger.info("处理DEP记录,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{}", sourceNodeId, rpcTypeValueName, rpcId,
				reqId, contentTypeValueName, timestamp);

		ByteString contentByteString;
		if (contentType == ContentType.COMPRESSED_LZ4) {
			try (InputStream in = new ByteArrayInputStream(dep.getContentBytes().toByteArray());
					BufferedInputStream bin = new BufferedInputStream(in);
					FramedLZ4CompressorInputStream zIn = new FramedLZ4CompressorInputStream(bin);) {

				contentByteString = ByteString.readFrom(zIn);
			} catch (IOException e) {
				logger.error("处理DEP异常,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}时间戳:{},无法使用LZ4正确解析报文内容", sourceNodeId,
						rpcTypeValueName, rpcId, reqId, timestamp, e);
				sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "无法使用LZ4正确解析报文内容");
				return;
			}

		} else if (contentType == ContentType.ROUTINE) {
			contentByteString = dep.getContentBytes();
		} else {
			logger.error("处理DEP错误，来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{},不支持的报文类型", sourceNodeId,
					rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);
			sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "不支持的报文类型");
			return;
		}

		if (contentByteString == null || contentByteString.size() <= 0) {
			logger.error("处理DEP错误，来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{},报文内容长度错误", sourceNodeId,
					rpcTypeValueName, rpcId, contentTypeValueName, timestamp);
			sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "报文内容长度错误");
			return;
		}

		if (dep.getRpcType() != RpcType.CORE_RPC) {
			logger.error("处理DEP错误，来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{},未能识别的RPC类型", sourceNodeId,
					rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);
			return;
		}

		doCoreRpc(sourceNodeId, rpcId, reqId, contentByteString, timestamp);

	}

	private void doCoreRpc(int sourceNodeId, int rpcId, String reqId, ByteString contentByteString, long timestamp) {

		switch (rpcId) {
		case RpcId.UNKNOWN_RPC_ID_VALUE: {
			logger.warn("处理RPC,来源节点ID:{},RPC ID:{}", sourceNodeId, rpcId);
			break;
		}
		case RpcId.SUBMIT_ORDER_REQ_VALUE: {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcSubmitOrderReq rpcSubmitOrderReq = RpcSubmitOrderReq.parseFrom(contentByteString);
						checkCommonReq(rpcSubmitOrderReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SUBMIT_ORDER_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.submitOrder(rpcSubmitOrderReq.getCommonReq(),
								rpcSubmitOrderReq.getSubmitOrderReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SUBMIT_ORDER_REQ", e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.CANCEL_ORDER_REQ_VALUE: {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcCancelOrderReq rpcCancelOrderReq = RpcCancelOrderReq.parseFrom(contentByteString);
						checkCommonReq(rpcCancelOrderReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:CANCEL_ORDER_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.cancelOrder(rpcCancelOrderReq.getCommonReq(),
								rpcCancelOrderReq.getCancelOrderReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:CANCEL_ORDER_REQ", e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.SEARCH_CONTRACT_REQ_VALUE: {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcSearchContractReq rpcSearchContractReq = RpcSearchContractReq.parseFrom(contentByteString);
						checkCommonReq(rpcSearchContractReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SEARCH_CONTRACT_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.searchContract(rpcSearchContractReq.getCommonReq(),
								rpcSearchContractReq.getContract());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SEARCH_CONTRACT_REQ", e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		case RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_RSP_VALUE: {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp = RpcSyncSlaveNodeRuntimeDataRsp
								.parseFrom(contentByteString);
						checkCommonRsp(rpcSyncSlaveNodeRuntimeDataRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SYNC_SLAVE_NODE_RUNTIME_DATA_RSP", sourceNodeId,
								reqId);
						rpcClientRspHandlerService.onSyncSlaveNodeRuntimeDataRsp(rpcSyncSlaveNodeRuntimeDataRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SYNC_SLAVE_NODE_RUNTIME_DATA_RSP", e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.EXCEPTION_RSP_VALUE: {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcExceptionRsp rpcExceptionRsp = RpcExceptionRsp.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:EXCEPTION_RSP", sourceNodeId, reqId);
						rpcClientRspHandlerService.onExceptionRsp(rpcExceptionRsp);

					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:EXCEPTION_RSP", e);
					}
				}
			});
			break;
		}
		default:
			logger.error("处理RPC错误,来源节点ID:{},RPC ID:{},请求ID:{}不支持此功能", sourceNodeId, rpcId, reqId);
			sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "不支持此功能");
			break;
		}

	}

	private void checkCommonRsp(CommonRspField commonRsp, int sourceNodeId, String reqId) {
		if (commonRsp == null) {
			logger.error("参数commonRsp缺失");
			throw new IllegalArgumentException("参数commonRsp缺失");
		}

		if (sourceNodeId != commonRsp.getSourceNodeId()) {
			throw new IllegalArgumentException("不合法的来源节点ID:" + commonRsp.getSourceNodeId());
		}

		if (commonRsp.getTargetNodeId() != nodeId) {
			throw new IllegalArgumentException("目标节点ID不为" + nodeId + "!");
		}

		if (StringUtils.isBlank(commonRsp.getReqId())) {
			logger.error("参数reqId缺失");
			throw new IllegalArgumentException("参数reqId缺失");
		}

		if (!commonRsp.getReqId().equals(reqId)) {
			logger.error("请求ID不匹配");
			throw new IllegalArgumentException("请求ID不匹配");
		}
	}

	private void checkCommonReq(CommonReqField commonReq, int sourceNodeId, String reqId) {
		if (commonReq == null) {
			logger.error("参数commonReq缺失");
			throw new IllegalArgumentException("参数commonReq缺失");
		}

		if (sourceNodeId != commonReq.getSourceNodeId()) {
			throw new IllegalArgumentException("不合法的来源节点ID:" + commonReq.getSourceNodeId());
		}

		if (commonReq.getTargetNodeId() != nodeId) {
			throw new IllegalArgumentException("目标节点ID不为当前节点!");
		}

		if (StringUtils.isBlank(commonReq.getReqId())) {
			logger.error("参数reqId缺失");
			throw new IllegalArgumentException("参数reqId缺失");
		}

		if (!commonReq.getReqId().equals(reqId)) {
			logger.error("请求ID不匹配");
			throw new IllegalArgumentException("请求ID不匹配");
		}

		if (StringUtils.isBlank(commonReq.getOperatorId())) {
			logger.error("参数operatorId缺失");
			throw new IllegalArgumentException("参数operatorId缺失");
		}

	}

	public void sendExceptionRsp(int targetNodeId, int originalRpcId, String originalReqId, long originalTimestamp,
			String info) {
		if (info == null) {
			info = "";
		}
		ByteString content = RpcExceptionRsp.newBuilder() //
				.setOriginalRpcId(originalRpcId) //
				.setOriginalReqId(originalReqId) //
				.setOriginalTimestamp(originalTimestamp) //
				.setInfo(info) //
				.build().toByteString();
		sendRoutineCoreRpc(targetNodeId, content, originalReqId, RpcId.EXCEPTION_RSP);
	}

	public boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {
		logger.info("发送RPC记录,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, reqId, rpcId.getValueDescriptor().getName());

		DataExchangeProtocol.Builder depBuilder = DataExchangeProtocol.newBuilder() //
				.setRpcId(rpcId.getNumber()) //
				.setReqId(reqId) //
				.setContentType(ContentType.ROUTINE) //
				.setSourceNodeId(nodeId) //
				.setTargetNodeId(targetNodeId) //
				.setTimestamp(System.currentTimeMillis()) //
				.setContentBytes(content);

		if (!webSocketClientHandler.sendData(depBuilder.build().toByteArray())) {
			logger.error("发送RPC错误,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, reqId, rpcId.getValueDescriptor().getName());
			return false;
		}
		return true;
	}

	public boolean sendLz4CoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {
		logger.info("发送RPC记录,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, reqId, rpcId.getValueDescriptor().getName());

		ByteString contentByteString = null;
		try (InputStream in = new ByteArrayInputStream(content.toByteArray());
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				FramedLZ4CompressorOutputStream lzOut = new FramedLZ4CompressorOutputStream(bOut);) {
			lzOut.write(in.readAllBytes());
			lzOut.close();
			contentByteString = ByteString.copyFrom(bOut.toByteArray());
		} catch (IOException e) {
			logger.error("发送RPC错误,压缩异常,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, reqId,
					rpcId.getValueDescriptor().getName(), e);
			return false;
		}

		DataExchangeProtocol.Builder depBuilder = DataExchangeProtocol.newBuilder() //
				.setRpcId(rpcId.getNumber()) //
				.setReqId(reqId) //
				.setContentType(ContentType.COMPRESSED_LZ4) //
				.setSourceNodeId(nodeId) //
				.setTargetNodeId(targetNodeId) //
				.setTimestamp(System.currentTimeMillis()) //
				.setContentBytes(contentByteString);

		if (!webSocketClientHandler.sendData(depBuilder.build().toByteArray())) {
			logger.error("发送RPC错误,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, rpcId.getValueDescriptor().getName(), rpcId);
			return false;
		}
		return true;
	}

	@Override
	public void onWsClosed() {
		// NOP

	}

	@Override
	public void onWsError() {
		// NOP
	}

	@Override
	public void onWsConnected() {
		this.sendCache();
	}

	private void sendCache() {
		CommonRtnField commonRtn = CommonRtnField.newBuilder().setSourceNodeId(nodeId).setTargetNodeId(0).build();

		List<OrderField> orderList = slaveTradeCachesService.getOrderList();
		RpcOrderListRtn.Builder rpcOrderListRtnBuilder = RpcOrderListRtn.newBuilder();
		rpcOrderListRtnBuilder.setCommonRtn(commonRtn);
		rpcOrderListRtnBuilder.addAllOrder(orderList);
		sendLz4CoreRpc(0, rpcOrderListRtnBuilder.build().toByteString(), "", RpcId.ORDER_LIST_RTN);

		List<TradeField> tradeList = slaveTradeCachesService.getTradeList();
		RpcTradeListRtn.Builder rpcTradeListRtnBuilder = RpcTradeListRtn.newBuilder();
		rpcTradeListRtnBuilder.setCommonRtn(commonRtn);
		rpcTradeListRtnBuilder.addAllTrade(tradeList);
		sendLz4CoreRpc(0, rpcTradeListRtnBuilder.build().toByteString(), "", RpcId.TRADE_LIST_RTN);

		List<AccountField> accountList = slaveTradeCachesService.getAccountList();
		RpcAccountListRtn.Builder rpcAccountListRtnBuilder = RpcAccountListRtn.newBuilder();
		rpcAccountListRtnBuilder.setCommonRtn(commonRtn);
		rpcAccountListRtnBuilder.addAllAccount(accountList);
		sendLz4CoreRpc(0, rpcAccountListRtnBuilder.build().toByteString(), "", RpcId.ACCOUNT_LIST_RTN);

		List<PositionField> positionList = slaveTradeCachesService.getPositionList();
		RpcPositionListRtn.Builder rpcPositionListRtnBuilder = RpcPositionListRtn.newBuilder();
		rpcPositionListRtnBuilder.setCommonRtn(commonRtn);
		rpcPositionListRtnBuilder.addAllPosition(positionList);
		sendLz4CoreRpc(0, rpcPositionListRtnBuilder.build().toByteString(), "", RpcId.POSITION_LIST_RTN);

		List<ContractField> contractList = slaveTradeCachesService.getContractList();
		RpcContractListRtn.Builder rpcContractListRtnBuilder = RpcContractListRtn.newBuilder();
		rpcContractListRtnBuilder.setCommonRtn(commonRtn);
		rpcContractListRtnBuilder.addAllContract(contractList);
		sendLz4CoreRpc(0, rpcContractListRtnBuilder.build().toByteString(), "", RpcId.CONTRACT_LIST_RTN);

		List<TickField> tickList = slaveTradeCachesService.getTickList();
		RpcTickListRtn.Builder rpcTickListRtnBuilder = RpcTickListRtn.newBuilder();
		rpcTickListRtnBuilder.setCommonRtn(commonRtn);
		rpcTickListRtnBuilder.addAllTick(tickList);
		sendLz4CoreRpc(0, rpcTickListRtnBuilder.build().toByteString(), "", RpcId.TICK_LIST_RTN);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			try {
				this.sendCache();
			} catch (Exception e) {
				logger.error("定时发送缓存异常", e);
			}
		}, 45, 45, TimeUnit.SECONDS);

	}

}
