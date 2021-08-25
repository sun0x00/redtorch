package xyz.redtorch.common.service;

import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

import java.time.LocalDateTime;
import java.util.List;

public interface TodayMarketDataService {

    List<BarField> queryBarList(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriodEnum);

    List<TickField> queryTickList(long startTimestamp, long endTimestamp, String uniformSymbol);

    List<BarField> queryBarList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol, BarPeriodEnum barPeriodEnum);

    List<TickField> queryTickList(LocalDateTime startDateTime, LocalDateTime endDateTime, String uniformSymbol);

    List<TickField> queryTickListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol);

    List<BarField> queryBarListByEndTimestampAndLimit(long endTimestamp, int limit, String uniformSymbol, BarPeriodEnum barPeriodEnum);

    boolean upsertBar(BarField bar);

    boolean upsertBarList(List<BarField> barList);

    boolean upsertTick(TickField tick);

    boolean upsertTickList(List<TickField> tickList);

}
