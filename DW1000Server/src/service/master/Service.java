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
	
	private int maxSeconds;
	
	private List<common.AnchorTagDistance> listAnchorTagDistance;
	private List<common.Tag> listAvailableTags;
	
	private Service(){}
	
	public static synchronized Service getInstance()
	{
		if(instance == null)
		{
			instance = new Service();
		}
		return instance;
	}
	
	public void initialize() throws Exception
	{
		listAnchorTagDistance = new ArrayList<common.AnchorTagDistance>();
		listAvailableTags = new ArrayList<common.Tag>();
		
		Integer httpPort = null;//the port in which the HTTP server will be listening for requests
		
		common.network.Device currentDevice = common.Config.getCurrentNetworkDevice();
		
		for(common.network.DeviceService nds : currentDevice.listServices)
		{
			if(nds.type.equals("master"))
			{
				httpPort = Integer.parseInt(nds.parameters.get("httpPort"));
				break;
			}
		}
		
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
				System.out.println("updated: "+newAtd.anchorId+"<->"+newAtd.tagId+":"+newAtd.distance);
				break;
			}
		}
		
		if(!found)
		{
			System.out.println("added: "+newAtd.anchorId+"<->"+newAtd.tagId+":"+newAtd.distance);
			listAnchorTagDistance.add(newAtd);
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
	
	public synchronized void purgeOldData()
	{
		ListIterator<common.AnchorTagDistance> iter = listAnchorTagDistance.listIterator();
		Date now = new Date();
		
		//remove anchor-tag distance messages older than the max time allowded
		while(iter.hasNext()){
			long diff = now.getTime() - iter.next().ts.getTime();
		    if((diff / 1000 % 60)>maxSeconds)
		    {
		    	iter.remove();
		    }
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
				case "/addAnchorTagDistance":
					response = handleAddAnchorTagDistance();
					break;
			}
			httpExchange.sendResponseHeaders(200, response.length()); 
			os.write(response.getBytes());
			os.flush();
		}
		catch(Exception ex)
		{
			if(common.Config.debugMode)
				ex.printStackTrace();
			else
				System.out.println(ex.getMessage());
		}
		
	}
	
	//handles the HTTP request for adding a new Anchor-Tag distance message from one of the Anchors
	private String handleAddAnchorTagDistance() throws Exception
	{
		String result = null;
		
		Map<String,String> parameters = common.Util.splitQuery(httpExchange.getRequestURI().getQuery());
		
		common.AnchorTagDistance atd = new common.AnchorTagDistance();
		atd.anchorId = parameters.get("anchorId");
		atd.tagId = parameters.get("tagId");
		atd.distance = Float.parseFloat(parameters.get("distance"));
		atd.ts = new Date();
		
		service.master.Service.getInstance().addAnchorTagDistance(atd);
		
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
				service.master.Service.getInstance().purgeOldData();
				service.master.Service.getInstance().updateListAvailableTags();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
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