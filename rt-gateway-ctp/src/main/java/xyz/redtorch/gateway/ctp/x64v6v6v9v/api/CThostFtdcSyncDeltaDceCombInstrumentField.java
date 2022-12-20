/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9v.api;

public class CThostFtdcSyncDeltaDceCombInstrumentField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcSyncDeltaDceCombInstrumentField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcSyncDeltaDceCombInstrumentField obj) {
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
        jctpv6v6v9x64apiJNI.delete_CThostFtdcSyncDeltaDceCombInstrumentField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setCombInstrumentID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_CombInstrumentID_set(swigCPtr, this, value);
  }

  public String getCombInstrumentID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_CombInstrumentID_get(swigCPtr, this);
  }

  public void setExchangeID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ExchangeID_set(swigCPtr, this, value);
  }

  public String getExchangeID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ExchangeID_get(swigCPtr, this);
  }

  public void setExchangeInstID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ExchangeInstID_set(swigCPtr, this, value);
  }

  public String getExchangeInstID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ExchangeInstID_get(swigCPtr, this);
  }

  public void setTradeGroupID(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_TradeGroupID_set(swigCPtr, this, value);
  }

  public int getTradeGroupID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_TradeGroupID_get(swigCPtr, this);
  }

  public void setCombHedgeFlag(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_CombHedgeFlag_set(swigCPtr, this, value);
  }

  public char getCombHedgeFlag() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_CombHedgeFlag_get(swigCPtr, this);
  }

  public void setCombinationType(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_CombinationType_set(swigCPtr, this, value);
  }

  public char getCombinationType() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_CombinationType_get(swigCPtr, this);
  }

  public void setDirection(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_Direction_set(swigCPtr, this, value);
  }

  public char getDirection() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_Direction_get(swigCPtr, this);
  }

  public void setProductID(String value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ProductID_set(swigCPtr, this, value);
  }

  public String getProductID() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ProductID_get(swigCPtr, this);
  }

  public void setXparameter(double value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_Xparameter_set(swigCPtr, this, value);
  }

  public double getXparameter() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_Xparameter_get(swigCPtr, this);
  }

  public void setActionDirection(char value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ActionDirection_set(swigCPtr, this, value);
  }

  public char getActionDirection() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_ActionDirection_get(swigCPtr, this);
  }

  public void setSyncDeltaSequenceNo(int value) {
    jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_SyncDeltaSequenceNo_set(swigCPtr, this, value);
  }

  public int getSyncDeltaSequenceNo() {
    return jctpv6v6v9x64apiJNI.CThostFtdcSyncDeltaDceCombInstrumentField_SyncDeltaSequenceNo_get(swigCPtr, this);
  }

  public CThostFtdcSyncDeltaDceCombInstrumentField() {
    this(jctpv6v6v9x64apiJNI.new_CThostFtdcSyncDeltaDceCombInstrumentField(), true);
  }

}