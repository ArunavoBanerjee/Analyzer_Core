package analyzer.SourceAdaptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import analyzer.Base.Splitter;

public class KVPExtraction {

	void KVPextractAll(String nodeNameNDL, String textContent, HashMap<String, HashSet<String>> dataDict) {
		JsonParser parser = new JsonParser();
		if (!Splitter.NDLSchemaInfo.containsKey(nodeNameNDL)) {
			try {
				Object obj = parser.parse(textContent);    // checking end of nesting
				if(obj instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) obj;
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					String __nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					textContent = entry.getValue().getAsString().replace("\\", "\\\\");
					KVPextractAll(__nodeNameNDL, textContent, dataDict);
				}
				} else if(obj instanceof JsonArray) {
					JsonArray jsonArr = (JsonArray) obj;
					for(JsonElement jel : jsonArr) {
						textContent = jel.toString();
						KVPextractAll(nodeNameNDL, textContent, dataDict);
				}
				} else if(obj instanceof JsonPrimitive){
					textContent = ((JsonPrimitive) obj).getAsString();
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
	void KVPextractKeys(String nodeNameNDL, String textContent, ArrayList<String> keyMaster) {
		JsonParser parser = new JsonParser();
		if (!Splitter.NDLSchemaInfo.containsKey(nodeNameNDL)) {
			try {
				Object obj = parser.parse(textContent);    // checking end of nesting
				if(obj instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) obj;
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					String __nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					textContent = entry.getValue().getAsString().replace("\\", "\\\\");
					KVPextractKeys(__nodeNameNDL, textContent, keyMaster);
				}
				} else if(obj instanceof JsonArray) {
					JsonArray jsonArr = (JsonArray) obj;
					for(JsonElement jel : jsonArr) {
						textContent = jel.toString();
						KVPextractKeys(nodeNameNDL, textContent, keyMaster);
				}
				} else if(obj instanceof JsonPrimitive){
					textContent = ((JsonPrimitive) obj).getAsString();
					if (!keyMaster.contains(nodeNameNDL))
						keyMaster.add(nodeNameNDL);
				}
			} catch (Exception e) {
				if (!keyMaster.contains(nodeNameNDL))
					keyMaster.add(nodeNameNDL);
			}
		} else {
			if (!keyMaster.contains(nodeNameNDL))
				keyMaster.add(nodeNameNDL);
		}
	}
	
}
