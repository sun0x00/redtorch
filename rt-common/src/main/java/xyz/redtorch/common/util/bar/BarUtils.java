package xyz.redtorch.common.util.bar;

import java.util.ArrayList;
import java.util.List;

import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

public class BarUtils {
	public static List<BarField> generate1MinBar(List<TickField> tickList) {

		List<BarField> barList = new ArrayList<>();
		BarGenerator bg = new BarGenerator(new CommonBarCallBack() {
			@Override
			public void call(BarField bar) {
				barList.add(bar);
			}
		});

		for (TickField tick : tickList) {
			bg.updateTick(tick);
		}

		bg.finish();
		
		return barList;

	}

	public static List<BarField> generateXMinBar(int xMin, List<BarField> bar1MinList) {
		List<BarField> resultBarList = new ArrayList<>();
		XMinBarGenerator bg = new XMinBarGenerator(xMin, new CommonBarCallBack() {
			@Override
			public void call(BarField bar) {
				resultBarList.add(bar);
			}
		});
		for (BarField bar1Min : bar1MinList) {
			bg.updateBar(bar1Min);
		}
		
		bg.finish();
		
		return resultBarList;

	}
	
	public static List<BarField> generateXSecBar(int xSec, List<TickField> tickList) {
		List<BarField> resultBarList = new ArrayList<>();
		XSecBarGenerator bg = new XSecBarGenerator(xSec, new CommonBarCallBack() {
			@Override
			public void call(BarField bar) {
				resultBarList.add(bar);
			}
		});
		for (TickField tick : tickList) {
			bg.updateTick(tick);
		}
		
		bg.finish();
		
		return resultBarList;

	}
	
}
