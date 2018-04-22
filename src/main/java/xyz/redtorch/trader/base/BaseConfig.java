package xyz.redtorch.trader.base;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author sun0x00@gmail.com
 */
public class BaseConfig {
	private static Logger log =  LoggerFactory.getLogger(BaseConfig.class);
	public static CompositeConfiguration rtConfig;
	static {
		rtConfig = new CompositeConfiguration();
        try {
        	FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
        		    new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
        		    .configure(new Parameters().properties()
        		        .setFileName("RtConfig.properties")
        		        .setThrowExceptionOnMissing(true)
        		        .setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
        		        .setIncludesAllowed(false));
        		PropertiesConfiguration propertiesConfiguration = builder.getConfiguration();
        		rtConfig.addConfiguration(propertiesConfiguration);
        } catch (ConfigurationException e) {
        	log.error("配置文件RtConfig.properties加载失败",e);
        	throw new Error("配置文件RtConfig.properties加载失败");
        }
	}
}
