package xyz.redtorch.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import xyz.redtorch.common.service.HistoryMarketDataService;
import xyz.redtorch.common.service.MarketDataService;
import xyz.redtorch.common.service.TodayMarketDataService;
import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

import java.time.LocalDateTime;
import java.util.List;

public class MarketDataServiceImpl implements MarketDataService {

    private final TodayMarketDataService todayMarketDataService;

    private final HistoryMarketDataService historyMarketDataService;


    public MarketDataServiceImpl(@Autowired TodayMarketDataService todayMarketDataService, @Autowired HistoryMarketDataService historyMarketDataService){
        this.todayMarketDataService = todayMarketDataService;
        this.historyMarketDataService = historyMarketDataService;
    }

    @Override
    public List<BarField> queryBarList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        List<BarField> barList = historyMarketDataService.queryBarList(startDateTime,endDateTime,uniformSymbol,barPeriodEnum);
        barList.addAll(todayMarketDataService.queryBarList(startDateTime, endDateTime, uniformSymbol,barPeriodEnum));
        return barList;
    }

    @Override
    public List<TickField> queryTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol) {
        List<TickField> tickList = historyMarketDataService.queryTickList(startDateTime,endDateTime,uniformSymbol);
        tickList.addAll(todayMarketDataService.queryTickList(startDateTime, endDateTime, uniformSymbol));
        return tickList;
    }

    @Override
    public List<BarField> queryBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        List<BarField> barList = historyMarketDataService.queryBarList(startTimestamp,endTimestamp,uniformSymbol,barPeriodEnum);
        barList.addAll(todayMarketDataService.queryBarList(startTimestamp, endTimestamp, uniformSymbol,barPeriodEnum));
        return barList;
    }

    @Override
    public List<TickField> queryTickList(long startTimestamp, long endTimestamp, String uniformSymbol) {
        List<TickField> tickList = historyMarketDataService.queryTickList(startTimestamp,endTimestamp,uniformSymbol);
        tickList.addAll(todayMarketDataService.queryTickList(startTimestamp, endTimestamp, uniformSymbol));
        return tickList;
    }

    @Override
    public List<TickField> queryTickListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol) {
        List<TickField> tickList = historyMarketDataService.queryTickListByEndTimestampAndLimit(endTimestamp, limit, uniformSymbol);
        tickList.addAll(todayMarketDataService.queryTickListByEndTimestampAndLimit(endTimestamp, limit, uniformSymbol));

        if (tickList.size() > limit) {
            tickList = tickList.subList(tickList.size() - limit, tickList.size());
        }

        return tickList;
    }

    @Override
    public List<BarField> queryBarListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol, BarPeriodEnum barPeriodEnum) {
        List<BarField> barList = historyMarketDataService.queryBarListByEndTimestampAndLimit(endTimestamp, limit, uniformSymbol, barPeriodEnum);
        barList.addAll(todayMarketDataService.queryBarListByEndTimestampAndLimit(endTimestamp, limit, uniformSymbol, barPeriodEnum));

        if (barList.size() > limit) {
            barList = barList.subList(barList.size() - limit, barList.size());
        }

        return barList;
    }
}
