package xyz.redtorch.common.util.bar;

import xyz.redtorch.pb.CoreField.BarField;
/**
 * CallBack接口,用于注册Bar生成器回调事件
 */
public interface CommonBarCallBack {
	void call(BarField bar);
}