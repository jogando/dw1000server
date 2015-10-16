package service.anchor;

public class Service  implements serialPort.ISerialPortInterfaceObserver{
	public static Service instance;
	private String anchorId;
	private String masterUrl;
	private String portName;
	
	private Service()
	{}
	
	public static Service getInstance()
	{
		if(instance == null)
		{
			instance = new Service();
		}
		return instance;
	}
	
	public void initialize(common.network.DeviceService nds) throws Exception
	{
		//Serial Port Name in which the arduino is connected to the computer
		portName = nds.parameters.get("portName");
			
		common.network.Device masterDevice = common.Config.getMasterNetworkDevice();
		common.network.DeviceService masterService = masterDevice.getServicesByType("master").get(0);//there´s only 1 master service
		
		masterUrl = "http://"+masterDevice.ip+":" + Integer.parseInt(masterService.parameters.get("httpPort"));
		
		anchorId = nds.parameters.get("anchorId");;
		
		serialPort.Dispatcher.getInstance().registerObserver(this);
	}
	
	@Override
	public void receiveData(String data) {
		// TODO Auto-generated method stub
		
		String[] parts = data.replace("[", "").replace("]", "").split(":");
		
		String url = masterUrl
				+"/anchorTagDistance?a=add"
				+ "&anchorId="+anchorId
				+ "&tagId="+parts[0]
				+ "&distance="+parts[1];
		
		helper.HttpRequest req = new helper.HttpRequest(url);
		req.sendAsync();
		
		common.Util.addToLog(common.LogType.INFO, "distance to "+parts[0]+": "+parts[1]+"m");
	}

	@Override
	public String getPortName() {
		// TODO Auto-generated method stub
		return portName;
	}

}
