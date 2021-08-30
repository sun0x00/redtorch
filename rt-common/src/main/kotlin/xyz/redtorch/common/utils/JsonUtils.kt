package xyz.redtorch.common.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.DiffFlags
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import java.util.*


object JsonUtils {

    @JvmStatic
    val mapper = ObjectMapper()

    /**
     * JSON字符串转换为对象
     */
    @JvmStatic
    fun <T> readToObject(jsonString: String, type: Class<T>): T {
        return mapper.readValue(jsonString, type)
    }

    /**
     * JSON字符串转换为JsonNode
     */
    @JvmStatic
    fun readToJsonNode(jsonString: String): JsonNode {
        return mapper.readTree(jsonString)
    }

    /**
     * JsonNode转换为对象
     */
    @JvmStatic
    fun <T> convertToObject(node: JsonNode, type: Class<T>): T {
        return mapper.convertValue(node, type)
    }

    /**
     * 对象转换为JSON字符串
     */
    @JvmStatic
    fun writeToJsonString(obj: Any): String {
        return mapper.writeValueAsString(obj)
    }

    /**
     * 对象转换为JsonNode
     */
    @JvmStatic
    fun convertToJsonNode(obj: Any): JsonNode {
        return mapper.convertValue(obj, JsonNode::class.java)
    }


    /**
     * JsonNode差异转换为JsonPatch（RFC 6902）对应的JsonNode
     */
    @JvmStatic
    fun diffAsJsonPatch(sourceJsonNode: JsonNode, targetJsonNode: JsonNode): JsonNode {
        val flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone()
        return JsonDiff.asJson(sourceJsonNode, targetJsonNode, flags)
    }

    /**
     * 对象差异转换为JsonPatch（RFC 6902）对应的JsonNode
     */
    @JvmStatic
    fun diffAsJsonPatch(sourceObject: Any, targetObject: Any): JsonNode {
        val sourceJsonNode = convertToJsonNode(sourceObject)
        val targetJsonNode = convertToJsonNode(targetObject)
        return diffAsJsonPatch(sourceJsonNode, targetJsonNode)
    }

    /**
     * 对象差异转换为JsonPatch（RFC 6902）字符串
     */
    @JvmStatic
    fun diffAsJsonPatchString(sourceObject: Any, targetObject: Any): String {
        return diffAsJsonPatch(sourceObject, targetObject).toString();
    }

    /**
     * JsonNode应用JsonPatch（RFC 6902）
     */
    @JvmStatic
    fun applyJsonPatch(source: JsonNode, jsonPatch: JsonNode): JsonNode {
        return JsonPatch.apply(jsonPatch, source)
    }

    /**
     * JsonNode应用JsonPatch（RFC 6902）
     */
    @JvmStatic
    fun applyJsonPatchInPlace(source: JsonNode, jsonPatch: JsonNode) {
        return JsonPatch.applyInPlace(jsonPatch, source)
    }

    /**
     * JsonNode应用JsonPatch（RFC 6902）
     */
    @JvmStatic
    fun applyJsonPatch(source: JsonNode, jsonPatchString: String): JsonNode {
        return JsonPatch.apply(readToJsonNode(jsonPatchString), source)
    }

    /**
     * JsonNode应用JsonPatch（RFC 6902）
     */
    @JvmStatic
    fun applyJsonPatchInPlace(source: JsonNode, jsonPatchString: String) {
        return JsonPatch.applyInPlace(readToJsonNode(jsonPatchString), source)
    }

    /**
     * 对象应用JsonPatch（RFC 6902）
     */
    @JvmStatic
    fun <T> applyJsonPatch(obj: T, jsonPatch: JsonNode): T {
        val nonNullObj = obj!!
        return convertToObject(JsonPatch.apply(jsonPatch, convertToJsonNode(nonNullObj)), nonNullObj::class.java)
    }

    /**
     * 对象应用JsonPatch（RFC 6902）字符串
     */
    @JvmStatic
    fun <T> applyJsonPatch(obj: T, jsonPatchString: String): T {
        val notNullObj = obj!!
        return convertToObject(
            JsonPatch.apply(readToJsonNode(jsonPatchString), convertToJsonNode(notNullObj)),
            notNullObj::class.java
        )
    }
}