package xyz.redtorch.core.rmi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.core.zeus.ZeusTradingBaseService;
import xyz.redtorch.core.zeus.ZeusDataService;

@PropertySource(value = { "classpath:rt-core.properties" })
// @Configuration // 在策略项目中使用@Import引入
public class RMIClientConfiguration {

	@Value("${rt.core.rmi.host}")
	private String rmiHost;

	@Value("${rt.core.rmi.port}")
	private String rmiPort;

	@Bean(name = "coreEngineServiceRmiProxyFactory")
	public RmiProxyFactoryBean coreEngineServiceRmiProxyFactoryBean() {
		RmiProxyFactoryBean factoryBean = new RmiProxyFactoryBean();
		factoryBean.setServiceUrl("rmi://" + rmiHost + ":" + rmiPort + "/coreEngineService");
		factoryBean.setServiceInterface(CoreEngineService.class);
		return factoryBean;
	}

	@Bean(name = "zeusDataServiceRmiProxyFactory")
	public RmiProxyFactoryBean zeusDataServiceRmiProxyFactoryBean() {
		RmiProxyFactoryBean factoryBean = new RmiProxyFactoryBean();
		factoryBean.setServiceUrl("rmi://" + rmiHost + ":" + rmiPort + "/zeusDataService");
		factoryBean.setServiceInterface(ZeusDataService.class);
		return factoryBean;
	}

	@Bean(name = "zeusTradingBaseServiceRmiProxyFactory")
	public RmiProxyFactoryBean zeusTradingBaseServiceRmiProxyFactoryBean() {
		RmiProxyFactoryBean factoryBean = new RmiProxyFactoryBean();
		factoryBean.setServiceUrl("rmi://" + rmiHost + ":" + rmiPort + "/zeusTradingBaseService");
		factoryBean.setServiceInterface(ZeusTradingBaseService.class);
		return factoryBean;
	}

}
