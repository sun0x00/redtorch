package xyz.redtorch.desktop.rpc.service;

import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreRpc.*;

public interface RpcClientApiService {

    boolean asyncSubscribe(ContractField contract, String reqId);

    boolean subscribe(ContractField contract, String reqId, Integer timeoutSeconds);

    boolean asyncUnsubscribe(ContractField contract, String reqId);

    boolean unsubscribe(ContractField contract, String reqId, Integer timeoutSeconds);

    boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, String reqId);

    String submitOrder(SubmitOrderReqField submitOrderReq, String reqId, Integer timeoutSeconds);

    boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, String reqId);

    boolean cancelOrder(CancelOrderReqField cancelOrderReq, String reqId, Integer timeoutSeconds);

    boolean asyncSearchContract(ContractField contract, String reqId);

    boolean searchContract(ContractField contract, String reqId, Integer timeoutSeconds);

    boolean asyncGetContractList(String reqId);

    RpcGetContractListRsp getContractList(String reqId, Integer timeoutSeconds);

    boolean asyncGetTickList(String reqId);

    RpcGetTickListRsp getTickList(String reqId, Integer timeoutSeconds);

    boolean asyncGetOrderList(String reqId);

    RpcGetOrderListRsp getOrderList(String reqId, Integer timeoutSeconds);

    boolean asyncGetPositionList(String reqId);

    RpcGetPositionListRsp getPositionList(String reqId, Integer timeoutSeconds);

    boolean asyncGetTradeList(String reqId);

    RpcGetTradeListRsp getTradeList(String reqId, Integer timeoutSeconds);

    boolean asyncGetAccountList(String reqId);

    RpcGetAccountListRsp getAccountList(String reqId, Integer timeoutSeconds);

    boolean asyncQueryDBBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType, String reqId);

    RpcQueryDBBarListRsp queryDBBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType, String reqId,
                                        Integer timeoutSeconds);

    boolean asyncQueryDBTickList(long startTimestamp, long endTimestamp, String uniformSymbol, MarketDataDBTypeEnum marketDataDBType, String reqId);

    RpcQueryDBTickListRsp queryDBTickList(long startTimestamp, long endTimestamp, String uniformSymbol, MarketDataDBTypeEnum marketDataDBType, String reqId, Integer timeoutSeconds);

    boolean asyncQueryVolumeBarList(long startTimestamp, long endTimestamp, String uniformSymbol, int volume, String reqId);

    RpcQueryVolumeBarListRsp queryVolumeBarList(long startTimestamp, long endTimestamp, String uniformSymbol, int volume, String reqId, Integer timeoutSeconds);
}
