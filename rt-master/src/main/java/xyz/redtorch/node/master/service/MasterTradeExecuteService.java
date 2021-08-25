package xyz.redtorch.node.master.service;

import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

import java.util.List;
import java.util.Set;

public interface MasterTradeExecuteService {
    void subscribe(String sessionId, CommonReqField commonReq, ContractField contract);

    void unsubscribe(String sessionId, CommonReqField commonReq, ContractField contract);

    void submitOrder(String sessionId, CommonReqField commonReq, SubmitOrderReqField submitOrderReq);

    void cancelOrder(String sessionId, CommonReqField commonReq, CancelOrderReqField cancelOrderReq);

    void searchContract(String sessionId, CommonReqField commonReq, ContractField contract);

    String getSessionIdByOrderId(String orderId);

    String getSessionIdByOriginOrderId(String originOrderId);

    void removeSubscribeRelationBySessionId(String sessionId);

    Set<String> getSubscribedSessionIdSet(String subscribeKey);

    Set<String> getSubscribeKeySet(String sessionId);

    List<ContractField> getSubscribedContract();

    SubmitOrderReqField getSubmitOrderReqByOrderId(String orderId);
}
