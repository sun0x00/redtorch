package xyz.redtorch.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.web.annotation.Authorization;
import xyz.redtorch.web.service.DataRecordWebService;
import xyz.redtorch.web.vo.ResultVO;

@Controller
@RequestMapping("${api.base.path}/dataRecord")
public class DataRecordController {

	@Autowired
	DataRecordWebService dataRecordWebService;
	
	@RequestMapping(path = "/subscribeReqs")
	@ResponseBody
	@Authorization
	public ResultVO getSubscribeReqs() {

		ResultVO result = new ResultVO();
		result.setData(dataRecordWebService.getSubscribeReqs());
		return result;
	}
	
	@RequestMapping(value = "/subscribeReq", method = RequestMethod.PUT)
	@ResponseBody
	@Authorization
	public ResultVO saveOrUpdateSubscribeReq(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		SubscribeReq subscribeReq = jsonObject.toJavaObject(SubscribeReq.class);
		if (subscribeReq == null) {
			result.setStatus(ResultVO.ERROR);
			return result;
		}
		
		dataRecordWebService.saveOrUpdateSubscribeReq(subscribeReq);

		return result;
	}
	
	@RequestMapping(value = "/subscribeReq", method = RequestMethod.DELETE)
	@ResponseBody
	@Authorization
	public ResultVO deleteSubscribeReq(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if (!jsonObject.containsKey("subscribeReqID")) {
			result.setStatus(ResultVO.ERROR);
			return result;
		}

		dataRecordWebService.deleteSubscribeReq(jsonObject.getString("subscribeReqID"));

		return result;
	}
	
}
