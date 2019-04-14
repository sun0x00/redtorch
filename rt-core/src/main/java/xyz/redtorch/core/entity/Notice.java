package xyz.redtorch.core.entity;

import java.io.Serializable;
import java.time.LocalTime;

/**
 *  连接状态
 * @author kevinhuangwl
 *
 */
public class Notice implements Serializable {

	private static final long serialVersionUID = 8364092686476605949L;

	public enum Type{
		MarketDataConnected("行情连线"), 
		MarketDataDisconnected("行情断线"), 
		AccountConnected("账户连线"), 
		AccountDisconnected("账户断线"),
		PlacedOrder("委托下单"),
		WithdrawedOrder("撤单成功"),
		DoneTransaction("成交");
		
		String name;
		
		Type(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	private Type type;
	
	private String gatewayName;
	
	private String userID;
	
	private LocalTime time;
	
	private String message = "";
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
