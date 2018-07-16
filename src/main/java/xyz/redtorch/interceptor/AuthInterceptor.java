package xyz.redtorch.interceptor;

import com.alibaba.fastjson.JSON;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import xyz.redtorch.author.annotation.Authorization;
import xyz.redtorch.web.vo.ResultVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static xyz.redtorch.web.service.impl.TokenServiceImpl.tokenMap;

public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            Authorization auth = ((HandlerMethod) handler).getMethodAnnotation(Authorization.class);
            if(auth == null){
                return true;
            }else {
                String token = request.getParameter("token");
                if(tokenMap.containsKey(token)){
                    return true;
                }else{
                    ResultVO result = new ResultVO();
                    result.setResultCode(ResultVO.ERROR);
                    response.getWriter().print(JSON.toJSONString(result));
                }
            }
        }else{
            return true;
        }
        return super.preHandle(request, response, handler);
    }
}
