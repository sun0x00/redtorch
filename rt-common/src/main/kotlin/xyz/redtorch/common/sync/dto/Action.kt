package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.sync.enumeration.ActionEnum

class Action {
    var actionEnum = ActionEnum.Unknown
    var data: String? = null
}