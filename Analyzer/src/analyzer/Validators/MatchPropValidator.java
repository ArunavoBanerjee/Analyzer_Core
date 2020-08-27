package analyzer.Validators;

import java.util.ArrayList;

public class MatchPropValidator {
	ArrayList<String> matchProp;

	public boolean validateMP(ArrayList<String> matchProp_in) throws Exception {
		matchProp = matchProp_in;
		if (!checkEmptyPattern()) {
			String dataType = matchProp_in.get(0);
			if (dataType.equalsIgnoreCase("str")) {
				str_mpValidator();
				return true;
			}
			else if (dataType.equalsIgnoreCase("regx")) {
				str_mpValidator();
				return true;
			}
			else if (dataType.equals("int")) {
				int_mpValidator();
				return true;
			}
			else if (dataType.equals("item")) {
				item_mpValidator();
				return false;
			}
			else if (dataType.matches("(?i)(coll|json|date)"))
				throw new Exception("DataType support is not provided in the current version.");
			else
				throw new Exception("Invalid dataType specified.");
		} else
			return true;
	}

	boolean checkEmptyPattern() {
		for (String value : matchProp)
			if (!value.isBlank()) {
				return false;
			}
		return true;
	}

	void str_mpValidator() throws Exception {
		if (matchProp.size() > 1 && matchProp.size() < 6) {
			if (matchProp.get(0).equals("regx") && matchProp.get(1).equals("equals"))
				throw new Exception("Regx datatype can not have equals option.");
			else if (!matchProp.get(1).matches("(?i)(startsWith|endsWith|contains|matches|equals|uniq|dupl)")) {
				throw new Exception("matchType is not valid.");
			} else {
				matchProp.set(1, matchProp.get(1).toLowerCase());
			}
			switch (matchProp.size()) {
			case 2:
				if (matchProp.get(1).equals("equals"))
					matchProp.add(2, "fold");
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
		} else
			throw new Exception("MatchType syntax Error.");
	}

	void int_mpValidator() throws Exception {
	}

	void item_mpValidator() throws Exception {
		if (!matchProp.get(1).matches("(?i)exists")) {
			throw new Exception("matchType " + matchProp.get(1) + " is not valid.\nItem datatype only supports exists condition till the current version.");
		}
	}

}
