package xyz.redtorch.desktop.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.util.CommonUtils;
import xyz.redtorch.common.util.TechnicalAnalysisUtils;
import xyz.redtorch.desktop.rpc.service.RpcClientApiService;
import xyz.redtorch.desktop.service.ChartsDataService;
import xyz.redtorch.pb.CoreEnum.BarCycleEnum;
import xyz.redtorch.pb.CoreEnum.MarketDataDBTypeEnum;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBBarListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryDBTickListRsp;
import xyz.redtorch.pb.CoreRpc.RpcQueryVolumeBarListRsp;

@Service
public class ChartsDataServiceImpl implements ChartsDataService {

	private final static Logger logger = LoggerFactory.getLogger(ChartsDataServiceImpl.class);

	@Autowired
	private RpcClientApiService rpcClientApiService;

	private Map<String, JSON> chartDataMap = new HashMap<String, JSON>();

	private Set<String> processingKeySet = new HashSet<>();

	private ExecutorService executor = Executors.newCachedThreadPool();

	@Override
	public void generateCandlestickData(long startTimestamp, long endTimestamp, String unifiedSymbol,
			BarCycleEnum barCycle, String key) {
		chartDataMap.remove(key);
		processingKeySet.add(key);
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					RpcQueryDBBarListRsp rpcQueryDBBarListRsp = rpcClientApiService.queryDBBarList(startTimestamp,
							endTimestamp, unifiedSymbol, barCycle, MarketDataDBTypeEnum.MDDT_MIX, null, 120);

					JSONObject erchartsOptionData = new JSONObject();

					List<JSONArray> valueList = new ArrayList<>();
					List<String> categoryList = new ArrayList<>();
					List<Double> closePriceList = new ArrayList<>();
					List<JSONArray> volumeDeltaList = new ArrayList<>();
					List<Double> openInterestList = new ArrayList<>();

					List<Double> ma5List = new ArrayList<>();
					List<Double> ma10List = new ArrayList<>();
					List<Double> ma20List = new ArrayList<>();
					List<Double> ma30List = new ArrayList<>();

					if (rpcQueryDBBarListRsp != null) {
						List<BarField> barList = rpcQueryDBBarListRsp.getBarList();
						logger.info("共加载Bar数据{}条,合约:{},key:{}", barList.size(), unifiedSymbol, key);

						if (!barList.isEmpty()) {
							for (int i = 0; i < barList.size(); i++) {
								BarField bar = barList.get(i);
								JSONArray value = new JSONArray();
								LocalDateTime barLocalDateTime = CommonUtils
										.millsToLocalDateTime(bar.getActionTimestamp());
								String barDateTimeStr = barLocalDateTime
										.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
								value.add(bar.getOpenPrice());
								value.add(bar.getClosePrice());
								value.add(bar.getLowPrice());
								value.add(bar.getHighPrice());
								value.add(bar.getVolumeDelta());
								// value.add(bar.getVolume());
								// value.add(bar.getOpenInterestDelta());
								// value.add(bar.getOpenInterest());
								// value.add(bar.getTurnoverDelta());
								// value.add(bar.getTurnover());

								JSONArray volumeDeltaArray = new JSONArray();
								volumeDeltaArray.add(i);
								volumeDeltaArray.add(bar.getVolumeDelta());
								if (bar.getOpenPrice() > bar.getClosePrice()) {
									volumeDeltaArray.add(1);
								} else {
									volumeDeltaArray.add(-1);
								}

								categoryList.add(barDateTimeStr);
								closePriceList.add(bar.getClosePrice());
								volumeDeltaList.add(volumeDeltaArray);
								openInterestList.add(bar.getOpenInterest());

								valueList.add(value);
							}

							ma5List = TechnicalAnalysisUtils.calculateMA(closePriceList, 5);
							ma10List = TechnicalAnalysisUtils.calculateMA(closePriceList, 10);
							ma20List = TechnicalAnalysisUtils.calculateMA(closePriceList, 20);
							ma30List = TechnicalAnalysisUtils.calculateMA(closePriceList, 30);

						}
					}

					erchartsOptionData.put("valueList", valueList);
					erchartsOptionData.put("categoryList", categoryList);
					erchartsOptionData.put("volumeDeltaList", volumeDeltaList);
					erchartsOptionData.put("openInterestList", openInterestList);

					erchartsOptionData.put("ma5List", ma5List);
					erchartsOptionData.put("ma10List", ma10List);
					erchartsOptionData.put("ma20List", ma20List);
					erchartsOptionData.put("ma30List", ma30List);

					JSONObject resData = new JSONObject();
					resData.put("data", erchartsOptionData);
					resData.put("chartType", "candlestick");

					if (processingKeySet.contains(key)) {
						chartDataMap.put(key, resData);
					}
				} catch (Exception e) {
					logger.error("生成K数据错误,合约:{},key:{}", unifiedSymbol, key, e);
				} finally {
					processingKeySet.remove(key);
				}

			}
		});

	}

	@Override
	public JSON getChartData(String key) {
		logger.info("获取图表数据,key:{}", key);
		long beginTime = System.currentTimeMillis();
		while (processingKeySet.contains(key)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				break;
			}

			if (System.currentTimeMillis() - beginTime > 5 * 60 * 1000) {
				logger.info("获取图表数据超时,key:{}", key);
				break;
			}
		}
		return chartDataMap.get(key);
	}

	@Override
	public void removeChartData(String key) {
		processingKeySet.remove(key);
		chartDataMap.remove(key);
	}

	@Override
	public void generateVolOPIDeltaHistogramData(long startTimestamp, long endTimestamp, String unifiedSymbol,
			String key) {

		chartDataMap.remove(key);
		processingKeySet.add(key);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					RpcQueryDBTickListRsp rpcQueryDBTickListRsp = rpcClientApiService.queryDBTickList(startTimestamp,
							endTimestamp, unifiedSymbol, MarketDataDBTypeEnum.MDDT_MIX, null, 180);

					JSONObject erchartsOptionData = new JSONObject();

					List<Long> volumeDeltaValueList = new ArrayList<>();
					List<Double> opiDeltaValueList = new ArrayList<>();
					List<String> categoryList = new ArrayList<>();

					if (rpcQueryDBTickListRsp != null) {
						List<TickField> tickList = rpcQueryDBTickListRsp.getTickList();
						logger.info("共加载Tick数据{}条,合约:{},key:{}", tickList.size(), unifiedSymbol, key);

						if (!tickList.isEmpty()) {
							Map<Long, Map<String, Object>> dataMap = new HashMap<>();
							for (int i = 0; i < tickList.size(); i++) {

								TickField tick = tickList.get(i);

								Long priceLong = Math.round(tick.getLastPrice() * 10000);
								Long volumeDelta = tick.getVolumeDelta();
								Double opiDelta = tick.getOpenInterestDelta();

								if (volumeDelta > 0) {
									Map<String, Object> data;
									if (dataMap.containsKey(priceLong)) {
										data = dataMap.get(priceLong);
										data.put("volumeDeltaSum", (Long) data.get("volumeDeltaSum") + volumeDelta);
										data.put("opiDeltaSum", (Double) data.get("opiDeltaSum") + opiDelta);
									} else {
										data = new HashMap<>();
										data.put("volumeDeltaSum", volumeDelta);
										data.put("opiDeltaSum", opiDelta);
										data.put("priceLong", priceLong);

										dataMap.put(priceLong, data);
									}
								}
							}

							List<Map<String, Object>> dataMapList = new ArrayList<>(dataMap.values());

//							Collections.sort(dataMapList , (Map<String,Object> d1, Map<String,Object> d2) -> (Double)d1.get(price).compareTo((Double)d2.get(price)));
							Collections.sort(dataMapList, (Map<String, Object> d1, Map<String, Object> d2) -> Long
									.compare((Long) d1.get("priceLong"), (Long) d2.get("priceLong")));

							for (Map<String, Object> tmpDataMap : dataMapList) {
								categoryList.add(Double.valueOf((Long) tmpDataMap.get("priceLong")) / 10000 + "");
								volumeDeltaValueList.add((Long) tmpDataMap.get("volumeDeltaSum"));
								opiDeltaValueList.add((Double) tmpDataMap.get("opiDeltaSum"));
							}

						}
					}

					erchartsOptionData.put("volumeDeltaValueList", volumeDeltaValueList);
					erchartsOptionData.put("opiDeltaValueList", opiDeltaValueList);
					erchartsOptionData.put("categoryList", categoryList);

					JSONObject resData = new JSONObject();
					resData.put("data", erchartsOptionData);
					resData.put("chartType", "volOPIDeltaHistogram");

					if (processingKeySet.contains(key)) {
						chartDataMap.put(key, resData);
					}
				} catch (Exception e) {
					logger.error("生成K数据错误,合约:{},key:{}", unifiedSymbol, key, e);
				} finally {
					processingKeySet.remove(key);
				}

			}
		});
	}

	@Override
	public void generateTickLineData(long startTimestamp, long endTimestamp, String unifiedSymbol, String key) {
		chartDataMap.remove(key);
		processingKeySet.add(key);
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					RpcQueryDBTickListRsp rpcQueryDBTickListRsp = rpcClientApiService.queryDBTickList(startTimestamp,
							endTimestamp, unifiedSymbol, MarketDataDBTypeEnum.MDDT_MIX, null, 180);

					JSONObject erchartsOptionData = new JSONObject();

					List<String> categoryList = new ArrayList<>();
					List<Double> lastPriceValueList = new ArrayList<>();
					List<Long> volumeDeltaList = new ArrayList<>();
					List<Double> openInterestList = new ArrayList<>();

					if (rpcQueryDBTickListRsp != null) {
						List<TickField> tickList = rpcQueryDBTickListRsp.getTickList();
						logger.info("共加载Tick数据{}条,合约:{},key:{}", tickList.size(), unifiedSymbol, key);

						if (tickList.size() > 0) {
							for (int i = 0; i < tickList.size(); i++) {
								TickField tick = tickList.get(i);
								LocalDateTime tickLocalDateTime = CommonUtils
										.millsToLocalDateTime(tick.getActionTimestamp());
								String tickDateTimeStr = tickLocalDateTime
										.format(CommonConstant.DT_FORMAT_WITH_MS_FORMATTER);
								categoryList.add(tickDateTimeStr);
								volumeDeltaList.add(tick.getVolumeDelta());
								openInterestList.add(tick.getOpenInterest());
								lastPriceValueList.add(tick.getLastPrice());
							}

						}
					}

					erchartsOptionData.put("lastPriceValueList", lastPriceValueList);
					erchartsOptionData.put("categoryList", categoryList);
					erchartsOptionData.put("volumeDeltaList", volumeDeltaList);
					erchartsOptionData.put("openInterestList", openInterestList);

					JSONObject resData = new JSONObject();
					resData.put("data", erchartsOptionData);
					resData.put("chartType", "tick");

					if (processingKeySet.contains(key)) {
						chartDataMap.put(key, resData);
					}
				} catch (Exception e) {
					logger.error("生成K数据错误,合约:{},key:{}", unifiedSymbol, key, e);
				} finally {
					processingKeySet.remove(key);
				}

			}
		});

	}

	@Override
	public void generateVolumeBarCandlestickData(long startTimestamp, long endTimestamp, String unifiedSymbol,
			int volumeBarSize, String key) {
		chartDataMap.remove(key);
		processingKeySet.add(key);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					RpcQueryVolumeBarListRsp rpcQueryVolumeBarListRsp = rpcClientApiService
							.queryVolumeBarList(startTimestamp, endTimestamp, unifiedSymbol, volumeBarSize, null, 120);

					JSONObject erchartsOptionData = new JSONObject();

					List<JSONArray> valueList = new ArrayList<>();
					List<String> categoryList = new ArrayList<>();
					List<Double> closePriceList = new ArrayList<>();
					List<JSONArray> volumeDeltaList = new ArrayList<>();
					List<Double> openInterestList = new ArrayList<>();

					List<Double> ma5List = new ArrayList<>();
					List<Double> ma10List = new ArrayList<>();
					List<Double> ma20List = new ArrayList<>();
					List<Double> ma30List = new ArrayList<>();

					if (rpcQueryVolumeBarListRsp != null) {
						List<BarField> barList = rpcQueryVolumeBarListRsp.getBarList();
						logger.info("共加载Bar数据{}条,合约:{},key:{}", barList.size(), unifiedSymbol, key);

						if (barList.size() > 0) {
							for (int i = 0; i < barList.size(); i++) {
								BarField bar = barList.get(i);
								JSONArray value = new JSONArray();
								LocalDateTime barLocalDateTime = CommonUtils
										.millsToLocalDateTime(bar.getActionTimestamp());
								String barDateTimeStr = barLocalDateTime
										.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
								value.add(bar.getOpenPrice());
								value.add(bar.getClosePrice());
								value.add(bar.getLowPrice());
								value.add(bar.getHighPrice());
								value.add(bar.getVolumeDelta());
								// value.add(bar.getVolume());
								// value.add(bar.getOpenInterestDelta());
								// value.add(bar.getOpenInterest());
								// value.add(bar.getTurnoverDelta());
								// value.add(bar.getTurnover());

								JSONArray volumeDeltaArray = new JSONArray();
								volumeDeltaArray.add(i);
								volumeDeltaArray.add(bar.getVolumeDelta());
								if (bar.getOpenPrice() > bar.getClosePrice()) {
									volumeDeltaArray.add(1);
								} else {
									volumeDeltaArray.add(-1);
								}

								categoryList.add(barDateTimeStr);
								closePriceList.add(bar.getClosePrice());
								volumeDeltaList.add(volumeDeltaArray);
								openInterestList.add(bar.getOpenInterest());

								valueList.add(value);
							}

							ma5List = TechnicalAnalysisUtils.calculateMA(closePriceList, 5);
							ma10List = TechnicalAnalysisUtils.calculateMA(closePriceList, 10);
							ma20List = TechnicalAnalysisUtils.calculateMA(closePriceList, 20);
							ma30List = TechnicalAnalysisUtils.calculateMA(closePriceList, 30);

						}
					}

					erchartsOptionData.put("valueList", valueList);
					erchartsOptionData.put("categoryList", categoryList);
					erchartsOptionData.put("volumeDeltaList", volumeDeltaList);
					erchartsOptionData.put("openInterestList", openInterestList);

					erchartsOptionData.put("ma5List", ma5List);
					erchartsOptionData.put("ma10List", ma10List);
					erchartsOptionData.put("ma20List", ma20List);
					erchartsOptionData.put("ma30List", ma30List);

					JSONObject resData = new JSONObject();
					resData.put("data", erchartsOptionData);
					resData.put("chartType", "candlestick");

					if (processingKeySet.contains(key)) {
						chartDataMap.put(key, resData);
					}
				} catch (Exception e) {
					logger.error("生成K数据错误,合约:{},key:{}", unifiedSymbol, key, e);
				} finally {
					processingKeySet.remove(key);
				}

			}
		});

	}

	@Override
	public void setCharData(JSONObject charData, String key) {
		chartDataMap.put(key, charData);
	}

}
