package service.webApp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;


public class Service {
	private HttpServer server;
	private static Service instance;
	
	private String webPath;
	
	public Service()
	{
		
	}

	
	public void initialize(common.network.DeviceService nds) throws Exception
	{
		webPath = nds.parameters.get("webPath");

		HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(nds.parameters.get("httpPort"))), 0);
	    server.createContext("/", new MyHttpHandler());
	    server.setExecutor(null); // creates a default executor
	    server.start();
	}
	
	class MyHttpHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {		
			List<String> listFiles = new ArrayList<String>();
			listFiles.add("/index.html");
			
			Thread thread = null;
			
			if(listFiles.contains(t.getRequestURI().getPath()))
			{
				FileHttpWorker worker = new FileHttpWorker(t, webPath);
				thread = new Thread(worker);
			}
			else
			{
				HttpWorker worker = new HttpWorker(t);
				thread = new Thread(worker);
			}
			
			thread.start();
    	}
	}
	
}

class FileHttpWorker implements Runnable
{
	private String webPath;
	private HttpExchange httpExchange;
	public FileHttpWorker(HttpExchange t, String webPth)
	{
		webPath = webPth;
		httpExchange = t;
	}
	@Override
	public void run() {
		OutputStream os = httpExchange.getResponseBody();
		try
		{
			Headers h = httpExchange.getResponseHeaders();
			h.add("Content-Type", "text/html");
			
			File file = new File (webPath+httpExchange.getRequestURI().getPath());
			byte [] bytearray  = new byte [(int)file.length()];
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(bytearray, 0, bytearray.length);
			
			  // ok, we are ready to send the response.
			httpExchange.sendResponseHeaders(200, file.length());
			os.write(bytearray,0,bytearray.length);
			
		}
		catch(Exception e)
		{
			if(common.Config.debugMode)
				e.printStackTrace();
			else
				common.Util.addToLog(common.LogType.ERROR, e.getMessage());
		}
		finally
		{
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


class HttpWorker implements Runnable
{
	private HttpExchange httpExchange;
	public HttpWorker(HttpExchange t)
	{
		httpExchange = t;
	}
	
	public void run() 
	{
		OutputStream os = httpExchange.getResponseBody();
		try
		{
			String response = handleRequest();
			
			httpExchange.sendResponseHeaders(200, response.length()); 
			os.write(response.getBytes());
			os.flush();
		}
		catch(Exception e)
		{
			if(common.Config.debugMode)
				e.printStackTrace();
			else
				common.Util.addToLog(common.LogType.ERROR, e.getMessage());
		}
		finally
		{
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String handleRequest()  throws Exception
	{
		String result = "";
		
		switch(httpExchange.getRequestURI().getPath())
		{
			case "/tag":
				result = handleTag();
				break;
			case "/scene":
				result = handleScene();
				break;
		}
		
		return result;
	}
	
	
	private String handleTag() throws Exception
	{
		Gson gson = new Gson();
		
		String result = null;
		
		Map<String,String> parameters = common.Util.splitQuery(httpExchange.getRequestURI().getQuery());
		
		switch(parameters.get("a"))
		{
			case "listAllPositions":
				List<common.TagPosition> listAllPositions = new ArrayList<common.TagPosition>();
				List<common.Tag> availableTags = service.master.Service.getInstance().getListAvailableTags();
				for(common.Tag t:availableTags)
				{
					common.TagPosition tp = null;
					tp = service.master.PositionWorker.getTagPositionByTagId(t.id);
					
					if(tp !=null)
					{
						listAllPositions.add(tp);
					}
				}
				result = gson.toJson(listAllPositions);
				break;
			case "listAvailable":
				List<common.Tag> listAvailableTags = service.master.Service.getInstance().getListAvailableTags();
				result = gson.toJson(listAvailableTags);
				break;
		}
		
		
		return result;
	}
	
	
	
	private String handleScene() throws Exception
	{
		Gson gson = new Gson();
		String result = gson.toJson(common.Config.scene);
			
		return result;
	}

}