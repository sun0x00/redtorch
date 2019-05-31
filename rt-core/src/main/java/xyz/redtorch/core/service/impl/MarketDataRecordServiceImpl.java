package xyz.redtorch.core.service.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import xyz.redtorch.core.entity.Bar;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.service.DataRecordService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandler;
import xyz.redtorch.core.utils.BarGenerator;
import xyz.redtorch.core.utils.BarGenerator.CommonBarCallBack;
import xyz.redtorch.utils.MongoDBClient;
import xyz.redtorch.utils.MongoDBUtil;

/**
 * 	重写DataRecordServiceImpl的事件消费逻辑
 * 	由于父类没有实现Bar记录，并且也没有对Tick数据按合约分开存放，因此重写此逻辑
 * @author kevinhuangwl
 *
 */
@Service
public class MarketDataRecordServiceImpl extends DataRecordServiceImpl
		implements FastEventDynamicHandler, DataRecordService, InitializingBean {

	private static Logger log = LoggerFactory.getLogger(MarketDataRecordServiceImpl.class);
	
	private static final String MARKET_TICK_DATA = "MarketTickData";
	
	private static final String MARKET_BAR_M1_DATA = "MarketBarM1Data";
	
	private BarCallBackHandler barCallbackHandler = new BarCallBackHandler();
	
	private ConcurrentHashMap<String, BarGenerator> barGenMap = new ConcurrentHashMap<String, BarGenerator>();
	
	@Override
	public void afterPropertiesSet() throws Exception {

		fastEventEngineService.addHandler(this);
		subscribeEvent(EventConstant.EVENT_TICK);
		
		executor.execute(new DataRecordTask());
		
		List<SubscribeReq> subscribeReqs = getSubscribeReqs();
		
		if(subscribeReqs!=null) {
			for(SubscribeReq subscribeReq: subscribeReqs) {
				coreEngineService.subscribe(subscribeReq, subscriberID);
				String tickID = subscribeReq.getSymbol()+"."+subscribeReq.getExchange()+"."+subscribeReq.getGatewayID();
				recordRtTickIDSet.add(tickID);
				String tickKey = subscribeReq.getSymbol().toUpperCase()+"_"+subscribeReq.getExchange();
				barGenMap.put(tickKey, new BarGenerator(barCallbackHandler));
				log.info("增加TickID-[{}]记录行情",tickID);
			}
		}

	}
	
	/**
	 * 重写DataRecordTask， 实现按名称分类
	 * @author kevinhuangwl
	 *
	 */
	private class DataRecordTask implements Runnable{
		
		private void updateTick(String tickKey, Tick tick) {
			BarGenerator barGen = barGenMap.get(tickKey);
			barGen.updateTick(tick);
		}

		@Override
		public void run() {
			log.info("行情记录任务启动");
			while (!Thread.currentThread().isInterrupted()) {
				FastEvent fastEvent = eventQueue.poll();
				if (fastEvent != null) {
					// 判断消息类型
					if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
						Tick tick = fastEvent.getTick();
						String symbol = tick.getSymbol().toUpperCase();
						String exchange = tick.getExchange();
						String colName = symbol + "_" + exchange;
						updateTick(colName, tick);
						Document filter = new Document();
						filter.append("dateTime", tick.getDateTime().toDate()).append("rtSymbol", tick.getRtSymbol());
						
						try {
							Document document = MongoDBUtil.beanToDocument(tick);
							MongoDBClient client = mongoDBService.getMdDBClient();
							client.upsert(MARKET_TICK_DATA, colName, document, filter);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							log.error("行情记录存储Tick行情发生异常", e);
						}
					}
				}else {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// nop
					}
				}
			}			
		}
		
	}
	
	/**
	 * Bar生成的回调处理
	 * @author kevinhuangwl
	 *
	 */
	private class BarCallBackHandler implements CommonBarCallBack{

		@Override
		public void call(Bar bar) {
			String symbol = bar.getSymbol().toUpperCase();
			String exchange = bar.getExchange();
			String colName = symbol + "_" + exchange;
			Document filter = new Document();
			filter.append("dateTime", bar.getDateTime().toDate()).append("rtSymbol", bar.getRtSymbol());
			
			try {
				Document document = MongoDBUtil.beanToDocument(bar);
				MongoDBClient client = mongoDBService.getMdDBClient();
				client.upsert(MARKET_BAR_M1_DATA, colName, document, filter);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error("行情记录存储Bar行情发生异常", e);
			}
		}
		
	}
}
