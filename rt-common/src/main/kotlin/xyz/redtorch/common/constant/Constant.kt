package xyz.redtorch.common.constant

import xyz.redtorch.common.trade.enumeration.OrderStatusEnum
import java.time.format.DateTimeFormatter

object Constant {

    // 调试开关
    const val isDebugEnable = false

    const val KEY_TRUE = "true"
    const val KEY_FALSE = "false"

    const val LZ4FRAME_HEADER = "lz4f-"

    const val DT_FORMAT_WITH_MS = "yyyy-MM-dd HH:mm:ss.SSS"
    const val DT_FORMAT_WITH_MS_INT = "yyyyMMddHHmmssSSS"
    const val DT_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val DT_FORMAT_INT = "yyyyMMddHHmmss"

    const val T_FORMAT_WITH_MS_INT = "HHmmssSSS"
    const val T_FORMAT_WITH_MS = "HH:mm:ss.SSS"
    const val T_FORMAT_INT = "HHmmss"
    const val T_FORMAT = "HH:mm:ss"

    const val D_FORMAT_INT = "yyyyMMdd"
    const val D_FORMAT = "yyyy-MM-dd"

    val DT_FORMAT_WITH_MS_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DT_FORMAT_WITH_MS)
    val DT_FORMAT_WITH_MS_INT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DT_FORMAT_WITH_MS_INT)
    val DT_FORMAT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DT_FORMAT)
    val DT_FORMAT_INT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DT_FORMAT_INT)

    val T_FORMAT_WITH_MS_INT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(T_FORMAT_WITH_MS_INT)
    val T_FORMAT_WITH_MS_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(T_FORMAT_WITH_MS)
    val T_FORMAT_INT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(T_FORMAT_INT)
    val T_FORMAT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(T_FORMAT)

    val D_FORMAT_INT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(D_FORMAT_INT)
    val D_FORMAT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(D_FORMAT)

    val ORDER_STATUS_FINISHED_SET = HashSet<OrderStatusEnum>().apply {
        add(OrderStatusEnum.Rejected)
        add(OrderStatusEnum.Canceled)
        add(OrderStatusEnum.AllTraded)
        add(OrderStatusEnum.Touched)
    }

    val ORDER_STATUS_WORKING_SET = HashSet<OrderStatusEnum>().apply {
        add(OrderStatusEnum.Unknown)
        add(OrderStatusEnum.NoTradeQueueing)
        add(OrderStatusEnum.NoTradeNotQueueing)
        add(OrderStatusEnum.PartTradedNotQueueing)
        add(OrderStatusEnum.PartTradedQueueing)
        add(OrderStatusEnum.NotTouched)
    }
}