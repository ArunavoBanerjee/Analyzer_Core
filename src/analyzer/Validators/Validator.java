package analyzer.Validators;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analyzer.Evaluator.BooleanParser;
import analyzer.PatternLoader.Data;
import analyzer.PatternLoader.LoadPatterns;

/**
 * The operation done in Validator class is to validate if the split-expression follows the matchProperty construct. The construct of
 * MatchProperty is to have the components separated by the token ":" and in the order dataType:condition:case_info:fieldName. Components
 * can be mandatory or optional depending on the design specification.
 * 
 * @author banerjee.arunavo.cse16@gmail.com
 * @see <a href="https://docs.google.com/document/d/1Xqhd1NWnlNVF2UZJT8LKMb5gKaaRa_hnMvktb_kBpLg/edit?usp=sharing">Analyzer-How-To</a>
 *
 */
public class Validator {
	public static String dataType = "", matchType = "", matchCase = "", left_token = "", right_token = "", expr_str = "", splitlistPath = "";
	public static HashMap<String, Data> exprfieldList = new HashMap<String, Data>();
	List<String> splitexpr_input = new ArrayList<String>();
	public List<String> splitexpr = new ArrayList<String>();
	public ArrayList<String> expr = new ArrayList<String>();
	MatchPropValidator mpv = new MatchPropValidator();
	public BooleanParser boolParse = new BooleanParser();
	Boolean patternLoadRequired = false;

	public Validator() throws Exception {
		parseExpr();
		if (patternLoadRequired) {
			if (splitlistPath.isEmpty())
				throw new Exception("SplitList file is empty. Please check configuration.");
			else
				loadSplitList();
		}
	}

	/**
	 * Parsing and validating split expression. The method parse the text form of the split-expression and segments it to matchProperty
	 * structure. Each segments are then passed through the validation method {@link analyzer.Validators.MatchPropValidator#validateMP}.
	 * 
	 * @throws Exception if the expression is invalid as per matchProperty construct.
	 * @see MatchPropValidator
	 */
	private void parseExpr() throws Exception {
		splitexpr_input = Arrays.asList(expr_str.split("\\s+")); // splitting text expression to processing segments.
		for (String eachexpr : splitexpr_input) {
			eachexpr = eachexpr.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ");
			for (String eachexpr_norm : eachexpr.split("\\s")) { // TODO test eachexpr_norm for various indentations.
				String fieldName = "";
				if (eachexpr_norm.contains(":")) {
					ArrayList<String> f_prop = new ArrayList(Arrays.asList(eachexpr_norm.split(":")));
					for (String propParts : f_prop)
						if (propParts.matches("and|or|not|\\(|\\)"))
							throw new Exception(eachexpr + " boolean operators can not have matchProperty.");
					fieldName = f_prop.remove(f_prop.size() - 1); // fieldName is not required for matchProperty validation
					// matchProperty construct validation. {datatype:condition<optional>:case_info<optional>}. Throws java.lang.Exception if not validated. Else
					// returns boolean result if the validation condition requires external data loading.
					patternLoadRequired = mpv.validateMP(f_prop);
					// TODO One of the most important step. Validate in details.
					exprfieldList.put(fieldName, Data.getObject(f_prop, patternLoadRequired));
					splitexpr.add(fieldName);
				} else if (eachexpr_norm.matches("and|or|not|\\(|\\)")) {
					splitexpr.add(eachexpr_norm);
				}
				else {
					ArrayList<String> f_prop = new ArrayList<String>(5);
					f_prop.add(dataType.toLowerCase());
					f_prop.add(matchType.toLowerCase());
					f_prop.add(matchCase.toLowerCase());
					f_prop.add(left_token.toLowerCase());
					f_prop.add(right_token.toLowerCase());
					fieldName = eachexpr_norm;
					mpv.validateMP(f_prop);
					patternLoadRequired = true;
					exprfieldList.put(fieldName, Data.getObject(f_prop, patternLoadRequired));
					splitexpr.add(fieldName);
				}
			}
		}
		if (!boolParse.checkExprValidity(splitexpr))
			throw new Exception("SplitExpression has error at point : " + BooleanParser.valid_expr_substr);
		expr = new ArrayList<String>(splitexpr);
	}

	private void loadSplitList() throws Exception {
		LoadPatterns newLoad = new LoadPatterns(splitlistPath);
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
}
