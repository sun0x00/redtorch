/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcTradingAccountPasswordUpdateV1Field {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcTradingAccountPasswordUpdateV1Field(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcTradingAccountPasswordUpdateV1Field obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcTradingAccountPasswordUpdateV1Field(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_BrokerID_get(swigCPtr, this);
  }

  public void setInvestorID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_InvestorID_set(swigCPtr, this, value);
  }

  public String getInvestorID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_InvestorID_get(swigCPtr, this);
  }

  public void setOldPassword(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_OldPassword_set(swigCPtr, this, value);
  }

  public String getOldPassword() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_OldPassword_get(swigCPtr, this);
  }

  public void setNewPassword(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_NewPassword_set(swigCPtr, this, value);
  }

  public String getNewPassword() {
    return jctpv6v6v9x64apiJNI.CThostFtdcTradingAccountPasswordUpdateV1Field_NewPassword_get(swigCPtr, this);
  }

  public CThostFtdcTradingAccountPasswordUpdateV1Field() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcTradingAccountPasswordUpdateV1Field(), true);
  }

}
