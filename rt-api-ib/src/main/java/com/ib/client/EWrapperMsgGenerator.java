/* Copyright (C) 2018 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import static com.ib.controller.Formats.fmt;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EWrapperMsgGenerator {
    public static final String SCANNER_PARAMETERS = "SCANNER PARAMETERS:";
    public static final String FINANCIAL_ADVISOR = "FA:";
    
	public static String tickPrice( int tickerId, int field, double price, TickAttr attribs) {
    	return "id=" + tickerId + "  " + TickType.getField( field) + "=" + price + " " + 
        (attribs.canAutoExecute() ? " canAutoExecute" : " noAutoExecute") + " pastLimit = " + attribs.pastLimit() +
        (field == TickType.BID.index() || field == TickType.ASK.index() ? " preOpen = " + attribs.preOpen() : "");
    }
	
    public static String tickSize( int tickerId, int field, int size) {
    	return "id=" + tickerId + "  " + TickType.getField( field) + "=" + size;
    }
    
    public static String tickOptionComputation( int tickerId, int field, double impliedVol,
    		double delta, double optPrice, double pvDividend,
    		double gamma, double vega, double theta, double undPrice) {
		return "id=" + tickerId + "  " + TickType.getField( field) +
            ": impliedVol = " + Util.maxDoubleToString(impliedVol) +
            " delta = " + Util.maxDoubleToString(delta) +
            " gamma = " + Util.maxDoubleToString(gamma) +
            " vega = " + Util.maxDoubleToString(vega) +
            " theta = " + Util.maxDoubleToString(theta) +
            " optPrice = " + Util.maxDoubleToString(optPrice) +
            " pvDividend = " + Util.maxDoubleToString(pvDividend) +
            " undPrice = " + Util.maxDoubleToString(undPrice);
    }
    
    public static String tickGeneric(int tickerId, int tickType, double value) {
    	return "id=" + tickerId + "  " + TickType.getField( tickType) + "=" + value;
    }
    
    public static String tickString(int tickerId, int tickType, String value) {
    	return "id=" + tickerId + "  " + TickType.getField( tickType) + "=" + value;
    }
    
    public static String tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {
    	return "id=" + tickerId + "  " + TickType.getField(tickType)
		+ ": basisPoints = " + basisPoints + "/" + formattedBasisPoints
		+ " impliedFuture = " + impliedFuture + " holdDays = " + holdDays +
		" futureLastTradeDate = " + futureLastTradeDate + " dividendImpact = " + dividendImpact +
		" dividends to expiry = "	+ dividendsToLastTradeDate;
    }
    
    public static String orderStatus( int orderId, String status, double filled, double remaining,
            double avgFillPrice, int permId, int parentId, double lastFillPrice,
            int clientId, String whyHeld, double mktCapPrice) {
    	return "order status: orderId=" + orderId + " clientId=" + clientId + " permId=" + permId +
        " status=" + status + " filled=" + filled + " remaining=" + remaining +
        " avgFillPrice=" + avgFillPrice + " lastFillPrice=" + lastFillPrice +
        " parent Id=" + parentId + " whyHeld=" + whyHeld + " mktCapPrice=" + mktCapPrice;
    }
    
    public static String openOrder( int orderId, Contract contract, Order order, OrderState orderState) {
		final StringBuilder sb = new StringBuilder(1024);
        sb.append("open order: orderId=").append(orderId)
				.append(" action=").append(order.getAction())
                .append(" quantity=").append(order.totalQuantity())
                .append(" cashQty=").append(Util.DoubleMaxString(order.cashQty()))
                .append(" conid=").append(contract.conid())
                .append(" symbol=").append(contract.symbol())
                .append(" secType=").append(contract.getSecType())
                .append(" lastTradeDate=").append(contract.lastTradeDateOrContractMonth())
                .append(" strike=").append(contract.strike())
                .append(" right=").append(contract.getRight())
                .append(" multiplier=").append(contract.multiplier())
                .append(" exchange=").append(contract.exchange())
                .append(" primaryExch=").append(contract.primaryExch())
                .append(" currency=").append(contract.currency())
                .append(" localSymbol=").append(contract.localSymbol())
                .append(" tradingClass=").append(contract.tradingClass())
                .append(" type=").append(order.getOrderType())
                .append(" lmtPrice=").append(Util.DoubleMaxString(order.lmtPrice()))
                .append(" auxPrice=").append(Util.DoubleMaxString(order.auxPrice()))
                .append(" TIF=").append(order.getTif())
                .append(" localSymbol=").append(contract.localSymbol())
                .append(" client Id=").append(order.clientId())
                .append(" parent Id=").append(order.parentId())
                .append(" permId=").append(order.permId())
                .append(" outsideRth=").append(order.outsideRth())
                .append(" hidden=").append(order.hidden())
                .append(" discretionaryAmt=").append(order.discretionaryAmt())
                .append(" displaySize=").append(order.displaySize())
                .append(" triggerMethod=").append(order.getTriggerMethod())
				.append(" goodAfterTime=").append(order.goodAfterTime())
				.append(" goodTillDate=").append(order.goodTillDate())
				.append(" faGroup=").append(order.faGroup())
				.append(" faMethod=").append(order.getFaMethod())
				.append(" faPercentage=").append(order.faPercentage())
				.append(" faProfile=").append(order.faProfile())
				.append(" shortSaleSlot=").append(order.shortSaleSlot())
				.append(" designatedLocation=").append(order.designatedLocation())
				.append(" exemptCode=").append(order.exemptCode())
				.append(" ocaGroup=").append(order.ocaGroup())
				.append(" ocaType=").append(order.getOcaType())
				.append(" rule80A=").append(order.getRule80A())
				.append(" allOrNone=").append(order.allOrNone())
				.append(" minQty=").append(Util.IntMaxString(order.minQty()))
				.append(" percentOffset=").append( Util.DoubleMaxString(order.percentOffset()))
				.append(" eTradeOnly=").append(order.eTradeOnly())
				.append(" firmQuoteOnly=").append(order.firmQuoteOnly())
				.append(" nbboPriceCap=").append(Util.DoubleMaxString(order.nbboPriceCap()))
				.append(" optOutSmartRouting=").append(order.optOutSmartRouting())
				.append(" auctionStrategy=").append(order.auctionStrategy())
				.append(" startingPrice=").append(Util.DoubleMaxString(order.startingPrice()))
				.append(" stockRefPrice=").append(Util.DoubleMaxString(order.stockRefPrice()))
				.append(" delta=").append(Util.DoubleMaxString(order.delta()))
				.append(" stockRangeLower=").append(Util.DoubleMaxString(order.stockRangeLower()))
				.append(" stockRangeUpper=").append( Util.DoubleMaxString(order.stockRangeUpper()))
				.append(" volatility=").append(Util.DoubleMaxString(order.volatility()))
				.append(" volatilityType=").append(order.getVolatilityType())
				.append(" deltaNeutralOrderType=").append(order.getDeltaNeutralOrderType())
				.append(" deltaNeutralAuxPrice=").append(Util.DoubleMaxString(order.deltaNeutralAuxPrice()))
				.append(" deltaNeutralConId=").append(order.deltaNeutralConId())
				.append(" deltaNeutralSettlingFirm=").append(order.deltaNeutralSettlingFirm())
				.append(" deltaNeutralClearingAccount=").append(order.deltaNeutralClearingAccount())
				.append(" deltaNeutralClearingIntent=").append(order.deltaNeutralClearingIntent())
				.append(" deltaNeutralOpenClose=").append(order.deltaNeutralOpenClose())
				.append(" deltaNeutralShortSale=").append(order.deltaNeutralShortSale())
				.append(" deltaNeutralShortSaleSlot=").append(order.deltaNeutralShortSaleSlot())
				.append(" deltaNeutralDesignatedLocation=").append(order.deltaNeutralDesignatedLocation())
				.append(" continuousUpdate=").append(order.continuousUpdate())
				.append(" referencePriceType=").append(order.getReferencePriceType())
				.append(" trailStopPrice=").append(Util.DoubleMaxString(order.trailStopPrice()))
				.append(" trailingPercent=").append(Util.DoubleMaxString(order.trailingPercent()))
				.append(" scaleInitLevelSize=").append(Util.IntMaxString(order.scaleInitLevelSize()))
				.append(" scaleSubsLevelSize=").append(Util.IntMaxString(order.scaleSubsLevelSize()))
				.append(" scalePriceIncrement=").append(Util.DoubleMaxString(order.scalePriceIncrement()))
				.append(" scalePriceAdjustValue=").append(Util.DoubleMaxString(order.scalePriceAdjustValue()))
				.append(" scalePriceAdjustInterval=").append(Util.IntMaxString(order.scalePriceAdjustInterval()))
				.append(" scaleProfitOffset=").append(Util.DoubleMaxString(order.scaleProfitOffset()))
				.append(" scaleAutoReset=").append(order.scaleAutoReset())
				.append(" scaleInitPosition=").append(Util.IntMaxString(order.scaleInitPosition()))
				.append(" scaleInitFillQty=").append(Util.IntMaxString(order.scaleInitFillQty()))
				.append(" scaleRandomPercent=").append(order.scaleRandomPercent())
				.append(" hedgeType=").append(order.getHedgeType())
				.append(" hedgeParam=").append(order.hedgeParam())
				.append(" account=").append(order.account())
				.append(" modelCode=").append(order.modelCode())
				.append(" settlingFirm=").append(order.settlingFirm())
				.append(" clearingAccount=").append(order.clearingAccount())
				.append(" clearingIntent=").append(order.clearingIntent())
				.append(" notHeld=").append(order.notHeld())
				.append(" whatIf=").append(order.whatIf())
				.append(" solicited=").append(order.solicited())
				.append(" randomize size=").append(order.randomizeSize())
				.append(" randomize price=").append(order.randomizePrice())
				.append(" dontUseAutoPriceForHedge=").append(order.dontUseAutoPriceForHedge());
        

        if ("BAG".equals(contract.getSecType())) {
        	if (contract.comboLegsDescrip() != null) {
        		sb.append(" comboLegsDescrip=").append(contract.comboLegsDescrip());
        	}
        	
           	sb.append(" comboLegs={");
            if (contract.comboLegs() != null) {
            	for (int i = 0; i < contract.comboLegs().size(); ++i) {
            		ComboLeg comboLeg = contract.comboLegs().get(i);
            		sb.append(" leg ").append(i+1).append(": ")
							.append("conId=").append(comboLeg.conid())
							.append(" ratio=").append(comboLeg.ratio())
            		        .append(" action=").append(comboLeg.getAction())
							.append(" exchange=").append(comboLeg.exchange())
            		        .append(" openClose=").append(comboLeg.getOpenClose())
            		        .append(" shortSaleSlot=").append(comboLeg.shortSaleSlot())
            		        .append(" designatedLocation=").append(comboLeg.designatedLocation())
            		        .append(" exemptCode=").append(comboLeg.exemptCode());
            		if (order.orderComboLegs() != null && contract.comboLegs().size() == order.orderComboLegs().size()) {
            			OrderComboLeg orderComboLeg = order.orderComboLegs().get(i);
            			sb.append(" price=").append(Util.DoubleMaxString(orderComboLeg.price()));
            		}
            		sb.append(';');
            	}
            }
           	sb.append('}');
           	
        	if (order.basisPoints() != Double.MAX_VALUE) {
        		sb.append(" basisPoints=").append(Util.DoubleMaxString(order.basisPoints()))
						.append(" basisPointsType=").append(Util.IntMaxString(order.basisPointsType()));
        	}
        }
        
    	if (contract.deltaNeutralContract() != null) {
    		DeltaNeutralContract deltaNeutralContract = contract.deltaNeutralContract();
    		sb.append(" deltaNeutralContract.conId=").append(deltaNeutralContract.conid())
					.append(" deltaNeutralContract.delta=").append(deltaNeutralContract.delta())
					.append(" deltaNeutralContract.price=").append(deltaNeutralContract.price());
    	}
    	
        if (!Util.StringIsEmpty(order.getAlgoStrategy())) {
    		sb.append(" algoStrategy=").append(order.getAlgoStrategy()).append(" algoParams={");
    		if (order.algoParams() != null) {
				for (TagValue param : order.algoParams()) {
					sb.append(param.m_tag).append('=').append(param.m_value).append(',');
				}
				if (!order.algoParams().isEmpty()) {
					sb.setLength(sb.length() - 1);
				}
    		}
    		sb.append('}');
    	}
    	
        if ("BAG".equals(contract.getSecType())) {
        	sb.append(" smartComboRoutingParams={");
        	if (order.smartComboRoutingParams() != null) {
				for (TagValue param : order.smartComboRoutingParams()) {
					sb.append(param.m_tag).append('=').append(param.m_value).append(',');
				}
				if (!order.smartComboRoutingParams().isEmpty()) {
					sb.setLength(sb.length() - 1);
				}
        	}
        	sb.append('}');
        }

		sb.append(" status=").append(orderState.getStatus())
				.append(" initMarginBefore=").append(orderState.initMarginBefore() != null ? fmt(Double.parseDouble(orderState.initMarginBefore())) : "")
				.append(" maintMarginBefore=").append(orderState.maintMarginBefore() != null ? fmt(Double.parseDouble(orderState.maintMarginBefore())) : "")
				.append(" equityWithLoanBefore=").append(orderState.equityWithLoanBefore() != null ? fmt(Double.parseDouble(orderState.equityWithLoanBefore())) : "")
				.append(" initMarginChange=").append(orderState.initMarginChange() != null ? fmt(Double.parseDouble(orderState.initMarginChange())) : "")
				.append(" maintMarginChange=").append(orderState.maintMarginChange() != null ? fmt(Double.parseDouble(orderState.maintMarginChange())) : "")
				.append(" equityWithLoanChange=").append(orderState.equityWithLoanChange() != null ? fmt(Double.parseDouble(orderState.equityWithLoanChange())) : "")
				.append(" initMarginAfter=").append(fmt(Double.parseDouble(orderState.initMarginAfter())))
				.append(" maintMarginAfter=").append(fmt(Double.parseDouble(orderState.maintMarginAfter())))
				.append(" equityWithLoanAfter=").append(fmt(Double.parseDouble(orderState.equityWithLoanAfter())))
				.append(" commission=").append(Util.DoubleMaxString(orderState.commission()))
				.append(" minCommission=").append(Util.DoubleMaxString(orderState.minCommission()))
				.append(" maxCommission=").append(Util.DoubleMaxString(orderState.maxCommission()))
				.append(" commissionCurrency=").append(orderState.commissionCurrency())
				.append(" warningText=").append(orderState.warningText());

        return sb.toString();
    }
    
    public static String openOrderEnd() {
    	return " =============== end ===============";
    }
    
    public static String updateAccountValue(String key, String value, String currency, String accountName) {
    	return "updateAccountValue: " + key + " " + value + " " + currency + " " + accountName;
    }
    
    public static String updatePortfolio(Contract contract, double position, double marketPrice,
    									 double marketValue, double averageCost, double unrealizedPNL,
    									 double realizedPNL, String accountName) {
		return "updatePortfolio: "
            + contractMsg(contract)
            + position + " " + marketPrice + " " + marketValue + " " + averageCost + " " + unrealizedPNL + " " + realizedPNL + " " + accountName;
    }
    
    public static String updateAccountTime(String timeStamp) {
    	return "updateAccountTime: " + timeStamp;
    }
    
    public static String accountDownloadEnd(String accountName) {
    	return "accountDownloadEnd: " + accountName;
    }
    
    public static String nextValidId( int orderId) {
    	return "Next Valid Order ID: " + orderId;
    }
    
    public static String contractDetails(int reqId, ContractDetails contractDetails) {
    	Contract contract = contractDetails.contract();
		return "reqId = " + reqId + " ===================================\n"
            + " ---- Contract Details begin ----\n"
            + contractMsg(contract) + contractDetailsMsg(contractDetails)
            + " ---- Contract Details End ----\n";
    }
    
    private static String contractDetailsMsg(ContractDetails contractDetails) {
		return "marketName = " + contractDetails.marketName() + "\n"
        + "minTick = " + contractDetails.minTick() + "\n"
        + "price magnifier = " + contractDetails.priceMagnifier() + "\n"
        + "orderTypes = " + contractDetails.orderTypes() + "\n"
        + "validExchanges = " + contractDetails.validExchanges() + "\n"
        + "underConId = " + contractDetails.underConid() + "\n"
        + "longName = " + contractDetails.longName() + "\n"
        + "contractMonth = " + contractDetails.contractMonth() + "\n"
        + "industry = " + contractDetails.industry() + "\n"
        + "category = " + contractDetails.category() + "\n"
        + "subcategory = " + contractDetails.subcategory() + "\n"
        + "timeZoneId = " + contractDetails.timeZoneId() + "\n"
        + "tradingHours = " + contractDetails.tradingHours() + "\n"
        + "liquidHours = " + contractDetails.liquidHours() + "\n"
        + "evRule = " + contractDetails.evRule() + "\n"
        + "evMultiplier = " + contractDetails.evMultiplier() + "\n"
        + "mdSizeMultiplier = " + contractDetails.mdSizeMultiplier() + "\n"
        + "aggGroup = " + contractDetails.aggGroup() + "\n"
        + "underSymbol = " + contractDetails.underSymbol() + "\n"
        + "underSecType = " + contractDetails.underSecType() + "\n"
        + "marketRuleIds = " + contractDetails.marketRuleIds() + "\n"
        + "realExpirationDate = " + contractDetails.realExpirationDate() + "\n"
        + "lastTradeTime = " + contractDetails.lastTradeTime() + "\n"
        + contractDetailsSecIdList(contractDetails);
    }
    
	private static String contractMsg(Contract contract) {
		return "conid = " + contract.conid() + "\n"
        + "symbol = " + contract.symbol() + "\n"
        + "secType = " + contract.getSecType() + "\n"
        + "lastTradeDate = " + contract.lastTradeDateOrContractMonth() + "\n"
        + "strike = " + contract.strike() + "\n"
        + "right = " + contract.getRight() + "\n"
        + "multiplier = " + contract.multiplier() + "\n"
        + "exchange = " + contract.exchange() + "\n"
        + "primaryExch = " + contract.primaryExch() + "\n"
        + "currency = " + contract.currency() + "\n"
        + "localSymbol = " + contract.localSymbol() + "\n"
        + "tradingClass = " + contract.tradingClass() + "\n";
    }
	
    public static String bondContractDetails(int reqId, ContractDetails contractDetails) {
        Contract contract = contractDetails.contract();
		return "reqId = " + reqId + " ===================================\n"	
        + " ---- Bond Contract Details begin ----\n"
        + "symbol = " + contract.symbol() + "\n"
        + "secType = " + contract.getSecType() + "\n"
        + "cusip = " + contractDetails.cusip() + "\n"
        + "coupon = " + contractDetails.coupon() + "\n"
        + "maturity = " + contractDetails.maturity() + "\n"
        + "issueDate = " + contractDetails.issueDate() + "\n"
        + "ratings = " + contractDetails.ratings() + "\n"
        + "bondType = " + contractDetails.bondType() + "\n"
        + "couponType = " + contractDetails.couponType() + "\n"
        + "convertible = " + contractDetails.convertible() + "\n"
        + "callable = " + contractDetails.callable() + "\n"
        + "putable = " + contractDetails.putable() + "\n"
        + "descAppend = " + contractDetails.descAppend() + "\n"
        + "exchange = " + contract.exchange() + "\n"
        + "currency = " + contract.currency() + "\n"
        + "marketName = " + contractDetails.marketName() + "\n"
        + "tradingClass = " + contract.tradingClass() + "\n"
        + "conid = " + contract.conid() + "\n"
        + "minTick = " + contractDetails.minTick() + "\n"
        + "orderTypes = " + contractDetails.orderTypes() + "\n"
        + "validExchanges = " + contractDetails.validExchanges() + "\n"
        + "nextOptionDate = " + contractDetails.nextOptionDate() + "\n"
        + "nextOptionType = " + contractDetails.nextOptionType() + "\n"
        + "nextOptionPartial = " + contractDetails.nextOptionPartial() + "\n"
        + "notes = " + contractDetails.notes() + "\n"
        + "longName = " + contractDetails.longName() + "\n"
        + "evRule = " + contractDetails.evRule() + "\n"
        + "evMultiplier = " + contractDetails.evMultiplier() + "\n"
        + "mdSizeMultiplier = " + contractDetails.mdSizeMultiplier() + "\n"
        + "aggGroup = " + contractDetails.aggGroup() + "\n"
        + "marketRuleIds = " + contractDetails.marketRuleIds() + "\n"
        + "timeZoneId = " + contractDetails.timeZoneId() + "\n"
        + "lastTradeTime = " + contractDetails.lastTradeTime() + "\n"
        + contractDetailsSecIdList(contractDetails)
        + " ---- Bond Contract Details End ----\n";
    }
    
    private static String contractDetailsSecIdList(ContractDetails contractDetails) {
        final StringBuilder sb = new StringBuilder(32);
        sb.append("secIdList={");
        if (contractDetails.secIdList() != null) {
			for (TagValue param : contractDetails.secIdList()) {
				sb.append(param.m_tag).append("=").append(param.m_value).append(',');
			}
			if (!contractDetails.secIdList().isEmpty()) {
				sb.setLength(sb.length() - 1);
			}
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static String contractDetailsEnd(int reqId) {
    	return "reqId = " + reqId + " =============== end ===============";
    }
    
    public static String execDetails( int reqId, Contract contract, Execution execution) {
		return " ---- Execution Details begin ----\n"
        + "reqId = " + reqId + "\n"
        + "orderId = " + execution.orderId() + "\n"
        + "clientId = " + execution.clientId() + "\n"
        + contractMsg(contract)
        + "execId = " + execution.execId() + "\n"
        + "time = " + execution.time() + "\n"
        + "acctNumber = " + execution.acctNumber() + "\n"
        + "executionExchange = " + execution.exchange() + "\n"
        + "side = " + execution.side() + "\n"
        + "shares = " + execution.shares() + "\n"
        + "price = " + execution.price() + "\n"
        + "permId = " + execution.permId() + "\n"
        + "liquidation = " + execution.liquidation() + "\n"
        + "cumQty = " + execution.cumQty() + "\n"
        + "avgPrice = " + execution.avgPrice() + "\n"
        + "orderRef = " + execution.orderRef() + "\n"
        + "evRule = " + execution.evRule() + "\n"
        + "evMultiplier = " + execution.evMultiplier() + "\n"
        + "modelCode = " + execution.modelCode() + "\n"
        + "lastLiquidity = " + execution.lastLiquidity() + "\n"
        + " ---- Execution Details end ----\n";
    }
    
    public static String execDetailsEnd(int reqId) {
    	return "reqId = " + reqId + " =============== end ===============";
    }
    
    public static String updateMktDepth( int tickerId, int position, int operation, int side,
    									 double price, int size) {
    	return "updateMktDepth: " + tickerId + " " + position + " " + operation + " " + side + " " + price + " " + size;
    }
    
    public static String updateMktDepthL2( int tickerId, int position, String marketMaker,
    									   int operation, int side, double price, int size) {
    	return "updateMktDepth: " + tickerId + " " + position + " " + marketMaker + " " + operation + " " + side + " " + price + " " + size;
    }
    
    public static String updateNewsBulletin( int msgId, int msgType, String message, String origExchange) {
    	return "MsgId=" + msgId + " :: MsgType=" + msgType +  " :: Origin=" + origExchange + " :: Message=" + message;
    }
    
    public static String managedAccounts( String accountsList) {
    	return "Connected : The list of managed accounts are : [" + accountsList + "]";
    }
    
    public static String receiveFA(int faDataType, String xml) {
    	return FINANCIAL_ADVISOR + " " + EClient.faMsgTypeName(faDataType) + " " + xml;
    }
    
    public static String historicalData(int reqId, String date, double open, double high, double low,
                      					double close, long volume, int count, double WAP) {
    	return "id=" + reqId +
        " date = " + date +
        " open=" + open +
        " high=" + high +
        " low=" + low +
        " close=" + close +
        " volume=" + volume +
        " count=" + count +
        " WAP=" + WAP;
    }
    public static String historicalDataEnd(int reqId, String startDate, String endDate) {
    	return "id=" + reqId +
    			" start date = " + startDate +
    			" end date=" + endDate;
    }
    
	public static String realtimeBar(int reqId, long time, double open,
			double high, double low, double close, long volume, double wap, int count) {
        return "id=" + reqId +
        " time = " + time +
        " open=" + open +
        " high=" + high +
        " low=" + low +
        " close=" + close +
        " volume=" + volume +
        " count=" + count +
        " WAP=" + wap;
	}
	
    public static String scannerParameters(String xml) {
    	return SCANNER_PARAMETERS + "\n" + xml;
    }
    
    public static String scannerData(int reqId, int rank, ContractDetails contractDetails,
    								 String distance, String benchmark, String projection,
    								 String legsStr) {
        Contract contract = contractDetails.contract();
    	return "id = " + reqId +
        " rank=" + rank +
        " symbol=" + contract.symbol() +
        " secType=" + contract.getSecType() +
        " lastTradeDate=" + contract.lastTradeDateOrContractMonth() +
        " strike=" + contract.strike() +
        " right=" + contract.getRight() +
        " exchange=" + contract.exchange() +
        " currency=" + contract.currency() +
        " localSymbol=" + contract.localSymbol() +
        " marketName=" + contractDetails.marketName() +
        " tradingClass=" + contract.tradingClass() +
        " distance=" + distance +
        " benchmark=" + benchmark +
        " projection=" + projection +
        " legsStr=" + legsStr;
    }
    
    public static String scannerDataEnd(int reqId) {
    	return "id = " + reqId + " =============== end ===============";
    }
    
    public static String currentTime(long time) {
		return "current time = " + time +
		" (" + DateFormat.getDateTimeInstance().format(new Date(time * 1000)) + ")";
    }

    public static String fundamentalData(int reqId, String data) {
		return "id  = " + reqId + " len = " + data.length() + '\n' + data;
    }
    
    public static String deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
    	return "id = " + reqId
    	+ " deltaNeutralContract.conId =" + deltaNeutralContract.conid()
    	+ " deltaNeutralContract.delta =" + deltaNeutralContract.delta()
    	+ " deltaNeutralContract.price =" + deltaNeutralContract.price();
    }
    public static String tickSnapshotEnd(int tickerId) {
    	return "id=" + tickerId + " =============== end ===============";
    }
    
    public static String marketDataType(int reqId, int marketDataType){
    	return "id=" + reqId + " marketDataType = " + MarketDataType.getField(marketDataType);
    }
    
    public static String commissionReport( CommissionReport commissionReport) {
		return "commission report:" +
        " execId=" + commissionReport.m_execId +
        " commission=" + Util.DoubleMaxString(commissionReport.m_commission) +
        " currency=" + commissionReport.m_currency +
        " realizedPNL=" + Util.DoubleMaxString(commissionReport.m_realizedPNL) +
        " yield=" + Util.DoubleMaxString(commissionReport.m_yield) +
        " yieldRedemptionDate=" + Util.IntMaxString(commissionReport.m_yieldRedemptionDate);
    }
    
    public static String position( String account, Contract contract, double pos, double avgCost) {
		return " ---- Position begin ----\n"
        + "account = " + account + "\n"
        + contractMsg(contract)
        + "position = " + Util.DoubleMaxString(pos) + "\n"
        + "avgCost = " + Util.DoubleMaxString(avgCost) + "\n"
        + " ---- Position end ----\n";
    }    

    public static String positionEnd() {
        return " =============== end ===============";
    }

    public static String accountSummary( int reqId, String account, String tag, String value, String currency) {
		return " ---- Account Summary begin ----\n"
        + "reqId = " + reqId + "\n"
        + "account = " + account + "\n"
        + "tag = " + tag + "\n"
        + "value = " + value + "\n"
        + "currency = " + currency + "\n"
        + " ---- Account Summary end ----\n";
    }

    public static String accountSummaryEnd( int reqId) {
    	return "id=" + reqId + " =============== end ===============";
    }

    public static String positionMulti( int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
		return " ---- Position begin ----\n"
        + "id = " + reqId + "\n"
        + "account = " + account + "\n"
        + "modelCode = " + modelCode + "\n"
        + contractMsg(contract)
        + "position = " + Util.DoubleMaxString(pos) + "\n"
        + "avgCost = " + Util.DoubleMaxString(avgCost) + "\n"
        + " ---- Position end ----\n";
    }    

    public static String positionMultiEnd( int reqId) {
        return "id = " + reqId + " =============== end ===============";
    }

    public static String accountUpdateMulti( int reqId, String account, String modelCode, String key, String value, String currency) {
		return " id = " + reqId + " account = " + account + " modelCode = " + modelCode + 
                " key = " + key + " value = " + value + " currency = " + currency;
    }

    public static String accountUpdateMultiEnd( int reqId) {
    	return "id = " + reqId + " =============== end ===============";
    }    

	public static String securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass,
			String multiplier, Set<String> expirations, Set<Double> strikes) {
		final StringBuilder sb = new StringBuilder(128);
		sb.append(" id = ").append(reqId)
				.append(" exchange = ").append(exchange)
				.append(" underlyingConId = ").append(underlyingConId)
				.append(" tradingClass = ").append(tradingClass)
				.append(" multiplier = ").append(multiplier)
				.append(" expirations: ");
		for (String expiration : expirations) {
			sb.append(expiration).append(", ");
		}
		sb.append(" strikes: ");
		for (Double strike : strikes) {
			sb.append(strike).append(", ");
		}
		return sb.toString();
	}

	public static String securityDefinitionOptionalParameterEnd( int reqId) {
		return "id = " + reqId + " =============== end ===============";
	}

	public static String softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		StringBuilder sb = new StringBuilder();
		sb.append("==== Soft Dollar Tiers Begin (total=").append(tiers.length).append(") reqId: ").append(reqId).append(" ====\n");
		for (int i = 0; i < tiers.length; i++) {
			sb.append("Soft Dollar Tier [").append(i).append("] - name: ").append(tiers[i].name())
					.append(", value: ").append(tiers[i].value()).append("\n");
		}
		sb.append("==== Soft Dollar Tiers End (total=").append(tiers.length).append(") ====\n");

		return sb.toString();
	}

	public static String familyCodes(FamilyCode[] familyCodes) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("==== Family Codes Begin (total=").append(familyCodes.length).append(") ====\n");
        for (int i = 0; i < familyCodes.length; i++) {
            sb.append("Family Code [").append(i)
					.append("] - accountID: ").append(familyCodes[i].accountID())
					.append(", familyCode: ").append(familyCodes[i].familyCodeStr())
					.append("\n");
        }
        sb.append("==== Family Codes End (total=").append(familyCodes.length).append(") ====\n");

        return sb.toString();
    }

    public static String symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("==== Symbol Samples Begin (total=").append(contractDescriptions.length).append(") reqId: ").append(reqId).append(" ====\n");
        for (int i = 0; i < contractDescriptions.length; i++) {
            sb.append("---- Contract Description Begin (").append(i).append(") ----\n");
            sb.append("conId: ").append(contractDescriptions[i].contract().conid()).append("\n");
            sb.append("symbol: ").append(contractDescriptions[i].contract().symbol()).append("\n");
            sb.append("secType: ").append(contractDescriptions[i].contract().secType()).append("\n");
            sb.append("primaryExch: ").append(contractDescriptions[i].contract().primaryExch()).append("\n");
            sb.append("currency: ").append(contractDescriptions[i].contract().currency()).append("\n");
            sb.append("derivativeSecTypes (total=").append(contractDescriptions[i].derivativeSecTypes().length).append("): ");
            for (int j = 0; j < contractDescriptions[i].derivativeSecTypes().length; j++){
                sb.append(contractDescriptions[i].derivativeSecTypes()[j]).append(' ');
            }
            sb.append("\n");
            sb.append("---- Contract Description End (").append(i).append(") ----\n");
        }
        sb.append("==== Symbol Samples End (total=").append(contractDescriptions.length).append(") reqId: ").append(reqId).append(" ====\n");

        return sb.toString();
    }

	public static String mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
		StringBuilder sb = new StringBuilder();
		sb.append("==== Market Depth Exchanges Begin (total=").append(depthMktDataDescriptions.length).append(") ====\n");
		for (int i = 0; i < depthMktDataDescriptions.length; i++) {
			sb.append("Depth Market Data Description [").append(i).append("] - exchange: ").append(depthMktDataDescriptions[i].exchange())
					.append(", secType: ").append(depthMktDataDescriptions[i].secType())
					.append(", listingExch: ").append(depthMktDataDescriptions[i].listingExch())
					.append(", serviceDataType: ").append(depthMktDataDescriptions[i].serviceDataType())
					.append(", aggGroup: ").append(depthMktDataDescriptions[i].aggGroup() != Integer.MAX_VALUE ? 
							depthMktDataDescriptions[i].aggGroup() : "").append("\n");
		}
		sb.append("==== Market Depth Exchanges End (total=").append(depthMktDataDescriptions.length).append(") ====\n");
		return sb.toString();
	}

	public static String tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {
		return "TickNews. tickerId: " + tickerId + ", timeStamp: " + Util.UnixMillisecondsToString(timeStamp, "yyyy-MM-dd HH:mm:ss zzz") + 
				", providerCode: " + providerCode + ", articleId: " + articleId + ", headline: " + headline + ", extraData: " + extraData;
	}

	public static String newsProviders(NewsProvider[] newsProviders) {
		StringBuilder sb = new StringBuilder();
		sb.append("==== News Providers Begin (total=").append(newsProviders.length).append(") ====\n");
		for (int i = 0; i < newsProviders.length; i++) {
			sb.append("News Provider [").append(i).append("] - providerCode: ").append(newsProviders[i].providerCode()).append(", providerName: ")
					.append(newsProviders[i].providerName()).append("\n");
		}
		sb.append("==== News Providers End (total=").append(newsProviders.length).append(") ====\n");

		return sb.toString();
	}

    public static String error( Exception ex) { return "Error - " + ex;}
    public static String error( String str) { return str;}

	public static String error(int id, int errorCode, String errorMsg) {
		return id + " | " + errorCode + " | " + errorMsg;
	}

	public static String connectionClosed() {
		return "Connection Closed";
	}

	public static String softDollarTiers(SoftDollarTier[] tiers) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("==== Soft Dollar Tiers Begin (total=").append(tiers.length).append(") ====\n");
		
		for (SoftDollarTier tier : tiers) {
			sb.append(tier).append("\n");
		}
		
		sb.append("==== Soft Dollar Tiers End (total=").append(tiers.length).append(") ====\n");
		
		return sb.toString();
	}

	public static String tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		return "id=" + tickerId + " minTick = " + minTick + " bboExchange = " + bboExchange + " snapshotPermissions = " + snapshotPermissions;
	}

	public static String smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("==== Smart Components Begin (total=").append(theMap.entrySet().size()).append(") reqId = ").append(reqId).append("====\n");
		
		for (Map.Entry<Integer, Entry<String, Character>> item : theMap.entrySet()) {
			sb.append("bit number: ").append(item.getKey()).append(", exchange: ").append(item.getValue().getKey()).append(", exchange letter: ").append(item.getValue().getValue()).append("\n");
		}
		
		sb.append("==== Smart Components End (total=").append(theMap.entrySet().size()).append(") reqId = ").append(reqId).append("====\n");
		
		return sb.toString();
	}

	public static String newsArticle(int requestId, int articleType, String articleText) {
		StringBuilder sb = new StringBuilder();
		sb.append("==== News Article Begin requestId: ").append(requestId).append(" ====\n");
		if (articleType == 0) {
			sb.append("---- Article type is text or html ----\n");
			sb.append(articleText).append("\n");
		} else if (articleType == 1) {
			sb.append("---- Article type is binary/pdf ----\n");
			sb.append("Binary/pdf article text cannot be displayed\n");
		}
		sb.append("==== News Article End requestId: ").append(requestId).append(" ====\n");
		return sb.toString();
	}
	
	public static String historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
		return "Historical News. RequestId: " + requestId + ", time: " + time + ", providerCode: " + providerCode + 
				", articleId: " + articleId + ", headline: " + headline;
	}

	public static String historicalNewsEnd( int requestId, boolean hasMore) {
		return "Historical News End. RequestId: " + requestId + ", hasMore: " + hasMore;
	}

	public static String headTimestamp(int reqId, String headTimestamp) {		
		return "Head timestamp. Req Id: " + reqId + ", headTimestamp: " + headTimestamp;
	}

	public static String histogramData(int reqId, List<HistogramEntry> items) {
		StringBuilder sb = new StringBuilder();		
		sb.append("Histogram data. Req Id: ").append(reqId).append(", Data (").append(items.size()).append("):\n");		
		items.forEach(i -> sb.append("\tPrice: ").append(i.price).append(", Size: ").append(i.size).append("\n"));
		return sb.toString();
	}
	
	public static String rerouteMktDataReq(int reqId, int conId, String exchange) {
		return "Re-route market data request. Req Id: " + reqId + ", Con Id: " + conId + ", Exchange: " + exchange;
	}

	public static String rerouteMktDepthReq(int reqId, int conId, String exchange) {
		return "Re-route market depth request. Req Id: " + reqId + ", Con Id: " + conId + ", Exchange: " + exchange;
	}
	
	public static String marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
		DecimalFormat df = new DecimalFormat("#.#");
		df.setMaximumFractionDigits(340);
		StringBuilder sb = new StringBuilder(256);
		sb.append("==== Market Rule Begin (marketRuleId=").append(marketRuleId).append(") ====\n");
		for (PriceIncrement priceIncrement : priceIncrements) {
			sb.append("Low Edge: ").append(df.format(priceIncrement.lowEdge()));
			sb.append(", Increment: ").append(df.format(priceIncrement.increment()));
			sb.append("\n");
		}
		sb.append("==== Market Rule End (marketRuleId=").append(marketRuleId).append(") ====\n");
		return sb.toString();
	}
	

    public static String pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
		return "Daily PnL. Req Id: " + reqId + ", daily PnL: " + dailyPnL + ", unrealizedPnL: " + unrealizedPnL + ", realizedPnL: " + realizedPnL;
    }
    
    public static String pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
		return "Daily PnL Single. Req Id: " + reqId + ", pos: " + pos + ", daily PnL: " + dailyPnL + ", unrealizedPnL: " + unrealizedPnL + ", realizedPnL: " + realizedPnL + ", value: " + value;
    }

    public static String historicalTick(int reqId, long time, double price, long size) {
        return "Historical Tick. Req Id: " + reqId + ", time: " + Util.UnixSecondsToString(time, "yyyyMMdd-HH:mm:ss zzz") + ", price: " + price + ", size: " 
                + size;
    }

    public static String historicalTickBidAsk(int reqId, long time, int mask, double priceBid, double priceAsk,
            long sizeBid, long sizeAsk) {
        return "Historical Tick Bid/Ask. Req Id: " + reqId + ", time: " + Util.UnixSecondsToString(time, "yyyyMMdd-HH:mm:ss zzz") + ", bid price: " + priceBid 
                + ", ask price: " + priceAsk + ", bid size: " + sizeBid + ", ask size: " + sizeAsk;
    }

    public static String historicalTickLast(int reqId, long time, int mask, double price, long size, String exchange,
            String specialConditions) {        
        return "Historical Tick Last. Req Id: " + reqId + ", time: " + Util.UnixSecondsToString(time, "yyyyMMdd-HH:mm:ss zzz") + ", price: " + price + ", size: " 
                + size + ", exchange: " + exchange + ", special conditions:" + specialConditions;
    }
    
    public static String tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttr attribs, 
            String exchange, String specialConditions){
        return (tickType == 1 ? "Last." : "AllLast.") +
                " Req Id: " + reqId + " Time: " + Util.UnixSecondsToString(time, "yyyyMMdd-HH:mm:ss zzz") + " Price: " + price + " Size: " + size +
                " Exch: " + exchange + " Spec Cond: " + specialConditions + (attribs.pastLimit() ? " pastLimit" : "") +
                (tickType == 1 ? "" : (attribs.unreported() ? " unreported" : ""));
    }
    
    public static String tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
            TickAttr attribs){
        return "BidAsk. Req Id: " + reqId + " Time: " + Util.UnixSecondsToString(time, "yyyyMMdd-HH:mm:ss zzz") + " BidPrice: " + bidPrice + 
                " AskPrice: " + askPrice + " BidSize: " + bidSize + " AskSize: " + askSize + 
                (attribs.bidPastLow() ? " bidPastLow" : "") + (attribs.askPastHigh() ? " askPastHigh" : "");
    }

    public static String tickByTickMidPoint(int reqId, long time, double midPoint){
        return "MidPoint. Req Id: " + reqId + " Time: " + Util.UnixSecondsToString(time, "yyyyMMdd-HH:mm:ss zzz") + " MidPoint: " + midPoint;
    }
}