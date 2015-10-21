package service.webApp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;

public class WebAppHttpHandler extends AbstractHandler  {
	public WebAppHttpHandler()
	{

	}
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		
		String responseString = null;
		
		try
		{
			switch(target)
			{
				case "/tag":
					responseString = handleTag(request);
					baseRequest.setHandled(true);
					break;
				case "/scene":
					responseString = handleScene(request);
					baseRequest.setHandled(true);
					break; 
			}
			
			if(baseRequest.isHandled() && responseString != null)
			{
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(responseString);
			}
			
			
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
	
	private String handleTag(HttpServletRequest request) throws Exception
	{
		Gson gson = new Gson();
		
		String result = null;
		

		switch(request.getParameter("a"))
		{
			case "listAllPositions":
				List<common.TagPosition> listAllPositions = new ArrayList<common.TagPosition>();
				List<common.Tag> availableTags = service.master.Service.getInstance().getListAvailableTags();
				for(common.Tag t:availableTags)
				{
					common.TagPosition tp = null;
					tp = service.master.PositionWorker.getTagPositionByTagId(t.id);
					
					if(tp !=null)
					{
						listAllPositions.add(tp);
					}
				}
				result = gson.toJson(listAllPositions);
				break;
			case "listAvailable":
				List<common.Tag> listAvailableTags = service.master.Service.getInstance().getListAvailableTags();
				result = gson.toJson(listAvailableTags);
				break;
		}
		
		
		return result;
	}
	
	
	
	private String handleScene(HttpServletRequest request) throws Exception
	{
		Gson gson = new Gson();
		String result = gson.toJson(common.Config.scene);
			
		return result;
	}
}
