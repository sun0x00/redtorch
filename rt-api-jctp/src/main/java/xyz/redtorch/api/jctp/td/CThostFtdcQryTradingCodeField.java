/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.api.jctp.td;

public class CThostFtdcQryTradingCodeField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcQryTradingCodeField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcQryTradingCodeField obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jctptraderapiv6v3v11x64JNI.delete_CThostFtdcQryTradingCodeField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_BrokerID_get(swigCPtr, this);
  }

  public void setInvestorID(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_InvestorID_set(swigCPtr, this, value);
  }

  public String getInvestorID() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_InvestorID_get(swigCPtr, this);
  }

  public void setExchangeID(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_ExchangeID_set(swigCPtr, this, value);
  }

  public String getExchangeID() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_ExchangeID_get(swigCPtr, this);
  }

  public void setClientID(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_ClientID_set(swigCPtr, this, value);
  }

  public String getClientID() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_ClientID_get(swigCPtr, this);
  }

  public void setClientIDType(char value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_ClientIDType_set(swigCPtr, this, value);
  }

  public char getClientIDType() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_ClientIDType_get(swigCPtr, this);
  }

  public void setInvestUnitID(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_InvestUnitID_set(swigCPtr, this, value);
  }

  public String getInvestUnitID() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcQryTradingCodeField_InvestUnitID_get(swigCPtr, this);
  }

  public CThostFtdcQryTradingCodeField() {
    this(jctptraderapiv6v3v11x64JNI.new_CThostFtdcQryTradingCodeField(), true);
  }

}
