package xyz.redtorch.gateway.ctp.x64v6v3v11v;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.gateway.ctp.x64v6v3v11v.api.*;
import xyz.redtorch.pb.CoreEnum.CommonStatusEnum;
import xyz.redtorch.pb.CoreEnum.CurrencyEnum;
import xyz.redtorch.pb.CoreEnum.DirectionEnum;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.OffsetEnum;
import xyz.redtorch.pb.CoreEnum.OptionTypeEnum;
import xyz.redtorch.pb.CoreEnum.OrderStatusEnum;
import xyz.redtorch.pb.CoreEnum.PriceTypeEnum;
import xyz.redtorch.pb.CoreEnum.ProductTypeEnum;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.NoticeField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;
import xyz.redtorch.pb.CoreField.TradeField;

/**
 * @author sun0x00@gmail.com
 */
public class TdSpi extends CThostFtdcTraderSpi {

	private static int CONNECTION_STATUS_DISCONNECTED = 0;
	private static int CONNECTION_STATUS_CONNECTED = 1;
	private static int CONNECTION_STATUS_CONNECTING = 2;
	private static int CONNECTION_STATUS_DISCONNECTING = 3;

	private final static Logger logger = LoggerFactory.getLogger(TdSpi.class);
	private CtpGatewayImpl ctpGatewayImpl;
	private String tdHost;
	private String tdPort;
	private String brokerId;
	private String userId;
	private String password;
	private String authCode;
	private String userProductInfo;
	private String logInfo;
	private String gatewayId;

	private String investorName = "";

	private HashMap<String, PositionField.Builder> positionBuilderMap = new HashMap<>();

	private Map<String, String> orderIdOriginalOrderIdMap = new ConcurrentHashMap<>();
	private Map<String, String> originalOrderIdOrderIdMap = new ConcurrentHashMap<>();
	private Map<String, String> orderIdAdapterOrderIdMap = new ConcurrentHashMap<>();
	private Map<String, OrderField> orderMap = new ConcurrentHashMap<>();
	private Map<String, SubmitOrderReqField> submitOrderReqMap = new ConcurrentHashMap<>();

	private Thread intervalQueryThread;

	TdSpi(CtpGatewayImpl ctpGatewayImpl) {

		this.ctpGatewayImpl = ctpGatewayImpl;
		this.tdHost = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getTdHost();
		this.tdPort = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getTdPort();
		this.brokerId = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getBrokerId();
		this.userId = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getUserId();
		this.password = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getPassword();
		this.authCode = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getAuthCode();
		this.userProductInfo = ctpGatewayImpl.getGatewaySetting().getCtpApiSetting().getUserProductInfo();
		this.logInfo = ctpGatewayImpl.getLogInfo();
		this.gatewayId = ctpGatewayImpl.getGatewayId();

	}

	private CThostFtdcTraderApi cThostFtdcTraderApi;

	private int connectionStatus = CONNECTION_STATUS_DISCONNECTED; // 避免重复调用
	private boolean loginStatus = false; // 登陆状态
	private String tradingDay;

	private boolean instrumentQueried = false;
	private boolean investorNameQueried = false;

	private AtomicInteger reqId = new AtomicInteger(0); // 操作请求编号
	private AtomicInteger orderRef = new AtomicInteger(0); // 订单编号

	private boolean loginFailed = false; // 是否已经使用错误的信息尝试登录过

	private int frontId = 0; // 前置机编号
	private int sessionId = 0; // 会话编号

	private List<OrderField.Builder> orderBuilderCacheList = new LinkedList<>(); // 登录起始阶段缓存Order
	private List<TradeField.Builder> tradeBuilderCacheList = new LinkedList<>(); // 登录起始阶段缓存Trade

	private void startIntervalQuery() {
		if (this.intervalQueryThread != null) {
			logger.error("{}定时查询线程已存在,首先终止", logInfo);
			stopQuery();
		}
		this.intervalQueryThread = new Thread() {
			public void run() {
				Thread.currentThread().setName("CTP Gateway Interval Query Thread, "+gatewayId+" " + System.currentTimeMillis());
				while (!Thread.currentThread().isInterrupted()) {
					try {
						if(cThostFtdcTraderApi == null) {
							logger.error("{}定时查询线程检测到API实例不存在,退出", logInfo);
							break;
						}
						queryAccount();
						Thread.sleep(1250);
						queryPosition();
						Thread.sleep(1250);
					} catch (InterruptedException e) {
						logger.warn("{}定时查询线程睡眠时检测到中断,退出线程", logInfo, e);
						break;
					} catch (Exception e) {
						logger.error("{}定时查询线程发生异常", logInfo, e);
					}
				}
			};
		};
		this.intervalQueryThread.start();

	}

	private void stopQuery() {
		try {
			if (intervalQueryThread != null && !intervalQueryThread.isInterrupted()) {
				intervalQueryThread.interrupt();
				intervalQueryThread = null;
			}
		} catch (Exception e) {
			logger.error(logInfo + "停止线程发生异常", e);
		}
	}

	public void connect() {
		if (isConnected() || connectionStatus == CONNECTION_STATUS_CONNECTING) {
			logger.warn("{}交易接口已经连接或正在连接，不再重复连接", logInfo);
			return;
		}

		if (connectionStatus == CONNECTION_STATUS_CONNECTED) {
			if(StringUtils.isBlank(userProductInfo)) {
				CThostFtdcReqUserLoginField reqUserLoginField = new CThostFtdcReqUserLoginField();
				reqUserLoginField.setBrokerID(this.brokerId);
				reqUserLoginField.setUserID(this.userId);
				reqUserLoginField.setPassword(this.password);
				cThostFtdcTraderApi.ReqUserLogin(reqUserLoginField, reqId.incrementAndGet());
			}else {
				reqAuth();
			}
			return;
		}

		connectionStatus = CONNECTION_STATUS_CONNECTING;
		loginStatus = false;
		instrumentQueried = false;
		investorNameQueried = false;

		try {

			if (cThostFtdcTraderApi != null) {
				try {
					cThostFtdcTraderApi.RegisterSpi(null);
					CThostFtdcTraderApi cThostFtdcTraderApiForRelease = cThostFtdcTraderApi;
					cThostFtdcTraderApi = null;

					new Thread() {
						public void run() {
							Thread.currentThread().setName("GatewayID " + gatewayId + " TD API Release Thread, Time "
									+ System.currentTimeMillis());

							try {
								logger.warn("交易接口异步释放启动！");
								cThostFtdcTraderApiForRelease.Release();
								logger.warn("交易接口异步释放完成！");
							} catch (Throwable t) {
								logger.error("交易接口异步释放发生异常！", t);
							}
						}
					}.start();

					Thread.sleep(100);
				} catch (Exception e) {
					logger.warn("{}交易接口连接前释放异常", logInfo, e);
				}

			}

			logger.warn("{}交易接口实例初始化", logInfo);
			String envTmpDir = System.getProperty("java.io.tmpdir");
			String tempFilePath = envTmpDir + File.separator + "xyz" + File.separator + "redtorch" + File.separator
					+ "api" + File.separator + "jctp" + File.separator + "TEMP_CTP" + File.separator + "TD_" + gatewayId
					+ "_";
			File tempFile = new File(tempFilePath);
			if (!tempFile.getParentFile().exists()) {
				try {
					FileUtils.forceMkdirParent(tempFile);
					logger.info("{}交易接口创建临时文件夹 {}", logInfo, tempFile.getParentFile().getAbsolutePath());
				} catch (IOException e) {
					logger.error("{}交易接口创建临时文件夹失败{}", logInfo, tempFile.getParentFile().getAbsolutePath(), e);
				}
			}
			logger.warn("{}交易接口使用临时文件夹{}", logInfo, tempFile.getParentFile().getAbsolutePath());
			cThostFtdcTraderApi = CThostFtdcTraderApi.CreateFtdcTraderApi(tempFile.getAbsolutePath());
			cThostFtdcTraderApi.RegisterSpi(this);
			cThostFtdcTraderApi.RegisterFront("tcp://" + tdHost + ":" + tdPort);
			cThostFtdcTraderApi.Init();
			new Thread() {
				public void run() {
					try {
						Thread.sleep(3 * 1000);
						if (!isConnected()) {
							logger.error("{}交易接口连接超时,尝试断开", logInfo);
							ctpGatewayImpl.disconnect();
						}
					} catch (Throwable t) {
						logger.error("{}交易接口处理连接超时线程异常", logInfo, t);
					}
				}

			}.start();

		} catch (Throwable t) {
			logger.error("{}交易接口连接异常", logInfo, t);
		}

	}

	public void disconnect() {
		try {
			this.stopQuery();
			if (cThostFtdcTraderApi != null && connectionStatus != CONNECTION_STATUS_DISCONNECTING) {
				logger.warn("{}交易接口实例开始关闭并释放", logInfo);
				loginStatus = false;
				instrumentQueried = false;
				investorNameQueried = false;
				connectionStatus = CONNECTION_STATUS_DISCONNECTING;
				try {
					if (cThostFtdcTraderApi != null) {
						cThostFtdcTraderApi.RegisterSpi(null);
						CThostFtdcTraderApi cThostFtdcTraderApiForRelease = cThostFtdcTraderApi;
						cThostFtdcTraderApi = null;

						new Thread() {
							public void run() {
								Thread.currentThread().setName("GatewayID " + gatewayId
										+ " TD API Release Thread,Start Time " + System.currentTimeMillis());

								try {
									logger.warn("交易接口异步释放启动！");
									cThostFtdcTraderApiForRelease.Release();
									logger.warn("交易接口异步释放完成！");
								} catch (Throwable t) {
									logger.error("交易接口异步释放发生异常！", t);
								}
							}
						}.start();

					}
					Thread.sleep(100);
				} catch (Exception e) {
					logger.error("{}交易接口实例关闭并释放异常", logInfo, e);
				}

				connectionStatus = CONNECTION_STATUS_DISCONNECTED;
				logger.warn("{} 交易接口实例关闭并异步释放", logInfo);
			} else {
				logger.warn("{} 交易接口实例不存在或正在关闭释放,无需操作", logInfo);
			}
		} catch (Throwable t) {
			logger.error("{} 交易接口实例关闭并释放异常", logInfo, t);
		}

	}

	public boolean isConnected() {
		return connectionStatus == CONNECTION_STATUS_CONNECTED && loginStatus;
	}

	public String getTradingDay() {
		return tradingDay;
	}

	public void queryAccount() {
		if (cThostFtdcTraderApi == null) {
			logger.warn("{}交易接口尚未初始化,无法查询账户", logInfo);
			return;
		}
		if (!loginStatus) {
			logger.warn("{}交易接口尚未登录,无法查询账户", logInfo);
			return;
		}
		if (!instrumentQueried) {
			logger.warn("{}交易接口尚未获取到合约信息,无法查询持仓", logInfo);
			return;
		}
		if (!investorNameQueried) {
			logger.warn("{}交易接口尚未获取到投资者姓名,无法查询持仓", logInfo);
			return;
		}
		try {
			CThostFtdcQryTradingAccountField cThostFtdcQryTradingAccountField = new CThostFtdcQryTradingAccountField();
			cThostFtdcTraderApi.ReqQryTradingAccount(cThostFtdcQryTradingAccountField, reqId.incrementAndGet());
		} catch (Throwable t) {
			logger.error("{} 交易接口查询账户异常", logInfo, t);
		}

	}

	public void queryPosition() {
		if (cThostFtdcTraderApi == null) {
			logger.warn("{}交易接口尚未初始化,无法查询持仓", logInfo);
			return;
		}
		if (!loginStatus) {
			logger.warn("{}交易接口尚未登录,无法查询持仓", logInfo);
			return;
		}

		if (!instrumentQueried) {
			logger.warn("{}交易接口尚未获取到合约信息,无法查询持仓", logInfo);
			return;
		}
		if (!investorNameQueried) {
			logger.warn("{}交易接口尚未获取到投资者姓名,无法查询持仓", logInfo);
			return;
		}

		try {
			CThostFtdcQryInvestorPositionField cThostFtdcQryInvestorPositionField = new CThostFtdcQryInvestorPositionField();
			// log.info("查询持仓");
			cThostFtdcQryInvestorPositionField.setBrokerID(brokerId);
			cThostFtdcQryInvestorPositionField.setInvestorID(userId);
			cThostFtdcTraderApi.ReqQryInvestorPosition(cThostFtdcQryInvestorPositionField, reqId.incrementAndGet());
		} catch (Throwable t) {
			logger.error("{} 交易接口查询持仓异常", logInfo, t);
		}

	}

	public String submitOrder(SubmitOrderReqField submitOrderReq) {
		if (cThostFtdcTraderApi == null) {
			logger.warn("{}交易接口尚未初始化,无法发单", logInfo);
			return null;
		}

		if (!loginStatus) {
			logger.warn("{}交易接口尚未登录,无法发单", logInfo);
			return null;
		}

		try {
			CThostFtdcInputOrderField cThostFtdcInputOrderField = new CThostFtdcInputOrderField();
			orderRef.incrementAndGet();
			cThostFtdcInputOrderField.setInstrumentID(submitOrderReq.getContract().getSymbol());
			cThostFtdcInputOrderField.setLimitPrice(submitOrderReq.getPrice());
			cThostFtdcInputOrderField.setVolumeTotalOriginal(submitOrderReq.getVolume());

			cThostFtdcInputOrderField.setOrderPriceType(
					CtpConstant.priceTypeMap.getOrDefault(submitOrderReq.getPriceType(), Character.valueOf('\0')));
			cThostFtdcInputOrderField.setDirection(
					CtpConstant.directionMap.getOrDefault(submitOrderReq.getDirection(), Character.valueOf('\0')));
			cThostFtdcInputOrderField.setCombOffsetFlag(String
					.valueOf(CtpConstant.offsetMap.getOrDefault(submitOrderReq.getOffset(), Character.valueOf('\0'))));
			cThostFtdcInputOrderField.setOrderRef(orderRef.get() + "");
			cThostFtdcInputOrderField.setInvestorID(userId);
			cThostFtdcInputOrderField.setUserID(userId);
			cThostFtdcInputOrderField.setBrokerID(brokerId);
			cThostFtdcInputOrderField.setExchangeID(CtpConstant.exchangeMap.getOrDefault(submitOrderReq.getContract().getExchange(), ""));

			cThostFtdcInputOrderField
					.setCombHedgeFlag(String.valueOf(jctpv6v3v11x64apiConstants.THOST_FTDC_HF_Speculation));
			cThostFtdcInputOrderField.setContingentCondition(jctpv6v3v11x64apiConstants.THOST_FTDC_CC_Immediately);
			cThostFtdcInputOrderField.setForceCloseReason(jctpv6v3v11x64apiConstants.THOST_FTDC_FCC_NotForceClose);
			cThostFtdcInputOrderField.setIsAutoSuspend(0);
			cThostFtdcInputOrderField.setTimeCondition(jctpv6v3v11x64apiConstants.THOST_FTDC_TC_GFD);
			cThostFtdcInputOrderField.setVolumeCondition(jctpv6v3v11x64apiConstants.THOST_FTDC_VC_AV);
			cThostFtdcInputOrderField.setMinVolume(1);

			// 判断FAK FOK市价单
			if (PriceTypeEnum.FAK == submitOrderReq.getPriceType()) {
				cThostFtdcInputOrderField.setOrderPriceType(jctpv6v3v11x64apiConstants.THOST_FTDC_OPT_LimitPrice);
				cThostFtdcInputOrderField.setTimeCondition(jctpv6v3v11x64apiConstants.THOST_FTDC_TC_IOC);
				cThostFtdcInputOrderField.setVolumeCondition(jctpv6v3v11x64apiConstants.THOST_FTDC_VC_AV);
			} else if (PriceTypeEnum.FOK == submitOrderReq.getPriceType()) {
				cThostFtdcInputOrderField.setOrderPriceType(jctpv6v3v11x64apiConstants.THOST_FTDC_OPT_LimitPrice);
				cThostFtdcInputOrderField.setTimeCondition(jctpv6v3v11x64apiConstants.THOST_FTDC_TC_IOC);
				cThostFtdcInputOrderField.setVolumeCondition(jctpv6v3v11x64apiConstants.THOST_FTDC_VC_CV);
			}

			cThostFtdcTraderApi.ReqOrderInsert(cThostFtdcInputOrderField, reqId.incrementAndGet());
			String orderId = gatewayId + "@" + orderRef.get();

			if (StringUtils.isNotBlank(submitOrderReq.getOriginOrderId())) {
				orderIdOriginalOrderIdMap.put(orderId, submitOrderReq.getOriginOrderId());
				originalOrderIdOrderIdMap.put(submitOrderReq.getOriginOrderId(), orderId);
			}

			submitOrderReqMap.put(orderId, submitOrderReq);
			orderIdAdapterOrderIdMap.put(orderId, orderRef.get()+"");

			return orderId;
		} catch (Throwable t) {
			logger.error("{}交易接口发单错误", logInfo, t);
			return null;
		}

	}

	// 撤单
	public boolean cancelOrder(CancelOrderReqField cancelOrderReq) {

		if (cThostFtdcTraderApi == null) {
			logger.warn("{}交易接口尚未初始化,无法撤单", logInfo);
			return false;
		}

		if (!loginStatus) {
			logger.warn("{}交易接口尚未登录,无法撤单", logInfo);
			return false;
		}

		if (StringUtils.isBlank(cancelOrderReq.getOrderId())
				&& StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
			logger.error("{}参数为空,无法撤单", logInfo);
			return false;
		}

		String orderId = cancelOrderReq.getOrderId();
		if (StringUtils.isBlank(orderId)) {
			orderId = originalOrderIdOrderIdMap.get(cancelOrderReq.getOriginOrderId());
			if (StringUtils.isBlank(orderId)) {
				logger.error("{}交易接口未能找到有效定单号,无法撤单", logInfo);
				return false;
			}
		}

		try {
			CThostFtdcInputOrderActionField cThostFtdcInputOrderActionField = new CThostFtdcInputOrderActionField();
			if (submitOrderReqMap.containsKey(orderId)) {

				cThostFtdcInputOrderActionField
						.setInstrumentID(submitOrderReqMap.get(orderId).getContract().getSymbol());
				cThostFtdcInputOrderActionField.setExchangeID(CtpConstant.exchangeMap
						.getOrDefault(submitOrderReqMap.get(orderId).getContract().getExchange(), ""));
				cThostFtdcInputOrderActionField.setOrderRef(orderIdAdapterOrderIdMap.get(orderId));
				cThostFtdcInputOrderActionField.setFrontID(frontId);
				cThostFtdcInputOrderActionField.setSessionID(sessionId);

				cThostFtdcInputOrderActionField.setActionFlag(jctpv6v3v11x64apiConstants.THOST_FTDC_AF_Delete);
				cThostFtdcInputOrderActionField.setBrokerID(brokerId);
				cThostFtdcInputOrderActionField.setInvestorID(userId);
				cThostFtdcInputOrderActionField.setUserID(userId);
				cThostFtdcInputOrderActionField.setExchangeID(CtpConstant.exchangeMap.getOrDefault(submitOrderReqMap.get(orderId).getContract().getExchange(),""));
				cThostFtdcTraderApi.ReqOrderAction(cThostFtdcInputOrderActionField, reqId.incrementAndGet());
				return true;

			} else if (orderMap.containsKey(orderId)) {
				cThostFtdcInputOrderActionField.setInstrumentID(orderMap.get(orderId).getContract().getSymbol());
				cThostFtdcInputOrderActionField.setExchangeID(
						CtpConstant.exchangeMap.getOrDefault(orderMap.get(orderId).getContract().getExchange(), ""));
				cThostFtdcInputOrderActionField.setOrderRef(orderMap.get(orderId).getAdapterOrderId());
				cThostFtdcInputOrderActionField.setFrontID(orderMap.get(orderId).getFrontId());
				cThostFtdcInputOrderActionField.setSessionID(orderMap.get(orderId).getSessionId());

				cThostFtdcInputOrderActionField.setActionFlag(jctpv6v3v11x64apiConstants.THOST_FTDC_AF_Delete);
				cThostFtdcInputOrderActionField.setBrokerID(brokerId);
				cThostFtdcInputOrderActionField.setInvestorID(userId);
				cThostFtdcInputOrderActionField.setUserID(userId);
				cThostFtdcInputOrderActionField.setExchangeID(CtpConstant.exchangeMap.getOrDefault(orderMap.get(orderId).getContract().getExchange(),""));
				cThostFtdcTraderApi.ReqOrderAction(cThostFtdcInputOrderActionField, reqId.incrementAndGet());
				return true;
			} else {
				logger.error("{}无法找到定单请求或者回报,无法撤单", logInfo);
				return false;
			}
		} catch (Throwable t) {
			logger.error("{}撤单异常", logInfo, t);
			return false;
		}

	}

	private void reqAuth() {
		if (loginFailed) {
			logger.warn("{}交易接口登录曾发生错误,不再登录,以防被锁",logInfo);
			return;
		}

		if (cThostFtdcTraderApi == null) {
			logger.warn("{}发起客户端验证请求错误,交易接口实例不存在", logInfo);
			return;
		}

		if (StringUtils.isEmpty(brokerId)) {
			logger.error("{}BrokerID不允许为空",logInfo);
			return;
		}

		if (StringUtils.isEmpty(userId)) {
			logger.error("{}UserId不允许为空",logInfo);
			return;
		}

		if (StringUtils.isEmpty(password)) {
			logger.error("{}Password不允许为空",logInfo);
			return;
		}

		if (StringUtils.isEmpty(authCode)) {
			logger.error("{}AuthCode不允许为空",logInfo);
			return;
		}

		try {
			CThostFtdcReqAuthenticateField authenticateField = new CThostFtdcReqAuthenticateField();
			authenticateField.setAuthCode(authCode);
			authenticateField.setBrokerID(brokerId);
			authenticateField.setUserProductInfo(userProductInfo);
			authenticateField.setUserID(userId);
			cThostFtdcTraderApi.ReqAuthenticate(authenticateField, reqId.incrementAndGet());
		} catch (Throwable t) {
			logger.error("{}发起客户端验证异常", logInfo, t);
			ctpGatewayImpl.disconnect();
		}

	}

	// 前置机联机回报
	public void OnFrontConnected() {
		try {
			logger.warn("{} 交易接口前置机已连接", logInfo);
			// 修改前置机连接状态
			connectionStatus = CONNECTION_STATUS_CONNECTED;
			if(StringUtils.isBlank(userProductInfo)) {
				CThostFtdcReqUserLoginField reqUserLoginField = new CThostFtdcReqUserLoginField();
				reqUserLoginField.setBrokerID(this.brokerId);
				reqUserLoginField.setUserID(this.userId);
				reqUserLoginField.setPassword(this.password);
				cThostFtdcTraderApi.ReqUserLogin(reqUserLoginField, reqId.incrementAndGet());
			}else {
				reqAuth();
			}
		} catch (Throwable t) {
			logger.error("{}OnFrontConnected Exception",logInfo,t);
		}
	}

	// 前置机断开回报
	public void OnFrontDisconnected(int nReason) {
		try {
			logger.warn("{}交易接口前置机已断开, 原因:{}", logInfo, nReason);
			ctpGatewayImpl.disconnect();
		} catch (Exception e) {
			logger.error("{}OnFrontDisconnected Exception",logInfo,e);
			throw e;
		}
	}

	// 登录回报
	public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		try {
			if (pRspInfo.getErrorID() == 0) {
				logger.warn("{} 交易接口登录成功 TradingDay:{},SessionID:{},BrokerID:{},UserId:{}", logInfo,
						pRspUserLogin.getTradingDay(), pRspUserLogin.getSessionID(), pRspUserLogin.getBrokerID(),
						pRspUserLogin.getUserID());
				sessionId = pRspUserLogin.getSessionID();
				frontId = pRspUserLogin.getFrontID();
				// 修改登录状态为true
				loginStatus = true;
				tradingDay = pRspUserLogin.getTradingDay();
				logger.warn("{}交易接口获取到的交易日为{}", logInfo, tradingDay);

				// 确认结算单
				CThostFtdcSettlementInfoConfirmField settlementInfoConfirmField = new CThostFtdcSettlementInfoConfirmField();
				settlementInfoConfirmField.setBrokerID(brokerId);
				settlementInfoConfirmField.setInvestorID(userId);
				cThostFtdcTraderApi.ReqSettlementInfoConfirm(settlementInfoConfirmField, reqId.incrementAndGet());

			} else {
				logger.error("{}交易接口登录回报错误 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
				loginFailed = true;
			}
		} catch (Throwable t) {
			logger.error("{}交易接口处理登录回报异常", logInfo, t);
			loginFailed = true;
		}

	}

	// 心跳警告
	public void OnHeartBeatWarning(int nTimeLapse) {
		logger.warn("{} 交易接口心跳警告, Time Lapse:{}", logInfo, nTimeLapse);
	}

	// 登出回报
	public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
		try {
			if (pRspInfo.getErrorID() != 0) {
				logger.error("{}OnRspUserLogout!错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(),
						pRspInfo.getErrorMsg());
			} else {
				logger.info("{}OnRspUserLogout!BrokerID:{},UserId:{}", logInfo, pUserLogout.getBrokerID(),
						pUserLogout.getUserID());

			}
		} catch (Throwable t) {
			logger.error("{}交易接口处理登出回报错误", logInfo, t);
		}

		loginStatus = false;
	}

	// 错误回报
	public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		try {
			logger.error("{}交易接口错误回报!错误ID:{},错误信息:{},请求ID:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg(),
					nRequestID);
			if(instrumentQueried) {
				if(pRspInfo.getErrorID() == 0) {
					NoticeField.Builder  noticeBuilder = NoticeField.newBuilder();
					noticeBuilder.setContent("网关:"+ctpGatewayImpl.getGatewayName()
							+",网关ID:"+ctpGatewayImpl.getGatewayId()
							+",交易接口错误回报:"+pRspInfo.getErrorMsg()
							+",错误ID:"+pRspInfo.getErrorID());
					noticeBuilder.setStatus(CommonStatusEnum.INFO);
					noticeBuilder.setTimestamp(System.currentTimeMillis());
					ctpGatewayImpl.emitNotice(noticeBuilder.build());
				}else {

					NoticeField.Builder  noticeBuilder = NoticeField.newBuilder();
					noticeBuilder.setContent("网关:"+ctpGatewayImpl.getGatewayName()
							+",网关ID:"+ctpGatewayImpl.getGatewayId()
							+",交易接口错误回报:"+pRspInfo.getErrorMsg()
							+",错误ID:"+pRspInfo.getErrorID());
					noticeBuilder.setStatus(CommonStatusEnum.ERROR);
					noticeBuilder.setTimestamp(System.currentTimeMillis());
					ctpGatewayImpl.emitNotice(noticeBuilder.build());
				}
			}
			// CTP查询尚未就绪,断开
			if(pRspInfo.getErrorID() == 90) {
				ctpGatewayImpl.disconnect();
			}
		} catch (Throwable t) {
			logger.error("{}OnRspError Exception",logInfo,t);
			throw t;
		}
	}

	// 验证客户端回报
	public void OnRspAuthenticate(CThostFtdcRspAuthenticateField pRspAuthenticateField, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		try {
			if (pRspInfo != null) {
				if (pRspInfo.getErrorID() == 0) {
					logger.warn(logInfo + "交易接口客户端验证成功");
					CThostFtdcReqUserLoginField reqUserLoginField = new CThostFtdcReqUserLoginField();
					reqUserLoginField.setBrokerID(this.brokerId);
					reqUserLoginField.setUserID(this.userId);
					reqUserLoginField.setPassword(this.password);
					cThostFtdcTraderApi.ReqUserLogin(reqUserLoginField, reqId.incrementAndGet());
				} else {

					logger.error("{}交易接口客户端验证失败 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(),
							pRspInfo.getErrorMsg());
					loginFailed = true;
				}
			} else {
				loginFailed = true;
				logger.error("{}处理交易接口客户端验证回报错误,回报信息为空", logInfo);
			}
		} catch (Throwable t) {
			loginFailed = true;
			logger.error("{}处理交易接口客户端验证回报异常", logInfo, t);
		}
	}

	public void OnRspUserPasswordUpdate(CThostFtdcUserPasswordUpdateField pUserPasswordUpdate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspTradingAccountPasswordUpdate(
			CThostFtdcTradingAccountPasswordUpdateField pTradingAccountPasswordUpdate, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	// 发单错误（柜台）
	public void OnRspOrderInsert(CThostFtdcInputOrderField pInputOrder, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
		try {
			if (pInputOrder != null) {
				// 使用特定值
				String accountId = userId + "@CNY@" + gatewayId;

				String symbol = pInputOrder.getInstrumentID();
				String adapterOrderId = pInputOrder.getOrderRef();
				String orderId = gatewayId + "@" + adapterOrderId;
				DirectionEnum direction = CtpConstant.directionMapReverse.getOrDefault(pInputOrder.getDirection(),
						DirectionEnum.UNKNOWN_DIRECTION);
				OffsetEnum offset = CtpConstant.offsetMapReverse.getOrDefault(pInputOrder.getCombOffsetFlag().toCharArray()[0],
						OffsetEnum.UNKNOWN_OFFSET);
				double price = pInputOrder.getLimitPrice();
				int totalVolume = pInputOrder.getVolumeTotalOriginal();
				int tradedVolume = 0;
				OrderStatusEnum orderStatus = OrderStatusEnum.REJECTED;

				String originalOrderId = orderIdOriginalOrderIdMap.getOrDefault(orderId, "");

				OrderField.Builder orderBuilder = OrderField.newBuilder();
				orderBuilder.setAccountId(accountId);
				orderBuilder.setOriginOrderId(originalOrderId);
				orderBuilder.setOrderId(orderId);
				orderBuilder.setAdapterOrderId(adapterOrderId);
				orderBuilder.setDirection(direction);
				orderBuilder.setOffset(offset);
				orderBuilder.setPrice(price);
				orderBuilder.setTotalVolume(totalVolume);
				orderBuilder.setTradedVolume(tradedVolume);
				orderBuilder.setOrderStatus(orderStatus);
				orderBuilder.setTradingDay(tradingDay);
				orderBuilder.setFrontId(frontId);
				orderBuilder.setSessionId(sessionId);
				orderBuilder.setGateway(ctpGatewayImpl.getGateway());

				if (pRspInfo != null && pRspInfo.getErrorMsg() != null) {
					orderBuilder.setStatusInfo(pRspInfo.getErrorMsg());
				}

				if (instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol)) {
					orderBuilder.setContract(ctpGatewayImpl.contractMap.get(symbol));
					OrderField order = orderBuilder.build();
					orderMap.put(order.getOrderId(), order);
					ctpGatewayImpl.emitOrder(order);
				} else {
					ContractField.Builder contractBuilder = ContractField.newBuilder();
					contractBuilder.setSymbol(symbol);
					orderBuilder.setContract(ctpGatewayImpl.contractMap.get(symbol));

					orderBuilderCacheList.add(orderBuilder);
				}
			} else {
				logger.error("{}处理交易接口发单错误回报(柜台)错误,空数据", logInfo);
			}

			if (pRspInfo != null) {
				logger.error("{}交易接口发单错误回报(柜台) 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(),
						pRspInfo.getErrorMsg());
				if(instrumentQueried) {
					NoticeField.Builder  noticeBuilder = NoticeField.newBuilder();
					noticeBuilder.setContent(logInfo+"交易接口发单错误回报(柜台) 错误ID:"+pRspInfo.getErrorID()+",错误信息:"+pRspInfo.getErrorMsg());
					noticeBuilder.setStatus(CommonStatusEnum.ERROR);
					noticeBuilder.setTimestamp(System.currentTimeMillis());
					ctpGatewayImpl.emitNotice(noticeBuilder.build());
				}
			} else {
				logger.error("{}处理交易接口发单错误回报(柜台)错误,回报信息为空", logInfo);
			}

		} catch (Throwable t) {
			logger.error("{}处理交易接口发单错误回报(柜台)异常", logInfo, t);
		}

	}

	public void OnRspParkedOrderInsert(CThostFtdcParkedOrderField pParkedOrder, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspParkedOrderAction(CThostFtdcParkedOrderActionField pParkedOrderAction,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	// 撤单错误回报（柜台）
	public void OnRspOrderAction(CThostFtdcInputOrderActionField pInputOrderAction, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		if (pRspInfo != null) {
			logger.error("{}交易接口撤单错误回报(柜台) 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
			if(instrumentQueried) {
				NoticeField.Builder  noticeBuilder = NoticeField.newBuilder();
				noticeBuilder.setContent(logInfo+"交易接口撤单错误回报(柜台) 错误ID:"+pRspInfo.getErrorID()+",错误信息:"+pRspInfo.getErrorMsg());
				noticeBuilder.setStatus(CommonStatusEnum.ERROR);
				noticeBuilder.setTimestamp(System.currentTimeMillis());
				ctpGatewayImpl.emitNotice(noticeBuilder.build());
			}
		} else {
			logger.error("{}处理交易接口撤单错误回报(柜台)错误,无有效信息", logInfo);
		}
	}

	public void OnRspQueryMaxOrderVolume(CThostFtdcQueryMaxOrderVolumeField pQueryMaxOrderVolume,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	// 确认结算信息回报
	public void OnRspSettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		try {
			if (pRspInfo.getErrorID() == 0) {
				logger.warn("{}交易接口结算信息确认完成", logInfo);
			} else {
				logger.error("{}交易接口结算信息确认出错 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
				ctpGatewayImpl.disconnect();
				return;
			}

			// 防止被限流
			Thread.sleep(1000);

			logger.warn("{}交易接口开始查询投资者信息", logInfo);
			CThostFtdcQryInvestorField pQryInvestor = new CThostFtdcQryInvestorField();
			pQryInvestor.setInvestorID(userId);
			pQryInvestor.setBrokerID(brokerId);
			cThostFtdcTraderApi.ReqQryInvestor(pQryInvestor, reqId.addAndGet(1));
		} catch (Throwable t) {
			logger.error("{}处理结算单确认回报错误", logInfo, t);
			ctpGatewayImpl.disconnect();
		}
	}

	public void OnRspRemoveParkedOrder(CThostFtdcRemoveParkedOrderField pRemoveParkedOrder,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspRemoveParkedOrderAction(CThostFtdcRemoveParkedOrderActionField pRemoveParkedOrderAction,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspExecOrderInsert(CThostFtdcInputExecOrderField pInputExecOrder, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspExecOrderAction(CThostFtdcInputExecOrderActionField pInputExecOrderAction,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspForQuoteInsert(CThostFtdcInputForQuoteField pInputForQuote, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQuoteInsert(CThostFtdcInputQuoteField pInputQuote, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	public void OnRspQuoteAction(CThostFtdcInputQuoteActionField pInputQuoteAction, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspBatchOrderAction(CThostFtdcInputBatchOrderActionField pInputBatchOrderAction,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspOptionSelfCloseInsert(CThostFtdcInputOptionSelfCloseField pInputOptionSelfClose,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspOptionSelfCloseAction(CThostFtdcInputOptionSelfCloseActionField pInputOptionSelfCloseAction,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspCombActionInsert(CThostFtdcInputCombActionField pInputCombAction, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryOrder(CThostFtdcOrderField pOrder, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	public void OnRspQryTrade(CThostFtdcTradeField pTrade, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	// 持仓查询回报
	public void OnRspQryInvestorPosition(CThostFtdcInvestorPositionField pInvestorPosition,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

		try {
			if (pInvestorPosition == null || StringUtils.isEmpty(pInvestorPosition.getInstrumentID())) {
				return;
			}
			String symbol = pInvestorPosition.getInstrumentID();

			if (!instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol)) {
				logger.warn("{}尚未获取到合约信息,暂时不处理持仓数据", logInfo);
				return;
			}

			ContractField contract = ctpGatewayImpl.contractMap.get(symbol);

			String uniqueSymbol = symbol + "@" + contract.getExchange().getValueDescriptor().getName() + "@"
					+ contract.getProductType().getValueDescriptor().getName();

			// 无法获取账户信息,使用userId作为账户ID
			String accountCode = userId;
			// 无法获取币种信息使用特定值
			String accountId = accountCode + "@CNY@" + gatewayId;

			DirectionEnum direction = CtpConstant.posiDirectionMapReverse
					.getOrDefault(pInvestorPosition.getPosiDirection(), DirectionEnum.UNKNOWN_DIRECTION);

			// 获取持仓缓存
			String positionId = uniqueSymbol + "@" + direction.getValueDescriptor().getName() + "@" + accountCode + "@"
					+ gatewayId;

			PositionField.Builder positionBuilder;
			if (positionBuilderMap.containsKey(positionId)) {
				positionBuilder = positionBuilderMap.get(positionId);
			} else {
				positionBuilder = PositionField.newBuilder();
				positionBuilderMap.put(positionId, positionBuilder);
				positionBuilder.setContract(ctpGatewayImpl.contractMap.get(symbol));
				positionBuilder.setDirection(CtpConstant.posiDirectionMapReverse
						.getOrDefault(pInvestorPosition.getPosiDirection(), DirectionEnum.UNKNOWN_DIRECTION));
				positionBuilder.setPositionId(positionId);

				positionBuilder.setAccountId(accountId);
				positionBuilder.setGateway(ctpGatewayImpl.getGateway());

			}

			positionBuilder.setUseMargin(positionBuilder.getUseMargin() + pInvestorPosition.getUseMargin());
			positionBuilder
					.setExchangeMargin(positionBuilder.getExchangeMargin() + pInvestorPosition.getExchangeMargin());

			positionBuilder.setPosition(positionBuilder.getPosition() + pInvestorPosition.getPosition());

			if (positionBuilder.getDirection() == DirectionEnum.LONG) {
				positionBuilder.setFrozen(pInvestorPosition.getShortFrozen());
			} else {
				positionBuilder.setFrozen(pInvestorPosition.getLongFrozen());
			}

			if (ExchangeEnum.INE == positionBuilder.getContract().getExchange()
					|| ExchangeEnum.SHFE == positionBuilder.getContract().getExchange()) {
				// 针对上期所、上期能源持仓的今昨分条返回（有昨仓、无今仓）,读取昨仓数据
				if (pInvestorPosition.getYdPosition() > 0 && pInvestorPosition.getTodayPosition() == 0) {

					positionBuilder.setYdPosition(positionBuilder.getYdPosition() + pInvestorPosition.getPosition());

					if (positionBuilder.getDirection() == DirectionEnum.LONG) {
						positionBuilder.setYdFrozen(positionBuilder.getYdFrozen() + pInvestorPosition.getShortFrozen());
					} else {
						positionBuilder.setYdFrozen(positionBuilder.getYdFrozen() + pInvestorPosition.getLongFrozen());
					}
				} else {
					positionBuilder.setTdPosition(positionBuilder.getTdPosition() + pInvestorPosition.getPosition());

					if (positionBuilder.getDirection() == DirectionEnum.LONG) {
						positionBuilder.setTdFrozen(positionBuilder.getTdFrozen() + pInvestorPosition.getShortFrozen());
					} else {
						positionBuilder.setTdFrozen(positionBuilder.getTdFrozen() + pInvestorPosition.getLongFrozen());
					}
				}
			} else {
				positionBuilder.setTdPosition(positionBuilder.getTdPosition() + pInvestorPosition.getTodayPosition());
				positionBuilder.setYdPosition(positionBuilder.getPosition() - positionBuilder.getTdPosition());

				// 中金所优先平今
				if (ExchangeEnum.CFFEX == positionBuilder.getContract().getExchange()) {
					if (positionBuilder.getTdPosition() > 0) {
						if (positionBuilder.getTdPosition() >= positionBuilder.getFrozen()) {
							positionBuilder.setTdFrozen(positionBuilder.getFrozen());
						} else {
							positionBuilder.setTdFrozen(positionBuilder.getTdPosition());
							positionBuilder.setYdFrozen(positionBuilder.getFrozen() - positionBuilder.getTdPosition());
						}
					} else {
						positionBuilder.setYdFrozen(positionBuilder.getFrozen());
					}
				} else {
					// 除了上面几个交易所之外的交易所，优先平昨
					if (positionBuilder.getYdPosition() > 0) {
						if (positionBuilder.getYdPosition() >= positionBuilder.getFrozen()) {
							positionBuilder.setYdFrozen(positionBuilder.getFrozen());
						} else {
							positionBuilder.setYdFrozen(positionBuilder.getYdPosition());
							positionBuilder.setTdFrozen(positionBuilder.getFrozen() - positionBuilder.getYdPosition());
						}
					} else {
						positionBuilder.setTdFrozen(positionBuilder.getFrozen());
					}
				}

			}

			// 计算成本
			Double cost = positionBuilder.getPrice() * positionBuilder.getPosition()
					* positionBuilder.getContract().getMultiplier();
			Double openCost = positionBuilder.getOpenPrice() * positionBuilder.getPosition()
					* positionBuilder.getContract().getMultiplier();

			// 汇总总仓
			positionBuilder
					.setPositionProfit(positionBuilder.getPositionProfit() + pInvestorPosition.getPositionProfit());

			// 计算持仓均价
			if (positionBuilder.getPosition() != 0) {
				positionBuilder.setPrice((cost + pInvestorPosition.getPositionCost())
						/ (positionBuilder.getPosition() * positionBuilder.getContract().getMultiplier()));
				positionBuilder.setOpenPrice((openCost + pInvestorPosition.getOpenCost())
						/ (positionBuilder.getPosition() * positionBuilder.getContract().getMultiplier()));
			}

			// 回报结束
			if (bIsLast) {
				for (PositionField.Builder tmpPositionBuilder : positionBuilderMap.values()) {

					if (tmpPositionBuilder.getPosition() != 0) {

						tmpPositionBuilder.setPriceDiff(tmpPositionBuilder.getPositionProfit()
								/ tmpPositionBuilder.getContract().getMultiplier() / tmpPositionBuilder.getPosition());

						if (tmpPositionBuilder.getDirection() == DirectionEnum.LONG
								|| (tmpPositionBuilder.getPosition() > 0
										&& tmpPositionBuilder.getDirection() == DirectionEnum.NET)) {

							// 计算最新价格
							tmpPositionBuilder
									.setLastPrice(tmpPositionBuilder.getPrice() + tmpPositionBuilder.getPriceDiff());
							// 计算开仓价格差距
							tmpPositionBuilder.setOpenPriceDiff(
									tmpPositionBuilder.getLastPrice() - tmpPositionBuilder.getOpenPrice());
							// 计算开仓盈亏
							tmpPositionBuilder.setOpenPositionProfit(
									tmpPositionBuilder.getOpenPriceDiff() * tmpPositionBuilder.getPosition()
											* tmpPositionBuilder.getContract().getMultiplier());

						} else if (tmpPositionBuilder.getDirection() == DirectionEnum.SHORT
								|| (tmpPositionBuilder.getPosition() < 0
										&& tmpPositionBuilder.getDirection() == DirectionEnum.NET)) {

							// 计算最新价格
							tmpPositionBuilder
									.setLastPrice(tmpPositionBuilder.getPrice() - tmpPositionBuilder.getPriceDiff());
							// 计算开仓价格差距
							tmpPositionBuilder.setOpenPriceDiff(
									tmpPositionBuilder.getOpenPrice() - tmpPositionBuilder.getLastPrice());
							// 计算开仓盈亏
							tmpPositionBuilder.setOpenPositionProfit(
									tmpPositionBuilder.getOpenPriceDiff() * tmpPositionBuilder.getPosition()
											* tmpPositionBuilder.getContract().getMultiplier());

						} else {
							logger.error("{} 计算持仓时发现未处理方向，持仓详情{}", logInfo, tmpPositionBuilder.toString());
						}

						// 计算保最新合约价值
						tmpPositionBuilder.setContractValue(tmpPositionBuilder.getLastPrice()
								* tmpPositionBuilder.getContract().getMultiplier() * tmpPositionBuilder.getPosition());

						if (tmpPositionBuilder.getUseMargin() != 0) {
							tmpPositionBuilder.setPositionProfitRatio(
									tmpPositionBuilder.getPositionProfit() / tmpPositionBuilder.getUseMargin());
							tmpPositionBuilder.setOpenPositionProfitRatio(
									tmpPositionBuilder.getOpenPositionProfit() / tmpPositionBuilder.getUseMargin());

						}
					}

					// 发送持仓事件
					ctpGatewayImpl.emitPosition(tmpPositionBuilder.build());
				}

				// 清空缓存
				positionBuilderMap = new HashMap<>();
			}

		} catch (Throwable t) {
			logger.error("{}处理查询持仓回报异常", logInfo, t);
			ctpGatewayImpl.disconnect();
		}
	}

	// 账户查询回报
	public void OnRspQryTradingAccount(CThostFtdcTradingAccountField pTradingAccount, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {

		try {
			String accountCode = pTradingAccount.getAccountID();
			String currency = pTradingAccount.getCurrencyID();
			// 无法获取币种信息使用特定值
			String accountId = accountCode + "@" + currency + "@" + gatewayId;

			AccountField.Builder accountBuilder = AccountField.newBuilder();
			accountBuilder.setCode(accountCode);
			accountBuilder.setCurrency(CurrencyEnum.valueOf(currency));
			accountBuilder.setAvailable(pTradingAccount.getAvailable());
			accountBuilder.setCloseProfit(pTradingAccount.getCloseProfit());
			accountBuilder.setCommission(pTradingAccount.getCommission());
			accountBuilder.setGateway(ctpGatewayImpl.getGateway());
			accountBuilder.setMargin(pTradingAccount.getCurrMargin());
			accountBuilder.setPositionProfit(pTradingAccount.getPositionProfit());
			accountBuilder.setPreBalance(pTradingAccount.getPreBalance());
			accountBuilder.setAccountId(accountId);
			accountBuilder.setDeposit(pTradingAccount.getDeposit());
			accountBuilder.setWithdraw(pTradingAccount.getWithdraw());
			accountBuilder.setHolder(investorName);

			accountBuilder.setBalance(pTradingAccount.getBalance());

			ctpGatewayImpl.emitAccount(accountBuilder.build());
		} catch (Throwable t) {
			logger.error("{}处理查询账户回报异常", logInfo, t);
			ctpGatewayImpl.disconnect();
		}

	}

	public void OnRspQryInvestor(CThostFtdcInvestorField pInvestor, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {

		try {

			if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
				logger.error("{}查询投资者信息失败 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
				ctpGatewayImpl.disconnect();
			} else {
				if (pInvestor != null) {
					investorName = pInvestor.getInvestorName();
					logger.warn("{}交易接口获取到的投资者名为:{}", logInfo, investorName);
				} else {
					logger.error("{}交易接口未能获取到投资者名", logInfo);
				}
			}

			if (bIsLast) {
				if (StringUtils.isBlank(investorName)) {
					logger.warn("{}交易接口未能获取到投资者名,准备断开", logInfo);
					ctpGatewayImpl.disconnect();
				}
				investorNameQueried = true;

				// 防止被限流
				Thread.sleep(1000);
				// 查询所有合约
				logger.warn("{}交易接口开始查询合约信息", logInfo);
				CThostFtdcQryInstrumentField cThostFtdcQryInstrumentField = new CThostFtdcQryInstrumentField();
				cThostFtdcTraderApi.ReqQryInstrument(cThostFtdcQryInstrumentField, reqId.incrementAndGet());
			}
		} catch (Throwable t) {
			logger.error("{}处理查询投资者回报异常", logInfo, t);
			ctpGatewayImpl.disconnect();
		}

	}

	public void OnRspQryTradingCode(CThostFtdcTradingCodeField pTradingCode, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryInstrumentMarginRate(CThostFtdcInstrumentMarginRateField pInstrumentMarginRate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryInstrumentCommissionRate(CThostFtdcInstrumentCommissionRateField pInstrumentCommissionRate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryExchange(CThostFtdcExchangeField pExchange, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	public void OnRspQryProduct(CThostFtdcProductField pProduct, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	// 合约查询回报
	public void OnRspQryInstrument(CThostFtdcInstrumentField pInstrument, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
		try {

			ContractField.Builder contractBuilder = ContractField.newBuilder();
			contractBuilder.setGateway(ctpGatewayImpl.getGateway());
			contractBuilder.setSymbol(pInstrument.getInstrumentID());
			contractBuilder.setExchange(CtpConstant.exchangeMapReverse.getOrDefault(pInstrument.getExchangeID(),
					ExchangeEnum.UNKNOWN_EXCHANGE));
			contractBuilder.setProductType(CtpConstant.productTypeMapReverse.getOrDefault(pInstrument.getProductClass(),
					ProductTypeEnum.UNKNOWN_PRODUCT_TYPE));
			contractBuilder.setUnifiedSymbol(contractBuilder.getSymbol() + "@" + contractBuilder.getExchange() + "@"
					+ contractBuilder.getProductType());
			contractBuilder.setContractId(contractBuilder.getUnifiedSymbol() + "@" + gatewayId);
			contractBuilder.setShortName(pInstrument.getInstrumentName());
			contractBuilder.setFullName(pInstrument.getInstrumentName());
			contractBuilder.setThirdPartyId(contractBuilder.getSymbol());

			contractBuilder.setMultiplier(pInstrument.getVolumeMultiple());
			contractBuilder.setPriceTick(pInstrument.getPriceTick());
			contractBuilder.setCurrency(CurrencyEnum.CNY); // 默认人民币
			contractBuilder.setLastTradeDateOrContractMonth(pInstrument.getExpireDate());

			contractBuilder.setStrikePrice(pInstrument.getStrikePrice());
			contractBuilder.setOptionType(CtpConstant.optionTypeMapReverse.getOrDefault(pInstrument.getOptionsType(),
					OptionTypeEnum.UNKNOWN_OPTION_TYPE));
			if(pInstrument.getUnderlyingInstrID()!=null) {
				contractBuilder.setUnderlyingSymbol(pInstrument.getUnderlyingInstrID());
			}
			contractBuilder.setUnderlyingMultiplier(pInstrument.getUnderlyingMultiple());
			contractBuilder.setMaxLimitOrderVolume(pInstrument.getMaxLimitOrderVolume());
			contractBuilder.setMaxMarketOrderVolume(pInstrument.getMaxMarketOrderVolume());
			contractBuilder.setMinLimitOrderVolume(pInstrument.getMinLimitOrderVolume());
			contractBuilder.setMinMarketOrderVolume(pInstrument.getMinMarketOrderVolume());
			contractBuilder.setMaxMarginSideAlgorithm(pInstrument.getMaxMarginSideAlgorithm() == '1');
			
			contractBuilder.setLongMarginRatio(pInstrument.getLongMarginRatio());
			contractBuilder.setShortMarginRatio(pInstrument.getShortMarginRatio());
			

			ContractField contract = contractBuilder.build();
			ctpGatewayImpl.contractMap.put(contractBuilder.getSymbol(), contract);

			if (bIsLast) {

				for (ContractField tmpContract : ctpGatewayImpl.contractMap.values()) {
					ctpGatewayImpl.emitContract(tmpContract);
				}

				logger.warn("{} 交易接口合约信息获取完成!共计{}条", logInfo, ctpGatewayImpl.contractMap.size());
				instrumentQueried = true;
				this.startIntervalQuery();

				logger.warn("{} 交易接口开始推送缓存Order,共计{}条", logInfo, orderBuilderCacheList.size());
				for (OrderField.Builder orderBuilder : orderBuilderCacheList) {
					if (ctpGatewayImpl.contractMap.containsKey(orderBuilder.getContract().getSymbol())) {
						orderBuilder.setContract(ctpGatewayImpl.contractMap.get(orderBuilder.getContract().getSymbol()));
						OrderField order = orderBuilder.build();
						orderMap.put(order.getOrderId(), order);
						ctpGatewayImpl.emitOrder(order);
					} else {
						logger.error("{}未能正确获取到合约信息，代码{}", logInfo, orderBuilder.getContract().getSymbol());
					}
				}
				orderBuilderCacheList.clear();

				logger.warn("{} 交易接口开始推送缓存Trade,共计{}条", logInfo, tradeBuilderCacheList.size());
				for (TradeField.Builder tradeBuilder : tradeBuilderCacheList) {
					if (ctpGatewayImpl.contractMap.containsKey(tradeBuilder.getContract().getSymbol())) {
						tradeBuilder.setContract(ctpGatewayImpl.contractMap.get(tradeBuilder.getContract().getSymbol()));
						ctpGatewayImpl.emitTrade(tradeBuilder.build());
					} else {
						logger.error("{}未能正确获取到合约信息，代码{}", logInfo, tradeBuilder.getContract().getSymbol());
					}
				}
				tradeBuilderCacheList.clear();
				
			}
		} catch (Throwable t) {
			logger.error("{}OnRspQryInstrument Exception",logInfo,t);
		}

	}

	public void OnRspQryDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQrySettlementInfo(CThostFtdcSettlementInfoField pSettlementInfo, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryTransferBank(CThostFtdcTransferBankField pTransferBank, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryInvestorPositionDetail(CThostFtdcInvestorPositionDetailField pInvestorPositionDetail,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryNotice(CThostFtdcNoticeField pNotice, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	public void OnRspQrySettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryInvestorPositionCombineDetail(
			CThostFtdcInvestorPositionCombineDetailField pInvestorPositionCombineDetail,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryCFMMCTradingAccountKey(CThostFtdcCFMMCTradingAccountKeyField pCFMMCTradingAccountKey,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryEWarrantOffset(CThostFtdcEWarrantOffsetField pEWarrantOffset, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryInvestorProductGroupMargin(
			CThostFtdcInvestorProductGroupMarginField pInvestorProductGroupMargin, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryExchangeMarginRate(CThostFtdcExchangeMarginRateField pExchangeMarginRate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryExchangeMarginRateAdjust(CThostFtdcExchangeMarginRateAdjustField pExchangeMarginRateAdjust,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryExchangeRate(CThostFtdcExchangeRateField pExchangeRate, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQrySecAgentACIDMap(CThostFtdcSecAgentACIDMapField pSecAgentACIDMap,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryProductExchRate(CThostFtdcProductExchRateField pProductExchRate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryProductGroup(CThostFtdcProductGroupField pProductGroup, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryMMInstrumentCommissionRate(
			CThostFtdcMMInstrumentCommissionRateField pMMInstrumentCommissionRate, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryMMOptionInstrCommRate(CThostFtdcMMOptionInstrCommRateField pMMOptionInstrCommRate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryInstrumentOrderCommRate(CThostFtdcInstrumentOrderCommRateField pInstrumentOrderCommRate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQrySecAgentTradingAccount(CThostFtdcTradingAccountField pTradingAccount,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQrySecAgentCheckMode(CThostFtdcSecAgentCheckModeField pSecAgentCheckMode,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryOptionInstrTradeCost(CThostFtdcOptionInstrTradeCostField pOptionInstrTradeCost,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryOptionInstrCommRate(CThostFtdcOptionInstrCommRateField pOptionInstrCommRate,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryExecOrder(CThostFtdcExecOrderField pExecOrder, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	public void OnRspQryForQuote(CThostFtdcForQuoteField pForQuote, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	public void OnRspQryQuote(CThostFtdcQuoteField pQuote, CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {
	}

	public void OnRspQryOptionSelfClose(CThostFtdcOptionSelfCloseField pOptionSelfClose,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryInvestUnit(CThostFtdcInvestUnitField pInvestUnit, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryCombInstrumentGuard(CThostFtdcCombInstrumentGuardField pCombInstrumentGuard,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryCombAction(CThostFtdcCombActionField pCombAction, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryTransferSerial(CThostFtdcTransferSerialField pTransferSerial, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryAccountregister(CThostFtdcAccountregisterField pAccountregister,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	// 委托回报
	public void OnRtnOrder(CThostFtdcOrderField pOrder) {
		
		try {

			String newRef = pOrder.getOrderRef().replace(" ", "");
			// 更新最大报单编号
			orderRef = new AtomicInteger(Math.max(orderRef.get(), Integer.valueOf(newRef)));

			String symbol = pOrder.getInstrumentID();
			/*
			 * CTP的报单号一致性维护需要基于frontID, sessionID, orderID三个字段
			 * 但在本接口设计中,已经考虑了CTP的OrderRef的自增性,避免重复 唯一可能出现OrderRef重复的情况是多处登录并在非常接近的时间内（几乎同时发单
			 */

			// 无法获取账户信息,使用userId作为账户ID
			String accountCode = userId;
			// 无法获取币种信息使用特定值
			String accountId = accountCode + "@CNY@" + gatewayId;

			String adapterOrderId = StringUtils.trim(pOrder.getOrderRef());
			String orderId = gatewayId + "@" + adapterOrderId;
			DirectionEnum direction = CtpConstant.directionMapReverse.getOrDefault(pOrder.getDirection(),
					DirectionEnum.UNKNOWN_DIRECTION);
			OffsetEnum offset = CtpConstant.offsetMapReverse.getOrDefault(pOrder.getCombOffsetFlag().toCharArray()[0],
					OffsetEnum.UNKNOWN_OFFSET);
			double price = pOrder.getLimitPrice();
			int totalVolume = pOrder.getVolumeTotalOriginal();
			int tradedVolume = pOrder.getVolumeTraded();
			OrderStatusEnum orderStatus = CtpConstant.statusMapReverse.get(pOrder.getOrderStatus());
			String orderDate = pOrder.getInsertDate();
			String orderTime = pOrder.getInsertTime();
			String cancelTime = pOrder.getCancelTime();
			String activeTime = pOrder.getActiveTime();
			String updateTime = pOrder.getUpdateTime();
			int frontID = pOrder.getFrontID();
			int sessionID = pOrder.getSessionID();
			String statusMsg = pOrder.getStatusMsg();

			String originalOrderID = orderIdOriginalOrderIdMap.getOrDefault(orderId, "");

			OrderField.Builder orderBuilder = OrderField.newBuilder();

			orderBuilder.setAccountId(accountId);
			;
			orderBuilder.setActiveTime(activeTime);
			orderBuilder.setAdapterOrderId(adapterOrderId);
			orderBuilder.setCancelTime(cancelTime);
			orderBuilder.setDirection(direction);
			orderBuilder.setFrontId(frontID);
			orderBuilder.setOffset(offset);
			orderBuilder.setOrderDate(orderDate);
			orderBuilder.setOrderId(orderId);
			orderBuilder.setOrderStatus(orderStatus);
			orderBuilder.setOrderTime(orderTime);
			orderBuilder.setOriginOrderId(originalOrderID);
			orderBuilder.setPrice(price);
			orderBuilder.setSessionId(sessionID);
			orderBuilder.setTotalVolume(totalVolume);
			orderBuilder.setTradedVolume(tradedVolume);
			orderBuilder.setTradingDay(tradingDay);
			orderBuilder.setUpdateTime(updateTime);
			orderBuilder.setStatusInfo(statusMsg);
			orderBuilder.setGateway(ctpGatewayImpl.getGateway());

			if (instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol)) {
				orderBuilder.setContract(ctpGatewayImpl.contractMap.get(symbol));
				OrderField order = orderBuilder.build();
				orderMap.put(order.getOrderId(), order);
				ctpGatewayImpl.emitOrder(order);
			} else {
				ContractField.Builder contractBuilder = ContractField.newBuilder();
				contractBuilder.setSymbol(symbol);

				orderBuilder.setContract(contractBuilder);

				orderBuilderCacheList.add(orderBuilder);
			}
			
		} catch (Throwable t) {
			logger.error("{}OnRtnOrder Exception",logInfo,t);
		}

	}

	// 成交回报
	public void OnRtnTrade(CThostFtdcTradeField pTrade) {
		try {
			String symbol = pTrade.getInstrumentID();
			String adapterOrderId = StringUtils.trim(pTrade.getOrderRef());
			String orderId = gatewayId + "@" + StringUtils.trim(pTrade.getOrderRef());
			DirectionEnum direction = CtpConstant.directionMapReverse.getOrDefault(pTrade.getDirection(),
					DirectionEnum.UNKNOWN_DIRECTION);
			String adapterTradeId = StringUtils.trim(pTrade.getTradeID());
			String tradeId = gatewayId + "@" + direction.getValueDescriptor().getName() + "@" + adapterTradeId;
			OffsetEnum offset = CtpConstant.offsetMapReverse.getOrDefault(pTrade.getOffsetFlag(),
					OffsetEnum.UNKNOWN_OFFSET);
			double price = pTrade.getPrice();
			int volume = pTrade.getVolume();
			String tradeDate = pTrade.getTradeDate();
			String tradeTime = pTrade.getTradeTime();

			String originalOrderID = orderIdOriginalOrderIdMap.getOrDefault(orderId, "");

			// 无法获取账户信息,使用userId作为账户ID
			String accountCode = userId;
			// 无法获取币种信息使用特定值
			String accountId = accountCode + "@CNY@" + gatewayId;

			TradeField.Builder tradeBuilder = TradeField.newBuilder();

			tradeBuilder.setAccountId(accountId);
			tradeBuilder.setAdapterOrderId(adapterOrderId);
			tradeBuilder.setAdapterTradeId(adapterTradeId);
			tradeBuilder.setTradeDate(tradeDate);
			tradeBuilder.setTradeId(tradeId);
			tradeBuilder.setTradeTime(tradeTime);
			tradeBuilder.setTradingDay(tradingDay);
			tradeBuilder.setDirection(direction);
			tradeBuilder.setOffset(offset);
			tradeBuilder.setOrderId(orderId);
			tradeBuilder.setOriginOrderId(originalOrderID);
			tradeBuilder.setPrice(price);
			tradeBuilder.setVolume(volume);
			tradeBuilder.setGateway(ctpGatewayImpl.getGateway());

			if (instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol)) {
				tradeBuilder.setContract(ctpGatewayImpl.contractMap.get(symbol));
				ctpGatewayImpl.emitTrade(tradeBuilder.build());
			} else {
				ContractField.Builder contractBuilder = ContractField.newBuilder();
				contractBuilder.setSymbol(symbol);

				tradeBuilder.setContract(contractBuilder);
				tradeBuilderCacheList.add(tradeBuilder);
			}
			
		} catch (Throwable t) {
			logger.error("{}OnRtnTrade Exception",logInfo,t);
		}

	}

	// 发单错误回报（交易所）
	public void OnErrRtnOrderInsert(CThostFtdcInputOrderField pInputOrder, CThostFtdcRspInfoField pRspInfo) {
		
		try {

			String newRef = StringUtils.trim(pInputOrder.getOrderRef());
			// 更新最大报单编号
			orderRef = new AtomicInteger(Math.max(orderRef.get(), Integer.valueOf(newRef)));

			String symbol = pInputOrder.getInstrumentID();
			/*
			 * CTP的报单号一致性维护需要基于frontID, sessionID, orderID三个字段
			 * 但在本接口设计中,已经考虑了CTP的OrderRef的自增性,避免重复 唯一可能出现OrderRef重复的情况是多处登录并在非常接近的时间内（几乎同时发单
			 */

			// 无法获取账户信息,使用userId作为账户ID
			String accountCode = userId;
			// 无法获取币种信息使用特定值
			String accountId = accountCode + "@CNY@" + gatewayId;

			String adapterOrderId = StringUtils.trim(pInputOrder.getOrderRef());
			String orderId = gatewayId + "@" + adapterOrderId;
			DirectionEnum direction = CtpConstant.directionMapReverse.getOrDefault(pInputOrder.getDirection(),
					DirectionEnum.UNKNOWN_DIRECTION);
			OffsetEnum offset = CtpConstant.offsetMapReverse.getOrDefault(pInputOrder.getCombOffsetFlag().toCharArray()[0],
					OffsetEnum.UNKNOWN_OFFSET);
			double price = pInputOrder.getLimitPrice();
			int totalVolume = pInputOrder.getVolumeTotalOriginal();
			int tradedVolume = 0;
			OrderStatusEnum orderStatus = OrderStatusEnum.REJECTED;

			String originalOrderID = orderIdOriginalOrderIdMap.getOrDefault(orderId, "");

			OrderField.Builder orderBuilder = OrderField.newBuilder();
			orderBuilder.setAccountId(accountId);
			orderBuilder.setAdapterOrderId(adapterOrderId);
			orderBuilder.setDirection(direction);
			orderBuilder.setOffset(offset);
			orderBuilder.setOrderId(orderId);
			orderBuilder.setOrderStatus(orderStatus);
			orderBuilder.setOriginOrderId(originalOrderID);
			orderBuilder.setPrice(price);
			orderBuilder.setTotalVolume(totalVolume);
			orderBuilder.setTradedVolume(tradedVolume);
			orderBuilder.setTradingDay(tradingDay);
			orderBuilder.setGateway(ctpGatewayImpl.getGateway());

			if (pRspInfo != null && pRspInfo.getErrorMsg() != null) {
				orderBuilder.setStatusInfo(pRspInfo.getErrorMsg());
			}

			if (instrumentQueried && ctpGatewayImpl.contractMap.containsKey(symbol)) {
				orderBuilder.setContract(ctpGatewayImpl.contractMap.get(symbol));
				OrderField order = orderBuilder.build();
				orderMap.put(order.getOrderId(), order);
				ctpGatewayImpl.emitOrder(order);
			} else {
				ContractField.Builder contractBuilder = ContractField.newBuilder();
				contractBuilder.setSymbol(symbol);

				orderBuilder.setContract(contractBuilder);

				orderBuilderCacheList.add(orderBuilder);
			}
			
			logger.error("{}交易接口发单错误回报（交易所） 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
			
			if(instrumentQueried) {
				NoticeField.Builder  noticeBuilder = NoticeField.newBuilder();
				noticeBuilder.setContent(logInfo+"}交易接口发单错误回报（交易所） 错误ID:"+pRspInfo.getErrorID()+",错误信息:"+pRspInfo.getErrorMsg());
				noticeBuilder.setStatus(CommonStatusEnum.ERROR);
				noticeBuilder.setTimestamp(System.currentTimeMillis());
				ctpGatewayImpl.emitNotice(noticeBuilder.build());
			}
			
		} catch (Throwable t) {
			logger.error("{}OnErrRtnOrderInsert Exception",logInfo,t);
		}


	}

	// 撤单错误回报（交易所）
	public void OnErrRtnOrderAction(CThostFtdcOrderActionField pOrderAction, CThostFtdcRspInfoField pRspInfo) {
		if (pRspInfo != null) {
			logger.error("{}交易接口撤单错误(交易所) 错误ID:{},错误信息:{}", logInfo, pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
			if(instrumentQueried) {
				NoticeField.Builder  noticeBuilder = NoticeField.newBuilder();
				noticeBuilder.setContent(logInfo+"交易接口撤单错误回报(交易所) 错误ID:"+pRspInfo.getErrorID()+",错误信息:"+pRspInfo.getErrorMsg());
				noticeBuilder.setStatus(CommonStatusEnum.ERROR);
				noticeBuilder.setTimestamp(System.currentTimeMillis());
				ctpGatewayImpl.emitNotice(noticeBuilder.build());
			}
		} else {
			logger.error("{}处理交易接口撤单错误(交易所)错误,无有效信息", logInfo);
		}
	}

	public void OnRtnInstrumentStatus(CThostFtdcInstrumentStatusField pInstrumentStatus) {
	}

	public void OnRtnBulletin(CThostFtdcBulletinField pBulletin) {
	}

	public void OnRtnTradingNotice(CThostFtdcTradingNoticeInfoField pTradingNoticeInfo) {
	}

	public void OnRtnErrorConditionalOrder(CThostFtdcErrorConditionalOrderField pErrorConditionalOrder) {
	}

	public void OnRtnExecOrder(CThostFtdcExecOrderField pExecOrder) {
	}

	public void OnErrRtnExecOrderInsert(CThostFtdcInputExecOrderField pInputExecOrder,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnExecOrderAction(CThostFtdcExecOrderActionField pExecOrderAction,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnForQuoteInsert(CThostFtdcInputForQuoteField pInputForQuote, CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnRtnQuote(CThostFtdcQuoteField pQuote) {
	}

	public void OnErrRtnQuoteInsert(CThostFtdcInputQuoteField pInputQuote, CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnQuoteAction(CThostFtdcQuoteActionField pQuoteAction, CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnRtnForQuoteRsp(CThostFtdcForQuoteRspField pForQuoteRsp) {
	}

	public void OnRtnCFMMCTradingAccountToken(CThostFtdcCFMMCTradingAccountTokenField pCFMMCTradingAccountToken) {
	}

	public void OnErrRtnBatchOrderAction(CThostFtdcBatchOrderActionField pBatchOrderAction,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnRtnOptionSelfClose(CThostFtdcOptionSelfCloseField pOptionSelfClose) {
	}

	public void OnErrRtnOptionSelfCloseInsert(CThostFtdcInputOptionSelfCloseField pInputOptionSelfClose,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnOptionSelfCloseAction(CThostFtdcOptionSelfCloseActionField pOptionSelfCloseAction,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnRtnCombAction(CThostFtdcCombActionField pCombAction) {
	}

	public void OnErrRtnCombActionInsert(CThostFtdcInputCombActionField pInputCombAction,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnRspQryContractBank(CThostFtdcContractBankField pContractBank, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryParkedOrder(CThostFtdcParkedOrderField pParkedOrder, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryParkedOrderAction(CThostFtdcParkedOrderActionField pParkedOrderAction,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryTradingNotice(CThostFtdcTradingNoticeField pTradingNotice, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryBrokerTradingParams(CThostFtdcBrokerTradingParamsField pBrokerTradingParams,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQryBrokerTradingAlgos(CThostFtdcBrokerTradingAlgosField pBrokerTradingAlgos,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRspQueryCFMMCTradingAccountToken(
			CThostFtdcQueryCFMMCTradingAccountTokenField pQueryCFMMCTradingAccountToken,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRtnFromBankToFutureByBank(CThostFtdcRspTransferField pRspTransfer) {
	}

	public void OnRtnFromFutureToBankByBank(CThostFtdcRspTransferField pRspTransfer) {
	}

	public void OnRtnRepealFromBankToFutureByBank(CThostFtdcRspRepealField pRspRepeal) {
	}

	public void OnRtnRepealFromFutureToBankByBank(CThostFtdcRspRepealField pRspRepeal) {
	}

	public void OnRtnFromBankToFutureByFuture(CThostFtdcRspTransferField pRspTransfer) {
	}

	public void OnRtnFromFutureToBankByFuture(CThostFtdcRspTransferField pRspTransfer) {
	}

	public void OnRtnRepealFromBankToFutureByFutureManual(CThostFtdcRspRepealField pRspRepeal) {
	}

	public void OnRtnRepealFromFutureToBankByFutureManual(CThostFtdcRspRepealField pRspRepeal) {
	}

	public void OnRtnQueryBankBalanceByFuture(CThostFtdcNotifyQueryAccountField pNotifyQueryAccount) {
	}

	public void OnErrRtnBankToFutureByFuture(CThostFtdcReqTransferField pReqTransfer, CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnFutureToBankByFuture(CThostFtdcReqTransferField pReqTransfer, CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnRepealBankToFutureByFutureManual(CThostFtdcReqRepealField pReqRepeal,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnRepealFutureToBankByFutureManual(CThostFtdcReqRepealField pReqRepeal,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnErrRtnQueryBankBalanceByFuture(CThostFtdcReqQueryAccountField pReqQueryAccount,
			CThostFtdcRspInfoField pRspInfo) {
	}

	public void OnRtnRepealFromBankToFutureByFuture(CThostFtdcRspRepealField pRspRepeal) {
	}

	public void OnRtnRepealFromFutureToBankByFuture(CThostFtdcRspRepealField pRspRepeal) {
	}

	public void OnRspFromBankToFutureByFuture(CThostFtdcReqTransferField pReqTransfer, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspFromFutureToBankByFuture(CThostFtdcReqTransferField pReqTransfer, CThostFtdcRspInfoField pRspInfo,
			int nRequestID, boolean bIsLast) {
	}

	public void OnRspQueryBankAccountMoneyByFuture(CThostFtdcReqQueryAccountField pReqQueryAccount,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	public void OnRtnOpenAccountByBank(CThostFtdcOpenAccountField pOpenAccount) {
	}

	public void OnRtnCancelAccountByBank(CThostFtdcCancelAccountField pCancelAccount) {
	}

	public void OnRtnChangeAccountByBank(CThostFtdcChangeAccountField pChangeAccount) {
	}
}