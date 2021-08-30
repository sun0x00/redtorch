package xyz.redtorch.master.gui.bean

import javafx.beans.property.SimpleStringProperty

class UserSessionFXBean(sessionId: String, address: String, userId: String, delay: String, timeOfDuration: String) {
    private val sessionId = SimpleStringProperty()
    private var address = SimpleStringProperty()
    private val userId = SimpleStringProperty()
    private val delay = SimpleStringProperty()
    private var timeOfDuration = SimpleStringProperty()

    init {
        this.sessionId.set(sessionId)
        this.address.set(address)
        this.userId.set(userId)
        this.delay.set(delay)
        this.timeOfDuration.set(timeOfDuration)
    }


    fun getSessionId(): String {
        return sessionId.get()
    }

    fun setSessionId(value: String) {
        return sessionId.set(value)
    }

    fun sessionIdProperty(): SimpleStringProperty {
        return sessionId
    }

    fun getAddress(): String {
        return address.get()
    }

    fun setAddress(value: String) {
        return address.set(value)
    }

    fun addressProperty(): SimpleStringProperty {
        return address
    }

    fun getUserId(): String {
        return userId.get()
    }

    fun setUserId(value: String) {
        return userId.set(value)
    }

    fun userIdProperty(): SimpleStringProperty {
        return userId
    }

    fun getDelay(): String {
        return delay.get()
    }

    fun setDelay(value: String) {
        return delay.set(value)
    }

    fun delayProperty(): SimpleStringProperty {
        return delay
    }

    fun getTimeOfDuration(): String {
        return timeOfDuration.get()
    }

    fun setTimeOfDuration(value: String) {
        return timeOfDuration.set(value)
    }

    fun timeOfDurationProperty(): SimpleStringProperty {
        return timeOfDuration
    }

}