package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Config {
	public static String deviceId;
	//public List<Tag> listTags;
	public static List<Anchor> listAnchors;
	public static List<Scene> listScenes;
	
	public static boolean debugMode = true;
	
	private static List<common.network.Device> listNetworkDevices;
	
	private static String configPath;


	public static void initialize(String cnfPath) throws Exception
	{
		deviceId = Util.getHostname();
		configPath = cnfPath;
		loadValues();
		System.out.println("Config loading finished...");
	}
	
	private static void loadValues() throws Exception
	{
		String jsonString = Util.readTextFromFile(configPath);
		
		JsonParser parser = new JsonParser();
		JsonObject rootObj = parser.parse(jsonString).getAsJsonObject();
		
		loadNetworkDevices(rootObj.getAsJsonArray("networkDevices"));
		//loadTags(rootObj.getAsJsonArray("listTags"));
		loadAnchors();
		loadScenes(rootObj.getAsJsonArray("listScenes"));
	}
	
	private static void loadAnchors()
	{
		listAnchors = new ArrayList<Anchor>();

		for(common.network.Device device : listNetworkDevices)
		{
			for(common.network.DeviceService service : device.listServices)
			{
				if(service.type.equals("anchor"))
				{
					Anchor anchor = new Anchor();
					//anchor.arduinoSerialPort = service.parameters.get("portName");
					//anchor.uwbShortAddress = service.parameters.get("uwbShortAddress");
					anchor.id = service.parameters.get("id");
					anchor.coordinates = new Coordinate(
							Float.parseFloat(service.parameters.get("coordinateX")),
							Float.parseFloat(service.parameters.get("coordinateY")));
					
					listAnchors.add(anchor);
				}
			}
		}
	}
	
	private static void loadScenes(JsonArray scenes)
	{
		listScenes = new ArrayList<Scene>();
		Gson gson = new Gson();

		for(int i =0;i<scenes.size();i++)
		{
			JsonObject jsonScene = scenes.get(i).getAsJsonObject();
			
			Scene scene = gson.fromJson(jsonScene.toString(), Scene.class);
			
			listScenes.add(scene);
		}
		
	}
	
	private static void loadNetworkDevices(JsonArray networkDevices)
	{
		listNetworkDevices = new ArrayList<common.network.Device>();
		for(int i =0;i<networkDevices.size();i++)
		{
			JsonObject networkDevice = networkDevices.get(i).getAsJsonObject();
			
			common.network.Device device = new common.network.Device();
			device.id =networkDevice.get("id").getAsString();
			device.ip =networkDevice.get("ip").getAsString();
			device.listServices = getListServices(networkDevice.getAsJsonArray("listServices"));
			
			listNetworkDevices.add(device);
		}
	}
	
	private static List<common.network.DeviceService> getListServices(JsonArray services)
	{
		List<common.network.DeviceService> result = new ArrayList<common.network.DeviceService>();
		for(int i =0;i<services.size();i++)
		{
			JsonObject serviceObject = services.get(i).getAsJsonObject();
			
			common.network.DeviceService service = new common.network.DeviceService();
			service.type = serviceObject.get("type").getAsString();
			if(serviceObject.get("parameters") != null)
			{
				service.parameters = getServiceParameters(serviceObject.get("parameters").getAsJsonArray());
			}
			result.add(service);
		}
		return result;
	}

	
	private static HashMap<String, String> getServiceParameters(JsonArray serviceParameters)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		for(int i =0;i<serviceParameters.size();i++)
		{
			JsonObject jObject = serviceParameters.get(i).getAsJsonObject();
			Set<Map.Entry<String,JsonElement>> entrySet=jObject.entrySet();
			
			for(Map.Entry<String,JsonElement> entry:entrySet){
			    result.put(entry.getKey(),entry.getValue().getAsString());
			}
		}
		return result;
	}

	
	public static common.network.Device getMasterNetworkDevice()
	{
		common.network.Device result = null;
		
		for(common.network.Device device : listNetworkDevices)
		{
			for(common.network.DeviceService service : device.listServices)
			{
				if(service.type.equals("master"))
				{

					result = device;
					break;
					
				}
			}
		}
		
		return result;
	}
	
	public static Anchor getAnchorById(String id)
	{
		Anchor result = null;
		
		for(Anchor a : listAnchors)
		{
			if(a.id.equals(id))
			{
				result = a;
				break;
			}
		}
		
		return result;
	}
	
	public static common.network.Device getCurrentNetworkDevice()
	{
		common.network.Device result = null;
		
		for(common.network.Device device : listNetworkDevices)
		{
			if(device.id.equals(deviceId))
			{
				result = device;
				break;
			}
		}
		
		return result;
	}
	
	public static Scene getSceneById(String id)
	{
		Scene result = null;
		
		for(Scene t : listScenes)
		{
			if(t.id.equals(id))
			{
				result = t;
				result.listAnchors = listAnchors;
				break;
			}
		}
		
		return result;
	}
}