package xyz.redtorch.gateway.ctp.x64v6v3v15v;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcDepthMarketDataField;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcForQuoteRspField;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcMdApi;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcMdSpi;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcReqUserLoginField;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcRspInfoField;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcRspUserLoginField;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcSpecificInstrumentField;
import xyz.redtorch.gateway.ctp.x64v6v3v15v.api.CThostFtdcUserLogoutField;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.TickField;

public class MdSpi extends CThostFtdcMdSpi {

	private final static int CONNECTION_STATUS_DISCONNECTED = 0;
	private final static int CONNECTION_STATUS_CONNECTED = 1;
	private final static int CONNECTION_STATUS_CONNECTING = 2;
	private final static int CONNECTION_STATUS_DISCONNECTING = 3;

	private static final Logger logger = LoggerFactory.getLogger(MdSpi.class);

	private CtpGatewayImpl ctpGatewayImpl;
	private String mdHost;
	private String mdPort;
	private String brokerId;
	private String userId;
	private String password;
	private String logInfo;
	private String gatewayId;
	private String tradingDay;

	private Map<String, TickField> preTickMap = new HashMap<>();

	private Set<String> subscribedSymbolSet = ConcurrentHashMap.newKeySet();

	MdSpi(CtpGatewayImpl ctpGatewayImpl) {
		this.ctpGatewayImpl = ctpGatewayImpl;
		this.mdHost = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getMdHost();
		this.mdPort = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getMdPort();
		this.brokerId = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getBrokerId();
		this.userId = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getUserId();
		this.password = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getPassword();
		this.logInfo = ctpGatewayImpl.getLogInfo();
		this.gatewayId = ctpGatewayImpl.getGatewayId();
	}

	private CThostFtdcMdApi cThostFtdcMdApi;

	private int connectionStatus = CONNECTION_STATUS_DISCONNECTED; // 避免重复调用
	private boolean loginStatus = false; // 登陆状态

	public void connect() {
		if (isConnected() || connectionStatus == CONNECTION_STATUS_CONNECTING) {
			return;
		}

		if (connectionStatus == CONNECTION_STATUS_CONNECTED) {
			login();
			return;
		}

		connectionStatus = CONNECTION_STATUS_CONNECTING;
		loginStatus = false;

		if (cThostFtdcMdApi != null) {
			try {
				logger.warn("{}行情接口检测到旧实例,准备释放", logInfo);
				CThostFtdcMdApi cThostFtdcMdApiForRelease = cThostFtdcMdApi;
				cThostFtdcMdApi = null;
				cThostFtdcMdApiForRelease.RegisterSpi(null);

				new Thread() {
					public void run() {
						Thread.currentThread().setName("GatewayId [" + gatewayId + "] MD API Release Thread, Start Time " + System.currentTimeMillis());
						try {
							logger.warn("行情接口异步释放启动！");
							cThostFtdcMdApiForRelease.Release();
							logger.warn("行情接口异步释放完成！");
						} catch (Throwable t) {
							logger.error("行情接口异步释放发生异常！", t);
						}
					}
				}.start();

				Thread.sleep(100);
			} catch (Throwable t) {
				logger.warn("{}交易接口连接前释放异常", logInfo, t);
			}
		}

		logger.warn("{}行情接口实例初始化", logInfo);

		String envTmpDir = System.getProperty("java.io.tmpdir");
		String tempFilePath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator + "gateway" + File.separator + "ctp" + File.separator + "jctpv6v3v15x64api"
				+ File.separator + "CTP_FLOW_TEMP" + File.separator + "MD_" + ctpGatewayImpl.getGatewayId();
		File tempFile = new File(tempFilePath);
		if (!tempFile.getParentFile().exists()) {
			try {
				FileUtils.forceMkdirParent(tempFile);
				logger.info("{}行情接口创建临时文件夹:{}", logInfo, tempFile.getParentFile().getAbsolutePath());
			} catch (IOException e) {
				logger.error("{}行情接口创建临时文件夹失败", logInfo, e);
			}
		}

		logger.warn("{}行情接口使用临时文件夹:{}", logInfo, tempFile.getParentFile().getAbsolutePath());

		try {
			cThostFtdcMdApi = CThostFtdcMdApi.CreateFtdcMdApi(tempFile.getAbsolutePath());
			cThostFtdcMdApi.RegisterSpi(this);
			cThostFtdcMdApi.RegisterFront("tcp://" + mdHost + ":" + mdPort);
			cThostFtdcMdApi.Init();
		} catch (Throwable t) {
			logger.error("{}行情接口连接异常", logInfo, t);
		}

		new Thread() {
			public void run() {
				try {
					Thread.sleep(15 * 1000);
					if (!isConnected()) {
						logger.error("{}行情接口连接超时,尝试断开", logInfo);
						ctpGatewayImpl.disconnect();
					}

				} catch (Throwable t) {
					logger.error("{}行情接口处理连接超时线程异常", logInfo, t);
				}
			}

		}.start();

	}

	// 关闭
	public void disconnect() {
		if (cThostFtdcMdApi != null && connectionStatus != CONNECTION_STATUS_DISCONNECTING) {
			logger.warn("{}行情接口实例开始关闭并释放", logInfo);
			loginStatus = false;
			connectionStatus = CONNECTION_STATUS_DISCONNECTING;
			if (cThostFtdcMdApi != null) {
				try {
					CThostFtdcMdApi cThostFtdcMdApiForRelease = cThostFtdcMdApi;
					cThostFtdcMdApi = null;
					cThostFtdcMdApiForRelease.RegisterSpi(null);
					new Thread() {
						public void run() {
							Thread.currentThread().setName("GatewayId " + gatewayId + " MD API Release Thread, Time " + System.currentTimeMillis());
							try {
								logger.warn("行情接口异步释放启动！");
								cThostFtdcMdApiForRelease.Release();
								logger.warn("行情接口异步释放完成！");
							} catch (Throwable t) {
								logger.error("行情接口异步释放发生异常", t);
							}
						}
					}.start();
					Thread.sleep(100);
				} catch (Throwable t) {
					logger.error("{}行情接口实例关闭并释放异常", logInfo, t);
				}
			}
			connectionStatus = CONNECTION_STATUS_DISCONNECTED;
			logger.warn("{}行情接口实例关闭并释放", logInfo);
		} else {
			logger.warn("{}行情接口实例不存在,无需关闭释放", logInfo);
		}

	}

	// 返回接口状态
	public boolean isConnected() {
		return connectionStatus == CONNECTION_STATUS_CONNECTED && loginStatus;
	}

	// 获取交易日
	public String getTradingDay() {
		return tradingDay;
	}

	// 订阅行情
	public boolean subscribe(String symbol) {
		subscribedSymbolSet.add(symbol);
		if (isConnected()) {
			String[] symbolArray = new String[1];
			symbolArray[0] = symbol;
			try {
				cThostFtdcMdApi.SubscribeMarketData(symbolArray, 1);
			} catch (Throwable t) {
				logger.error("{}订阅行情异常,合约代码{}", logInfo, symbol, t);
				return false;
			}
			return true;
		} else {
			logger.warn("{}无法订阅行情,行情服务器尚未连接成功,合约代码:{}", logInfo, symbol);
			return false;
		}
	}

	// 退订行情
	public boolean unsubscribe(String symbol) {
		subscribedSymbolSet.remove(symbol);
		if (isConnected()) {
			String[] symbolArray = new String[1];
			symbolArray[0] = symbol;
			symbolArray[0] = symbol;
			try {
				cThostFtdcMdApi.UnSubscribeMarketData(symbolArray, 1);
			} catch (Throwable t) {
				logger.error("{}行情退订异常,合约代码{}", logInfo, symbol, t);
				return false;
			}
			return true;
		} else {
			logger.warn("{}行情退订无效,行情服务器尚未连接成功,合约代码:{}", logInfo, symbol);
			return false;
		}
	}

	private void login() {
		if (StringUtils.isEmpty(brokerId) || StringUtils.isEmpty(userId) || StringUtils.isEmpty(password)) {
			logger.error("{}BrokerId UserID Password 不可为空", logInfo);
			return;
		}
		try {
			// 登录
			CThostFtdcReqUserLoginField userLoginField = new CThostFtdcReqUserLoginField();
			userLoginField.setBrokerID(brokerId);
			userLoginField.setUserID(userId);
			userLoginField.setPassword(password);
			cThostFtdcMdApi.ReqUserLogin(userLoginField, 0);
		} catch (Throwable t) {
			logger.error("{}登录异常", logInfo, t);
		}

	}

	// 前置机联机回报
	public void OnFrontConnected() {
		try {
			logger.warn(logInfo + "行情接口前置机已连接");
			// 修改前置机连接状态
			connectionStatus = CONNECTION_STATUS_CONNECTED;
			login();
		} catch (Throwable t) {
			logger.error("{} OnFrontConnected Exception", logInfo, t);
		}
	}

	// 前置机断开回报
	public void OnFrontDisconnected(int nReason) {
		try {
			logger.warn("{}行情接口前置机已断开, 原因:{}", logInfo, nReason);
			ctpGatewayImpl.disconnect();
		} catch (Throwable t) {
			logger.error("{} OnFrontDisconnected Exception", logInfo, t);
		}
	}

	// 登录回报
	public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		try {
			if (pRspInfo.getErrorID() == 0) {
				logger.info("{}OnRspUserLogin TradingDay:{},SessionID:{},BrokerId:{},UserID:{}", logInfo, pRspUserLogin.getTradingDay(), pRspUserLogin.getSessionID(), pRspUserLogin.getBrokerID(),
						pRspUserLogin.getUserID());
				// 修改登录状态为true
				this.loginStatus = true;
				tradingDay = pRspUserLogin.getTradingDay();
				logger.warn("{}行情接口获取到的交易日为{}", logInfo, tradingDay);

				if (!subscribedSymbolSet.isEmpty()) {
					String[] symbolArray = subscribedSymbolSet.toArray(new String[subscribedSymbolSet.size()]);
					cThostFtdcMdApi.SubscribeMarketData(symbolArray, subscribedSymbolSet.size());
				}
			} else {
				logger.warn("{}行情接口登录回报错误 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
				// 不合法的登录
				if (pRspInfo.getErrorID() == 3) {
					ctpGatewayImpl.setAuthErrorFlag(true);
				}
			}

		} catch (Throwable t) {
			logger.error("{} OnRspUserLogin Exception", logInfo, t);
		}

	}

	// 心跳警告
	public void OnHeartBeatWarning(int nTimeLapse) {
		logger.warn(logInfo + "行情接口心跳警告 nTimeLapse:" + nTimeLapse);
	}

	// 登出回报
	public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		try {

			if (pRspInfo.getErrorID() != 0) {
				logger.error("{}OnRspUserLogout!错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
			} else {
				logger.warn("{}OnRspUserLogout!BrokerId:{},UserID:{}", logInfo, pUserLogout.getBrokerID(), pUserLogout.getUserID());

			}

		} catch (Throwable t) {
			logger.error("{} OnRspUserLogout Exception", logInfo, t);
		}

		this.loginStatus = false;
	}

	// 错误回报
	public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo != null) {
			logger.error("{}行情接口错误回报!错误ID:{},错误信息:{},请求ID:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg(), nRequestID);
		} else {
			logger.error("{}行情接口错误回报!不存在错误回报信息", logInfo);
		}
	}

	// 订阅合约回报
	public void OnRspSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo != null) {
			if (pRspInfo.getErrorID() == 0) {
				if (pSpecificInstrument != null) {
					logger.info("{}行情接口订阅合约成功:{}", logInfo, pSpecificInstrument.getInstrumentID());
				} else {
					logger.error("{}行情接口订阅合约成功,不存在合约信息", logInfo);
				}
			} else {
				logger.error("{}行情接口订阅合约失败,错误ID:{} 错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
			}
		} else {
			logger.info("{}行情接口订阅回报，不存在回报信息", logInfo);
		}
	}

	// 退订合约回报
	public void OnRspUnSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		if (pRspInfo != null) {
			if (pRspInfo.getErrorID() == 0) {
				if (pSpecificInstrument != null) {
					logger.info("{}行情接口退订合约成功:{}", logInfo, pSpecificInstrument.getInstrumentID());
				} else {
					logger.error("{}行情接口退订合约成功,不存在合约信息", logInfo);
				}
			} else {
				logger.error("{}行情接口退订合约失败,错误ID:{} 错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
			}
		} else {
			logger.info("{}行情接口退订回报，不存在回报信息", logInfo);
		}
	}

	// 合约行情推送
	public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
		if (pDepthMarketData != null) {
			try {
				String symbol = pDepthMarketData.getInstrumentID();

				if (!ctpGatewayImpl.contractMap.containsKey(symbol)) {
					logger.warn("{}行情接口收到合约{}数据,但尚未获取到合约信息,丢弃", logInfo, symbol);
					return;
				}

				ContractField contract = ctpGatewayImpl.contractMap.get(symbol);

				String actionDay = pDepthMarketData.getActionDay();

				Long updateTime = Long.valueOf(pDepthMarketData.getUpdateTime().replaceAll(":", ""));
				Long updateMillisec = (long) pDepthMarketData.getUpdateMillisec();
				/*
				 * 大商所获取的ActionDay可能是不正确的,因此这里采用本地时间修正 1.请注意，本地时间应该准确 2.使用 SimNow 7x24
				 * 服务器获取行情时,这个修正方式可能会导致问题
				 */
				if (contract.getExchange() == ExchangeEnum.DCE) {
					// 只修正夜盘
					if (updateTime > 200000 && updateTime <= 235959) {
						actionDay = LocalDateTime.now().format(CommonConstant.D_FORMAT_INT_FORMATTER);
					}
				}

				Long actionDayLong = Long.valueOf(actionDay);

				String updateDateTimeWithMS = (actionDayLong * 1000000 * 1000 + updateTime * 1000 + updateMillisec) + "";

				LocalDateTime dateTime;
				try {
					dateTime = LocalDateTime.parse(updateDateTimeWithMS, CommonConstant.DT_FORMAT_WITH_MS_INT_FORMATTER);
				} catch (Exception e) {
					logger.error("{}解析日期发生异常", logInfo, e);
					return;
				}

				String contractId = contract.getContractId();
				String actionTime = dateTime.format(CommonConstant.T_FORMAT_WITH_MS_INT_FORMATTER);
				double lastPrice = pDepthMarketData.getLastPrice();
				int volume = pDepthMarketData.getVolume();
				int volumeDelta = 0;
				if (preTickMap.containsKey(contractId)) {
					volumeDelta = (int) (volume - preTickMap.get(contractId).getVolume());
				}

				Double turnover = pDepthMarketData.getTurnover();
				double turnoverDelta = 0;
				if (preTickMap.containsKey(contractId)) {
					turnoverDelta = turnover - preTickMap.get(contractId).getTurnover();
				}

				Long preOpenInterest = (long) pDepthMarketData.getPreOpenInterest();

				double openInterest = pDepthMarketData.getOpenInterest();
				int openInterestDelta = 0;
				if (preTickMap.containsKey(contractId)) {
					openInterestDelta = (int) (openInterest - preTickMap.get(contractId).getOpenInterestDelta());
				}

				Double preClosePrice = pDepthMarketData.getPreClosePrice();
				Double preSettlePrice = pDepthMarketData.getPreSettlementPrice();
				Double openPrice = pDepthMarketData.getOpenPrice();
				Double highPrice = pDepthMarketData.getHighestPrice();
				Double lowPrice = pDepthMarketData.getLowestPrice();
				Double upperLimit = pDepthMarketData.getUpperLimitPrice();
				Double lowerLimit = pDepthMarketData.getLowerLimitPrice();

				List<Double> bidPriceList = new ArrayList<>();
				bidPriceList.add(pDepthMarketData.getBidPrice1());
				bidPriceList.add(pDepthMarketData.getBidPrice2());
				bidPriceList.add(pDepthMarketData.getBidPrice3());
				bidPriceList.add(pDepthMarketData.getBidPrice4());
				bidPriceList.add(pDepthMarketData.getBidPrice5());
				List<Integer> bidVolumeList = new ArrayList<>();
				bidVolumeList.add(pDepthMarketData.getBidVolume1());
				bidVolumeList.add(pDepthMarketData.getBidVolume2());
				bidVolumeList.add(pDepthMarketData.getBidVolume3());
				bidVolumeList.add(pDepthMarketData.getBidVolume4());
				bidVolumeList.add(pDepthMarketData.getBidVolume5());

				List<Double> askPriceList = new ArrayList<>();
				askPriceList.add(pDepthMarketData.getAskPrice1());
				askPriceList.add(pDepthMarketData.getAskPrice2());
				askPriceList.add(pDepthMarketData.getAskPrice3());
				askPriceList.add(pDepthMarketData.getAskPrice4());
				askPriceList.add(pDepthMarketData.getAskPrice5());
				List<Integer> askVolumeList = new ArrayList<>();
				askVolumeList.add(pDepthMarketData.getAskVolume1());
				askVolumeList.add(pDepthMarketData.getAskVolume2());
				askVolumeList.add(pDepthMarketData.getAskVolume3());
				askVolumeList.add(pDepthMarketData.getAskVolume4());
				askVolumeList.add(pDepthMarketData.getAskVolume5());

				Double averagePrice = pDepthMarketData.getAveragePrice();
				Double settlePrice = pDepthMarketData.getSettlementPrice();

				TickField.Builder tickBuilder = TickField.newBuilder();
				ContractField.Builder contractBuilder = contract.toBuilder();
				contractBuilder.setContractId(contractId);
				tickBuilder.setUnifiedSymbol(contract.getUnifiedSymbol());
				tickBuilder.setActionDay(actionDay);
				tickBuilder.setActionTime(actionTime);
				tickBuilder.setActionTimestamp(CommonUtils.localDateTimeToMills(dateTime));
				tickBuilder.setAvgPrice(averagePrice);

				tickBuilder.setHighPrice(highPrice);
				tickBuilder.setLowPrice(lowPrice);
				tickBuilder.setOpenPrice(openPrice);
				tickBuilder.setLastPrice(lastPrice);

				tickBuilder.setSettlePrice(settlePrice);

				tickBuilder.setOpenInterest(openInterest);
				tickBuilder.setOpenInterestDelta(openInterestDelta);
				tickBuilder.setVolume(volume);
				tickBuilder.setVolumeDelta(volumeDelta);
				tickBuilder.setTurnover(turnover);
				tickBuilder.setTurnoverDelta(turnoverDelta);

				tickBuilder.setTradingDay(tradingDay);

				tickBuilder.setLowerLimit(lowerLimit);
				tickBuilder.setUpperLimit(upperLimit);

				tickBuilder.setPreClosePrice(preClosePrice);
				tickBuilder.setPreSettlePrice(preSettlePrice);
				tickBuilder.setPreOpenInterest(preOpenInterest);

				tickBuilder.addAllAskPrice(askPriceList);
				tickBuilder.addAllAskVolume(askVolumeList);
				tickBuilder.addAllBidPrice(bidPriceList);
				tickBuilder.addAllBidVolume(bidVolumeList);
				tickBuilder.setGatewayId(gatewayId);

				TickField tick = tickBuilder.build();

				preTickMap.put(contractId, tick);

				ctpGatewayImpl.emitTick(tick);
			} catch (Throwable t) {
				logger.error("{} OnRtnDepthMarketData Exception", logInfo, t);
			}

		} else {
			logger.warn("{}行情接口收到行情数据为空", logInfo);
		}
	}

	// 订阅期权询价
	public void OnRspSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		logger.info("{}OnRspSubForQuoteRsp", logInfo);
	}

	// 退订期权询价
	public void OnRspUnSubForQuoteRsp(CThostFtdcSpecificInstrumentField pSpecificInstrument, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		logger.info("{}OnRspUnSubForQuoteRsp", logInfo);
	}

	// 期权询价推送
	public void OnRtnForQuoteRsp(CThostFtdcForQuoteRspField pForQuoteRsp) {
		logger.info("{}OnRspUnSubForQuoteRsp", logInfo);
	}

}