package xyz.redtorch.common.storage.po

class User : BasePo() {
    var token: String? = null
    var description: String? = null
    var banned: Boolean = false

    var permitReadAllAccounts = true
    var acceptReadAccountIdSet: Set<String> = HashSet()
    var denyReadAccountIdSet: Set<String> = HashSet()

    var permitTradeAllAccounts = false
    var acceptTradeAccountIdSet: Set<String> = HashSet()
    var denyTradeAccountIdSet: Set<String> = HashSet()

    var permitTradeAllContracts = false
    var acceptTradeUniformSymbolSet: Set<String> = HashSet()
    var denyTradeUniformSymbolSet: Set<String> = HashSet()
}