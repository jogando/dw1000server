package common;

public class Program {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String configFilePath = "/Users/jogando/dev/config/DW1000Server.json";
		for (String s: args) {
            String[] parts = s.split(":");
            if(parts[0].equals("configPath"))
            {
            	configFilePath = parts[1];
            }
        }
		Program pr = new Program(configFilePath);
	}
	
	public Program(String configFilePath)
	{
		try {
			Config.initialize(configFilePath);
			
			common.network.Device currentDevice = Config.getCurrentNetworkDevice();
			
			for(common.network.DeviceService nds:currentDevice.listServices)
			{
				if(nds.type.equals("anchor"))
				{
					service.anchor.Service.getInstance().initialize(nds.parameters.get("portName"));
					System.out.println("anchor: started");
				}
				else if(nds.type.equals("master"))
				{
					service.master.Service.getInstance().initialize();
					System.out.println("master: started");
				}
				else if(nds.type.equals("webApp"))
				{
					service.webApp.Service webApp = new service.webApp.Service();
					webApp.initialize(Integer.parseInt(nds.parameters.get("httpPort")), nds.parameters.get("webPath"));
					System.out.println("webApp: started");
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
