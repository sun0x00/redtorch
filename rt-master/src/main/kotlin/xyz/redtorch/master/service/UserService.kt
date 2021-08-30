package xyz.redtorch.master.service

import xyz.redtorch.common.storage.po.User

interface UserService {
    fun getUserList(): List<User>

    fun deleteUserById(userId: String)

    fun resetUserTokenById(userId: String): User?

    fun addUser(userId: String, description: String): User?

    fun userAuth(userId: String, token: String): User?

    fun updateUserDescriptionById(userId: String, description: String)

    fun upsertUserById(user: User)

    fun enableUserById(userId: String)

    fun banUserById(userId: String)

}