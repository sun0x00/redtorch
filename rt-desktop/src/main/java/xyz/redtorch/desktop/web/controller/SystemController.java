package xyz.redtorch.desktop.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.desktop.service.ChartsDataService;
import xyz.redtorch.desktop.web.vo.ResponseVo;

@Controller
@RequestMapping("${rt.desktop.web.api-base-path}/system")
public class SystemController {
	private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

	@Autowired
	private ChartsDataService chartsDataService;

	@RequestMapping(value = { "/getEchartsOption" }, method = { RequestMethod.POST })
	@ResponseBody
	public ResponseVo<JSON> getEchartsOption(HttpServletRequest request, @RequestBody String key) {
		ResponseVo<JSON> responseVo = new ResponseVo<>();
		try {
			responseVo.setVoData(chartsDataService.getChartData(key));
		} catch (Exception e) {
			logger.error("获取数据异常", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}
}
