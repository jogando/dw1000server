package helper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpRequest {
	String _urlString;
	String _body;
	String _contentType;
	String _method;
	public HttpRequest(String url)
	{
		_urlString = url;
		_body = "";
		_method = "POST"; //default;
	}
	
	public String send() throws Exception
	{
		Worker worker = new Worker();
		return worker.sendRequest();
	}
	
	public void sendAsync()
	{
		Worker worker = new Worker();
		Thread thread = new Thread(worker);
		thread.start();
	}
	
	public void setMethod(String method)
	{
		_method = method;
	}
	
	public String getMethod()
	{
		return _method;
	}
	
	public void setContentType(String contentType)
	{
		_contentType = contentType;
	}
	
	public String getContentType()
	{
		return _contentType;
	}
	

	class Worker implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				sendRequest();
			} catch (Exception e) {
				if(common.Config.debugMode)
					e.printStackTrace();
				else
					common.Util.addToLog(common.LogType.ERROR, e.getMessage());
			}
		}
		
		public String sendRequest() throws Exception
		{
			String response = ""; 
	        URL url = null;
	        HttpURLConnection connection = null; 
	        try
	        {
	            //+ "&json="+URLEncoder.encode(gson.toJson(_message));
	            //url = new URL(config.HSCloudURL+"/vars.jsp");
	            url = new URL(_urlString);

	            connection = (HttpURLConnection)url.openConnection();
	            connection.setRequestMethod(_method);
	            connection.setDefaultUseCaches(false);
	            connection.setUseCaches(false);

	            if(_contentType != null)
	            {
	            	connection.setRequestProperty("Content-Type", _contentType);
	            }
	            connection.setRequestProperty("Content-Length", "" + 
	                   Integer.toString(_body.getBytes().length));
	            connection.setRequestProperty("Content-Language", "en-US");  

	            connection.setUseCaches (false);
	            connection.setDoInput(true);
	            connection.setDoOutput(true);

	            //Send request
	            DataOutputStream wr = new DataOutputStream (
	                connection.getOutputStream ());
	            wr.writeBytes (_body);
	            wr.flush ();
	            wr.close ();

	            //System.out.println("request was closed");

	            //Get Response    
	            InputStream is = connection.getInputStream();
	            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	            String line;
	            
	            while((line = rd.readLine()) != null) {
	            	response += line;
	            }

	            rd.close();

	        } 
	        catch (Exception e) 
	        {
	        	if(e.getMessage().equals("Connection refused"))
	        	{
	        		String message = e.getMessage()+ " on "+_urlString+". Is the Master service running?";
	        		common.Util.addToLog(common.LogType.ERROR, message);
	        	}
	        	else
	        	{
	        		common.Util.addToLog(common.LogType.ERROR, e.getMessage());
	        	}
				
	        } 
	        finally {
	            if(connection != null) {
	              connection.disconnect(); 
	            }
	        }
	        return response;
		}

		
	}
	

}
