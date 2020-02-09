package xyz.redtorch.desktop.web.vo;

public class ResponseVo<T> {

	private boolean status = true;
	private T voData;
	private String message;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public T getVoData() {
		return voData;
	}

	public void setVoData(T voData) {
		this.voData = voData;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
