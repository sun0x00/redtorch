package xyz.redtorch.trader.rm;

import xyz.redtorch.trader.entity.CancelOrderReq;
import xyz.redtorch.trader.entity.OrderReq;

/**
 * 风控组件
 * @author sun0x00@gmail.com
 *
 */
public interface RiskManagement {
	boolean filterOrder(OrderReq orderReq);
	boolean filterCancelOrder(CancelOrderReq cancelOrderReq);
}
