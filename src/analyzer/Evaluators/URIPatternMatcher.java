package analyzer.Evaluators;

import java.util.ArrayList;

public class URIPatternMatcher {
private static volatile URIPatternMatcher instance = null;
	
	private URIPatternMatcher() {
		
	}
	
	public static URIPatternMatcher getInstance() {
		if(instance == null) {
			synchronized (URIPatternMatcher.class) {
				instance = new URIPatternMatcher();
			}
		}
		return instance;
	}
	
	public boolean uri_matcher(String fieldValue, ArrayList<String> patternProp) throws Exception{
		String matchType = patternProp.get(1);
		switch(matchType) {
		case "liveexists":
			return ProdDupChecker.getInstance().getResult(fieldValue);
		default:
				throw new Exception("URI validation is currently only supported for Live Checking.\nPlease refer Analyzer Manual for more details.");
		}
	}
	
}
