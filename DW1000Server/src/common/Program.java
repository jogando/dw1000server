package common;

public class Program {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String configFilePath = null;
		String deviceId = null;
		
		for (String s: args) {
            String[] parts = s.split(":");
            switch(parts[0])
            {
	            case "configPath":
	            	configFilePath = parts[1];
	            	break;
	            case "deviceId":
	            	deviceId = parts[1];
	            	break;
            }

        }
		configFilePath = "C:\\Users\\juliano\\git\\dw1000server\\DW1000Server\\doc\\config.json";
		deviceId = "JULIANO";
		Program pr = new Program(configFilePath, deviceId);
	}
	
	public Program(String configFilePath, String deviceId)
	{
		try {
			Config.initialize(configFilePath, deviceId);
			
			common.network.Device currentDevice = Config.getCurrentNetworkDevice();
			
			for(common.network.DeviceService nds:currentDevice.listServices)
			{
				if(nds.type.equals("anchor"))
				{
					service.anchor.Service.getInstance().initialize(nds);
					common.Util.addToLog(LogType.INFO, "anchor: started");
				}
				else if(nds.type.equals("master"))
				{
					service.master.Service.getInstance().initialize(nds);
					common.Util.addToLog(LogType.INFO, "master: started");
				}
				else if(nds.type.equals("webApp"))
				{
					service.webApp.Service webApp = new service.webApp.Service();
					webApp.initialize(nds);
					common.Util.addToLog(LogType.INFO, "webApp: started");
				}
				else if(nds.type.equals("heartbeat"))
				{
					service.heartbeat.Service.getInstance().initialize(nds);
					common.Util.addToLog(LogType.INFO, "heartbeat: started");
				}
				
			}
		} catch (Exception e) {
			if(common.Config.debugMode)
				e.printStackTrace();
			else
				common.Util.addToLog(LogType.ERROR, e.getMessage());
		}
	}

}
