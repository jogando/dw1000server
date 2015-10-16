package common.network;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Device {
	public String id;
	public String ip;
	public Date lastSeen;//tells us when was the device "last seen" at
	public List<DeviceService> listServices;
	
	public List<DeviceService> getServicesByType(String type)
	{
		List<DeviceService> result = null;
		
		for(DeviceService service:listServices)
		{
			if(service.type.equals(type))
			{
				if(result == null)
				{
					result = new ArrayList<DeviceService>();
				}
				result.add(service);
			}
		}
		
		return result;
	}
}
