package xyz.redtorch.core.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.core.service.DataRecordService;
import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.core.service.MongoDBService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandler;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandlerAbstract;

@Service
public class DataRecordServiceImpl  extends FastEventDynamicHandlerAbstract implements 
FastEventDynamicHandler,DataRecordService,InitializingBean{
	private Logger log = LoggerFactory.getLogger(DataRecordServiceImpl.class);
	
	@Autowired
	private MongoDBService mongoDBService;
	
	@Autowired
	private CoreEngineService coreEngineService;

	@Value("${rt.client.dbname}")
	private String clientDBName;
	
	private Set<String> recordRtTickIDSet = new HashSet<>();
	
	private String dataRecordSettingCollection = "DataRecordSetting";
	
	private String subscriberID = "DATA_RECORD";
	
	@Autowired
	private FastEventEngineService fastEventEngineService;
	
	private Queue<FastEvent> eventQueue = new ConcurrentLinkedQueue<>();

	private ExecutorService executor = Executors.newCachedThreadPool();
	
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
				log.info("增加TickID-[{}]记录行情",tickID);
			}
		}

	}
	
	@Override
	public void onEvent(FastEvent fastEvent, long sequence, boolean endOfBatch) throws Exception {

		if (!subscribedEventSet.contains(fastEvent.getEvent())) {
			return;
		}
		
		eventQueue.add(fastEvent);
	}
	
	
	@Override
	public List<SubscribeReq> getSubscribeReqs() {
		
		List<SubscribeReq> subscribeReqs = new ArrayList<>();
		
		List<Document> documents = mongoDBService.getDefaultDBClient().find(clientDBName, dataRecordSettingCollection);
		if(documents!=null) {
			for(Document document:documents) {
				SubscribeReq subscribeReq = JSON.parseObject(document.toJson(), SubscribeReq.class);
				subscribeReqs.add(subscribeReq);
			}
		}
		return subscribeReqs;
	}

	@Override
	public void saveOrUpdateSubscribeReq(SubscribeReq subscribeReq) {

		Document filter = new Document();
		filter.put("id", subscribeReq.getId());
		
		List<Document> documents = mongoDBService.getDefaultDBClient().find(clientDBName, dataRecordSettingCollection, filter);

		String newTickID = subscribeReq.getRtSymbol()+"."+subscribeReq.getGatewayID();
		
		if(documents!=null&&documents.size()>0) {
			for(Document document:documents) {
				SubscribeReq oldSubscribeReq = JSON.parseObject(document.toJson(), SubscribeReq.class);
				coreEngineService.unsubscribe(oldSubscribeReq.getRtSymbol(), oldSubscribeReq.getGatewayID(), subscriberID);
				
				String oldTickID = oldSubscribeReq.getRtSymbol()+"."+oldSubscribeReq.getGatewayID();
				if(!oldTickID.equals(newTickID)) {
					recordRtTickIDSet.remove(oldTickID);
					log.info("停止TickID-[{}]记录行情",oldTickID);
					recordRtTickIDSet.add(newTickID);
					log.info("增加TickID-[{}]记录行情",newTickID);
				}
			}
		} else {
			recordRtTickIDSet.add(newTickID);
			log.info("增加TickID-[{}]记录行情",newTickID);
		}

		
		coreEngineService.subscribe(subscribeReq, subscriberID);

		Document insertDocument = Document.parse(JSON.toJSONString(subscribeReq));

		mongoDBService.getDefaultDBClient().delete(clientDBName, dataRecordSettingCollection, filter);
		mongoDBService.getDefaultDBClient().insert(clientDBName, dataRecordSettingCollection, insertDocument);
	}

	@Override
	public void deleteSubscribeReq(String id) {
		Document filter = new Document();
		filter.put("id", id);
		
		List<Document> documents = mongoDBService.getDefaultDBClient().find(clientDBName, dataRecordSettingCollection, filter);
		
		if(documents!=null) {
			for(Document document:documents) {
				SubscribeReq oldSubscribeReq = JSON.parseObject(document.toJson(), SubscribeReq.class);
				coreEngineService.unsubscribe(oldSubscribeReq.getRtSymbol(), oldSubscribeReq.getGatewayID(), subscriberID);
				String oldTickID = oldSubscribeReq.getRtSymbol()+"."+oldSubscribeReq.getGatewayID();
				recordRtTickIDSet.remove(oldTickID);
				log.info("停止对TickID-[{}]记录行情",oldTickID);
			}
		}
		
		mongoDBService.getDefaultDBClient().delete(clientDBName, dataRecordSettingCollection, filter);
	}
	
	private class DataRecordTask implements Runnable {

		@Override
		public void run() {
			log.info("行情记录任务启动");
			while (!Thread.currentThread().isInterrupted()) {
				FastEvent fastEvent = eventQueue.poll();
				if (fastEvent != null) {
					// 判断消息类型
					if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
						Tick tick = fastEvent.getTick();
						if(recordRtTickIDSet.contains(tick.getRtTickID())) {
							mongoDBService.saveTickToDailyDB(tick);
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

}
