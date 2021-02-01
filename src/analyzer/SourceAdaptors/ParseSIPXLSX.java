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

/**
 * XLSX input data parser. WIP
 * @author ndl
 *
 */

public class ParseSIPXLSX extends Parser {

	String _csvName = "";
	String[] header = null;
	CSVReader cr = null;
	public ParseSIPXLSX(String _csvPath) throws Exception {
		// TODO Auto-generated constructor stub
		File _csvFile = new File(_csvPath);
		_csvName = _csvFile.getName();
		cr = new CSVReader(new FileReader(_csvFile));
		header = cr.readNext();
//		if(!testforheader(header))
//			throw new Exception("CSV File does not contain a header column.");
	}
	
	public String getSourceName() {
		return _csvName;
	}
	
	public boolean clean() throws Exception{
		cr.close();
		return true;
	}


	public boolean next() throws Exception {
		dataDict.clear();
		boolean nextExists = false;
		String [] row = null;
		while((row = cr.readNext()) != null) {
			nextExists = true;
			for(int i = 0; i < row.length; i++) {
				if(row[i].isBlank())
					continue;
				String field_name = header[i].strip();
				if(!dataDict.containsKey(field_name)) {
					HashSet<String> values = new HashSet<String>();
					for(String eachValue : row[i].strip().split(";"))
						values.add(eachValue);
					dataDict.put(field_name,values);
				} else {
					for(String eachValue : row[i].strip().split(";"))
						dataDict.get(field_name).add(eachValue);
				}
			}
		}
		//System.out.println(dataDict);
		return nextExists;
	}
	
	public ArrayList<String> loadKeys() throws Exception {
		ArrayList<String> keyMaster = new ArrayList<String>();
		return keyMaster;
	}
	
	
}
