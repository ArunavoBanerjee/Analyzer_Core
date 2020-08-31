package analyzer.SourceAdaptors;

import java.io.File;
import java.io.StringReader;
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

public class ParseXMLtoDict {

	static void getSourceInfo(File sipItem, HashMap<String, HashSet<String>> dict) throws Exception {
		JsonParser parser = new JsonParser();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
		Document inputDoc = documentBuilder.parse(sipItem);
		Element root = inputDoc.getDocumentElement();
		String schema = root.getAttribute("schema");
		NodeList docNodes = root.getChildNodes();
		for (int i = 0; i < docNodes.getLength(); i++) {
			Node docNode = docNodes.item(i);
			if (docNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeNameNDL = formReadable(docNode, schema);
			String textContent = docNode.getTextContent().trim();
			try {
				JsonObject jsonObject = parser.parse(textContent).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					textContent = entry.getValue().getAsString();
				}
			} catch (Exception e) {

			}
			if (!dict.containsKey(nodeNameNDL)) {
				HashSet<String> values = new HashSet<String>();
				values.add(textContent);
				dict.put(nodeNameNDL, values);
			} else
				dict.get(nodeNameNDL).add(textContent);
		}
	}
	
	static void getSourceInfo(String itemContent, HashMap<String, HashSet<String>> dict) throws Exception {
		JsonParser parser = new JsonParser();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
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
			try {
				JsonObject jsonObject = parser.parse(textContent).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					textContent = entry.getValue().getAsString();
				}
			} catch (Exception e) {

			}
			if (!dict.containsKey(nodeNameNDL)) {
				HashSet<String> values = new HashSet<String>();
				values.add(textContent);
				dict.put(nodeNameNDL, values);
			} else
				dict.get(nodeNameNDL).add(textContent);
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
			}
		}

		read = (schema + "." + element + "." + qualifier);
		read = read.replaceAll("[.]$", "");

		return read;
	}
	
}
