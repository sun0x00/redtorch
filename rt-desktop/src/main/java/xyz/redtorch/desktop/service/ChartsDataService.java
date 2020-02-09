package xyz.redtorch.desktop.service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.pb.CoreEnum.BarCycleEnum;

public interface ChartsDataService {
	JSON getChartData(String key);

	void removeChartData(String key);

	void generateCandlestickData(long startTimestamp, long endTimestamp, String unifiedSymbol, BarCycleEnum barCycle, String key);

	void generateVolOPIDeltaHistogramData(long startTimestamp, long endTimestamp, String unifiedSymbol, String key);

	void generateTickLineData(long startTimestamp, long endTimestamp, String unifiedSymbol, String key);

	void generateVolumeBarCandlestickData(long startTimestamp, long endTimestamp, String unifiedSymbol, int volumeBarSize, String key);
}
