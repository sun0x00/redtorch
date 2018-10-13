package xyz.redtorch.gateway.ib;

import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.gateway.GatewayAbstract;
import xyz.redtorch.core.gateway.GatewaySetting;
import xyz.redtorch.core.service.FastEventEngineService;

/**
 * @author sun0x00@gmail.com
 */
public class IbGateway extends GatewayAbstract {
    
	protected IbWrapper ibWrapper;

	public IbGateway(FastEventEngineService fastEventEngineService,GatewaySetting gatewaySetting) {
		super(fastEventEngineService, gatewaySetting);
		ibWrapper = new IbWrapper(this);
	}


	@Override
	public void subscribe(SubscribeReq subscribeReq) {
		if(ibWrapper!=null) {
			ibWrapper.subscribe(subscribeReq);
		}
	}

	@Override
	public void unSubscribe(String rtSymbol) {
		
		if(ibWrapper!=null) {
			ibWrapper.unSubscribe(rtSymbol);
		}
	}

	@Override
	public void connect() {
		if(ibWrapper!=null) {
			// 发起连接
			ibWrapper.connect();
			// 查询服务器时间
			ibWrapper.reqCurrentTime();
		}
	}

	@Override
	public void close() {
		if(ibWrapper!=null) {
			ibWrapper.disconnect();
		}
	}

	@Override
	public String sendOrder(OrderReq orderReq) {
		if(ibWrapper!=null) {
			return ibWrapper.sendOrder(orderReq);
		}else {
			return null;
		}

	}

	@Override
	public void cancelOrder(CancelOrderReq cancelOrderReq) {
		if(ibWrapper!=null) {
			ibWrapper.cancelOrder(cancelOrderReq);
		}
	}

	@Override
	public boolean isConnected() {
		return ibWrapper!=null && ibWrapper.isConnected();
	}

}
