package xyz.redtorch.common.service;

import xyz.redtorch.common.util.rpc.RpcLock;
import xyz.redtorch.pb.CoreField.CommonRspField;

public interface RpcRspHandlerService {

    Object getAndRemoveRpcRsp(String transactionId);

    void onRpcRsp(String transactionId, Object object);

    RpcLock getRpcLock(String transactionId, Integer timeoutSeconds);

    void registerRpcLock(RpcLock rpcLock);

    void unregisterLock(RpcLock rpcLock);

    <T> T processCommonRsp(String transactionId, CommonRspField commonRsp, Object rsp, String logPartial);

    boolean processBooleanRsp(String transactionId, CommonRspField commonRsp, String logPartial);

    <T> T processObjectRsp(String transactionId, RpcLock rpcLock, Class<T> cls, String logPartial);

    void notifyAndRemoveRpcLock(String transactionId);
}
