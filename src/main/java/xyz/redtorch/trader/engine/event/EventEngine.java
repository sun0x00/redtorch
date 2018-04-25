package xyz.redtorch.trader.engine.event;
/**
 * @author sun0x00@gmail.com
 */
public interface EventEngine{
	/**
	 * 注册事件监听
	 * @param event 事件,一般是事件类型+ID
	 * @param eventListener 事件监听实例
	 */
    void registerListener(String event, EventListener eventListener);
    
    /**
     * 删除事件监听
     * @param event 事件,一般是事件类型+ID, 如果event为null,则删除参数Listener所有监听
     * @param eventListener 事件监听实例
     */
    void removeListener(String event, EventListener eventListener);
	
    /**
     * 发送事件
     * @param event 事件,一般是事件类型+ID
     * @param eventData 事件数据
     */
    void emit(String event, EventData eventData);
}