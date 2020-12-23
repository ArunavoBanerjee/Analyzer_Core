package analyzer.PatternLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import analyzer.CustomException.DataStructureException;

public class Data {
	private static volatile Data nullObject = null;
	public LinkedHashMap<ArrayList<String>, HashSet<String>> patternMap = new LinkedHashMap<ArrayList<String>, HashSet<String>>();
	public HashSet<String> dupSet = new HashSet<String>();
	public ArrayList<String> genericDefinition;

	private Data(ArrayList<String> patternproperties, Boolean emptyPattern) {
		if (!emptyPattern) {
			this.genericDefinition = patternproperties;
			this.patternMap.put(patternproperties, new HashSet<String>());
		}
	}
/**
 * Method to call the instance of data properties.
 * @param patternproperties
 * @return
 */
	public static Data getObject(ArrayList<String> patternproperties) {
		Boolean emptyPattern = true;
		for (String value : patternproperties)
			if (!value.isBlank()) {
				emptyPattern = false;
				break;
			}
		if (patternproperties.get(0).equals("item") && patternproperties.get(1).equals("exists"))
			return nullObject;
		else
			return new Data(patternproperties, emptyPattern);
	}

	public void loadConstPatterns(String data) throws Exception {
		patternMap.get(genericDefinition).add(data);
	}

	public void loadVariablePatterns(ArrayList<String> patternProperty, String data) {
		if (!patternMap.containsKey(patternProperty))
			patternMap.put(patternProperty, new HashSet<String>() {
				{
					add(data);
				}
			});
		else {
			patternMap.get(patternProperty).add(data);
		}

	}
}
