package service.master;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;




public class Service {
	private volatile static Service instance;
	private HttpServer _server;
	private Thread threadDataMaintainer;
	
	private int maxSecondsAnchorTagDistance;
	private int maxSecondsNetworkDevice;
	private int maxSecondsTag;
	
	private List<common.AnchorTagDistance> listAnchorTagDistance;
	private List<common.Tag> listAvailableTags;
	private List<common.network.Device> listAvailableNetworkDevices;
	
	private Service(){}
	
	public static synchronized Service getInstance()
	{
		if(instance == null)
		{
			instance = new Service();
		}
		return instance;
	}
	
	public void initialize(common.network.DeviceService nds) throws Exception
	{
		listAnchorTagDistance = new ArrayList<common.AnchorTagDistance>();
		listAvailableTags = new ArrayList<common.Tag>();
		listAvailableNetworkDevices = new ArrayList<common.network.Device>();
		
		maxSecondsAnchorTagDistance = Integer.parseInt(nds.parameters.get("maxSecondsAnchorTagDistance"));
		maxSecondsNetworkDevice = Integer.parseInt(nds.parameters.get("maxSecondsNetworkDevice"));
		maxSecondsTag = Integer.parseInt(nds.parameters.get("maxSecondsTag"));
		
		int httpPort = Integer.parseInt(nds.parameters.get("httpPort"));
		
		startHttpServer(httpPort);
		
		DataMaintainer dm = new DataMaintainer();
		threadDataMaintainer = new Thread(dm, "DataMaintainer");
		threadDataMaintainer.start();
	}
	
	private void startHttpServer(int httpPort) throws Exception
	{
		_server = HttpServer.create(new InetSocketAddress(httpPort), 0);
		_server.createContext("/", new MyHttpHandler());
		_server.setExecutor(null); 
		_server.start();
	}
	
	public synchronized void addAnchorTagDistance(common.AnchorTagDistance newAtd)
	{
		boolean found = false;
		
		for(common.AnchorTagDistance atd : listAnchorTagDistance)
		{
			if(atd.anchorId.equals(newAtd.anchorId) && atd.tagId.equals(newAtd.tagId))
			{
				atd.distance = newAtd.distance;
				atd.ts = new Date();
				found = true;
				common.Util.addToLog(common.LogType.INFO, "updated: "+newAtd.anchorId+"<->"+newAtd.tagId+":"+newAtd.distance);
				break;
			}
		}
		
		if(!found)
		{
			common.Util.addToLog(common.LogType.INFO, "added: "+newAtd.anchorId+"<->"+newAtd.tagId+":"+newAtd.distance);
			listAnchorTagDistance.add(newAtd);
		}
		
		//update available tags
		boolean tagFound = false;
		
		for(common.Tag tag : listAvailableTags)
		{
			if(tag.id.equals(newAtd.tagId))
			{
				tagFound = true;
				tag.lastSeen = new Date();//update the last seen datetime
				break;
			}
		}
		
		if(!tagFound)
		{
			common.Tag tag = new common.Tag();
			tag.id = newAtd.tagId;
			tag.lastSeen = new Date();
			
			listAvailableTags.add(tag);
			common.Util.addToLog(common.LogType.INFO, "tag added: "+tag.id);
		}
	}
	
	//this method returns a COPY of the original list, so the thread doesn't lock the original one
	public synchronized List<common.AnchorTagDistance> getListAnchorTagDistance()
	{
		List<common.AnchorTagDistance> result = new ArrayList<common.AnchorTagDistance>();
		
		for(common.AnchorTagDistance atd : listAnchorTagDistance)
		{
			result.add(new common.AnchorTagDistance(atd));
		}
		return result;
	}
	
	//this method returns a COPY of the original list, so the thread doesn't lock the original one
	public synchronized List<common.Tag> getListAvailableTags()
	{
		List<common.Tag> result = new ArrayList<common.Tag>();
		
		for(common.Tag t : listAvailableTags)
		{
			result.add(new common.Tag(t));
		}
		return result;
	}
	
	public synchronized void purgeOldAnchorTagDistance()
	{
		ListIterator<common.AnchorTagDistance> iter = listAnchorTagDistance.listIterator();
		Date now = new Date();
		
		//remove anchor-tag distance messages older than the max time allowed
		while(iter.hasNext()){
			long diff = now.getTime() - iter.next().ts.getTime();
		    if((diff / 1000 % 60)>maxSecondsAnchorTagDistance)
		    {
		    	iter.remove();
		    }
		}
	}
	
	public synchronized void purgeOldTags()
	{
		ListIterator<common.Tag> iter = listAvailableTags.listIterator();
		Date now = new Date();
		
		//remove anchor-tag distance messages older than the max time allowed
		while(iter.hasNext()){
			common.Tag tag = iter.next();
			long diff = now.getTime() - tag.lastSeen.getTime();
		    if((diff / 1000 % 60)>maxSecondsTag)
		    {
		    	common.Util.addToLog(common.LogType.INFO, "tag deleted: "+tag.id);
		    	iter.remove();
		    }
		}
	}
	
	public synchronized void purgeOldNetworkDevices()
	{
		ListIterator<common.network.Device> iter = listAvailableNetworkDevices.listIterator();
		Date now = new Date();
		
		//remove anchor-tag distance messages older than the max time allowed
		while(iter.hasNext()){
			common.network.Device device = iter.next();
			long diff = now.getTime() - device.lastSeen.getTime();
		    if((diff / 1000 % 60)>maxSecondsNetworkDevice)
		    {
		    	common.Util.addToLog(common.LogType.INFO, "networkDevice deleted: "+device.id);
		    	iter.remove();
		    }
		}
	}
	
	public synchronized void updateNetworkDeviceLastSeen(String deviceId)
	{
		boolean found = false;
		
		for(common.network.Device device : listAvailableNetworkDevices)
		{
			if(device.id.equals(deviceId))
			{
				device.lastSeen = new Date();
				found = true;
				break;
			}
		}
		
		if(!found)
		{
			common.network.Device device = common.Config.getNetworkDeviceById(deviceId);
			device.lastSeen = new Date();
			
			listAvailableNetworkDevices.add(device);
			common.Util.addToLog(common.LogType.INFO, "networkDevice added: "+deviceId);
		}
	}
	
	public synchronized void updateListAvailableTags()
	{
		listAvailableTags = new ArrayList<common.Tag>();
		List<String> listAllTagIds = new ArrayList<String>();
		
		
		for(common.AnchorTagDistance atd : listAnchorTagDistance)
		{
			if(!listAllTagIds.contains(atd.tagId))
			{
				listAllTagIds.add(atd.tagId);
			}
		}
		
		for(String tagId:listAllTagIds)
		{
			int countAnchors = 0;
			for(common.AnchorTagDistance atd : listAnchorTagDistance)
			{
				if(atd.tagId.equals(tagId))
				{
					countAnchors++;
					if(countAnchors==3)
					{
						listAvailableTags.add(new common.Tag(tagId));
						break;
					}
				}
			}
		}
	}
	
	
	//Creates a new thread for each HTTP request
	static class MyHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        	HttpWorker worker = new HttpWorker(t);
        	Thread threadWorker = new Thread(worker);
        	threadWorker.start();
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
	
	@Override
	public void run() {
		OutputStream os = httpExchange.getResponseBody();
		try
		{
			String response = null;
			switch(httpExchange.getRequestURI().getPath())
			{
				case "/anchorTagDistance":
					response = handleAddAnchorTagDistance();
					break;
				case "/heartbeat":
					response = handleHeartbeat();
					break;
			}
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
		
	}
	
	//handles the HTTP request for adding a new Anchor-Tag distance message from one of the Anchors
	private String handleAddAnchorTagDistance() throws Exception
	{
		String result = null;
		
		Map<String,String> parameters = common.Util.splitQuery(httpExchange.getRequestURI().getQuery());
		
		switch(parameters.get("a"))
		{
			case "add":
				common.AnchorTagDistance atd = new common.AnchorTagDistance();
				atd.anchorId = parameters.get("anchorId");
				atd.tagId = parameters.get("tagId");
				atd.distance = Float.parseFloat(parameters.get("distance"));
				atd.ts = new Date();
				
				service.master.Service.getInstance().addAnchorTagDistance(atd);
				break;
		}
		
		
		result = "ok";
		
		return result;
	}
	
	private String handleHeartbeat() throws Exception
	{
		String result = null;
		
		Map<String,String> parameters = common.Util.splitQuery(httpExchange.getRequestURI().getQuery());
		
		switch(parameters.get("a"))
		{
			case "send":
				String deviceId = parameters.get("deviceId");
				service.master.Service.getInstance().updateNetworkDeviceLastSeen(deviceId);
				break;
		}
		
		result = "ok";
		
		return result;
	}
	
}


class DataMaintainer implements Runnable
{
	private int sleepSeconds;
	
	public DataMaintainer()
	{
		sleepSeconds = 1;;
	}
	
	@Override
	public void run() {
		while (true)
		{
			try
			{
				service.master.Service.getInstance().purgeOldAnchorTagDistance();
				service.master.Service.getInstance().purgeOldNetworkDevices();
				service.master.Service.getInstance().purgeOldTags();
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
				sleep();
			}
		}
	}
	
	private void sleep()
	{
		try {
			Thread.sleep(sleepSeconds*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}