package analyzer;

import java.util.Arrays;

public class MatchPropValidator {
String dataType = "", cmd = "", data = "";
String[] matchProp;

	protected String[] valdateMP(String[] matchProp_in, String data_in) throws Exception{
		matchProp = matchProp_in;
		data = data_in;
		cmd = matchProp[0];
		dataType = matchProp[1];
		if(cmd.equals("exists"))
			throw new Exception("matchType exists is not applicable to data level.");
		if(dataType.matches("str|regx"))
			return str_mpValidator();
		if(dataType.equals("int"))
			return int_mpValidator();
		else
			throw new Exception("Invalid dataType specified.");
	}
	
	String[] str_mpValidator() throws Exception {
		String[] dataProp = new String[5];
		if (matchProp.length >= 2 && matchProp.length <= 5) {
			if (!cmd.matches("(?i)(startsWith|endsWith|contains|matches|equals)")) {
				throw new Exception("matchType is not valid.");
			} else {
				dataProp[1] = cmd;
			}
			if (!matchProp[2].strip().matches("(?i)(fold|no-fold)"))
				throw new Exception("matchCase is not a valid value.");
			else
				dataProp[2] = matchProp[2].strip().toLowerCase();
			switch (matchProp.length) {
			case 2:
				if(!cmd.equals("equals"))
					data = data.replaceAll("([\\W&&\\S])", "\\\\$1");
				break;
			case 3:
				if (!cmd.equals("equals")) {
					if(matchProp[1].strip().equalsIgnoreCase("str")) {
						data = data.replaceAll("([\\W&&\\S])", "\\\\$1");
					}
				} else if (dataType.equalsIgnoreCase("regx")) {
						throw new Exception("Equals option can not have regx datatype.");
				}
				break;
			case 4:
				if (!cmd.equals("equals")) {
					if(dataType.equals("str")) {
						data = data.replaceAll("([\\W&&\\S])", "\\\\$1");
					}
					if(cmd.equals("startswith") && !(matchProp[3].equals("^")||matchProp[3].isBlank())) {
						throw new Exception("leftTokenizer can't be defined with matchProperty startWith.");
					} else {
						dataProp[3] = matchProp[3];
					}
				} else if (dataType.equals("regx")) {
					throw new Exception("Equals option can not have regx datatype.");
				}
				break;
			case 5:
				if (!cmd.equals("equals")) {
					if(dataType.equals("str")) {
						data = data.replaceAll("([\\W&&\\S])", "\\\\$1");
					}
					if(cmd.equals("startswith") && !(matchProp[3].equals("^")||matchProp[3].isBlank())) {
						throw new Exception("leftTokenizer can't be defined with matchProperty startWith.");
					} else if(cmd.equals("endswith") && !(matchProp[4].equals("$")||matchProp[4].isBlank())) {
						throw new Exception("rightTokenizer can't be defined with matchProperty endsWith.");
					}
					else {
						dataProp[3] = matchProp[3];
						dataProp[4] = matchProp[4];
					}
				} else if (dataType.equalsIgnoreCase("regx")) {
					throw new Exception("Equals option can not have regx datatype.");
				}
				break;
			default:
				break;
			}
		} else
			throw new Exception("Please refer matchType syntax.");
		dataProp[0] = data;
		if(dataProp[1].equalsIgnoreCase("equals") && dataProp[2].equalsIgnoreCase("fold"))
			dataProp[0] = dataProp[0].toLowerCase();
		return dataProp;
	}
	
	String[] int_mpValidator() throws Exception {
		String[] dataProp = new String[5];
		return dataProp;
	}

}
