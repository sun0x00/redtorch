package xyz.redtorch.gateway.ctp;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.gateway.GatewayAbstract;
import xyz.redtorch.core.gateway.GatewaySetting;
import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.utils.CommonUtil;

/**
 * @author sun0x00@gmail.com
 */
public class CtpGateway extends GatewayAbstract {

	private static Logger log = LoggerFactory.getLogger(CtpGateway.class);
	
	private Timer timer = new Timer();

	static {
		try {
			String envTmpDir = "";
			String tempLibPath = "";
			try {
				if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
	
					envTmpDir = System.getProperty("java.io.tmpdir");
					tempLibPath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator + "api"
							+ File.separator + "jctp" + File.separator + "lib";
					
					CommonUtil.copyURLToFileForTmp(tempLibPath, CtpGateway.class.getResource("/assembly/libiconv.dll"));
					CommonUtil.copyURLToFileForTmp(tempLibPath, CtpGateway.class.getResource("/assembly/thostmduserapi.dll"));
					CommonUtil.copyURLToFileForTmp(tempLibPath,
							CtpGateway.class.getResource("/assembly/jctpmdapiv6v3v11x64.dll"));
					CommonUtil.copyURLToFileForTmp(tempLibPath, CtpGateway.class.getResource("/assembly/thosttraderapi.dll"));
					CommonUtil.copyURLToFileForTmp(tempLibPath,
							CtpGateway.class.getResource("/assembly/jctptraderapiv6v3v11x64.dll"));
				} else {
	
					envTmpDir = "/tmp";
					tempLibPath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator + "api"
							+ File.separator + "jctp" + File.separator + "lib";
					
					CommonUtil.copyURLToFileForTmp(tempLibPath, CtpGateway.class.getResource("/assembly/libthostmduserapi.so"));
					CommonUtil.copyURLToFileForTmp(tempLibPath, CtpGateway.class.getResource("/assembly/libthosttraderapi.so"));
					CommonUtil.copyURLToFileForTmp(tempLibPath,
							CtpGateway.class.getResource("/assembly/libjctpmdapiv6v3v11x64.so"));
					CommonUtil.copyURLToFileForTmp(tempLibPath,
							CtpGateway.class.getResource("/assembly/libjctptraderapiv6v3v11x64.so"));
				}
			}catch (Exception e) {
				log.warn("复制库文件到临时目录失败", e);
			}
			

			if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
				System.load(tempLibPath + File.separator + "libiconv.dll");
				System.load(tempLibPath + File.separator + "thostmduserapi.dll");
				System.load(tempLibPath + File.separator + "jctpmdapiv6v3v11x64.dll");
				System.load(tempLibPath + File.separator + "thosttraderapi.dll");
				System.load(tempLibPath + File.separator + "jctptraderapiv6v3v11x64.dll");
			} else {
				System.load(tempLibPath + File.separator + "libthostmduserapi.so");
				System.load(tempLibPath + File.separator + "libjctpmdapiv6v3v11x64.so");
				System.load(tempLibPath + File.separator + "libthosttraderapi.so");
				System.load(tempLibPath + File.separator + "libjctptraderapiv6v3v11x64.so");
			}
		} catch (Exception e) {
			log.error("加载库失败!", e);
		}

	}

	private HashSet<String> subscribedSymbols = new HashSet<>();
	private HashMap<String, String> contractExchangeMap = new HashMap<>();
	private HashMap<String, Integer> contractSizeMap = new HashMap<>();
	private HashMap<String, String> contractNameMap = new HashMap<>();

	private MdSpi mdSpi = new MdSpi(this);
	private TdSpi tdSpi = new TdSpi(this);

	public CtpGateway(FastEventEngineService fastEventEngineService,GatewaySetting gatewaySetting) {
		super(fastEventEngineService, gatewaySetting);
		timer.schedule(new QueryTimerTask(), new Date(), 1000);
	}

	public HashMap<String, String> getContractExchangeMap() {
		return contractExchangeMap;
	}

	public HashMap<String, Integer> getContractSizeMap() {
		return contractSizeMap;
	}
	
	public HashMap<String, String> getContractNameMap() {
		return contractNameMap;
	}
	
	public HashSet<String> getSubscribedSymbols() {
		return subscribedSymbols;
	}

	@Override
	public void subscribe(SubscribeReq subscribeReq) {
		subscribedSymbols.add(subscribeReq.getSymbol());
		if (mdSpi != null) {
			mdSpi.subscribe(subscribeReq.getSymbol());
		}
	}

	@Override
	public void unSubscribe(String rtSymbol) {
		String[] rtSymbolArray = rtSymbol.split("\\.");
		String symbol = rtSymbol;
		if (rtSymbolArray.length > 1) {
			symbol = rtSymbol.replace(("."+rtSymbolArray[rtSymbolArray.length-1]),"");
		}
		subscribedSymbols.remove(symbol);
		if (mdSpi != null) {
			mdSpi.unSubscribe(symbol);
		}
	}

	@Override
	public void connect() {
		if (tdSpi != null) {
			tdSpi.connect();
		}
		if (mdSpi != null) {
			mdSpi.connect();
		}
	}

	@Override
	public void close() {
		// 务必判断连接状态，防止死循环
		if (tdSpi != null&&tdSpi.isConnected()) {
			tdSpi.close();
		}
		if (mdSpi != null&&mdSpi.isConnected()) {
			mdSpi.close();
		}
		
		// 在这里发送事件主要是由于接口可能自动断开，需要广播通知
		fastEventEngineService.emitSimpleEvent(EventConstant.EVENT_GATEWAY, EventConstant.EVENT_GATEWAY,null);
	}

	@Override
	public String sendOrder(OrderReq orderReq) {
		if (tdSpi != null) {
			return tdSpi.sendOrder(orderReq);
		} else {
			return null;
		}

	}

	@Override
	public void cancelOrder(CancelOrderReq cancelOrderReq) {
		if (tdSpi != null) {
			tdSpi.cancelOrder(cancelOrderReq);
		}
	}

	public void queryAccount() {
		if (tdSpi != null) {
			tdSpi.queryAccount();
		}
	}

	public void queryPosition() {
		if (tdSpi != null) {
			tdSpi.queryPosition();
		}
	}

	@Override
	public boolean isConnected() {
		return tdSpi != null && mdSpi != null && tdSpi.isConnected() && mdSpi.isConnected();
	}
	
	class QueryTimerTask extends TimerTask{

	    @Override
	    public void run() {
	    	try {
		    	if(isConnected()) {
			        queryAccount();
		    	}
			    Thread.sleep(1250);
			    if(isConnected()) {
				    queryPosition();
			    }
			    Thread.sleep(1250);
	    	}catch (Exception e) {
				log.error(gatewayLogInfo+"定时查询发生异常",e);
			}
	    }
	}


}
