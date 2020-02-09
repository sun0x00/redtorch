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
import xyz.redtorch.node.master.po.NodePo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.NodeService;
import xyz.redtorch.node.master.web.vo.RequestVo;
import xyz.redtorch.node.master.web.vo.ResponseVo;

@Controller
@RequestMapping("${rt.node.master.web.api-base-path}/management/node")
public class NodeController {

	private static final Logger logger = LoggerFactory.getLogger(NodeController.class);

	@Autowired
	private NodeService nodeService;

	@RequestMapping(value = { "/getNodeList" })
	@ResponseBody
	public ResponseVo<List<NodePo>> getNodeList(HttpServletRequest request) {
		ResponseVo<List<NodePo>> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanReadNode()) {
				List<NodePo> nodeList = nodeService.getNodeList();
				responseVo.setVoData(nodeList);
			} else {
				logger.error("查询节点列表发生错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("查询节点列表发生错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("查询节点列表发生错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/deleteNodeByNodeId" })
	@ResponseBody
	public ResponseVo<String> deleteNodeByNodeId(HttpServletRequest request, @RequestBody RequestVo<Integer> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteNode()) {
				logger.info("用户{}删除节点,ID:{}", user.getUsername(), requestVo.getVoData());
				nodeService.deleteNodeByNodeId(requestVo.getVoData());
			} else {
				logger.error("根据节点ID删除节点发生错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据节点ID删除节点发生错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("根据节点ID删除节点发生错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/resetNodeTokenByNodeId" })
	@ResponseBody
	public ResponseVo<String> resetNodeTokenByNodeId(HttpServletRequest request, @RequestBody RequestVo<Integer> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanChangeNodeToken()) {
				logger.info("用户{}重置节点令牌,ID:{}", user.getUsername(), requestVo.getVoData());
				nodeService.resetNodeTokenByNodeId(requestVo.getVoData());
			} else {
				logger.error("根据节点ID重置节点令牌发生错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据节点ID重置节点令牌发生错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("根据节点ID重置节点令牌发生错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/updateNodeDescriptionByNodeId" })
	@ResponseBody
	public ResponseVo<String> updateNodeDescriptionByNodeId(HttpServletRequest request, @RequestBody NodePo node) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteNode()) {
				logger.info("用户{}更新节点描述,ID:{}", user.getUsername(), node.getNodeId());
				nodeService.updateNodeDescriptionByNodeId(node);
			} else {
				logger.error("根据节点ID更新节点描述发生错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据节点ID更新节点描述发生错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("根据节点ID更新节点描述发生错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/createNode" })
	@ResponseBody
	public ResponseVo<String> createNode(HttpServletRequest request) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (user.isCanWriteNode()) {
				NodePo node = nodeService.createNode();
				logger.info("用户{}新增节点,节点ID:{}", user.getUsername(), node.getNodeId());
			} else {
				logger.error("新增节点发生错误,用户{}没有权限", user.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("新增节点发生错误,用户没有权限");
			}
		} catch (Exception e) {
			logger.error("新增节点发生错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

}
