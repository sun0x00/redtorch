package xyz.redtorch.web.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import xyz.redtorch.web.service.TokenService;

/**
 * @author sun0x00@gmail.com
 */
@Service
public class TokenServiceImpl implements TokenService,InitializingBean {

	private Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

	public static Map<String, String> tokenMap = new HashMap<>();

	@Value("${rt.web.username}")
	String usernameConf;
	@Value("${rt.web.password}")
	String passwordConf;
	@Value("#{'${rt.web.token.preset}'.split(',')}")
	private List<String> presetTokenList;

	@Override
	public void afterPropertiesSet() throws Exception {
		for(String presetToken: presetTokenList) {
			// 预置一些不需要走登录流程的Token
			tokenMap.put(presetToken,"PRESET");
		}
		
	}
	
	@Override
	public String login(String username, String password) {
		if (usernameConf != null && usernameConf.equals(username) && passwordConf != null
				&& passwordConf.equals(password)) {
			// Base64编码后看起来太乱
			// String token = CommonUtil.generateUuidAsBase64();
			String token = UUID.randomUUID().toString().replace("-", "").toLowerCase();
			tokenMap.put(token, username);
			log.info("用户{}登录成功,Token-{}", username, token);
			return token;
		} else {
			return null;
		}
	}

	@Override
	public boolean validate(String token) {
		if (tokenMap.containsKey(token)) {
			return true;
		}
		return false;
	}

	@Override
	public void logout(String token) {
		if (tokenMap.containsKey(token)) {
			tokenMap.remove(token);
		}
	}

	@Override
	public String getUsername(String token) {
		return tokenMap.get(token);
	}

}
