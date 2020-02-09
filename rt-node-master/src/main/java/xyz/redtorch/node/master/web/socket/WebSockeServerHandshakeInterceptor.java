package xyz.redtorch.node.master.web.socket;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import xyz.redtorch.RtConstant;
import xyz.redtorch.node.master.po.UserPo;

@Component
public class WebSockeServerHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(WebSockeServerHandshakeInterceptor.class);

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
			HttpServletRequest servletRequest = serverRequest.getServletRequest();

			try {
				if (servletRequest.getSession().getAttribute(RtConstant.KEY_USER_PO) != null) {
					UserPo userPo = (UserPo) servletRequest.getSession().getAttribute(RtConstant.KEY_USER_PO);
					attributes.put(RtConstant.KEY_NODE_ID, userPo.getRecentlyNodeId());
					attributes.put(RtConstant.KEY_OPERATOR_ID, userPo.getOperatorId());
					attributes.put(WebSocketConstant.KEY_SKIP_LOGIN, true);
				} else {
					String nodeIdStr = servletRequest.getParameter(RtConstant.KEY_NODE_ID);
					String token = servletRequest.getParameter(RtConstant.KEY_TOKEN);
					String operatorId = servletRequest.getParameter(RtConstant.KEY_OPERATOR_ID);

					if (StringUtils.isBlank(nodeIdStr)) {
						logger.error("连接前校验登录字段失败,未获取到参数nodeId,远程地址{}", request.getRemoteAddress().toString());
						return false;
					}
					if (StringUtils.isBlank(token)) {
						logger.error("连接前校验登录字段失败,未获取到参数token,远程地址{}", request.getRemoteAddress().toString());
						return false;
					}
					if (StringUtils.isBlank(operatorId)) {
						logger.error("连接前校验登录字段失败,未获取到参数operatorId,远程地址{}", request.getRemoteAddress().toString());
						return false;
					}
					int nodeId = Integer.valueOf(nodeIdStr).intValue();
					attributes.put(RtConstant.KEY_TOKEN, token);
					attributes.put(RtConstant.KEY_NODE_ID, nodeId);
					attributes.put(RtConstant.KEY_OPERATOR_ID, operatorId);
				}

				String skipTradeEvents = servletRequest.getParameter(WebSocketConstant.KEY_SKIP_TRADE_EVENTS);
				if (!StringUtils.isBlank(skipTradeEvents) && "true".equals(skipTradeEvents)) {
					attributes.put(WebSocketConstant.KEY_SKIP_TRADE_EVENTS, true);
				}

			} catch (Exception e) {
				logger.error("连接前校验登录字段失败,发生异常!{}", request.getRemoteAddress().toString(), e);
				return false;
			}
			return super.beforeHandshake(request, response, wsHandler, attributes);

		} else {
			logger.error("连接前获取ServletServerHttpRequest失败!");
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
		super.afterHandshake(request, response, wsHandler, ex);
	}
}