package xyz.redtorch.node.master.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;

import xyz.redtorch.RtConstant;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.web.vo.ResponseVo;

@Component
public class LoginInterceptor implements HandlerInterceptor {

	@Value("${rt.node.master.web.api-base-path}")
	String apiBasePath;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		UserPo userPo = (UserPo) request.getSession().getAttribute(RtConstant.KEY_USER_PO);
		String uri = request.getRequestURI();
		if (uri.startsWith(apiBasePath)) {
			if (userPo == null) {
				ResponseVo<String> responseVo = new ResponseVo<>();
				responseVo.setStatus(false);
				responseVo.setMessage("用户未登录");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType("application/json; charset=utf-8");
				response.getWriter().print(JSON.toJSONString(responseVo));
				return false;
			} else {
				return true; // 如果session里有user，表示该用户已经登陆，放行，用户即可继续调用自己需要的接口
			}
		}

		if (uri.startsWith("/")) {
			return true;
		} else {
			response.sendRedirect(request.getContextPath() + "/");
			return false;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}