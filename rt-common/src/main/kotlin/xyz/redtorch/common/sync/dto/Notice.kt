package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.sync.enumeration.InfoLevelEnum

class Notice {
    var timestamp = System.currentTimeMillis()
    var infoLevel = InfoLevelEnum.INFO
    var info = ""
}