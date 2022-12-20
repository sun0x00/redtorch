/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcInputOptionSelfCloseActionField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcInputOptionSelfCloseActionField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcInputOptionSelfCloseActionField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcInputOptionSelfCloseActionField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_BrokerID_get(swigCPtr, this);
  }

  public void setInvestorID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_InvestorID_set(swigCPtr, this, value);
  }

  public String getInvestorID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_InvestorID_get(swigCPtr, this);
  }

  public void setOptionSelfCloseActionRef(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_OptionSelfCloseActionRef_set(swigCPtr, this, value);
  }

  public int getOptionSelfCloseActionRef() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_OptionSelfCloseActionRef_get(swigCPtr, this);
  }

  public void setOptionSelfCloseRef(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_OptionSelfCloseRef_set(swigCPtr, this, value);
  }

  public String getOptionSelfCloseRef() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_OptionSelfCloseRef_get(swigCPtr, this);
  }

  public void setRequestID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_RequestID_set(swigCPtr, this, value);
  }

  public int getRequestID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_RequestID_get(swigCPtr, this);
  }

  public void setFrontID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_FrontID_set(swigCPtr, this, value);
  }

  public int getFrontID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_FrontID_get(swigCPtr, this);
  }

  public void setSessionID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_SessionID_set(swigCPtr, this, value);
  }

  public int getSessionID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_SessionID_get(swigCPtr, this);
  }

  public void setExchangeID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_ExchangeID_set(swigCPtr, this, value);
  }

  public String getExchangeID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_ExchangeID_get(swigCPtr, this);
  }

  public void setOptionSelfCloseSysID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_OptionSelfCloseSysID_set(swigCPtr, this, value);
  }

  public String getOptionSelfCloseSysID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_OptionSelfCloseSysID_get(swigCPtr, this);
  }

  public void setActionFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_ActionFlag_set(swigCPtr, this, value);
  }

  public char getActionFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_ActionFlag_get(swigCPtr, this);
  }

  public void setUserID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_UserID_set(swigCPtr, this, value);
  }

  public String getUserID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_UserID_get(swigCPtr, this);
  }

  public void setReserve1(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_reserve1_set(swigCPtr, this, value);
  }

  public String getReserve1() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_reserve1_get(swigCPtr, this);
  }

  public void setInvestUnitID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_InvestUnitID_set(swigCPtr, this, value);
  }

  public String getInvestUnitID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_InvestUnitID_get(swigCPtr, this);
  }

  public void setReserve2(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_reserve2_set(swigCPtr, this, value);
  }

  public String getReserve2() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_reserve2_get(swigCPtr, this);
  }

  public void setMacAddress(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_MacAddress_set(swigCPtr, this, value);
  }

  public String getMacAddress() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_MacAddress_get(swigCPtr, this);
  }

  public void setInstrumentID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_InstrumentID_set(swigCPtr, this, value);
  }

  public String getInstrumentID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_InstrumentID_get(swigCPtr, this);
  }

  public void setIPAddress(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_IPAddress_set(swigCPtr, this, value);
  }

  public String getIPAddress() {
    return jctpv6v6v9x64apiJNI.CThostFtdcInputOptionSelfCloseActionField_IPAddress_get(swigCPtr, this);
  }

  public CThostFtdcInputOptionSelfCloseActionField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcInputOptionSelfCloseActionField(), true);
  }

}
