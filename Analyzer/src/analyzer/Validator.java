package analyzer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

import engine.BooleanParser;

public class Validator {
	static String matchType = "", matchCase = "", expr_str = "", splitlistPath = "", dataType = "";
	static HashMap<String, String[]> exprfieldList = new HashMap<String, String[]>();
	List<String> splitexpr_input = new ArrayList<String>();
	List<String> splitexpr = new ArrayList<String>();
	List<String> expr = new ArrayList<String>();
	HashMap<String, HashSet<String[]>> splitListMatcher = new HashMap<String, HashSet<String[]>>();
	HashMap<String[], HashSet<String>> splitListMatcherEquals = new HashMap<String[], HashSet<String>>();
	Set<String> existsList = new HashSet<String>();
	Set<String> dupList = new HashSet<String>();
	Set<String> dupSet = new HashSet<String>();
	BooleanParser boolParse = new BooleanParser();

	public Validator() throws Exception {
		parseExpr();
		loadSplitList();
	}

	private void parseExpr() throws Exception {
		splitexpr_input = Arrays.asList(expr_str.split("\\s"));
		for (String eachexpr : splitexpr_input) {
			eachexpr = eachexpr.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").replaceAll("\\s+", " ");
			for (String eachexpr_norm : eachexpr.split("\\s")) {
				eachexpr_norm = eachexpr_norm.strip();
				if (!eachexpr_norm.isEmpty()) {
					if (eachexpr_norm.contains(":")) {
						String[] f_prop = eachexpr_norm.split(":");
						for (String propParts : f_prop)
							if (propParts.matches("and|or|not|\\(|\\)"))
								throw new Exception(eachexpr + " boolean operand can not have matchProperty.");
						if (f_prop.length != 4)
							throw new Exception(eachexpr + " has wrongly defined matchProperty.");
						else {
							if (!f_prop[0].matches("(?i)startsWith|endsWith|contains|matches|exists|equals|uniq"))
								throw new Exception(eachexpr + " has wrongly defined matchProperty.");
							else
								f_prop[0] = f_prop[0].toLowerCase();
							if (!(f_prop[1].matches("(?i)str|regx") || f_prop[0].matches("(?i)(exists|uniq)")))
								throw new Exception(eachexpr + " has wrongly defined matchProperty.");
							else
								f_prop[1] = f_prop[1].toLowerCase();
							if (!(f_prop[2].matches("(?i)fold|no-fold") || f_prop[0].matches("(?i)(exists|uniq)")))
								throw new Exception(eachexpr + " has wrongly defined matchProperty.");
							else
								f_prop[2] = f_prop[2].toLowerCase();
							if (f_prop[0].equals("equals") && f_prop[1].equals("regx"))
								throw new Exception(
										eachexpr + " has wrongly defined matchProperty." + "\nequals can not be combined with regx. Please refer documentation.");
							exprfieldList.put(f_prop[3], f_prop);
							splitexpr.add(f_prop[3]);
						}
					} else if (eachexpr_norm.matches("and|or|not|\\(|\\)"))
						splitexpr.add(eachexpr_norm);
					else {
						String[] f_prop = new String[3];
						f_prop[0] = matchType.toLowerCase();
						f_prop[1] = dataType.toLowerCase();
						f_prop[2] = matchCase.toLowerCase();
						exprfieldList.put(eachexpr_norm, f_prop);
						splitexpr.add(eachexpr_norm);
					}
				}
			}
		}
		for (Map.Entry<String, String[]> splitProp : exprfieldList.entrySet())
			if (splitProp.getValue()[0].equals("exists"))
				existsList.add(splitProp.getKey());
			else if (splitProp.getValue()[0].equals("dup"))
				dupList.add(splitProp.getKey());
		if (!boolParse.checkExprValidity(splitexpr))
			throw new Exception("SplitExpression has error at point : " + BooleanParser.valid_expr_substr);
		expr = new ArrayList<String>(splitexpr);
	}

	private void loadSplitList() throws Exception {
		LoadData newLoad = new LoadData();
		if (splitlistPath.endsWith(".csv"))
			newLoad.loadDataCSV(splitListMatcher, splitListMatcherEquals, splitlistPath);
		else if (splitlistPath.endsWith(".xlsx"))
			newLoad.loadDataXLSX(splitListMatcher, splitListMatcherEquals, splitlistPath);
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
	protected boolean validate(HashMap<String, HashSet<String>> sourceDict) {
		Boolean writetomatch = true;
		for (Map.Entry<String, HashSet<String[]>> entry : splitListMatcher.entrySet()) {
			boolean testsplit = false;
			if (sourceDict.containsKey(entry.getKey())) {
				for (String value : sourceDict.get(entry.getKey())) {
					if (listMatcher(value, entry.getValue())) {
						testsplit = true;
						if (!splitexpr.isEmpty())
							expr.set(splitexpr.indexOf(entry.getKey()), String.valueOf(true));
						break;
					}
				}
			}
			if (!splitexpr.isEmpty()) {
				// System.out.println(expr+""+splitexpr+entry.getKey());
				if (!testsplit) {
					expr.set(splitexpr.indexOf(entry.getKey()), String.valueOf(false));
				}
			} else
				writetomatch &= testsplit;
		}
		for (Map.Entry<String[], HashSet<String>> entry : splitListMatcherEquals.entrySet()) {
			boolean testsplit = false;
			if (sourceDict.containsKey(entry.getKey()[0])) {
				for (String value : sourceDict.get(entry.getKey()[0])) {
					if (entry.getKey()[1].equalsIgnoreCase("fold"))
						value = value.toLowerCase();
					if (entry.getValue().contains(value)) {
						testsplit = true;
						if (!splitexpr.isEmpty())
							expr.set(splitexpr.indexOf(entry.getKey()[0]), String.valueOf(true));
						break;
					}
				}
			}
			if (!splitexpr.isEmpty()) {
				if (!testsplit) {
//						System.out.println(entry.getKey()[0]);
//						System.out.println(splitexpr+""+expr);
					expr.set(splitexpr.indexOf(entry.getKey()[0]), String.valueOf(false));
				}
			} else
				writetomatch &= testsplit;
		}
		for (String existsID : existsList) {
			boolean testsplit = false;
			if (sourceDict.containsKey(existsID)) {
				testsplit = true;
				expr.set(splitexpr.indexOf(existsID), String.valueOf(true));
			}
			if (!testsplit)
				expr.set(splitexpr.indexOf(existsID), String.valueOf(false));
		}
		for (String dupID : dupList) {
			boolean testsplit = false;
			if (sourceDict.containsKey(dupID)) {
				for (String value : sourceDict.get(dupID)) {
					value = value.toLowerCase();
					if (!dupSet.contains(value)) {
						testsplit = true;
						dupSet.add(value);
						expr.set(splitexpr.indexOf(dupID), String.valueOf(true));
					}
				}
			}
			if (!testsplit)
				expr.set(splitexpr.indexOf(dupID), String.valueOf(false));
		}
		if (!splitexpr.isEmpty())
			writetomatch = boolParse.evalExpr(expr);

		return writetomatch;
	}

	boolean listMatcher(String fieldValue, HashSet<String[]> listProp) {
		for (String[] srctriple : listProp) {
//			System.out.println(fieldValue + Arrays.asList(srctriple));
			// if(srctriple[2].matches("str|regx"))
			if (str_matcher(fieldValue, srctriple))
				return true;
		}
		return false;
	}

	boolean str_matcher(String fieldValue, String[] fieldProp) {
//		System.out.println(fieldValue + fieldProp[0] + fieldProp[1] + fieldProp[2] + fieldProp[3] + fieldProp[4]);
		String case_match = fieldProp[2].equals("fold") ? "(?i)" : "";
		if (fieldProp[1].equals("equals") && fieldProp[2].equals("fold") && fieldValue.equalsIgnoreCase(fieldProp[0]))
			return true;
		else if (fieldProp[1].equals("equals") && fieldProp[2].equals("no-fold") && fieldValue.equals(fieldProp[0]))
			return true;
		else if (fieldProp[1].equals("contains") && fieldValue.matches(case_match + ".*" + fieldProp[3] + fieldProp[0] + fieldProp[4] + ".*")) {
			return true;
		} else if (fieldProp[1].equals("startswith") && fieldValue.matches(case_match + "^" + fieldProp[0] + fieldProp[4] + ".*"))
			return true;
		else if (fieldProp[1].equals("endswith") && fieldValue.matches(case_match + ".*" + fieldProp[3] + fieldProp[0] + "$"))
			return true;
		else if (fieldProp[1].equals("matches") && fieldValue.matches(case_match + fieldProp[0]))
			return true;
		else
			return false;
	}
}
