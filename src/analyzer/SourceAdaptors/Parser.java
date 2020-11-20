package analyzer.SourceAdaptors;

import java.util.HashMap;
import java.util.HashSet;

public abstract class Parser {

	public HashMap<String, HashSet<String>> dataDict = new HashMap<String, HashSet<String>>();
	public HashMap<String, byte[]> entryMap = new HashMap<String, byte[]>();
	
	abstract public boolean next() throws Exception;
	
}