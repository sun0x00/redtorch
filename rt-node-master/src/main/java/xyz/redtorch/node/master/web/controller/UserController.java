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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import xyz.redtorch.RtConstant;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.UserService;
import xyz.redtorch.node.master.web.vo.RequestVo;
import xyz.redtorch.node.master.web.vo.ResponseVo;

@Controller
@RequestMapping("${rt.node.master.web.api-base-path}/management/user")
public class UserController {
	
	private Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@RequestMapping(value = { "/changePassword" }, method = { RequestMethod.POST })
	@ResponseBody
	public ResponseVo<UserPo> changePassword(HttpServletRequest request, @RequestBody UserPo user) {
		ResponseVo<UserPo> responseVo = new ResponseVo<>();
		try {
			if (user == null) {
				responseVo.setStatus(false);
				responseVo.setMessage("修改密码失败,未找到请求体");
			} else {
				if (request.getSession().getAttribute(RtConstant.KEY_USER_PO) != null) {
					UserPo sessionUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
					logger.info("用户{}修改密码,地址{}:{}", sessionUser.getUsername(), request.getRemoteAddr(), request.getRemotePort());
					if ("admin".equals(sessionUser.getUsername())) {
						responseVo.setStatus(false);
						responseVo.setMessage("修改密码失败,admin用户仅允许通过配置文件修改密码");
						return responseVo;
					} else {
						user.setUsername(sessionUser.getUsername());
						String resString = userService.userChangePassword(user);
						if (!StringUtils.isAllBlank(resString)) {
							responseVo.setStatus(false);
							responseVo.setMessage(resString);
						}
					}
				} else {
					responseVo.setStatus(false);
					responseVo.setMessage("修改密码失败,SESSION中未能找到用户");
				}
			}
		} catch (Exception e) {
			logger.error("修改密码异常", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/getUserList" }, method = { RequestMethod.GET })
	@ResponseBody
	public ResponseVo<List<UserPo>> getUserList(HttpServletRequest request) {
		ResponseVo<List<UserPo>> responseVo = new ResponseVo<>();
		try {
			UserPo loginUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (loginUser.isCanReadUser()) {
				List<UserPo> userList = userService.getUserList();
				responseVo.setVoData(userList);
			} else {
				logger.error("查询用户列表错误,用户{}没有权限", loginUser.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("查询用户列表错误,没有权限");
			}
		} catch (Exception e) {
			logger.error("查询用户列表异常", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/deleteUserByUsername" })
	@ResponseBody
	public ResponseVo<String> deleteUserByUsername(HttpServletRequest request, @RequestBody RequestVo<String> requestVo) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo loginUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (loginUser.isCanWriteUser()) {
				logger.info("用户{}删除用户,用户名:{}", loginUser.getUsername(), requestVo.getVoData());
				userService.deleteUserByUsername(requestVo.getVoData());
			} else {
				logger.error("根据用户名删除用户错误,用户{}没有权限", loginUser.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据用户名删除用户错误,没有权限");
			}
		} catch (Exception e) {
			logger.error("删除用户发生错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/updateUserDescriptionByUsername" })
	@ResponseBody
	public ResponseVo<String> updateUserDescriptionByUsername(HttpServletRequest request, @RequestBody UserPo user) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo loginUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (loginUser.isCanWriteUser()) {
				logger.info("用户{}更新用户描述,用户名:{}", loginUser.getUsername(), user.getUsername());
				userService.updateUserDescriptionByUsername(user);
			} else {
				logger.error("根据用户名更新用户描述错误,用户{}没有权限", loginUser.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据用户名更新用户描述错误,没有权限");
			}
		} catch (Exception e) {
			logger.error("根据用户名更新用户描述错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/updateUserPasswordByUsername" })
	@ResponseBody
	public ResponseVo<String> updateUserPasswordByUsername(HttpServletRequest request, @RequestBody UserPo user) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {

			UserPo loginUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (loginUser.isCanWriteUser()) {
				logger.info("用户{}更新用户密码,用户名:{}", loginUser.getUsername(), user.getUsername());
				userService.updateUserPasswordByUsername(user);
			} else {
				logger.error("根据用户名更新用户密码错误,用户{}没有权限", loginUser.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据用户名更新用户密码错误,没有权限");
			}
		} catch (Exception e) {
			logger.error("根据用户名更新用户密码错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;

	}

	@RequestMapping(value = { "/addUser" })
	@ResponseBody
	public ResponseVo<String> addUser(HttpServletRequest request, @RequestBody UserPo user) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {

			UserPo loginUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (loginUser.isCanWriteUser()) {
				if (user == null) {
					responseVo.setStatus(false);
					responseVo.setMessage("参数user为空");
					return responseVo;
				}
				userService.addUser(user);
			} else {
				logger.error("新增用户错误,用户{}没有权限", loginUser.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("新增用户错误,没有权限");
			}
		} catch (Exception e) {
			logger.error("新增用户错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/updateUserPermissionByUsername" })
	@ResponseBody
	public ResponseVo<String> updateUserPermissionByUsername(HttpServletRequest request, @RequestBody UserPo user) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			UserPo loginUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
			if (loginUser.isCanWriteUser()) {
				logger.info("用户{}更新用户权限,用户名:{}", loginUser.getUsername(), user.getUsername());
				userService.updateUserPermissionByUsername(user);
			} else {
				logger.error("根据用户名更新用户权限错误,用户{}没有权限", loginUser.getUsername());
				responseVo.setStatus(false);
				responseVo.setMessage("根据用户名更新用户权限错误,没有权限");
			}
		} catch (Exception e) {
			logger.error("根据用户名更新用户权限错误", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

}
