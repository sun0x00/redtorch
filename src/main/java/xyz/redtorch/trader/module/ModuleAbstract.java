package xyz.redtorch.trader.module;

import xyz.redtorch.trader.engine.event.FastEventDynamicHandlerAbstract;
import xyz.redtorch.trader.engine.main.MainEngine;

/**
 * Module抽象类
 * @author sun0x00@gmail.com
 *
 */
public abstract class ModuleAbstract extends FastEventDynamicHandlerAbstract implements Module{
	protected MainEngine mainEngine;

	protected ModuleAbstract(MainEngine mainEngine){
		this.mainEngine = mainEngine;
	}
}
