/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcUserRightField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcUserRightField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcUserRightField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcUserRightField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_BrokerID_get(swigCPtr, this);
  }

  public void setUserID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_UserID_set(swigCPtr, this, value);
  }

  public String getUserID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_UserID_get(swigCPtr, this);
  }

  public void setUserRightType(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_UserRightType_set(swigCPtr, this, value);
  }

  public char getUserRightType() {
    return jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_UserRightType_get(swigCPtr, this);
  }

  public void setIsForbidden(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_IsForbidden_set(swigCPtr, this, value);
  }

  public int getIsForbidden() {
    return jctpv6v6v9x64apiJNI.CThostFtdcUserRightField_IsForbidden_get(swigCPtr, this);
  }

  public CThostFtdcUserRightField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcUserRightField(), true);
  }

}
