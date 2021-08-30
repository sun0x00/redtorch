package xyz.redtorch.master.dao.impl

import org.springframework.stereotype.Service
import xyz.redtorch.common.storage.po.SlaveNode
import xyz.redtorch.master.dao.SlaveNodeDao

@Service
class SlaveNodeDaoImpl : BaseDaoImpl<SlaveNode>("node"), SlaveNodeDao