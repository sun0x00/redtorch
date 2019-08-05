package xyz.redtorch.gateway.ctp.x64v6v3v15v;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.common.service.FastEventService;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.gateway.GatewayApiAbstract;
import xyz.redtorch.pb.CoreEnum.GatewayTypeEnum;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

/**
 * @author sun0x00@gmail.com
 */
public class CtpGatewayImpl extends GatewayApiAbstract {

	private static Logger logger = LoggerFactory.getLogger(CtpGatewayImpl.class);

	static {
		String envTmpDir = "";
		String tempLibPath = "";
		try {
			if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {

				envTmpDir = System.getProperty("java.io.tmpdir");
				tempLibPath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator
						+ "api" + File.separator + "jctp" + File.separator + "lib" + File.separator + "jctpv6v3v15x64api" + File.separator  ; 

				CommonUtils.copyURLToFileForTmp(tempLibPath, CtpGatewayImpl.class.getResource("/assembly/libiconv.dll"));
				CommonUtils.copyURLToFileForTmp(tempLibPath,
						CtpGatewayImpl.class.getResource("/assembly/jctpv6v3v15x64api/thostmduserapi_se.dll"));
				CommonUtils.copyURLToFileForTmp(tempLibPath,
						CtpGatewayImpl.class.getResource("/assembly/jctpv6v3v15x64api/thosttraderapi_se.dll"));
				CommonUtils.copyURLToFileForTmp(tempLibPath,
						CtpGatewayImpl.class.getResource("/assembly/jctpv6v3v15x64api/jctpv6v3v15x64api.dll"));
			} else {

				envTmpDir = "/tmp";
				tempLibPath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator
						+ "api" + File.separator + "jctp" + File.separator + "lib" + File.separator + "jctpv6v3v15x64api" + File.separator  ; 

				CommonUtils.copyURLToFileForTmp(tempLibPath, CtpGatewayImpl.class.getResource("/assembly/jctpv6v3v15x64api/libthostmduserapi_se.so"));
				CommonUtils.copyURLToFileForTmp(tempLibPath, CtpGatewayImpl.class.getResource("/assembly/jctpv6v3v15x64api/libthosttraderapi_se.so"));
				CommonUtils.copyURLToFileForTmp(tempLibPath,
						CtpGatewayImpl.class.getResource("/assembly/jctpv6v3v15x64api/libjctpv6v3v15x64api.so"));
			}
		} catch (Exception e) {
			logger.warn("复制运行库失败", e);
		}

		try {
			if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
				System.load(tempLibPath + File.separator + "libiconv.dll");
				System.load(tempLibPath + File.separator + "thostmduserapi_se.dll");
				System.load(tempLibPath + File.separator + "thosttraderapi_se.dll");
				System.load(tempLibPath + File.separator + "jctpv6v3v15x64api.dll");
			} else {
				System.load(tempLibPath + File.separator + "libthostmduserapi_se.so");
				System.load(tempLibPath + File.separator + "libthosttraderapi_se.so");
				System.load(tempLibPath + File.separator + "libjctpv6v3v15x64api.so");
			}
		} catch (Exception e) {
			logger.warn("加载运行库失败", e);
		}
	}
	

	private MdSpi mdSpi = null;
	private TdSpi tdSpi = null;
	
	public Map<String,ContractField> contractMap = new HashMap<>();


	public CtpGatewayImpl(FastEventService fastEventService, GatewaySettingField gatewaySetting) {
		super(fastEventService, gatewaySetting);
		
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_ONLY) {
			tdSpi = new TdSpi(this);
		}else if (gatewaySetting.getGatewayType() == GatewayTypeEnum.MARKET_DATA) {
			mdSpi = new MdSpi(this);
		} else {
			mdSpi = new MdSpi(this);
			tdSpi = new TdSpi(this);
		}
	}

	@Override
	public boolean subscribe(ContractField contractField) {
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.MARKET_DATA || gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_AND_MARKET_DATA ) {
			if(mdSpi == null) {
				logger.error(getLogInfo() + "行情接口尚未初始化或已断开");
				return false;
			}else {
				// 如果网关类型仅为行情,那就无法通过交易接口拿到合约信息，以订阅时的合约信息为准
				if(gatewaySetting.getGatewayType() == GatewayTypeEnum.MARKET_DATA) {
					contractMap.put(contractField.getSymbol(), contractField);
				}
				
				return mdSpi.subscribe(contractField.getSymbol());
			}
		}else {
			logger.warn(getLogInfo() + "不包含订阅功能");
			return false;
		}
	}

	@Override
	public boolean unsubscribe(ContractField contractField) {
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.MARKET_DATA || gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_AND_MARKET_DATA ) {
			if(mdSpi == null) {
				logger.error(getLogInfo() + "行情接口尚未初始化或已断开");
				return false;
			}else {
				return mdSpi.unsubscribe(contractField.getSymbol());
			}
		}else {
			logger.warn(getLogInfo() + "不包含取消订阅功能");
			return false;
		}
	}

	@Override
	public String submitOrder(SubmitOrderReqField submitOrderReq) {
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_ONLY || gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_AND_MARKET_DATA ) {
			if(tdSpi == null || !tdSpi.isConnected()) {
				logger.error(getLogInfo() + "交易接口尚未初始化或已断开");
				return "";
			}else {
				return tdSpi.submitOrder(submitOrderReq);
			}
		}else {
			logger.warn(getLogInfo() + "不包含提交定单功能");
			return "";
		}
	}

	@Override
	public boolean cancelOrder(CancelOrderReqField cancelOrderReq) {
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_ONLY || gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_AND_MARKET_DATA ) {
			if(tdSpi == null || !tdSpi.isConnected()) {
				logger.error(getLogInfo() + "交易接口尚未初始化或已断开");
				return false;
			}else {
				return tdSpi.cancelOrder(cancelOrderReq);
			}
		}else {
			logger.warn(getLogInfo() + "不包含撤销定单功能");
			return false;
		}
	}

	@Override
	public void searchContract(ContractField contract) {
		logger.warn(getLogInfo() + "不包含查询合约功能");
	}

	@Override
	public void disconnect() {
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_ONLY && tdSpi!=null) {
			tdSpi.disconnect();
		}else if (gatewaySetting.getGatewayType() == GatewayTypeEnum.MARKET_DATA && mdSpi!=null) {
			mdSpi.disconnect();
		} else if(gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_AND_MARKET_DATA && tdSpi!=null&& mdSpi!=null){
			tdSpi.disconnect();
			mdSpi.disconnect();
		}else {
			logger.error(getLogInfo() +"检测到SPI实例为空");
		}
	}

	@Override
	public void connect() {
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_ONLY && tdSpi!=null) {
			tdSpi.connect();		
		}else if (gatewaySetting.getGatewayType() == GatewayTypeEnum.MARKET_DATA && mdSpi!=null) {
			mdSpi.connect();
		} else if(gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_AND_MARKET_DATA && tdSpi!=null&& mdSpi!=null){
			tdSpi.connect();
			mdSpi.connect();
		}else {
			logger.error(getLogInfo() +"检测到SPI实例为空");
		}
	}

	@Override
	public boolean isConnected() {
		
		if (gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_ONLY && tdSpi!=null) {
			return tdSpi.isConnected();
		}else if (gatewaySetting.getGatewayType() == GatewayTypeEnum.MARKET_DATA && mdSpi!=null) {
			return mdSpi.isConnected();
		} else if(gatewaySetting.getGatewayType() == GatewayTypeEnum.TRADE_AND_MARKET_DATA && tdSpi!=null&& mdSpi!=null){
			return tdSpi.isConnected() && mdSpi.isConnected();
		}else {
			logger.error(getLogInfo() +"检测到SPI实例为空");
		}
		return false;
	}


}
