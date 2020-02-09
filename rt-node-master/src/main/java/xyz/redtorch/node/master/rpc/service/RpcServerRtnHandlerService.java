package xyz.redtorch.node.master.rpc.service;

import xyz.redtorch.pb.CoreRpc.RpcAccountListRtn;
import xyz.redtorch.pb.CoreRpc.RpcAccountRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractListRtn;
import xyz.redtorch.pb.CoreRpc.RpcContractRtn;
import xyz.redtorch.pb.CoreRpc.RpcNoticeRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderListRtn;
import xyz.redtorch.pb.CoreRpc.RpcOrderRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionListRtn;
import xyz.redtorch.pb.CoreRpc.RpcPositionRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTickRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeListRtn;
import xyz.redtorch.pb.CoreRpc.RpcTradeRtn;

public interface RpcServerRtnHandlerService {
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
