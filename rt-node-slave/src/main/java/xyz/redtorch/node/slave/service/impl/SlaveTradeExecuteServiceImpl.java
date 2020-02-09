package xyz.redtorch.node.slave.service.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.gateway.GatewayApi;
import xyz.redtorch.node.slave.service.SlaveSystemService;
import xyz.redtorch.node.slave.service.SlaveTradeCachesService;
import xyz.redtorch.node.slave.service.SlaveTradeExecuteService;
import xyz.redtorch.pb.CoreField.CancelOrderReqField;
import xyz.redtorch.pb.CoreField.CommonReqField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.SubmitOrderReqField;

@Service
public class SlaveTradeExecuteServiceImpl implements SlaveTradeExecuteService {

	private static final Logger logger = LoggerFactory.getLogger(SlaveTradeExecuteServiceImpl.class);

	@Autowired
	private SlaveTradeCachesService slaveTradeCachesService;
	@Autowired
	private SlaveSystemService slaveSystemService;
	private Map<String, SubmitOrderReqField> submitOrderReqMap = new ConcurrentHashMap<>(5000);

	private Map<String, String> originOrderIdToOrderIdMap = new ConcurrentHashMap<>(5000);
	private Map<String, String> orderIdToOriginOrderIdMap = new ConcurrentHashMap<>(5000);

	private Set<String> originOrderIdSet = new HashSet<>(5000);

	@Override
	public String submitOrder(CommonReqField commonReq, SubmitOrderReqField submitOrderReq) {

		if (submitOrderReq == null) {
			logger.error("提交定单错误，参数submitOrderReq缺失");
			throw new IllegalArgumentException("提交定单错误，参数submitOrderReq缺失");
		} else if (submitOrderReq.getContract() == null) {
			logger.error("提交定单错误，参数contract缺失");
			throw new IllegalArgumentException("提交定单错误，参数contract缺失");
		} else if (StringUtils.isBlank(submitOrderReq.getOriginOrderId())) {
			logger.error("提交定单错误,原始定单ID为空");
			throw new IllegalArgumentException("提交定单错误,原始定单ID为空");
		} else if (StringUtils.isBlank(submitOrderReq.getGatewayId())) {
			logger.error("提交定单错误,网关ID为空");
			throw new IllegalArgumentException("提交定单错误,网关ID为空");
		} else if (slaveSystemService.getGatewayApi(submitOrderReq.getGatewayId()) == null) {
			logger.error("提交定单错误,网关不存在,网关ID:{}", submitOrderReq.getGatewayId());
			throw new RuntimeException("提交定单错误,网关不存在,网关ID:" + submitOrderReq.getGatewayId());
		} else {

			if (!originOrderIdSet.contains(submitOrderReq.getOriginOrderId())) {
				originOrderIdSet.add(submitOrderReq.getOriginOrderId());
			} else {
				logger.error("提交定单错误,原始定单ID重复,原始订单ID{}", submitOrderReq.getOriginOrderId());
				throw new RuntimeException("提交定单错误,原始定单ID重复,原始订单ID:" + submitOrderReq.getOriginOrderId());
			}

			GatewayApi gatewayApi = slaveSystemService.getGatewayApi(submitOrderReq.getGatewayId());
			String orderId = gatewayApi.submitOrder(submitOrderReq);
			originOrderIdToOrderIdMap.put(submitOrderReq.getOriginOrderId(), orderId);
			orderIdToOriginOrderIdMap.put(orderId, submitOrderReq.getOriginOrderId());
			submitOrderReqMap.put(orderId, submitOrderReq);
			logger.info("提交定单完成,原始定单ID:{},定单ID:{}", submitOrderReq.getOriginOrderId(), orderId);
			return orderId;
		}

	}

	@Override
	public boolean cancelOrder(CommonReqField commonReq, CancelOrderReqField cancelOrderReq) {

		if (cancelOrderReq == null) {
			logger.error("撤销定单错误，参数cancelOrderReq缺失");
			throw new IllegalArgumentException("撤销定单错误，参数cancelOrderReq缺失");
		} else if (StringUtils.isBlank(cancelOrderReq.getOrderId()) && StringUtils.isBlank(cancelOrderReq.getOriginOrderId())) {
			logger.error("撤销定单错误，参数orderId与originOrderId同时缺失");
			throw new IllegalArgumentException("撤销定单错误，参数orderId与originOrderId同时缺失");
		} else if (StringUtils.isBlank(cancelOrderReq.getOrderId()) && StringUtils.isBlank(originOrderIdToOrderIdMap.get(cancelOrderReq.getOriginOrderId()))) {
			logger.error("撤销定单错误,无有效定单ID");
			throw new IllegalArgumentException("撤销定单错误,无有效定单ID");
		} else {
			String orderId = cancelOrderReq.getOrderId();
			if (StringUtils.isBlank(orderId)) {
				orderId = originOrderIdToOrderIdMap.get(cancelOrderReq.getOriginOrderId());
			}
			SubmitOrderReqField submitOrderReq = submitOrderReqMap.get(orderId);

			if (submitOrderReq == null) {
				OrderField order = slaveTradeCachesService.queryOrderByOrderId(orderId);
				if (order == null) {
					logger.error("撤销定单错误,无定单请求或订单回报");
					throw new RuntimeException("撤销定单错误,无定单请求或订单回报");
				} else {
					logger.info("撤销定单记录,原始定单ID:{},定单ID:{}", order.getOriginOrderId(), orderId);
					String gatewayId = order.getContract().getGatewayId();

					GatewayApi gatewayApi = slaveSystemService.getGatewayApi(gatewayId);

					if (gatewayApi == null) {
						logger.error("撤销定单错误,网关不存在,网关ID:{}", gatewayId);
						throw new RuntimeException("撤销定单错误,网关不存在,网关ID:" + gatewayId);
					} else {
						boolean result = gatewayApi.cancelOrder(cancelOrderReq);
						logger.info("撤销定单已经执行,原始定单ID:{},定单ID:{},网关返回结果:{}", order.getOriginOrderId(), orderId, result);
						return result;
					}
				}

			} else {
				logger.info("撤销定单记录,原始定单ID:{},定单ID:{}", submitOrderReq.getOriginOrderId(), orderId);
				String gatewayId = submitOrderReq.getGatewayId();

				GatewayApi gatewayApi = slaveSystemService.getGatewayApi(gatewayId);

				if (gatewayApi == null) {
					logger.error("撤销定单错误,网关不存在,网关ID:{}", gatewayId);
					throw new RuntimeException("撤销定单错误,网关不存在,网关ID:" + gatewayId);
				} else {
					boolean result = gatewayApi.cancelOrder(cancelOrderReq);
					logger.info("撤销定单已经执行,原始定单ID:{},定单ID:{},网关返回结果:{}", submitOrderReq.getOriginOrderId(), orderId, result);
					return result;
				}
			}
		}
	}

	@Override
	public void searchContract(CommonReqField commonReq, ContractField contract) {
		if (contract == null) {
			logger.error("搜索合约错误，参数contract缺失");
			throw new IllegalArgumentException("订阅错误，参数contract缺失");
		} else if (StringUtils.isBlank(contract.getGatewayId())) {
			logger.error("搜索合约错误,网关ID为空");
			throw new IllegalArgumentException("搜索合约错误,网关ID为空");
		} else if (slaveSystemService.getGatewayApi(contract.getGatewayId()) == null) {
			logger.error("搜索合约错误,网关不存在,网关ID:{}", contract.getGatewayId());
			throw new RuntimeException("搜索合约错误,网关不存在,网关ID:" + contract.getGatewayId());
		} else {
			logger.info("搜索合约请求记录,合约:{}", contract.toString());
			GatewayApi gatewayApi = slaveSystemService.getGatewayApi(contract.getGatewayId());
			gatewayApi.searchContract(contract);
		}
	}

}
