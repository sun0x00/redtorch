package xyz.redtorch.core.utils;

import xyz.redtorch.core.base.RtConstant;
import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.Tick;

/**
 * 1分钟Bar生成器
 */
public class BarGenerator {

	private Bar bar = null;
	private Tick lastTick = null;
	CommonBarCallBack commonBarCallBack;

	public BarGenerator(CommonBarCallBack commonBarCallBack) {
		this.commonBarCallBack = commonBarCallBack;
	}

	/**
	 * 更新Tick数据
	 * 
	 * @param tick
	 */
	public void updateTick(Tick tick) {

		boolean newMinute = false;

		if (lastTick != null) {
			// 此处过滤用于一个策略在多个网关订阅了同一个合约的情况下,Tick到达顺序和实际产生顺序不一致或者重复的情况
			if (tick.getDateTime().getMillis() <= lastTick.getDateTime().getMillis()) {
				return;
			}
		}

		if (bar == null) {
			bar = new Bar();
			newMinute = true;
		} else if (bar.getDateTime().getMinuteOfDay() != tick.getDateTime().getMinuteOfDay()) {

			bar.setDateTime(bar.getDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
			bar.setActionTime(bar.getDateTime().toString(RtConstant.T_FORMAT_WITH_MS_FORMATTER));

			// 回调OnBar方法
			commonBarCallBack.call(bar);

			bar = new Bar();
			newMinute = true;
		}

		if (newMinute) {
			bar.setGatewayID(tick.getGatewayID());
			bar.setExchange(tick.getExchange());
			bar.setRtSymbol(tick.getRtSymbol());
			bar.setSymbol(tick.getSymbol());
			bar.setRtBarID(tick.getRtTickID());

			bar.setTradingDay(tick.getTradingDay());
			;
			bar.setActionDay(tick.getActionDay());

			bar.setOpen(tick.getLastPrice());
			bar.setHigh(tick.getLastPrice());
			bar.setLow(tick.getLastPrice());

			bar.setDateTime(tick.getDateTime());
		} else {
			bar.setHigh(Math.max(bar.getHigh(), tick.getLastPrice()));
			bar.setLow(Math.min(bar.getLow(), tick.getLastPrice()));
		}

		bar.setClose(tick.getLastPrice());
		bar.setOpenInterest(tick.getOpenInterest());
		if (lastTick != null) {
			bar.setVolume(bar.getVolume() + (tick.getVolume() - lastTick.getVolume()));
		}

		lastTick = tick;
	}
	
	/**
	 * CallBack接口,用于注册Bar生成器回调事件
	 */
	public static interface CommonBarCallBack {
		void call(Bar bar);
	}
	
	/**
	 * X分钟Bar生成器,xMin在策略初始化时指定,当值大于1小于时生效,建议此数值不要大于120
	 */
	public static class XMinBarGenerator {

		private int xMin;
		private Bar xMinBar = null;
		CommonBarCallBack commonBarCallBack;

		public XMinBarGenerator(int xMin, CommonBarCallBack commonBarCallBack) {
			this.commonBarCallBack = commonBarCallBack;
			this.xMin = xMin;
		}

		public void updateBar(Bar bar) {

			if (xMinBar == null) {
				xMinBar = new Bar();
				xMinBar.setGatewayID(bar.getGatewayID());
				xMinBar.setExchange(bar.getExchange());
				xMinBar.setRtSymbol(bar.getRtSymbol());
				xMinBar.setSymbol(bar.getSymbol());
				xMinBar.setRtBarID(bar.getRtBarID());

				xMinBar.setTradingDay(bar.getTradingDay());
				xMinBar.setActionDay(bar.getActionDay());

				xMinBar.setOpen(bar.getOpen());
				xMinBar.setHigh(bar.getHigh());
				xMinBar.setLow(bar.getLow());

			} else {
				xMinBar.setHigh(Math.max(xMinBar.getHigh(), bar.getHigh()));
				xMinBar.setLow(Math.min(xMinBar.getLow(), bar.getLow()));
			}

			xMinBar.setDateTime(bar.getDateTime());
			xMinBar.setClose(bar.getClose());
			xMinBar.setOpenInterest(bar.getOpenInterest());
			xMinBar.setVolume(xMinBar.getVolume() + bar.getVolume());

			if ((xMinBar.getDateTime().getMinuteOfDay() + 1) % xMin == 0) {

				xMinBar.setDateTime(xMinBar.getDateTime().plusMinutes(1).withSecondOfMinute(0).withMillisOfSecond(0));
				xMinBar.setActionTime(xMinBar.getDateTime().toString(RtConstant.T_FORMAT_WITH_MS_FORMATTER));

				// 回调onXMinBar方法
				commonBarCallBack.call(xMinBar);

				xMinBar = null;
			}

		}
	}
}

