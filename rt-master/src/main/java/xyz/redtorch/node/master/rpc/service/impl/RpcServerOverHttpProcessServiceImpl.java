package xyz.redtorch.node.master.rpc.service.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.service.RpcRspHandlerService;
import xyz.redtorch.common.util.rpc.RpcUtils;
import xyz.redtorch.node.master.rpc.service.RpcServerOverHttpProcessService;
import xyz.redtorch.node.master.rpc.service.RpcServerOverHttpReqHandlerService;
import xyz.redtorch.pb.CoreRpc.*;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.ContentType;

@Service
public class RpcServerOverHttpProcessServiceImpl implements RpcServerOverHttpProcessService {

    @Autowired
    private RpcServerOverHttpReqHandlerService rpcServerOverHttpReqHandlerService;
    @Autowired
    private RpcRspHandlerService rpcRspHandlerService;

    private static final Logger logger = LoggerFactory.getLogger(RpcServerOverHttpProcessServiceImpl.class);

    @Override
    public byte[] processData(String sessionId, byte[] data) {

        if (data == null) {
            logger.error("处理DEP错误,接收到空数据,会话ID:{}", sessionId);
            return null;
        }
        DataExchangeProtocol dep;
        try {
            dep = DataExchangeProtocol.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            logger.error("处理DEP错误,PB解析数据异常,会话ID:{}", sessionId, e);
            logger.error("处理DEP错误,PB解析数据异常,会话ID:{},原始数据HEX:{}", sessionId, Hex.encodeHexString(data));
            return null;
        }

        ContentType contentType = dep.getContentType();
        int rpcId = dep.getRpcId();
        long timestamp = dep.getTimestamp();

        if (logger.isDebugEnabled()) {
            String contentTypeValueName = contentType.getValueDescriptor().getName();
            logger.debug("处理DEP记录,会话ID:{},内容类型:{},RPC ID:{},时间戳:{}", sessionId, contentTypeValueName, rpcId, timestamp);
        }

        ByteString contentByteString = RpcUtils.processByteString( contentType,  dep.getContentBytes(),  rpcId, timestamp);

        if(contentByteString == null){
            return null;
        }

        return doRpc(sessionId, rpcId, contentByteString, timestamp);
    }

    public byte[] doRpc(String sessionId, int rpcId, ByteString contentByteString, long timestamp) {

        // 在发生错误时,部分情况下能够获取并返回业务ID
        String transactionId = "";
        switch (rpcId) {
            case RpcId.UNKNOWN_RPC_ID_VALUE: {
                logger.error("处理RPC,会话ID:{},RPC ID:UNKNOWN_RPC_ID_VALUE,时间戳:{}", sessionId, timestamp);
                break;
            }
            case RpcId.GET_ORDER_LIST_REQ_VALUE: {
                try {
                    RpcGetOrderListReq rpcGetOrderListReq = RpcGetOrderListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetOrderListReq.getCommonReq());
                    transactionId = rpcGetOrderListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getOrderList(rpcGetOrderListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_ORDER_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.GET_WORKING_ORDER_LIST_REQ_VALUE: {
                try {
                    RpcGetWorkingOrderListReq rpcGetWorkingOrderListReq = RpcGetWorkingOrderListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetWorkingOrderListReq.getCommonReq());
                    transactionId = rpcGetWorkingOrderListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getWorkingOrderList(rpcGetWorkingOrderListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_WORKING_ORDER_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_ORDER_BY_ORDER_ID_REQ_VALUE: {
                try {
                    RpcQueryOrderByOrderIdReq rpcQueryOrderByOrderIdReq = RpcQueryOrderByOrderIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryOrderByOrderIdReq.getCommonReq());
                    transactionId = rpcQueryOrderByOrderIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryOrderByOrderId(rpcQueryOrderByOrderIdReq.getCommonReq(), rpcQueryOrderByOrderIdReq.getOrderId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_ORDER_BY_ORDER_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_ORDER_BY_ORIGIN_ORDER_ID_REQ_VALUE: {

                try {
                    RpcQueryOrderByOriginOrderIdReq rpcQueryOrderByOriginOrderIdReq = RpcQueryOrderByOriginOrderIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryOrderByOriginOrderIdReq.getCommonReq());
                    transactionId = rpcQueryOrderByOriginOrderIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryOrderByOriginOrderId(rpcQueryOrderByOriginOrderIdReq.getCommonReq(), rpcQueryOrderByOriginOrderIdReq.getOriginOrderId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_ORDER_BY_ORIGIN_ORDER_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_ORDER_LIST_BY_ACCOUNT_ID_REQ_VALUE: {
                try {
                    RpcQueryOrderListByAccountIdReq rpcQueryOrderListByAccountIdReq = RpcQueryOrderListByAccountIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryOrderListByAccountIdReq.getCommonReq());
                    transactionId = rpcQueryOrderListByAccountIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryOrderListByAccountId(rpcQueryOrderListByAccountIdReq.getCommonReq(), rpcQueryOrderListByAccountIdReq.getAccountId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_ORDER_LIST_BY_ACCOUNT_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_ORDER_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {
                try {
                    RpcQueryOrderListByUnifiedSymbolReq rpcQueryOrderListByUnifiedSymbolReq = RpcQueryOrderListByUnifiedSymbolReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryOrderListByUnifiedSymbolReq.getCommonReq());
                    transactionId = rpcQueryOrderListByUnifiedSymbolReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryOrderListByUnifiedSymbol(rpcQueryOrderListByUnifiedSymbolReq.getCommonReq(), rpcQueryOrderListByUnifiedSymbolReq.getUnifiedSymbol());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_ORDER_LIST_BY_UNIFIED_SYMBOL_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.GET_TRADE_LIST_REQ_VALUE: {
                try {
                    RpcGetTradeListReq rpcGetTradeListReq = RpcGetTradeListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetTradeListReq.getCommonReq());
                    transactionId = rpcGetTradeListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getTradeList(rpcGetTradeListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_TRADE_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.GET_TICK_LIST_REQ_VALUE: {
                try {
                    RpcGetTickListReq rpcGetTickListReq = RpcGetTickListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetTickListReq.getCommonReq());
                    transactionId = rpcGetTickListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getTickList(sessionId, rpcGetTickListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_TICK_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_TRADE_BY_TRADE_ID_REQ_VALUE: {
                try {
                    RpcQueryTradeByTradeIdReq rpcQueryTradeByTradeIdReq = RpcQueryTradeByTradeIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryTradeByTradeIdReq.getCommonReq());
                    transactionId = rpcQueryTradeByTradeIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryTradeByTradeId(rpcQueryTradeByTradeIdReq.getCommonReq(), rpcQueryTradeByTradeIdReq.getTradeId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_TRADE_BY_TRADE_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_TRADE_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {
                try {
                    RpcQueryTradeListByUnifiedSymbolReq rpcQueryTradeListByUnifiedSymbolReq = RpcQueryTradeListByUnifiedSymbolReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryTradeListByUnifiedSymbolReq.getCommonReq());
                    transactionId = rpcQueryTradeListByUnifiedSymbolReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryTradeListByUnifiedSymbol(rpcQueryTradeListByUnifiedSymbolReq.getCommonReq(), rpcQueryTradeListByUnifiedSymbolReq.getUnifiedSymbol());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_TRADE_LIST_BY_UNIFIED_SYMBOL_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_TRADE_LIST_BY_ACCOUNT_ID_REQ_VALUE: {
                try {
                    RpcQueryTradeListByAccountIdReq rpcQueryTradeListByAccountIdReq = RpcQueryTradeListByAccountIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryTradeListByAccountIdReq.getCommonReq());
                    transactionId = rpcQueryTradeListByAccountIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryTradeListByAccountId(rpcQueryTradeListByAccountIdReq.getCommonReq(), rpcQueryTradeListByAccountIdReq.getAccountId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_TRADE_LIST_BY_ACCOUNT_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_TRADE_LIST_BY_ORDER_ID_REQ_VALUE: {
                try {
                    RpcQueryTradeListByOrderIdReq rpcQueryTradeListByOrderIdReq = RpcQueryTradeListByOrderIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryTradeListByOrderIdReq.getCommonReq());
                    transactionId = rpcQueryTradeListByOrderIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryTradeListByOrderId(rpcQueryTradeListByOrderIdReq.getCommonReq(), rpcQueryTradeListByOrderIdReq.getOrderId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_TRADE_LIST_BY_ORDER_ID_REQ2", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_TRADE_LIST_BY_ORIGIN_ORDER_ID_REQ_VALUE: {
                try {
                    RpcQueryTradeListByOriginOrderIdReq rpcQueryTradeListByOriginOrderIdReq = RpcQueryTradeListByOriginOrderIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryTradeListByOriginOrderIdReq.getCommonReq());
                    transactionId = rpcQueryTradeListByOriginOrderIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryTradeListByOriginOrderId(rpcQueryTradeListByOriginOrderIdReq.getCommonReq(), rpcQueryTradeListByOriginOrderIdReq.getOriginOrderId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_TRADE_LIST_BY_ORIGIN_ORDER_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.GET_POSITION_LIST_REQ_VALUE: {
                try {
                    RpcGetPositionListReq rpcGetPositionListReq = RpcGetPositionListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetPositionListReq.getCommonReq());
                    transactionId = rpcGetPositionListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getPositionList(rpcGetPositionListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_POSITION_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_POSITION_BY_POSITION_ID_REQ_VALUE: {
                try {
                    RpcQueryPositionByPositionIdReq rpcQueryPositionByPositionIdReq = RpcQueryPositionByPositionIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryPositionByPositionIdReq.getCommonReq());
                    transactionId = rpcQueryPositionByPositionIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryPositionByPositionId(rpcQueryPositionByPositionIdReq.getCommonReq(), rpcQueryPositionByPositionIdReq.getPositionId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_POSITION_BY_POSITION_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_POSITION_LIST_BY_ACCOUNT_ID_REQ_VALUE: {

                try {
                    RpcQueryPositionListByAccountIdReq rpcQueryPositionListByAccountIdReq = RpcQueryPositionListByAccountIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryPositionListByAccountIdReq.getCommonReq());
                    transactionId = rpcQueryPositionListByAccountIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryPositionListByAccountId(rpcQueryPositionListByAccountIdReq.getCommonReq(), rpcQueryPositionListByAccountIdReq.getAccountId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_POSITION_LIST_BY_ACCOUNT_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_POSITION_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {

                try {
                    RpcQueryPositionListByUnifiedSymbolReq rpcQueryPositionListByUnifiedSymbolReq = RpcQueryPositionListByUnifiedSymbolReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryPositionListByUnifiedSymbolReq.getCommonReq());
                    transactionId = rpcQueryPositionListByUnifiedSymbolReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryPositionListByUnifiedSymbol(rpcQueryPositionListByUnifiedSymbolReq.getCommonReq(), rpcQueryPositionListByUnifiedSymbolReq.getUnifiedSymbol());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_POSITION_LIST_BY_UNIFIED_SYMBOL_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.GET_ACCOUNT_LIST_REQ_VALUE: {
                try {
                    RpcGetAccountListReq rpcGetAccountListReq = RpcGetAccountListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetAccountListReq.getCommonReq());
                    transactionId = rpcGetAccountListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getAccountList(rpcGetAccountListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_ACCOUNT_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_ACCOUNT_BY_ACCOUNT_ID_REQ_VALUE: {
                try {
                    RpcQueryAccountByAccountIdReq rpcQueryAccountByAccountIdReq = RpcQueryAccountByAccountIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryAccountByAccountIdReq.getCommonReq());
                    transactionId = rpcQueryAccountByAccountIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryAccountByAccountId(rpcQueryAccountByAccountIdReq.getCommonReq(), rpcQueryAccountByAccountIdReq.getAccountId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_ACCOUNT_BY_ACCOUNT_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_ACCOUNT_LIST_BY_ACCOUNT_CODE_REQ_VALUE: {
                try {
                    RpcQueryAccountListByAccountCodeReq rpcQueryAccountListByAccountCodeReq = RpcQueryAccountListByAccountCodeReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryAccountListByAccountCodeReq.getCommonReq());
                    transactionId = rpcQueryAccountListByAccountCodeReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryAccountListByAccountCode(rpcQueryAccountListByAccountCodeReq.getCommonReq(), rpcQueryAccountListByAccountCodeReq.getAccountCode());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_ACCOUNT_LIST_BY_ACCOUNT_CODE_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.GET_CONTRACT_LIST_REQ_VALUE: {
                try {
                    RpcGetContractListReq rpcGetContractListReq = RpcGetContractListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetContractListReq.getCommonReq());
                    transactionId = rpcGetContractListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getContractList(rpcGetContractListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_CONTRACT_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_CONTRACT_BY_CONTRACT_ID_REQ_VALUE: {
                try {
                    RpcQueryContractByContractIdReq rpcQueryContractByContractIdReq = RpcQueryContractByContractIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryContractByContractIdReq.getCommonReq());
                    transactionId = rpcQueryContractByContractIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryContractByContractId(rpcQueryContractByContractIdReq.getCommonReq(), rpcQueryContractByContractIdReq.getContractId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_CONTRACT_BY_CONTRACT_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_CONTRACT_LIST_BY_UNIFIED_SYMBOL_REQ_VALUE: {
                try {
                    RpcQueryContractListByUnifiedSymbolReq rpcQueryContractListByUnifiedSymbolReq = RpcQueryContractListByUnifiedSymbolReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryContractListByUnifiedSymbolReq.getCommonReq());
                    transactionId = rpcQueryContractListByUnifiedSymbolReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryContractListByUnifiedSymbol(rpcQueryContractListByUnifiedSymbolReq.getCommonReq(), rpcQueryContractListByUnifiedSymbolReq.getUnifiedSymbol());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_CONTRACT_LIST_BY_UNIFIED_SYMBOL_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_CONTRACT_LIST_BY_GATEWAY_ID_REQ_VALUE: {
                try {
                    RpcQueryContractListByGatewayIdReq rpcQueryContractListByGatewayIdReq = RpcQueryContractListByGatewayIdReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryContractListByGatewayIdReq.getCommonReq());
                    transactionId = rpcQueryContractListByGatewayIdReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryContractListByGatewayId(rpcQueryContractListByGatewayIdReq.getCommonReq(), rpcQueryContractListByGatewayIdReq.getGatewayId());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_CONTRACT_LIST_BY_GATEWAY_ID_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_REQ_VALUE: {
                try {
                    RpcSyncSlaveNodeRuntimeDataReq rpcSyncSlaveNodeRuntimeDataReq = RpcSyncSlaveNodeRuntimeDataReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcSyncSlaveNodeRuntimeDataReq.getCommonReq());
                    transactionId = rpcSyncSlaveNodeRuntimeDataReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.syncSlaveNodeRuntimeData(sessionId, rpcSyncSlaveNodeRuntimeDataReq.getCommonReq(), rpcSyncSlaveNodeRuntimeDataReq.getGatewayList());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:SYNC_SLAVE_NODE_RUNTIME_DATA_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }

            case RpcId.GET_MIX_CONTRACT_LIST_REQ_VALUE: {
                try {
                    RpcGetMixContractListReq rpcGetMixContractListReq = RpcGetMixContractListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcGetMixContractListReq.getCommonReq());
                    transactionId = rpcGetMixContractListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.getMixContractList(rpcGetMixContractListReq.getCommonReq());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_MIX_CONTRACT_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_DB_BAR_LIST_REQ_VALUE: {
                try {
                    RpcQueryDBBarListReq rpcQueryDBBarListReq = RpcQueryDBBarListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryDBBarListReq.getCommonReq());
                    transactionId = rpcQueryDBBarListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryDBBarList(rpcQueryDBBarListReq.getCommonReq(), rpcQueryDBBarListReq.getStartTimestamp(), rpcQueryDBBarListReq.getEndTimestamp(),
                            rpcQueryDBBarListReq.getUnifiedSymbol(), rpcQueryDBBarListReq.getBarPeriod(), rpcQueryDBBarListReq.getMarketDataDBType());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_DB_BAR_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_DB_TICK_LIST_REQ_VALUE: {
                try {
                    RpcQueryDBTickListReq rpcQueryDBTickListReq = RpcQueryDBTickListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryDBTickListReq.getCommonReq());
                    transactionId = rpcQueryDBTickListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryDBTickList(rpcQueryDBTickListReq.getCommonReq(), rpcQueryDBTickListReq.getStartTimestamp(), rpcQueryDBTickListReq.getEndTimestamp(),
                            rpcQueryDBTickListReq.getUnifiedSymbol(), rpcQueryDBTickListReq.getMarketDataDBType());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_DB_TICK_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            case RpcId.QUERY_VOLUME_BAR_LIST_REQ_VALUE: {
                try {
                    RpcQueryVolumeBarListReq rpcQueryVolumeBarListReq = RpcQueryVolumeBarListReq.parseFrom(contentByteString);
                    RpcUtils.checkCommonReq(rpcQueryVolumeBarListReq.getCommonReq());
                    transactionId = rpcQueryVolumeBarListReq.getCommonReq().getTransactionId();
                    return rpcServerOverHttpReqHandlerService.queryVolumeBarList(rpcQueryVolumeBarListReq.getCommonReq(), rpcQueryVolumeBarListReq.getStartTimestamp(), rpcQueryVolumeBarListReq.getEndTimestamp(),
                            rpcQueryVolumeBarListReq.getUnifiedSymbol(), rpcQueryVolumeBarListReq.getVolume());
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:QUERY_VOLUME_BAR_LIST_REQ", sessionId, transactionId, e);
                    return generateExceptionRsp(rpcId, transactionId, timestamp, e.getMessage());
                }
            }
            // -----------------------------------------------------
            case RpcId.GET_CONTRACT_LIST_RSP_VALUE:
            {
                try {
                    RpcGetContractListRsp rpcGetContractListRsp = RpcGetContractListRsp.parseFrom(contentByteString);
                    RpcUtils.checkCommonRsp(rpcGetContractListRsp.getCommonRsp());
                    transactionId = rpcGetContractListRsp.getCommonRsp().getTransactionId();
                    rpcRspHandlerService.onRpcRsp(transactionId, rpcGetContractListRsp);
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_CONTRACT_LIST_RSP", sessionId, transactionId, e);
                }
                return null;
            }
            case RpcId.GET_TICK_LIST_RSP_VALUE: {
                try {
                    RpcGetTickListRsp rpcGetTickListRsp = RpcGetTickListRsp.parseFrom(contentByteString);
                    RpcUtils.checkCommonRsp(rpcGetTickListRsp.getCommonRsp());
                    transactionId = rpcGetTickListRsp.getCommonRsp().getTransactionId();
                    rpcRspHandlerService.onRpcRsp(transactionId, rpcGetTickListRsp);
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_TICK_LIST_RSP", sessionId, transactionId, e);
                }
                return null;
            }
            case RpcId.GET_POSITION_LIST_RSP_VALUE: {
                        try {
                            RpcGetPositionListRsp rpcGetPositionListRsp = RpcGetPositionListRsp.parseFrom(contentByteString);
                            RpcUtils.checkCommonRsp(rpcGetPositionListRsp.getCommonRsp());
                            transactionId = rpcGetPositionListRsp.getCommonRsp().getTransactionId();
                            rpcRspHandlerService.onRpcRsp(transactionId, rpcGetPositionListRsp);
                        } catch (Exception e) {
                            logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_POSITION_LIST_RSP", sessionId, transactionId, e);
                        }
                return null;
            }
            case RpcId.GET_ACCOUNT_LIST_RSP_VALUE: {
                try {
                    RpcGetAccountListRsp rpcGetAccountListRsp = RpcGetAccountListRsp.parseFrom(contentByteString);
                    RpcUtils.checkCommonRsp(rpcGetAccountListRsp.getCommonRsp());
                    transactionId = rpcGetAccountListRsp.getCommonRsp().getTransactionId();
                    rpcRspHandlerService.onRpcRsp(transactionId, rpcGetAccountListRsp);
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_ACCOUNT_LIST_RSP", sessionId, transactionId, e);
                }
                return null;
            }
            case RpcId.GET_TRADE_LIST_RSP_VALUE: {
                try {
                    RpcGetTradeListRsp rpcGetTradeListRsp = RpcGetTradeListRsp.parseFrom(contentByteString);
                    RpcUtils.checkCommonRsp(rpcGetTradeListRsp.getCommonRsp());
                    transactionId = rpcGetTradeListRsp.getCommonRsp().getTransactionId();
                    rpcRspHandlerService.onRpcRsp(transactionId, rpcGetTradeListRsp);
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_TRADE_LIST_RSP", sessionId, transactionId, e);
                }
                return null;
            }
            case RpcId.GET_ORDER_LIST_RSP_VALUE: {
                try {
                    RpcGetOrderListRsp rpcGetOrderListRsp = RpcGetOrderListRsp.parseFrom(contentByteString);
                    RpcUtils.checkCommonRsp(rpcGetOrderListRsp.getCommonRsp());
                    transactionId = rpcGetOrderListRsp.getCommonRsp().getTransactionId();
                    rpcRspHandlerService.onRpcRsp(transactionId, rpcGetOrderListRsp);
                } catch (Exception e) {
                    logger.error("处理RPC异常,会话ID:{},业务ID:{},RPC:GET_ORDER_LIST_RSP", sessionId, transactionId, e);
                }
                return null;
            }

            default:
                logger.error("处理RPC错误,会话ID:{},RPC ID:{},不支持此功能", sessionId, rpcId);
                return generateExceptionRsp(rpcId, transactionId, timestamp, "不支持此功能");
        }

        return null;
    }


    public byte[] generateExceptionRsp(int originalRpcId, String originalTransactionId, long originalTimestamp, String info) {
        if (info == null) {
            info = "";
        }
        ByteString content = RpcExceptionRsp.newBuilder() //
                .setOriginalRpcId(originalRpcId) //
                .setOriginalTransactionId(originalTransactionId) //
                .setOriginalTimestamp(originalTimestamp) //
                .setInfo(info) //
                .build().toByteString();

        DataExchangeProtocol.Builder depBuilder = DataExchangeProtocol.newBuilder() //
                .setRpcId(RpcId.EXCEPTION_RSP_VALUE) //
                .setContentType(ContentType.ROUTINE) //
                .setContentBytes(content) //
                .setTimestamp(System.currentTimeMillis());

        return depBuilder.build().toByteArray();
    }

}
