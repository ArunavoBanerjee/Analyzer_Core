package analyzer.SourceAdaptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import analyzer.Base.Splitter;

public class SourceParserFactory {
	
	public String csvMultivalueSep = "";
	
	public SourceParserFactory() throws Exception {
		LoadDefaultNDLISchema();
	}
	
	void LoadDefaultNDLISchema() throws Exception {
		try {
			InputStream inputFile = null;
			if (Splitter.schemaPath.isBlank()) {
				String schemaType = Splitter.schemaFileUsage.toLowerCase();
				switch (schemaType) {
				case "general":
					inputFile = SourceParserFactory.class
							.getResourceAsStream("/analyzer/DefaultConfig/NDLIGeneralSchema.json");
					loadSchemaDetails(inputFile);
					break;
				case "none":
					break;
				default:
					throw new Exception("Invalid schema type mentioned.");
				}
			} else {
				inputFile = new FileInputStream(new File(Splitter.schemaPath));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void loadSchemaDetails(InputStream __schemaFile){
		JsonParser parser = new JsonParser();
		Object obj = parser.parse(new InputStreamReader(__schemaFile));
		JsonObject jsonObject = (JsonObject) obj;
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			HashMap<String, JsonElement> nodeProperties = new HashMap<String, JsonElement>();
			for (Map.Entry<String, JsonElement> fieldentry : entry.getValue().getAsJsonObject().entrySet()) {
				nodeProperties.put(fieldentry.getKey(), fieldentry.getValue());
			}
			Splitter.NDLSchemaInfo.put(entry.getKey(), nodeProperties);
		}
		
	}
	
	
	public Parser getParser(String sourcePath, String dataReadPath) throws Exception{
		if (sourcePath.endsWith(".tar.gz"))
			return new ParseSIPTar(sourcePath, dataReadPath);
		else if (new File(sourcePath).isDirectory())
			return new ParseSIPDir(sourcePath);
		else if (sourcePath.endsWith(".csv"))
			return new ParseSIPCSV(sourcePath, csvMultivalueSep);
		else if (sourcePath.endsWith(".xlsx"))
			return new ParseSIPXLSX(sourcePath, csvMultivalueSep);
		else
			return null;
			
	}

}
