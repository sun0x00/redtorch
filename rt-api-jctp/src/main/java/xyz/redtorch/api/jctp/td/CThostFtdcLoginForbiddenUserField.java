/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.api.jctp.td;

public class CThostFtdcLoginForbiddenUserField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcLoginForbiddenUserField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcLoginForbiddenUserField obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        jctptraderapiv6v3v11x64JNI.delete_CThostFtdcLoginForbiddenUserField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcLoginForbiddenUserField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcLoginForbiddenUserField_BrokerID_get(swigCPtr, this);
  }

  public void setUserID(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcLoginForbiddenUserField_UserID_set(swigCPtr, this, value);
  }

  public String getUserID() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcLoginForbiddenUserField_UserID_get(swigCPtr, this);
  }

  public void setIPAddress(String value) {
    jctptraderapiv6v3v11x64JNI.CThostFtdcLoginForbiddenUserField_IPAddress_set(swigCPtr, this, value);
  }

  public String getIPAddress() {
    return jctptraderapiv6v3v11x64JNI.CThostFtdcLoginForbiddenUserField_IPAddress_get(swigCPtr, this);
  }

  public CThostFtdcLoginForbiddenUserField() {
    this(jctptraderapiv6v3v11x64JNI.new_CThostFtdcLoginForbiddenUserField(), true);
  }

}
