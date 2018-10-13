package xyz.redtorch.core.rm;

import xyz.redtorch.core.entity.CancelOrderReq;
import xyz.redtorch.core.entity.OrderReq;

/**
 * 风控组件
 * 
 * @author sun0x00@gmail.com
 *
 */
public interface RiskManagement {
	boolean filterOrder(OrderReq orderReq);

	boolean filterCancelOrder(CancelOrderReq cancelOrderReq);
}
