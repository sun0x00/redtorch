package xyz.redtorch.master.dao.impl

import org.springframework.stereotype.Service
import xyz.redtorch.common.storage.po.User
import xyz.redtorch.master.dao.UserDao

@Service
class UserDaoImpl : BaseDaoImpl<User>("user"), UserDao