package service.webApp;


import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;



public class Service {
	private static Service instance;
	private Thread threadHttpServer;
	
	private String webPath;
	
	private Service()
	{
		
	}
	
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
		webPath = nds.parameters.get("webPath");
		
		HttpServer server = new HttpServer(Integer.parseInt(nds.parameters.get("httpPort")),nds.parameters.get("webPath"));
		threadHttpServer = new Thread(server, "WebApp - HTTPServer");
		threadHttpServer.start();
	}
	
}

class HttpServer implements Runnable
{
	private Server _server;
	private int httpPort;
	private String webPath;
	
	public HttpServer(int _httpPort, String _webPath)
	{
		httpPort = _httpPort;
		webPath = _webPath;
	}
	
	@Override
	public void run() {
		//For handling the file requests
		ResourceHandler resourceHandler= new ResourceHandler();
		resourceHandler.setResourceBase(webPath);
		
		//For handling other type of requests
		WebAppHttpHandler webHandler = new WebAppHttpHandler();
		
		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[] { webHandler, resourceHandler  });
		
		_server = new Server(httpPort);
		_server.setHandler(handlers);

		
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

