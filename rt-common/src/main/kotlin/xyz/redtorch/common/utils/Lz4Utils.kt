package xyz.redtorch.common.utils

import net.jpountz.lz4.LZ4Factory
import net.jpountz.lz4.LZ4FastDecompressor
import net.jpountz.lz4.LZ4FrameInputStream
import net.jpountz.lz4.LZ4FrameOutputStream
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.*


object Lz4Utils {

    private val logger = LoggerFactory.getLogger(Lz4Utils::class.java)

    fun frameCompress(data: ByteArray): ByteArray? {
        val beginTimestamp = System.currentTimeMillis()

        val bOut = ByteArrayOutputStream()
        val lzOut = LZ4FrameOutputStream(bOut)

        try {
            lzOut.write(data)
            // 必须在此处关闭,否则无法读取
            lzOut.close()
            bOut.close()
            val compressed = bOut.toByteArray()
            logger.info(
                "数据长度{},压缩后长度{},压缩率{},耗时{}ms",
                data.size,
                compressed.size,
                compressed.size.toDouble() / data.size,
                System.currentTimeMillis() - beginTimestamp
            )
            return compressed
        } catch (e: Exception) {
            logger.error("压缩异常", e)
        } finally {
            try {
                lzOut.close()
                bOut.close()
            } catch (e: Exception) {
                logger.info("关闭流异常", e)
            }
        }

        return null
    }


    fun frameDecompress(compressedData: ByteArray): ByteArray? {
        val beginTimestamp = System.currentTimeMillis()

        val dataIn = ByteArrayInputStream(compressedData)
        val bIn = BufferedInputStream(dataIn)
        val lzIn = LZ4FrameInputStream(bIn)

        try {
            val restored = lzIn.readAllBytes()
            logger.info("数据长度{},解压后后长度{},耗时{}ms", compressedData.size, restored.size, System.currentTimeMillis() - beginTimestamp)
            return restored;
        } catch (e: Exception) {
            logger.error("解压异常", e)
        } finally {
            try {
                dataIn.close()
                bIn.close()
                lzIn.close()
            } catch (e: Exception) {
                logger.info("关闭流异常", e)
            }
        }
        return null

    }

    fun frameCompress(data: String): String? {
        val dataArray = data.toByteArray(Charset.forName("UTF-8"))
        val compressed = frameCompress(dataArray) ?: return null
        return Base64.getEncoder().encodeToString(compressed)
    }

    fun frameDecompress(dataBase64String: String): String? {
        val compressed = Base64.getDecoder().decode(dataBase64String)
        val restored = frameDecompress(compressed) ?: return null
        return String(restored, Charset.forName("UTF-8"))
    }

    fun compress(data: ByteArray): Pair<ByteArray, Int> {
        val beginTimestamp = System.currentTimeMillis()
        val factory = LZ4Factory.fastestInstance()

        val decompressedLength = data.size

        val compressor = factory.fastCompressor()
        val maxCompressedLength = compressor.maxCompressedLength(decompressedLength)
        val compressed = ByteArray(maxCompressedLength)
        val compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength)

        logger.info(
            "数据长度{},压缩后长度{},压缩率{},耗时{}ms",
            data.size,
            compressedLength,
            compressedLength / data.size,
            System.currentTimeMillis() - beginTimestamp
        )
        return Pair(compressed.sliceArray(0 until compressedLength), decompressedLength)
    }

    fun decompress(compressedData: ByteArray, decompressedLength: Int): ByteArray {
        val beginTimestamp = System.currentTimeMillis()
        val factory = LZ4Factory.fastestInstance()
        val decompressor: LZ4FastDecompressor = factory.fastDecompressor()
        val restored = ByteArray(decompressedLength)
        decompressor.decompress(compressedData, 0, restored, 0, decompressedLength)
        logger.info("数据长度{},解压后后长度{},耗时{}ms", compressedData.size, restored.size, System.currentTimeMillis() - beginTimestamp)
        return restored
    }

    fun compress(data: String): Pair<String, Int> {
        val dataArray = data.toByteArray(Charset.forName("UTF-8"))
        val compressedPair = compress(dataArray)
        return Pair(Base64.getEncoder().encodeToString(compressedPair.first), compressedPair.second)
    }

    fun decompress(dataBase64String: String, decompressedLength: Int): String {
        val compressed = Base64.getDecoder().decode(dataBase64String)
        val restored = decompress(compressed, decompressedLength)
        return String(restored, Charset.forName("UTF-8"))
    }

}