package xyz.redtorch.node.master.rpc.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.service.RpcRspHandlerService;
import xyz.redtorch.common.util.rpc.RpcLock;
import xyz.redtorch.node.master.rpc.service.RpcServerApiService;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketProcessService;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreRpc.*;

@Service
public class RpcServerApiServiceImpl implements RpcServerApiService {

    private static final Logger logger = LoggerFactory.getLogger(RpcServerApiServiceImpl.class);

    @Value("${rt.master.operatorId}")
    private String masterOperatorId;
    @Autowired
    private RpcRspHandlerService rpcRspHandlerService;

    @Autowired
    private RpcServerOverWebSocketProcessService rpcOverWebSocketProcessService;

    @Override
    public boolean asyncSubscribe(ContractField contract, String sessionId, String transactionId) {

        if (StringUtils.isBlank(transactionId)) {
            logger.error("订阅错误,参数transactionId缺失");
            return false;
        } else if (contract == null) {
            logger.error("订阅错误,请求ID:{},参数contract缺失", transactionId);
            return false;
        } else if (StringUtils.isBlank(sessionId)) {
            logger.error("订阅错误,请求ID{},参数sessionId错误", sessionId);
            return false;
        } else {
            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId)//
                    .setTransactionId(transactionId);

            RpcSubscribeReq.Builder rpcSubscribeReqBuilder = RpcSubscribeReq.newBuilder() //
                    .setCommonReq(commonReqBuilder) //
                    .setContract(contract);
            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.SUBSCRIBE_REQ, transactionId, rpcSubscribeReqBuilder.build().toByteString());
        }

    }

    @Override
    public boolean subscribe(ContractField contract, String sessionId, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncSubscribe(contract, sessionId, finalTransactionId)) {
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
    public boolean asyncUnsubscribe(ContractField contract, String sessionId, String transactionId) {

        if (StringUtils.isBlank(transactionId)) {
            logger.error("取消订阅错误,参数transactionId缺失");
            return false;
        } else if (contract == null) {
            logger.error("取消订阅错误,请求ID:{},参数contract缺失", transactionId);
            return false;
        } else if (StringUtils.isBlank(sessionId)) {
            logger.error("取消订阅错误,请求ID:{},参数sessionId错误", sessionId);
            return false;
        } else {
            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcUnsubscribeReq.Builder rpcUnsubscribeReqBuilder = RpcUnsubscribeReq.newBuilder() //
                    .setCommonReq(commonReqBuilder) //
                    .setContract(contract);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.UNSUBSCRIBE_REQ, transactionId, rpcUnsubscribeReqBuilder.build().toByteString());

        }

    }

    @Override
    public boolean unsubscribe(ContractField contract, String sessionId, String transactionId, Integer timeoutSeconds) {

        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncUnsubscribe(contract, sessionId, finalTransactionId)) {
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
    public boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("提交定单错误,参数transactionId缺失");
            return false;
        } else if (submitOrderReq == null) {
            logger.error("提交定单错误,请求ID:{},参数submitOrderReq缺失", transactionId);
            return false;
        } else if (StringUtils.isBlank(sessionId)) {
            logger.error("提交定单错误,请求ID:{},参数sessionId错误", sessionId);
            return false;
        } else {
            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcSubmitOrderReq.Builder rpcSubmitOrderReqBuilder = RpcSubmitOrderReq.newBuilder() //
                    .setCommonReq(commonReqBuilder) //
                    .setSubmitOrderReq(submitOrderReq); //

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.SUBMIT_ORDER_REQ, transactionId, rpcSubmitOrderReqBuilder.build().toByteString());

        }
    }

    @Override
    public String submitOrder(SubmitOrderReqField submitOrderReq, String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncSubmitOrder(submitOrderReq, sessionId, finalTransactionId)) {
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
    public boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("撤销定单错误,参数transactionId缺失");
            return false;
        } else if (cancelOrderReq == null) {
            logger.error("撤销定单错误,请求ID:{},参数cancelOrderReq缺失", transactionId);
            return false;
        } else if (StringUtils.isBlank(sessionId)) {
            logger.error("撤销定单错误,请求ID:{},参数sessionId错误", sessionId);
            return false;
        } else {
            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcCancelOrderReq.Builder rpcCancelOrderReqBuilder = RpcCancelOrderReq.newBuilder() //
                    .setCommonReq(commonReqBuilder) //
                    .setCancelOrderReq(cancelOrderReq);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.CANCEL_ORDER_REQ, transactionId, rpcCancelOrderReqBuilder.build().toByteString());

        }
    }

    @Override
    public boolean cancelOrder(CancelOrderReqField cancelOrderReq, String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncCancelOrder(cancelOrderReq, sessionId, finalTransactionId)) {
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
    public boolean asyncSearchContract(ContractField contract, String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("搜寻合约错误,参数transactionId缺失");
            return false;
        } else if (contract == null) {
            logger.error("搜寻合约错误,请求ID:{},参数contract缺失", transactionId);
            return false;
        } else if (StringUtils.isBlank(sessionId)) {
            logger.error("搜寻合约错误,请求ID:{},参数sessionId错误", sessionId);
            return false;
        } else {
            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcSearchContractReq.Builder rpcSearchContractReqBuilder = RpcSearchContractReq.newBuilder() //
                    .setCommonReq(commonReqBuilder) //
                    .setContract(contract);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.SEARCH_CONTRACT_REQ, transactionId, rpcSearchContractReqBuilder.build().toByteString());

        }
    }

    @Override
    public boolean searchContract(ContractField contract, String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncSearchContract(contract, sessionId, finalTransactionId)) {
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
    public boolean asyncGetContractList(String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询合约列表错误,参数transactionId缺失");
            return false;
        } else {

            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcGetContractListReq.Builder rpcGetContractListReqBuilder = RpcGetContractListReq.newBuilder() //
                    .setCommonReq(commonReqBuilder);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.GET_CONTRACT_LIST_REQ, transactionId, rpcGetContractListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcGetContractListRsp getContractList(String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetContractList(sessionId, finalTransactionId)) {
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
    public boolean asyncGetTickList(String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询Tick列表错误,参数transactionId缺失");
            return false;
        } else {
            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcGetTickListReq.Builder rpcGetTickListReqBuilder = RpcGetTickListReq.newBuilder() //
                    .setCommonReq(commonReqBuilder);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.GET_TICK_LIST_REQ, transactionId, rpcGetTickListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcGetTickListRsp getTickList(String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetTickList(sessionId, finalTransactionId)) {
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
    public boolean asyncGetOrderList(String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询定单列表错误,参数transactionId缺失");
            return false;
        } else {


            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcGetOrderListReq.Builder rpcGetOrderListReqBuilder = RpcGetOrderListReq.newBuilder() //
                    .setCommonReq(commonReqBuilder);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.GET_ORDER_LIST_REQ, transactionId, rpcGetOrderListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcGetOrderListRsp getOrderList(String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetOrderList(sessionId, finalTransactionId)) {
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
    public boolean asyncGetPositionList(String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询持仓列表错误,参数transactionId缺失");
            return false;
        } else {


            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //


                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcGetPositionListReq.Builder rpcGetPositionListReqBuilder = RpcGetPositionListReq.newBuilder() //
                    .setCommonReq(commonReqBuilder);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.GET_POSITION_LIST_REQ, transactionId, rpcGetPositionListReqBuilder.build().toByteString());

        }
    }

    @Override
    public RpcGetPositionListRsp getPositionList(String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetPositionList(sessionId, finalTransactionId)) {
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
    public boolean asyncGetTradeList(String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询成交列表错误,参数transactionId缺失");
            return false;
        } else {
            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcGetTradeListReq.Builder rpcGetTradeListReqBuilder = RpcGetTradeListReq.newBuilder() //
                    .setCommonReq(commonReqBuilder);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.GET_TRADE_LIST_REQ, transactionId, rpcGetTradeListReqBuilder.build().toByteString());
        }
    }

    @Override
    public RpcGetTradeListRsp getTradeList(String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetTradeList(sessionId, finalTransactionId)) {
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
    public boolean asyncGetAccountList(String sessionId, String transactionId) {
        if (StringUtils.isBlank(transactionId)) {
            logger.error("查询账户列表错误,参数transactionId缺失");
            return false;
        } else {


            CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //


                    .setOperatorId(masterOperatorId) //
                    .setTransactionId(transactionId);

            RpcGetAccountListReq.Builder rpcGetAccountListReqBuilder = RpcGetAccountListReq.newBuilder() //
                    .setCommonReq(commonReqBuilder);

            return rpcOverWebSocketProcessService.sendCoreRpc(sessionId, RpcId.GET_ACCOUNT_LIST_REQ, transactionId, rpcGetAccountListReqBuilder.build().toByteString());
        }
    }

    @Override
    public RpcGetAccountListRsp getAccountList(String sessionId, String transactionId, Integer timeoutSeconds) {
        RpcLock rpcLock = rpcRspHandlerService.getRpcLock(transactionId, timeoutSeconds);
        String finalTransactionId = rpcLock.getTransactionId();
        if (!asyncGetAccountList(sessionId, finalTransactionId)) {
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

}
