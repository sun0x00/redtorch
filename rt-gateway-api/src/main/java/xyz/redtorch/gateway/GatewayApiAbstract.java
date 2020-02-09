package xyz.redtorch.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.common.service.FastEventService;

import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.GatewaySettingField;
import xyz.redtorch.pb.CoreField.NoticeField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

public abstract class GatewayApiAbstract implements GatewayApi {

	private static final Logger logger = LoggerFactory.getLogger(GatewayApiAbstract.class);

	protected String gatewayId;
	protected String gatewayName;
	protected String logInfo;
	private boolean autoErrorFlag = false;
	protected long lastConnectBeginTimestamp = 0;

	protected GatewaySettingField gatewaySetting;

	private FastEventService fastEventService;

	private GatewayField gateway;

	public GatewayApiAbstract(FastEventService fastEventService, GatewaySettingField gatewaySetting) {
		this.fastEventService = fastEventService;
		this.gatewaySetting = gatewaySetting;
		this.gatewayId = gatewaySetting.getGatewayId();
		this.gatewayName = gatewaySetting.getGatewayName();
		this.logInfo = "网关ID-[" + gatewayId + "] 名称-[" + gatewayName + "] [→] ";

		GatewayField.Builder gatewayBuilder = GatewayField.newBuilder();
		gatewayBuilder.setDescription(gatewaySetting.getGatewayDescription());
		gatewayBuilder.setGatewayAdapterType(gatewaySetting.getGatewayAdapterType());
		gatewayBuilder.setGatewayType(gatewaySetting.getGatewayType());
		gatewayBuilder.setGatewayId(gatewaySetting.getGatewayId());
		gatewayBuilder.setName(gatewaySetting.getGatewayName());
		gatewayBuilder.setStatus(gatewaySetting.getStatus());
		gateway = gatewayBuilder.build();
		logger.info(logInfo + "开始初始化");

	}

	@Override
	public boolean getAuthErrorFlag() {
		return autoErrorFlag;
	}

	@Override
	public void setAuthErrorFlag(boolean authErrorFlag) {
		this.autoErrorFlag = authErrorFlag;
	}

	@Override
	public long getLastConnectBeginTimestamp() {
		return this.lastConnectBeginTimestamp;
	}

	@Override
	public GatewaySettingField getGatewaySetting() {
		return gatewaySetting;
	}

	@Override
	public String getGatewayId() {
		return gatewayId;
	}

	@Override
	public String getGatewayName() {
		return gatewayName;
	}

	@Override
	public String getLogInfo() {
		return this.logInfo;
	}

	@Override
	public GatewayField getGateway() {
		return gateway;
	}

	@Override
	public void emitPosition(PositionField position) {
		fastEventService.emitPosition(position);
	}

	@Override
	public void emitAccount(AccountField account) {
		fastEventService.emitAccount(account);
	}

	@Override
	public void emitContract(ContractField contract) {
		fastEventService.emitContract(contract);
	}

	@Override
	public void emitTick(TickField tick) {
		fastEventService.emitTick(tick);
	}

	@Override
	public void emitTrade(TradeField trade) {
		fastEventService.emitTrade(trade);
	}

	@Override
	public void emitOrder(OrderField order) {
		fastEventService.emitOrder(order);
	}

	@Override
	public void emitNotice(NoticeField notice) {
		fastEventService.emitNotice(notice);
	}

}
