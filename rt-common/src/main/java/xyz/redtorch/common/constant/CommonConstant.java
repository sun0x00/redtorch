package xyz.redtorch.common.constant;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import xyz.redtorch.pb.CoreEnum.OrderStatusEnum;

public interface CommonConstant {

	String KEY_USER_PO = "userPo";
	String KEY_OPERATOR_ID = "operatorId";
	String KEY_NODE_ID = "nodeId";
	String KEY_AUTH_TOKEN = "Auth-Token";
	String KEY_VERIFIED = "verified";
	String KEY_WEBSOCKET_SESSION_ID = "websocketSessionId";

	String SECURITY_MASK = "********";

	String DT_FORMAT_WITH_MS = "yyyy-MM-dd HH:mm:ss.SSS";
	String DT_FORMAT_WITH_MS_INT = "yyyyMMddHHmmssSSS";
	String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	String DT_FORMAT_INT = "yyyyMMddHHmmss";

	String T_FORMAT_WITH_MS_INT = "HHmmssSSS";
	String T_FORMAT_WITH_MS = "HH:mm:ss.SSS";
	String T_FORMAT_INT = "HHmmss";
	String T_FORMAT = "HH:mm:ss";
	String D_FORMAT_INT = "yyyyMMdd";
	String D_FORMAT = "yyyy-MM-dd";

	DateTimeFormatter DT_FORMAT_WITH_MS_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT_WITH_MS);
	DateTimeFormatter DT_FORMAT_WITH_MS_INT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT_WITH_MS_INT);
	DateTimeFormatter DT_FORMAT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);
	DateTimeFormatter DT_FORMAT_INT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT_INT);

	DateTimeFormatter T_FORMAT_WITH_MS_INT_FORMATTER = DateTimeFormatter.ofPattern(T_FORMAT_WITH_MS_INT);
	DateTimeFormatter T_FORMAT_WITH_MS_FORMATTER = DateTimeFormatter.ofPattern(T_FORMAT_WITH_MS);
	DateTimeFormatter T_FORMAT_INT_FORMATTER = DateTimeFormatter.ofPattern(T_FORMAT_INT);
	DateTimeFormatter T_FORMAT_FORMATTER = DateTimeFormatter.ofPattern(T_FORMAT);

	DateTimeFormatter D_FORMAT_INT_FORMATTER = DateTimeFormatter.ofPattern(D_FORMAT_INT);
	DateTimeFormatter D_FORMAT_FORMATTER = DateTimeFormatter.ofPattern(D_FORMAT);

	HashSet<OrderStatusEnum> ORDER_STATUS_FINISHED_SET = new HashSet<>() {
		private static final long serialVersionUID = 909683985291870766L;

		{
			add(OrderStatusEnum.OS_Rejected);
			add(OrderStatusEnum.OS_Canceled);
			add(OrderStatusEnum.OS_AllTraded);
			add(OrderStatusEnum.OS_Touched);
		}
	};

	HashSet<OrderStatusEnum> ORDER_STATUS_WORKING_SET = new HashSet<>() {
		private static final long serialVersionUID = 909683985291870766L;

		{
			add(OrderStatusEnum.OS_Unknown);
			add(OrderStatusEnum.OS_NoTradeQueueing);
			add(OrderStatusEnum.OS_NoTradeNotQueueing);
			add(OrderStatusEnum.OS_PartTradedNotQueueing);
			add(OrderStatusEnum.OS_PartTradedQueueing);
			add(OrderStatusEnum.OS_NotTouched);
		}
	};
}
