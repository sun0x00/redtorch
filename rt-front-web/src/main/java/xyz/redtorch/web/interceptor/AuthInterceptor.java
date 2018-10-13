package xyz.redtorch.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.redtorch.web.annotation.Authorization;
import xyz.redtorch.web.servlet.http.BodyReaderHttpServletRequestWrapper;
import xyz.redtorch.web.vo.ResultVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static xyz.redtorch.web.service.impl.TokenServiceImpl.tokenMap;

import java.util.HashSet;

public class AuthInterceptor extends HandlerInterceptorAdapter {
	
	Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
	
	private Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

	public static final HashSet<String> presetUriPermitSet = new HashSet<String>() {
		private static final long serialVersionUID = -6226049537971596206L;
		{
			add("/api/core/sendOrder");
			add("/api/core/cancelOrder");
			add("/api/core/subscribe");
			add("/api/core/gateways");
			add("/api/core/accounts");
			add("/api/core/trades");
			add("/api/core/orders");
			add("/api/core/localPositionDetails");
			add("/api/core/positions");
			add("/api/core/contracts");
			add("/api/core/ticks");
		}}; 

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		

		String uri = request.getRequestURI();
		
		if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
			Authorization auth = ((HandlerMethod) handler).getMethodAnnotation(Authorization.class);
			if (auth == null) {
				return true;
			} else {

				try {
					JSONObject rbJSONObject = JSON
							.parseObject(new BodyReaderHttpServletRequestWrapper(request).getBodyString(request));
					if (rbJSONObject != null && rbJSONObject.containsKey("token")) {
						if (tokenMap.containsKey(rbJSONObject.get("token"))) {
							return true;
						}
					}
				} catch (Exception e) {
					logger.info("解析RequestBody发生异常", e);
				}

				String token = request.getParameter("token");
				if (tokenMap.containsKey(token)) {
					if("PRESET".equals(tokenMap.get(token))) {
						if(presetUriPermitSet.contains(uri)) {
							return true;
						}else {
							log.warn("访问被阻断,URI不在授权范围内,URI-[{}]",uri);
							return false;
						}
					}else {
						return true;
					}
				} else {
					ResultVO result = new ResultVO();
					result.setStatus(ResultVO.ERROR);
					result.setMessage("请求中应包含令牌信息");
					response.setStatus(HttpStatus.UNAUTHORIZED.value());
					response.setContentType("application/json; charset=utf-8");
					response.getWriter().print(JSON.toJSONString(result));
					return false;
				}
			}
		} else {
			return true;
		}
	}
}
