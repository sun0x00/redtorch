package xyz.redtorch.node.master.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import xyz.redtorch.RtConstant;
import xyz.redtorch.common.util.UUIDStringPoolUtils;
import xyz.redtorch.node.master.po.GatewayPo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.GatewayService;
import xyz.redtorch.node.master.web.vo.RequestVo;
import xyz.redtorch.node.master.web.vo.ResponseVo;

@Controller
@RequestMapping("${rt.node.master.web.api-base-path}/management/gateway")
public class GatewayController {

	private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

	@Autowired
	private GatewayService gatewayService;

	@RequestMapping(value = { "/getGatewayList" })
	@ResponseBody
	public ResponseVo<List<GatewayPo>> getGatewayList(HttpServletRequest request) {
		ResponseVo<List<GatewayPo>> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanReadGateway()) {
				List<GatewayPo> gatewayList = gatewayService.getGatewayList();
				for (GatewayPo gateway : gatewayList) {
					if (gateway.getCtpSetting() != null) {
						gateway.getCtpSetting().setAuthCode(RtConstant.SECURITY_MASK);
						gateway.getCtpSetting().setPassword(RtConstant.SECURITY_MASK);
					}

				}
				responseVo.setVoData(gatewayList);
			} else {
				logger.error("查询网关列表错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("查询网关列表错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("查询网关列表错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping("/getGatewayByGatewayId")
	@ResponseBody
	public ResponseVo<GatewayPo> getGatewayByGatewayId(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<GatewayPo> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanReadGateway()) {
				String gatewayId = requestVo.getVoData();
				GatewayPo gatewayPo = gatewayService.getGatewayByGatewayId(gatewayId);
				responseVo.setVoData(gatewayPo);
			} else {
				logger.error("根据ID查询网关错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据ID查询网关错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("根据ID查询网关错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/deleteGatewayByGatewayId" })
	@ResponseBody
	public ResponseVo<String> deleteGatewayByGatewayId(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteGateway()) {
				logger.info("用户{}删除网关,ID:{}", user.getUsername(), requestVo.getVoData());
				gatewayService.deleteGatewayByGatewayId(requestVo.getVoData());
			} else {
				logger.error("根据网关ID删除网关错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据网关ID删除网关错误,用户没有权限");
			}
		} catch (Exception e) {
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/saveOrUpdateGateway" })
	@ResponseBody
	public ResponseVo<String> saveOrUpdateGateway(HttpServletRequest request, @RequestBody GatewayPo gateway) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteGateway()) {
				long version = System.currentTimeMillis();
				if (StringUtils.isBlank(gateway.getGatewayId())) {
					gateway.setGatewayId(UUIDStringPoolUtils.getUUIDString());
					logger.info("用户{}新建网关,ID:{},版本:{}", user.getUsername(), gateway.getGatewayId(), version);
				} else {
					logger.info("用户{}修改网关,ID:{},版本:{}", user.getUsername(), gateway.getGatewayId(), version);
				}
				gateway.setVersion(version);
				gatewayService.upsertGatewayByGatewayId(gateway);
			} else {
				logger.error("新增或修改网关错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("新增或修改网关错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("新增或修改网关错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/connectGatewayByGatewayId" })
	@ResponseBody
	public ResponseVo<String> connectGatewayByGatewayId(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanChangeGatewayStatus()) {
				logger.info("用户{}连接网关,ID:{}", user.getUsername(), requestVo.getVoData());
				gatewayService.connectGatewayByGatewayId(requestVo.getVoData());
			} else {
				logger.error("根据网关ID连接网关错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据网关ID连接网关错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("根据网关ID连接网关错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/disconnectGatewayByGatewayId" })
	@ResponseBody
	public ResponseVo<String> disconnectGatewayByGatewayId(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanChangeGatewayStatus()) {
				logger.info("用户{}断开网关,ID:{}", user.getUsername(), requestVo.getVoData());
				gatewayService.disconnectGatewayByGatewayId(requestVo.getVoData());
			} else {
				logger.error("根据网关ID断开网关错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据网关ID断开网关错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("断开网关错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/disconnectAllGateways" })
	@ResponseBody
	public ResponseVo<String> disconnectAllGateways(HttpServletRequest request) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanChangeGatewayStatus()) {
				logger.info("用户{}断开全部网关", user.getUsername());
				gatewayService.disconnectAllGateways();
			} else {
				logger.error("断开全部网关错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("断开全部网关错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("断开全部网关错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/connectAllGateways" })
	@ResponseBody
	public ResponseVo<String> connectAllGateways(HttpServletRequest request) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanChangeGatewayStatus()) {
				logger.info("用户{}连接全部网关", user.getUsername());
				gatewayService.connectAllGateways();
			} else {
				logger.error("连接全部网关错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("连接全部网关错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("连接全部网关错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}
}
