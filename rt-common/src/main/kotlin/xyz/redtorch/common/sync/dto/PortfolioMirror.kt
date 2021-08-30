package xyz.redtorch.common.sync.dto

import xyz.redtorch.common.trade.dto.Account
import xyz.redtorch.common.trade.dto.Position

class PortfolioMirror {
    var accountMap = HashMap<String, Account>()
    var positionMap = HashMap<String, Position>()

    fun clone(): PortfolioMirror {
        val portfolioMirror = PortfolioMirror()
        portfolioMirror.accountMap.putAll(accountMap)
        portfolioMirror.positionMap.putAll(positionMap)
        return portfolioMirror
    }
}