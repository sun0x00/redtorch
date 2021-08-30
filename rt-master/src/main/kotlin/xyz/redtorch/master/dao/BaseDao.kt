package xyz.redtorch.master.dao

import xyz.redtorch.common.storage.po.BasePo

interface BaseDao<T : BasePo> {
    fun queryList(): List<T>

    fun queryById(id: String): T?

    fun deleteById(id: String)

    fun upsert(obj: T)
}