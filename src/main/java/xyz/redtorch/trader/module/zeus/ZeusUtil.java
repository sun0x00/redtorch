package xyz.redtorch.trader.module.zeus;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.trader.base.BaseConfig;
import xyz.redtorch.trader.module.zeus.strategy.StrategySetting;
import xyz.redtorch.utils.CommonUtil;

/**
 * Zeus引擎工具类
 * @author sun0x00@gmail.com
 *
 */
public class ZeusUtil {
	
	private static Logger log = LoggerFactory.getLogger(ZeusUtil.class);
	
	/**
	 * 查找策略配置JSON文件
	 * @param path
	 * @param fileName
	 * @return
	 */
	public static File getStartegyConfigFile(String fileName) {
		String path = BaseConfig.rtConfig.getString("module.zeus.strategy.config.dir");
		if(StringUtils.isEmpty(path)) {
			path  = Thread.currentThread().getContextClassLoader ().getResource("").getPath();
			File file = new File(path+File.separator+"ZeusStartegyConfig"+File.separator+fileName);
			return file;
		}else {
			File file = new File(path+File.separator+fileName);
			return file;
		}
	}
	
	/**
	 * 从文件获取策略配置
	 * @param strategyConfigFile
	 * @return
	 */
	public static StrategySetting getStrategySetting(File strategyConfigFile) {
		if (strategyConfigFile == null || !strategyConfigFile.exists() || strategyConfigFile.isDirectory()) {
			log.error("未能找到策略的配置文件");
		} else {
			String configString = CommonUtil.readFileToString(strategyConfigFile.getAbsolutePath());
			if (StringUtils.isEmpty(configString)) {
				log.error("读取策略的配置文件发生异常");
			} else {
				StrategySetting strategySetting = null;
				try {
					strategySetting = JSON.parseObject(configString, StrategySetting.class);
					if (strategySetting == null) {
						log.error("解析策略的配置文件发生异常");
						return null;
					}
					// 合成一些配置
					strategySetting.fixSetting();
					/////////////////////////////
					// 对配置文件进行基本检查
					////////////////////////////
					if (StringUtils.isEmpty(strategySetting.getId())) {
						log.error("解析策略的配置文件未能找到ID配置");
						return null;
					}
					if (StringUtils.isEmpty(strategySetting.getName())) {
						log.error("解析策略的配置文件未能找到Name配置");
						return null;
					}
					if (StringUtils.isEmpty(strategySetting.getTradingDay())) {
						log.error("解析策略的配置文件未能找到tradingDay配置");
						return null;
					}
					if (strategySetting.getGateways() == null || strategySetting.getGateways().isEmpty()) {
						log.error("解析策略的配置文件未能找到gateways配置");
						return null;
					}
					if (strategySetting.getContracts() == null || strategySetting.getContracts().isEmpty()) {
						log.error("解析策略的配置文件未能找到contracts配置");
						return null;
					}

					boolean error = false;
					for (StrategySetting.TradeContractSetting tradeContractSetting : strategySetting.getContracts()) {
						if (tradeContractSetting.getTradeGateways() == null
								|| tradeContractSetting.getTradeGateways().isEmpty()) {
							log.error("解析策略的配置文件未能找到合约{}的gateways配置", tradeContractSetting.getRtSymbol());
							error = true;
							break;
						}

					}
					if (!error) {
						return strategySetting;
					}

				} catch (Exception e) {
					log.error("解析策略的配置文件发生异常", e);
				}
			}
		}
		return null;

	}
	
}
