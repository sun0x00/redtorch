package xyz.redtorch.node.slave.rpc.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.util.rpc.RpcUtils;
import xyz.redtorch.node.slave.rpc.service.RpcClientReqHandlerService;
import xyz.redtorch.node.slave.service.ConfigService;
import xyz.redtorch.node.slave.service.SlaveTradeCachesService;
import xyz.redtorch.node.slave.service.SlaveTradeExecuteService;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RpcClientReqHandlerServiceImpl implements RpcClientReqHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientReqHandlerServiceImpl.class);

    @Autowired
    private SlaveTradeExecuteService slaveTradeExecuteService;
    @Autowired
    private RpcClientProcessService rpcClientProcessService;
    @Autowired
    private SlaveTradeCachesService slaveTradeCachesService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private RestTemplate restTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);


    @Override
    public void submitOrder(CommonReqField commonReq, SubmitOrderReqField submitOrderReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        String orderId = null;
        if (submitOrderReq == null) {
            logger.error("提交订单错误,请求ID:{},参数submitOrderReq缺失", transactionId);
            commonRspBuilder.setErrorId(1);
            commonRspBuilder.setErrorMsg("提交订单错误,参数submitOrderReq缺失");
        } else {
            try {
                orderId = slaveTradeExecuteService.submitOrder(commonReq, submitOrderReq);
                commonRspBuilder.setErrorId(0);
                commonRspBuilder.setErrorMsg("提交定单完成,原始定单ID:" + submitOrderReq.getOriginOrderId() + "定单ID:" + orderId);
            } catch (Exception e) {
                logger.error("提交定单错误,请求ID:{}", transactionId, e);
                commonRspBuilder.setErrorId(1);
                commonRspBuilder.setErrorMsg("提交定单错误,异常信息:" + e.getMessage());
            }
        }
        RpcSubmitOrderRsp.Builder rpcSubmitOrderRspBuilder = RpcSubmitOrderRsp.newBuilder();
        if (orderId == null) {
            rpcSubmitOrderRspBuilder.setCommonRsp(commonRspBuilder);
        } else {
            rpcSubmitOrderRspBuilder.setCommonRsp(commonRspBuilder).setOrderId(orderId);
        }
        rpcClientProcessService.sendRpc(RpcId.SUBMIT_ORDER_RSP, transactionId, rpcSubmitOrderRspBuilder.build().toByteString());
    }

    @Override
    public void cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);
        if (cancelOrderReq == null) {
            logger.error("撤销定单错误,请求ID:{},参数cancelOrderReq缺失", transactionId);
            commonRspBuilder.setErrorId(1);
            commonRspBuilder.setErrorMsg("撤销定单错误,参数cancelOrderReq缺失");
        } else {
            try {
                slaveTradeExecuteService.cancelOrder(commonReq, cancelOrderReq);
            } catch (Exception e) {
                logger.error("撤销定单错误,请求ID:{}", transactionId, e);
                commonRspBuilder.setErrorId(1);
                commonRspBuilder.setErrorMsg("撤销定单错误,异常信息:" + e.getMessage());
            }
        }

        RpcCancelOrderRsp.Builder rpcCancelOrderRspBuilder = RpcCancelOrderRsp.newBuilder().setCommonRsp(commonRspBuilder);
        rpcClientProcessService.sendRpc(RpcId.CANCEL_ORDER_RSP, transactionId, rpcCancelOrderRspBuilder.build().toByteString());
    }

    @Override
    public void searchContract(CommonReqField commonReq, ContractField contract) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        if (contract == null) {
            logger.error("搜寻合约错误,请求ID:{},参数contract缺失", transactionId);
            commonRspBuilder.setErrorId(1);
            commonRspBuilder.setErrorMsg("搜寻合约错误,参数contract缺失");
        } else {
            try {
                slaveTradeExecuteService.searchContract(commonReq, contract);
            } catch (Exception e) {
                logger.error("搜寻合约错误,请求ID:{}", transactionId, e);
                commonRspBuilder.setErrorId(1);
                commonRspBuilder.setErrorMsg("搜寻合约错误,异常信息:" + e.getMessage());
            }
        }

        RpcSearchContractRsp.Builder rpcSearchContractRspBuilder = RpcSearchContractRsp.newBuilder().setCommonRsp(commonRspBuilder);
        rpcClientProcessService.sendRpc(RpcId.SEARCH_CONTRACT_RSP, transactionId, rpcSearchContractRspBuilder.build().toByteString());
    }

    // -------------------------------------------------------------------------------------------------------------

    // 以下方法通过处理的WebSocket请求通过HTTP发送回报

    @Override
    public void getOrderList(CommonReqField commonReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        List<OrderField> orderList = slaveTradeCachesService.getOrderList();

        if (orderList == null) {
            orderList = new ArrayList<>();
        }

        RpcGetOrderListRsp.Builder rpcGetOrderListRspBuilder = RpcGetOrderListRsp.newBuilder() //
                .setCommonRsp(commonRspBuilder) //
                .addAllOrder(orderList);

        RpcUtils.sendAsyncHttpRpc(executor, restTemplate, configService.getRpcURI(), configService.getAuthToken(), RpcId.GET_ORDER_LIST_RSP, rpcGetOrderListRspBuilder.build().toByteString());
    }

    @Override
    public void getTradeList(CommonReqField commonReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        List<TradeField> tradeList = slaveTradeCachesService.getTradeList();
        if (tradeList == null) {
            tradeList = new ArrayList<>();
        }

        RpcGetTradeListRsp.Builder rpcGetTradeListRspBuilder = RpcGetTradeListRsp.newBuilder() //
                .setCommonRsp(commonRspBuilder) //
                .addAllTrade(tradeList);
        RpcUtils.sendAsyncHttpRpc(executor, restTemplate, configService.getRpcURI(), configService.getAuthToken(), RpcId.GET_TRADE_LIST_RSP, rpcGetTradeListRspBuilder.build().toByteString());
    }

    @Override
    public void getPositionList(CommonReqField commonReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        List<PositionField> positionList = slaveTradeCachesService.getPositionList();

        if (positionList == null) {
            positionList = new ArrayList<>();
        }

        RpcGetPositionListRsp.Builder rpcGetPositionListRspBuilder = RpcGetPositionListRsp.newBuilder() //
                .setCommonRsp(commonRspBuilder) //
                .addAllPosition(positionList);
        RpcUtils.sendAsyncHttpRpc(executor, restTemplate, configService.getRpcURI(), configService.getAuthToken(), RpcId.GET_POSITION_LIST_RSP, rpcGetPositionListRspBuilder.build().toByteString());
    }

    @Override
    public void getAccountList(CommonReqField commonReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        List<AccountField> accountList = slaveTradeCachesService.getAccountList();

        if (accountList == null) {
            accountList = new ArrayList<>();
        }

        RpcGetAccountListRsp.Builder rpcGetAccountListRspBuilder = RpcGetAccountListRsp.newBuilder().setCommonRsp(commonRspBuilder) //
                .addAllAccount(accountList); //
        RpcUtils.sendAsyncHttpRpc(executor, restTemplate, configService.getRpcURI(), configService.getAuthToken(), RpcId.GET_ACCOUNT_LIST_RSP, rpcGetAccountListRspBuilder.build().toByteString());
    }

    @Override
    public void getContractList(CommonReqField commonReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        List<ContractField> contractList = slaveTradeCachesService.getContractList();

        if (contractList == null) {
            contractList = new ArrayList<>();
        }

        RpcGetContractListRsp.Builder rpcGetContractListRspBuilder = RpcGetContractListRsp.newBuilder() //
                .setCommonRsp(commonRspBuilder) //
                .addAllContract(contractList);
        RpcUtils.sendAsyncHttpRpc(executor, restTemplate, configService.getRpcURI(), configService.getAuthToken(), RpcId.GET_CONTRACT_LIST_RSP, rpcGetContractListRspBuilder.build().toByteString());
    }

    @Override
    public void getTickList(CommonReqField commonReq) {
        String transactionId = commonReq.getTransactionId();

        CommonRspField.Builder commonRspBuilder = CommonRspField.newBuilder() //
                .setTransactionId(transactionId) //
                .setErrorId(0);

        List<TickField> tickList = slaveTradeCachesService.getTickList();
        if (tickList == null) {
            tickList = new ArrayList<>();
        }

        RpcGetTickListRsp.Builder rpcGetTickListRspBuilder = RpcGetTickListRsp.newBuilder() //
                .setCommonRsp(commonRspBuilder) //
                .addAllTick(tickList);
        RpcUtils.sendAsyncHttpRpc(executor, restTemplate, configService.getRpcURI(), configService.getAuthToken(), RpcId.GET_TICK_LIST_RSP, rpcGetTickListRspBuilder.build().toByteString());
    }

}
