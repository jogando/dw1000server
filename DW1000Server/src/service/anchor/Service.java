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
	
	public void initialize(String _portName) throws Exception
	{
		portName = _portName;
			
		common.network.Device masterDevice = common.Config.getMasterNetworkDevice();
		
		for(common.network.DeviceService nds : masterDevice.listServices)
		{
			if(nds.type.equals("master"))
			{
				masterUrl = "http://"+masterDevice.ip+":" + Integer.parseInt(nds.parameters.get("httpPort"));
				break;
			}
		}
		
		for(common.network.DeviceService nds : common.Config.getCurrentNetworkDevice().listServices)
		{
			if(nds.type.equals("anchor"))
			{
				anchorId = nds.parameters.get("id");
				break;
			}
		}
		
		serialPort.Dispatcher.getInstance().registerObserver(this);
	}
	
	@Override
	public void receiveData(String data) {
		// TODO Auto-generated method stub
		
		String[] parts = data.replace("[", "").replace("]", "").split(":");
		
		String url = masterUrl
				+"/addAnchorTagDistance?"
				+ "anchorId="+anchorId
				+ "&tagId="+parts[0]
				+ "&distance="+parts[1];
		
		helper.HttpRequest req = new helper.HttpRequest(url);
		req.sendAsync();
	}

	@Override
	public String getPortName() {
		// TODO Auto-generated method stub
		return portName;
	}

}
