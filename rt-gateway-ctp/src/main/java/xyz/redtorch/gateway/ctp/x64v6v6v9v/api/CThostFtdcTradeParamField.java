/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcTradeParamField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcTradeParamField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcTradeParamField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcTradeParamField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_BrokerID_get(swigCPtr, this);
  }

  public void setTradeParamID(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_TradeParamID_set(swigCPtr, this, value);
  }

  public char getTradeParamID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_TradeParamID_get(swigCPtr, this);
  }

  public void setTradeParamValue(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_TradeParamValue_set(swigCPtr, this, value);
  }

  public String getTradeParamValue() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_TradeParamValue_get(swigCPtr, this);
  }

  public void setMemo(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_Memo_set(swigCPtr, this, value);
  }

  public String getMemo() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradeParamField_Memo_get(swigCPtr, this);
  }

  public CThostFtdcTradeParamField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcTradeParamField(), true);
  }

}
