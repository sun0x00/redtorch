package xyz.redtorch.common.utils

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.time.*
import java.util.*
import kotlin.math.abs
import kotlin.math.floor


object CommonUtils {

    private val logger = LoggerFactory.getLogger(CommonUtils::class.java)

    /**
     * 复制文件到临时文件夹
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFileByPath(targetDir: String, sourceFilePath: String) {
        val originFile = File(sourceFilePath)
        val targetFile = File(targetDir + File.separator + originFile.name)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        FileUtils.copyFileToDirectory(originFile, File(targetDir))
        targetFile.deleteOnExit()
    }

    /**
     * 复制URL到临时文件夹,例如从war包中
     */
    @Throws(IOException::class)
    @JvmStatic
    fun copyFileByURL(targetDir: String, sourceURL: URL) {
        val originFile = File(sourceURL.file)
        val targetFile = File(targetDir + File.separator + originFile.name)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        FileUtils.copyURLToFile(sourceURL, targetFile)
        targetFile.deleteOnExit()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun forceMkdirParent(file: File) {
        FileUtils.forceMkdirParent(file)
    }

    @JvmStatic
    fun readCsvToRecordList(file: File, charset: Charset): List<Map<String, String>> {
        val recordList = ArrayList<Map<String, String>>()
        val br = BufferedReader(InputStreamReader(FileInputStream(file), charset))

        br.use {
            // 读取表头
            val header = it.readLine()
            // 解析列名
            val columnNameList = header.split(",")
            // 标记列名在行中所处的位置
            val indexToColumnNameMap = HashMap<Int, String>()
            for ((index, columnName) in columnNameList.withIndex()) {
                indexToColumnNameMap[index] = columnName
            }
            // 读取所有行
            val lines = br.readLines()
            // 解析行
            for (line in lines) {
                val record = HashMap<String, String>()
                // 拆分列
                val columnList = line.split(",")
                for ((index, column) in columnList.withIndex()) {
                    // 根据当前解析的列的位置确定cell对应的列名
                    record[indexToColumnNameMap[index]!!] = column
                }
                recordList.add(record)
            }
        }
        return recordList
    }


    @JvmStatic
    fun millsToLocalDateTime(millis: Long): LocalDateTime {
        val instant = Instant.ofEpochMilli(millis)
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    @JvmStatic
    fun millsToLocalDateTime(millis: Long, zoneId: String): LocalDateTime {
        val instant = Instant.ofEpochMilli(millis)
        return instant.atZone(ZoneId.of(zoneId)).toLocalDateTime()
    }

    @JvmStatic
    fun localDateTimeToMills(ldt: LocalDateTime, offsetId: String): Long {
        return ldt.toInstant(ZoneOffset.of(offsetId)).toEpochMilli()
    }

    @JvmStatic
    fun localDateTimeToMills(ldt: LocalDateTime): Long {
        return ldt.toInstant(OffsetDateTime.now().offset).toEpochMilli()
    }

    @JvmStatic
    fun isStillAlive(pidStr: String): Boolean {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        val command: String
        if (os.contains("win")) {
            logger.debug("Check alive Windows mode. Pid: [{}]", pidStr)
            command = "cmd /c tasklist /FI \"PId eq $pidStr\""
            return isProcessIdRunning(pidStr, command)
        } else if (os.contains("nix") || os.contains("nux")) {
            logger.debug("Check alive Linux/Unix mode. Pid: [{}]", pidStr)
            command = "ps -p $pidStr"
            return isProcessIdRunning(pidStr, command)
        }
        logger.debug("Default Check alive for Pid: [{}] is false", pidStr)
        return false
    }

    @JvmStatic
    fun isProcessIdRunning(pid: String, command: String?): Boolean {
        logger.debug("Command [{}]", command)
        return try {
            val rt = Runtime.getRuntime()
            val pr = rt.exec(command)
            val isReader = InputStreamReader(pr.inputStream)
            val bReader = BufferedReader(isReader)
            var strLine: String
            while (bReader.readLine().also { strLine = it } != null) {
                if (strLine.contains(" $pid ")) {
                    return true
                }
            }
            false
        } catch (ex: Exception) {
            logger.warn("Got exception using system command [{}].", command, ex)
            true
        }
    }

    private const val EPSILON = 0.0000001

    @JvmStatic
    fun isEquals(a: Double, b: Double): Boolean {
        return if (a == b) true else abs(a - b) < EPSILON
    }

    @JvmStatic
    fun isNotEquals(a: Double, b: Double): Boolean {
        return !isEquals(a, b)
    }

    @JvmStatic
    fun getNumberDecimalDigits(value: Double): Int {
        try {
            if (isInteger(value)) {
                return 0
            }
            var decimalDigits = 0
            val valueStr = value.toString()
            val indexOf = valueStr.indexOf(".")
            if (indexOf > 0) {
                decimalDigits = valueStr.length - 1 - indexOf
            }
            return decimalDigits
        } catch (e: Exception) {
            logger.error("获取小数点位数异常", e)
        }
        return 4
    }

    @JvmStatic
    fun isInteger(obj: Double): Boolean {
        val eps = 1e-10 // 精度范围
        return obj - floor(obj) < eps
    }

    @JvmStatic
    fun doubleStringCompare(doubleStr1: String, doubleStr2: String): Int {
        try {
            val double1 = doubleStr1.replace(",", "").replace("%", "").toDouble()
            val double2 = doubleStr2.replace(",", "").replace("%", "").toDouble()
            return double1.compareTo(double2)
        } catch (e: Exception) {
            logger.error("排序异常", e)
        }
        return 0
    }

    fun formatDouble(value: Double, priceTick: Double): String {
        var res = "渲染异常"
        try {
            var decimalDigits = getNumberDecimalDigits(priceTick)
            if (decimalDigits < 0) {
                decimalDigits = 0
            }
            val priceStringFormat = "%,." + decimalDigits + "f"
            res = String.format(priceStringFormat, value)
        } catch (e: Exception) {
            logger.error("渲染异常", e)
        }
        return res
    }

    fun formatDouble(value: Double, decimalDigits: Int): String {
        var res = "渲染异常"
        try {
            var validDecimalDigits = 4
            if (decimalDigits >= 0) {
                validDecimalDigits = decimalDigits
            }
            val priceStringFormat = "%,." + validDecimalDigits + "f"
            res = String.format(priceStringFormat, value)
        } catch (e: Exception) {
            logger.error("渲染异常", e)
        }
        return res
    }

    fun formatDouble(value: Double, format: String): String {
        var res = "渲染异常"
        try {
            res = String.format(format, value)
        } catch (e: Exception) {
            logger.error("渲染异常", e)
        }
        return res
    }

    fun formatDouble(value: Double): String {
        return formatDouble(value, "%,.2f")
    }

}