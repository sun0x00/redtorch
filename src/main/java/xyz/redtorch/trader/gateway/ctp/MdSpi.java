package xyz.redtorch.trader.gateway.ctp;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.api.jctp.CThostFtdcDepthMarketDataField;
import xyz.redtorch.api.jctp.CThostFtdcForQuoteRspField;
import xyz.redtorch.api.jctp.CThostFtdcMdApi;
import xyz.redtorch.api.jctp.CThostFtdcMdSpi;
import xyz.redtorch.api.jctp.CThostFtdcReqUserLoginField;
import xyz.redtorch.api.jctp.CThostFtdcRspInfoField;
import xyz.redtorch.api.jctp.CThostFtdcRspUserLoginField;
import xyz.redtorch.api.jctp.CThostFtdcSpecificInstrumentField;
import xyz.redtorch.api.jctp.CThostFtdcUserLogoutField;
import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.entity.Tick;

/**
 * @author sun0x00@gmail.com
 */
public class MdSpi extends CThostFtdcMdSpi {
	
	Logger log = LoggerFactory.getLogger(MdSpi.class);
	
	private CtpGateway ctpGateway;
	private String mdAddress;
	//private String tdAddress;
	private String brokerID;
	private String userID;
	private String password;
	//private String autoCode;
	//private String userProductInfo;
	private String gatewayLogInfo;
	private String gatewayID;
	//private String gatewayDisplayName;
	
	private String tradingDay;
	
	private HashMap<String, String> contractExchangeMap;
	//private HashMap<String, Integer> contractSizeMap;
	
	MdSpi(CtpGateway ctpGateway){
		
		this.ctpGateway = ctpGateway;
		this.ctpGateway = ctpGateway;
		this.mdAddress = ctpGateway.getGatewaySetting().getMdAddress();
		//this.tdAddress = ctpGateway.getGatewaySetting().getTdAddress();
		this.brokerID = ctpGateway.getGatewaySetting().getBrokerID();
		this.userID = ctpGateway.getGatewaySetting().getUserID();
		this.password = ctpGateway.getGatewaySetting().getPassword();
		//this.autoCode = ctpGateway.getGatewaySetting().getAuthCode();
		this.gatewayLogInfo = ctpGateway.getGatewayLogInfo();
		this.gatewayID = ctpGateway.getGatewayID();
		//this.gatewayDisplayName = ctpGateway.getGatewayDisplayName();
		
		this.contractExchangeMap = ctpGateway.getContractExchangeMap();
		//this.contractSizeMap = ctpGateway.getContractSizeMap();
		
	}
	
	private CThostFtdcMdApi cThostFtdcMdApi;
	
	private boolean connectProcessStatus = false;  // 避免重复调用
	private boolean connectionStatus = false;  // 前置机连接状态
	private boolean loginStatus = false;  // 登陆状态

	/**
	 * 连接
	 */
	public synchronized void connect() {
		String logContent;
		if (isConnected() || connectProcessStatus) {
			return;
		}

		if (connectionStatus) {
			login();
			return;
		}
		if (cThostFtdcMdApi != null) {
			cThostFtdcMdApi.Release();
			cThostFtdcMdApi.delete();
			connectionStatus = false;
			loginStatus = false;

		}
		String envTmpDir = System.getProperty("java.io.tmpdir");
		String tempFilePath = envTmpDir + File.separator 
				+ "xyz"+ File.separator 
				+"redtorch"+ File.separator 
				+"api"+ File.separator 
				+"jctp"+ File.separator 
				+"TEMP_CTP"+File.separator 
				+ "MD_" + ctpGateway.getGatewayID()+"_";
		File tempFile = new File(tempFilePath);
		if(!tempFile.getParentFile().exists()) {
			try {
				FileUtils.forceMkdirParent(tempFile);
				logContent = gatewayLogInfo + "创建临时文件夹"+tempFile.getParentFile().getAbsolutePath();
				ctpGateway.emitInfoLog(logContent);
				log.info(logContent);
			} catch (IOException e) {
				logContent = gatewayLogInfo + "创建临时文件夹失败"+tempFile.getParentFile().getAbsolutePath();
				ctpGateway.emitErrorLog(logContent);
				log.error(gatewayLogInfo);
			}
		}
		logContent = gatewayLogInfo + "使用临时文件夹"+tempFile.getParentFile().getAbsolutePath();
		ctpGateway.emitInfoLog(logContent);
		log.info(logContent);
		
		cThostFtdcMdApi = CThostFtdcMdApi.CreateFtdcMdApi(tempFile.getAbsolutePath());
		cThostFtdcMdApi.RegisterSpi(this);
		cThostFtdcMdApi.RegisterFront(mdAddress);
		cThostFtdcMdApi.Init();
		connectProcessStatus = true;
		
	}

	/**
	 * 关闭
	 */
	public synchronized void close() {
		if (!isConnected()) {
			return;
		}

		if (cThostFtdcMdApi != null) {
			cThostFtdcMdApi.Release();
			cThostFtdcMdApi.delete();
			connectionStatus = false;
			loginStatus = false;
			connectProcessStatus = false;
		}

	}

	/**
	 * 返回接口状态
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connectionStatus && loginStatus;
	}
	
	/**
	 * 获取交易日
	 * @return
	 */
	public String getTradingDay() {
		return tradingDay;
	}
	
	/**
	 * 订阅行情
	 * 
	 * @param rtSymbol
	 */
	public void subscribe(String symbol) {
		if(isConnected()) {
			String[] symbolArray = new String[1];
			symbolArray[0] = symbol;
			cThostFtdcMdApi.SubscribeMarketData(symbolArray, 1);
		}else {
			String logContent = gatewayLogInfo + "无法订阅行情,行情服务器尚未连接成功";
			log.warn(logContent);
			ctpGateway.emitWarnLog(logContent);
		}
	}

	/**
	 * 退订行情
	 */
	public void unSubscribe(String rtSymbol) {
		if(isConnected()) {
			String[] rtSymbolArray = new String[1];
			rtSymbolArray[0] = rtSymbol;
			cThostFtdcMdApi.UnSubscribeMarketData(rtSymbolArray, 1);
		}else {
			String logContent = gatewayLogInfo + "退订无效,行情服务器尚未连接成功";
			log.warn(logContent);
			ctpGateway.emitWarnLog(logContent);
		}
	}

	private void login() {
		if(StringUtils.isEmpty(brokerID)
				||StringUtils.isEmpty(userID)
				||StringUtils.isEmpty(password)) {
			String logContent = gatewayLogInfo+"BrokerID UserID Password不允许为空";
			log.error(logContent);
			ctpGateway.emitErrorLog("logContent");
			return;
		}
		// 登录
		CThostFtdcReqUserLoginField userLoginField = new CThostFtdcReqUserLoginField();
		userLoginField.setBrokerID(brokerID);
		userLoginField.setUserID(userID);
		userLoginField.setPassword(password);
		cThostFtdcMdApi.ReqUserLogin(userLoginField, 0);
	}
	
	// 前置机联机回报
	public void OnFrontConnected() {
		String logContent = gatewayLogInfo +"行情接口前置机已连接";
		log.info(logContent );
		ctpGateway.emitInfoLog(logContent);
		// 修改前置机连接状态为true
		connectionStatus = true;
		connectProcessStatus = false;
		login();
	}

	// 前置机断开回报
	public void OnFrontDisconnected(int nReason) {
		String logContent = gatewayLogInfo+"行情接口前置机已断开,Reason:"+nReason;
		log.info(logContent);
		ctpGateway.emitInfoLog(logContent);
		this.connectionStatus = false;
	}

	// 登录回报
	public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() == 0) {
			log.info("{}OnRspUserLogin! TradingDay:{},SessionID:{},BrokerID:{},UserID:{}", gatewayLogInfo,
					pRspUserLogin.getTradingDay(), pRspUserLogin.getSessionID(), pRspUserLogin.getBrokerID(),
					pRspUserLogin.getUserID());
			// 修改登录状态为true
			this.loginStatus = true;
			tradingDay = pRspUserLogin.getTradingDay();
			log.info("{}获取到的交易日为{}", gatewayLogInfo,tradingDay);
			// 重新订阅之前的合约
			if (!ctpGateway.getSubscribedSymbols().isEmpty()) {
				String[] subscribedSymbolsArray = ctpGateway.getSubscribedSymbols().toArray(new String[ctpGateway.getSubscribedSymbols().size()]);
				cThostFtdcMdApi.SubscribeMarketData(subscribedSymbolsArray, subscribedSymbolsArray.length + 1);
			}
		} else {
			log.warn("{}行情接口登录回报错误! ErrorID:{},ErrorMsg:{}", gatewayLogInfo, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		}

	}

	// 心跳警告
	public void OnHeartBeatWarning(int nTimeLapse) {
		String logContent = gatewayLogInfo + "行情接口心跳警告 nTimeLapse:" + nTimeLapse;
		log.warn(logContent);
		ctpGateway.emitWarnLog(logContent);
	}

	// 登出回报
	public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() != 0) {
			log.info("{}OnRspUserLogout!ErrorID:{},ErrorMsg:{}", gatewayLogInfo, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		} else {
			log.info("{}OnRspUserLogout!BrokerID:{},UserID:{}", gatewayLogInfo, pUserLogout.getBrokerID(),
					pUserLogout.getUserID());

		}
		this.loginStatus = false;
	}

	// 错误回报
	public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		String logContent = MessageFormat.format("{0}行情接口错误回报!ErrorID:{1},ErrorMsg:{2},RequestID:{3},isLast{4}",gatewayLogInfo, pRspInfo.getErrorID(),
				pRspInfo.getErrorMsg(), nRequestID, bIsLast);
		ctpGateway.emitErrorLog(logContent);
		log.info(logContent);
	}

	// 订阅合约回报
	public void OnRspSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() != 0) {
			log.info("{}OnRspSubMarketData! 订阅合约成功:{}", gatewayLogInfo, pSpecificInstrument.getInstrumentID());
		} else {
			log.warn("{}OnRspSubMarketData! ErrorID:{},ErrorMsg:{}", gatewayLogInfo, pRspInfo.getErrorID(),
					pRspInfo.getErrorMsg());
		}
	}

	// 退订合约回报
	public void OnRspUnSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo.getErrorID() != 0) {
			String logContent = gatewayLogInfo + "OnRspUnSubMarketData! 退订合约成功:"+pSpecificInstrument.getInstrumentID();
			ctpGateway.emitInfoLog(logContent);
			log.info(logContent);
		} else {
			String logContent = gatewayLogInfo + "OnRspUnSubMarketData! 退订合约失败,ErrorID："+pRspInfo.getErrorID()+"ErrorMsg:"+pRspInfo.getErrorMsg();
			ctpGateway.emitWarnLog(logContent);
			log.warn(logContent);
		}
	}

	// 合约行情推送
	public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
		if (pDepthMarketData != null) {
			
			//T2T Test
			//System.out.println("T2T-Tick-"+System.nanoTime());
			
			String symbol = pDepthMarketData.getInstrumentID();
			
			if(!contractExchangeMap.containsKey(symbol)) {
				String logContent = gatewayLogInfo + "收到合约"+symbol+"行情,但尚未获取到交易所信息,丢弃";
				ctpGateway.emitInfoLog(logContent);
				log.info(logContent);
			}
			
			Tick tick = new Tick();
			tick.setTradingDay(tradingDay);
			tick.setGatewayID(gatewayID);
			tick.setSymbol(symbol);
			tick.setExchange(contractExchangeMap.get(symbol));
			tick.setRtSymbol(symbol+"."+tick.getExchange());
			
			tick.setLastPrice(pDepthMarketData.getLastPrice());
			tick.setVolume(pDepthMarketData.getVolume());
			tick.setOpenInterest(pDepthMarketData.getOpenInterest());

			// 上期所 郑商所正常,大商所错误
			//TODO 大商所时间修正
			tick.setActionDay(pDepthMarketData.getActionDay());
			Long updateTime = Long.valueOf(pDepthMarketData.getUpdateTime().replaceAll(":",""));
			Long updateMillisec = (long)pDepthMarketData.getUpdateMillisec();
			Long actionDay = Long.valueOf(tick.getActionDay());
			
			String updateDateTimeWithMS = (actionDay*100*100*100*1000+updateTime*1000+updateMillisec)+"";
			
			DateTime dateTime;
			try {
				dateTime = RtConstant.DT_FORMAT_WITH_MS_INT_Formatter.parseDateTime(updateDateTimeWithMS);
			}catch(Exception e) {
				log.error("{}解析日期发生异常",gatewayLogInfo,e);
				return;
			}
			tick.setActionTime(dateTime.toString(RtConstant.T_FORMAT_WITH_MS_INT_Formatter));
			
			tick.setDateTime(dateTime);
			
			tick.setOpenPrice(pDepthMarketData.getOpenPrice());
			tick.setHighPrice(pDepthMarketData.getHighestPrice());
			tick.setLowPrice(pDepthMarketData.getLowestPrice());
			tick.setPreClosePrice(pDepthMarketData.getPreClosePrice());
			
			tick.setUpperLimit(pDepthMarketData.getUpperLimitPrice());
			tick.setLowerLimit(pDepthMarketData.getLowerLimitPrice());
			
			tick.setPreSettlePrice(pDepthMarketData.getPreSettlementPrice());
			
			tick.setAskPrice1(pDepthMarketData.getAskPrice1());
			tick.setAskVolume1(pDepthMarketData.getAskVolume1());
			tick.setBidPrice1(pDepthMarketData.getBidPrice1());
			tick.setBidVolume1(pDepthMarketData.getBidVolume1());
			
			
			ctpGateway.emitTick(tick);
			
		} else {
			log.warn("{}OnRtnDepthMarketData! 收到行情信息为空", gatewayLogInfo);
		}
	}

	// 订阅期权询价
	public void OnRspSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}OnRspSubForQuoteRsp!", gatewayLogInfo);
	}

	// 退订期权询价
	public void OnRspUnSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		log.info("{}OnRspUnSubForQuoteRsp!", gatewayLogInfo);
	}

	// 期权询价推送
	public void OnRtnForQuoteRsp(CThostFtdcForQuoteRspField pForQuoteRsp) {
		log.info("{}OnRspUnSubForQuoteRsp!", gatewayLogInfo);
	}

}