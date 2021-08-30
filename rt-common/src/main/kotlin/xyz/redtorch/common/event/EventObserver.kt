package xyz.redtorch.common.event

interface EventObserver {
    fun handleTransactionEvent(event: Event)
    fun handleQuoteEvent(event: Event)
    fun handleCommonEvent(event: Event)
}