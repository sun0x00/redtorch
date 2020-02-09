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
import xyz.redtorch.node.master.po.OperatorPo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.OperatorService;
import xyz.redtorch.node.master.web.vo.RequestVo;
import xyz.redtorch.node.master.web.vo.ResponseVo;

@Controller
@RequestMapping("${rt.node.master.web.api-base-path}/management/operator")
public class OperatorController {

	private static final Logger logger = LoggerFactory.getLogger(OperatorController.class);

	@Autowired
	private OperatorService operatorService;

	@RequestMapping(value = { "/getOperatorList" })
	@ResponseBody
	public ResponseVo<List<OperatorPo>> getOperatorList(HttpServletRequest request) {
		ResponseVo<List<OperatorPo>> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanReadOperator()) {
				List<OperatorPo> operatorList = operatorService.getOperatorList();
				responseVo.setVoData(operatorList);
			} else {
				logger.error("查询操作员列表错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("查询操作员列表错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("查询操作员列表错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/deleteOperatorByOperatorId" })
	@ResponseBody
	public ResponseVo<String> deleteOperatorByOperatorId(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteOperator()) {
				logger.info("用户{}删除操作员,ID:{}", user.getUsername(), requestVo.getVoData());
				operatorService.deleteOperatorByOperatorId(requestVo.getVoData());
			} else {
				logger.error("根据操作员ID删除操作员错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据操作员ID删除操作员错误,用户没有权限");
			}
		} catch (Exception e) {
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/saveOrUpdateOperator" })
	@ResponseBody
	public ResponseVo<String> saveOrUpdateOperator(HttpServletRequest request, @RequestBody OperatorPo operator) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteOperator()) {
				if (StringUtils.isBlank(operator.getOperatorId())) {
					operator.setOperatorId(UUIDStringPoolUtils.getUUIDString());
					logger.info("用户{}新建操作员,ID:{}", user.getUsername(), operator.getOperatorId());
				} else {
					logger.info("用户{}修改操作员,ID:{}", user.getUsername(), operator.getOperatorId());
				}
				operatorService.upsertOperatorByOperatorId(operator);
			} else {
				logger.error("新增或修改操作员错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("新增或修改操作员错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("新增或修改操作员错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/createOperator" })
	@ResponseBody
	public ResponseVo<String> createOperator(HttpServletRequest request) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteOperator()) {
				OperatorPo operator = operatorService.createOperator();
				logger.info("用户{}新增操作员,操作员ID:{}", user.getUsername(), operator.getOperatorId());
			} else {
				logger.error("新增操作员发生错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("新增操作员发生错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("新增操作员发生错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}
}
