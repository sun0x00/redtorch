package xyz.redtorch.node.master.rpc.service.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.service.RpcRspHandlerService;
import xyz.redtorch.common.util.rpc.RpcUtils;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketProcessService;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketReqHandlerService;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketRtnHandlerService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;
import xyz.redtorch.pb.CoreRpc.*;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RpcServerOverWebSocketProcessServiceImpl implements RpcServerOverWebSocketProcessService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcServerOverWebSocketProcessServiceImpl.class);
    private final ExecutorService importantExecutorService = Executors.newCachedThreadPool();
    private final ExecutorService unimportantSingleThreadExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService tradeRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService marketRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();

    @Autowired
    private WebSocketServerHandler webSocketServerHandler;
    @Autowired
    private RpcServerOverWebSocketReqHandlerService rpcServerOverWebSocketReqHandlerService;
    @Autowired
    private RpcServerOverWebSocketRtnHandlerService rpcServerOverWebSocketRtnHandlerService;
    @Autowired
    private RpcRspHandlerService rpcRspHandlerService;

    @Value("${rt.master.rpcProcessNormalThreadsNum}")
    private Integer rpcProcessNormalThreadsNum;
    private ExecutorService normalExecutorService; //

    @Override
    public void afterPropertiesSet() throws Exception {
        normalExecutorService = Executors.newFixedThreadPool(rpcProcessNormalThreadsNum);
    }

    @Override
    public void processData(String sessionId, byte[] data) {
        if (data == null) {
            logger.error("处理DEP错误,接收到空数据,会话ID:{}", sessionId);
            return;
        }
        DataExchangeProtocol dep;
        try {
            dep = DataExchangeProtocol.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            logger.error("处理DEP错误,PB解析数据异常,会话ID:{}", sessionId, e);
            logger.error("处理DEP错误,PB解析数据异常,会话ID:{},原始数据HEX:{}", sessionId, Hex.encodeHexString(data));
            return;
        }

        DataExchangeProtocol.ContentType contentType = dep.getContentType();
        int rpcId = dep.getRpcId();
        long timestamp = dep.getTimestamp();

        if (logger.isDebugEnabled()) {
            String contentTypeValueName = contentType.getValueDescriptor().getName();
            logger.debug("处理DEP记录,会话ID:{},内容类型:{},RPC ID:{},时间戳:{}", sessionId, contentTypeValueName, rpcId, timestamp);
        }

        ByteString contentByteString = RpcUtils.processByteString(contentType, dep.getContentBytes(), rpcId, timestamp);

        if (contentByteString == null) {
            return;
        }

        doRpc(sessionId, rpcId, contentByteString, timestamp);

    }

    private void doRpc(String sessionId, int rpcId, ByteString contentByteString, long timestamp) {

        switch (rpcId) {
            case RpcId.UNKNOWN_RPC_ID_VALUE: {
                logger.error("处理RPC,会话ID:{},RPC ID:UNKNOWN_RPC_ID_VALUE,时间戳:{}", sessionId, timestamp);
                break;
            }
            case RpcId.SUBSCRIBE_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSubscribeReq rpcSubscribeReq = RpcSubscribeReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcSubscribeReq.getCommonReq());
                        transactionId = rpcSubscribeReq.getCommonReq().getTransactionId();
                        rpcServerOverWebSocketReqHandlerService.subscribe(sessionId, rpcSubscribeReq.getCommonReq(), rpcSubscribeReq.getContract());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:SUBSCRIBE_REQ", sessionId, transactionId, e);
                        sendExceptionRsp(sessionId, rpcId, transactionId, timestamp, e.getMessage());
                    }
                });

                break;
            }
            case RpcId.UNSUBSCRIBE_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcUnsubscribeReq rpcUnsubscribeReq = RpcUnsubscribeReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcUnsubscribeReq.getCommonReq());
                        transactionId = rpcUnsubscribeReq.getCommonReq().getTransactionId();
                        rpcServerOverWebSocketReqHandlerService.unsubscribe(sessionId, rpcUnsubscribeReq.getCommonReq(), rpcUnsubscribeReq.getContract());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:UNSUBSCRIBE_REQ", sessionId, transactionId, e);
                        sendExceptionRsp(sessionId, rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.SUBMIT_ORDER_REQ_VALUE: {
                importantExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSubmitOrderReq rpcSubmitOrderReq = RpcSubmitOrderReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcSubmitOrderReq.getCommonReq());
                        transactionId = rpcSubmitOrderReq.getCommonReq().getTransactionId();
                        rpcServerOverWebSocketReqHandlerService.submitOrder(sessionId, rpcSubmitOrderReq.getCommonReq(), rpcSubmitOrderReq.getSubmitOrderReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:SUBMIT_ORDER_REQ", sessionId, transactionId, e);
                        sendExceptionRsp(sessionId, rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.CANCEL_ORDER_REQ_VALUE: {
                importantExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcCancelOrderReq rpcCancelOrderReq = RpcCancelOrderReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcCancelOrderReq.getCommonReq());
                        transactionId = rpcCancelOrderReq.getCommonReq().getTransactionId();
                        rpcServerOverWebSocketReqHandlerService.cancelOrder(sessionId, rpcCancelOrderReq.getCommonReq(), rpcCancelOrderReq.getCancelOrderReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:CANCEL_ORDER_REQ", sessionId, transactionId, e);
                        sendExceptionRsp(sessionId, rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.SEARCH_CONTRACT_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSearchContractReq rpcSearchContractReq = RpcSearchContractReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcSearchContractReq.getCommonReq());
                        transactionId = rpcSearchContractReq.getCommonReq().getTransactionId();
                        rpcServerOverWebSocketReqHandlerService.searchContract(sessionId, rpcSearchContractReq.getCommonReq(), rpcSearchContractReq.getContract());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:SEARCH_CONTRACT_REQ", sessionId, transactionId, e);
                        sendExceptionRsp(sessionId, rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            // ------------------------------------------------------------------------------------------------------------

            case RpcId.SUBSCRIBE_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSubscribeRsp rpcSubscribeRsp = RpcSubscribeRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcSubscribeRsp.getCommonRsp());
                        transactionId = rpcSubscribeRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcSubscribeRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:SUBSCRIBE_RSP", sessionId, transactionId, e);
                    }
                });
                break;
            }

            case RpcId.UNSUBSCRIBE_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcUnsubscribeRsp rpcUnsubscribeRsp = RpcUnsubscribeRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcUnsubscribeRsp.getCommonRsp());
                        transactionId = rpcUnsubscribeRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcUnsubscribeRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:UNSUBSCRIBE_RSP", sessionId, transactionId, e);
                    }
                });
                break;
            }

            case RpcId.SUBMIT_ORDER_RSP_VALUE: {
                importantExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSubmitOrderRsp rpcSubmitOrderRsp = RpcSubmitOrderRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcSubmitOrderRsp.getCommonRsp());
                        transactionId = rpcSubmitOrderRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcSubmitOrderRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:SUBMIT_ORDER_RSP", sessionId, transactionId, e);
                    }
                });
                break;
            }

            case RpcId.CANCEL_ORDER_RSP_VALUE: {
                importantExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcCancelOrderRsp rpcCancelOrderRsp = RpcCancelOrderRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcCancelOrderRsp.getCommonRsp());
                        transactionId = rpcCancelOrderRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcCancelOrderRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:CANCEL_ORDER_RSP", sessionId, transactionId, e);
                    }
                });
                break;
            }

            case RpcId.SEARCH_CONTRACT_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSearchContractRsp rpcSearchContractRsp = RpcSearchContractRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcSearchContractRsp.getCommonRsp());
                        transactionId = rpcSearchContractRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcSearchContractRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:SEARCH_CONTRACT_RSP", sessionId, transactionId, e);
                    }
                });
                break;
            }

            case RpcId.EXCEPTION_RSP_VALUE: {
                importantExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcExceptionRsp rpcExceptionRsp = RpcExceptionRsp.parseFrom(contentByteString);
                        transactionId = rpcExceptionRsp.getOriginalTransactionId();
                        logger.info("处理RPC记录,会话ID:{},业务ID:{},RPC:EXCEPTION_RSP", sessionId, transactionId);
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcExceptionRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:EXCEPTION_RSP", sessionId, transactionId, e);
                    }
                });
                break;
            }

            // ------------------------------------------------------------------------------------------------------------
            case RpcId.ORDER_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcOrderRtn rpcOrderRtn = RpcOrderRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onOrderRtn(rpcOrderRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:ORDER_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.TRADE_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcTradeRtn rpcTradeRtn = RpcTradeRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onTradeRtn(rpcTradeRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:TRADE_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.CONTRACT_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcContractRtn rpcContractRtn = RpcContractRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onContractRtn(rpcContractRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:CONTRACT_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.POSITION_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcPositionRtn rpcPositionRtn = RpcPositionRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onPositionRtn(rpcPositionRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:POSITION_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.ACCOUNT_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcAccountRtn rpcAccountRtn = RpcAccountRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onAccountRtn(rpcAccountRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:ACCOUNT_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.NOTICE_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcNoticeRtn rpcNoticeRtn = RpcNoticeRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onNoticeRtn(rpcNoticeRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:NOTICE_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.TICK_RTN_VALUE: {
                marketRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcTickRtn rpcTickRtn = RpcTickRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onTickRtn(rpcTickRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:TICK_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.ORDER_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcOrderListRtn rpcOrderListRtn = RpcOrderListRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onOrderListRtn(rpcOrderListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:ORDER_LIST_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.TRADE_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcTradeListRtn rpcTradeListRtn = RpcTradeListRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onTradeListRtn(rpcTradeListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:TRADE_LIST_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.CONTRACT_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcContractListRtn rpcContractListRtn = RpcContractListRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onContractListRtn(rpcContractListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:CONTRACT_LIST_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.POSITION_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcPositionListRtn rpcPositionListRtn = RpcPositionListRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onPositionListRtn(rpcPositionListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:POSITION_LIST_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.ACCOUNT_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcAccountListRtn rpcAccountListRtn = RpcAccountListRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onAccountListRtn(rpcAccountListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:ACCOUNT_LIST_RTN", sessionId, e);
                    }
                });
                break;
            }
            case RpcId.TICK_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcTickListRtn rpcTickListRtn = RpcTickListRtn.parseFrom(contentByteString);
                        rpcServerOverWebSocketRtnHandlerService.onTickListRtn(rpcTickListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},RPC:TICK_LIST_RTN", sessionId, e);
                    }
                });
                break;
            }

            default:
                logger.error("处理RPC错误,会话ID:{},RPC ID:{},不支持此功能", sessionId, rpcId);
                sendExceptionRsp(sessionId, rpcId, "", timestamp, "不支持此功能");
                break;
        }
    }

    public void sendExceptionRsp(String sessionId, int originalRpcId, String originalTransactionId, long originalTimestamp, String info) {
        if (info == null) {
            info = "";
        }
        ByteString content = RpcExceptionRsp.newBuilder() //
                .setOriginalRpcId(originalRpcId) //
                .setOriginalTransactionId(originalTransactionId) //
                .setOriginalTimestamp(originalTimestamp) //
                .setInfo(info) //
                .build().toByteString();
        sendCoreRpc(sessionId, RpcId.EXCEPTION_RSP, originalTransactionId, content);
    }

    public boolean sendCoreRpc(String sessionId, RpcId rpcId, String transactionId, ByteString content) {
        byte[] data = RpcUtils.generateRpcDep(rpcId, transactionId, content);

        if (!webSocketServerHandler.sendDataBySessionId(sessionId, data)) {
            logger.error("发送RPC错误,会话ID:{},RPC:{},业务ID:{}", sessionId, rpcId.getValueDescriptor().getName(), transactionId);
            return false;
        }
        return true;
    }
}
