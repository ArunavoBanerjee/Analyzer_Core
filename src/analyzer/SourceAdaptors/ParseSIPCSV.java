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

	String _csvName = "";
	String[] header = null;
	CSVReader cr = null;
	public ParseSIPCSV(String _csvPath) throws Exception {
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
		entryMap.clear();
		boolean nextExists = false;
		while(cr.readNext() != null) {
			nextExists = true;
			if(in_tarEntry != null) {
				String tarEntryName = in_tarEntry.getName();
//				System.out.println(dataReadPath+":" + tarEntryName);
				if (!(tarEntryName.contains(dataReadPath) && in_tarEntry.isFile()))
					continue;
				nextExists = true;
				if (root.isEmpty()) {
					int root_idx = tarEntryName.indexOf('/');
					if (root_idx != -1)
						root = tarEntryName.substring(0, root_idx);
					else
						root = tarEntryName;
				}
				String parent_entry = tarEntryName.replaceAll("/$", "").replaceAll("(.*)[/\\\\].*", "$1");
				if(parentDir.isBlank())
					parentDir = parent_entry;
				if (in_tarEntry.isFile()) {
					if (!parentDir.equals(parent_entry))
						break;
						byte[] content = new byte[(int) in_tarEntry.getSize()];
						int offset = 0;
						tis.read(content, offset, content.length - offset);
						tarEntryName = tarEntryName.replace(trimPath, "");
						entryMap.put(tarEntryName, content);
						if (tarEntryName.endsWith(".xml")) {
							String contentString = new String(content);
							ParseXMLtoDict.getSourceInfo(contentString, dataDict);
						} else if (tarEntryName.endsWith("handle")) {
							String handle = new String(content).strip();
							dataDict.put("Handle_ID", new HashSet<String>() {
								{
									add(handle);
								}
							});
						}
				}
			}
		}
		while ((in_tarEntry = tis.getNextTarEntry()) != null);
		//System.out.println(dataDict);
		return nextExists;
	}
}
