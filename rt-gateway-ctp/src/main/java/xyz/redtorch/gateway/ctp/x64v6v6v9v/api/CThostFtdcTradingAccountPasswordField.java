/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcTradingAccountPasswordField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcTradingAccountPasswordField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcTradingAccountPasswordField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcTradingAccountPasswordField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_BrokerID_get(swigCPtr, this);
  }

  public void setAccountID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_AccountID_set(swigCPtr, this, value);
  }

  public String getAccountID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_AccountID_get(swigCPtr, this);
  }

  public void setPassword(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_Password_set(swigCPtr, this, value);
  }

  public String getPassword() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_Password_get(swigCPtr, this);
  }

  public void setCurrencyID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_CurrencyID_set(swigCPtr, this, value);
  }

  public String getCurrencyID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordField_CurrencyID_get(swigCPtr, this);
  }

  public CThostFtdcTradingAccountPasswordField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcTradingAccountPasswordField(), true);
  }

}
