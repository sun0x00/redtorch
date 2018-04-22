package xyz.redtorch.trader.module.zeus;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;

/**
 * @author sun0x00@gmail.com
 */
public class BacktestingUtil {
	private static Logger log = LoggerFactory.getLogger(BacktestingUtil.class);
	
	public static StrategySetting getConfigFileByClassName(String strategyClassName) {
		String[] classNameArray = strategyClassName.split("\\.");
		String simpleClassName = classNameArray[classNameArray.length-1];
		File strategyConfigFile = ZeusUtil.getStartegyConfigFile(simpleClassName+"-Setting.json");
		StrategySetting strategySetting = ZeusUtil.getStrategySetting(strategyConfigFile);
		if (strategySetting == null) {
			log.error("未能获取到配置,文件路径{}",strategyConfigFile.getAbsolutePath());
		}
		return strategySetting;
	}
	
	public static StrategySetting getConfigFileByFilePath(String filePath) {
		if(StringUtils.isEmpty(filePath)) {
			log.error("文件路径错误");
			return null;
		}
		File strategyConfigFile = new File(filePath);
		
		StrategySetting strategySetting = ZeusUtil.getStrategySetting(strategyConfigFile);
		if (strategySetting == null) {
			log.error("未能获取到配置,文件路径{}",strategyConfigFile.getAbsolutePath());
		}
		return strategySetting;
	}
}
