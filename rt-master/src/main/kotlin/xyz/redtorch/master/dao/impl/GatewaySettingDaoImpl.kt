package xyz.redtorch.master.dao.impl

import org.springframework.stereotype.Service
import xyz.redtorch.common.storage.po.GatewaySetting
import xyz.redtorch.master.dao.GatewaySettingDao

@Service
class GatewaySettingDaoImpl : BaseDaoImpl<GatewaySetting>("gateway"), GatewaySettingDao