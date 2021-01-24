package xyz.redtorch.node.master.util;

import xyz.redtorch.node.master.po.ContractPo;
import xyz.redtorch.pb.CoreEnum.CombinationTypeEnum;
import xyz.redtorch.pb.CoreEnum.CurrencyEnum;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.OptionsTypeEnum;
import xyz.redtorch.pb.CoreEnum.ProductClassEnum;
import xyz.redtorch.pb.CoreField.ContractField;

public class BeanUtils {
	public static ContractPo contractFieldToContractPo(ContractField contractField) {
		ContractPo contractPo = new ContractPo();
		contractPo.setCombinationType(contractField.getCombinationTypeValue());
		contractPo.setContractId(contractField.getContractId());
		contractPo.setCurrency(contractField.getCurrencyValue());
		contractPo.setExchange(contractField.getExchangeValue());
		contractPo.setFullName(contractField.getFullName());
		contractPo.setGatewayId(contractField.getGatewayId());
		contractPo.setLastTradeDateOrContractMonth(contractField.getLastTradeDateOrContractMonth());
		contractPo.setLongMarginRatio(contractField.getLongMarginRatio());
		contractPo.setMaxLimitOrderVolume(contractField.getMaxLimitOrderVolume());
		contractPo.setMaxMarginSideAlgorithm(contractField.getMaxMarginSideAlgorithm());
		contractPo.setMaxMarketOrderVolume(contractField.getMaxMarketOrderVolume());
		contractPo.setMinLimitOrderVolume(contractField.getMinLimitOrderVolume());
		contractPo.setMinMarketOrderVolume(contractField.getMinMarketOrderVolume());
		contractPo.setMultiplier(contractField.getMultiplier());
		contractPo.setName(contractField.getName());
		contractPo.setOptionsType(contractField.getOptionsTypeValue());
		contractPo.setPriceTick(contractField.getPriceTick());
		contractPo.setProductClass(contractField.getProductClassValue());
		contractPo.setShortMarginRatio(contractField.getShortMarginRatio());
		contractPo.setStrikePrice(contractField.getStrikePrice());
		contractPo.setSymbol(contractField.getSymbol());
		contractPo.setThirdPartyId(contractField.getThirdPartyId());
		contractPo.setUnderlyingMultiplier(contractField.getUnderlyingMultiplier());
		contractPo.setUnderlyingSymbol(contractField.getUnderlyingSymbol());
		contractPo.setUnifiedSymbol(contractField.getUnifiedSymbol());
		return contractPo;
	}
	
	public static ContractField contractPoToContractField(ContractPo contractPo) {
		
		ContractField.Builder contractFieldBuilder = ContractField.newBuilder();
		
		contractFieldBuilder.setCombinationType(CombinationTypeEnum.forNumber(contractPo.getCombinationType()));
		contractFieldBuilder.setContractId(contractPo.getContractId());
		contractFieldBuilder.setCurrency(CurrencyEnum.forNumber(contractPo.getCurrency()));
		contractFieldBuilder.setExchange(ExchangeEnum.forNumber(contractPo.getExchange()));
		contractFieldBuilder.setFullName(contractPo.getFullName());
		contractFieldBuilder.setGatewayId(contractPo.getGatewayId());
		contractFieldBuilder.setLastTradeDateOrContractMonth(contractPo.getLastTradeDateOrContractMonth());
		contractFieldBuilder.setLongMarginRatio(contractPo.getLongMarginRatio());
		contractFieldBuilder.setMaxLimitOrderVolume(contractPo.getMaxLimitOrderVolume());
		contractFieldBuilder.setMaxMarginSideAlgorithm(contractPo.getMaxMarginSideAlgorithm());
		contractFieldBuilder.setMaxMarketOrderVolume(contractPo.getMaxMarketOrderVolume());
		contractFieldBuilder.setMinLimitOrderVolume(contractPo.getMinLimitOrderVolume());
		contractFieldBuilder.setMinMarketOrderVolume(contractPo.getMinMarketOrderVolume());
		contractFieldBuilder.setMultiplier(contractPo.getMultiplier());
		contractFieldBuilder.setName(contractPo.getName());
		contractFieldBuilder.setOptionsType(OptionsTypeEnum.forNumber(contractPo.getOptionsType()));
		contractFieldBuilder.setPriceTick(contractPo.getPriceTick());
		contractFieldBuilder.setProductClass(ProductClassEnum.forNumber(contractPo.getProductClass()));
		contractFieldBuilder.setShortMarginRatio(contractPo.getShortMarginRatio());
		contractFieldBuilder.setStrikePrice(contractPo.getStrikePrice());
		contractFieldBuilder.setSymbol(contractPo.getSymbol());
		contractFieldBuilder.setThirdPartyId(contractPo.getThirdPartyId());
		contractFieldBuilder.setUnderlyingMultiplier(contractPo.getUnderlyingMultiplier());
		contractFieldBuilder.setUnderlyingSymbol(contractPo.getUnderlyingSymbol());
		contractFieldBuilder.setUnifiedSymbol(contractPo.getUnifiedSymbol());
		return contractFieldBuilder.build();
	}
}
