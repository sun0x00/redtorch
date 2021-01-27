package xyz.redtorch.desktop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;

public interface ChartsDataService {
    JSON getChartData(String key);

    void removeChartData(String key);

    void setCharData(JSONObject charData, String key);

    void generateCandlestickData(long startTimestamp, long endTimestamp, String uniformSymbol, BarPeriodEnum barPeriod, String key);

    void generateVolOPIDeltaHistogramData(long startTimestamp, long endTimestamp, String uniformSymbol, String key);

    void generateTickLineData(long startTimestamp, long endTimestamp, String uniformSymbol, String key);

    void generateVolumeBarCandlestickData(long startTimestamp, long endTimestamp, String uniformSymbol, int volumeBarSize, String key);
}
