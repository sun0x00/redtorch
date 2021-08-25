package xyz.redtorch.node.master.rpc.service;

import xyz.redtorch.pb.CoreRpc.*;

public interface RpcServerOverWebSocketRtnHandlerService {
    void onOrderRtn(RpcOrderRtn rpcOrderRtn);

    void onTradeRtn(RpcTradeRtn rpcTradeRtn);

    void onContractRtn(RpcContractRtn rpcContractRtn);

    void onPositionRtn(RpcPositionRtn rpcPositionRtn);

    void onAccountRtn(RpcAccountRtn rpcAccountRtn);

    void onTickRtn(RpcTickRtn rpcTickRtn);

    void onNoticeRtn(RpcNoticeRtn rpcNoticeRtn);

    void onOrderListRtn(RpcOrderListRtn rpcOrderListRtn);

    void onTradeListRtn(RpcTradeListRtn rpcTradeListRtn);

    void onContractListRtn(RpcContractListRtn rpcContractListRtn);

    void onPositionListRtn(RpcPositionListRtn rpcPositionListRtn);

    void onAccountListRtn(RpcAccountListRtn rpcAccountListRtn);

    void onTickListRtn(RpcTickListRtn rpcTickListRtn);
}
