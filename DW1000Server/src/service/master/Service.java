package service.master;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;






public class Service {
	private volatile static Service instance;

	private Thread threadDataMaintainer;
	private Thread threadHttpServer;
	
	private int maxSecondsAnchorTagDistance = 3;
	private int maxSecondsNetworkDevice = 3;
	private int maxSecondsTag = 2;
	
	private List<common.RangeReport> listRangeReports;
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
		listRangeReports = new ArrayList<common.RangeReport>();
		listAvailableTags = new ArrayList<common.Tag>();
		listAvailableNetworkDevices = new ArrayList<common.network.Device>();

		DataMaintainer dm = new DataMaintainer();
		threadDataMaintainer = new Thread(dm, "DataMaintainer");
		threadDataMaintainer.start();
		
		HttpServer server = new HttpServer(Integer.parseInt(nds.parameters.get("httpPort")));
		threadHttpServer = new Thread(server, "Master-HTTPServer");
		threadHttpServer.start();
	}
	

	public synchronized void addRangeReport(common.RangeReport newRR)
	{
		boolean found = false;
		
		for(common.RangeReport rr : listRangeReports)
		{
			if(rr.anchorId.equals(newRR.anchorId) && rr.tagId.equals(newRR.tagId))
			{
				rr.distance = newRR.distance;
				rr.ts = new Date();
				found = true;
				common.Util.addToLog(common.LogType.INFO, "updated: "+newRR.anchorId+"<->"+newRR.tagId+":"+newRR.distance);
				break;
			}
		}
		
		if(!found)
		{
			common.Util.addToLog(common.LogType.INFO, "added: "+newRR.anchorId+"<->"+newRR.tagId+":"+newRR.distance);
			listRangeReports.add(newRR);
		}
		
		//update available tags
		boolean tagFound = false;
		
		for(common.Tag tag : listAvailableTags)
		{
			if(tag.id.equals(newRR.tagId))
			{
				tagFound = true;
				tag.lastSeen = new Date();//update the last seen datetime
				break;
			}
		}
		
		if(!tagFound)
		{
			common.Tag tag = new common.Tag();
			tag.id = newRR.tagId;
			tag.lastSeen = new Date();
			
			listAvailableTags.add(tag);
			common.Util.addToLog(common.LogType.INFO, "tag added: "+tag.id);
		}
	}
	
	//this method returns a COPY of the original list, so the thread doesn't lock the original one
	public synchronized List<common.RangeReport> getListRangeReport()
	{
		List<common.RangeReport> result = new ArrayList<common.RangeReport>();
		
		for(common.RangeReport atd : listRangeReports)
		{
			result.add(new common.RangeReport(atd));
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
	
	public synchronized void purgeOldRangeReports()
	{
		ListIterator<common.RangeReport> iter = listRangeReports.listIterator();
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
		
		
		for(common.RangeReport atd : listRangeReports)
		{
			if(!listAllTagIds.contains(atd.tagId))
			{
				listAllTagIds.add(atd.tagId);
			}
		}
		
		for(String tagId:listAllTagIds)
		{
			int countAnchors = 0;
			for(common.RangeReport atd : listRangeReports)
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
}

class HttpServer implements Runnable
{
	private Server _server;
	private int httpPort;
	
	public HttpServer(int _httpPort)
	{
		httpPort = _httpPort;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		_server = new Server(httpPort);
		_server.setHandler(new MasterHttpHandler());
		
		try {
			_server.start();
			_server.join();
		} catch (Exception e) {

			if(common.Config.debugMode)
				e.printStackTrace();
			else
				common.Util.addToLog(common.LogType.ERROR, e.getMessage());
			
		}
		
	}
	
}
