package xyz.redtorch.node.master.rpc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.redtorch.node.master.rpc.service.RpcServerOverWebSocketReqHandlerService;
import xyz.redtorch.node.master.service.MasterTradeExecuteService;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

@Service
public class RpcServerOverWebSocketReqHandlerServiceImpl implements RpcServerOverWebSocketReqHandlerService {


    @Autowired
    private MasterTradeExecuteService masterTradeExecuteService;

    @Override
    public void subscribe(String sessionId, CommonReqField commonReq, ContractField contract) {
        masterTradeExecuteService.subscribe(sessionId, commonReq, contract);
    }

    @Override
    public void unsubscribe(String sessionId, CommonReqField commonReq, ContractField contract) {
        masterTradeExecuteService.unsubscribe(sessionId, commonReq, contract);
    }

    @Override
    public void submitOrder(String sessionId, CommonReqField commonReq, SubmitOrderReqField submitOrderReq) {
        masterTradeExecuteService.submitOrder(sessionId, commonReq, submitOrderReq);
    }

    @Override
    public void cancelOrder(String sessionId, CommonReqField commonReq, CancelOrderReqField cancelOrderReq) {
        masterTradeExecuteService.cancelOrder(sessionId, commonReq, cancelOrderReq);

    }

    @Override
    public void searchContract(String sessionId, CommonReqField commonReq, ContractField contract) {
        masterTradeExecuteService.searchContract(sessionId, commonReq, contract);

    }


}
