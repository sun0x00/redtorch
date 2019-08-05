package xyz.redtorch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Import;

import xyz.redtorch.common.service.impl.FastEventServiceImpl;

/**
 * 
 * @author sun0x00@gmail.com
 *
 */
@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
@Import({ FastEventServiceImpl.class })
public class RtNodeMasterApplication {
	public static void main(String[] args) {
		SpringApplication.run(RtNodeMasterApplication.class, args);
	}
}
