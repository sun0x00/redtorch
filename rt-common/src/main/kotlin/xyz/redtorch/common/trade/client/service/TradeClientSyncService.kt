package xyz.redtorch.common.trade.client.service

import xyz.redtorch.common.sync.dto.CancelOrder
import xyz.redtorch.common.sync.dto.InsertOrder
import xyz.redtorch.common.trade.dto.Contract

interface TradeClientSyncService {
    fun setCallBack(callBack: TradeClientSyncServiceCallBack)

    fun auth(userId: String, authToken: String): Boolean

    fun subscribeContract(contract: Contract)
    fun unsubscribeContract(contract: Contract)

    fun subscribeContractList(contractList: List<Contract>)
    fun unsubscribeContractList(contractList: List<Contract>)

    fun cancelOrder(cancelOrder: CancelOrder)
    fun submitOrder(insertOrder: InsertOrder)

    fun getDelay(): Long

    fun isConnected(): Boolean
    fun isAutoReconnect(): Boolean
}