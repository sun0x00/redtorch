/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcQryRiskSettleProductStatusField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcQryRiskSettleProductStatusField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcQryRiskSettleProductStatusField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcQryRiskSettleProductStatusField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setProductID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcQryRiskSettleProductStatusField_ProductID_set(swigCPtr, this, value);
  }

  public String getProductID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcQryRiskSettleProductStatusField_ProductID_get(swigCPtr, this);
  }

  public CThostFtdcQryRiskSettleProductStatusField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcQryRiskSettleProductStatusField(), true);
  }

}
