package Normalizer;

import java.util.Iterator;
import org.jsoup.parser.Parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.net.URLEncoder;
import java.io.InputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NDL_DataService_V3 {
	private String serviceUrl;
	private String service;
	private Gson gson  = new GsonBuilder().disableHtmlEscaping().create();

	HashMap<String, Object> paramConfig = new HashMap<String, Object>();

	public NDL_DataService_V3(final String serviceUrl, final String service, HashMap<String, Object> config) {
		this.serviceUrl = serviceUrl;
		this.service = service;
		paramConfig = config;
	}

	public NDL_DataService_V3(final String serviceUrl, final String service) {
		this.serviceUrl = serviceUrl;
		this.service = service;
		gson = new GsonBuilder().disableHtmlEscaping().create();
	}

	public String getDataServiceResponse(ArrayList<String> input) {
		String query = createQuery(input);	
		HttpURLConnection connection = null;
		try {
			final URL url = new URL(String.valueOf(serviceUrl) + service);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			final byte[] postDataBytes = query.toString().getBytes("UTF-8");
			connection.setRequestProperty("content-type", "application/json");
			connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			connection.setDoOutput(true);
			connection.getOutputStream().write(postDataBytes);
			final InputStream is = connection.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			final StringBuffer response = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error In service point connection. There might be Network problem . \nConsult your network administrator to resolve the issue\n");
			return "";
		}
	}

	private String createQuery(ArrayList<String> input) {
		paramConfig.put("values", input);
		return gson.toJson(paramConfig);
	}

//	public static void main(final String[] args) throws Exception {
//		String showValue = "";
//		final NDL_DataService_V3 dsGetmeta = new NDL_DataService_V3("http://10.72.22.155:65/services/", "extractMetadataFromId");
//		final NDL_DataService_V3 dsNormText = new NDL_DataService_V3("http://10.72.22.155:65/services/v3/", "normalizeText");
//		ArrayList<String> input = new ArrayList<String>();
//		input.add("anything<6");
//		showValue = dsNormText.getResult(input).get(0);
//		final NDL_DataService_V3 dsGethier = new NDL_DataService_V3("http://10.72.22.155:65/services/", "getClassHierarchy");
//		final NDL_DataService_V3 dsNormDate = new NDL_DataService_V3("http://10.4.8.239:65/services/", "normalizeDate");
//		final NDL_DataService_V3 dsNormLanguage = new NDL_DataService_V3("http://10.72.22.155:65/services/", "normalizeLanguage");
//		System.out.println(showValue);
//	}
}
