package xyz.redtorch.core.base;

import java.util.HashSet;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author sun0x00@gmail.com
 */
public interface RtConstant {

	// 方向常量
	static final String DIRECTION_NONE = "NONE";// 无方向
	static final String DIRECTION_LONG = "LONG"; // 多
	static final String DIRECTION_SHORT = "SHORT"; // 空
	static final String DIRECTION_UNKNOWN = "UNKNOWN"; // 未知
	static final String DIRECTION_NET = "NET"; // 净
	static final String DIRECTION_SELL = "SELL"; // 卖出 IB
	static final String DIRECTION_COVEREDSHORT = "COVEREDSHORT"; // 备兑空 // 证券期权

	// 开平常量
	static final String OFFSET_NONE = "NONE"; // 无开平
	static final String OFFSET_OPEN = "OPEN"; // 开仓
	static final String OFFSET_CLOSE = "CLOSE"; // 平仓
	static final String OFFSET_CLOSETODAY = "CLOSETODAY"; // 平今
	static final String OFFSET_CLOSEYESTERDAY = "CLOSEYESTERDAY"; // 平昨
	static final String OFFSET_UNKNOWN = "UNKNOWN"; // 未知

	// 状态常量
	static final String STATUS_NOTTRADED = "NOTTRADED"; // 未成交
	static final String STATUS_PARTTRADED = "PARTTRADED"; // 部分成交
	static final String STATUS_ALLTRADED = "ALLTRADED"; // 全部成交
	static final String STATUS_CANCELLED = "CANCELLED"; // 已撤销
	static final String STATUS_REJECTED = "REJECTED"; // 拒单
	static final String STATUS_UNKNOWN = "UNKNOWN"; // 未知

	static final HashSet<String> STATUS_FINISHED = new HashSet<String>() {
		private static final long serialVersionUID = 8777691797309945190L;
		{
			add(RtConstant.STATUS_REJECTED);
			add(RtConstant.STATUS_CANCELLED);
			add(RtConstant.STATUS_ALLTRADED);
		}
	};

	static final HashSet<String> STATUS_WORKING = new HashSet<String>() {
		private static final long serialVersionUID = 909683985291870766L;
		{
			add(RtConstant.STATUS_UNKNOWN);
			add(RtConstant.STATUS_NOTTRADED);
			add(RtConstant.STATUS_PARTTRADED);
		}
	};

	// 合约类型常量
	static final String PRODUCT_EQUITY = "EQUITY"; // 股票
	static final String PRODUCT_FUTURES = "FUTURES"; // 期货
	static final String PRODUCT_OPTION = "OPTION"; // 期权
	static final String PRODUCT_INDEX = "INDEX"; // 指数
	static final String PRODUCT_COMBINATION = "COMBINATION"; // 组合
	static final String PRODUCT_FOREX = "FOREX"; // 外汇
	static final String PRODUCT_UNKNOWN = "UNKNOWN"; // 未知
	static final String PRODUCT_SPOT = "SPOT"; // 现货
	static final String PRODUCT_DEFER = "DEFER "; // 延期
	static final String PRODUCT_ETF = "ETF"; // ETF
	static final String PRODUCT_WARRANT = "WARRANT"; // 权证
	static final String PRODUCT_BOND = "BOND"; // 债券
	static final String PRODUCT_NONE = "NONE"; // NONE

	// 价格类型常量
	static final String PRICETYPE_LIMITPRICE = "LIMITPRICE"; // 限价
	static final String PRICETYPE_MARKETPRICE = "MARKETPRICE "; // 市价
	static final String PRICETYPE_FAK = "FAK"; // FAK
	static final String PRICETYPE_FOK = "FOK"; // FOK

	// 期权类型
	static final String OPTION_CALL = "CALL"; // 看涨期权
	static final String OPTION_PUT = "PUT"; // 看跌期权

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
	static final String EXCHANGE_SEHK = "SEHK"; // 港交所
	static final String EXCHANGE_HKFE = "HKFE"; // 香港期货交易所
	static final String EXCHANGE_SGX = "SGX"; // 新加坡交易所

	static final String EXCHANGE_SMART = "SMART"; // IB智能路由（股票、期权）
	static final String EXCHANGE_NYMEX = "NYMEX"; // IB 期货
	static final String EXCHANGE_GLOBEX = "GLOBEX"; // CME电子交易平台
	static final String EXCHANGE_IDEALPRO = "IDEALPRO"; // IB外汇ECN

	static final String EXCHANGE_CME = "CME"; // 芝商所
	static final String EXCHANGE_ICE = "ICE"; // 洲际交易所
	static final String EXCHANGE_IPE = "IPE"; // 洲际交易所
	static final String EXCHANGE_LME = "LME"; // 伦敦金属交易所

	static final String EXCHANGE_OANDA = "OANDA"; // OANDA外汇做市商
	static final String EXCHANGE_FXCM = "FXCM"; // FXCM外汇做市商

	// 货币类型
	static final String CURRENCY_USD = "USD"; // 美元
	static final String CURRENCY_CNY = "CNY"; // 人民币
	static final String CURRENCY_CNH = "CNH"; // 离岸人民币
	static final String CURRENCY_HKD = "HKD"; // 港币
	static final String CURRENCY_JPY = "JPY"; // 日元
	static final String CURRENCY_EUR = "EUR"; // 欧元
	static final String CURRENCY_GBP = "GBP"; // 英镑
	static final String CURRENCY_DEM = "DEM"; // 德国马克
	static final String CURRENCY_CHF = "CHF"; // 瑞士法郎
	static final String CURRENCY_FRF = "FRF"; // 法国法郎
	static final String CURRENCY_CAD = "CAD"; // 加拿大元
	static final String CURRENCY_AUD = "AUD"; // 澳大利亚元
	static final String CURRENCY_ATS = "ATS"; // 奥地利先令
	static final String CURRENCY_FIM = "FIM"; // 芬兰马克
	static final String CURRENCY_BEF = "BEF"; // 比利时法郎
	static final String CURRENCY_IEP = "IEP"; // 爱尔兰镑
	static final String CURRENCY_ITL = "ITL"; // 意大利里拉
	static final String CURRENCY_LUF = "LUF"; // 卢森堡法郎
	static final String CURRENCY_NLG = "NLG"; // 荷兰盾
	static final String CURRENCY_PTE = "PTE"; // 葡萄牙埃斯库多
	static final String CURRENCY_ESP = "ESP"; // 西班牙比塞塔
	static final String CURRENCY_IDR = "IDR"; // 印尼盾
	static final String CURRENCY_MYR = "MYR"; // 马来西亚林吉特
	static final String CURRENCY_NZD = "NZD"; // 新西兰元
	static final String CURRENCY_PHP = "PHP"; // 菲律宾比索
	static final String CURRENCY_SUR = "SUR"; // 俄罗斯卢布
	static final String CURRENCY_SGD = "SGD"; // 新加坡元
	static final String CURRENCY_KRW = "KRW"; // 韩国元
	static final String CURRENCY_THB = "THB"; // 泰铢

	static final String CURRENCY_UNKNOWN = "UNKNOWN"; // 未知货币
	static final String CURRENCY_NONE = ""; // 空货币

	// 网关类型
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
