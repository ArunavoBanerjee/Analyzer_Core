package analyzer.Evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import analyzer.PatternLoader.Data;
import analyzer.Validators.Validator;

public class Evaluator {
Validator v = null;
	public Evaluator(Validator _v_in) {
		this.v = _v_in;
	}
	public boolean evaluate(HashMap<String, HashSet<String>> sourceDict) {
		Boolean writetomatch = true;
		for (Map.Entry<String, Data> entry : Validator.exprfieldList.entrySet()) {
			boolean splitFlag = false;
			String testField = entry.getKey();
			Data testCondition = entry.getValue();
			if (sourceDict.containsKey(testField)) {
				if (testCondition == null)
					splitFlag = true;
				else {
					for (String value : sourceDict.get(testField)) {
						if (patternMatcher(testCondition, value)) {
							splitFlag = true;
							if (!v.splitexpr.isEmpty())
								v.expr.set(v.splitexpr.indexOf(testField), String.valueOf(true));
							break;
						}
					}
				}
			}
			if (!v.splitexpr.isEmpty()) {
				// System.out.println(expr+""+splitexpr+entry.getKey());
				if (splitFlag) {
					v.expr.set(v.splitexpr.indexOf(entry.getKey()), String.valueOf(true));
				} else
					v.expr.set(v.splitexpr.indexOf(entry.getKey()), String.valueOf(false));
			}
		}
		if (!v.splitexpr.isEmpty())
			writetomatch = v.boolParse.evalExpr(v.expr);

		return writetomatch;
	}
	
	boolean patternMatcher(Data data, String fieldValue) {
		for (ArrayList<String> patternClass : data.patternMap.keySet()) {
			String dataType = patternClass.get(0);
			if (dataType.matches("str|regx") && StrPatternMatcher.getInstance().str_matcher(fieldValue, patternClass, data.patternMap.get(patternClass)))
				return true;
			else if (dataType.matches("item") && ItemPatternMatcher.getInstance().item_matcher(fieldValue, patternClass, data))
				return true;
		}
		return false;
	}
	
}
