package xyz.redtorch.web.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import xyz.redtorch.web.annotation.Authorization;
import xyz.redtorch.web.service.ZeusEngineWebService;
import xyz.redtorch.web.vo.ResultVO;

@Controller
@RequestMapping("${api.base.path}/zeus")
public class ZeusController {

	@Autowired
	private ZeusEngineWebService zeusEngineWebService;

	@RequestMapping("/srategyInfos")
	@ResponseBody
	@Authorization
	public ResultVO zeusGetStrategyInfos(String token) {

		ResultVO result = new ResultVO();

		List<Map<String, Object>> strategyInfos = zeusEngineWebService.getStrategyInfos();

		result.setData(strategyInfos);

		return result;
	}

	@RequestMapping(value = "/changeStrategyStatus", method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO zeusChangeStrategyStatus(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if (!jsonObject.containsKey("actionType")) {
			result.setStatus(ResultVO.ERROR);
			return result;
		}

		String type = jsonObject.getString("actionType");

		String strategyID = jsonObject.getString("strategyID");

		if (!type.contains("All")) {
			if (StringUtils.isEmpty(strategyID)) {
				result.setStatus(ResultVO.ERROR);
				return result;
			}
		}
		if ("init".equals(type)) {
			zeusEngineWebService.initStrategy(strategyID);
		} else if ("start".equals(type)) {
			zeusEngineWebService.sartStrategy(strategyID);
		} else if ("stop".equals(type)) {
			zeusEngineWebService.stopStrategy(strategyID);
		} else if ("reload".equals(type)) {
			zeusEngineWebService.reloadStrategy(strategyID);
		} else if ("initAll".equals(type)) {
			zeusEngineWebService.initAllStrategy();
		} else if ("startAll".equals(type)) {
			zeusEngineWebService.startAllStrategy();
		} else if ("stopAll".equals(type)) {
			zeusEngineWebService.stopAllStrategy();
		} else if ("reloadAll".equals(type)) {
			zeusEngineWebService.stopAllStrategy();
		}

		return result;
	}

}
