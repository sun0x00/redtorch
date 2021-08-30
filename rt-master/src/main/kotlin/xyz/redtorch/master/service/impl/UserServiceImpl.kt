package xyz.redtorch.master.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.redtorch.common.storage.po.User
import xyz.redtorch.master.dao.UserDao
import xyz.redtorch.master.service.UserService
import xyz.redtorch.master.web.socket.TradeClientWebSocketHandler
import java.util.*

@Service
class UserServiceImpl : UserService {

    private val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    @Autowired
    private lateinit var userDao: UserDao

    @Autowired
    private lateinit var tradeClientWebSocketHandler: TradeClientWebSocketHandler

    override fun getUserList(): List<User> {
        return userDao.queryList()
    }

    override fun deleteUserById(userId: String) {
        userDao.deleteById(userId)

        // 断开可能存在的WebSocket连接
        tradeClientWebSocketHandler.closeByUserId(userId)
    }

    override fun resetUserTokenById(userId: String): User? {
        val user = userDao.queryById(userId)
        if (user == null) {
            logger.error("更新令牌失败,未能查出用户,userId={}", userId)
            return null
        }

        user.token = UUID.randomUUID().toString()
        userDao.upsert(user)

        // 断开可能存在的WebSocket连接
        tradeClientWebSocketHandler.closeByUserId(userId)
        return user
    }

    override fun addUser(userId: String, description: String): User? {
        val queriedUser = userDao.queryById(userId)
        if (queriedUser != null) {
            logger.error("增加用户错误,userId重复")
            return null
        }
        val user = User()
        user.id = userId
        user.description = description
        user.token = UUID.randomUUID().toString()

        userDao.upsert(user)

        return user
    }

    override fun userAuth(userId: String, token: String): User? {
        val user = userDao.queryById(userId)
        return if (user != null && user.token == token) {
            logger.info("用户审核成功,userId={}", userId)
            user
        } else {
            logger.info("用户审核失败,userId={}", userId)
            null
        }
    }

    override fun updateUserDescriptionById(userId: String, description: String) {
        val user = userDao.queryById(userId)
        if (user != null) {
            user.description = description
            userDao.upsert(user)
        } else {
            logger.warn("更新用户描述失败,未查出用户,userId={}", userId)
        }
    }

    override fun upsertUserById(user: User) {
        userDao.upsert(user)
    }

    override fun enableUserById(userId: String) {
        val user = userDao.queryById(userId)
        if (user != null) {
            user.banned = false
            userDao.upsert(user)
        } else {
            logger.warn("启用用户失败,未查出用户,userId={}", userId)
        }
    }

    override fun banUserById(userId: String) {
        val user = userDao.queryById(userId)
        if (user != null) {
            user.banned = true
            userDao.upsert(user)
        } else {
            logger.warn("封禁用户失败,未查出用户,userId={}", userId)
        }
        // 断开可能存在的WebSocket连接
        tradeClientWebSocketHandler.closeByUserId(userId)
    }
}