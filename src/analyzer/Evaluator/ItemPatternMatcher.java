package analyzer.Evaluator;

import java.util.ArrayList;

import analyzer.PatternLoader.Data;

public class ItemPatternMatcher {
	private static volatile ItemPatternMatcher instance = null;

	private ItemPatternMatcher() {

	}

	public static ItemPatternMatcher getInstance() {
		if (instance == null) {
			synchronized (ItemPatternMatcher.class) {
				instance = new ItemPatternMatcher();
			}
		}
		return instance;
	}

	public boolean item_matcher(String fieldValue, ArrayList<String> patternProp, Data data) {
		if (patternProp.get(1).equals("uniq"))
			fieldValue = fieldValue.toLowerCase();
		if (!data.dupSet.contains(fieldValue)) {
			data.dupSet.add(fieldValue);
			return true;
		} else
			return false;
	}
}
