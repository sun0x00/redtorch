/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9cpv.api;

public class CThostFtdcParkedOrderField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcParkedOrderField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcParkedOrderField obj) {
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
        jctpv6v6v9cpx64apiJNI.delete_CThostFtdcParkedOrderField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_BrokerID_get(swigCPtr, this);
  }

  public void setInvestorID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_InvestorID_set(swigCPtr, this, value);
  }

  public String getInvestorID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_InvestorID_get(swigCPtr, this);
  }

  public void setReserve1(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_reserve1_set(swigCPtr, this, value);
  }

  public String getReserve1() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_reserve1_get(swigCPtr, this);
  }

  public void setOrderRef(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_OrderRef_set(swigCPtr, this, value);
  }

  public String getOrderRef() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_OrderRef_get(swigCPtr, this);
  }

  public void setUserID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_UserID_set(swigCPtr, this, value);
  }

  public String getUserID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_UserID_get(swigCPtr, this);
  }

  public void setOrderPriceType(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_OrderPriceType_set(swigCPtr, this, value);
  }

  public char getOrderPriceType() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_OrderPriceType_get(swigCPtr, this);
  }

  public void setDirection(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_Direction_set(swigCPtr, this, value);
  }

  public char getDirection() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_Direction_get(swigCPtr, this);
  }

  public void setCombOffsetFlag(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_CombOffsetFlag_set(swigCPtr, this, value);
  }

  public String getCombOffsetFlag() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_CombOffsetFlag_get(swigCPtr, this);
  }

  public void setCombHedgeFlag(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_CombHedgeFlag_set(swigCPtr, this, value);
  }

  public String getCombHedgeFlag() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_CombHedgeFlag_get(swigCPtr, this);
  }

  public void setLimitPrice(double value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_LimitPrice_set(swigCPtr, this, value);
  }

  public double getLimitPrice() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_LimitPrice_get(swigCPtr, this);
  }

  public void setVolumeTotalOriginal(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_VolumeTotalOriginal_set(swigCPtr, this, value);
  }

  public int getVolumeTotalOriginal() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_VolumeTotalOriginal_get(swigCPtr, this);
  }

  public void setTimeCondition(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_TimeCondition_set(swigCPtr, this, value);
  }

  public char getTimeCondition() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_TimeCondition_get(swigCPtr, this);
  }

  public void setGTDDate(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_GTDDate_set(swigCPtr, this, value);
  }

  public String getGTDDate() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_GTDDate_get(swigCPtr, this);
  }

  public void setVolumeCondition(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_VolumeCondition_set(swigCPtr, this, value);
  }

  public char getVolumeCondition() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_VolumeCondition_get(swigCPtr, this);
  }

  public void setMinVolume(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_MinVolume_set(swigCPtr, this, value);
  }

  public int getMinVolume() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_MinVolume_get(swigCPtr, this);
  }

  public void setContingentCondition(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ContingentCondition_set(swigCPtr, this, value);
  }

  public char getContingentCondition() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ContingentCondition_get(swigCPtr, this);
  }

  public void setStopPrice(double value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_StopPrice_set(swigCPtr, this, value);
  }

  public double getStopPrice() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_StopPrice_get(swigCPtr, this);
  }

  public void setForceCloseReason(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ForceCloseReason_set(swigCPtr, this, value);
  }

  public char getForceCloseReason() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ForceCloseReason_get(swigCPtr, this);
  }

  public void setIsAutoSuspend(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_IsAutoSuspend_set(swigCPtr, this, value);
  }

  public int getIsAutoSuspend() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_IsAutoSuspend_get(swigCPtr, this);
  }

  public void setBusinessUnit(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_BusinessUnit_set(swigCPtr, this, value);
  }

  public String getBusinessUnit() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_BusinessUnit_get(swigCPtr, this);
  }

  public void setRequestID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_RequestID_set(swigCPtr, this, value);
  }

  public int getRequestID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_RequestID_get(swigCPtr, this);
  }

  public void setUserForceClose(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_UserForceClose_set(swigCPtr, this, value);
  }

  public int getUserForceClose() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_UserForceClose_get(swigCPtr, this);
  }

  public void setExchangeID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ExchangeID_set(swigCPtr, this, value);
  }

  public String getExchangeID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ExchangeID_get(swigCPtr, this);
  }

  public void setParkedOrderID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ParkedOrderID_set(swigCPtr, this, value);
  }

  public String getParkedOrderID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ParkedOrderID_get(swigCPtr, this);
  }

  public void setUserType(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_UserType_set(swigCPtr, this, value);
  }

  public char getUserType() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_UserType_get(swigCPtr, this);
  }

  public void setStatus(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_Status_set(swigCPtr, this, value);
  }

  public char getStatus() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_Status_get(swigCPtr, this);
  }

  public void setErrorID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ErrorID_set(swigCPtr, this, value);
  }

  public int getErrorID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ErrorID_get(swigCPtr, this);
  }

  public void setErrorMsg(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ErrorMsg_set(swigCPtr, this, value);
  }

  public String getErrorMsg() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ErrorMsg_get(swigCPtr, this);
  }

  public void setIsSwapOrder(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_IsSwapOrder_set(swigCPtr, this, value);
  }

  public int getIsSwapOrder() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_IsSwapOrder_get(swigCPtr, this);
  }

  public void setAccountID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_AccountID_set(swigCPtr, this, value);
  }

  public String getAccountID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_AccountID_get(swigCPtr, this);
  }

  public void setCurrencyID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_CurrencyID_set(swigCPtr, this, value);
  }

  public String getCurrencyID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_CurrencyID_get(swigCPtr, this);
  }

  public void setClientID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ClientID_set(swigCPtr, this, value);
  }

  public String getClientID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_ClientID_get(swigCPtr, this);
  }

  public void setInvestUnitID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_InvestUnitID_set(swigCPtr, this, value);
  }

  public String getInvestUnitID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_InvestUnitID_get(swigCPtr, this);
  }

  public void setReserve2(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_reserve2_set(swigCPtr, this, value);
  }

  public String getReserve2() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_reserve2_get(swigCPtr, this);
  }

  public void setMacAddress(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_MacAddress_set(swigCPtr, this, value);
  }

  public String getMacAddress() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_MacAddress_get(swigCPtr, this);
  }

  public void setInstrumentID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_InstrumentID_set(swigCPtr, this, value);
  }

  public String getInstrumentID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_InstrumentID_get(swigCPtr, this);
  }

  public void setIPAddress(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_IPAddress_set(swigCPtr, this, value);
  }

  public String getIPAddress() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcParkedOrderField_IPAddress_get(swigCPtr, this);
  }

  public CThostFtdcParkedOrderField() {
    this(jctpv6v6v9cpx64apiJNI.new_CThostFtdcParkedOrderField(), true);
  }

}
