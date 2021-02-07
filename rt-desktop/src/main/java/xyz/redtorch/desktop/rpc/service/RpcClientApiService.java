package xyz.redtorch.desktop.rpc.service;

import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreRpc.*;

public interface RpcClientApiService {

    boolean asyncSubscribe(ContractField contract, String transactionId);

    boolean subscribe(ContractField contract, String transactionId, Integer timeoutSeconds);

    boolean asyncUnsubscribe(ContractField contract, String transactionId);

    boolean unsubscribe(ContractField contract, String transactionId, Integer timeoutSeconds);

    boolean asyncSubmitOrder(SubmitOrderReqField submitOrderReq, String transactionId);

    String submitOrder(SubmitOrderReqField submitOrderReq, String transactionId, Integer timeoutSeconds);

    boolean asyncCancelOrder(CancelOrderReqField cancelOrderReq, String transactionId);

    boolean cancelOrder(CancelOrderReqField cancelOrderReq, String transactionId, Integer timeoutSeconds);

    boolean asyncSearchContract(ContractField contract, String transactionId);

    boolean searchContract(ContractField contract, String transactionId, Integer timeoutSeconds);

    boolean asyncGetContractList(String transactionId);

    RpcGetContractListRsp getContractList(String transactionId, Integer timeoutSeconds);

    boolean asyncGetTickList(String transactionId);

    RpcGetTickListRsp getTickList(String transactionId, Integer timeoutSeconds);

    boolean asyncGetOrderList(String transactionId);

    RpcGetOrderListRsp getOrderList(String transactionId, Integer timeoutSeconds);

    boolean asyncGetPositionList(String transactionId);

    RpcGetPositionListRsp getPositionList(String transactionId, Integer timeoutSeconds);

    boolean asyncGetTradeList(String transactionId);

    RpcGetTradeListRsp getTradeList(String transactionId, Integer timeoutSeconds);

    boolean asyncGetAccountList(String transactionId);

    RpcGetAccountListRsp getAccountList(String transactionId, Integer timeoutSeconds);

    boolean asyncQueryDBBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType, String transactionId);

    RpcQueryDBBarListRsp queryDBBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, MarketDataDBTypeEnum marketDataDBType, String transactionId,
                                        Integer timeoutSeconds);

    boolean asyncQueryDBTickList(long startTimestamp, long endTimestamp, String uniformSymbol, MarketDataDBTypeEnum marketDataDBType, String transactionId);

    RpcQueryDBTickListRsp queryDBTickList(long startTimestamp, long endTimestamp, String uniformSymbol, MarketDataDBTypeEnum marketDataDBType, String transactionId, Integer timeoutSeconds);

    boolean asyncQueryVolumeBarList(long startTimestamp, long endTimestamp, String uniformSymbol, int volume, String transactionId);

    RpcQueryVolumeBarListRsp queryVolumeBarList(long startTimestamp, long endTimestamp, String uniformSymbol, int volume, String transactionId, Integer timeoutSeconds);
}
