package xyz.redtorch.common.util.bar;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreField.BarField;

/**
 * X分钟Bar生成器,xMin在策略初始化时指定,当值大于1小于时生效,建议此数值不要大于120
 */
public class XMinBarGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(XMinBarGenerator.class);

	private int xMin;
	private BarField.Builder xMinBarBuilder = null;
	private LocalDateTime xMinBarLocalDateTime = null;

	private String xMinBarUnifiedSymbol = null;
	
	CommonBarCallBack commonBarCallBack;
	private LocalDateTime lastBarLocalDateTime = null;

	public XMinBarGenerator(int xMin, CommonBarCallBack commonBarCallBack) {
		if(xMin<1) {
			throw new RuntimeException("分钟周期错误");
		}
		
		if(60*24%xMin!=0) {
			logger.warn("应选取能整除60*24的参数");
		}
		
		this.commonBarCallBack = commonBarCallBack;
		this.xMin = xMin;
	}

	public void updateBar(BarField bar) {
		
		// 如果bar为空或者合约不匹配则返回
		if (bar == null) {
			logger.warn("输入的Bar数据为空,当前XMinBar合约{}",xMinBarUnifiedSymbol);
			return;
		}

		if (xMinBarUnifiedSymbol == null) {
			xMinBarUnifiedSymbol = bar.getUnifiedSymbol();
		} else if (!xMinBarUnifiedSymbol.equals(bar.getUnifiedSymbol())) {
			logger.warn("合约不匹配,当前XMinBar合约{}",xMinBarUnifiedSymbol);
			return;
		}

		LocalDateTime barLocalDateTime = CommonUtils.millsToLocalDateTime(bar.getActionTimestamp());

		if(lastBarLocalDateTime!=null && barLocalDateTime.isBefore(lastBarLocalDateTime)) {
			logger.warn("时间乱序,当前XMinBar合约{}",xMinBarUnifiedSymbol);
		}
		
		lastBarLocalDateTime = barLocalDateTime;
		
		// 如果交易日发生了变更
		if(xMinBarBuilder!=null&&!xMinBarBuilder.getTradingDay().equals(bar.getTradingDay())) {
			finish();
		}
		
		// 如果日期发生了变更
		if(xMinBarBuilder!=null&&!xMinBarBuilder.getActionDay().equals(bar.getActionDay())) {
			finish();
		}
		
		// 如果已经不属于同一个周期
		if(xMinBarBuilder!=null&&(int)xMinBarLocalDateTime.get(ChronoField.MINUTE_OF_DAY)/xMin != (int)barLocalDateTime.get(ChronoField.MINUTE_OF_DAY)/xMin) {
			finish();
		}

		if (xMinBarBuilder == null) {
			xMinBarBuilder = BarField.newBuilder();
			xMinBarBuilder.setUnifiedSymbol(bar.getUnifiedSymbol());
			xMinBarBuilder.setGatewayId(bar.getGatewayId());

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
		xMinBarBuilder.setVolume(bar.getVolume());
		xMinBarBuilder.setTurnover(bar.getTurnover());
		xMinBarBuilder.setOpenInterest(bar.getOpenInterest());
		xMinBarBuilder.setVolumeDelta(xMinBarBuilder.getVolumeDelta() + bar.getVolumeDelta());
		xMinBarBuilder.setTurnoverDelta(xMinBarBuilder.getTurnoverDelta() + bar.getTurnoverDelta());
		xMinBarBuilder.setOpenInterestDelta(xMinBarBuilder.getOpenInterestDelta() + bar.getOpenInterestDelta());

		// 如果当前周期结束
		if ((barLocalDateTime.get(ChronoField.MINUTE_OF_DAY) + 1) % xMin == 0) {
			finish();
		}
	}
	
	public void finish() {
		if(xMinBarLocalDateTime!=null&&xMinBarBuilder!=null) {
			xMinBarLocalDateTime = xMinBarLocalDateTime.minusMinutes(xMinBarLocalDateTime.get(ChronoField.MINUTE_OF_DAY)  % xMin).withSecond(0).withNano(0);
			xMinBarBuilder.setActionTime(xMinBarLocalDateTime.format(CommonConstant.T_FORMAT_WITH_MS_INT_FORMATTER));
			xMinBarBuilder.setActionTimestamp(CommonUtils.localDateTimeToMills(xMinBarLocalDateTime));

			// 回调onXMinBar方法
			commonBarCallBack.call(xMinBarBuilder.build());
		}
		
		xMinBarLocalDateTime = null;
		xMinBarBuilder = null;
	}
}