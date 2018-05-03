package xyz.redtorch.trader.base;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sun0x00@gmail.com
 */
public class BaseConfig {
	private static Logger log = LoggerFactory.getLogger(BaseConfig.class);
	public static CompositeConfiguration rtConfig;
	static {
		rtConfig = new CompositeConfiguration();

		try {
			FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
					PropertiesConfiguration.class)
							.configure(new Parameters().properties().setFileName("RtConfig.properties")
									.setThrowExceptionOnMissing(true)
									.setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
									.setIncludesAllowed(false));
			PropertiesConfiguration propertiesConfiguration = builder.getConfiguration();
			rtConfig.addConfiguration(propertiesConfiguration);
			log.info("默认配置文件加载成功");
		} catch (ConfigurationException e) {
			log.error("配置文件RtConfig.properties加载失败", e);
			throw new Error("配置文件RtConfig.properties加载失败");
		}
		PropertiesBuilderParameters params = new Parameters().properties().setThrowExceptionOnMissing(true)
				.setListDelimiterHandler(new DefaultListDelimiterHandler(';')).setIncludesAllowed(false);
		
		String classPath = BaseConfig.class.getResource("/").getPath();
		// 如果是war包，尝试使用外部配置文件覆盖
		if(classPath.contains("war!")) {
			try {
				String warPath = new File(".").getCanonicalPath();
				log.info("检测到从war启动,尝试加载外部配置文件,当前文件目录{}",warPath);
				if (!tryConfig(params, warPath + File.separator + "RtConfig.properties")) {
					if (tryConfig(params, warPath + File.separator +  "config" + File.separator + "RtConfig.properties")) {
						log.info("外部配置文件加载成功{}", warPath + "config" + File.separator + "RtConfig.properties");
					}
				}else {
					log.info("外部配置文件加载成功{}", warPath + "RtConfig.properties" );
				}
			} catch (IOException e) {
				log.error("获取war所在目录发生错误",e);
			}

		}
	}

	static boolean tryConfig(PropertiesBuilderParameters params, String configUrlStr) {
		try {
			URL configFileUrl = new URL(configUrlStr);
			FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
					PropertiesConfiguration.class).configure(params.setURL(configFileUrl));
			PropertiesConfiguration propertiesConfiguration = builder.getConfiguration();
			rtConfig.addConfiguration(propertiesConfiguration);
			return true;
		} catch (ConfigurationException e) {
			log.debug("配置文件RtConfig.properties加载失败", e);
			return false;
		} catch (MalformedURLException e) {
			log.debug("配置文件RtConfig.properties加载失败", e);
			return false;
		}
	}
}
