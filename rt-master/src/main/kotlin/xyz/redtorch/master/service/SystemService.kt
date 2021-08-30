package xyz.redtorch.master.service

import xyz.redtorch.common.sync.dto.CancelOrder
import xyz.redtorch.common.sync.dto.InsertOrder

interface SystemService {
    fun syncSlaveNodeSettingMirror()
    fun cancelOrder(cancelOrder: CancelOrder)
    fun submitOrder(insertOrder: InsertOrder)
}