package xyz.redtorch.node.slave.rpc.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.node.slave.rpc.service.RpcClientReqHandlerService;
import xyz.redtorch.node.slave.rpc.service.RpcClientRspHandlerService;
import xyz.redtorch.node.slave.web.socket.WebSocketClientHandler;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListReq;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractReq;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataRsp;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.ContentType;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.RpcType;

@Service
public class RpcClientProcessServiceImpl implements RpcClientProcessService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientProcessServiceImpl.class);

	@Value("${rt.rpc.client.node-id}")
	private int nodeId;

	@Autowired
	private RpcClientReqHandlerService rpcClientReqHandlerService;
	@Autowired
	private RpcClientRspHandlerService rpcClientRspHandlerService;
	@Autowired
	private WebSocketClientHandler webSocketClientHandler;

	@Value("${rt.rpc.client.threads-normal}")
	private Integer threadsNormal;

	private ExecutorService normalExecutorService; //
	private ExecutorService importantExecutorService = Executors.newCachedThreadPool();

	@Override
	public void afterPropertiesSet() throws Exception {
		normalExecutorService = Executors.newFixedThreadPool(threadsNormal);
	}

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

		logger.info("处理DEP记录,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{}", sourceNodeId, rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);

		ByteString contentByteString;
		if (contentType == ContentType.COMPRESSED_LZ4) {
			try (InputStream in = new ByteArrayInputStream(dep.getContentBytes().toByteArray());
					BufferedInputStream bin = new BufferedInputStream(in);
					LZ4FrameInputStream zIn = new LZ4FrameInputStream(bin);) {

				contentByteString = ByteString.readFrom(zIn);
			} catch (Exception e) {
				logger.error("处理DEP异常,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}时间戳:{},无法使用LZ4正确解析报文内容", sourceNodeId, rpcTypeValueName, rpcId, reqId, timestamp, e);
				sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "无法使用LZ4正确解析报文内容");
				return;
			}

		} else if (contentType == ContentType.ROUTINE) {
			contentByteString = dep.getContentBytes();
		} else {
			logger.error("处理DEP错误，来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{},不支持的报文类型", sourceNodeId, rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);
			sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "不支持的报文类型");
			return;
		}

		if (contentByteString == null || contentByteString.size() <= 0) {
			logger.error("处理DEP错误，来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{},报文内容长度错误", sourceNodeId, rpcTypeValueName, rpcId, contentTypeValueName, timestamp);
			sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "报文内容长度错误");
			return;
		}

		if (dep.getRpcType() != RpcType.CORE_RPC) {
			logger.error("处理DEP错误，来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{}内容类型:{},时间戳:{},未能识别的RPC类型", sourceNodeId, rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);
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
			importantExecutorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcSubmitOrderReq rpcSubmitOrderReq = RpcSubmitOrderReq.parseFrom(contentByteString);
						checkCommonReq(rpcSubmitOrderReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SUBMIT_ORDER_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.submitOrder(rpcSubmitOrderReq.getCommonReq(), rpcSubmitOrderReq.getSubmitOrderReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SUBMIT_ORDER_REQ", e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.CANCEL_ORDER_REQ_VALUE: {
			importantExecutorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcCancelOrderReq rpcCancelOrderReq = RpcCancelOrderReq.parseFrom(contentByteString);
						checkCommonReq(rpcCancelOrderReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:CANCEL_ORDER_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.cancelOrder(rpcCancelOrderReq.getCommonReq(), rpcCancelOrderReq.getCancelOrderReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:CANCEL_ORDER_REQ", e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.SEARCH_CONTRACT_REQ_VALUE: {
			importantExecutorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcSearchContractReq rpcSearchContractReq = RpcSearchContractReq.parseFrom(contentByteString);
						checkCommonReq(rpcSearchContractReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SEARCH_CONTRACT_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.searchContract(rpcSearchContractReq.getCommonReq(), rpcSearchContractReq.getContract());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SEARCH_CONTRACT_REQ", e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						RpcSyncSlaveNodeRuntimeDataRsp rpcSyncSlaveNodeRuntimeDataRsp = RpcSyncSlaveNodeRuntimeDataRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcSyncSlaveNodeRuntimeDataRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SYNC_SLAVE_NODE_RUNTIME_DATA_RSP", sourceNodeId, reqId);
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
			importantExecutorService.execute(new Runnable() {

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
		case RpcId.GET_TICK_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetTickListReq rpcGetTickListReq = RpcGetTickListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetTickListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_TICK_LIST_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.getTickList(rpcGetTickListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_TICK_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_POSITION_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetPositionListReq rpcGetPositionListReq = RpcGetPositionListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetPositionListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_POSITION_LIST_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.getPositionList(rpcGetPositionListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_POSITION_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_CONTRACT_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetContractListReq rpcGetContractListReq = RpcGetContractListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetContractListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_CONTRACT_LIST_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.getContractList(rpcGetContractListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_CONTRACT_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_ACCOUNT_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetAccountListReq rpcGetAccountListReq = RpcGetAccountListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetAccountListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_ACCOUNT_LIST_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.getAccountList(rpcGetAccountListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_ACCOUNT_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_ORDER_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetOrderListReq rpcGetOrderListReq = RpcGetOrderListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetOrderListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_ORDER_LIST_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.getOrderList(rpcGetOrderListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_ORDER_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_TRADE_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetTradeListReq rpcGetTradeListReq = RpcGetTradeListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetTradeListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_TRADE_LIST_REQ", sourceNodeId, reqId);
						rpcClientReqHandlerService.getTradeList(rpcGetTradeListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_TRADE_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
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
			throw new IllegalArgumentException("目标节点ID " + commonReq.getTargetNodeId() + "，与当前节点" + nodeId + "不匹配!");
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

	public void sendExceptionRsp(int targetNodeId, int originalRpcId, String originalReqId, long originalTimestamp, String info) {
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

	public boolean sendCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {
		logger.info("发送RPC记录,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, reqId, rpcId.getValueDescriptor().getName());
		if (content.size() > 262144) {
			return sendLz4CoreRpc(targetNodeId, content, reqId, rpcId);
		} else {
			return sendRoutineCoreRpc(targetNodeId, content, reqId, rpcId);
		}
	}

	public boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {
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
		ByteString contentByteString = ByteString.EMPTY;
		long beginTime = System.currentTimeMillis();
		try (InputStream in = new ByteArrayInputStream(content.toByteArray()); ByteArrayOutputStream bOut = new ByteArrayOutputStream(); LZ4FrameOutputStream lzOut = new LZ4FrameOutputStream(bOut);) {
			final byte[] buffer = new byte[10240];
			int n = 0;
			while (-1 != (n = in.read(buffer))) {
				lzOut.write(buffer, 0, n);
			}
			lzOut.close();
			in.close();
			contentByteString = ByteString.copyFrom(bOut.toByteArray());
			logger.info("发送RPC记录,目标节点ID:{},请求ID:{},RPC:{},压缩耗时{}ms,原始数据大小{},压缩后数据大小{},压缩率{}", targetNodeId, reqId, rpcId.getValueDescriptor().getName(), System.currentTimeMillis() - beginTime,
					content.size(), contentByteString.size(), contentByteString.size() / (double) content.size());
		} catch (Exception e) {
			logger.error("发送RPC错误,压缩异常,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, reqId, rpcId.getValueDescriptor().getName(), e);
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
		// NOP
	}

	@Override
	public void onHeartbeat(String result) {
		// NOP
	}
}
