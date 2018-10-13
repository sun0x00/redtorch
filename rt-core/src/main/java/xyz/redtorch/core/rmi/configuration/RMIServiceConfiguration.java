package xyz.redtorch.core.rmi.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.remoting.rmi.RmiServiceExporter;

import xyz.redtorch.core.service.CoreEngineService;
import xyz.redtorch.core.utils.RtRMIServiceExporter;
import xyz.redtorch.core.zeus.ZeusDataService;
import xyz.redtorch.core.zeus.ZeusTradingBaseService;

@Configuration
@PropertySource(value = { "classpath:rt-core.properties" })
public class RMIServiceConfiguration {

	@Value("${rt.core.rmi.host}")
	private String rmiHost;
	@Value("${rt.core.rmi.port}")
	private Integer rmiPort;

	@Bean
	public RmiServiceExporter coreEngineServiceRmiExporter(CoreEngineService coreEngineService) {
		RtRMIServiceExporter exporter = new RtRMIServiceExporter();
		if (StringUtils.isNotBlank(rmiHost)) {
			exporter.setRegistryHost(rmiHost);
		}
		if (rmiPort != null) {
			exporter.setRegistryPort(rmiPort);
		}
		exporter.setService(coreEngineService);
		// serviceName属性用来在RMI注册表中注册一个服务
		exporter.setServiceName("coreEngineService");
		exporter.setServiceInterface(CoreEngineService.class);
		return exporter;
	}

	@Bean
	public RmiServiceExporter zeusDataServiceRmiExporter(ZeusDataService zeusDataService) {
		RtRMIServiceExporter exporter = new RtRMIServiceExporter();
		if (StringUtils.isNotBlank(rmiHost)) {
			exporter.setRegistryHost(rmiHost);
		}
		if (rmiPort != null) {
			exporter.setRegistryPort(rmiPort);
		}
		exporter.setService(zeusDataService);
		// serviceName属性用来在RMI注册表中注册一个服务
		exporter.setServiceName("zeusDataService");
		exporter.setServiceInterface(ZeusDataService.class);
		return exporter;
	}

	@Bean
	public RmiServiceExporter zeusTradingBaseServiceRmiExporter(ZeusTradingBaseService zeusTradingBaseService) {
		RtRMIServiceExporter exporter = new RtRMIServiceExporter();
		if (StringUtils.isNotBlank(rmiHost)) {
			exporter.setRegistryHost(rmiHost);
		}
		if (rmiPort != null) {
			exporter.setRegistryPort(rmiPort);
		}
		exporter.setService(zeusTradingBaseService);
		// serviceName属性用来在RMI注册表中注册一个服务
		exporter.setServiceName("zeusTradingBaseService");
		exporter.setServiceInterface(ZeusTradingBaseService.class);
		return exporter;
	}

}
