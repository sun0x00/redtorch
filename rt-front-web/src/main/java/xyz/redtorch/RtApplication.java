package xyz.redtorch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.FilterType;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;

import xyz.redtorch.core.zeus.impl.TradingEngineServiceImpl;
import xyz.redtorch.web.service.TokenService;

/**
 * @author sun0x00@gmail.com
 */
@ServletComponentScan(basePackages = { "xyz.redtorch.web.filter" })
@ComponentScan(basePackages = { "xyz.redtorch" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = { TradingEngineServiceImpl.class }) })
@RestController
@SpringBootApplication(exclude = { MongoAutoConfiguration.class })
public class RtApplication {

	private Logger log = LoggerFactory.getLogger(RtApplication.class);

	@Autowired
	private TokenService tokenService;

	@Bean
	public SocketIOServer socketIOServer(@Value("${rt.web.socketio.host}") String host,
			@Value("${rt.web.socketio.port}") Integer port) {
		com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();

		config.setHostname(host);
		config.setPort(port);

		config.setAuthorizationListener(new AuthorizationListener() {// 类似过滤器
			@Override
			public boolean isAuthorized(HandshakeData data) {
				// protocol//host:port?token=xx
				String token = data.getSingleUrlParam("token");
				if (tokenService.validate(token)) {
					log.info("SocketIO认证成功,用户[{}],Token-[{}] Address-[{}]", tokenService.getUsername(token), token,
							data.getAddress(),data.getHttpHeaders().getAsString(""));
					return true;
				}

				log.warn("SocketIO认证失败,Token-[{}] Address-[{}]", token, data.getAddress());
				return false;
			}
		});

		final SocketIOServer socketIOServer = new SocketIOServer(config);
		return socketIOServer;
	}

	@Bean
	public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
		return new SpringAnnotationScanner(socketServer);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	public static void main(String[] args) {
		SpringApplication.run(RtApplication.class, args);
	}
}
