/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcRspRepealField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcRspRepealField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcRspRepealField obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jctpv6v6v9x64apiJNI.delete_CThostFtdcRspRepealField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setRepealTimeInterval(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_RepealTimeInterval_set(swigCPtr, this, value);
  }

  public int getRepealTimeInterval() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_RepealTimeInterval_get(swigCPtr, this);
  }

  public void setRepealedTimes(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_RepealedTimes_set(swigCPtr, this, value);
  }

  public int getRepealedTimes() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_RepealedTimes_get(swigCPtr, this);
  }

  public void setBankRepealFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankRepealFlag_set(swigCPtr, this, value);
  }

  public char getBankRepealFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankRepealFlag_get(swigCPtr, this);
  }

  public void setBrokerRepealFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerRepealFlag_set(swigCPtr, this, value);
  }

  public char getBrokerRepealFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerRepealFlag_get(swigCPtr, this);
  }

  public void setPlateRepealSerial(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_PlateRepealSerial_set(swigCPtr, this, value);
  }

  public int getPlateRepealSerial() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_PlateRepealSerial_get(swigCPtr, this);
  }

  public void setBankRepealSerial(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankRepealSerial_set(swigCPtr, this, value);
  }

  public String getBankRepealSerial() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankRepealSerial_get(swigCPtr, this);
  }

  public void setFutureRepealSerial(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FutureRepealSerial_set(swigCPtr, this, value);
  }

  public int getFutureRepealSerial() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FutureRepealSerial_get(swigCPtr, this);
  }

  public void setTradeCode(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeCode_set(swigCPtr, this, value);
  }

  public String getTradeCode() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeCode_get(swigCPtr, this);
  }

  public void setBankID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankID_set(swigCPtr, this, value);
  }

  public String getBankID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankID_get(swigCPtr, this);
  }

  public void setBankBranchID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankBranchID_set(swigCPtr, this, value);
  }

  public String getBankBranchID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankBranchID_get(swigCPtr, this);
  }

  public void setBrokerID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerID_get(swigCPtr, this);
  }

  public void setBrokerBranchID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerBranchID_set(swigCPtr, this, value);
  }

  public String getBrokerBranchID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerBranchID_get(swigCPtr, this);
  }

  public void setTradeDate(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeDate_set(swigCPtr, this, value);
  }

  public String getTradeDate() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeDate_get(swigCPtr, this);
  }

  public void setTradeTime(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeTime_set(swigCPtr, this, value);
  }

  public String getTradeTime() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeTime_get(swigCPtr, this);
  }

  public void setBankSerial(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankSerial_set(swigCPtr, this, value);
  }

  public String getBankSerial() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankSerial_get(swigCPtr, this);
  }

  public void setTradingDay(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradingDay_set(swigCPtr, this, value);
  }

  public String getTradingDay() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradingDay_get(swigCPtr, this);
  }

  public void setPlateSerial(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_PlateSerial_set(swigCPtr, this, value);
  }

  public int getPlateSerial() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_PlateSerial_get(swigCPtr, this);
  }

  public void setLastFragment(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_LastFragment_set(swigCPtr, this, value);
  }

  public char getLastFragment() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_LastFragment_get(swigCPtr, this);
  }

  public void setSessionID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_SessionID_set(swigCPtr, this, value);
  }

  public int getSessionID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_SessionID_get(swigCPtr, this);
  }

  public void setCustomerName(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CustomerName_set(swigCPtr, this, value);
  }

  public String getCustomerName() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CustomerName_get(swigCPtr, this);
  }

  public void setIdCardType(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_IdCardType_set(swigCPtr, this, value);
  }

  public char getIdCardType() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_IdCardType_get(swigCPtr, this);
  }

  public void setIdentifiedCardNo(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_IdentifiedCardNo_set(swigCPtr, this, value);
  }

  public String getIdentifiedCardNo() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_IdentifiedCardNo_get(swigCPtr, this);
  }

  public void setCustType(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CustType_set(swigCPtr, this, value);
  }

  public char getCustType() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CustType_get(swigCPtr, this);
  }

  public void setBankAccount(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankAccount_set(swigCPtr, this, value);
  }

  public String getBankAccount() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankAccount_get(swigCPtr, this);
  }

  public void setBankPassWord(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankPassWord_set(swigCPtr, this, value);
  }

  public String getBankPassWord() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankPassWord_get(swigCPtr, this);
  }

  public void setAccountID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_AccountID_set(swigCPtr, this, value);
  }

  public String getAccountID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_AccountID_get(swigCPtr, this);
  }

  public void setPassword(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_Password_set(swigCPtr, this, value);
  }

  public String getPassword() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_Password_get(swigCPtr, this);
  }

  public void setInstallID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_InstallID_set(swigCPtr, this, value);
  }

  public int getInstallID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_InstallID_get(swigCPtr, this);
  }

  public void setFutureSerial(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FutureSerial_set(swigCPtr, this, value);
  }

  public int getFutureSerial() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FutureSerial_get(swigCPtr, this);
  }

  public void setUserID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_UserID_set(swigCPtr, this, value);
  }

  public String getUserID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_UserID_get(swigCPtr, this);
  }

  public void setVerifyCertNoFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_VerifyCertNoFlag_set(swigCPtr, this, value);
  }

  public char getVerifyCertNoFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_VerifyCertNoFlag_get(swigCPtr, this);
  }

  public void setCurrencyID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CurrencyID_set(swigCPtr, this, value);
  }

  public String getCurrencyID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CurrencyID_get(swigCPtr, this);
  }

  public void setTradeAmount(double value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeAmount_set(swigCPtr, this, value);
  }

  public double getTradeAmount() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TradeAmount_get(swigCPtr, this);
  }

  public void setFutureFetchAmount(double value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FutureFetchAmount_set(swigCPtr, this, value);
  }

  public double getFutureFetchAmount() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FutureFetchAmount_get(swigCPtr, this);
  }

  public void setFeePayFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FeePayFlag_set(swigCPtr, this, value);
  }

  public char getFeePayFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_FeePayFlag_get(swigCPtr, this);
  }

  public void setCustFee(double value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CustFee_set(swigCPtr, this, value);
  }

  public double getCustFee() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_CustFee_get(swigCPtr, this);
  }

  public void setBrokerFee(double value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerFee_set(swigCPtr, this, value);
  }

  public double getBrokerFee() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerFee_get(swigCPtr, this);
  }

  public void setMessage(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_Message_set(swigCPtr, this, value);
  }

  public String getMessage() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_Message_get(swigCPtr, this);
  }

  public void setDigest(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_Digest_set(swigCPtr, this, value);
  }

  public String getDigest() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_Digest_get(swigCPtr, this);
  }

  public void setBankAccType(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankAccType_set(swigCPtr, this, value);
  }

  public char getBankAccType() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankAccType_get(swigCPtr, this);
  }

  public void setDeviceID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_DeviceID_set(swigCPtr, this, value);
  }

  public String getDeviceID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_DeviceID_get(swigCPtr, this);
  }

  public void setBankSecuAccType(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankSecuAccType_set(swigCPtr, this, value);
  }

  public char getBankSecuAccType() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankSecuAccType_get(swigCPtr, this);
  }

  public void setBrokerIDByBank(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerIDByBank_set(swigCPtr, this, value);
  }

  public String getBrokerIDByBank() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BrokerIDByBank_get(swigCPtr, this);
  }

  public void setBankSecuAcc(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankSecuAcc_set(swigCPtr, this, value);
  }

  public String getBankSecuAcc() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankSecuAcc_get(swigCPtr, this);
  }

  public void setBankPwdFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankPwdFlag_set(swigCPtr, this, value);
  }

  public char getBankPwdFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_BankPwdFlag_get(swigCPtr, this);
  }

  public void setSecuPwdFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_SecuPwdFlag_set(swigCPtr, this, value);
  }

  public char getSecuPwdFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_SecuPwdFlag_get(swigCPtr, this);
  }

  public void setOperNo(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_OperNo_set(swigCPtr, this, value);
  }

  public String getOperNo() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_OperNo_get(swigCPtr, this);
  }

  public void setRequestID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_RequestID_set(swigCPtr, this, value);
  }

  public int getRequestID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_RequestID_get(swigCPtr, this);
  }

  public void setTID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TID_set(swigCPtr, this, value);
  }

  public int getTID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TID_get(swigCPtr, this);
  }

  public void setTransferStatus(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TransferStatus_set(swigCPtr, this, value);
  }

  public char getTransferStatus() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_TransferStatus_get(swigCPtr, this);
  }

  public void setErrorID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_ErrorID_set(swigCPtr, this, value);
  }

  public int getErrorID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_ErrorID_get(swigCPtr, this);
  }

  public void setErrorMsg(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_ErrorMsg_set(swigCPtr, this, value);
  }

  public String getErrorMsg() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_ErrorMsg_get(swigCPtr, this);
  }

  public void setLongCustomerName(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_LongCustomerName_set(swigCPtr, this, value);
  }

  public String getLongCustomerName() {
    return jctpv6v6v9x64apiJNI.CThostFtdcRspRepealField_LongCustomerName_get(swigCPtr, this);
  }

  public CThostFtdcRspRepealField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcRspRepealField(), true);
  }

}
