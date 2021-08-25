package xyz.redtorch.node.master.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.web.vo.RequestVo;
import xyz.redtorch.common.web.vo.ResponseVo;
import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.FavoriteContractService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("${rt.master.apiBasePath}/customize")
public class CustomizeController {

    private static final Logger logger = LoggerFactory.getLogger(CustomizeController.class);

    @Autowired
    private FavoriteContractService favoriteContractService;

    @RequestMapping(value = {"/getFavoriteContractList"})
    @ResponseBody
    public ResponseVo<List<ContractPo>> getFavoriteContractList(HttpServletRequest request) {
        ResponseVo<List<ContractPo>> responseVo = new ResponseVo<>();
        try {
            UserPo user = (UserPo) request.getAttribute(CommonConstant.KEY_USER_PO);
            responseVo.setVoData(favoriteContractService.getContractListByUsername(user.getUsername()));
        } catch (Exception e) {
            logger.error("获取常用合约列表错误", e);
            responseVo.setStatus(false);
            responseVo.setMessage(e.getMessage());
        }
        return responseVo;
    }

    @RequestMapping(value = {"/addFavoriteContractByUniformSymbol"})
    @ResponseBody
    public ResponseVo<String> addFavoriteContractByUniformSymbol(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
        ResponseVo<String> responseVo = new ResponseVo<>();
        try {
            UserPo user = (UserPo) request.getAttribute(CommonConstant.KEY_USER_PO);
            favoriteContractService.upsertContractByUsernameAndUniformSymbol(user.getUsername(), requestVo.getVoData());
        } catch (Exception e) {
            logger.error("根据统一合约标识新增合约错误", e);
            responseVo.setStatus(false);
            responseVo.setMessage(e.getMessage());
        }
        return responseVo;
    }

    @RequestMapping(value = {"/deleteFavoriteContractByUniformSymbol"})
    @ResponseBody
    public ResponseVo<String> deleteFavoriteContractByUniformSymbol(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
        ResponseVo<String> responseVo = new ResponseVo<>();
        try {
            UserPo user = (UserPo) request.getAttribute(CommonConstant.KEY_USER_PO);
            favoriteContractService.deleteContractByUsernameAndUniformSymbol(user.getUsername(), requestVo.getVoData());
        } catch (Exception e) {
            logger.error("根据用户名和统一合约标识删除合约错误", e);
            responseVo.setStatus(false);
            responseVo.setMessage(e.getMessage());
        }
        return responseVo;

    }
}
