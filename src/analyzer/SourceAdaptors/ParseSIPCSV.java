package analyzer.SourceAdaptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;

public class ParseSIPCSV extends Parser {

	String _csvName = "", multiValueSep = "";
	String[] header = null;
	CSVReader cr = null;
	JsonParser parser = new JsonParser();
	HashMap<String, String> deepKVP = new HashMap<String, String>();
	KVPExtraction kvp = null;

	public ParseSIPCSV(String _csvPath, String multiValueSep) throws Exception {
		// TODO Auto-generated constructor stub
		File _csvFile = new File(_csvPath);
		_csvName = _csvFile.getName();
		this.multiValueSep = multiValueSep;
		cr = new CSVReader(new FileReader(_csvFile));
		header = cr.readNext();
		kvp = new KVPExtraction();
//		if(!testforheader(header))
//			throw new Exception("CSV File does not contain a header column.");
	}

	public String getSourceName() {
		return _csvName;
	}

	public boolean clean() throws Exception {
		cr.close();
		return true;
	}
	
	public void loadKeys(ArrayList<String> keyMaster) throws Exception {
		String[] row = null;
		while ((row = cr.readNext()) != null) {
			for (int i = 0; i < row.length; i++) {
				if(row[i].isBlank())
					continue;
				String field_name = header[i].strip();
				keyMaster.add(field_name);
				HashSet<String> field_value_list = new HashSet<String>();
				if(multiValueSep.isBlank())
					field_value_list.add(row[i].strip());
				else
					for (String eachValue : row[i].strip().split(multiValueSep))
						field_value_list.add(eachValue);
				for(String field_value : field_value_list) {
					kvp.KVPextractKeys(field_name, field_value, keyMaster);
				}
			}
		}
	}

	public boolean next() throws Exception {
		dataDict.clear();
		boolean nextExists = false;
		String[] row = null;
		if ((row = cr.readNext()) != null) {
			nextExists = true;
			for (int i = 0; i < row.length; i++) {
				if (row[i].isBlank())
					continue;
				//System.out.println(row[i]);
				String field_name = header[i].strip();
				HashSet<String> field_value_list = new HashSet<String>();
				if(multiValueSep.isBlank())
					field_value_list.add(row[i].strip());
				else
					for (String eachValue : row[i].strip().split(multiValueSep))
						field_value_list.add(eachValue);			
				for(String field_value : field_value_list){
					kvp.KVPextractAll(field_name, field_value, dataDict);
			}
			}
		}
		// System.out.println(dataDict);
		return nextExists;
	}

	HashMap<String, String> deepKVPextract(String field, String value) {
		deepKVP.clear();
		//System.out.println(value);
		try {
		JsonObject jsonObject = parser.parse(value).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String extendedField = field + "@" + entry.getKey();
			String textContent = entry.getValue().getAsString();
			deepKVP.put(extendedField, textContent);
		}
		} catch (Exception e) {
			
		}
		return deepKVP;
	}
	void deepKVPextractKeys(String field, String value, ArrayList<String> deepKeys) {
		//System.out.println(value);
		try {
		JsonObject jsonObject = parser.parse(value).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String extendedField = field + "@" + entry.getKey();
			deepKeys.add(extendedField);
		}
		} catch (Exception e) {
			
		}
	}
}
