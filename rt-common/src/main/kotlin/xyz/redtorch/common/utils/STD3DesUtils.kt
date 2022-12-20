package xyz.redtorch.common.utils

import java.nio.charset.Charset
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.IvParameterSpec


object STD3DesUtils {

    @Throws(Exception::class)
    @JvmStatic
    fun des3EncodeECBBase64String(keyStr: String, dataStr: String): String {
        val data = dataStr.toByteArray(charset("UTF-8"))
        val key = keyStr.toByteArray()
        val result = des3EncodeECB(key, data)
        return Base64.getEncoder().encodeToString(result)
    }

    @Throws(Exception::class)
    @JvmStatic
    fun des3DecodeECBBase64String(keyStr: String, base64DataStr: String): String {
        val data: ByteArray = Base64.getDecoder().decode(base64DataStr)
        val key = keyStr.toByteArray()
        val result = des3DecodeECB(key, data)
        return String(result, Charset.forName("UTF-8"))
    }

    /**
     * ECB加密,不要IV
     *
     * @param key  密钥
     * @param data 明文
     * @return Base64编码的密文
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun des3EncodeECB(key: ByteArray?, data: ByteArray): ByteArray {
        val spec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("desede")
        val desKey = keyFactory.generateSecret(spec)
        val cipher: Cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, desKey)
        return cipher.doFinal(data)
    }

    /**
     * ECB解密,不要IV
     *
     * @param key  密钥
     * @param data Base64编码的密文
     * @return 明文
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun des3DecodeECB(key: ByteArray?, data: ByteArray): ByteArray {
        val spec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("desede")
        val desKey = keyFactory.generateSecret(spec)
        val cipher: Cipher = Cipher.getInstance("desede" + "/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, desKey)
        return cipher.doFinal(data)
    }

    /**
     * CBC加密
     *
     * @param key   密钥
     * @param keyIv IV
     * @param data  明文
     * @return Base64编码的密文
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun des3EncodeCBC(key: ByteArray, keyIv: ByteArray, data: ByteArray): ByteArray {
        val spec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("desede")
        val desKey = keyFactory.generateSecret(spec)
        val cipher: Cipher = Cipher.getInstance("desede" + "/CBC/PKCS5Padding")
        val ips = IvParameterSpec(keyIv)
        cipher.init(Cipher.ENCRYPT_MODE, desKey, ips)
        return cipher.doFinal(data)
    }

    /**
     * CBC解密
     *
     * @param key   密钥
     * @param keyIv IV
     * @param data  Base64编码的密文
     * @return 明文
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun des3DecodeCBC(key: ByteArray, keyIv: ByteArray, data: ByteArray): ByteArray {
        val desKey: Key?
        val spec = DESedeKeySpec(key)
        val keyFactory = SecretKeyFactory.getInstance("desede")
        desKey = keyFactory.generateSecret(spec)
        val cipher: Cipher = Cipher.getInstance("desede" + "/CBC/PKCS5Padding")
        val ips = IvParameterSpec(keyIv)
        cipher.init(Cipher.DECRYPT_MODE, desKey, ips)
        return cipher.doFinal(data)
    }
}

//fun main() {
//    val key = "f510b8737344cddbca1c8564".toByteArray()
//    // byte[] keyiv = {0x66,0x6f,0x61,0x6f,0x63,0x75,0x65,0x6e};
//    val keyIv = byteArrayOf(
//        'f'.code.toByte(),
//        'o'.code.toByte(),
//        'a'.code.toByte(),
//        'o'.code.toByte(),
//        'c'.code.toByte(),
//        'u'.code.toByte(),
//        'e'.code.toByte(),
//        'n'.code.toByte()
//    )
//
//    println(
//        "key.length:${key.size}"
//    )
//    val data = "中国ABCabc1234".encodeToByteArray()
//    println("ECB加密解密")
//
//    val des3ECBEncodeData = STD3DesUtils.des3EncodeECB(key, data)
//    val des3ECBDecodeData = STD3DesUtils.des3DecodeECB(key, des3ECBEncodeData)
//    println(Base64.getEncoder().encodeToString(des3ECBEncodeData))
//    println(String(des3ECBDecodeData, Charset.forName("UTF-8")))
//    println("<=============>")
//
//    println("CBC加密解密")
//
//    val des3CBCEncodeData = STD3DesUtils.des3EncodeCBC(key, keyIv, data)
//    val des3CBCDecodeData = STD3DesUtils.des3DecodeCBC(key, keyIv, des3CBCEncodeData)
//    println(Base64.getEncoder().encodeToString(des3CBCEncodeData))
//    println(String(des3CBCDecodeData, Charset.forName("UTF-8")))
//
//}