package xyz.redtorch.desktop.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xyz.redtorch.desktop.service.ConfigService;
import xyz.redtorch.desktop.service.DesktopTradeCachesService;
import xyz.redtorch.desktop.service.HttpLoginService;

@Service
public class HttpLoginServiceImpl implements HttpLoginService {

    private static final Logger logger = LoggerFactory.getLogger(HttpLoginServiceImpl.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DesktopTradeCachesService desktopTradeCachesService;

    @Override
    public synchronized boolean login(String username, String password) {

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return false;
        }

        JSONObject reqDataJsonObject = new JSONObject();
        reqDataJsonObject.put("username", username);
        reqDataJsonObject.put("password", password);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/json");
        requestHeaders.add("Accept", "*/*");
        HttpEntity<String> requestEntity = new HttpEntity<String>(reqDataJsonObject.toJSONString(), requestHeaders);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(configService.getPriorityPriorityLoginURI(), HttpMethod.POST, requestEntity, String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                JSONObject resultJSONObject = JSON.parseObject(responseEntity.getBody());
                if (resultJSONObject.getBooleanValue("status")) {
                    JSONObject voData = resultJSONObject.getJSONObject("voData");
                    String authToken = voData.getString("randomAuthToken");
                    String operatorId = voData.getString("operatorId");
                    configService.setOperatorId(operatorId);
                    configService.setAuthToken(authToken);
                    logger.info("登录成功！");
                    new Thread(() -> {
                        desktopTradeCachesService.reloadData();
                    }).start();
                    return true;
                } else {
                    logger.error("登录失败！服务器返回错误!");
                    return false;
                }

            } else {
                logger.info("登录失败！");
                return false;
            }

        } catch (Exception e) {
            logger.error("登录请求错误!", e);
            return false;
        }
    }


}
