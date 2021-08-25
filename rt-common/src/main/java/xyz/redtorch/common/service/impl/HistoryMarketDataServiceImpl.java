package xyz.redtorch.common.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import xyz.redtorch.common.service.HistoryMarketDataService;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

import java.time.LocalDateTime;
import java.util.List;

public class HistoryMarketDataServiceImpl implements HistoryMarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryMarketDataServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public HistoryMarketDataServiceImpl(@Autowired JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<BarField> queryBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriodEnum) {

        String sql = "select * from tbl_bar where actionTimestamp between " +startTimestamp+ " and "+endTimestamp+ " and uniformSymbol='"+uniformSymbol+"' and period=" + barPeriodEnum.getNumber() + " order by actionTimestamp asc";

        return queryBarList(sql);
    }

    @Override
    public List<TickField> queryTickList(long startTimestamp, long endTimestamp, String uniformSymbol) {

        String sql = "select * from tbl_tick where actionTimestamp between " +startTimestamp+ " and "+endTimestamp+ " and uniformSymbol='"+uniformSymbol+"' order by actionTimestamp asc";

        return queryTickList(sql);
    }

    @Override
    public List<BarField> queryBarList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        return queryBarList(CommonUtils.localDateTimeToMills(startDateTime), CommonUtils.localDateTimeToMills(endDateTime), uniformSymbol, barPeriodEnum);
    }

    @Override
    public List<TickField> queryTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol) {
        return queryTickList(CommonUtils.localDateTimeToMills(startDateTime), CommonUtils.localDateTimeToMills(endDateTime), uniformSymbol);
    }

    @Override
    public List<TickField> queryTickListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol) {
        String sql = "select * from tbl_tick where actionTimestamp<="+endTimestamp+ " and uniformSymbol='"+uniformSymbol+"' order by actionTimestamp asc limit " + limit;

        return queryTickList(sql);
    }

    @Override
    public List<BarField> queryBarListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        String sql = "select * from tbl_bar where actionTimestamp<="+endTimestamp+ " and uniformSymbol='"+uniformSymbol+"' and period=" + barPeriodEnum.getNumber() + " order by actionTimestamp asc limit " + limit;

        return queryBarList(sql);
    }

    private List<BarField> queryBarList(String sql) {

        try {

            return jdbcTemplate.query(sql, (rs, rowNum) -> {

                BarField.Builder barBuilder = BarField.newBuilder();

                barBuilder.setUniformSymbol(rs.getString("uniformSymbol"));
                barBuilder.setGatewayId(MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
                barBuilder.setTradingDay(rs.getInt("tradingDay"));
                barBuilder.setActionDay(rs.getInt("actionDay"));
                barBuilder.setActionTime(rs.getInt("actionTime"));
                barBuilder.setActionTimestamp(rs.getLong("actionTimestamp"));
                barBuilder.setOpenPrice(rs.getDouble("openPrice"));
                barBuilder.setHighPrice(rs.getDouble("highPrice"));
                barBuilder.setLowPrice(rs.getDouble("lowPrice"));
                barBuilder.setClosePrice(rs.getDouble("closePrice"));
                barBuilder.setOpenInterest(rs.getDouble("openInterest"));
                barBuilder.setOpenInterestDelta(rs.getDouble("openInterestDelta"));
                barBuilder.setVolume(rs.getLong("volume"));
                barBuilder.setVolumeDelta(rs.getLong("volumeDelta"));
                barBuilder.setTurnover(rs.getDouble("turnover"));
                barBuilder.setTurnoverDelta(rs.getDouble("turnoverDelta"));
                barBuilder.setPreOpenInterest(rs.getDouble("preOpenInterest"));
                barBuilder.setPreClosePrice(rs.getDouble("preClosePrice"));
                barBuilder.setPreSettlePrice(rs.getDouble("preSettlePrice"));
                barBuilder.setPeriod(rs.getInt("period"));

                return barBuilder.build();
            });

        } catch (Exception e) {
            logger.error("数据查询异常",e);
        }
        return null;
    }


    private List<TickField> queryTickList(String sql) {

        try {

            return jdbcTemplate.query(sql, (rs, rowNum) -> {

                TickField.Builder tickBuilder = TickField.newBuilder();

                tickBuilder.setUniformSymbol(rs.getString("uniformSymbol"));
                tickBuilder.setGatewayId(MarketDataDBTypeEnum.MDDT_HIST.getValueDescriptor().getName());
                tickBuilder.setTradingDay(rs.getInt("tradingDay"));
                tickBuilder.setActionDay(rs.getInt("actionDay"));
                tickBuilder.setActionTime(rs.getInt("actionTime"));
                tickBuilder.setActionTimestamp(rs.getLong("actionTimestamp"));
                tickBuilder.setLastPrice(rs.getDouble("lastPrice"));
                tickBuilder.setAvgPrice(rs.getDouble("avgPrice"));
                tickBuilder.setVolumeDelta(rs.getLong("volumeDelta"));
                tickBuilder.setVolume(rs.getLong("volume"));
                tickBuilder.setTurnover(rs.getDouble("turnover"));
                tickBuilder.setTurnoverDelta(rs.getDouble("turnoverDelta"));
                tickBuilder.setOpenInterest(rs.getDouble("openInterest"));
                tickBuilder.setOpenInterestDelta(rs.getDouble("openInterestDelta"));
                tickBuilder.setPreOpenInterest(rs.getDouble("preOpenInterest"));
                tickBuilder.setPreClosePrice(rs.getDouble("preClosePrice"));
                tickBuilder.setSettlePrice(rs.getDouble("settlePrice"));
                tickBuilder.setPreSettlePrice(rs.getDouble("preSettlePrice"));
                tickBuilder.setOpenPrice(rs.getDouble("openPrice"));
                tickBuilder.setHighPrice(rs.getDouble("highPrice"));
                tickBuilder.setLowPrice(rs.getDouble("lowPrice"));
                tickBuilder.setUpperLimit(rs.getDouble("upperLimit"));
                tickBuilder.setLowerLimit(rs.getDouble("lowerLimit"));

                for(int i = 1;i<=5;i++){
                    tickBuilder.addAskPrice(rs.getDouble("askPrice"+i));
                    tickBuilder.addBidPrice(rs.getDouble("bidPrice"+i));
                    tickBuilder.addAskVolume(rs.getInt("askVolume"+i));
                    tickBuilder.addBidVolume(rs.getInt("bidVolume"+i));
                }

                return tickBuilder.build();
            });

        } catch (Exception e) {
            logger.error("数据查询异常",e);
        }
        return null;
    }
}
