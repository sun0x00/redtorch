/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcSPBMInvestorPortfDefField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcSPBMInvestorPortfDefField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcSPBMInvestorPortfDefField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcSPBMInvestorPortfDefField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setExchangeID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_ExchangeID_set(swigCPtr, this, value);
  }

  public String getExchangeID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_ExchangeID_get(swigCPtr, this);
  }

  public void setBrokerID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_BrokerID_get(swigCPtr, this);
  }

  public void setInvestorID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_InvestorID_set(swigCPtr, this, value);
  }

  public String getInvestorID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_InvestorID_get(swigCPtr, this);
  }

  public void setPortfolioDefID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_PortfolioDefID_set(swigCPtr, this, value);
  }

  public int getPortfolioDefID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSPBMInvestorPortfDefField_PortfolioDefID_get(swigCPtr, this);
  }

  public CThostFtdcSPBMInvestorPortfDefField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcSPBMInvestorPortfDefField(), true);
  }

}
