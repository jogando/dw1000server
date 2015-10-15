package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Util {

	//returns the Hostname of the device the program is running on
	public static String getHostname() throws Exception
	{
		String hostname = null;

	    InetAddress addr;
	    addr = InetAddress.getLocalHost();
	    hostname = addr.getHostName();

		return hostname;
	}
	

	//Splits the Query String of a HTTP REQUEST into a collection of parameters
	//Example: http://localhost:8000/test?param1=val1&param2=val2
	//Result: 
	//param1 = val1
	//param2 = val2
	public static Map<String, String> splitQuery(String str) throws Exception {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String[] pairs = str.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    return query_pairs;
	}
	
	//Returns the content of a text file as a string
	public static String readTextFromFile(String path) throws Exception
	{
		String everything = "";
		BufferedReader br = new BufferedReader(new FileReader(path));
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    everything = sb.toString();
		} finally {
		    br.close();
		}
		
		return everything;
	}
}
