package xyz.redtorch.trader.gateway.ctp;

import java.io.File;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.trader.engine.event.EventEngine;
import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.OrderReq;
import xyz.redtorch.trader.entity.SubscribeReq;
import xyz.redtorch.trader.gateway.GatewayAbstract;
import xyz.redtorch.trader.gateway.GatewaySetting;

/**
 * @author sun0x00@gmail.com
 */
public class CtpGateway extends GatewayAbstract {

	static {		
		String classPath = CtpGateway.class.getResource("/").getPath();
		String suffix = ".dll";
		if(System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
			suffix = ".dll";
		}else {
			suffix = ".so";
		}
		System.load(classPath +File.separator+ "assembly" + File.separator + "libiconv"+ suffix);
		System.load(classPath +File.separator+ "assembly" + File.separator + "thostmduserapi"+ suffix);
		System.load(classPath +File.separator+ "assembly" + File.separator + "jctpmdapiv6v3v11x64"+ suffix);
		System.load(classPath +File.separator+ "assembly" + File.separator + "thosttraderapi"+ suffix);
		System.load(classPath +File.separator+ "assembly" + File.separator + "jctptraderapiv6v3v11x64"+ suffix);
	}
	
	private static Logger log = LoggerFactory.getLogger(CtpGateway.class);
	
	private HashMap<String, String> contractExchangeMap = new HashMap<>();
	private HashMap<String, Integer> contractSizeMap = new HashMap<>();

	private MdSpi mdSpi = new MdSpi(this);
	private TdSpi tdSpi = new TdSpi(this);
	
	public CtpGateway(GatewaySetting gatewaySetting, EventEngine eventEngine) {
		super(gatewaySetting, eventEngine);
		log.info("初始化CTP接口,{}", gatewayLogInfo);
	}

	public HashMap<String, String> getContractExchangeMap() {
		return contractExchangeMap;
	}

	public HashMap<String, Integer> getContractSizeMap() {
		return contractSizeMap;
	}
	
	@Override
	public void subscribe(SubscribeReq subscribeReq) {
		subscribedSymbols.add(subscribeReq.getSymbol());
		if(mdSpi!=null) {
			mdSpi.subscribe(subscribeReq.getSymbol());
		}
	}

	@Override
	public void unSubscribe(String rtSymbol) {
		subscribedSymbols.remove(rtSymbol);
		if(mdSpi!=null) {
			mdSpi.unSubscribe(rtSymbol);
		}
	}

	@Override
	public void connect() {
		if(tdSpi!=null) {
			tdSpi.connect();
		}
		if(mdSpi!=null) {
			mdSpi.connect();
		}
	}

	@Override
	public void close() {
		if(tdSpi!=null) {
			tdSpi.close();
		}
		if(mdSpi!=null) {
			mdSpi.close();
		}

	}

	@Override
	public String sendOrder(OrderReq orderReq) {
		if(tdSpi!=null) {
			return tdSpi.sendOrder(orderReq);
		}else {
			return null;
		}

	}

	@Override
	public void cancelOrder(CancelOrderReq cancelOrderReq) {
		if(tdSpi!=null) {
			tdSpi.cancelOrder(cancelOrderReq);
		}
	}

	@Override
	public void queryAccount() {
		if(tdSpi!=null) {
			tdSpi.queryAccount();
		}
	}

	@Override
	public void queryPosition() {
		if(tdSpi!=null) {
			tdSpi.queryPosition();
		}
	}

	@Override
	public boolean isConnected() {
		return tdSpi!=null&&mdSpi!=null&&tdSpi.isConnected()&&mdSpi.isConnected();
	}

}
