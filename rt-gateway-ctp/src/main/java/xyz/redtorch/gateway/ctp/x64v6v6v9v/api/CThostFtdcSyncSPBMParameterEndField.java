/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcSyncSPBMParameterEndField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcSyncSPBMParameterEndField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcSyncSPBMParameterEndField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcSyncSPBMParameterEndField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setTradingDay(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncSPBMParameterEndField_TradingDay_set(swigCPtr, this, value);
  }

  public String getTradingDay() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncSPBMParameterEndField_TradingDay_get(swigCPtr, this);
  }

  public CThostFtdcSyncSPBMParameterEndField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcSyncSPBMParameterEndField(), true);
  }

}