package xyz.redtorch.desktop.rpc.service.impl;

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
import xyz.redtorch.desktop.layout.base.MainLayout;
import xyz.redtorch.desktop.rpc.service.RpcClientRspHandlerService;
import xyz.redtorch.desktop.rpc.service.RpcClientRtnHandlerService;
import xyz.redtorch.desktop.service.AuthService;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.web.socket.WebSocketClientHandler;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreRpc.RpcAccountListRtn;
import xyz.redtorch.pb.CoreRpc.RpcAccountRtn;
import xyz.redtorch.pb.CoreRpc.RpcCancelOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcContractListRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractRtn;
import xyz.redtorch.pb.CoreRpc.RpcExceptionRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetAccountListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetMixContractListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetOrderListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetPositionListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcGetTradeListRsp;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.CoreRpc.RpcNoticeRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderListRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionRtn;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryVolumeBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcSearchContractRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubmitOrderRsp;
import xyz.redtorch.pb.CoreRpc.RpcSubscribeRsp;
import xyz.redtorch.pb.CoreRpc.RpcTickListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeRtn;
import xyz.redtorch.pb.CoreRpc.RpcUnsubscribeRsp;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.ContentType;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.RpcType;

@Service
public class RpcClientProcessServiceImpl implements RpcClientProcessService, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientProcessServiceImpl.class);

	@Autowired
	private AuthService authService;
	@Autowired
	private MainLayout mainLayout;
	@Autowired
	private RpcClientRspHandlerService rpcClientRspHandlerService;
	@Autowired
	private RpcClientRtnHandlerService rpcClientRtnHandlerService;
	@Autowired
	private WebSocketClientHandler webSocketClientHandler;
	@Autowired
	private DesktopTradeCachesService desktopTradeCachesService;

	@Value("${rt.rpc.client.threads-normal}")
	private Integer threadsNormal;

	private ExecutorService normalExecutorService;
	private ExecutorService importantExecutorService = Executors.newCachedThreadPool();
	private ExecutorService unimportantSingleThreadExecutorService = Executors.newSingleThreadExecutor();
	private ExecutorService tradeRtnQueueSingleThreadExecutorService = Executors.newSingleThreadExecutor();
	private ExecutorService marketRtnQueueSingleThreadExecutorService = Executors.newSingleThreadExecutor();

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

		if (targetNodeId != authService.getNodeId()) {
			logger.error("处理DEP错误,目标节点ID不匹配当前节点ID:{},目标节点ID:{}", authService.getNodeId(), targetNodeId);
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
		// ------------------------------------------------------------------------------------------------------------
		case RpcId.SUBSCRIBE_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcSubscribeRsp rpcSubscribeRsp = RpcSubscribeRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcSubscribeRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:SUBSCRIBE_RSP", sourceNodeId, reqId);
						rpcClientRspHandlerService.onSubscribeRsp(rpcSubscribeRsp);
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
						rpcClientRspHandlerService.onUnsubscribeRsp(rpcUnsubscribeRsp);
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
						rpcClientRspHandlerService.onSubmitOrderRsp(rpcSubmitOrderRsp);
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
						rpcClientRspHandlerService.onCancelOrderRsp(rpcCancelOrderRsp);
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
						rpcClientRspHandlerService.onSearchContractRsp(rpcSearchContractRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:SEARCH_CONTRACT_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		// ------------------------------------------------------------------------------------------------------------
		case RpcId.GET_CONTRACT_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetContractListRsp rpcGetContractListRsp = RpcGetContractListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetContractListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_CONTRACT_LIST_RSP", sourceNodeId, reqId);
						rpcClientRspHandlerService.onGetContractListRsp(rpcGetContractListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_CONTRACT_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.GET_MIX_CONTRACT_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcGetMixContractListRsp rpcGetMixContractListRsp = RpcGetMixContractListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcGetMixContractListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:GET_MIX_CONTRACT_LIST_RSP", sourceNodeId, reqId);
						rpcClientRspHandlerService.onGetMixContractListRsp(rpcGetMixContractListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:GET_MIX_CONTRACT_LIST_RSP", sourceNodeId, e);
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
						rpcClientRspHandlerService.onGetTickListRsp(rpcGetTickListRsp);
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
						rpcClientRspHandlerService.onGetPositionListRsp(rpcGetPositionListRsp);
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
						rpcClientRspHandlerService.onGetAccountListRsp(rpcGetAccountListRsp);
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
						rpcClientRspHandlerService.onGetTradeListRsp(rpcGetTradeListRsp);
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
						rpcClientRspHandlerService.onGetOrderListRsp(rpcGetOrderListRsp);
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
						rpcClientRspHandlerService.onExceptionRsp(rpcExceptionRsp);

					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:EXCEPTION_RSP", e);
					}
				}
			});
			break;
		}
		case RpcId.QUERY_DB_BAR_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcQueryDBBarListRsp rpcQueryDBBarListRsp = RpcQueryDBBarListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcQueryDBBarListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_DB_BAR_LIST_RSP", sourceNodeId, reqId);
						rpcClientRspHandlerService.onQueryDBBarListRsp(rpcQueryDBBarListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_DB_BAR_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_DB_TICK_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcQueryDBTickListRsp rpcQueryDBTickListRsp = RpcQueryDBTickListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcQueryDBTickListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_DB_TICK_LIST_RSP", sourceNodeId, reqId);
						rpcClientRspHandlerService.onQueryDBTickListRsp(rpcQueryDBTickListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_DB_TICK_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.QUERY_VOLUME_BAR_LIST_RSP_VALUE: {
			normalExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcQueryVolumeBarListRsp rpcQueryVolumeBarListRsp = RpcQueryVolumeBarListRsp.parseFrom(contentByteString);
						checkCommonRsp(rpcQueryVolumeBarListRsp.getCommonRsp(), sourceNodeId, reqId);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:QUERY_VOLUME_BAR_LIST_RSP", sourceNodeId, reqId);
						rpcClientRspHandlerService.onQueryVolumeBarListRsp(rpcQueryVolumeBarListRsp);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:QUERY_VOLUME_BAR_LIST_RSP", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}

		// ------------------------------------------------------------------------------------------------------------
		case RpcId.ORDER_RTN_VALUE: {
			tradeRtnQueueSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcOrderRtn rpcOrderRtn = RpcOrderRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:ORDER_RTN", sourceNodeId, reqId);
						rpcClientRtnHandlerService.onOrderRtn(rpcOrderRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:ORDER_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.TRADE_RTN_VALUE: {
			tradeRtnQueueSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcTradeRtn rpcTradeRtn = RpcTradeRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:TRADE_RTN", sourceNodeId, reqId);
						rpcClientRtnHandlerService.onTradeRtn(rpcTradeRtn);
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
						rpcClientRtnHandlerService.onContractRtn(rpcContractRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:CONTRACT_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.POSITION_RTN_VALUE: {
			tradeRtnQueueSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcPositionRtn rpcPositionRtn = RpcPositionRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:POSITION_RTN", sourceNodeId, reqId);
						rpcClientRtnHandlerService.onPositionRtn(rpcPositionRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:POSITION_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.ACCOUNT_RTN_VALUE: {
			tradeRtnQueueSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcAccountRtn rpcAccountRtn = RpcAccountRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:ACCOUNT_RTN", sourceNodeId, reqId);
						rpcClientRtnHandlerService.onAccountRtn(rpcAccountRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:ACCOUNT_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.NOTICE_RTN_VALUE: {
			tradeRtnQueueSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcNoticeRtn rpcNoticeRtn = RpcNoticeRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:NOTICE_RTN", sourceNodeId, reqId);
						rpcClientRtnHandlerService.onNoticeRtn(rpcNoticeRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:NOTICE_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		case RpcId.TICK_RTN_VALUE: {
			marketRtnQueueSingleThreadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RpcTickRtn rpcTickRtn = RpcTickRtn.parseFrom(contentByteString);
						logger.info("处理RPC记录,来源节点ID:{},请求ID:{},RPC:TICK_RTN", sourceNodeId, reqId);
						rpcClientRtnHandlerService.onTickRtn(rpcTickRtn);
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
						rpcClientRtnHandlerService.onOrderListRtn(rpcOrderListRtn);
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
						rpcClientRtnHandlerService.onTradeListRtn(rpcTradeListRtn);
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
						rpcClientRtnHandlerService.onContractListRtn(rpcContractListRtn);
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
						rpcClientRtnHandlerService.onPositionListRtn(rpcPositionListRtn);
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
						rpcClientRtnHandlerService.onAccountListRtn(rpcAccountListRtn);
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
						rpcClientRtnHandlerService.onTickListRtn(rpcTickListRtn);
					} catch (Exception e) {
						logger.error("处理RPC异常,来源节点ID:{},RPC:TICK_LIST_RTN", sourceNodeId, e);
						sendExceptionRsp(sourceNodeId, rpcId, reqId, timestamp, e.getMessage());
					}
				}
			});
			break;
		}
		// ------------------------------------------------------------------------------------------------------------
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
				.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
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
				.setSourceNodeId(authService.getNodeId() == null ? 0 : authService.getNodeId()) //
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
		mainLayout.onDisconnected();
	}

	@Override
	public void onWsError() {
		// NOP
	}

	@Override
	public void onWsConnected() {
		mainLayout.onConnected();
		desktopTradeCachesService.reloadData();
	}

	@Override
	public void onHeartbeat(String result) {
		mainLayout.onHeartbeat(result);
	}
}
