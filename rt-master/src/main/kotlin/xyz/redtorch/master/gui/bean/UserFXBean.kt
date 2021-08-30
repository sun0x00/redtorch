package xyz.redtorch.master.gui.bean

import javafx.beans.property.SimpleStringProperty
import xyz.redtorch.common.storage.po.User

class UserFXBean(private val user: User) {
    private val id = SimpleStringProperty()
    private var token = SimpleStringProperty()
    private var description = SimpleStringProperty()
    private var banned = SimpleStringProperty()

    init {
        id.set(user.id ?: "")
        token.set(user.token ?: "")
        description.set(user.description ?: "")

        if (user.banned) {
            banned.set("已封禁")
        } else {
            banned.set("正常")
        }
    }

    fun getUser(): User {
        return user
    }

    fun getId(): String {
        return id.get()
    }

    fun setId(value: String) {
        return id.set(value)
    }

    fun idProperty(): SimpleStringProperty {
        return id
    }

    fun getToken(): String {
        return token.get()
    }

    fun setToken(value: String) {
        return token.set(value)
    }

    fun tokenProperty(): SimpleStringProperty {
        return token
    }

    fun getDescription(): String {
        return description.get()
    }

    fun setDescription(value: String) {
        return description.set(value)
    }

    fun descriptionProperty(): SimpleStringProperty {
        return description
    }

    fun getBanned(): String {
        return banned.get()
    }

    fun setBanned(value: String) {
        return banned.set(value)
    }

    fun bannedProperty(): SimpleStringProperty {
        return banned
    }

}