package xyz.redtorch.common.trade.enumeration

// CurrencyEnum是一个货币类型
enum class CurrencyEnum(val value: Int) {
    Unknown(0),  // 未知
    USD(1),  // 美元
    CNY(2),  // 人民币
    CNH(3),  // 离岸人民币
    HKD(4),  // 港币
    JPY(5),  // 日元
    EUR(6),  // 欧元
    GBP(7),  // 英镑
    DEM(8),  // 德国马克
    CHF(9),  // 瑞士法郎
    FRF(10),  // 法国法郎
    CAD(11),  // 加拿大元
    AUD(12),  // 澳大利亚元
    ATS(13),  // 奥地利先令
    FIM(14),  // 芬兰马克
    BEF(15),  // 比利时法郎
    THB(16),  // 泰铢
    IEP(17),  // 爱尔兰镑
    ITL(18),  // 意大利里拉
    LUF(19),  // 卢森堡法郎
    NLG(20),  // 荷兰盾
    PTE(21),  // 葡萄牙埃斯库多
    ESP(22),  // 西班牙比塞塔
    IDR(23),  // 印尼盾
    MYR(24),  // 马来西亚林吉特
    NZD(25),  // 新西兰元
    PHP(26),  // 菲律宾比索
    SUR(27),  // 俄罗斯卢布
    SGD(28),  // 新加坡元
    KRW(29)   // 韩国元
}