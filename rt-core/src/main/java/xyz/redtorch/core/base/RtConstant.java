package xyz.redtorch.core.base;

import java.util.HashSet;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
/**
 * @author sun0x00@gmail.com
 */
public interface RtConstant {

	// 方向常量
	static final String DIRECTION_NONE = "无方向";
	static final String DIRECTION_LONG = "多";
	static final String DIRECTION_SHORT = "空";
	static final String DIRECTION_UNKNOWN = "未知";
	static final String DIRECTION_NET = "净";
	static final String DIRECTION_SELL = "卖出"; // IB接口
	static final String DIRECTION_COVEREDSHORT = "备兑空"; // 证券期权

	// 开平常量
	static final String OFFSET_NONE = "无开平";
	static final String OFFSET_OPEN = "开仓";
	static final String OFFSET_CLOSE = "平仓";
	static final String OFFSET_CLOSETODAY = "平今";
	static final String OFFSET_CLOSEYESTERDAY = "平昨";
	static final String OFFSET_UNKNOWN = "未知";

	// 状态常量
	static final String STATUS_NOTTRADED = "未成交";
	static final String STATUS_PARTTRADED = "部分成交";
	static final String STATUS_ALLTRADED = "全部成交";
	static final String STATUS_CANCELLED = "已撤销";
	static final String STATUS_REJECTED = "拒单";
	static final String STATUS_UNKNOWN = "未知";
	
	static HashSet<String> STATUS_FINISHED = new HashSet<String>() {
		private static final long serialVersionUID = 8777691797309945190L;
		{
			add(RtConstant.STATUS_REJECTED);
			add(RtConstant.STATUS_CANCELLED);
			add(RtConstant.STATUS_ALLTRADED);
		}
	};
	
	static HashSet<String> STATUS_WORKING = new HashSet<String>() {
		private static final long serialVersionUID = 909683985291870766L;
		{
			add(RtConstant.STATUS_UNKNOWN);
			add(RtConstant.STATUS_NOTTRADED);
			add(RtConstant.STATUS_PARTTRADED);
		}
	};

	// 合约类型常量
	static final String PRODUCT_EQUITY = "股票";
	static final String PRODUCT_FUTURES = "期货";
	static final String PRODUCT_OPTION = "期权";
	static final String PRODUCT_INDEX = "指数";
	static final String PRODUCT_COMBINATION = "组合";
	static final String PRODUCT_FOREX = "外汇";
	static final String PRODUCT_UNKNOWN = "未知";
	static final String PRODUCT_SPOT = "现货";
	static final String PRODUCT_DEFER = "延期";
	static final String PRODUCT_ETF = "ETF";
	static final String PRODUCT_WARRANT = "权证";
	static final String PRODUCT_BOND = "债券";
	static final String PRODUCT_NONE = "";

	// 价格类型常量
	static final String PRICETYPE_LIMITPRICE = "限价";
	static final String PRICETYPE_MARKETPRICE = "市价";
	static final String PRICETYPE_FAK = "FAK";
	static final String PRICETYPE_FOK = "FOK";

	// 期权类型
	static final String OPTION_CALL = "看涨期权";
	static final String OPTION_PUT = "看跌期权";

	// 交易所类型
	static final String EXCHANGE_SSE = "SSE"; // 上交所
	static final String EXCHANGE_SZSE = "SZSE"; // 深交所
	static final String EXCHANGE_CFFEX = "CFFEX"; // 中金所
	static final String EXCHANGE_SHFE = "SHFE"; // 上期所
	static final String EXCHANGE_CZCE = "CZCE"; // 郑商所
	static final String EXCHANGE_DCE = "DCE"; // 大商所
	static final String EXCHANGE_SGE = "SGE"; // 上金所
	static final String EXCHANGE_INE = "INE"; // 国际能源交易中心
	static final String EXCHANGE_UNKNOWN = "UNKNOWN";// 未知交易所
	static final String EXCHANGE_NONE = ""; // 空交易所
	static final String EXCHANGE_HKEX = "HKEX"; // 港交所
	static final String EXCHANGE_HKFE = "HKFE"; // 香港期货交易所

	static final String EXCHANGE_SMART = "SMART"; // IB智能路由（股票、期权）
	static final String EXCHANGE_NYMEX = "NYMEX"; // IB 期货
	static final String EXCHANGE_GLOBEX = "GLOBEX"; // CME电子交易平台
	static final String EXCHANGE_IDEALPRO = "IDEALPRO"; // IB外汇ECN

	static final String EXCHANGE_CME = "CME"; // CME交易所
	static final String EXCHANGE_ICE = "ICE"; // ICE交易所
	static final String EXCHANGE_LME = "LME"; // LME交易所

	static final String EXCHANGE_OANDA = "OANDA"; // OANDA外汇做市商
	static final String EXCHANGE_FXCM = "FXCM"; // FXCM外汇做市商

	static final String EXCHANGE_OKCOIN = "OKCOIN"; // OKCOIN比特币交易所
	static final String EXCHANGE_HUOBI = "HUOBI"; // 火币比特币交易所
	static final String EXCHANGE_LBANK = "LBANK"; // LBANK比特币交易所
	static final String EXCHANGE_KORBIT = "KORBIT"; // KORBIT韩国交易所
	static final String EXCHANGE_ZB = "ZB"; // 比特币中国比特币交易所
	static final String EXCHANGE_OKEX = "OKEX"; // OKEX比特币交易所
	static final String EXCHANGE_ZAIF = "ZAIF"; // ZAIF日本比特币交易所
	static final String EXCHANGE_COINCHECK = "COINCHECK"; // COINCHECK日本比特币交易所

	// 货币类型
	static final String CURRENCY_USD = "USD"; // 美元
	static final String CURRENCY_CNY = "CNY"; // 人民币
	static final String CURRENCY_HKD = "HKD"; // 港币
	static final String CURRENCY_UNKNOWN = "UNKNOWN"; // 未知货币
	static final String CURRENCY_NONE = ""; // 空货币

	// 接口类型
	static final String GATEWAYTYPE_EQUITY = "equity"; // 股票、ETF、债券
	static final String GATEWAYTYPE_FUTURES = "futures"; // 期货、期权、贵金属
	static final String GATEWAYTYPE_INTERNATIONAL = "international"; // 外盘
	static final String GATEWAYTYPE_BTC = "btc"; // 比特币
	static final String GATEWAYTYPE_DATA = "data"; // 数据（非交易）

	static final String RED_TORCH_DB_NAME = "redtorch_j_db";
	static final String LOG_DEBUG = "DEBUG";
	static final String LOG_INFO = "INFO";
	static final String LOG_WARN = "WARN";
	static final String LOG_ERROR = "ERROR";

	static final String DT_FORMAT_WITH_MS = "yyyy-MM-dd HH:mm:ss.SSS";
	static final String DT_FORMAT_WITH_MS_INT = "yyyyMMddHHmmssSSS";
	static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	static final String DT_FORMAT_INT = "yyyyMMddHHmmss";

	static final String T_FORMAT_WITH_MS_INT = "HHmmssSSS";
	static final String T_FORMAT_WITH_MS = "HH:mm:ss.SSS";
	static final String T_FORMAT_INT = "HHmmss";
	static final String T_FORMAT = "HH:mm:ss";
	static final String D_FORMAT_INT = "yyyyMMdd";
	static final String D_FORMAT = "yyyy-MM-dd";

	static final DateTimeFormatter DT_FORMAT_WITH_MS_FORMATTER = DateTimeFormat
			.forPattern(RtConstant.DT_FORMAT_WITH_MS);
	static final DateTimeFormatter DT_FORMAT_WITH_MS_INT_FORMATTER = DateTimeFormat
			.forPattern(RtConstant.DT_FORMAT_WITH_MS_INT);
	static final DateTimeFormatter DT_FORMAT_FORMATTER = DateTimeFormat.forPattern(RtConstant.DT_FORMAT);
	static final DateTimeFormatter DT_FORMAT_INT_FORMATTER = DateTimeFormat.forPattern(RtConstant.DT_FORMAT_INT);

	static final DateTimeFormatter T_FORMAT_WITH_MS_INT_FORMATTER = DateTimeFormat
			.forPattern(RtConstant.T_FORMAT_WITH_MS_INT);
	static final DateTimeFormatter T_FORMAT_WITH_MS_FORMATTER = DateTimeFormat.forPattern(RtConstant.T_FORMAT_WITH_MS);
	static final DateTimeFormatter T_FORMAT_INT_FORMATTER = DateTimeFormat.forPattern(RtConstant.T_FORMAT_INT);
	static final DateTimeFormatter T_FORMAT_FORMATTER = DateTimeFormat.forPattern(RtConstant.T_FORMAT);

	static final DateTimeFormatter D_FORMAT_INT_FORMATTER = DateTimeFormat.forPattern(RtConstant.D_FORMAT_INT);
	static final DateTimeFormatter D_FORMAT_FORMATTER = DateTimeFormat.forPattern(RtConstant.D_FORMAT);
}
