package xyz.redtorch.node.slave.rpc.service.impl;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.redtorch.common.service.RpcClientProcessService;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.common.util.rpc.RpcUtils;
import xyz.redtorch.node.slave.rpc.service.RpcClientApiService;
import xyz.redtorch.node.slave.service.ConfigService;
import xyz.redtorch.pb.CoreField.*;
import xyz.redtorch.pb.CoreRpc.*;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.ContentType;

import java.util.List;

@Service
public class RpcClientApiServiceImpl implements RpcClientApiService {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientApiServiceImpl.class);

    @Autowired
    private ConfigService configService;
    @Autowired
    private RpcClientProcessService rpcClientProcessService;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public RpcSyncSlaveNodeRuntimeDataRsp syncSlaveNodeRuntimeData(List<GatewayField> gatewayList) {

        if (gatewayList == null) {
            logger.error("订阅错误,参数gatewayList为空");
            return null;
        }

        String transactionId = UUIDStringPoolUtils.getUUIDString();

        CommonReqField.Builder commonReqBuilder = CommonReqField.newBuilder() //
                .setOperatorId(configService.getOperatorId()) //
                .setTransactionId(transactionId); //

        RpcSyncSlaveNodeRuntimeDataReq.Builder rpcSyncSlaveNodeRuntimeDataReqBuilder = RpcSyncSlaveNodeRuntimeDataReq.newBuilder();

        rpcSyncSlaveNodeRuntimeDataReqBuilder.setCommonReq(commonReqBuilder);

        rpcSyncSlaveNodeRuntimeDataReqBuilder.addAllGateway(gatewayList);

        // SLAVE通过HTTP RPC向Master发起的请求比较少,因此直接发送同步请求
        DataExchangeProtocol retDep = RpcUtils.sendSyncHttpRpc(restTemplate, configService.getRpcURI(), configService.getAuthToken(), RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_REQ, transactionId, rpcSyncSlaveNodeRuntimeDataReqBuilder.build().toByteString());

        if (retDep == null) {
            return null;
        }

        ContentType contentType = retDep.getContentType();
        int rpcId = retDep.getRpcId();
        long timestamp = retDep.getTimestamp();

        ByteString contentByteString = RpcUtils.processByteString(contentType, retDep.getContentBytes(), rpcId, timestamp);

        if (contentByteString == null) {
            return null;
        }

        if (retDep.getRpcId() == RpcId.EXCEPTION_RSP_VALUE) {
            try {
                RpcExceptionRsp rpcExceptionRsp = RpcExceptionRsp.parseFrom(contentByteString);
                logger.error("请求处理发生错误,业务ID:{},RPC:{},时间戳:{},附加信息:{}", rpcExceptionRsp.getOriginalTransactionId(), rpcExceptionRsp.getOriginalRpcId(), rpcExceptionRsp.getOriginalTimestamp(), rpcExceptionRsp.getInfo());
            } catch (Exception e) {
                logger.error("请求处理发生错误", e);
            }
            return null;
        } else if (retDep.getRpcId() == RpcId.SYNC_SLAVE_NODE_RUNTIME_DATA_RSP_VALUE) {
            try {
                return RpcSyncSlaveNodeRuntimeDataRsp.parseFrom(contentByteString);
            } catch (Exception e) {
                logger.error("请求处理发生错误", e);
            }
        } else {
            logger.error("收到未知RPC:{}", rpcId);
        }

        return null;
    }


    @Override
    public boolean emitPositionRtn(PositionField position) {

        RpcPositionRtn.Builder rpcPositionRtnBuilder = RpcPositionRtn.newBuilder();
        rpcPositionRtnBuilder.setPosition(position);

        return rpcClientProcessService.sendRpc(RpcId.POSITION_RTN, "", rpcPositionRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitAccountRtn(AccountField account) {
        RpcAccountRtn.Builder rpcAccountRtnBuilder = RpcAccountRtn.newBuilder();
        rpcAccountRtnBuilder.setAccount(account);

        return rpcClientProcessService.sendRpc(RpcId.ACCOUNT_RTN, "", rpcAccountRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitContractRtn(ContractField contract) {
        RpcContractRtn.Builder rpcContractRtnBuilder = RpcContractRtn.newBuilder();
        rpcContractRtnBuilder.setContract(contract);

        return rpcClientProcessService.sendRpc(RpcId.CONTRACT_RTN, "", rpcContractRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitTickRtn(TickField tick) {
        RpcTickRtn.Builder rpcTickRtnBuilder = RpcTickRtn.newBuilder();
        rpcTickRtnBuilder.setTick(tick);

        return rpcClientProcessService.sendRpc(RpcId.TICK_RTN, "", rpcTickRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitTradeRtn(TradeField trade) {
        RpcTradeRtn.Builder rpcTradeRtnBuilder = RpcTradeRtn.newBuilder();
        rpcTradeRtnBuilder.setTrade(trade);

        return rpcClientProcessService.sendRpc(RpcId.TRADE_RTN, "", rpcTradeRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitOrderRtn(OrderField order) {
        RpcOrderRtn.Builder rpcOrderRtnBuilder = RpcOrderRtn.newBuilder();
        rpcOrderRtnBuilder.setOrder(order);

        return rpcClientProcessService.sendRpc(RpcId.ORDER_RTN, "", rpcOrderRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitPositionListRtn(List<PositionField> positionList) {
        RpcPositionListRtn.Builder rpcPositionListRtnBuilder = RpcPositionListRtn.newBuilder();
        rpcPositionListRtnBuilder.addAllPosition(positionList);

        return rpcClientProcessService.sendRpc(RpcId.POSITION_LIST_RTN, "", rpcPositionListRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitAccountListRtn(List<AccountField> accountList) {
        RpcAccountListRtn.Builder rpcAccountListRtnBuilder = RpcAccountListRtn.newBuilder();
        rpcAccountListRtnBuilder.addAllAccount(accountList);

        return rpcClientProcessService.sendRpc(RpcId.ACCOUNT_LIST_RTN, "", rpcAccountListRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitContractListRtn(List<ContractField> contractList) {
        RpcContractListRtn.Builder rpcContractListRtnBuilder = RpcContractListRtn.newBuilder();
        rpcContractListRtnBuilder.addAllContract(contractList);
        return rpcClientProcessService.sendRpc(RpcId.CONTRACT_LIST_RTN, "", rpcContractListRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitTickListRtn(List<TickField> tickList) {
        RpcTickListRtn.Builder rpcTickListRtnBuilder = RpcTickListRtn.newBuilder();
        rpcTickListRtnBuilder.addAllTick(tickList);

        return rpcClientProcessService.sendRpc(RpcId.TICK_LIST_RTN, "", rpcTickListRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitTradeListRtn(List<TradeField> tradeList) {
        RpcTradeListRtn.Builder rpcTradeListRtnBuilder = RpcTradeListRtn.newBuilder();
        rpcTradeListRtnBuilder.addAllTrade(tradeList);

        return rpcClientProcessService.sendRpc(RpcId.TRADE_LIST_RTN, "", rpcTradeListRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitOrderListRtn(List<OrderField> orderList) {
        RpcOrderListRtn.Builder rpcOrderListRtnBuilder = RpcOrderListRtn.newBuilder();
        rpcOrderListRtnBuilder.addAllOrder(orderList);

        return rpcClientProcessService.sendRpc(RpcId.ORDER_LIST_RTN, "", rpcOrderListRtnBuilder.build().toByteString());
    }

    @Override
    public boolean emitNotice(NoticeField notice) {
        RpcNoticeRtn.Builder rpcNoticeRtnBuilder = RpcNoticeRtn.newBuilder();
        rpcNoticeRtnBuilder.setNotice(notice);
        return rpcClientProcessService.sendRpc(RpcId.NOTICE_RTN, "", rpcNoticeRtnBuilder.build().toByteString());
    }
}
