package analyzer.SourceAdaptors;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import analyzer.Base.Splitter;

public class ParseXMLtoDict {

	DocumentBuilderFactory dbf = null;
	DocumentBuilder documentBuilder = null;
	
	public ParseXMLtoDict() throws Exception {
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		documentBuilder = dbf.newDocumentBuilder();
	}
	
	void getSourceInfo(File sipItem, HashMap<String, HashSet<String>> dataDict) throws Exception {
		Document inputDoc = documentBuilder.parse(sipItem);
		Element root = inputDoc.getDocumentElement();
		String schema = root.getAttribute("schema");
		NodeList docNodes = root.getChildNodes();
		dataDict.clear();
		for (int i = 0; i < docNodes.getLength(); i++) {
			Node docNode = docNodes.item(i);
			if (docNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeNameNDL = formReadable(docNode, schema);
			String textContent = docNode.getTextContent().trim();
			KVPextract(nodeNameNDL, textContent, dataDict);
		}
	}
	
	void getSourceInfo(String itemContent, HashMap<String, HashSet<String>> dataDict) throws Exception {	
		Document inputDoc = documentBuilder.parse(new InputSource(new StringReader(itemContent)));
		Element root = inputDoc.getDocumentElement();
		String schema = root.getAttribute("schema");
		NodeList docNodes = root.getChildNodes();
		for (int i = 0; i < docNodes.getLength(); i++) {
			Node docNode = docNodes.item(i);
			if (docNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeNameNDL = formReadable(docNode, schema);
			String textContent = docNode.getTextContent().trim();
			KVPextract(nodeNameNDL, textContent, dataDict);
		}
	}
	
	void getSourceFields(String itemContent, ArrayList<String> keyMaster) throws Exception {
		Document inputDoc = documentBuilder.parse(new InputSource(new StringReader(itemContent)));
		Element root = inputDoc.getDocumentElement();
		String schema = root.getAttribute("schema");
		NodeList docNodes = root.getChildNodes();
		for (int i = 0; i < docNodes.getLength(); i++) {
			Node docNode = docNodes.item(i);
			if (docNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeNameNDL = formReadable(docNode, schema);
			if(Splitter.NDLSchemaInfo.containsKey(nodeNameNDL)) {
				if(!keyMaster.contains(nodeNameNDL))
					keyMaster.add(nodeNameNDL);
			} else {
				String textContent = docNode.getTextContent().trim();
			}
			}
			
	}

	static String formReadable(Node thisNode, String schema) {
		NamedNodeMap attrs = thisNode.getAttributes();
		String element = "", qualifier = "", read;
		for (int i = 0; i < attrs.getLength(); i++) {
			switch (attrs.item(i).getNodeName()) {
			case "element":
				element = attrs.getNamedItem("element").getNodeValue();
				break;
			case "qualifier":
				qualifier = attrs.getNamedItem("qualifier").getNodeValue();
				break;
			default:
				break;
			}
		}
		read = (schema + "." + element + "." + qualifier);
		read = read.replaceAll("[.]$", "");
		return read;
	}
	
	void KVPextract(String nodeNameNDL, String textContent, HashMap<String, HashSet<String>> dataDict) throws Exception {
		JsonParser parser = new JsonParser();
		if (!Splitter.NDLSchemaInfo.containsKey(nodeNameNDL)) {
			try {
				Object obj = parser.parse(textContent);
				JsonObject jsonObject = (JsonObject) obj;
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					textContent = entry.getValue().getAsString().replace("\\", "\\\\");
					if (!dataDict.containsKey(nodeNameNDL)) {
						HashSet<String> values = new HashSet<String>();
						values.add(textContent);
						dataDict.put(nodeNameNDL, values);
					} else
						dataDict.get(nodeNameNDL).add(textContent);
				}
			} catch (Exception e) {
				if (!dataDict.containsKey(nodeNameNDL)) {
					HashSet<String> values = new HashSet<String>();
					values.add(textContent);
					dataDict.put(nodeNameNDL, values);
				} else
					dataDict.get(nodeNameNDL).add(textContent);
			}
		} else {
			if (!dataDict.containsKey(nodeNameNDL)) {
				HashSet<String> values = new HashSet<String>();
				values.add(textContent);
				dataDict.put(nodeNameNDL, values);
			} else
				dataDict.get(nodeNameNDL).add(textContent);
		}
	}
}
