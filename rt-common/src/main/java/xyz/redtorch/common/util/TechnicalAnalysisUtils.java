package xyz.redtorch.common.util;

import java.util.ArrayList;
import java.util.List;

public class TechnicalAnalysisUtils {
	public static List<Double> calculateMA(List<Double> valueList, int n) {
		List<Double> result = new ArrayList<>();
		for (int i = 0, len = valueList.size(); i < len; i++) {
			if (i < n) {
				result.add(null);
				continue;
			}
			Double sum = 0.0;
			for (var j = 0; j < n; j++) {
				sum += valueList.get(i - j);
			}
			result.add(sum / n);
		}
		return result;
	}
}
