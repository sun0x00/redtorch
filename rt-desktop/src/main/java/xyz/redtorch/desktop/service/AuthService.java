package xyz.redtorch.desktop.service;

import org.springframework.http.HttpHeaders;

public interface AuthService {

	boolean login(String username, String password);

	boolean getLoginStatus();

	HttpHeaders getResponseHttpHeaders();

	String getOperatorId();

	Integer getNodeId();

	String getUsername();
}