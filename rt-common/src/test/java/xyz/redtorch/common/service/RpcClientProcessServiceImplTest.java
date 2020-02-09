package xyz.redtorch.common.service;

import com.google.protobuf.ByteString;
import xyz.redtorch.pb.CoreRpc.RpcId;

public class RpcClientProcessServiceImplTest implements RpcClientProcessService {

	private int V_NUMBER = 0;

	@Override
	public void processData(byte[] data) {
	}

	@Override
	public boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {
		System.out.println("RpcClientProcessServiceImplTest targetNodeId:" + targetNodeId);
		System.out.println("RpcClientProcessServiceImplTest V_NUMBER:" + V_NUMBER);
		return false;
	}

	@Override
	public boolean sendLz4CoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {
		return false;
	}

	@Override
	public void onWsClosed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWsError() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWsConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHeartbeat(String result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean sendCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {
		// TODO Auto-generated method stub
		return false;
	}
}
