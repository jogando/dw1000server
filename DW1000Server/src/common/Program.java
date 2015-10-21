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
		//configFilePath = "/Users/jogando/dev/config/DW1000Server.json";
		//deviceId = "xter346";
		Program pr = new Program(configFilePath, deviceId);
	}
	
	public Program(String configFilePath, String deviceId)
	{
		try {
			Config.initialize(configFilePath, deviceId);
			
			common.network.Device currentDevice = Config.getCurrentNetworkDevice();
			
			if(currentDevice == null)
			{
				common.Util.addToLog(common.LogType.ERROR, "The current device '"+deviceId+"' couldn't be found in the configuration file");
				return;
			}
			
			
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
					service.webApp.Service.getInstance().initialize(nds);
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
