package analyzer.SourceAdaptors;

import java.io.File;

public class SourceParserFactory {

	public Parser getParser(String sourcePath, String dataReadPath) throws Exception{
		if (sourcePath.endsWith(".tar.gz"))
			return new ParseSIPTar(sourcePath, dataReadPath);
		else if (new File(sourcePath).isDirectory())
			return new ParseSIPDir(sourcePath);
		else if (sourcePath.endsWith(".csv"))
			return new ParseSIPCSV(sourcePath);
		else
			return null;
			
	}

}
