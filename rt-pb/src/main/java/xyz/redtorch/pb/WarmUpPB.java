package xyz.redtorch.pb;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import xyz.redtorch.pb.CoreEnum.CurrencyEnum;
import xyz.redtorch.pb.CoreEnum.ExchangeEnum;
import xyz.redtorch.pb.CoreEnum.ProductTypeEnum;
import xyz.redtorch.pb.CoreField.AccountField;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.ContractField;
import xyz.redtorch.pb.CoreField.GatewayField;
import xyz.redtorch.pb.CoreField.OrderField;
import xyz.redtorch.pb.CoreField.PositionField;
import xyz.redtorch.pb.CoreField.TickField;
import xyz.redtorch.pb.CoreField.TradeField;

public class WarmUpPB {
	
	static Logger log = LoggerFactory.getLogger(WarmUpPB.class);
	// TODO 这个方法需要进一步填充
	static public void start(){
	
		try {
			log.info("Protocol Buffers 开始预热");
			for(var i = 0;i < 11000; i++) {
				var gatewayField = GatewayField.newBuilder()
						.setDescription("qwertyuiop中文混合")
						.setGatewayId("01f2f497-d851-4188-8045-d71eec09d827")
						.setName("测试网关").build();
				
				GatewayField.parseFrom(gatewayField.toByteArray());
				
				var contractField = ContractField.newBuilder()
						.setCurrency(CurrencyEnum.CNY)
						.setExchange(ExchangeEnum.CFE)
						.setGateway(gatewayField)
						.setFullName("测试合约全名")
						.setContractId("ABC@DCE@FUTURES@01f2f497-d851-4188-8045-d71eec09d827")
						.setLastTradeDateOrContractMonth("197001")
						.setLongMarginRatio(Double.MAX_VALUE)
						.setShortMarginRatio(Double.MIN_VALUE)
						.setMaxMarginSideAlgorithm(false)
						.setMultiplier(Double.MIN_VALUE)
						.setPriceTick(Double.MIN_NORMAL)
						.setProductType(ProductTypeEnum.FUTURES)
						.setShortName("测试合约")
						.setSymbol("ABC")
						.setThirdPartyId("1234567890")
						.setUnifiedSymbol("ABC@DCE@FUTURES").build();
				
				var askPriceList = new ArrayList<Double>();
				askPriceList.add(Double.MAX_VALUE);
				askPriceList.add(Double.MIN_VALUE);
				askPriceList.add(Double.MAX_VALUE);
				askPriceList.add(Double.MIN_VALUE);
				askPriceList.add(Double.MAX_VALUE);
				askPriceList.add(Double.MIN_VALUE);
				askPriceList.add(Double.MAX_VALUE);
				askPriceList.add(Double.MIN_VALUE);
				askPriceList.add(Double.MAX_VALUE);
				askPriceList.add(Double.MIN_VALUE);
				
				var bidPriceList = new ArrayList<Double>();
				bidPriceList.add(Double.MAX_VALUE);
				bidPriceList.add(Double.MIN_VALUE);
				bidPriceList.add(Double.MAX_VALUE);
				bidPriceList.add(Double.MIN_VALUE);
				bidPriceList.add(Double.MAX_VALUE);
				bidPriceList.add(Double.MIN_VALUE);
				bidPriceList.add(Double.MAX_VALUE);
				bidPriceList.add(Double.MIN_VALUE);
				bidPriceList.add(Double.MAX_VALUE);
				bidPriceList.add(Double.MIN_VALUE);
				
				var askVolumeList = new ArrayList<Integer>();
				askVolumeList.add(Integer.MAX_VALUE);
				askVolumeList.add(Integer.MIN_VALUE);
				askVolumeList.add(Integer.MAX_VALUE);
				askVolumeList.add(Integer.MIN_VALUE);
				askVolumeList.add(Integer.MAX_VALUE);
				askVolumeList.add(Integer.MIN_VALUE);
				askVolumeList.add(Integer.MAX_VALUE);
				askVolumeList.add(Integer.MIN_VALUE);
				askVolumeList.add(Integer.MAX_VALUE);
				askVolumeList.add(Integer.MIN_VALUE);
				
				var bidVolumeList = new ArrayList<Integer>();
				bidVolumeList.add(Integer.MAX_VALUE);
				bidVolumeList.add(Integer.MIN_VALUE);
				bidVolumeList.add(Integer.MAX_VALUE);
				bidVolumeList.add(Integer.MIN_VALUE);
				bidVolumeList.add(Integer.MAX_VALUE);
				bidVolumeList.add(Integer.MIN_VALUE);
				bidVolumeList.add(Integer.MAX_VALUE);
				bidVolumeList.add(Integer.MIN_VALUE);
				bidVolumeList.add(Integer.MAX_VALUE);
				bidVolumeList.add(Integer.MIN_VALUE);
				
				
				TickField.parseFrom(TickField.newBuilder()
						.addAllAskPrice(askPriceList)
						.addAllBidPrice(bidPriceList)
						.addAllAskVolume(askVolumeList)
						.addAllBidVolume(bidVolumeList)
						.setDataSourceId("01f2f497-d851-4188-8045-d71eec09d827")
						.setActionDay("19700101")
						.setActionTime("00:00:00.000")
						.setActionTimestamp(2147454847000L)
						.setStatus(Integer.MAX_VALUE)
						.setLastPrice(Double.MAX_VALUE)
						.setAvgPrice(Double.MAX_VALUE)
						.setTotalBidVol(Integer.MAX_VALUE)
						.setTotalAskVol(Integer.MIN_VALUE)
						.setWeightedAvgAskPrice(Double.MAX_VALUE)
						.setWeightedAvgBidPrice(Double.MIN_VALUE)
						.setIopv(Double.MIN_VALUE)
						.setYieldToMaturity(Double.MAX_VALUE)
						.setVolume(Integer.MAX_VALUE)
						.setTurnover(Double.MAX_VALUE)
						.setNumTrades(Long.MAX_VALUE)
						.setOpenInterest(Double.MAX_VALUE)
						.setPreOpenInterest(Double.MIN_VALUE)
						.setSettlePrice(Double.MAX_VALUE)
						.setPreSettlePrice(Double.MIN_VALUE)
						.setOpenPrice(Double.MAX_VALUE)
						.setHighPrice(Double.MAX_VALUE)
						.setLowPrice(Double.MIN_VALUE)
						.setUpperLimit(Double.MAX_VALUE)
						.setLowerLimit(Double.MIN_VALUE)
						.setContract(contractField)
						.build().toByteArray());
				
				
				BarField.parseFrom(BarField.newBuilder().build().toByteArray());

				AccountField.parseFrom(AccountField.newBuilder().build().toByteArray());
				OrderField.parseFrom(OrderField.newBuilder().build().toByteArray());
				TradeField.parseFrom(TradeField.newBuilder().build().toByteArray());
				PositionField.parseFrom(PositionField.newBuilder().build().toByteArray());
			}
			log.info("Protocol Buffers 完成预热");
		} catch (InvalidProtocolBufferException e) {
			log.error("Protocol Buffers 预热失败",e);
		}
	}
}
