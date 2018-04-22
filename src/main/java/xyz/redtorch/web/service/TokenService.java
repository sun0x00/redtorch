package xyz.redtorch.web.service;

/**
 * @author sun0x00@gmail.com
 */
public interface TokenService {
	String login(String username,String password);
	boolean validate(String token);
	void logout(String token);
	String getUsername(String token);
}
