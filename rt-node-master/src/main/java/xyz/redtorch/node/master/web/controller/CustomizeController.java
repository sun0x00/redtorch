package xyz.redtorch.node.master.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import xyz.redtorch.RtConstant;
import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.FavoriteContractService;
import xyz.redtorch.node.master.web.vo.RequestVo;
import xyz.redtorch.node.master.web.vo.ResponseVo;

@Controller
@RequestMapping("${rt.node.master.web.api-base-path}/customize")
public class CustomizeController {

	private static final Logger logger = LoggerFactory.getLogger(CustomizeController.class);

	@Autowired
	private FavoriteContractService favoriteContractService;

	@RequestMapping(value = { "/getFavoriteContractList" })
	@ResponseBody
	public ResponseVo<List<ContractPo>> getFavoriteContractList(HttpServletRequest request) {
		ResponseVo<List<ContractPo>> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			responseVo.setVoData(favoriteContractService.getContractListByUsername(user.getUsername()));
		} catch (Exception e) {
			logger.error("获取常用合约列表错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/addFavoriteContractByUnifiedSymbol" })
	@ResponseBody
	public ResponseVo<String> addFavoriteContractByUnifiedSymbol(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			favoriteContractService.upsertContractByUsernameAndUnifiedSymbol(user.getUsername(), requestVo.getVoData());
		} catch (Exception e) {
			logger.error("根据统一合约标识新增合约错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/deleteFavoriteContractByUnifiedSymbol" })
	@ResponseBody
	public ResponseVo<String> deleteFavoriteContractByUnifiedSymbol(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			favoriteContractService.deleteContractByUsernameAndUnifiedSymbol(user.getUsername(), requestVo.getVoData());
		} catch (Exception e) {
			logger.error("根据用户名和统一合约标识删除合约错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;

	}
}
