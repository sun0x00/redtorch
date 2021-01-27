package xyz.redtorch.node.slave.rpc.service.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.util.rpc.RpcUtils;
import xyz.redtorch.node.slave.rpc.service.RpcClientReqHandlerService;
import xyz.redtorch.node.slave.service.ConfigService;
import xyz.redtorch.node.slave.web.socket.WebSocketClientHandler;
import xyz.redtorch.pb.CoreRpc.*;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RpcClientProcessServiceImpl implements RpcClientProcessService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProcessServiceImpl.class);

    private final ExecutorService importantExecutorService = Executors.newCachedThreadPool();
    @Autowired
    private RpcClientReqHandlerService rpcClientReqHandlerService;
    @Autowired
    private WebSocketClientHandler webSocketClientHandler;
    @Autowired
    private ConfigService configService;

    private ExecutorService normalExecutorService;

    @Override
    public void afterPropertiesSet() {
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
            case RpcId.SUBMIT_ORDER_REQ_VALUE: {
                importantExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSubmitOrderReq rpcSubmitOrderReq = RpcSubmitOrderReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcSubmitOrderReq.getCommonReq());
                        transactionId = rpcSubmitOrderReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.submitOrder(rpcSubmitOrderReq.getCommonReq(), rpcSubmitOrderReq.getSubmitOrderReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:SUBMIT_ORDER_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
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
                        rpcClientReqHandlerService.cancelOrder(rpcCancelOrderReq.getCommonReq(), rpcCancelOrderReq.getCancelOrderReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:CANCEL_ORDER_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.SEARCH_CONTRACT_REQ_VALUE: {
                importantExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcSearchContractReq rpcSearchContractReq = RpcSearchContractReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcSearchContractReq.getCommonReq());
                        transactionId = rpcSearchContractReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.searchContract(rpcSearchContractReq.getCommonReq(), rpcSearchContractReq.getContract());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:SEARCH_CONTRACT_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }

            case RpcId.GET_TICK_LIST_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetTickListReq rpcGetTickListReq = RpcGetTickListReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcGetTickListReq.getCommonReq());
                        transactionId = rpcGetTickListReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.getTickList(rpcGetTickListReq.getCommonReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_TICK_LIST_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.GET_POSITION_LIST_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetPositionListReq rpcGetPositionListReq = RpcGetPositionListReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcGetPositionListReq.getCommonReq());
                        transactionId = rpcGetPositionListReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.getPositionList(rpcGetPositionListReq.getCommonReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_POSITION_LIST_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.GET_CONTRACT_LIST_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetContractListReq rpcGetContractListReq = RpcGetContractListReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcGetContractListReq.getCommonReq());
                        transactionId = rpcGetContractListReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.getContractList(rpcGetContractListReq.getCommonReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_CONTRACT_LIST_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.GET_ACCOUNT_LIST_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetAccountListReq rpcGetAccountListReq = RpcGetAccountListReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcGetAccountListReq.getCommonReq());
                        transactionId = rpcGetAccountListReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.getAccountList(rpcGetAccountListReq.getCommonReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_ACCOUNT_LIST_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.GET_ORDER_LIST_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetOrderListReq rpcGetOrderListReq = RpcGetOrderListReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcGetOrderListReq.getCommonReq());
                        transactionId = rpcGetOrderListReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.getOrderList(rpcGetOrderListReq.getCommonReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_ORDER_LIST_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            case RpcId.GET_TRADE_LIST_REQ_VALUE: {
                normalExecutorService.execute(() -> {
                    String transactionId = "";
                    try {
                        RpcGetTradeListReq rpcGetTradeListReq = RpcGetTradeListReq.parseFrom(contentByteString);
                        RpcUtils.checkCommonReq(rpcGetTradeListReq.getCommonReq());
                        transactionId = rpcGetTradeListReq.getCommonReq().getTransactionId();
                        rpcClientReqHandlerService.getTradeList(rpcGetTradeListReq.getCommonReq());
                    } catch (Exception e) {
                        logger.error("处理RPC异常,业务ID:{},RPC:GET_TRADE_LIST_REQ", transactionId, e);
                        sendExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                    }
                });
                break;
            }
            default:
                logger.error("处理RPC错误,RPC ID:{},不支持此功能", rpcId);
                sendExceptionRsp(rpcId, "", timestamp, "不支持此功能");
                break;
        }

    }

    public void sendExceptionRsp(int originalRpcId, String originalTransactionId, long originalTimestamp, String info) {
        if (info == null) {
            info = "";
        }
        ByteString content = RpcExceptionRsp.newBuilder() //
                .setOriginalRpcId(originalRpcId) //
                .setOriginalTransactionId(originalTransactionId) //
                .setOriginalTimestamp(originalTimestamp) //
                .setInfo(info) //
                .build().toByteString();
        sendRpc(RpcId.EXCEPTION_RSP, originalTransactionId, content);
    }

    public boolean sendRpc(RpcId rpcId, String transactionId, ByteString content) {
        byte[] data = RpcUtils.generateRpcDep(rpcId, transactionId, content);

        if (!webSocketClientHandler.sendData(data)) {
            logger.error("发送RPC错误,RPC:{},业务ID:{}", rpcId.getValueDescriptor().getName(), transactionId);
            return false;
        }
        return true;
    }

    @Override
    public boolean sendAsyncHttpRpc(RpcId rpcId, String transactionId, ByteString content) {
        logger.error("发送HTTP RPC错误,RPC:{},业务ID:{},未实现此功能", rpcId.getValueDescriptor().getName(), transactionId);
        return false;
    }

}
