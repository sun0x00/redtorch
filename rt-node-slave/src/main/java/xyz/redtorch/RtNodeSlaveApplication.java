package xyz.redtorch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Import;

import xyz.redtorch.common.service.impl.FastEventServiceImpl;
import xyz.redtorch.common.util.CommonUtils;

@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
@Import({ FastEventServiceImpl.class })
public class RtNodeSlaveApplication {

	private static final Logger logger = LoggerFactory.getLogger(RtNodeSlaveApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(RtNodeSlaveApplication.class);

		// ↓↓↓↓↓ 下面的方法依赖操作系统，不一定有效↓↓↓↓↓
		RandomAccessFile raf = null;
		FileLock fileLock = null;
		try {
			raf = new RandomAccessFile("rt-node-slave.lock", "rw");
			raf.seek(raf.length());
			FileChannel fileChannel = raf.getChannel();

			fileLock = fileChannel.tryLock();
			if (fileLock == null || !fileLock.isValid()) {
				logger.error("#### 无法锁定Lock文件 ####");
				System.exit(0);
			}

		} catch (IOException e) {
			logger.error("#### 锁定Lock文件异常 ####", e);
			System.exit(0);
		}
		// ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

		File pidFile = new File("rt-node-slave.pid");
		try {
			if (pidFile.exists()) {
				FileInputStream fisTargetFile = new FileInputStream(pidFile);
				String pidStr = IOUtils.toString(fisTargetFile, "UTF-8");
				if (CommonUtils.isStillAllive(pidStr)) {
					logger.error("#### PID:{} 对应的进程仍在运行 ####", pidStr);
					System.exit(0);
				}
			}
		} catch (IOException e) {
			logger.error("#### 处理PID文件错误 ####", e);
			System.exit(0);
		}

		app.addListeners(new ApplicationPidFileWriter(pidFile));
		app.run(args);
//		SpringApplication.run(RtNodeSlaveApplication.class, args);
	}

}
