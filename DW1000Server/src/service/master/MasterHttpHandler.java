package service.master;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

public class MasterHttpHandler extends AbstractHandler {
	private Gson gson;
	public MasterHttpHandler()
	{
		gson = new Gson();
	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		
		String responseString = null;
		response.setContentType("application/json;charset=utf-8");
		baseRequest.setHandled(true);
		try
		{
			
			switch (target) {
				case "heartbeat":
					responseString = handleHeartbeat(request);
					break;
				case "rangeReport":
					responseString = handleAddRangeReport(request);
					break;
					
			}
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(gson.toJson(responseString));
		}
		catch(Exception e)
		{
			responseString = e.getMessage();
			if(common.Config.debugMode)
				e.printStackTrace();
			else
				common.Util.addToLog(common.LogType.ERROR, e.getMessage());
			
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(responseString);
		}
		
	}
	
	//handles the HTTP request for adding a new Anchor-Tag distance message from one of the Anchors
	private String handleAddRangeReport(HttpServletRequest request) throws Exception
	{
		String result = null;

		switch(request.getParameter("a"))
		{
			case "add":
				common.RangeReport rr = new common.RangeReport();
				rr.anchorId = request.getParameter("anchorId");
				rr.tagId = request.getParameter("tagId");
				rr.distance = Float.parseFloat(request.getParameter("distance"));
				rr.ts = new Date();
				
				service.master.Service.getInstance().addRangeReport(rr);
				break;
		}
		
		result = "ok";
		
		return result;
	}
	
	private String handleHeartbeat(HttpServletRequest request)
	{
		String result = null;
		
		switch(request.getParameter("a"))
		{
			case "send":
				String deviceId = request.getParameter("deviceId");
				service.master.Service.getInstance().updateNetworkDeviceLastSeen(deviceId);
				break;
		}
		
		return result;
	}

}
