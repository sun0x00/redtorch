package xyz.redtorch.web.vo;

/**
 * @author sun0x00@gmail.com
 */
public class ResultVO {
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	
	private String status = SUCCESS;
	private Object data;
	private String message;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}	
	
}
