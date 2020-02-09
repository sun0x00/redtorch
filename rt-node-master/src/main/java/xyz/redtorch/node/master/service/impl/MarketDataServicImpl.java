package xyz.redtorch.node.master.service.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.common.service.MarketDataService;
import xyz.redtorch.common.service.impl.MarketDataServiceBasicImpl;
import xyz.redtorch.node.db.MongoDBClientService;

@Service
public class MarketDataServicImpl extends MarketDataServiceBasicImpl implements MarketDataService, InitializingBean {

	@Autowired
	MongoDBClientService mongoDBClientService;

	@Override
	public void afterPropertiesSet() throws Exception {
		initSetting(mongoDBClientService.getMarketDataTodayDBClient(), mongoDBClientService.getMarketDataTodayDBName(), mongoDBClientService.getMarketDataHistDBClient(),
				mongoDBClientService.getMarketDataHistDBName());
	}

}
