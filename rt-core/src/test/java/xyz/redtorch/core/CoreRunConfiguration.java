package xyz.redtorch.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import xyz.redtorch.core.zeus.impl.TradingEngineServiceImpl;

@SpringBootApplication
@ComponentScan(basePackages = {"xyz.redtorch.core"}, excludeFilters={
		  @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=TradingEngineServiceImpl.class)})
public class CoreRunConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(CoreRunConfiguration.class, args);
    }
}
