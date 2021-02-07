package xyz.redtorch.desktop.rpc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.service.RpcRspHandlerService;
import xyz.redtorch.common.util.rpc.RpcLock;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.ConfigService;
import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.*;

@Service
public class RpcClientApiServiceImpl implements RpcClientApiService {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientApiServiceImpl.class);

    @Autowired
    private RpcClientProcessService rpcClientProcessService;
    @Autowired
    private RpcRspHandlerService rpcRspHandlerService;
    @Autowired
    private ConfigService configService;

    @Override
    public boolean asyncSubscribe(ContractField contract, String transactionId) {

        if (contract == null) {
            logger.error("订阅错误,参数contract缺失");
            return false;
        } else if (StringUtils.isBlank(transactionId)) {
            logger.error("订阅错误,参数transactionId缺失");
            return false;
        } else {
            RpcSubscribeReq.Builder rpcSubscribeReqBuilder = RpcSubscribeReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)) //
                    .setContract(contract);
            return rpcClientProcessService.sendRpc(RpcId.SUBSCRIBE_REQ, transactionId, rpcSubscribeReqBuilder.build().toByteString());
        }
    }

    @Override
    public boolean subscribe(ContractField contract, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncSubscribe(contract, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return false;
        }

        String logPartial = "订阅";

        RpcSubscribeRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcSubscribeRsp.class, logPartial);
        if (rsp == null) {
            return false;
        }

        return rpcRspHandlerService.processBooleanRsp(finalTransactionId, rsp.getCommonRsp(), logPartial);
    }

    @Override
    public boolean asyncUnsubscribe(ContractField contract, String transactionId) {

        if (contract == null) {
            logger.error("取消订阅错误,参数contract缺失");
            return false;
        } else if (StringUtils.isBlank(transactionId)) {
            logger.error("取消订阅错误,参数transactionId缺失");
            return false;
        } else {
            RpcUnsubscribeReq.Builder rpcUnsubscribeReqBuilder = RpcUnsubscribeReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)) //
                    .setContract(contract);
            return rpcClientProcessService.sendRpc(RpcId.UNSUBSCRIBE_REQ, transactionId, rpcUnsubscribeReqBuilder.build().toByteString());

        }

    }

    @Override
    public boolean unsubscribe(ContractField contract, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncUnsubscribe(contract, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return false;
        }

        String logPartial = "取消订阅";

        RpcUnsubscribeRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcUnsubscribeRsp.class, logPartial);
        if (rsp == null) {
            return false;
        }
        return rpcRspHandlerService.processBooleanRsp(finalTransactionId, rsp.getCommonRsp(), logPartial);
    }

    @Override
    public boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, String transactionId) {
        if (submitOrderReq == null) {
            logger.error("提交定单错误,参数submitOrderReq缺失");
            return false;
        } else if (StringUtils.isBlank(transactionId)) {
            logger.error("提交定单错误,参数transactionId缺失");
            return false;
        } else {
            RpcSubmitOrderReq.Builder rpcSubmitOrderReqBuilder = RpcSubmitOrderReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)) //
                    .setSubmitOrderReq(submitOrderReq);
            return rpcClientProcessService.sendRpc(RpcId.SUBMIT_ORDER_REQ, transactionId, rpcSubmitOrderReqBuilder.build().toByteString());
        }
    }

    @Override
    public String submitOrder(SubmitOrderReqField submitOrderReq, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncSubmitOrder(submitOrderReq, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "提交定单";

        RpcSubmitOrderRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcSubmitOrderRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        CommonRspField commonRsp = rsp.getCommonRsp();
        if (commonRsp.getErrorId() == 0) {
            logger.info("{}完成,业务ID:{}", logPartial, finalTransactionId);
            return rsp.getOrderId();
        } else {
            logger.error("{}错误,业务ID:{},错误ID:{},错误信息{}", logPartial, finalTransactionId, commonRsp.getErrorId(), commonRsp.getErrorMsg());
        }
        return null;

    }

    @Override
    public boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, String transactionId) {
        if (cancelOrderReq == null) {
            logger.error("撤销定单错误,参数cancelOrderReq缺失");
            return false;
        } else if (StringUtils.isBlank(transactionId)) {
            logger.error("撤销定单错误,参数transactionId缺失");
            return false;
        } else {
            RpcCancelOrderReq.Builder rpcCancelOrderReqBuilder = RpcCancelOrderReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)) //
                    .setCancelOrderReq(cancelOrderReq);
            return rpcClientProcessService.sendRpc(RpcId.CANCEL_ORDER_REQ, transactionId, rpcCancelOrderReqBuilder.build().toByteString());
        }
    }

    @Override
    public boolean cancelOrder(CancelOrderReqField cancelOrderReq, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncCancelOrder(cancelOrderReq, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return false;
        }

        String logPartial = "撤销定单";

        RpcCancelOrderRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcCancelOrderRsp.class, logPartial);
        if (rsp == null) {
            return false;
        }

        return rpcRspHandlerService.processBooleanRsp(finalTransactionId, rsp.getCommonRsp(), logPartial);
    }

    @Override
    public boolean asyncSearchContract(ContractField contract, String transactionId) {
        if (contract == null) {
            logger.error("搜寻合约错误,参数contract缺失");
            return false;
        } else if (StringUtils.isBlank(transactionId)) {
            logger.error("搜寻合约错误,参数transactionId缺失");
            return false;
        } else {

            RpcSearchContractReq.Builder rpcSearchContractReqBuilder = RpcSearchContractReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)) //
                    .setContract(contract);
            return rpcClientProcessService.sendRpc(RpcId.SEARCH_CONTRACT_REQ, transactionId, rpcSearchContractReqBuilder.build().toByteString());
        }
    }

    @Override
    public boolean searchContract(ContractField contract, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncSearchContract(contract, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return false;
        }
        String logPartial = "搜寻合约";

        RpcSearchContractRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcSearchContractRsp.class, logPartial);
        if (rsp == null) {
            return false;
        }
        return rpcRspHandlerService.processBooleanRsp(finalTransactionId, rsp.getCommonRsp(), logPartial);
    }

    @Override
    public boolean asyncGetContractList(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询合约列表错误,参数transactionId缺失");
            return false;
        } else {

            RpcGetContractListReq.Builder rpcGetContractListReqBuilder = RpcGetContractListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId));

            return rpcClientProcessService.sendAsyncHttpRpc( RpcId.GET_CONTRACT_LIST_REQ, transactionId, rpcGetContractListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcGetContractListRsp getContractList(String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetContractList(finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询合约列表";

        RpcGetContractListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcGetContractListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);

    }

    @Override
    public boolean asyncGetTickList(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询Tick列表错误,参数transactionId缺失");
            return false;
        } else {


            RpcGetTickListReq.Builder rpcGetTickListReqBuilder = RpcGetTickListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId));

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.GET_TICK_LIST_REQ, transactionId, rpcGetTickListReqBuilder.build().toByteString());
        }
    }

    @Override
    public RpcGetTickListRsp getTickList(String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetTickList(finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询Tick列表";

        RpcGetTickListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcGetTickListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);

    }

    @Override
    public boolean asyncGetOrderList(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询定单列表错误,参数transactionId缺失");
            return false;
        } else {

            RpcGetOrderListReq.Builder rpcGetOrderListReqBuilder = RpcGetOrderListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId));

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.GET_ORDER_LIST_REQ, transactionId, rpcGetOrderListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcGetOrderListRsp getOrderList(String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetOrderList(finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询定单列表";

        RpcGetOrderListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcGetOrderListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);

    }

    @Override
    public boolean asyncGetPositionList(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询持仓列表错误,参数transactionId缺失");
            return false;
        } else {

            RpcGetPositionListReq.Builder rpcGetPositionListReqBuilder = RpcGetPositionListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId));

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.GET_POSITION_LIST_REQ, transactionId, rpcGetPositionListReqBuilder.build().toByteString());
        }
    }

    @Override
    public RpcGetPositionListRsp getPositionList(String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetPositionList(finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询持仓列表";

        RpcGetPositionListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcGetPositionListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);

    }

    @Override
    public boolean asyncGetTradeList(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询成交列表错误,参数transactionId缺失");
            return false;
        } else {
            RpcGetTradeListReq.Builder rpcGetTradeListReqBuilder = RpcGetTradeListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId));

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.GET_TRADE_LIST_REQ, transactionId, rpcGetTradeListReqBuilder.build().toByteString());
        }
    }

    @Override
    public RpcGetTradeListRsp getTradeList(String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetTradeList(finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询成交列表";

        RpcGetTradeListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcGetTradeListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);

    }

    @Override
    public boolean asyncGetAccountList(String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询账户列表错误,参数transactionId缺失");
            return false;
        } else {


            RpcGetAccountListReq.Builder rpcGetAccountListReqBuilder = RpcGetAccountListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId));

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.GET_ACCOUNT_LIST_REQ, transactionId, rpcGetAccountListReqBuilder.build().toByteString());
        }
    }

    @Override
    public RpcGetAccountListRsp getAccountList(String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetAccountList(finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询账户列表";

        RpcGetAccountListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcGetAccountListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);

    }

    @Override
    public boolean asyncQueryDBBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询历史Bar列表错误,参数transactionId缺失");
            return false;
        } else {


            RpcQueryDBBarListReq.Builder rpcQueryDBBarListReqBuilder = RpcQueryDBBarListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)).setStartTimestamp(startTimestamp).setEndTimestamp(endTimestamp).setUniformSymbol(uniformSymbol).setBarPeriod(barPeriod)
                    .setMarketDataDBType(marketDataDBType);

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.QUERY_DB_BAR_LIST_REQ, transactionId, rpcQueryDBBarListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcQueryDBBarListRsp queryDBBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType, String transactionId,
                                               Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncQueryDBBarList(startTimestamp, endTimestamp, uniformSymbol, barPeriod, marketDataDBType, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }
        String logPartial = "查询Bar列表";

        RpcQueryDBBarListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcQueryDBBarListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);
    }

    @Override
    public boolean asyncQueryDBTickList(long startTimestamp, long endTimestamp, String uniformSymbol, MarketDataDBTypeEnum marketDataDBType, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询历史Tick列表错误,参数transactionId缺失");
            return false;
        } else {


            RpcQueryDBTickListReq.Builder rpcQueryDBTickListReqBuilder = RpcQueryDBTickListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)).setStartTimestamp(startTimestamp).setEndTimestamp(endTimestamp).setUniformSymbol(uniformSymbol).setMarketDataDBType(marketDataDBType);

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.QUERY_DB_TICK_LIST_REQ, transactionId, rpcQueryDBTickListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcQueryDBTickListRsp queryDBTickList(long startTimestamp, long endTimestamp, String uniformSymbol, MarketDataDBTypeEnum marketDataDBType, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncQueryDBTickList(startTimestamp, endTimestamp, uniformSymbol, marketDataDBType, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询历史Tick";

        RpcQueryDBTickListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcQueryDBTickListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);

    }

    @Override
    public boolean asyncQueryVolumeBarList(long startTimestamp, long endTimestamp, String uniformSymbol, int volume, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询历史Bar列表错误,参数transactionId缺失");
            return false;
        } else {

            RpcQueryVolumeBarListReq.Builder rpcQueryVolumeBarListReqBuilder = RpcQueryVolumeBarListReq.newBuilder() //
                    .setCommonReq(generateCommonReq(transactionId)).setStartTimestamp(startTimestamp).setEndTimestamp(endTimestamp).setUniformSymbol(uniformSymbol).setVolume(volume);

            return rpcClientProcessService.sendAsyncHttpRpc(RpcId.QUERY_VOLUME_BAR_LIST_REQ, transactionId, rpcQueryVolumeBarListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcQueryVolumeBarListRsp queryVolumeBarList(long startTimestamp, long endTimestamp, String uniformSymbol, int volume, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncQueryVolumeBarList(startTimestamp, endTimestamp, uniformSymbol, volume, finalTransactionId)) {
            rpcRspHandlerService.unregisterLock(rpcLock);
            return null;
        }

        String logPartial = "查询VolBar列表";

        RpcQueryVolumeBarListRsp rsp = rpcRspHandlerService.processObjectRsp(finalTransactionId, rpcLock, RpcQueryVolumeBarListRsp.class, logPartial);
        if (rsp == null) {
            return null;
        }
        return rpcRspHandlerService.processCommonRsp(finalTransactionId, rsp.getCommonRsp(), rsp, logPartial);
    }

    private CommonReqField.Builder generateCommonReq(String transactionId) {
        return CommonReqField.newBuilder() //
                .setOperatorId(configService.getOperatorId() == null ? "" : configService.getOperatorId())//
                .setTransactionId(transactionId);
    }


}
