package xyz.redtorch.node.master.web.controller;

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
import xyz.redtorch.node.master.web.vo.ResponseVo;

@Controller
@RequestMapping("${rt.node.master.web.api-base-path}")
public class SystemController {
	private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

	@Autowired
	private UserService userService;

	@RequestMapping(value = { "/login" }, method = { RequestMethod.POST })
	@ResponseBody
	public ResponseVo<UserPo> login(HttpServletRequest request, @RequestBody UserPo user) {
		ResponseVo<UserPo> responseVo = new ResponseVo<>();
		try {

			if (request.getSession().getAttribute(RtConstant.KEY_USER_PO) != null) {
				UserPo sessionUser = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
				logger.info("SESSION重复登录,提交用户{},已登录用户{},地址{}:{}", user.getUsername(), sessionUser.getUsername(), request.getRemoteAddr(), request.getRemotePort());
				responseVo.setVoData(sessionUser);
				responseVo.setMessage("SESSION重复登录,提交用户" + user.getUsername() + ",已登录用户" + sessionUser.getUsername());
				request.getSession().removeAttribute(RtConstant.KEY_USER_PO);
			} else {
				if (user == null) {
					responseVo.setStatus(false);
					responseVo.setMessage("登录验证失败,未找到请求体");
				} else {
					logger.info("用户{}尝试登录,地址{}:{}", user.getUsername(), request.getRemoteAddr(), request.getRemotePort());
					user.setRecentlyIpAddress(request.getRemoteHost());
					user.setRecentlyPort(request.getRemotePort());
					user.setRecentlySessionId(request.getSession().getId());
					UserPo loggedinUser = userService.userAuth(user);

					if (loggedinUser != null) {
						loggedinUser.setPassword(RtConstant.SECURITY_MASK);
						responseVo.setVoData(loggedinUser);
						responseVo.setMessage("登陆验证成功");
						logger.info("用户{}登录成功", user.getUsername());
						request.getSession().setAttribute(RtConstant.KEY_USER_PO, loggedinUser);
					} else {
						responseVo.setStatus(false);
						responseVo.setMessage("验证失败");
						logger.info("用户{}登录验证失败", user.getUsername());
					}
				}

			}
		} catch (Exception e) {
			logger.error("登录异常", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

	@RequestMapping(value = { "/keepLogin" }, method = { RequestMethod.GET })
	@ResponseBody
	public ResponseVo<String> keepLogin(HttpServletRequest request) {
		return new ResponseVo<>();
	}

	@RequestMapping(value = { "/logout" })
	@ResponseBody
	public ResponseVo<String> logout(HttpServletRequest request) {
		ResponseVo<String> responseVo = new ResponseVo<>();
		try {
			responseVo.setMessage("注销成功");
			if (request.getSession().getAttribute(RtConstant.KEY_USER_PO) != null) {
				UserPo user = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
				logger.info("用户{}注销,地址{}:{}", user.getUsername(), request.getRemoteAddr(), request.getRemotePort());
				request.getSession().removeAttribute(RtConstant.KEY_USER_PO);
			}
		} catch (Exception e) {
			logger.error("注销异常", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}

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
					responseVo.setMessage("修改密码失败,SESSION缓存中未能找到用户");
				}
			}
		} catch (Exception e) {
			logger.error("修改密码异常", e);
			responseVo.setStatus(false);
			responseVo.setMessage(e.getMessage());
		}
		return responseVo;
	}
}
