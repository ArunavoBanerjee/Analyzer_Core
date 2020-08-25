package analyzer.Engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StrPatternMatcher {
	private static volatile StrPatternMatcher instance = null;

	private StrPatternMatcher() {

	}

	public static StrPatternMatcher getInstance() {
		if (instance == null) {
			synchronized (StrPatternMatcher.class) {
				instance = new StrPatternMatcher();
			}
		}
		return instance;
	}

	public boolean str_matcher(String fieldValue, ArrayList<String> patternProp, HashSet<String> patternSet) {
//		System.out.println(fieldValue + fieldProp[0] + fieldProp[1] + fieldProp[2] + fieldProp[3] + fieldProp[4]);
		String case_match = patternProp.get(2).equals("no-fold") ? "(?i)" : "";
		String left_token = "";
		String right_token = "";
		if (patternProp.get(1).equals("equals")) {
			if (patternProp.get(2).equals("fold"))
				fieldValue = fieldValue.toLowerCase();
			if (patternSet.contains(fieldValue))
				return true;
		} else if (patternProp.get(1).equals("contains")) {
			left_token = patternProp.get(3);
			right_token = patternProp.get(4);
			for (String pattern : patternSet)
				if (fieldValue.matches(case_match + ".*" + left_token + pattern + right_token + ".*"))
					return true;
		} else if (patternProp.get(1).equals("startswith")) {
			right_token = patternProp.get(4);
			for (String pattern : patternSet)
				if (fieldValue.matches(case_match + "^" + pattern + right_token + ".*"))
					return true;
		} else if (patternProp.get(1).equals("endswith")) {
			left_token = patternProp.get(3);
			for (String pattern : patternSet)
				if (fieldValue.matches(case_match + ".*" + left_token + pattern + "$"))
					return true;
		} else if (patternProp.get(1).equals("matches")) {
			for (String pattern : patternSet)
				if (fieldValue.matches(case_match + pattern))
					return true;
		}
		return false;
	}
}
