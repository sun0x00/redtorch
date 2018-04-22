package xyz.redtorch.trader.module;

import xyz.redtorch.trader.engine.event.EventEngine;
import xyz.redtorch.trader.engine.main.MainEngine;

/**
 * Module抽象类
 * @author sun0x00@gmail.com
 *
 */
public abstract class ModuleAbstract implements Module{
	protected MainEngine mainEngine;
	protected EventEngine mainEventEngine;

	protected ModuleAbstract(MainEngine mainEngine){
		this.mainEngine = mainEngine;
		this.mainEventEngine = this.mainEngine.getEventEngine();
	}
}
