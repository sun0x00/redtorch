package xyz.redtorch.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import xyz.redtorch.web.service.TokenService;
import xyz.redtorch.web.vo.ResultVO;

@Controller
@RequestMapping("${api.base.path}/token")
public class TokenController {

	@Autowired
	private TokenService tokenService;

	@RequestMapping(path = "/login", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO getToken(@RequestBody JSONObject jsonObject) {

		String userName = jsonObject.getString("userName");
		String password = jsonObject.getString("password");

		ResultVO result = new ResultVO();
		String token = tokenService.login(userName, password);
		if (StringUtils.isEmpty(token)) {
			result.setStatus(ResultVO.ERROR);
		} else {
			result.setData(token);
		}
		return result;
	}

	@RequestMapping("/validate")
	public ResponseEntity<ResultVO> tokenValidate(@RequestBody JSONObject jsonObject) {
		String token = jsonObject.getString("token");
		ResultVO result = new ResultVO();

		if (!tokenService.validate(token)) {
			result.setStatus(ResultVO.ERROR);
			result.setMessage("令牌未授权");
			return new ResponseEntity<ResultVO>(result, HttpStatus.UNAUTHORIZED);
		} else {
			result.setData(token);
			return new ResponseEntity<ResultVO>(result, HttpStatus.OK);
		}
	}

}
