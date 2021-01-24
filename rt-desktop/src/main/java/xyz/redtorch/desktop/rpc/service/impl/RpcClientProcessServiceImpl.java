package xyz.redtorch.desktop.rpc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.service.RpcRspHandlerService;
import xyz.redtorch.common.util.rpc.RpcUtils;
import xyz.redtorch.common.web.vo.ResponseVo;
import xyz.redtorch.desktop.rpc.service.RpcClientRtnHandlerService;
import xyz.redtorch.desktop.service.ConfigService;
import xyz.redtorch.desktop.web.socket.WebSocketClientHandler;
import xyz.redtorch.pb.CoreRpc.*;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;

import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RpcClientProcessServiceImpl implements RpcClientProcessService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProcessServiceImpl.class);
    private final ExecutorService importantExecutorService = Executors.newCachedThreadPool();
    private final ExecutorService unimportantSingleThreadExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService tradeRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService marketRtnQueueSingleExecutorService = Executors.newSingleThreadExecutor();

    @Autowired
    private RpcRspHandlerService rpcRspHandlerService;
    @Autowired
    private RpcClientRtnHandlerService rpcClientRtnHandlerService;
    @Autowired
    private WebSocketClientHandler webSocketClientHandler;
    @Autowired
    private ConfigService configService;

    private ExecutorService normalExecutorService;

    @Autowired
    private RestTemplate restTemplate;

    private final Executor asyncHttpRpcExecutor = Executors.newFixedThreadPool(5);

    @Override
    public void afterPropertiesSet() throws Exception {
        normalExecutorService = Executors.newFixedThreadPool(configService.getRpcProcessNormalThreadsNum());
    }

    @Override
    public void processData(byte[] data) {
        if (data == null) {
            logger.error("处理DEP错误,接收到空数据");
            return;
        }
        DataExchangeProtocol dep;
        try {
            dep = DataExchangeProtocol.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            logger.error("处理DEP错误,PB解析数据异常", e);
            logger.error("处理DEP错误,PB解析数据异常,原始数据HEX:{}", Hex.encodeHexString(data));
            return;
        }

        DataExchangeProtocol.ContentType contentType = dep.getContentType();
        int rpcId = dep.getRpcId();
        long timestamp = dep.getTimestamp();

        if (logger.isDebugEnabled()) {
            String contentTypeValueName = contentType.getValueDescriptor().getName();
            logger.debug("处理DEP记录,内容类型:{},RPC ID:{},时间戳:{}", contentTypeValueName, rpcId, timestamp);
        }

        ByteString contentByteString = RpcUtils.processByteString(contentType, dep.getContentBytes(), rpcId, timestamp);

        if (contentByteString == null) {
            return;
        }

        doRpc(rpcId, contentByteString, timestamp);
    }

    private void doRpc(int rpcId, ByteString contentByteString, long timestamp) {

        switch (rpcId) {
            case RpcId.UNKNOWN_RPC_ID_VALUE: {
                logger.error("处理RPC,RPC ID:UNKNOWN_RPC_ID_VALUE,时间戳:{}", timestamp);
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
                        logger.error("处理RPC异常,来源节点ID:{},RPC:SUBSCRIBE_RSP", transactionId, e);
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
                        logger.error("处理RPC异常,业务ID:{},RPC:UNSUBSCRIBE_RSP", transactionId, e);
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
                        logger.error("处理RPC异常,业务ID:{},RPC:SUBMIT_ORDER_RSP", transactionId, e);
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
                        logger.error("处理RPC异常,业务ID:{},RPC:CANCEL_ORDER_RSP", transactionId, e);
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
                        logger.error("处理RPC异常,业务ID:{},RPC:SEARCH_CONTRACT_RSP", transactionId, e);
                    }
                });
                break;
            }
            // ------------------------------------------------------------------------------------------------------------
            case RpcId.GET_CONTRACT_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetContractListRsp rpcGetContractListRsp = RpcGetContractListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcGetContractListRsp.getCommonRsp());
                        transactionId = rpcGetContractListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcGetContractListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_CONTRACT_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.GET_MIX_CONTRACT_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetMixContractListRsp rpcGetMixContractListRsp = RpcGetMixContractListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcGetMixContractListRsp.getCommonRsp());
                        transactionId = rpcGetMixContractListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcGetMixContractListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_MIX_CONTRACT_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.GET_TICK_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetTickListRsp rpcGetTickListRsp = RpcGetTickListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcGetTickListRsp.getCommonRsp());
                        transactionId = rpcGetTickListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcGetTickListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_TICK_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.GET_POSITION_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetPositionListRsp rpcGetPositionListRsp = RpcGetPositionListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcGetPositionListRsp.getCommonRsp());
                        transactionId = rpcGetPositionListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcGetPositionListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_POSITION_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.GET_ACCOUNT_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetAccountListRsp rpcGetAccountListRsp = RpcGetAccountListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcGetAccountListRsp.getCommonRsp());
                        transactionId = rpcGetAccountListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcGetAccountListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_ACCOUNT_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.GET_TRADE_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetTradeListRsp rpcGetTradeListRsp = RpcGetTradeListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcGetTradeListRsp.getCommonRsp());
                        transactionId = rpcGetTradeListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcGetTradeListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_TRADE_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.GET_ORDER_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetOrderListRsp rpcGetOrderListRsp = RpcGetOrderListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcGetOrderListRsp.getCommonRsp());
                        transactionId = rpcGetOrderListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcGetOrderListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_ORDER_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.QUERY_DB_BAR_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcQueryDBBarListRsp rpcQueryDBBarListRsp = RpcQueryDBBarListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcQueryDBBarListRsp.getCommonRsp());
                        transactionId = rpcQueryDBBarListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcQueryDBBarListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:QUERY_DB_BAR_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.QUERY_DB_TICK_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcQueryDBTickListRsp rpcQueryDBTickListRsp = RpcQueryDBTickListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcQueryDBTickListRsp.getCommonRsp());
                        transactionId = rpcQueryDBTickListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcQueryDBTickListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:QUERY_DB_TICK_LIST_RSP", transactionId, e);
                    }
                });
                break;
            }
            case RpcId.QUERY_VOLUME_BAR_LIST_RSP_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcQueryVolumeBarListRsp rpcQueryVolumeBarListRsp = RpcQueryVolumeBarListRsp.parseFrom(contentByteString);
                        RpcUtils.checkCommonRsp(rpcQueryVolumeBarListRsp.getCommonRsp());
                        transactionId = rpcQueryVolumeBarListRsp.getCommonRsp().getTransactionId();
                        rpcRspHandlerService.onRpcRsp(transactionId, rpcQueryVolumeBarListRsp);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:QUERY_VOLUME_BAR_LIST_RSP", transactionId, e);
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
                        logger.info("处理RPC记录,业务ID:{},RPC:EXCEPTION_RSP", transactionId);
                        rpcRspHandlerService.onRpcRsp(rpcExceptionRsp.getOriginalTransactionId(), rpcExceptionRsp);

                    } catch (Exception e) {
                        logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:EXCEPTION_RSP", transactionId, e);
                    }
                });
                break;
            }

            // ------------------------------------------------------------------------------------------------------------
            case RpcId.ORDER_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcOrderRtn rpcOrderRtn = RpcOrderRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onOrderRtn(rpcOrderRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:ORDER_RTN", e);
                    }
                });
                break;
            }
            case RpcId.TRADE_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcTradeRtn rpcTradeRtn = RpcTradeRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onTradeRtn(rpcTradeRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:TRADE_RTN", e);
                    }
                });
                break;
            }
            case RpcId.POSITION_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcPositionRtn rpcPositionRtn = RpcPositionRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onPositionRtn(rpcPositionRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:POSITION_RTN", e);
                    }
                });
                break;
            }
            case RpcId.ACCOUNT_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcAccountRtn rpcAccountRtn = RpcAccountRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onAccountRtn(rpcAccountRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:ACCOUNT_RTN", e);
                    }
                });
                break;
            }
            case RpcId.CONTRACT_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcContractRtn rpcContractRtn = RpcContractRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onContractRtn(rpcContractRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:CONTRACT_RTN", e);
                    }
                });
                break;
            }
            case RpcId.NOTICE_RTN_VALUE: {
                tradeRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcNoticeRtn rpcNoticeRtn = RpcNoticeRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onNoticeRtn(rpcNoticeRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:NOTICE_RTN", e);
                    }
                });
                break;
            }
            case RpcId.TICK_RTN_VALUE: {
                marketRtnQueueSingleExecutorService.execute(() -> {
                    try {
                        RpcTickRtn rpcTickRtn = RpcTickRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onTickRtn(rpcTickRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:TICK_RTN", e);
                    }
                });
                break;
            }
            case RpcId.ORDER_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcOrderListRtn rpcOrderListRtn = RpcOrderListRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onOrderListRtn(rpcOrderListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:ORDER_LIST_RTN", e);
                    }
                });
                break;
            }
            case RpcId.TRADE_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcTradeListRtn rpcTradeListRtn = RpcTradeListRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onTradeListRtn(rpcTradeListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:TRADE_LIST_RTN", e);
                    }
                });
                break;
            }
            case RpcId.CONTRACT_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcContractListRtn rpcContractListRtn = RpcContractListRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onContractListRtn(rpcContractListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:CONTRACT_LIST_RTN", e);
                    }
                });
                break;
            }
            case RpcId.POSITION_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcPositionListRtn rpcPositionListRtn = RpcPositionListRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onPositionListRtn(rpcPositionListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:POSITION_LIST_RTN", e);
                    }
                });
                break;
            }
            case RpcId.ACCOUNT_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcAccountListRtn rpcAccountListRtn = RpcAccountListRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onAccountListRtn(rpcAccountListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:ACCOUNT_LIST_RTN", e);
                    }
                });
                break;
            }
            case RpcId.TICK_LIST_RTN_VALUE: {
                unimportantSingleThreadExecutorService.execute(() -> {
                    try {
                        RpcTickListRtn rpcTickListRtn = RpcTickListRtn.parseFrom(contentByteString);
                        rpcClientRtnHandlerService.onTickListRtn(rpcTickListRtn);
                    } catch (Exception e) {
                        logger.error("处理RPC异常,RPC:TICK_LIST_RTN", e);
                    }
                });
                break;
            }

            default:
                logger.error("处理RPC错误,RPC ID:{},不支持此功能", rpcId);
                break;
        }
    }

    public boolean sendRpc(RpcId rpcId, String transactionId, ByteString content) {
        byte[] data = RpcUtils.generateRpcDep(rpcId, transactionId, content);

        if (!webSocketClientHandler.sendData(data)) {
            logger.error("发送RPC错误,RPC:{},业务ID:{}", rpcId.getValueDescriptor().getName(), transactionId);
            return false;
        }
        return true;
    }

    // 这个方法一般用于发送Req,当发生一般错误时,会通知唤醒Rpc等待线程
    public boolean sendAsyncHttpRpc(RpcId rpcId, String transactionId, ByteString content) {

        asyncHttpRpcExecutor.execute(() -> {
            try {
                HttpEntity<String> requestEntity = RpcUtils.generateHttpEntity(configService.getAuthToken(), rpcId, content);

                ResponseEntity<String> responseEntity = restTemplate.exchange(configService.getPriorityRpcURI(), HttpMethod.POST, requestEntity, String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    ResponseVo<String> ret = JSON.parseObject(responseEntity.getBody(), new TypeReference<ResponseVo<String>>() {
                    });
                    if (ret == null) {
                        rpcRspHandlerService.notifyAndRemoveRpcLock(transactionId);
                    } else if (ret.isStatus()) {
                        String base64Data = ret.getVoData();
                        if (logger.isDebugEnabled()) {
                            logger.debug("业务ID:{},接收到的Base64Data:{}", transactionId, base64Data);
                        }
                        if (base64Data != null) {
                            byte[] data = Base64.getDecoder().decode(base64Data);
                            processData(data);
                        } else {
                            rpcRspHandlerService.notifyAndRemoveRpcLock(transactionId);
                        }
                    } else {
                        logger.error("HTTP RPC返回200,但状态回报错误,业务ID:{},RPC:{},信息:{}", transactionId, rpcId, ret.getMessage());
                        rpcRspHandlerService.notifyAndRemoveRpcLock(transactionId);
                    }
                } else {
                    logger.error("HTTP RPC状态非200,业务ID:{},RPC:{},状态码为:{}", transactionId, rpcId, responseEntity.getStatusCode().value());
                    rpcRspHandlerService.notifyAndRemoveRpcLock(transactionId);
                }

            } catch (Exception e) {
                logger.error("HTTP RPC错误,业务ID:{},RPC:{}", transactionId, rpcId, e);
                rpcRspHandlerService.notifyAndRemoveRpcLock(transactionId);
            }
        });
        return true;

    }

}
