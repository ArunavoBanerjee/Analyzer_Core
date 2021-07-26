package analyzer.Evaluator;

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
	
	
	
}
