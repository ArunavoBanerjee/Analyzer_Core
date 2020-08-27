package analyzer.Validators;

import java.util.ArrayList;

public class DataValidator {
	String data;
	ArrayList<String> matchProp; 
	public String validateData(ArrayList<String> matchProp_in, String data_in) throws Exception{
		data = data_in;
		matchProp = matchProp_in;
		String dataType = matchProp_in.get(0);
		if (dataType.equalsIgnoreCase("str"))
			strValidator();
		else if (dataType.equalsIgnoreCase("regx"))
			strValidator();
		else if (dataType.equals("int"))
			intValidator();
		else if (dataType.equals("date"))
			dateValidator();
		else if (dataType.equals("json"))
			dateValidator();
		else if (dataType.equals("item"))
			throw new Exception("Item datatype cannot have data value filter.");
		else if (dataType.equals("coll"))
			throw new Exception("Collection datatype cannot have data value filter.");
		else
			throw new Exception("Invalid dataType specified.");
		return data;
	}
	
	void strValidator() throws Exception {
		if(matchProp.get(1).equals("equals") && matchProp.get(2).equals("fold"))
			data = data.toLowerCase();
		else if(matchProp.get(1).matches("contains|startswith|endswith"))
			data = data.replaceAll("([\\W&&\\S])", "\\\\$1");
	}
	
	void intValidator() throws Exception {
		
	}

	void dateValidator() throws Exception {
		
	}
}
