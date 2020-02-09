package xyz.redtorch.desktop.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import xyz.redtorch.desktop.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	private boolean loginStatus = false;

	private HttpHeaders responseHttpHeaders = null;

	private String operatorId = "";

	private Integer nodeId = null;

	private String username = null;

	@Value("${rt.http.login.uri}")
	private String loginUri;

	@Override
	public boolean login(String username, String password) {

		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			return false;
		}

		JSONObject reqDataJsonObject = new JSONObject();
		reqDataJsonObject.put("username", username);
		reqDataJsonObject.put("password", password);

		RestTemplate rest = new RestTemplate();
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Content-Type", "application/json");
		requestHeaders.add("Accept", "*/*");
		HttpEntity<String> requestEntity = new HttpEntity<String>(reqDataJsonObject.toJSONString(), requestHeaders);

		loginStatus = false;
		responseHttpHeaders = null;
		this.username = "";
		try {
			ResponseEntity<String> responseEntity = rest.exchange(loginUri, HttpMethod.POST, requestEntity, String.class);
			System.out.println(responseEntity.toString());
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JSONObject resultJSONObject = JSON.parseObject(responseEntity.getBody());
				if (resultJSONObject.getBooleanValue("status")) {
					JSONObject voData = resultJSONObject.getJSONObject("voData");

					nodeId = voData.getInteger("recentlyNodeId");
					operatorId = voData.getString("operatorId");
					this.username = username;
					loginStatus = true;
					responseHttpHeaders = responseEntity.getHeaders();
					logger.info("登录成功！");
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

	@Override
	public boolean getLoginStatus() {
		return loginStatus;
	}

	@Override
	public HttpHeaders getResponseHttpHeaders() {
		return responseHttpHeaders;
	}

	@Override
	public String getOperatorId() {
		return operatorId;
	}

	@Override
	public Integer getNodeId() {
		return nodeId;
	}

	@Override
	public String getUsername() {
		return username;
	}
}
