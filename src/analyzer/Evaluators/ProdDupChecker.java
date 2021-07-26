package analyzer.Evaluators;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Normalizer.NDL_DataService_V3;

public class ProdDupChecker {
	static volatile ProdDupChecker instance = null;
	NDL_DataService_V3 NDLDS;
	JsonParser jsonParser = new JsonParser();
	
public static ProdDupChecker getInstance() {
	synchronized (ProdDupChecker.class) {
		if (instance == null)
			instance = new ProdDupChecker();
	}
	return instance;
}
	
	private ProdDupChecker() {
		HashMap<String,Object> config = new HashMap<String,Object>();
		config.put("type","uri");
		NDLDS = new NDL_DataService_V3("http://10.72.22.155:65/services/v3/", "ifExists", config);
	}
	
	
	public boolean getResult(String input) throws Exception {
		ArrayList<String> inputArr = new ArrayList<String>();
		inputArr.add(input);
		final String serviceResponse = NDLDS.getDataServiceResponse(inputArr);
		if (serviceResponse.isBlank()){
			return false;
		}
		try {
		final JsonObject jo = ((JsonObject) (jsonParser.parse(serviceResponse))).getAsJsonObject("response");
		JsonArray result = jo.getAsJsonArray("documents").get(0).getAsJsonObject().getAsJsonArray("ndli_id");
		if(result.size()==0)
			return false;
		else
			return true;
		} catch(Exception e) {
//			System.out.println(query);
//			System.out.println("sr:" + serviceResponse);
			throw e;
		}
	}
	
	public static void main(String[] args) throws Exception{
		ProdDupChecker newChecker = ProdDupChecker.getInstance();
		ArrayList<String> input = new ArrayList<String>();
		input.add("https://www.loc.gov/pictures/item/00649527/");
		System.out.println(newChecker.getResult("https://www.loc.gov/pictures/item/00649512/"));
	}
}
