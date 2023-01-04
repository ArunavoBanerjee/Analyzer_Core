package analyzer.Evaluators;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Normalizer.NDL_DataService_V3;

public class URIExistChecker {
	static volatile URIExistChecker instance = null;
	NDL_DataService_V3 NDLDS;
	JsonParser jsonParser = new JsonParser();
	
public static URIExistChecker getInstance() {
	synchronized (URIExistChecker.class) {
		if (instance == null)
			instance = new URIExistChecker();
	}
	return instance;
}
	
	private URIExistChecker() {
		
	}
	
	
	public boolean getResult(String input) throws Exception {
		try {
			if (!input.isBlank()) {
				URL obj = new URL(input);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				// optional default is GET
				con.setRequestMethod("GET");
				// add request header
				con.setRequestProperty("User-Agent", "Mozilla/5.0");
				int responseCode = con.getResponseCode();
				String responseName = con.getResponseMessage();
//				System.out.println(responseCode + " " + responseName);
				if (responseCode <= 299 || responseCode == 403) {
					return true;
				} else
					return false;
			} else
				throw new NullPointerException("inputValue is blank.");
		} catch(Exception e) {
//			System.out.println(query);
//			System.out.println("sr:" + serviceResponse);
			return false;
//			throw e;
		}
		
		
		
	}
	
	public static void main(String[] args) throws Exception{
		URIExistChecker newChecker = URIExistChecker.getInstance();
		System.out.println(newChecker.getResult("https://chroniclingamerica.loc.gov/lccn/2004260241/1909-08-02/ed-1/seq-3.pdf"));
	}
}
