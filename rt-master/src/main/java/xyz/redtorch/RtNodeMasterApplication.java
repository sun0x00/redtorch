package xyz.redtorch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import xyz.redtorch.common.service.impl.*;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@Import({FastEventServiceImpl.class, RpcRspHandlerServiceImpl.class, TodayMarketDataServiceImpl.class, HistoryMarketDataServiceImpl.class, MarketDataServiceImpl.class})
public class RtNodeMasterApplication {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        return new SimpleClientHttpRequestFactory();
    }

    public static void main(String[] args) {
        SpringApplication.run(RtNodeMasterApplication.class, args);
    }
}
