package xyz.redtorch.slave

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.Import
import xyz.redtorch.common.event.service.impl.EventServiceImpl
import xyz.redtorch.common.utils.CommonUtils
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import java.nio.charset.StandardCharsets
import kotlin.system.exitProcess

@SpringBootApplication
@Import(value = [EventServiceImpl::class])
open class RedTorchSlaveNodeApplication


fun main(args: Array<String>) {
    val app = SpringApplication(RedTorchSlaveNodeApplication::class.java)

    // ↓↓↓↓↓ 下面的方法依赖操作系统，不一定有效↓↓↓↓↓
    val raf: RandomAccessFile?
    val fileLock: FileLock?
    try {
        raf = RandomAccessFile("rt-node-slave.lock", "rw")
        raf.seek(raf.length())
        val fileChannel = raf.channel
        fileLock = fileChannel.tryLock()
        if (fileLock == null || !fileLock.isValid) {
            println("#### 无法锁定Lock文件 ####")
            exitProcess(0)
        }
    } catch (e: IOException) {
        println("#### 锁定Lock文件异常 ####")
        e.printStackTrace()
        exitProcess(0)
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    val pidFile = File("rt-node-slave.pid")
    try {
        if (pidFile.exists()) {
            val pidStr = pidFile.readText(StandardCharsets.UTF_8)

            if (CommonUtils.isStillAlive(pidStr)) {
                println("#### PID:$pidStr 对应的进程仍在运行 ####")
                exitProcess(0)
            }
        }
    } catch (e: IOException) {
        println("#### 处理PID文件错误 ####")
        e.printStackTrace()
        exitProcess(0)
    }
    app.addListeners(ApplicationPidFileWriter(pidFile))
    app.run(*args)
}