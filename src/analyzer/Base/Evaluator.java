package analyzer.Base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import analyzer.PatternLoader.Data;
import analyzer.Validators.Field;
import analyzer.Validators.Validator;

public class Evaluator {
	Validator v = null;

	public Evaluator(Validator _v_in) {
		this.v = _v_in;
	}

	public boolean evaluate(HashMap<String, HashSet<String>> sourceDict) throws Exception {
		Boolean writetomatch = true;
		for (Map.Entry<String, Data> entry : Validator.exprfieldList.entrySet()) {
			boolean splitFlag = false;
//			String testField = entry.getKey().getField();
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
//				 System.out.println(v.expr+""+v.splitexpr+entry.getKey());
				if (splitFlag) {
					v.expr.set(v.splitexpr.indexOf(testField), String.valueOf(true));
				} else
					v.expr.set(v.splitexpr.indexOf(testField), String.valueOf(false));
			}
		}
//		System.out.println(v.expr);
		if (!v.splitexpr.isEmpty())
			writetomatch = v.boolParse.evalExpr(v.expr);

		return writetomatch;
	}

	boolean patternMatcher(Data data, String fieldValue) throws Exception {
//		System.out.println(data.patternMap);
		if(data.patternMap.isEmpty()) {
			String dataType = data.genericDefinition.get(0);
			switch(dataType) {
			case "str":
				throw new Exception("Pattern Loading Required for String dataType validation.");
			case "regex":
				throw new Exception("Pattern Loading Required for Regular Expression(regx) dataType validation.");
			case "int":
				throw new Exception("Validation with Integer dataType is currently not supported.");
			case "uri":
				return URIPatternMatcher.getInstance().uri_matcher(fieldValue, data.genericDefinition);
			}
		} else { 
		for (ArrayList<String> patternClass : data.patternMap.keySet()) {
			String dataType = patternClass.get(0);
			if (dataType.matches("str|regex") && StrPatternMatcher.getInstance().str_matcher(fieldValue, patternClass,
					data.patternMap.get(patternClass)))
				return true;
			else if (dataType.matches("item")
					&& ItemPatternMatcher.getInstance().item_matcher(fieldValue, patternClass, data))
				return true;
		}
	}
		return false;
	}

}
