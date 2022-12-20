/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package xyz.redtorch.gateway.ctp.x64v6v6v9cpv.api;

public class CThostFtdcErrorConditionalOrderField {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CThostFtdcErrorConditionalOrderField(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CThostFtdcErrorConditionalOrderField obj) {
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
        jctpv6v6v9cpx64apiJNI.delete_CThostFtdcErrorConditionalOrderField(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setBrokerID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BrokerID_set(swigCPtr, this, value);
  }

  public String getBrokerID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BrokerID_get(swigCPtr, this);
  }

  public void setInvestorID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InvestorID_set(swigCPtr, this, value);
  }

  public String getInvestorID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InvestorID_get(swigCPtr, this);
  }

  public void setReserve1(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_reserve1_set(swigCPtr, this, value);
  }

  public String getReserve1() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_reserve1_get(swigCPtr, this);
  }

  public void setOrderRef(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderRef_set(swigCPtr, this, value);
  }

  public String getOrderRef() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderRef_get(swigCPtr, this);
  }

  public void setUserID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UserID_set(swigCPtr, this, value);
  }

  public String getUserID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UserID_get(swigCPtr, this);
  }

  public void setOrderPriceType(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderPriceType_set(swigCPtr, this, value);
  }

  public char getOrderPriceType() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderPriceType_get(swigCPtr, this);
  }

  public void setDirection(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_Direction_set(swigCPtr, this, value);
  }

  public char getDirection() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_Direction_get(swigCPtr, this);
  }

  public void setCombOffsetFlag(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CombOffsetFlag_set(swigCPtr, this, value);
  }

  public String getCombOffsetFlag() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CombOffsetFlag_get(swigCPtr, this);
  }

  public void setCombHedgeFlag(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CombHedgeFlag_set(swigCPtr, this, value);
  }

  public String getCombHedgeFlag() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CombHedgeFlag_get(swigCPtr, this);
  }

  public void setLimitPrice(double value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_LimitPrice_set(swigCPtr, this, value);
  }

  public double getLimitPrice() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_LimitPrice_get(swigCPtr, this);
  }

  public void setVolumeTotalOriginal(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeTotalOriginal_set(swigCPtr, this, value);
  }

  public int getVolumeTotalOriginal() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeTotalOriginal_get(swigCPtr, this);
  }

  public void setTimeCondition(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_TimeCondition_set(swigCPtr, this, value);
  }

  public char getTimeCondition() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_TimeCondition_get(swigCPtr, this);
  }

  public void setGTDDate(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_GTDDate_set(swigCPtr, this, value);
  }

  public String getGTDDate() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_GTDDate_get(swigCPtr, this);
  }

  public void setVolumeCondition(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeCondition_set(swigCPtr, this, value);
  }

  public char getVolumeCondition() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeCondition_get(swigCPtr, this);
  }

  public void setMinVolume(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_MinVolume_set(swigCPtr, this, value);
  }

  public int getMinVolume() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_MinVolume_get(swigCPtr, this);
  }

  public void setContingentCondition(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ContingentCondition_set(swigCPtr, this, value);
  }

  public char getContingentCondition() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ContingentCondition_get(swigCPtr, this);
  }

  public void setStopPrice(double value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_StopPrice_set(swigCPtr, this, value);
  }

  public double getStopPrice() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_StopPrice_get(swigCPtr, this);
  }

  public void setForceCloseReason(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ForceCloseReason_set(swigCPtr, this, value);
  }

  public char getForceCloseReason() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ForceCloseReason_get(swigCPtr, this);
  }

  public void setIsAutoSuspend(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_IsAutoSuspend_set(swigCPtr, this, value);
  }

  public int getIsAutoSuspend() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_IsAutoSuspend_get(swigCPtr, this);
  }

  public void setBusinessUnit(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BusinessUnit_set(swigCPtr, this, value);
  }

  public String getBusinessUnit() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BusinessUnit_get(swigCPtr, this);
  }

  public void setRequestID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_RequestID_set(swigCPtr, this, value);
  }

  public int getRequestID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_RequestID_get(swigCPtr, this);
  }

  public void setOrderLocalID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderLocalID_set(swigCPtr, this, value);
  }

  public String getOrderLocalID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderLocalID_get(swigCPtr, this);
  }

  public void setExchangeID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ExchangeID_set(swigCPtr, this, value);
  }

  public String getExchangeID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ExchangeID_get(swigCPtr, this);
  }

  public void setParticipantID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ParticipantID_set(swigCPtr, this, value);
  }

  public String getParticipantID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ParticipantID_get(swigCPtr, this);
  }

  public void setClientID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ClientID_set(swigCPtr, this, value);
  }

  public String getClientID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ClientID_get(swigCPtr, this);
  }

  public void setReserve2(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_reserve2_set(swigCPtr, this, value);
  }

  public String getReserve2() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_reserve2_get(swigCPtr, this);
  }

  public void setTraderID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_TraderID_set(swigCPtr, this, value);
  }

  public String getTraderID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_TraderID_get(swigCPtr, this);
  }

  public void setInstallID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InstallID_set(swigCPtr, this, value);
  }

  public int getInstallID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InstallID_get(swigCPtr, this);
  }

  public void setOrderSubmitStatus(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderSubmitStatus_set(swigCPtr, this, value);
  }

  public char getOrderSubmitStatus() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderSubmitStatus_get(swigCPtr, this);
  }

  public void setNotifySequence(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_NotifySequence_set(swigCPtr, this, value);
  }

  public int getNotifySequence() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_NotifySequence_get(swigCPtr, this);
  }

  public void setTradingDay(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_TradingDay_set(swigCPtr, this, value);
  }

  public String getTradingDay() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_TradingDay_get(swigCPtr, this);
  }

  public void setSettlementID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SettlementID_set(swigCPtr, this, value);
  }

  public int getSettlementID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SettlementID_get(swigCPtr, this);
  }

  public void setOrderSysID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderSysID_set(swigCPtr, this, value);
  }

  public String getOrderSysID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderSysID_get(swigCPtr, this);
  }

  public void setOrderSource(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderSource_set(swigCPtr, this, value);
  }

  public char getOrderSource() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderSource_get(swigCPtr, this);
  }

  public void setOrderStatus(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderStatus_set(swigCPtr, this, value);
  }

  public char getOrderStatus() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderStatus_get(swigCPtr, this);
  }

  public void setOrderType(char value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderType_set(swigCPtr, this, value);
  }

  public char getOrderType() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_OrderType_get(swigCPtr, this);
  }

  public void setVolumeTraded(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeTraded_set(swigCPtr, this, value);
  }

  public int getVolumeTraded() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeTraded_get(swigCPtr, this);
  }

  public void setVolumeTotal(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeTotal_set(swigCPtr, this, value);
  }

  public int getVolumeTotal() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_VolumeTotal_get(swigCPtr, this);
  }

  public void setInsertDate(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InsertDate_set(swigCPtr, this, value);
  }

  public String getInsertDate() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InsertDate_get(swigCPtr, this);
  }

  public void setInsertTime(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InsertTime_set(swigCPtr, this, value);
  }

  public String getInsertTime() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InsertTime_get(swigCPtr, this);
  }

  public void setActiveTime(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ActiveTime_set(swigCPtr, this, value);
  }

  public String getActiveTime() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ActiveTime_get(swigCPtr, this);
  }

  public void setSuspendTime(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SuspendTime_set(swigCPtr, this, value);
  }

  public String getSuspendTime() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SuspendTime_get(swigCPtr, this);
  }

  public void setUpdateTime(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UpdateTime_set(swigCPtr, this, value);
  }

  public String getUpdateTime() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UpdateTime_get(swigCPtr, this);
  }

  public void setCancelTime(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CancelTime_set(swigCPtr, this, value);
  }

  public String getCancelTime() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CancelTime_get(swigCPtr, this);
  }

  public void setActiveTraderID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ActiveTraderID_set(swigCPtr, this, value);
  }

  public String getActiveTraderID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ActiveTraderID_get(swigCPtr, this);
  }

  public void setClearingPartID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ClearingPartID_set(swigCPtr, this, value);
  }

  public String getClearingPartID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ClearingPartID_get(swigCPtr, this);
  }

  public void setSequenceNo(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SequenceNo_set(swigCPtr, this, value);
  }

  public int getSequenceNo() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SequenceNo_get(swigCPtr, this);
  }

  public void setFrontID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_FrontID_set(swigCPtr, this, value);
  }

  public int getFrontID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_FrontID_get(swigCPtr, this);
  }

  public void setSessionID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SessionID_set(swigCPtr, this, value);
  }

  public int getSessionID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_SessionID_get(swigCPtr, this);
  }

  public void setUserProductInfo(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UserProductInfo_set(swigCPtr, this, value);
  }

  public String getUserProductInfo() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UserProductInfo_get(swigCPtr, this);
  }

  public void setStatusMsg(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_StatusMsg_set(swigCPtr, this, value);
  }

  public String getStatusMsg() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_StatusMsg_get(swigCPtr, this);
  }

  public void setUserForceClose(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UserForceClose_set(swigCPtr, this, value);
  }

  public int getUserForceClose() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_UserForceClose_get(swigCPtr, this);
  }

  public void setActiveUserID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ActiveUserID_set(swigCPtr, this, value);
  }

  public String getActiveUserID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ActiveUserID_get(swigCPtr, this);
  }

  public void setBrokerOrderSeq(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BrokerOrderSeq_set(swigCPtr, this, value);
  }

  public int getBrokerOrderSeq() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BrokerOrderSeq_get(swigCPtr, this);
  }

  public void setRelativeOrderSysID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_RelativeOrderSysID_set(swigCPtr, this, value);
  }

  public String getRelativeOrderSysID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_RelativeOrderSysID_get(swigCPtr, this);
  }

  public void setZCETotalTradedVolume(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ZCETotalTradedVolume_set(swigCPtr, this, value);
  }

  public int getZCETotalTradedVolume() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ZCETotalTradedVolume_get(swigCPtr, this);
  }

  public void setErrorID(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ErrorID_set(swigCPtr, this, value);
  }

  public int getErrorID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ErrorID_get(swigCPtr, this);
  }

  public void setErrorMsg(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ErrorMsg_set(swigCPtr, this, value);
  }

  public String getErrorMsg() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ErrorMsg_get(swigCPtr, this);
  }

  public void setIsSwapOrder(int value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_IsSwapOrder_set(swigCPtr, this, value);
  }

  public int getIsSwapOrder() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_IsSwapOrder_get(swigCPtr, this);
  }

  public void setBranchID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BranchID_set(swigCPtr, this, value);
  }

  public String getBranchID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_BranchID_get(swigCPtr, this);
  }

  public void setInvestUnitID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InvestUnitID_set(swigCPtr, this, value);
  }

  public String getInvestUnitID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InvestUnitID_get(swigCPtr, this);
  }

  public void setAccountID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_AccountID_set(swigCPtr, this, value);
  }

  public String getAccountID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_AccountID_get(swigCPtr, this);
  }

  public void setCurrencyID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CurrencyID_set(swigCPtr, this, value);
  }

  public String getCurrencyID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_CurrencyID_get(swigCPtr, this);
  }

  public void setReserve3(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_reserve3_set(swigCPtr, this, value);
  }

  public String getReserve3() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_reserve3_get(swigCPtr, this);
  }

  public void setMacAddress(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_MacAddress_set(swigCPtr, this, value);
  }

  public String getMacAddress() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_MacAddress_get(swigCPtr, this);
  }

  public void setInstrumentID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InstrumentID_set(swigCPtr, this, value);
  }

  public String getInstrumentID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_InstrumentID_get(swigCPtr, this);
  }

  public void setExchangeInstID(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ExchangeInstID_set(swigCPtr, this, value);
  }

  public String getExchangeInstID() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_ExchangeInstID_get(swigCPtr, this);
  }

  public void setIPAddress(String value) {
    jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_IPAddress_set(swigCPtr, this, value);
  }

  public String getIPAddress() {
    return jctpv6v6v9cpx64apiJNI.CThostFtdcErrorConditionalOrderField_IPAddress_get(swigCPtr, this);
  }

  public CThostFtdcErrorConditionalOrderField() {
    this(jctpv6v6v9cpx64apiJNI.new_CThostFtdcErrorConditionalOrderField(), true);
  }

}
