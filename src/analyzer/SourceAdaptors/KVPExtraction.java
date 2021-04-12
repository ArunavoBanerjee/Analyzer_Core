package analyzer.SourceAdaptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import analyzer.Base.Splitter;

public class KVPExtraction {

	void KVPextract(String nodeNameNDL, String textContent, HashMap<String, HashSet<String>> dataDict) throws Exception {
		JsonParser parser = new JsonParser();
		if (!Splitter.NDLSchemaInfo.containsKey(nodeNameNDL)) {
			try {
				Object obj = parser.parse(textContent);
				if(obj instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) obj;
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					textContent = entry.getValue().getAsString().replace("\\", "\\\\");
					KVPextract(nodeNameNDL, textContent, dataDict);
					if (!dataDict.containsKey(nodeNameNDL)) {
						HashSet<String> values = new HashSet<String>();
						values.add(textContent);
						dataDict.put(nodeNameNDL, values);
					} else
						dataDict.get(nodeNameNDL).add(textContent);
				}
				} else if(obj instanceof JsonArray) {
					JsonArray jsonArr = (JsonArray) obj;
					for(JsonArr)
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
	void KVPextractKeys(String nodeNameNDL, String textContent, ArrayList<String> keyMaster) {
		JsonParser parser = new JsonParser();
			try {
				Object obj = parser.parse(textContent);
				JsonObject jsonObject = (JsonObject) obj;
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					if(!keyMaster.contains(nodeNameNDL))
						keyMaster.add(nodeNameNDL);
				}
			} catch (Exception e) {
				
			}
	}
	
}
