package xyz.redtorch.master.db

import com.fasterxml.jackson.databind.JsonNode
import xyz.redtorch.common.utils.ZookeeperUtils

interface ZookeeperService {
    fun getZkUtils(): ZookeeperUtils

    fun get3DesKey(): String

    fun find(collection: String, enableEncryption: Boolean = true): List<JsonNode>?

    fun findById(collection: String, id: String, enableEncryption: Boolean = true): JsonNode?

    fun update(collection: String, id: String, data: String, enableEncryption: Boolean = true): Boolean

    fun insert(collection: String, id: String, data: String, enableEncryption: Boolean = true): Boolean

    fun upsert(collection: String, id: String, data: String, enableEncryption: Boolean = true): Boolean

    fun deleteById(collection: String, id: String): Boolean
}