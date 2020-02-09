package xyz.redtorch.node.master.rpc.service.impl;

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
import xyz.redtorch.node.master.rpc.service.RpcServerProcessService;
import xyz.redtorch.node.master.rpc.service.RpcServerReqHandlerService;
import xyz.redtorch.node.master.rpc.service.RpcServerRspHandlerService;
import xyz.redtorch.node.master.rpc.service.RpcServerRtnHandlerService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreRpc.RpcAccountListRtn;
import xyz.redtorch.pb.CoreRpc.RpcAccountRtn;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcContractListRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractRtn;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetMixContractListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListReq;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetWorkingOrderListReq;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcNoticeRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderListRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionRtn;
import xyz.redtorch.pb.CoreRpc.RpcQueryAccountByAccountIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryAccountListByAccountCodeReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryContractByContractIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryContractListByGatewayIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryContractListByUnifiedSymbolReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBBarListReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBTickListReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderByOrderIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderByOriginOrderIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderListByAccountIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryOrderListByUnifiedSymbolReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryPositionByPositionIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryPositionListByAccountIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryPositionListByUnifiedSymbolReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeByTradeIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByAccountIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByOrderIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByOriginOrderIdReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryTradeListByUnifiedSymbolReq;
import xyz.redtorch.pb.CoreRpc.RpcQueryVolumeBarListReq;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractReq;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderReq;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeReq;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcSyncSlaveNodeRuntimeDataReq;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeReq;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.ContentType;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.RpcType;
import xyz.redtorch.pb.CoreRpc.RpcTickListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeRtn;

@Service
public class RpcServerProcessServiceImpl implements RpcServerProcessService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(RpcServerProcessServiceImpl.class);

	@Autowired
	private WebSocketServerHandler webSocketServerHandler;
	@Autowired
	private RpcServerReqHandlerService rpcServerReqHandlerService;
	@Autowired
	private RpcServerRtnHandlerService rpcServerRtnHandlerService;
	@Autowired
	private RpcServerRspHandlerService rpcServerRspHandlerService;

	@Value("${rt.rpc.server.threads-normal}")
	private Integer threadsNormal;

	private ExecutorService normalExecutorService; //
	private ExecutorService importantExecutorService = Executors.newCachedThreadPool();
	private ExecutorService unimportantSingleThreadExecutorService = Executors.newSingleThreadExecutor();
	private ExecutorService tradeRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();
	private ExecutorService marketRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();

	@Override
	public void afterPropertiesSet() throws Exception {
		normalExecutorService = Executors.newFixedThreadPool(threadsNormal);
	}

	@Override
	public void processData(int sourceNodeId, byte[] data) {
		if (data == null) {
			logger.error("处理DEP错误,接收到空数据,来源节点ID:{}", sourceNodeId);
			return;
		}
		DataExchangeProtocol dep = null;
		try {
			dep = DataExchangeProtocol.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			logger.error("处理DEP错误,PB解析数据异常,来源节点ID:{}", sourceNodeId, e);
			logger.error("处理DEP错误,PB解析数据异常,来源节点ID:{},原始数据HEX:{}", sourceNodeId, Hex.encodeHexString(data));
			return;
		}

		int targetNodeId = dep.getTargetNodeId();

		if (sourceNodeId != dep.getSourceNodeId()) {
			logger.error("处理DEP错误,来源节点ID无法与报文对应,来源节点ID:{},报文中的来源节点ID:{}", sourceNodeId, dep.getSourceNodeId());
			return;
		}

		int rpcId = dep.getRpcId();
		long timestamp = dep.getTimestamp();
		DataExchangeProtocol.ContentType contentType = dep.getContentType();
		String contentTypeValueName = contentType.getValueDescriptor().getName();
		RpcType rpcType = dep.getRpcType();
		String rpcTypeValueName = rpcType.getValueDescriptor().getName();
		String reqId = dep.getReqId();

		if (targetNodeId != 0) {
			logger.info("转发DEP记录,来源节点ID:{},目标节点ID：{},RPC类型:{},RPC ID:{},请求ID:{},内容类型:{},时间戳:{}", sourceNodeId, targetNodeId, sourceNodeId, rpcTypeValueName, rpcId, reqId, contentTypeValueName,
					timestamp);
			logger.error("转发功能已经禁用!");

			/**
			 * 禁用转发功能,不安全
			 * 
			 * logger.info("转发DEP记录,来源节点ID:{},目标节点ID：{},RPC类型:{},RPC
			 * ID:{},请求ID:{},内容类型:{},时间戳:{}", sourceNodeId, targetNodeId, sourceNodeId,
			 * rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);
			 *
			 * if (webSocketServerHandler.sendDataByNodeId(targetNodeId, data)) {
			 * logger.error("转发DEP错误,来源节点ID:{},目标节点ID：{},RPC类型:{},RPC
			 * ID:{},请求ID:{},内容类型:{},时间戳:{}", sourceNodeId, targetNodeId, sourceNodeId,
			 * rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp); }
			 */

		} else {

			logger.info("处理DEP记录,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{},内容类型:{},时间戳:{}", sourceNodeId, rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);

			ByteString contentByteString;
			if (contentType == ContentType.COMPRESSED_LZ4) {
				try (InputStream in = new ByteArrayInputStream(dep.getContentBytes().toByteArray());
						BufferedInputStream bin = new BufferedInputStream(in);
						LZ4FrameInputStream zIn = new LZ4FrameInputStream(bin);) {
					contentByteString = ByteString.readFrom(zIn);
				} catch (Exception e) {
					logger.error("处理DEP错误,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{},时间戳:{},无法使用LZ4正确解析报文内容", sourceNodeId, sourceNodeId, rpcTypeValueName, rpcId, timestamp, e);
					sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "无法使用LZ4正确解析报文内容");
					return;
				}

			} else if (contentType == ContentType.ROUTINE) {
				contentByteString = dep.getContentBytes();
			} else {
				logger.error("处理DEP错误,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{},内容类型:{},时间戳:{},不支持的报文类型", sourceNodeId, sourceNodeId, rpcTypeValueName, rpcId, reqId, contentTypeValueName, timestamp);
				sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "不支持的报文类型");
				return;
			}

			if (contentByteString == null || contentByteString.size() <= 0) {
				logger.error("处理DEP错误,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{},时间戳:{},报文内容长度错误", sourceNodeId, sourceNodeId, rpcTypeValueName, rpcId, reqId, timestamp);
				sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, "报文内容长度错误");
				return;
			}

			if (dep.getRpcType() == RpcType.CORE_RPC) {
				doCoreRpc(sourceNodeId, rpcId, reqId, contentByteString, timestamp);
			} else {
				logger.error("处理DEP错误,来源节点ID:{},RPC类型:{},RPC ID:{},请求ID:{},时间戳:{},未能识别的RPC类型", sourceNodeId, dep.getRpcType().getValueDescriptor().getName(), rpcId, reqId, timestamp);
			}
		}
	}

	private void doCoreRpc(int sourceNodeId, int rpcId, String reqId, ByteString contentByteString, long timestamp) {

		switch (rpcId) {
		case RpcId.UNKNOWN_RPC_ID_VALUE: {
			logger.warn("处理RPC,来源节点ID:{},RPC:UNKNOWN_RPC_ID_VALUE", sourceNodeId, rpcId);
			break;
		}
		case RpcId.SUBSCRIBE_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcSubscribeReq rpcSubscribeReq = RpcSubscribeReq.parseFrom(contentByteString);
						checkCommonReq(rpcSubscribeReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SUBSCRIBE_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.subscribe(rpcSubscribeReq.getCommonReq(), rpcSubscribeReq.getContract());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SUBSCRIBE_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});

			break;
		}
		case RpcId.UNSUBSCRIBE_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcUnsubscribeReq rpcUnsubscribeReq = RpcUnsubscribeReq.parseFrom(contentByteString);
						checkCommonReq(rpcUnsubscribeReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:UNSUBSCRIBE_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.unsubscribe(rpcUnsubscribeReq.getCommonReq(), rpcUnsubscribeReq.getContract());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:UNSUBSCRIBE_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
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
						rpcServerReqHandlerService.submitOrder(rpcSubmitOrderReq.getCommonReq(), rpcSubmitOrderReq.getSubmitOrderReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SUBMIT_ORDER_REQ", sourceNodeId, e);
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
						rpcServerReqHandlerService.cancelOrder(rpcCancelOrderReq.getCommonReq(), rpcCancelOrderReq.getCancelOrderReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:CANCEL_ORDER_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.SEARCH_CONTRACT_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcSearchContractReq rpcSearchContractReq = RpcSearchContractReq.parseFrom(contentByteString);
						checkCommonReq(rpcSearchContractReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SEARCH_CONTRACT_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.searchContract(rpcSearchContractReq.getCommonReq(), rpcSearchContractReq.getContract());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SEARCH_CONTRACT_REQ", sourceNodeId, e);
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
						rpcServerReqHandlerService.getOrderList(rpcGetOrderListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_ORDER_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_WORKING_ORDER_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetWorkingOrderListReq rpcGetWorkingOrderListReq = RpcGetWorkingOrderListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetWorkingOrderListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_WORKING_ORDER_LIST_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.getWorkingOrderList(rpcGetWorkingOrderListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_WORKING_ORDER_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_ORDER_BY_ORDER_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryOrderByOrderIdReq rpcQueryOrderByOrderIdReq = RpcQueryOrderByOrderIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryOrderByOrderIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_ORDER_BY_ORDER_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryOrderByOrderId(rpcQueryOrderByOrderIdReq.getCommonReq(), rpcQueryOrderByOrderIdReq.getOrderId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_ORDER_BY_ORDER_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_ORDER_BY_ORIGIN_ORDER_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryOrderByOriginOrderIdReq rpcQueryOrderByOriginOrderIdReq = RpcQueryOrderByOriginOrderIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryOrderByOriginOrderIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_ORDER_BY_ORIGIN_ORDER_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryOrderByOriginOrderId(rpcQueryOrderByOriginOrderIdReq.getCommonReq(), rpcQueryOrderByOriginOrderIdReq.getOriginOrderId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_ORDER_BY_ORIGIN_ORDER_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_ORDER_LIST_BY_ACCOUNT_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryOrderListByAccountIdReq rpcQueryOrderListByAccountIdReq = RpcQueryOrderListByAccountIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryOrderListByAccountIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_ORDER_LIST_BY_ACCOUNT_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryOrderListByAccountId(rpcQueryOrderListByAccountIdReq.getCommonReq(), rpcQueryOrderListByAccountIdReq.getAccountId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_ORDER_LIST_BY_ACCOUNT_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_ORDER_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryOrderListByUnifiedSymbolReq rpcQueryOrderListByUnifiedSymbolReq = RpcQueryOrderListByUnifiedSymbolReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryOrderListByUnifiedSymbolReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_ORDER_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryOrderListByUnifiedSymbol(rpcQueryOrderListByUnifiedSymbolReq.getCommonReq(), rpcQueryOrderListByUnifiedSymbolReq.getUnifiedSymbol());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_ORDER_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, e);
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
						rpcServerReqHandlerService.getTradeList(rpcGetTradeListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_TRADE_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
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
						rpcServerReqHandlerService.getTickList(rpcGetTickListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_TICK_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_TRADE_BY_TRADE_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryTradeByTradeIdReq rpcQueryTradeByTradeIdReq = RpcQueryTradeByTradeIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryTradeByTradeIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_TRADE_BY_TRADE_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryTradeByTradeId(rpcQueryTradeByTradeIdReq.getCommonReq(), rpcQueryTradeByTradeIdReq.getTradeId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_TRADE_BY_TRADE_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_TRADE_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryTradeListByUnifiedSymbolReq rpcQueryTradeListByUnifiedSymbolReq = RpcQueryTradeListByUnifiedSymbolReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryTradeListByUnifiedSymbolReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_TRADE_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryTradeListByUnifiedSymbol(rpcQueryTradeListByUnifiedSymbolReq.getCommonReq(), rpcQueryTradeListByUnifiedSymbolReq.getUnifiedSymbol());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_TRADE_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_TRADE_LIST_BY_ACCOUNT_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryTradeListByAccountIdReq rpcQueryTradeListByAccountIdReq = RpcQueryTradeListByAccountIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryTradeListByAccountIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_TRADE_LIST_BY_ACCOUNT_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryTradeListByAccountId(rpcQueryTradeListByAccountIdReq.getCommonReq(), rpcQueryTradeListByAccountIdReq.getAccountId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_TRADE_LIST_BY_ACCOUNT_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_TRADE_LIST_BY_ORDER_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryTradeListByOrderIdReq rpcQueryTradeListByOrderIdReq = RpcQueryTradeListByOrderIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryTradeListByOrderIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_TRADE_LIST_BY_ORDER_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryTradeListByOrderId(rpcQueryTradeListByOrderIdReq.getCommonReq(), rpcQueryTradeListByOrderIdReq.getOrderId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_TRADE_LIST_BY_ORDER_ID_REQ2", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_TRADE_LIST_BY_ORIGIN_ORDER_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryTradeListByOriginOrderIdReq rpcQueryTradeListByOriginOrderIdReq = RpcQueryTradeListByOriginOrderIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryTradeListByOriginOrderIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_TRADE_LIST_BY_ORIGIN_ORDER_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryTradeListByOriginOrderId(rpcQueryTradeListByOriginOrderIdReq.getCommonReq(), rpcQueryTradeListByOriginOrderIdReq.getOriginOrderId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_TRADE_LIST_BY_ORIGIN_ORDER_ID_REQ", sourceNodeId, e);
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
						rpcServerReqHandlerService.getPositionList(rpcGetPositionListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_POSITION_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_POSITION_BY_POSITION_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryPositionByPositionIdReq rpcQueryPositionByPositionIdReq = RpcQueryPositionByPositionIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryPositionByPositionIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_POSITION_BY_POSITION_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryPositionByPositionId(rpcQueryPositionByPositionIdReq.getCommonReq(), rpcQueryPositionByPositionIdReq.getPositionId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_POSITION_BY_POSITION_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_POSITION_LIST_BY_ACCOUNT_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryPositionListByAccountIdReq rpcQueryPositionListByAccountIdReq = RpcQueryPositionListByAccountIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryPositionListByAccountIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_POSITION_LIST_BY_ACCOUNT_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryPositionListByAccountId(rpcQueryPositionListByAccountIdReq.getCommonReq(), rpcQueryPositionListByAccountIdReq.getAccountId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_POSITION_LIST_BY_ACCOUNT_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_POSITION_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryPositionListByUnifiedSymbolReq rpcQueryPositionListByUnifiedSymbolReq = RpcQueryPositionListByUnifiedSymbolReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryPositionListByUnifiedSymbolReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_POSITION_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryPositionListByUnifiedSymbol(rpcQueryPositionListByUnifiedSymbolReq.getCommonReq(), rpcQueryPositionListByUnifiedSymbolReq.getUnifiedSymbol());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_POSITION_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, e);
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
						rpcServerReqHandlerService.getAccountList(rpcGetAccountListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_ACCOUNT_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_ACCOUNT_BY_ACCOUNT_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryAccountByAccountIdReq rpcQueryAccountByAccountIdReq = RpcQueryAccountByAccountIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryAccountByAccountIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_ACCOUNT_BY_ACCOUNT_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryAccountByAccountId(rpcQueryAccountByAccountIdReq.getCommonReq(), rpcQueryAccountByAccountIdReq.getAccountId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_ACCOUNT_BY_ACCOUNT_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_ACCOUNT_LIST_BY_ACCOUNT_CODE_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryAccountListByAccountCodeReq rpcQueryAccountListByAccountCodeReq = RpcQueryAccountListByAccountCodeReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryAccountListByAccountCodeReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_ACCOUNT_LIST_BY_ACCOUNT_CODE_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryAccountListByAccountCode(rpcQueryAccountListByAccountCodeReq.getCommonReq(), rpcQueryAccountListByAccountCodeReq.getAccountCode());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_ACCOUNT_LIST_BY_ACCOUNT_CODE_REQ", sourceNodeId, e);
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
						rpcServerReqHandlerService.getContractList(rpcGetContractListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_CONTRACT_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_CONTRACT_BY_CONTRACT_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryContractByContractIdReq rpcQueryContractByContractIdReq = RpcQueryContractByContractIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryContractByContractIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_CONTRACT_BY_CONTRACT_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryContractByContractId(rpcQueryContractByContractIdReq.getCommonReq(), rpcQueryContractByContractIdReq.getContractId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_CONTRACT_BY_CONTRACT_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_CONTRACT_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryContractListByUnifiedSymbolReq rpcQueryContractListByUnifiedSymbolReq = RpcQueryContractListByUnifiedSymbolReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryContractListByUnifiedSymbolReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_CONTRACT_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryContractListByUnifiedSymbol(rpcQueryContractListByUnifiedSymbolReq.getCommonReq(), rpcQueryContractListByUnifiedSymbolReq.getUnifiedSymbol());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_CONTRACT_LIST_BY_UNIFIED_SYMBOL_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_CONTRACT_LIST_BY_GATEWAY_ID_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcQueryContractListByGatewayIdReq rpcQueryContractListByGatewayIdReq = RpcQueryContractListByGatewayIdReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryContractListByGatewayIdReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_CONTRACT_LIST_BY_GATEWAY_ID_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryContractListByGatewayId(rpcQueryContractListByGatewayIdReq.getCommonReq(), rpcQueryContractListByGatewayIdReq.getGatewayId());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_CONTRACT_LIST_BY_GATEWAY_ID_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcSyncSlaveNodeRuntimeDataReq rpcSyncSlaveNodeRuntimeDataReq = RpcSyncSlaveNodeRuntimeDataReq.parseFrom(contentByteString);
						checkCommonReq(rpcSyncSlaveNodeRuntimeDataReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SYNC_SLAVE_NODE_RUNTIME_DATA_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.syncSlaveNodeRuntimeData(rpcSyncSlaveNodeRuntimeDataReq.getCommonReq(), rpcSyncSlaveNodeRuntimeDataReq.getGatewayList());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SYNC_SLAVE_NODE_RUNTIME_DATA_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		case RpcId.GET_MIX_CONTRACT_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcGetMixContractListReq rpcGetMixContractListReq = RpcGetMixContractListReq.parseFrom(contentByteString);
						checkCommonReq(rpcGetMixContractListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_MIX_CONTRACT_LIST_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.getMixContractList(rpcGetMixContractListReq.getCommonReq());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_MIX_CONTRACT_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_DB_BAR_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcQueryDBBarListReq rpcQueryDBBarListReq = RpcQueryDBBarListReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryDBBarListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_DB_BAR_LIST_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryDBBarList(rpcQueryDBBarListReq.getCommonReq(), rpcQueryDBBarListReq.getStartTimestamp(), rpcQueryDBBarListReq.getEndTimestamp(),
								rpcQueryDBBarListReq.getUnifiedSymbol(), rpcQueryDBBarListReq.getBarCycle(), rpcQueryDBBarListReq.getMarketDataDBType());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_DB_BAR_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_DB_TICK_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcQueryDBTickListReq rpcQueryDBTickListReq = RpcQueryDBTickListReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryDBTickListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_DB_TICK_LIST_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryDBTickList(rpcQueryDBTickListReq.getCommonReq(), rpcQueryDBTickListReq.getStartTimestamp(), rpcQueryDBTickListReq.getEndTimestamp(),
								rpcQueryDBTickListReq.getUnifiedSymbol(), rpcQueryDBTickListReq.getMarketDataDBType());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_DB_TICK_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_VOLUME_BAR_LIST_REQ_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcQueryVolumeBarListReq rpcQueryVolumeBarListReq = RpcQueryVolumeBarListReq.parseFrom(contentByteString);
						checkCommonReq(rpcQueryVolumeBarListReq.getCommonReq(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_VOLUME_BAR_LIST_REQ", sourceNodeId, reqId);
						rpcServerReqHandlerService.queryVolumeBarList(rpcQueryVolumeBarListReq.getCommonReq(), rpcQueryVolumeBarListReq.getStartTimestamp(), rpcQueryVolumeBarListReq.getEndTimestamp(),
								rpcQueryVolumeBarListReq.getUnifiedSymbol(), rpcQueryVolumeBarListReq.getVolume());
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_VOLUME_BAR_LIST_REQ", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		// ------------------------------------------------------------------------------------------------------------

		case RpcId.SUBSCRIBE_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcSubscribeRsp rpcSubscribeRsp = RpcSubscribeRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcSubscribeRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SUBSCRIBE_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onSubscribeRsp(rpcSubscribeRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SUBSCRIBE_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		case RpcId.UNSUBSCRIBE_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcUnsubscribeRsp rpcUnsubscribeRsp = RpcUnsubscribeRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcUnsubscribeRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:UNSUBSCRIBE_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onUnsubscribeRsp(rpcUnsubscribeRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:UNSUBSCRIBE_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		case RpcId.SUBMIT_ORDER_RSP_VALUE: {
			importantExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcSubmitOrderRsp rpcSubmitOrderRsp = RpcSubmitOrderRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcSubmitOrderRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SUBMIT_ORDER_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onSubmitOrderRsp(rpcSubmitOrderRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SUBMIT_ORDER_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		case RpcId.CANCEL_ORDER_RSP_VALUE: {
			importantExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcCancelOrderRsp rpcCancelOrderRsp = RpcCancelOrderRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcCancelOrderRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:CANCEL_ORDER_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onCancelOrderRsp(rpcCancelOrderRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:CANCEL_ORDER_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		case RpcId.SEARCH_CONTRACT_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcSearchContractRsp rpcSearchContractRsp = RpcSearchContractRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcSearchContractRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SEARCH_CONTRACT_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onSearchContractRsp(rpcSearchContractRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SEARCH_CONTRACT_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		case RpcId.GET_CONTRACT_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetContractListRsp rpcGetContractListRsp = RpcGetContractListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetContractListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_CONTRACT_LIST_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onGetContractListRsp(rpcGetContractListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_CONTRACT_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_TICK_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetTickListRsp rpcGetTickListRsp = RpcGetTickListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetTickListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_TICK_LIST_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onGetTickListRsp(rpcGetTickListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_TICK_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_POSITION_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetPositionListRsp rpcGetPositionListRsp = RpcGetPositionListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetPositionListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_POSITION_LIST_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onGetPositionListRsp(rpcGetPositionListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_POSITION_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_ACCOUNT_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetAccountListRsp rpcGetAccountListRsp = RpcGetAccountListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetAccountListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_ACCOUNT_LIST_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onGetAccountListRsp(rpcGetAccountListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_ACCOUNT_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_TRADE_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetTradeListRsp rpcGetTradeListRsp = RpcGetTradeListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetTradeListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_TRADE_LIST_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onGetTradeListRsp(rpcGetTradeListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_TRADE_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_ORDER_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetOrderListRsp rpcGetOrderListRsp = RpcGetOrderListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetOrderListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_ORDER_LIST_RSP", sourceNodeId, reqId);
						rpcServerRspHandlerService.onGetOrderListRsp(rpcGetOrderListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_ORDER_LIST_RSP", sourceNodeId, e);
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
						rpcServerRspHandlerService.onExceptionRsp(rpcExceptionRsp);

					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:EXCEPTION_RSP", e);
					}
				}
			});
			break;
		}

		// ------------------------------------------------------------------------------------------------------------
		case RpcId.ORDER_RTN_VALUE: {
			tradeRtnQueueSingleExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcOrderRtn rpcOrderRtn = RpcOrderRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:ORDER_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onOrderRtn(rpcOrderRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:ORDER_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.TRADE_RTN_VALUE: {
			tradeRtnQueueSingleExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcTradeRtn rpcTradeRtn = RpcTradeRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:TRADE_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onTradeRtn(rpcTradeRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:TRADE_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.CONTRACT_RTN_VALUE: {
			unimportantSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {

					try {
						RpcContractRtn rpcContractRtn = RpcContractRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:CONTRACT_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onContractRtn(rpcContractRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:CONTRACT_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.POSITION_RTN_VALUE: {
			tradeRtnQueueSingleExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcPositionRtn rpcPositionRtn = RpcPositionRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:POSITION_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onPositionRtn(rpcPositionRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:POSITION_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.ACCOUNT_RTN_VALUE: {
			tradeRtnQueueSingleExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcAccountRtn rpcAccountRtn = RpcAccountRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:ACCOUNT_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onAccountRtn(rpcAccountRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:ACCOUNT_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.NOTICE_RTN_VALUE: {
			tradeRtnQueueSingleExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcNoticeRtn rpcNoticeRtn = RpcNoticeRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:NOTICE_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onNoticeRtn(rpcNoticeRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:NOTICE_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.TICK_RTN_VALUE: {
			marketRtnQueueSingleExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcTickRtn rpcTickRtn = RpcTickRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:TICK_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onTickRtn(rpcTickRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:TICK_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.ORDER_LIST_RTN_VALUE: {
			unimportantSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcOrderListRtn rpcOrderListRtn = RpcOrderListRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:ORDER_LIST_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onOrderListRtn(rpcOrderListRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:ORDER_LIST_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.TRADE_LIST_RTN_VALUE: {
			unimportantSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcTradeListRtn rpcTradeListRtn = RpcTradeListRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:TRADE_LIST_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onTradeListRtn(rpcTradeListRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:TRADE_LIST_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.CONTRACT_LIST_RTN_VALUE: {
			unimportantSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcContractListRtn rpcContractListRtn = RpcContractListRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:CONTRACT_LIST_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onContractListRtn(rpcContractListRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:CONTRACT_LIST_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.POSITION_LIST_RTN_VALUE: {
			unimportantSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcPositionListRtn rpcPositionListRtn = RpcPositionListRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:POSITION_LIST_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onPositionListRtn(rpcPositionListRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:POSITION_LIST_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.ACCOUNT_LIST_RTN_VALUE: {
			unimportantSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcAccountListRtn rpcAccountListRtn = RpcAccountListRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:ACCOUNT_LIST_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onAccountListRtn(rpcAccountListRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:ACCOUNT_LIST_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.TICK_LIST_RTN_VALUE: {
			unimportantSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcTickListRtn rpcTickListRtn = RpcTickListRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:TICK_LIST_RTN", sourceNodeId, reqId);
						rpcServerRtnHandlerService.onTickListRtn(rpcTickListRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:TICK_LIST_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		default:
			logger.error("处理RPC错误,来源节点ID:{},RPC ID:{},请求ID:{},不支持此功能", sourceNodeId, rpcId, reqId);
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

		if (commonReq.getTargetNodeId() != 0) {
			throw new IllegalArgumentException("目标节点ID不为0(Master)!");
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
				.setSourceNodeId(0) //
				.setTargetNodeId(targetNodeId) //
				.setTimestamp(System.currentTimeMillis()) //
				.setContentBytes(content);

		if (!webSocketServerHandler.sendDataByNodeId(targetNodeId, depBuilder.build().toByteArray())) {
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
				.setSourceNodeId(0) //
				.setTargetNodeId(targetNodeId) //
				.setTimestamp(System.currentTimeMillis()) //
				.setContentBytes(contentByteString);

		if (!webSocketServerHandler.sendDataByNodeId(targetNodeId, depBuilder.build().toByteArray())) {
			logger.error("发送RPC错误,目标节点ID:{},请求ID:{},RPC:{}", targetNodeId, rpcId.getValueDescriptor().getName(), rpcId);
			return false;
		}
		return true;
	}
}
