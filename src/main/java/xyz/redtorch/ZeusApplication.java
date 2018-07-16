package xyz.redtorch;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;

import xyz.redtorch.web.annotation.Authorization;
import xyz.redtorch.trader.gateway.GatewaySetting;
import xyz.redtorch.web.service.TokenService;
import xyz.redtorch.web.service.TradingService;
import xyz.redtorch.web.vo.ResultVO;


/**
 * @author sun0x00@gmail.com
 */
@SpringBootApplication
@RestController
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
@ServletComponentScan(basePackages = {"xyz.redtorch.web.filter"})
public class ZeusApplication {
	
	private Logger log = LoggerFactory.getLogger(ZeusApplication.class);
	
	@Autowired
	private TokenService tokenService;
	@Autowired
	private TradingService tradingService;
	
	@RequestMapping("/")
	public String greeting() {
		return "Redtorch Application";
	}
	
	@RequestMapping("/getToken")
	@ResponseBody
	public ResultVO getToken(String username, String password) {

		ResultVO result = new ResultVO();
		String token = tokenService.login(username, password);
		if(StringUtils.isEmpty(token)) {
			result.setResultCode(ResultVO.ERROR);
		}else {
			result.setData(token);
		}
		return result;
	}
	
	@RequestMapping("/tokenValidate")
	@ResponseBody
	public ResultVO tokenValidate(String token) {

		ResultVO result = new ResultVO();
		
		if(!tokenService.validate(token)) {
			result.setResultCode(ResultVO.ERROR);
		}
		
		return result;
	}
	
	
	@RequestMapping(value = "/sendOrder",method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO sendOrder(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();
		
		String gatewayID = jsonObject.getString("gatewayID");
		String rtSymbol = jsonObject.getString("rtSymbol");
		double price = jsonObject.getDouble("price");
		int  volume = jsonObject.getInteger("volume");
		String priceType = jsonObject.getString("priceType");
		String direction = jsonObject.getString("direction");
		String offset = jsonObject.getString("offset");
		
		if(StringUtils.isEmpty(gatewayID)
				||StringUtils.isEmpty(rtSymbol)
				||StringUtils.isEmpty(priceType)
				||StringUtils.isEmpty(direction)
				||StringUtils.isEmpty(offset)
				|| volume<=0) {
			result.setResultCode(ResultVO.ERROR);
			result.setMessage("参数不正确");
			return result;
		}
		String orderID = tradingService.sendOrder(gatewayID, rtSymbol, price, volume, priceType, direction, offset);
		result.setData(orderID);
		
		return result;
	}
	
	@RequestMapping(value = "/cancelOrder",method = RequestMethod.POST)
	@ResponseBody
	public ResultVO cancelOrder(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();
		
		if(!tokenService.validate(jsonObject.getString("token"))) {
			result.setResultCode(ResultVO.ERROR);
			return result;
		}
		if(StringUtils.isEmpty(jsonObject.getString("rtOrderID"))) {
			result.setResultCode(ResultVO.ERROR);
		}else {
			tradingService.cancelOrder(jsonObject.getString("rtOrderID"));
		}
		
		return result;
	}
	
	@RequestMapping(value = "/cancelAllOrders",method = RequestMethod.POST)
	@ResponseBody
	public ResultVO cancelAllOrders(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();
		
		if(!tokenService.validate(jsonObject.getString("token"))) {
			result.setResultCode(ResultVO.ERROR);
			return result;
		}
		tradingService.cancelAllOrders();
		
		return result;
	}
	
	@RequestMapping(value = "/subscribe",method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO subscribe(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if(!tradingService.subscribe(jsonObject.getString("rtSymbol"), jsonObject.getString("gatewayID"))) {
			result.setResultCode(ResultVO.ERROR);
		}
		
		return result;
	}
	@RequestMapping(value = "/unsubscribe",method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO unsubscribe(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();

		if(!tradingService.unsubscribe(jsonObject.getString("rtSymbol"), jsonObject.getString("gatewayID"))) {
			result.setResultCode(ResultVO.ERROR);
		}
		
		return result;
	}
	
	@RequestMapping(value = "/saveGateway",method = RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO saveGateway(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();
		
		GatewaySetting gatewaySetting = jsonObject.toJavaObject(GatewaySetting.class);
		
		tradingService.saveOrUpdateGatewaySetting(gatewaySetting);
		
		return result;
	}
	
	
	@RequestMapping("/zeus/loadStrategy")
	@ResponseBody
	public ResultVO zeusStrategyLoad(String token) {

		ResultVO result = new ResultVO();
		
		if(!tokenService.validate(token)) {
			result.setResultCode(ResultVO.ERROR);
			return result;
		}
		tradingService.zeusLoadStrategy();
		
		//result.setData(tradingService.getGatewaySettings());
		
		return result;
	}
	@RequestMapping("/zeus/getStrategyInfos")
	@ResponseBody
	@Authorization
	public ResultVO zeusGetStrategyInfos(String token) {

		ResultVO result = new ResultVO();

		
		List<Map<String,Object>> strategyInfos = tradingService.zeusGetStrategyInfos();
		
		result.setData(strategyInfos);
		
		return result;
	}
	
	@RequestMapping(value="/zeus/changeStrategyStatus",method=RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO zeusChangeStrategyStatus(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();
		
		if(!jsonObject.containsKey("actionType")) {
			result.setResultCode(ResultVO.ERROR);
			return result;
		}
		
		String type = jsonObject.getString("actionType");
		
		String strategyID = jsonObject.getString("strategyID");
		
		if(!type.contains("All")) {
			if(StringUtils.isEmpty(strategyID)) {
				result.setResultCode(ResultVO.ERROR);
				return result;
			}
		}
		if("init".equals(type)) {
			tradingService.zeusInitStrategy(strategyID);
		}else if("start".equals(type)) {
			tradingService.zeusSartStrategy(strategyID);
		}else if("stop".equals(type)) {
			tradingService.zeusStopStrategy(strategyID);
		}else if("initAll".equals(type)) {
			tradingService.zeusInitAllStrategy();
		}else if("startAll".equals(type)) {
			tradingService.zeusSartAllStrategy();
		}else if("stopAll".equals(type)) {
			tradingService.zeusStopAllStrategy();
		}else if("reload".equals(type)) {
			tradingService.zeusReloadStrategy(strategyID);
		}
		
		return result;
	}
	
	@RequestMapping("/getGatewaySettings")
	@ResponseBody
	@Authorization
	public ResultVO getGatewaySettings(String token) {

		ResultVO result = new ResultVO();

		
		result.setData(tradingService.getGatewaySettings());
		
		return result;
	}
	
	@RequestMapping(value = "/deleteGateway",method= RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO deleteGateway(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();
		
		
		if(!jsonObject.containsKey("gatewayID")) {
			result.setResultCode(ResultVO.ERROR);
			return result;
		}
		
		tradingService.deleteGateway(jsonObject.getString("gatewayID"));
		
		return result;
	}
	@RequestMapping(value ="/changeGatewayConnectStatus",method= RequestMethod.POST)
	@ResponseBody
	@Authorization
	public ResultVO changeGatewayConnectStatus(@RequestBody JSONObject jsonObject) {

		ResultVO result = new ResultVO();
		
		if(!jsonObject.containsKey("gatewayID")) {
			result.setResultCode(ResultVO.ERROR);
			return result;
		}
		tradingService.changeGatewayConnectStatus(jsonObject.getString("gatewayID"));
		
		return result;
	}
	
	
	@RequestMapping("/getAccounts")
	@ResponseBody
	@Authorization
	public ResultVO getAccounts(String token) {

		ResultVO result = new ResultVO();

		
		result.setData(tradingService.getAccounts());
		return result;
	}
	
	@RequestMapping("/getTrades")
	@ResponseBody
	@Authorization
	public ResultVO getTrades(String token) {

		ResultVO result = new ResultVO();

		result.setData(tradingService.getTrades());
		return result;
	}
	
	@RequestMapping("/getOrders")
	@ResponseBody
	@Authorization
	public ResultVO getOrders(String token) {

		ResultVO result = new ResultVO();


		result.setData(tradingService.getOrders());
		return result;
	}
	
	@RequestMapping("/getLocalPositionDetails")
	@ResponseBody
	@Authorization
	public ResultVO getLocalPositionDetails(String token) {

		ResultVO result = new ResultVO();

		result.setData(tradingService.getLocalPositionDetails());
		return result;
	}
	
	@RequestMapping("/getPositions")
	@ResponseBody
	@Authorization
	public ResultVO getPositions(String token) {

		ResultVO result = new ResultVO();


		result.setData(tradingService.getPositions());
		return result;
	}
	
	
	@RequestMapping("/getContracts")
	@ResponseBody
	@Authorization
	public ResultVO getContracts(String token) {

		ResultVO result = new ResultVO();

		result.setData(tradingService.getContracts());
		return result;
	}
	
	@RequestMapping("/getLogs")
	@ResponseBody
	@Authorization
	public ResultVO getLogs(String token) {

		ResultVO result = new ResultVO();

		result.setData(tradingService.getLogDatas());
		return result;
	}
	
    @Bean  
    public SocketIOServer socketIOServer(@Value("${rt.web.socketio.host}")String host,@Value("${rt.web.socketio.port}") Integer port) {  
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();  
        
        config.setHostname(host);
        config.setPort(port);  
  
        config.setAuthorizationListener(new AuthorizationListener() {//类似过滤器 
            @Override 
            public boolean isAuthorized(HandshakeData data) { 
                // protocol//host:port?token=xx
                String token = data.getSingleUrlParam("token"); 
                if(tokenService.validate(token)){
                    log.info("SocketIO认证成功,用户{},Token-{} Address-{}",tokenService.getUsername(token), token, data.getAddress());
                    return true; 
                }
                
                log.warn("SocketIO认证失败,Token-{} Address-{}",token,data.getAddress());
            	return false;
            } 
        });  
  
        final SocketIOServer socketIOServer = new SocketIOServer(config);  
        return socketIOServer;  
    }  
  
    @Bean  
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {  
        return new SpringAnnotationScanner(socketServer);  
    }  
    @Bean 
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    	return new PropertySourcesPlaceholderConfigurer();
    }
	public static void main(String[] args) {
		
		SpringApplication.run(ZeusApplication.class, args);
		
		// 屏蔽MongoDB驱动中的无效错误
		// ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver.cluster").setLevel(Level.ERROR);
	}
}
