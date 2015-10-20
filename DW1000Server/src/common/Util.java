package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

public class Util {

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
	
	public static String getCurrentTime()
	{
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public static void addToLog(LogType type, String message)
	{
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String logMessage = "";
		if(type == LogType.ERROR)
		{
			logMessage += "ERROR";
		}
		else if (type == LogType.INFO)
		{
			logMessage += "INFO";
		}
		
		logMessage +=":"+ getCurrentTime();
		
		logMessage +=":"+ stackTraceElements[stackTraceElements.length-1].getClassName();//get the name of the class that called the addToLog method
		
		
		logMessage +="\t\t"+ message;
		
		System.out.println(logMessage);
	}
	

}
