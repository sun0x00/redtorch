package xyz.redtorch.trader.engine.event.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.trader.engine.event.EventData;
import xyz.redtorch.trader.engine.event.EventEngine;
import xyz.redtorch.trader.engine.event.EventListener;
/**
 * @author sun0x00@gmail.com
 */
public class EventEngineImpl implements EventEngine{

	private Logger log = LoggerFactory.getLogger(EventEngineImpl.class);
	private Map<String,LinkedHashSet<EventListener>> registeredEventMap = new HashMap<>();

	
	@Override
	public void registerListener(String event, EventListener eventListener) {
		addOrRemoveListener(event, eventListener, true);
	}

	@Override
	public void removeListener(String event, EventListener eventListener) {
		addOrRemoveListener(event, eventListener, false);
	}


	@Override
	public void emit(String event, EventData eventData) {
		LinkedHashSet<EventListener> registeredListener;
		if(registeredEventMap.containsKey(event)) {
			registeredListener = registeredEventMap.get(event);
			for(EventListener listener: registeredListener) {
				try {
					listener.onEvent(eventData);
				}catch (Exception e) {
					log.error("事件引擎捕获到异常,事件类型:{},事件:{}",eventData.getEventType(),event,e);
				}
			}
		}else {
			//log.debug("未能找到此事件对应的监听实例Event:{}", event);
		}
		
		
	}
	

	
	private synchronized void addOrRemoveListener(String event, EventListener eventListener, boolean add) {
		if(add) {
			LinkedHashSet<EventListener> registeredListener;
			if(registeredEventMap.containsKey(event)) {
				registeredListener = registeredEventMap.get(event);
			}else {
				registeredListener = new LinkedHashSet<>();
			}
			registeredListener.add(eventListener);
			registeredEventMap.put(event, registeredListener);
		}else {
			if (event == null) {
				List<String> removeEventList = new ArrayList<>();
				for(Entry<String, LinkedHashSet<EventListener>> entry: registeredEventMap.entrySet()) {
					String keyEvent = entry.getKey();
					LinkedHashSet<EventListener> registeredListener = entry.getValue();
					if(registeredListener.contains(eventListener)) {
						removeEventList.add(keyEvent);
					}
				}
				
				for(String removeEvent: removeEventList) {
					LinkedHashSet<EventListener> registeredListener;
					if(registeredEventMap.containsKey(removeEvent)) {
						registeredListener = registeredEventMap.get(removeEvent);
						if(registeredListener.contains(eventListener)) {
							registeredListener.remove(eventListener);
						}
						if(registeredListener.isEmpty()) {
							registeredEventMap.remove(event);
						}
					}
				}
				
			}else {
				LinkedHashSet<EventListener> registeredListener;
				if(registeredEventMap.containsKey(event)) {
					registeredListener = registeredEventMap.get(event);
					if(registeredListener.contains(eventListener)) {
						registeredListener.remove(eventListener);
					}
					if(registeredListener.isEmpty()) {
						registeredEventMap.remove(event);
					}
				}
			}
		}
	}

}
