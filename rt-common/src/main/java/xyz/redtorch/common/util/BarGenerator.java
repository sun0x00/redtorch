package xyz.redtorch.common.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

/**
 * 1分钟Bar生成器
 */
public class BarGenerator {

	private BarField.Builder barBuilder = null;
	private LocalDateTime barLocalDateTime = null;

	private TickField lastTick = null;
	private LocalDateTime lastTickLocalDateTime = null;
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

		boolean newFlag = false;
		LocalDateTime tickLocalDateTime = null;
		if (lastTick != null) {
			tickLocalDateTime = CommonUtils.millsToLocalDateTime(tick.getActionTimestamp());
			// 此处过滤用于一个策略在多个网关订阅了同一个合约的情况下,Tick到达顺序和实际产生顺序不一致或者重复的情况
			if (tickLocalDateTime.isBefore(lastTickLocalDateTime)) {
				return;
			}
		}

		lastTickLocalDateTime = tickLocalDateTime;

		if (barBuilder == null) {
			barBuilder = BarField.newBuilder();
			newFlag = true;
		} else if (barLocalDateTime.get(ChronoField.MINUTE_OF_DAY) != tickLocalDateTime
				.get(ChronoField.MINUTE_OF_DAY)) {
			barLocalDateTime = barLocalDateTime.withSecond(0).withNano(0);
			barBuilder.setActionTimestamp(CommonUtils.localDateTimeToMills(barLocalDateTime));
			barBuilder.setActionTime(barLocalDateTime.format(CommonConstant.T_FORMAT_WITH_MS_INT_FORMATTER));
			barBuilder.setActionDay(barLocalDateTime.format(CommonConstant.D_FORMAT_INT_FORMATTER));

			// 回调OnBar方法
			commonBarCallBack.call(barBuilder.build());

			barBuilder = BarField.newBuilder();
			newFlag = true;
		}

		if (newFlag) {
			barBuilder.setContract(tick.getContract());
			barBuilder.setDataSourceId(tick.getDataSourceId());
			barBuilder.setTradingDay(tick.getTradingDay());
			barBuilder.setActionDay(tick.getActionDay());

			barBuilder.setOpenPrice(tick.getLastPrice());
			barBuilder.setHighPrice(tick.getLastPrice());
			barBuilder.setLowPrice(tick.getLastPrice());

			barLocalDateTime = tickLocalDateTime;
		} else {
			barBuilder.setHighPrice(Math.max(barBuilder.getHighPrice(), tick.getLastPrice()));
			barBuilder.setLowPrice(Math.min(barBuilder.getLowPrice(), tick.getLastPrice()));
		}

		barBuilder.setClosePrice(tick.getLastPrice());
		barBuilder.setOpenInterest(tick.getOpenInterest());
		barBuilder.setVolume(tick.getVolume());
		barBuilder.setTurnover(tick.getTurnover());

		// 这样做可以避免一些交易日变动导致的负数问题
		if (lastTick != null) {
			barBuilder.setVolumeChange(barBuilder.getVolumeChange() + (tick.getVolume() - lastTick.getVolume()));
			barBuilder
					.setTurnoverChange(barBuilder.getTurnoverChange() + (tick.getTurnover() - lastTick.getTurnover()));
			barBuilder.setOpenInterestChange(
					barBuilder.getOpenInterestChange() + (tick.getOpenInterest() - lastTick.getOpenInterest()));
		}

		lastTick = tick;
	}

	/**
	 * CallBack接口,用于注册Bar生成器回调事件
	 */
	public static interface CommonBarCallBack {
		void call(BarField bar);
	}

	/**
	 * X分钟Bar生成器,xMin在策略初始化时指定,当值大于1小于时生效,建议此数值不要大于120
	 */
	public static class XMinBarGenerator {

		private int xMin;
		private BarField.Builder xMinBarBuilder = null;
		private LocalDateTime xMinBarLocalDateTime = null;
		CommonBarCallBack commonBarCallBack;

		public XMinBarGenerator(int xMin, CommonBarCallBack commonBarCallBack) {
			this.commonBarCallBack = commonBarCallBack;
			this.xMin = xMin;
		}

		public void updateBar(BarField bar) {

			if (xMinBarBuilder == null) {
				xMinBarBuilder = BarField.newBuilder();
				xMinBarBuilder.setContract(bar.getContract());
				xMinBarBuilder.setDataSourceId(bar.getDataSourceId());

				xMinBarBuilder.setTradingDay(bar.getTradingDay());
				xMinBarBuilder.setActionDay(bar.getActionDay());

				xMinBarBuilder.setOpenPrice(bar.getOpenPrice());
				xMinBarBuilder.setHighPrice(bar.getHighPrice());
				xMinBarBuilder.setLowPrice(bar.getLowPrice());

				xMinBarLocalDateTime = CommonUtils.millsToLocalDateTime(bar.getActionTimestamp());

			} else {
				xMinBarBuilder.setHighPrice(Math.max(xMinBarBuilder.getHighPrice(), bar.getHighPrice()));
				xMinBarBuilder.setLowPrice(Math.min(xMinBarBuilder.getLowPrice(), bar.getLowPrice()));
			}

			xMinBarBuilder.setClosePrice(bar.getClosePrice());
			xMinBarBuilder.setOpenInterest(bar.getOpenInterest());
			xMinBarBuilder.setVolumeChange(xMinBarBuilder.getVolumeChange() + bar.getVolumeChange());
			xMinBarBuilder.setTurnoverChange(xMinBarBuilder.getTurnoverChange() + bar.getTurnoverChange());
			xMinBarBuilder.setOpenInterestChange(xMinBarBuilder.getOpenInterestChange() + bar.getOpenInterestChange());

			if ((xMinBarLocalDateTime.get(ChronoField.MINUTE_OF_DAY) + 1) % xMin == 0) {

				xMinBarBuilder
						.setActionTime(xMinBarLocalDateTime.format(CommonConstant.T_FORMAT_WITH_MS_INT_FORMATTER));
				xMinBarBuilder.setActionTimestamp(CommonUtils.localDateTimeToMills(xMinBarLocalDateTime));

				// 回调onXMinBar方法
				commonBarCallBack.call(xMinBarBuilder.build());

				xMinBarBuilder = null;
			}

		}
	}
}
