package xyz.redtorch.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.redtorch.web.annotation.Authorization;
import xyz.redtorch.web.servlet.http.BodyReaderHttpServletRequestWrapper;
import xyz.redtorch.web.vo.ResultVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static xyz.redtorch.web.service.impl.TokenServiceImpl.tokenMap;

public class AuthInterceptor extends HandlerInterceptorAdapter {
	private Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
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
					return true;
				} else {
					ResultVO result = new ResultVO();
					result.setResultCode(ResultVO.ERROR);
					response.getWriter().print(JSON.toJSONString(result));
					return false;
				}
			}
		} else {
			return true;
		}
	}
}
