package xyz.redtorch.common.trade.enumeration

// ExchangeEnum是一个交易所类型
enum class ExchangeEnum(val value: Int) {
    Unknown(0),  // 未知
    SSE(1),  // 上海证券交易所	www.sse.com.cn
    SZSE(2),  //深圳证券交易所	www.szse.cn
    CFFEX(3),  // 中国金融期货交易所	www.cffex.com.cn
    SHFE(4),  // 上海期货交易所	www.shfe.com.cn
    CZCE(5),  // 郑州商品交易所	www.czce.com.cn
    DCE(6),  // 大连商品交易所	www.dce.com.cn
    SGE(7),  // 上海黄金交易所	www.sge.com.cn
    INE(8),  // 上海国际能源交易中心	www.ine.cn
    GFEX(9),  // 广州期货交易所  www.gfex.com.cn/
    SEHK(10),  // Hong Kong Stock Exchange (SEHK)	www.hkex.com.hk
    HKFE(11),  // Hong Kong Futures Exchange (HKFE)	www.hkex.com.hk
    SGX(12),  // 新加坡交易所(Singapore Exchange (SGX))	www.sgx.com
    NYMEX(13),  // 纽约商业交易所(New York Mercantile Exchange (NYMEX))	www.nymex.com
    CFE(14),  // 芝加哥期权交易所期货交易分所(CBOE Futures Exchange (CFE))	www.cboe.com/cfe
    GLOBEX(15),  // CME (GLOBEX)	www.cmegroup.com
    CMECRYPTO(16), // CME Cryptocurrencies	www.cmegroup.com/trading/equity-index/us-index/bitcoin_contract_specifications.html
    ICEEU(17),  // Intercontinental Exchange (ICEEU)	www.theice.com
    ICEEUSOFT(18), // Intercontinental Exchange (ICEEUSOFT)	www.theice.com
    ICEUS(19), //  ICE 期货 US (ICEUS)	www.theice.com/futures_us.jhtml
    IPE(20),  // 洲际交易所(Intercontinental Exchange (ICE/IPE))	www.theice.com
    LMEOTC(21),  // LMEOTC	伦敦金属交易所场外期货Lookalike平台
    ECBOT(22), // 芝加哥期货交易所（电子平台芝加哥期货交易所）(CBOT (ECBOT))	www.cmegroup.com
    APEX(23), // 新加坡亚太交易所	www.asiapacificex.com
    BMD(24), // 马来西亚衍生产品交易所	www.bursamalaysia.com
    MONEP(25), // Euronext France (MONEP)	www.euronext.com
    DTB(26), // EUREX (DTB)	www.eurexchange.com
    TOCOM(27), // 东京工业品交易所	www.tocom.or.jp
    TAIFEX(28), // 台湾期货交易所	www.taifex.com.tw
    SEHKSZSE(29), // Shenzhen-Hong Kong Stock Connect (SEHKSZSE)	www.hkex.com.hk/eng/csm/index.htm
    SEHKNTL(30), // Shanghai-Hong Kong Stock Connect (SEHKNTL)	www.hkex.com.hk/eng/csm/index.htm
    KSE(31), // Korea Stock Exchange (KSE)	eng.krx.co.kr
    OSE(32), // Osaka Exchange (OSE.JPN)	www.jpx.co.jp
    IB_IDEALPRO(33),  // IDEALPRO
    IB_SMART(34),  // IB智能路由
    NYBOT(35)  // ICE Futures U.S. (NYBOT)	www.theice.com
}