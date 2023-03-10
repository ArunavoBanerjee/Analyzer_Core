package analyzer.SourceAdaptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Parser {

	public HashMap<String, HashSet<String>> dataDict = new HashMap<String, HashSet<String>>();
	public HashMap<String, byte[]> entryMap = new HashMap<String, byte[]>();
	abstract public void loadKeys(ArrayList<String> keyMaster) throws Exception;
	abstract public boolean next() throws Exception;
	abstract public String getSourceName() throws Exception;
	abstract public boolean clean() throws Exception;
	
}