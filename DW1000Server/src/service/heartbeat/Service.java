package service.heartbeat;

public class Service {
	private Thread threadWorker;
	private int sleepSeconds;
	private volatile static Service instance; 
	
	private Service()
	{}
	
	public static synchronized Service getInstance()
	{
		if(instance == null)
        {
            instance = new Service();
        }
        return instance;
	}
	
	public void initialize(common.network.DeviceService nds)
	{
		common.network.Device masterDevice = common.Config.getMasterNetworkDevice();
		common.network.DeviceService masterService = masterDevice.getServicesByType("master").get(0);//there´s only 1 master service
		
		//example: http://192.168.0.2:8000/heartbeat?a=send&deviceId=deviceX
		String url = "http://"+masterDevice.ip+":" 
				+ Integer.parseInt(masterService.parameters.get("httpPort"))
				+ "/heartbeat?a=send"
				+ "&deviceId=" + common.Config.deviceId;
		
		sleepSeconds = Integer.parseInt(nds.parameters.get("sleepSeconds"));
		
		DaemonWorker worker = new DaemonWorker(url, sleepSeconds);
		threadWorker = new Thread(worker, "DaemonHeartbeat");
		threadWorker.start();
	}
	
}

class DaemonWorker implements Runnable
{
	private String url;
	private int sleepSeconds;
	public DaemonWorker(String _url, int _sleepSeconds)
	{
		url = _url;
		sleepSeconds = _sleepSeconds;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			try
			{
				helper.HttpRequest httpRequest = new helper.HttpRequest(url);
				httpRequest.sendAsync();
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
					Thread.sleep(sleepSeconds*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}