package analyzer.Validators;

import java.util.ArrayList;

/**
 * Module to validate matchProperty construct.
 * 
 * @author banerjee.arunavo.cse16@gmail.com
 *
 */
public class MatchPropValidator {
	ArrayList<String> matchProp;
	String dataType = "", matchType = "";

	public boolean validateMP(ArrayList<String> matchProp_in) throws Exception {
		matchProp = matchProp_in;
		if (!checkEmptyPattern() && (matchProp.size() > 1 && matchProp.size() < 6)) {
			dataType = matchProp_in.get(0).strip();
			matchType = matchProp_in.get(1).strip();
			if (dataType.equalsIgnoreCase("str")) {
				str_mpValidator();
				return true;
			} else if (dataType.equalsIgnoreCase("regex")) {
				regex_mpValidator();
				return true;
			} else if (dataType.equalsIgnoreCase("int")) {
				int_mpValidator();
				return true;
			} else if (dataType.equalsIgnoreCase("item")) {
				item_mpValidator();
				return false;
			} else if (dataType.equalsIgnoreCase("uri")) {
				uri_mpValidator();
				return false;
			} else if (dataType.matches("(?i)(coll|json|date)"))
				throw new Exception("DataType support is not provided in the current version.");
			else
				throw new Exception("Invalid dataType specified.");
		} else
			throw new Exception("MatchType syntax Error. Please check MatchType specifications in Analyzer Manual.");
	}

	boolean checkEmptyPattern() {
		for (String value : matchProp)
			if (!value.isBlank()) {
				return false;
			}
		return true;
	}

	void str_mpValidator() throws Exception {
		if (dataType.equals("regx") && matchType.equals("equals"))
			throw new Exception("Regx datatype can not have equals option.");
		else if (!matchType.matches("(startswith|endswith|contains|matches|equals|uniq|dupl)")) {
			throw new Exception("matchType is not valid.");
		} else {
			matchProp.set(0, dataType.toLowerCase());
			matchProp.set(1, matchType.toLowerCase());
		}
		switch (matchProp.size()) {
		case 2:
			if (matchType.equals("equalsignore"))
				matchProp.add(2, "fold");
			else if (matchType.equals("equals"))
				matchProp.add(2, "no-fold");
			else {
				matchProp.add(2, "");
				matchProp.add(3, "");
				matchProp.add(4, "");
			}
			break;
		case 3:
			if (matchProp.get(2).isEmpty())
				matchProp.set(2, "");
			else if (matchProp.get(2).strip().matches("(?i)(fold|no-fold)"))
				matchProp.set(2, matchProp.get(2).strip().toLowerCase());
			else
				throw new Exception("matchCase is not a valid value.");
			matchProp.add(3, "");
			matchProp.add(4, "");
			break;
		case 4:
			break;
		case 5:
			break;
		default:
			break;
		}
	}
	
	void regex_mpValidator() throws Exception {
		switch(matchType) {
		case "equals":
		case "startswith":
		case "endswith":
		case "contains":
		case "uniq":
		case "dupl":
			throw new Exception("MatchType " + matchType + " is not supported for " + dataType + " dataType.\nPlease check Analyzer Manual.");
		default:
			matchProp.set(0, dataType.toLowerCase());
			matchProp.set(1, matchType.toLowerCase());
			break;
		}
		switch (matchProp.size()) {
		case 2:
				matchProp.add(2, "no-fold");
				matchProp.add(3, "");
				matchProp.add(4, "");
			break;
		case 3:
			if (matchProp.get(2).isEmpty())
				matchProp.set(2, "no-fold");
			else if (matchProp.get(2).strip().matches("(?i)(fold|no-fold)"))
				matchProp.set(2, matchProp.get(2).strip().toLowerCase());
			else
				throw new Exception("matchCase is not a valid value.");
			matchProp.add(3, "");
			matchProp.add(4, "");
			break;
		case 4:
			break;
		case 5:
			break;
		default:
			break;
		}
	}

	void int_mpValidator() throws Exception {
	}

	void uri_mpValidator() throws Exception {
		if (matchType.equalsIgnoreCase("liveExists")) {
			matchProp.set(0, dataType.toLowerCase());
			matchProp.set(1, matchType.toLowerCase());	
		} else if (matchType.equalsIgnoreCase("URIExists")) {
			matchProp.set(0, dataType.toLowerCase());
			matchProp.set(1, matchType.toLowerCase());	
		} else {
			throw new Exception(
					"DataType URI currently supports only NDLI Live verification.\nPLease check Analyzer Manual for more details.");
		}

	}

	void item_mpValidator() throws Exception {
		if (!matchProp.get(1).matches("(?i)exists|uniq")) {
			throw new Exception("matchType " + matchProp.get(1)
					+ " is not valid.\nItem datatype only supports exists condition till the current version.");
		}
	}

}
