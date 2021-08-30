package xyz.redtorch.master.dao.impl

import org.springframework.beans.factory.annotation.Autowired
import xyz.redtorch.common.storage.po.BasePo
import xyz.redtorch.common.utils.JsonUtils
import xyz.redtorch.master.dao.BaseDao
import xyz.redtorch.master.db.ZookeeperService
import java.lang.reflect.ParameterizedType

abstract class BaseDaoImpl<T : BasePo>(private val collection: String) : BaseDao<T> {
    private val entityClass: Class<T> = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>

    @Autowired
    lateinit var zookeeperService: ZookeeperService

    override fun queryList(): List<T> {
        val dataList = ArrayList<T>()
        val jsonNodeList = zookeeperService.find(collection)
        if (jsonNodeList != null) {
            for (jsonNode in jsonNodeList) {
                val data = JsonUtils.convertToObject(jsonNode, entityClass)
                dataList.add(data)
            }
        }
        return dataList

    }

    override fun queryById(id: String): T? {
        val jsonNode = zookeeperService.findById(collection, id)
        if (jsonNode != null) {
            return JsonUtils.convertToObject(jsonNode, entityClass)
        }
        return null
    }

    override fun deleteById(id: String) {
        zookeeperService.deleteById(collection, id)
    }

    override fun upsert(obj: T) {
        obj.id?.let { zookeeperService.upsert(collection, it, JsonUtils.writeToJsonString(obj)) }
    }
}