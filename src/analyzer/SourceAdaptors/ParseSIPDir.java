package analyzer.SourceAdaptors;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ParseSIPDir extends Parser {
	ArrayList<File> childList = new ArrayList<File>();
	String sourceName = "";
	ParseXMLtoDict toDict = new ParseXMLtoDict();
	
	public ParseSIPDir(String _SIPRoot) throws Exception {
		// TODO Auto-generated constructor stub
		File f_SIPRoot = new File(_SIPRoot);
		sourceName = f_SIPRoot.getName();
		traverse(f_SIPRoot);
	}

	public void traverse(File parent) throws Exception {
		for (File source : parent.listFiles()) {
			if (source.isDirectory()) {
				traverse(source);
			} else if (source.getName().contains(".xml")) {
				childList.add(source.getParentFile());
				return;
			}
		}
	}
	
	public String getSourceName() {
		return sourceName;
	}
	
	public boolean clean() {
		return true;
	}

	public boolean next() throws Exception {
		entryMap.clear();
		if (childList.isEmpty())
			return false;
		else {
			for (File sipContent : childList.remove(0).listFiles()) {
				String sipEntry = sipContent.getAbsolutePath();
				byte[] content = Files.readAllBytes(Paths.get(sipEntry));
				entryMap.put(sipEntry, content);
				if (sipContent.getName().endsWith(".xml")) {
					 toDict.getSourceInfo(sipContent, dataDict);
				} else if (sipContent.getName().equals("handle")) {
					BufferedReader br = new BufferedReader(new FileReader(sipContent));
					dataDict.put("Handle_ID", new HashSet<String>() {
						{
							add(br.readLine());
						}
					});
					br.close();
				}
			}
			return true;
		}
	}

	public ArrayList<String> loadKeys() throws Exception {
		ArrayList<String> keyMaster = new ArrayList<String>();
		return keyMaster;
	}
	
}
