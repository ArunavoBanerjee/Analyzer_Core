package analyzer.SourceAdaptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

	public ParseSIPCSV(String _csvPath, String multiValueSep) throws Exception {
		// TODO Auto-generated constructor stub
		File _csvFile = new File(_csvPath);
		_csvName = _csvFile.getName();
		this.multiValueSep = multiValueSep;
		cr = new CSVReader(new FileReader(_csvFile));
		header = cr.readNext();
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

	public boolean next() throws Exception {
		dataDict.clear();
		boolean nextExists = false;
		String[] row = null;
		if ((row = cr.readNext()) != null) {
			nextExists = true;
			for (int i = 0; i < row.length; i++) {
				if (row[i].isBlank())
					continue;
				String field_name = header[i].strip();
				String field_value = row[i].strip();
				deepKVPextract(field_name, field_value);
				if (!deepKVP.isEmpty()) {
					for(Map.Entry<String, String> entry : deepKVP.entrySet()) {
						if (!dataDict.containsKey(entry.getKey())) {
							HashSet<String> values = new HashSet<String>();
							values.add(entry.getValue().strip());
							dataDict.put(entry.getKey(), values);
						} else {
							dataDict.get(entry.getKey()).add(entry.getValue().strip());
						}
					}
				} else {
					if (!dataDict.containsKey(field_name)) {
						HashSet<String> values = new HashSet<String>();
						if (multiValueSep.isBlank()) {
							values.add(row[i].strip());
						} else {
							for (String eachValue : row[i].strip().split(multiValueSep))
								values.add(eachValue);
						}
						dataDict.put(field_name, values);
					} else {
						if (multiValueSep.isBlank()) {
							dataDict.get(field_name).add(row[i].strip());
						} else {
							for (String eachValue : row[i].strip().split(multiValueSep))
								dataDict.get(field_name).add(eachValue);
						}
					}
				}
			}
		}
		// System.out.println(dataDict);
		return nextExists;
	}

	HashMap<String, String> deepKVPextract(String field, String value) {
		deepKVP.clear();
		JsonObject jsonObject = parser.parse(value).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String extendedField = field + "@" + entry.getKey();
			String textContent = entry.getValue().getAsString();
			deepKVP.put(extendedField, textContent);
		}
		return deepKVP;
	}
}
