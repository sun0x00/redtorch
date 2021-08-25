package xyz.redtorch.node.master.web.interceptor;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.web.vo.ResponseVo;
import xyz.redtorch.node.master.po.UserPo;
import xyz.redtorch.node.master.service.HaSessionMangerService;
import xyz.redtorch.node.master.service.NodeService;
import xyz.redtorch.node.master.web.socket.WebSocketServerHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Value("${rt.master.apiBasePath}")
    String apiBasePath;


    @Autowired
    private WebSocketServerHandler webSocketServerHandler;
    @Autowired
    private HaSessionMangerService haSessionMangerService;
    @Autowired
    private NodeService nodeService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        if (uri.startsWith(apiBasePath)) {

            // 获取鉴权Token
            String authToken = request.getHeader(CommonConstant.KEY_AUTH_TOKEN);

            if (StringUtils.isNotBlank(authToken)) {

                String sessionId = webSocketServerHandler.getSessionIdByAuthToken(authToken);
                if (sessionId == null) {
                    sessionId = "";
                }
                request.setAttribute(CommonConstant.KEY_WEBSOCKET_SESSION_ID, sessionId);

                Integer nodeId = nodeService.getNodeIdByToken(authToken);
                if (nodeId != null && nodeId != 0) {
                    request.setAttribute(CommonConstant.KEY_AUTH_TOKEN, authToken);
                    request.setAttribute(CommonConstant.KEY_NODE_ID, nodeId);
                    return true;
                } else {
                    UserPo userPo = haSessionMangerService.getUserPoByAuthToken(authToken);

                    if (!uri.equals(apiBasePath + "/checkLoginStatus")) {
                        haSessionMangerService.refreshAuthToken(authToken);
                    }

                    if (userPo != null) {
                        request.setAttribute(CommonConstant.KEY_AUTH_TOKEN, authToken);
                        request.setAttribute(CommonConstant.KEY_USER_PO, userPo);
                        return true;
                    }
                }
            }

            ResponseVo<String> responseVo = new ResponseVo<>();
            responseVo.setStatus(false);
            responseVo.setMessage("用户未登录");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().print(JSON.toJSONString(responseVo));
            return false;
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