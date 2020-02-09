package xyz.redtorch.common.util.bar;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

/**
 * 1分钟Bar生成器
 */
public class BarGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(BarGenerator.class);

	private BarField.Builder barBuilder = null;
	private LocalDateTime barLocalDateTime = null;

	private TickField preTick = null;
	private LocalDateTime lastTickLocalDateTime = null;

	private boolean newFlag = false;

	private String barUnifiedSymbol = null;
	
	CommonBarCallBack commonBarCallBack;

	public BarGenerator(CommonBarCallBack commonBarCallBack) {
		this.commonBarCallBack = commonBarCallBack;
	}

	/**
	 * 更新Tick数据
	 * 
	 * @param tick
	 */
	public void updateTick(TickField tick) {

		// 如果tick为空或者合约不匹配则返回
		if (tick == null) {
			logger.warn("输入的Tick数据为空,当前Bar合约{}",barUnifiedSymbol);
			return;
		}

		if (barUnifiedSymbol == null) {
			barUnifiedSymbol = tick.getUnifiedSymbol();
		} else if (!barUnifiedSymbol.equals(tick.getUnifiedSymbol())) {
			logger.warn("合约不匹配,当前Bar合约{}",barUnifiedSymbol);
			return;
		}

		LocalDateTime tickLocalDateTime = CommonUtils.millsToLocalDateTime(tick.getActionTimestamp());

		// 此处过滤用于一个策略在多个网关订阅了同一个合约的情况下,Tick到达顺序和实际产生顺序不一致或者重复的情况
		if (lastTickLocalDateTime != null && tickLocalDateTime.isBefore(lastTickLocalDateTime)) {
			logger.warn("时间乱序,当前Bar合约{}",barUnifiedSymbol);
			return;
		}

		lastTickLocalDateTime = tickLocalDateTime;

		if (preTick != null) {
			// 如果切换交易日
			if (!preTick.getTradingDay().equals(tick.getTradingDay())) {
				preTick = null;
				if(barBuilder!=null){
					finish();
				}
			}else if(!preTick.getActionDay().equals(tick.getActionDay())) {
				if(barBuilder!=null){
					finish();
				}
			}
		}

		if (barBuilder == null) {
			barBuilder = BarField.newBuilder();
			newFlag = true;
		} else if (barLocalDateTime.get(ChronoField.MINUTE_OF_DAY) != tickLocalDateTime.get(ChronoField.MINUTE_OF_DAY) 
				|| (preTick != null && !preTick.getTradingDay().equals(tick.getTradingDay()))) {
			finish();
			barBuilder = BarField.newBuilder();
		} else {
			newFlag = false;
		}

		if (newFlag) {
			barBuilder.setUnifiedSymbol(tick.getUnifiedSymbol());
			barBuilder.setGatewayId(tick.getGatewayId());
			barBuilder.setTradingDay(tick.getTradingDay());
			barBuilder.setActionDay(tick.getActionDay());

			barBuilder.setOpenPrice(tick.getLastPrice());
			barBuilder.setHighPrice(tick.getLastPrice());
			barBuilder.setLowPrice(tick.getLastPrice());

			barLocalDateTime = tickLocalDateTime;
		} else {
			// 当日最高价发生变动
			if (preTick != null && !CommonUtils.isEquals(tick.getHighPrice(), preTick.getHighPrice())) {
				barBuilder.setHighPrice(tick.getHighPrice());
			} else {
				barBuilder.setHighPrice(Math.max(barBuilder.getHighPrice(), tick.getLastPrice()));
			}

			// 当日最低价发生变动
			if (preTick != null && !CommonUtils.isEquals(tick.getLowPrice(), preTick.getLowPrice())) {
				barBuilder.setLowPrice(tick.getLowPrice());
			} else {
				barBuilder.setLowPrice(Math.min(barBuilder.getLowPrice(), tick.getLastPrice()));
			}

		}

		barBuilder.setClosePrice(tick.getLastPrice());
		barBuilder.setOpenInterest(tick.getOpenInterest());
		barBuilder.setVolume(tick.getVolume());
		barBuilder.setTurnover(tick.getTurnover());

		if (preTick != null) {
			barBuilder.setVolumeDelta(tick.getVolume() - preTick.getVolume() + barBuilder.getVolumeDelta());
			barBuilder.setTurnoverDelta(tick.getTurnover() - preTick.getTurnover() + barBuilder.getTurnoverDelta());
			barBuilder.setOpenInterestDelta(tick.getOpenInterest() - preTick.getOpenInterest() + barBuilder.getOpenInterestDelta());
		} else {
			barBuilder.setVolumeDelta(tick.getVolume());
			barBuilder.setTurnoverDelta(tick.getTurnover());
			barBuilder.setOpenInterestDelta(tick.getOpenInterest()-tick.getPreOpenInterest());
		}

		preTick = tick;
	}

	public void finish() {
		if(barBuilder!=null&&barLocalDateTime!=null) {
			barLocalDateTime = barLocalDateTime.withSecond(0).withNano(0);
			barBuilder.setActionTimestamp(CommonUtils.localDateTimeToMills(barLocalDateTime));
			barBuilder.setActionTime(barLocalDateTime.format(CommonConstant.T_FORMAT_WITH_MS_INT_FORMATTER));

			// 回调OnBar方法
			commonBarCallBack.call(barBuilder.build());
		}
		
		barLocalDateTime = null;
		barBuilder = null;
		newFlag = true;
	}

}
