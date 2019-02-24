package xyz.redtorch.core.entity;

import java.io.Serializable;

/**
 * @author sun0x00@gmail.com
 */
public class Account implements Serializable {

	private static final long serialVersionUID = 6823946394104654905L;

	private String gatewayID; // 网关
	private String gatewayDisplayName; // 网关显示名称

	// 账号代码相关
	private String accountID; // 账户代码
	private String rtAccountID; // 账户在RedTorch中的唯一代码,通常    账户代码.币种.网关

	private String currency; // 币种
	// 数值相关
	private double preBalance; // 昨日账户结算净值
	private double balance; // 账户净值
	private double available; // 可用资金
	private double commission; // 今日手续费
	private double margin; // 保证金占用
	private double closeProfit; // 平仓盈亏
	private double positionProfit; // 持仓盈亏
	private double deposit; // 入金
	private double withdraw; // 出金
	public String getGatewayID() {
		return gatewayID;
	}
	public void setGatewayID(String gatewayID) {
		this.gatewayID = gatewayID;
	}
	public String getGatewayDisplayName() {
		return gatewayDisplayName;
	}
	public void setGatewayDisplayName(String gatewayDisplayName) {
		this.gatewayDisplayName = gatewayDisplayName;
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
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
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
	public double getDeposit() {
		return deposit;
	}
	public void setDeposit(double deposit) {
		this.deposit = deposit;
	}
	public double getWithdraw() {
		return withdraw;
	}
	public void setWithdraw(double withdraw) {
		this.withdraw = withdraw;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountID == null) ? 0 : accountID.hashCode());
		long temp;
		temp = Double.doubleToLongBits(available);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(balance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(closeProfit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(commission);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		temp = Double.doubleToLongBits(deposit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((gatewayDisplayName == null) ? 0 : gatewayDisplayName.hashCode());
		result = prime * result + ((gatewayID == null) ? 0 : gatewayID.hashCode());
		temp = Double.doubleToLongBits(margin);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(positionProfit);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(preBalance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((rtAccountID == null) ? 0 : rtAccountID.hashCode());
		temp = Double.doubleToLongBits(withdraw);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (accountID == null) {
			if (other.accountID != null)
				return false;
		} else if (!accountID.equals(other.accountID))
			return false;
		if (Double.doubleToLongBits(available) != Double.doubleToLongBits(other.available))
			return false;
		if (Double.doubleToLongBits(balance) != Double.doubleToLongBits(other.balance))
			return false;
		if (Double.doubleToLongBits(closeProfit) != Double.doubleToLongBits(other.closeProfit))
			return false;
		if (Double.doubleToLongBits(commission) != Double.doubleToLongBits(other.commission))
			return false;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		if (Double.doubleToLongBits(deposit) != Double.doubleToLongBits(other.deposit))
			return false;
		if (gatewayDisplayName == null) {
			if (other.gatewayDisplayName != null)
				return false;
		} else if (!gatewayDisplayName.equals(other.gatewayDisplayName))
			return false;
		if (gatewayID == null) {
			if (other.gatewayID != null)
				return false;
		} else if (!gatewayID.equals(other.gatewayID))
			return false;
		if (Double.doubleToLongBits(margin) != Double.doubleToLongBits(other.margin))
			return false;
		if (Double.doubleToLongBits(positionProfit) != Double.doubleToLongBits(other.positionProfit))
			return false;
		if (Double.doubleToLongBits(preBalance) != Double.doubleToLongBits(other.preBalance))
			return false;
		if (rtAccountID == null) {
			if (other.rtAccountID != null)
				return false;
		} else if (!rtAccountID.equals(other.rtAccountID))
			return false;
		if (Double.doubleToLongBits(withdraw) != Double.doubleToLongBits(other.withdraw))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Account [gatewayID=" + gatewayID + ", gatewayDisplayName=" + gatewayDisplayName + ", accountID="
				+ accountID + ", rtAccountID=" + rtAccountID + ", currency=" + currency + ", preBalance=" + preBalance
				+ ", balance=" + balance + ", available=" + available + ", commission=" + commission + ", margin="
				+ margin + ", closeProfit=" + closeProfit + ", positionProfit=" + positionProfit + ", deposit="
				+ deposit + ", withdraw=" + withdraw + "]";
	}

}
