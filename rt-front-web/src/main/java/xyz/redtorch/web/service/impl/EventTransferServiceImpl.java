package xyz.redtorch.web.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.core.entity.Account;
import xyz.redtorch.core.entity.Contract;
import xyz.redtorch.core.entity.LogData;
import xyz.redtorch.core.entity.Order;
import xyz.redtorch.core.entity.Position;
import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.entity.Trade;
import xyz.redtorch.core.service.FastEventEngineService;
import xyz.redtorch.core.service.extend.event.EventConstant;
import xyz.redtorch.core.service.extend.event.FastEvent;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandler;
import xyz.redtorch.core.service.extend.event.FastEventDynamicHandlerAbstract;
import xyz.redtorch.web.service.EventTransferService;
import xyz.redtorch.web.socketio.SocketIOMessageEventHandler;

@Service
public class EventTransferServiceImpl extends FastEventDynamicHandlerAbstract
		implements FastEventDynamicHandler, EventTransferService, InitializingBean {

	private Logger log = LoggerFactory.getLogger(EventTransferServiceImpl.class);

	@Autowired
	private SocketIOMessageEventHandler socketIOMessageEventHandler;
	@Autowired
	private FastEventEngineService fastEventEngineService;

	@Override
	public void afterPropertiesSet() throws Exception {
		fastEventEngineService.addHandler(this);
		subscribeEvent(EventConstant.EVENT_TICK);
		subscribeEvent(EventConstant.EVENT_TRADE);
		subscribeEvent(EventConstant.EVENT_ORDER);
		subscribeEvent(EventConstant.EVENT_POSITION);
		subscribeEvent(EventConstant.EVENT_ACCOUNT);
		subscribeEvent(EventConstant.EVENT_CONTRACT);
		subscribeEvent(EventConstant.EVENT_ERROR);
		subscribeEvent(EventConstant.EVENT_GATEWAY);
		subscribeEvent(EventConstant.EVENT_LOG);
		subscribeEvent(EventConstant.EVENT_LOG + "ZEUS|");
	}

	@Override
	public void onEvent(final FastEvent fastEvent, final long sequence, final boolean endOfBatch) throws Exception {

		if (!subscribedEventSet.contains(fastEvent.getEvent())) {
			return;
		}
		// 判断消息类型
		// 使用复杂的对比判断逻辑,便于扩展修改
		if (EventConstant.EVENT_TICK.equals(fastEvent.getEventType())) {
			try {
				Tick tick = fastEvent.getTick();
				socketIOMessageEventHandler.sendEvent(fastEvent.getEvent(), tick);
			} catch (Exception e) {
				log.error("向SocketIO转发Tick发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_TRADE.equals(fastEvent.getEventType())) {
			try {
				Trade trade = fastEvent.getTrade();
				socketIOMessageEventHandler.sendEvent(fastEvent.getEvent(), trade);
			} catch (Exception e) {
				log.error("向SocketIO转发Trade发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_ORDER.equals(fastEvent.getEventType())) {
			try {
				Order order = fastEvent.getOrder();
				socketIOMessageEventHandler.sendEvent(fastEvent.getEvent(), order);
			} catch (Exception e) {
				log.error("向SocketIO转发Order发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_CONTRACT.equals(fastEvent.getEventType())) {
			try {
				Contract contract = fastEvent.getContract();
				socketIOMessageEventHandler.sendEvent(fastEvent.getEvent(), contract);
			} catch (Exception e) {
				log.error("向SocketIO转发Contract发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_POSITION.equals(fastEvent.getEventType())) {
			try {
				Position position = fastEvent.getPosition();
				socketIOMessageEventHandler.sendEvent(fastEvent.getEvent(), position);
			} catch (Exception e) {
				log.error("向SocketIO转发Position发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_ACCOUNT.equals(fastEvent.getEventType())) {
			try {
				Account account = fastEvent.getAccount();
				socketIOMessageEventHandler.sendEvent(fastEvent.getEvent(), account);
			} catch (Exception e) {
				log.error("向SocketIO转发Account发生异常!!!", e);
			}
		} else if (EventConstant.EVENT_LOG.equals(fastEvent.getEventType())) {
			try {
				LogData logData = fastEvent.getLogData();
				// 发送所有日志
				if (socketIOMessageEventHandler != null) {
					socketIOMessageEventHandler.sendEvent(EventConstant.EVENT_LOG, logData);
				} else {
					log.warn("系统启动初期socketIOMessageEventHandler可能尚未注入");
				}
			} catch (Exception e) {
				log.error("向SocketIO转发Log发生异常!!!", e);
			}
		} else {
			log.warn("主引擎未能识别的事件数据类型{}", JSON.toJSONString(fastEvent.getEvent()));
		}
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onShutdown() {
		shutdownLatch.countDown();
	}

}
