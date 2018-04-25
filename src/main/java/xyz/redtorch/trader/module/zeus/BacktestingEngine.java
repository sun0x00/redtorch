package xyz.redtorch.trader.module.zeus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.trader.base.RtConstant;
import xyz.redtorch.trader.entity.Bar;
import xyz.redtorch.trader.entity.Tick;
import xyz.redtorch.trader.entity.Trade;
import xyz.redtorch.utils.CommonUtil;


/**
 * @author sun0x00@gmail.com
 */
public interface BacktestingEngine extends ZeusEngine{
	
	final int DATA_MODE_TICK  = 0;
	final int DATA_MODE_BAR = 1;
	

	/**
	 * 运行回测
	 */
	void runBacktesting();
	

	/**
	 * 加载回测Bar数据
	 * @param startDate
	 * @param endDate
	 * @param subscribeRtSymbolList
	 * @return
	 */
	List<Bar> loadBacktestingBarDataList(String startDate, String endDate, List<String> subscribeRtSymbolList);

	/**
	 * 加载回测Tick数据
	 * @param startDate
	 * @param endDate
	 * @param subscribeRtSymbolList
	 * @return
	 */
	List<Tick> loadBacktestingTickDataList(String startDate, String endDate, List<String> subscribeRtSymbolList);

	/**
	 * 优化设置
	 * @author sun0x00@gmail.com
	 *
	 */
	static class OptimizationSetting{
		static Logger log = LoggerFactory.getLogger(BacktestingEngine.class);
		private Map<String,List<String>> paramMap;
		public void addStrParameter(String parameterName, Set<String> parameterSet) {
	        paramMap.put(parameterName, new ArrayList<String>(parameterSet));
		}
	    public void addParameter(String parameterName, double start, double end, double step) {

	        if(end < start) {
	            log.error("参数起始点必须不大于终止点");
	            return;
	        }
	        if(step <= 0) {
	            log.error("参数步进必须大于0");
	            return;
	        }

	        Set<String> parameterSet = new HashSet<>();
	        Double param = start;

	        while(param <= end) {
	        	parameterSet.add(String.valueOf(param));
	            param += step;
	        }
	        
	        paramMap.put(parameterName, new ArrayList<String>(parameterSet));
	    }
	       

	    public List<Map<String,String>> generateSetting(){
	
	        List<String> paramNameList = new ArrayList<>(paramMap.keySet());
	        List<List<String>> valuesList = new ArrayList<>(paramMap.values());

	        // 使用迭代工具生产参数对组合
	        List<List<String>> productList = CommonUtil.cartesianProduct(valuesList);

	        //把参数对组合打包到一个个字典组成的列表中
	    	List<Map<String,String>> settingList = new ArrayList<>();
	        for(List<String> paramValueList: productList) {
	        	Map<String, String> paramterMap = IntStream.range(0, paramNameList.size())
                        .collect(
                             HashMap::new, 
                             (m, i) -> m.put(paramNameList.get(i), paramValueList.get(i)), 
                             Map::putAll
                        );
	        	
	        	settingList.add(paramterMap);
	        }
	            

	        return settingList;
	    }
	    

	}
	
	/**
	 * 回测片段
	 * @author sun0x00@gmail.com
	 *
	 */
	static class BacktestingSection{
		private String startDate;
		private String endDate;
		private Map<String,String> aliasMap = new HashMap<>();
		private Map<String,List<String>> gatewaySubscribeRtSymbolsMap = new HashMap<>();
		public void addAliasRtSymbol(String alias,String rtSymbol) {
			aliasMap.put(alias, rtSymbol);
		}
		public void addSubscribeRtSymbol(String gatewayID,String rtSymbol) {
			List<String> subscribeRtSymbols;
			if(gatewaySubscribeRtSymbolsMap.containsKey(gatewayID)) {
				subscribeRtSymbols = gatewaySubscribeRtSymbolsMap.get(gatewayID);
			}else {
				subscribeRtSymbols = new ArrayList<>();
				gatewaySubscribeRtSymbolsMap.put(gatewayID,subscribeRtSymbols);
			}
			subscribeRtSymbols.add(rtSymbol);
		}
		public String getStartDate() {
			return startDate;
		}
		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}
		public String getEndDate() {
			return endDate;
		}
		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}
		public Map<String, String> getAliasMap() {
			return aliasMap;
		}
		public void setAliasMap(Map<String, String> aliasMap) {
			this.aliasMap = aliasMap;
		}
		public Map<String, List<String>> getGatewaySubscribeRtSymbolsMap() {
			return gatewaySubscribeRtSymbolsMap;
		}
		public void setGatewaySubscribeRtSymbolsMap(Map<String, List<String>> gatewaySubscribeRtSymbolsMap) {
			this.gatewaySubscribeRtSymbolsMap = gatewaySubscribeRtSymbolsMap;
		}
	}
	
	/**
	 * 回测交易清算结果
	 * @author sun0x00@gmail.com
	 *
	 */
	static class TradingResult {
		private double entryPrice; // 开仓价格
		private double exitPrice; // 平仓价格
		private DateTime entryDateTime; // 开仓时间
		private DateTime exitDateTime; // 平仓时间
		private int volume; // 交易数量 +/- 代表方向
		private String rtSymbol;
		private String gatewayID;
		private double turnover; // 成交金额
		private double commission; // 手续费
		private double slippge; // 滑点
		private double pnl; // 净盈亏

		public TradingResult(double entryPrice, DateTime entryDateTime, double exitPrice, DateTime exitDateTime,
				int volume, double rate, double slippage, int contractSize, String rtSymbol, String gatewayID) {
			this.entryPrice = entryPrice;
			this.exitPrice = exitPrice;
			this.entryDateTime = entryDateTime;
			this.exitDateTime = exitDateTime;
			this.volume = volume;
			this.rtSymbol = rtSymbol;
			this.gatewayID = gatewayID;

			this.turnover = (entryPrice + exitPrice) * contractSize * Math.abs(volume);
			this.commission = turnover * rate;
			this.slippge = slippage * 2 * contractSize * Math.abs(volume);
			this.pnl = (exitPrice - entryPrice) * volume * contractSize - this.commission - this.slippge;
		}

		public double getEntryPrice() {
			return entryPrice;
		}

		public double getExitPrice() {
			return exitPrice;
		}

		public DateTime getEntryDateTime() {
			return entryDateTime;
		}

		public DateTime getExitDateTime() {
			return exitDateTime;
		}

		public int getVolume() {
			return volume;
		}

		public String getRtSymbol() {
			return rtSymbol;
		}

		public String getGatewayID() {
			return gatewayID;
		}

		public double getTurnover() {
			return turnover;
		}

		public double getCommission() {
			return commission;
		}

		public double getSlippge() {
			return slippge;
		}

		public double getPnl() {
			return pnl;
		}

	}

	/**
	 * 回测结果
	 * @author sun0x00@gmail.com
	 *
	 */
	static class BacktestingResult{
		private String rtSymbol;
		private String gatewayID;
		private double capital;
		private double maxCapital;
		private double drawdown;
		private double totalResult;
		private double totalTurnover;
		private double totalCommission;
		private double totalSlippage;
		private List<DateTime> timeList;
		private List<Double> pnlList;
		private List<Double> capitalList;
		private List<Double> drawdownList;
		private double winningRate;
		private double averageWinning;
		private double averageLosing;
		private double profitLossRatio;
		private List<Integer> posList;
		private List<DateTime> tradeTimeList;
		private List<TradingResult> resultList;
		public String getRtSymbol() {
			return rtSymbol;
		}
		public void setRtSymbol(String rtSymbol) {
			this.rtSymbol = rtSymbol;
		}
		public String getGatewayID() {
			return gatewayID;
		}
		public void setGatewayID(String gatewayID) {
			this.gatewayID = gatewayID;
		}
		public double getCapital() {
			return capital;
		}
		public void setCapital(double capital) {
			this.capital = capital;
		}
		public double getMaxCapital() {
			return maxCapital;
		}
		public void setMaxCapital(double maxCapital) {
			this.maxCapital = maxCapital;
		}
		public double getDrawdown() {
			return drawdown;
		}
		public void setDrawdown(double drawdown) {
			this.drawdown = drawdown;
		}
		public double getTotalResult() {
			return totalResult;
		}
		public void setTotalResult(double totalResult) {
			this.totalResult = totalResult;
		}
		public double getTotalTurnover() {
			return totalTurnover;
		}
		public void setTotalTurnover(double totalTurnover) {
			this.totalTurnover = totalTurnover;
		}
		public double getTotalCommission() {
			return totalCommission;
		}
		public void setTotalCommission(double totalCommission) {
			this.totalCommission = totalCommission;
		}
		public double getTotalSlippage() {
			return totalSlippage;
		}
		public void setTotalSlippage(double totalSlippage) {
			this.totalSlippage = totalSlippage;
		}
		public List<DateTime> getTimeList() {
			return timeList;
		}
		public void setTimeList(List<DateTime> timeList) {
			this.timeList = timeList;
		}
		public List<Double> getPnlList() {
			return pnlList;
		}
		public void setPnlList(List<Double> pnlList) {
			this.pnlList = pnlList;
		}
		public List<Double> getCapitalList() {
			return capitalList;
		}
		public void setCapitalList(List<Double> capitalList) {
			this.capitalList = capitalList;
		}
		public List<Double> getDrawdownList() {
			return drawdownList;
		}
		public void setDrawdownList(List<Double> drawdownList) {
			this.drawdownList = drawdownList;
		}
		public double getWinningRate() {
			return winningRate;
		}
		public void setWinningRate(double winningRate) {
			this.winningRate = winningRate;
		}
		public double getAverageWinning() {
			return averageWinning;
		}
		public void setAverageWinning(double averageWinning) {
			this.averageWinning = averageWinning;
		}
		public double getAverageLosing() {
			return averageLosing;
		}
		public void setAverageLosing(double averageLosing) {
			this.averageLosing = averageLosing;
		}
		public double getProfitLossRatio() {
			return profitLossRatio;
		}
		public void setProfitLossRatio(double profitLossRatio) {
			this.profitLossRatio = profitLossRatio;
		}
		public List<Integer> getPosList() {
			return posList;
		}
		public void setPosList(List<Integer> posList) {
			this.posList = posList;
		}
		public List<DateTime> getTradeTimeList() {
			return tradeTimeList;
		}
		public void setTradeTimeList(List<DateTime> tradeTimeList) {
			this.tradeTimeList = tradeTimeList;
		}
		public List<TradingResult> getResultList() {
			return resultList;
		}
		public void setResultList(List<TradingResult> resultList) {
			this.resultList = resultList;
		}
	}
	
	/**
	 * 回测按日计算结果
	 * @author sun0x00@gmail.com
	 *
	 */
	static class DailyResult {

		private String date; // 日期
		private double closePrice; // 当日收盘价
		private double previousClose = 0; // 昨日收盘价

		private List<Trade> tradeList = new ArrayList<>(); // 成交列表
		private int tradeCount = 0; // 成交数量

		private int openPosition = 0; // 开盘时的持仓
		private int closePosition = 0; // 收盘时的持仓

		private double tradingPnl = 0; // 交易盈亏
		private double positionPnl = 0; // 持仓盈亏
		private double totalPnl = 0; // 总盈亏

		private double turnover = 0; // 成交额
		private double commission = 0; // 手续费
		private double totalSlippage = 0; // 滑点
		private double netPnl = 0; // 净盈亏

		private String rtSymbol;
		private String gatewayID;

		public DailyResult(String date, double closePrice, String rtSymbol, String gatewayID) {
			this.date = date;
			this.closePrice = closePrice;
			this.rtSymbol = rtSymbol;
			this.gatewayID = gatewayID;
		}

		public void addTrade(Trade trade) {
			tradeList.add(trade);
		}

		public void calculatePnl(int openPosition, int contractSize, double rate, double slippage) {
			if (contractSize == 0) {
				contractSize = 1;
			}
			// 持仓部分
			this.openPosition = openPosition;
			positionPnl = openPosition * (closePrice - previousClose) * contractSize;
			closePosition = openPosition;
			// 交易部分
			tradeCount = tradeList.size();

			for (Trade trade : tradeList) {
				int posChange;
				if (trade.getDirection().equals(RtConstant.DIRECTION_LONG)) {
					posChange = trade.getVolume();
				} else {
					posChange = -trade.getVolume();
				}
				tradingPnl += posChange * (closePrice - trade.getPrice()) * contractSize;
				closePosition += posChange;
				turnover += trade.getPrice() * trade.getVolume() * contractSize;
				commission += trade.getPrice() * trade.getVolume() * contractSize * rate;
				totalSlippage += trade.getVolume() * contractSize * slippage;
			}
			
			totalPnl = tradingPnl + positionPnl;
			netPnl = totalPnl - commission - totalSlippage;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public double getClosePrice() {
			return closePrice;
		}

		public void setClosePrice(double closePrice) {
			this.closePrice = closePrice;
		}

		public double getPreviousClose() {
			return previousClose;
		}

		public void setPreviousClose(double previousClose) {
			this.previousClose = previousClose;
		}

		public List<Trade> getTradeList() {
			return tradeList;
		}

		public void setTradeList(List<Trade> tradeList) {
			this.tradeList = tradeList;
		}

		public int getTradeCount() {
			return tradeCount;
		}

		public void setTradeCount(int tradeCount) {
			this.tradeCount = tradeCount;
		}

		public int getOpenPosition() {
			return openPosition;
		}

		public void setOpenPosition(int openPosition) {
			this.openPosition = openPosition;
		}

		public int getClosePosition() {
			return closePosition;
		}

		public void setClosePosition(int closePosition) {
			this.closePosition = closePosition;
		}

		public double getTradingPnl() {
			return tradingPnl;
		}

		public void setTradingPnl(double tradingPnl) {
			this.tradingPnl = tradingPnl;
		}

		public double getPositionPnl() {
			return positionPnl;
		}

		public void setPositionPnl(double positionPnl) {
			this.positionPnl = positionPnl;
		}

		public double getTotalPnl() {
			return totalPnl;
		}

		public void setTotalPnl(double totalPnl) {
			this.totalPnl = totalPnl;
		}

		public double getTurnover() {
			return turnover;
		}

		public void setTurnover(double turnover) {
			this.turnover = turnover;
		}

		public double getCommission() {
			return commission;
		}

		public void setCommission(double commission) {
			this.commission = commission;
		}

		public double getTotalSlippage() {
			return totalSlippage;
		}

		public void setTotalSlippage(double totalSlippage) {
			this.totalSlippage = totalSlippage;
		}

		public double getNetPnl() {
			return netPnl;
		}

		public void setNetPnl(double netPnl) {
			this.netPnl = netPnl;
		}

		public String getRtSymbol() {
			return rtSymbol;
		}

		public void setRtSymbol(String rtSymbol) {
			this.rtSymbol = rtSymbol;
		}

		public String getGatewayID() {
			return gatewayID;
		}

		public void setGatewayID(String gatewayID) {
			this.gatewayID = gatewayID;
		}

	}

	
}


