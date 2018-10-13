package xyz.redtorch.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import xyz.redtorch.core.entity.OrderReq;
import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.gateway.GatewaySetting;
import xyz.redtorch.web.annotation.Authorization;
import xyz.redtorch.web.service.CoreEngineWebService;
import xyz.redtorch.web.vo.ResultVO;

@Controller
@RequestMapping("${api.base.path}/core")
public class CoreController {
	@Autowired
	private CoreEngineWebService coreEngineWebService;

	@RequestMapping(value = "/sendOrder", method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO sendOrder(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		String rtSymbol = jsonObject.getString("symbol");
		String priceType = jsonObject.getString("priceType");
		String direction = jsonObject.getString("direction");
		String offset = jsonObject.getString("offset");
		String rtAccountID = jsonObject.getString("rtAccountID");

		if (StringUtils.isEmpty(rtSymbol) || StringUtils.isEmpty(priceType) || StringUtils.isEmpty(direction)
				|| StringUtils.isEmpty(offset) || StringUtils.isEmpty(rtAccountID) || !jsonObject.containsKey("volume")) {
			result.setStatus(ResultVO.ERROR);
			result.setMessage("参数不正确");
			return result;
		}
		OrderReq orderReq = jsonObject.toJavaObject(OrderReq.class);

		String rtOrderID = coreEngineWebService.sendOrder(orderReq);

		result.setData(rtOrderID);

		return result;
	}

	@RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO cancelOrder(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if (StringUtils.isEmpty(jsonObject.getString("rtOrderID"))) {
			result.setStatus(ResultVO.ERROR);
		} else {
			coreEngineWebService.cancelOrder(jsonObject.getString("rtOrderID"));
		}

		return result;
	}

	@RequestMapping(value = "/cancelAllOrders", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO cancelAllOrders(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		coreEngineWebService.cancelAllOrders();

		return result;
	}

	@RequestMapping(value = "/subscribe", method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO subscribe(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		SubscribeReq subscribeReq = jsonObject.toJavaObject(SubscribeReq.class);
		if (subscribeReq == null) {
			result.setStatus(ResultVO.ERROR);
			return result;
		}
		coreEngineWebService.subscribe(subscribeReq);

		return result;
	}

	@RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO unsubscribe(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if (!coreEngineWebService.unsubscribe(jsonObject.getString("rtSymbol"), jsonObject.getString("gatewayID"))) {
			result.setStatus(ResultVO.ERROR);
		}

		return result;
	}

	@RequestMapping(value = "/gateway", method = RequestMethod.PUT)
	@ResponseBody
	@Authorization
	public ResultVO saveGatewaySettings(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		GatewaySetting gatewaySetting = jsonObject.toJavaObject(GatewaySetting.class);

		coreEngineWebService.saveOrUpdateGatewaySetting(gatewaySetting);

		return result;
	}

	@RequestMapping(path = "/gateways")
	@ResponseBody
	@Authorization
	public ResultVO getGatewaySettings(String token) {

		ResultVO result = new ResultVO();

		result.setData(coreEngineWebService.getGatewaySettings());

		return result;
	}

	@RequestMapping(value = "/gateway", method = RequestMethod.DELETE)
	@ResponseBody
	@Authorization
	public ResultVO deleteGatewaySettings(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if (!jsonObject.containsKey("gatewayID")) {
			result.setStatus(ResultVO.ERROR);
			return result;
		}

		coreEngineWebService.deleteGateway(jsonObject.getString("gatewayID"));

		return result;
	}

	@RequestMapping(value = "/changeGatewayConnectStatus", method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO changeGatewayConnectStatus(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if (!jsonObject.containsKey("gatewayID")) {
			result.setStatus(ResultVO.ERROR);
			return result;
		}
		coreEngineWebService.changeGatewayConnectStatus(jsonObject.getString("gatewayID"));

		return result;
	}

	@RequestMapping(path = "/accounts")
	@ResponseBody
	@Authorization
	public ResultVO getAccounts(String token) {

		ResultVO result = new ResultVO();

		result.setData(coreEngineWebService.getAccounts());
		return result;
	}

	@RequestMapping(path = "/trades")
	@ResponseBody
	@Authorization
	public ResultVO getTrades(String token) {

		ResultVO result = new ResultVO();

		result.setData(coreEngineWebService.getTrades());
		return result;
	}

	@RequestMapping(path = "/orders")
	@ResponseBody
	@Authorization
	public ResultVO getOrders() {
		ResultVO result = new ResultVO();
		result.setData(coreEngineWebService.getOrders());
		return result;
	}

	@RequestMapping(path = "/localPositionDetails")
	@ResponseBody
	@Authorization
	public ResultVO getLocalPositionDetails(String token) {

		ResultVO result = new ResultVO();

		result.setData(coreEngineWebService.getLocalPositionDetails());
		return result;
	}

	@RequestMapping(path = "/positions")
	@ResponseBody
	@Authorization
	public ResultVO getPositions() {

		ResultVO result = new ResultVO();
		result.setData(coreEngineWebService.getPositions());
		return result;
	}

	@RequestMapping(path = "/contracts")
	@ResponseBody
	@Authorization
	public ResultVO getContracts(String token) {

		ResultVO result = new ResultVO();

		result.setData(coreEngineWebService.getContracts());
		return result;
	}

	@RequestMapping(path = "/ticks")
	@ResponseBody
	@Authorization
	public ResultVO getTicks(String token) {

		ResultVO result = new ResultVO();

		result.setData(coreEngineWebService.getTicks());
		return result;
	}

	@RequestMapping(path = "/logs")
	@ResponseBody
	@Authorization
	public ResultVO getLogs(String token) {

		ResultVO result = new ResultVO();

		result.setData(coreEngineWebService.getLogDatas());
		return result;
	}

}
