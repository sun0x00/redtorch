package xyz.redtorch.node.master.web.vo;

public class RequestVo<T> {
	private T voData;

	public T getVoData() {
		return voData;
	}

	public void setVoData(T voData) {
		this.voData = voData;
	}
}
