package xyz.redtorch.common.util;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UUIDStringPoolUtils {

	private static final Logger logger = LoggerFactory.getLogger(UUIDStringPoolUtils.class);
	private static Queue<String> uuidStringQueue = new ConcurrentLinkedQueue<String>();
	protected static volatile boolean isCharging = false;
	static {
		for (int i = 0; i < 100000; i++) {
			uuidStringQueue.add(UUID.randomUUID().toString());
		}
		logger.info("UUID字符串池已初始化");
	}

	public static String getUUIDString() {
		String uuidString = uuidStringQueue.poll();
		if (StringUtils.isBlank(uuidString)) {
			uuidString = UUID.randomUUID().toString();
		}
		if (uuidStringQueue.size() < 10000) {
			isCharging = true;
			new Thread(new ChargeTask()).start();
		}
		return uuidString;
	}

	static class ChargeTask implements Runnable {
		@Override
		public void run() {
			while (uuidStringQueue.size() < 100000) {
				uuidStringQueue.add(UUID.randomUUID().toString());
			}
			isCharging = false;
		}

	}

}
