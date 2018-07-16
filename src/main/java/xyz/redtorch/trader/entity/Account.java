package xyz.redtorch.trader.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Account implements Serializable{
	
	private static final long serialVersionUID = 6823946394104654905L;

	private String gatewayID; // 接口

	// 账号代码相关
	private String accountID; // 账户代码
	private String rtAccountID; // 账户在vt中的唯一代码,通常是 Gateway名.账户代码

	// 数值相关
	private double preBalance; // 昨日账户结算净值
	private double balance; // 账户净值
	private double available; // 可用资金
	private double commission; // 今日手续费
	private double margin; // 保证金占用
	private double closeProfit; // 平仓盈亏
	private double positionProfit; // 持仓盈亏
	public String getGatewayID() {
		return gatewayID;
	}
	public void setGatewayID(String gatewayID) {
		this.gatewayID = gatewayID;
	}
	public String getAccountID() {
		return accountID;
	}
	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}
	public String getRtAccountID() {
		return rtAccountID;
	}
	public void setRtAccountID(String rtAccountID) {
		this.rtAccountID = rtAccountID;
	}
	public double getPreBalance() {
		return preBalance;
	}
	public void setPreBalance(double preBalance) {
		this.preBalance = preBalance;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public double getAvailable() {
		return available;
	}
	public void setAvailable(double available) {
		this.available = available;
	}
	public double getCommission() {
		return commission;
	}
	public void setCommission(double commission) {
		this.commission = commission;
	}
	public double getMargin() {
		return margin;
	}
	public void setMargin(double margin) {
		this.margin = margin;
	}
	public double getCloseProfit() {
		return closeProfit;
	}
	public void setCloseProfit(double closeProfit) {
		this.closeProfit = closeProfit;
	}
	public double getPositionProfit() {
		return positionProfit;
	}
	public void setPositionProfit(double positionProfit) {
		this.positionProfit = positionProfit;
	}
	@Override
	public String toString() {
		return "Account [gatewayID=" + gatewayID + ", accountID=" + accountID + ", rtAccountID=" + rtAccountID
				+ ", preBalance=" + preBalance + ", balance=" + balance + ", available=" + available + ", commission="
				+ commission + ", margin=" + margin + ", closeProfit=" + closeProfit + ", positionProfit="
				+ positionProfit + "]";
	}

}
