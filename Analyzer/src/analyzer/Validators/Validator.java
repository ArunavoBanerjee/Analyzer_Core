package analyzer.Validators;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

import analyzer.LoadData;
import analyzer.Engine.BooleanParser;
import analyzer.Engine.Data;
import analyzer.Engine.ItemPatternMatcher;
import analyzer.Engine.StrPatternMatcher;

public class Validator {
	public static String dataType = "", matchType = "", matchCase = "", left_token= "", right_token="", expr_str = "", splitlistPath = "";
	public static HashMap<String, Data> exprfieldList = new HashMap<String, Data>();
	List<String> splitexpr_input = new ArrayList<String>();
	List<String> splitexpr = new ArrayList<String>();
	List<String> expr = new ArrayList<String>();
	MatchPropValidator mpv = new MatchPropValidator();
	BooleanParser boolParse = new BooleanParser();

	public Validator() throws Exception {
		parseExpr();
		if (!splitlistPath.isEmpty())
			loadSplitList();
	}

	private void parseExpr() throws Exception {
		splitexpr_input = Arrays.asList(expr_str.split("\\s"));
		for (String eachexpr : splitexpr_input) {
			eachexpr = eachexpr.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").replaceAll("\\s+", " ");
			for (String eachexpr_norm : eachexpr.split("\\s")) {
				eachexpr_norm = eachexpr_norm.strip();
				if (!eachexpr_norm.isEmpty()) {
					String fieldName = "";
					if (eachexpr_norm.contains(":")) {
						ArrayList<String> f_prop = new ArrayList(Arrays.asList(eachexpr_norm.split(":")));
						for (String propParts : f_prop)
							if (propParts.matches("and|or|not|\\(|\\)"))
								throw new Exception(eachexpr + " boolean operators can not have matchProperty.");
						fieldName = f_prop.remove(f_prop.size() - 1);
						mpv.validateMP(f_prop);
						exprfieldList.put(fieldName, Data.getObject(f_prop));
						splitexpr.add(fieldName);
					} else if (eachexpr_norm.matches("and|or|not|\\(|\\)"))
						splitexpr.add(eachexpr_norm);
					else {
						ArrayList<String> f_prop = new ArrayList<String>(5);
						f_prop.add(dataType.toLowerCase());
						f_prop.add(matchType.toLowerCase());
						f_prop.add(matchCase.toLowerCase());
						f_prop.add(left_token.toLowerCase());
						f_prop.add(right_token.toLowerCase());
						fieldName = eachexpr_norm;
						mpv.validateMP(f_prop);
						exprfieldList.put(fieldName, Data.getObject(f_prop));
						splitexpr.add(fieldName);
					}
				}
			}
		}
		if (!boolParse.checkExprValidity(splitexpr))
			throw new Exception("SplitExpression has error at point : " + BooleanParser.valid_expr_substr);
		expr = new ArrayList<String>(splitexpr);
	}

	private void loadSplitList() throws Exception {
		LoadData newLoad = new LoadData(splitlistPath);
		if (splitlistPath.endsWith(".csv"))
			newLoad.loadDataCSV();
		else if (splitlistPath.endsWith(".xlsx"))
			newLoad.loadDataXLSX();
	}

//	void showSplitList() {
//		System.out.println(splitListMatcher.size());
//		for(Map.Entry<String, HashSet<String[]>> entry : splitListMatcher.entrySet()) {
//			for(String[] row : entry.getValue())
//				System.out.print(entry.getKey() + " : " + Arrays.asList(row)+"\n");
//		}
//	}

//	void showSplitList(PrintStream pr) {
//		System.out.println("Triplet List Size : " + splitListMatcher.size());
//		System.out.println("Writing to File ... ");
//		for(Map.Entry<String, HashSet<String[]>> entry : splitListMatcher.entrySet()) {
//			for(String[] row : entry.getValue())
//				pr.print(entry.getKey() + " : " + Arrays.asList(row)+"\n");
//		}
//	}
	public boolean validate(HashMap<String, HashSet<String>> sourceDict) {
		Boolean writetomatch = true;
		for (Map.Entry<String, Data> entry : exprfieldList.entrySet()) {
			boolean splitFlag = false;
			String testField = entry.getKey();
			Data testCondition = entry.getValue();
			//System.out.println(testCondition);
			if (sourceDict.containsKey(testField)) {
				if (testCondition == null)
					splitFlag = true;
				else {
					for (String value : sourceDict.get(testField)) {
						if (patternMatcher(testCondition, value)) {
							splitFlag = true;
							if (!splitexpr.isEmpty())
								expr.set(splitexpr.indexOf(testField), String.valueOf(true));
							break;
						}
					}
				}
			}
			if (!splitexpr.isEmpty()) {
				// System.out.println(expr+""+splitexpr+entry.getKey());
				if (splitFlag) {
					expr.set(splitexpr.indexOf(entry.getKey()), String.valueOf(true));
				} else
					expr.set(splitexpr.indexOf(entry.getKey()), String.valueOf(false));
			}
		}
		if (!splitexpr.isEmpty())
			writetomatch = boolParse.evalExpr(expr);

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
