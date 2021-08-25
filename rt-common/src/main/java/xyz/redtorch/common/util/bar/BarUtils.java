package xyz.redtorch.common.util.bar;

import xyz.redtorch.pb.CoreEnum.BarPeriodEnum;
import xyz.redtorch.pb.CoreField.BarField;
import xyz.redtorch.pb.CoreField.TickField;

import java.util.ArrayList;
import java.util.List;

public class BarUtils {
    public static List<BarField> generate1MinBar(List<TickField> tickList) {

        List<BarField> barList = new ArrayList<>();
        BarGenerator bg = new BarGenerator(barBuilder -> {
            barBuilder.setPeriod(BarPeriodEnum.B_1Min_VALUE);
            barList.add(barBuilder.build());
        });

        for (TickField tick : tickList) {
            bg.updateTick(tick);
        }

        bg.finish();

        return barList;

    }

    public static List<BarField> generateXMinBar(List<BarField> bar1MinList, BarPeriodEnum barPeriodEnum) {

        int xMin;
        if(BarPeriodEnum.B_3Min_VALUE == barPeriodEnum.getNumber()){
            xMin = 3;
        }else if(BarPeriodEnum.B_5Min_VALUE == barPeriodEnum.getNumber()){
            xMin = 5;
        }else if(BarPeriodEnum.B_15Min_VALUE == barPeriodEnum.getNumber()){
            xMin = 15;
        }else{
            throw new RuntimeException("未定义的周期");
        }

        List<BarField> resultBarList = new ArrayList<>();
        XMinBarGenerator bg = new XMinBarGenerator(xMin, barBuilder -> {
            barBuilder.setPeriod(barPeriodEnum.getNumber());
            resultBarList.add(barBuilder.build());
        });
        for (BarField bar1Min : bar1MinList) {
            bg.updateBar(bar1Min);
        }

        bg.finish();

        return resultBarList;

    }

    public static List<BarField> generateXSecBar(List<TickField> tickList, BarPeriodEnum barPeriodEnum) {
        int xSec;
        if(BarPeriodEnum.B_5Sec_VALUE == barPeriodEnum.getNumber()){
            xSec = 5;
        }else{
            throw new RuntimeException("未定义的周期");
        }

        List<BarField> resultBarList = new ArrayList<>();
        XSecBarGenerator bg = new XSecBarGenerator(xSec, barBuilder -> {
            barBuilder.setPeriod(barPeriodEnum.getNumber());
            resultBarList.add(barBuilder.build());
        });
        for (TickField tick : tickList) {
            bg.updateTick(tick);
        }

        bg.finish();

        return resultBarList;

    }

    public static List<BarField> generateVolBar(int barVolumeChange, List<TickField> tickList) {
        List<BarField> resultBarList = new ArrayList<>();
        VolBarGenerator bg = new VolBarGenerator(barVolumeChange, barBuilder -> resultBarList.add(barBuilder.build()));
        for (TickField tick : tickList) {
            bg.updateTick(tick);
        }

        bg.finish();

        return resultBarList;

    }
}
