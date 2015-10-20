package service.master;

public class DataMaintainer implements Runnable
{
	private int sleepSeconds;
	
	public DataMaintainer()
	{
		sleepSeconds = 1;;
	}
	
	@Override
	public void run() {
		while (true)
		{
			try
			{
				service.master.Service.getInstance().purgeOldAnchorTagDistance();
				service.master.Service.getInstance().purgeOldNetworkDevices();
				service.master.Service.getInstance().purgeOldTags();
			}
			catch(Exception e)
			{
				if(common.Config.debugMode)
					e.printStackTrace();
				else
					common.Util.addToLog(common.LogType.ERROR, e.getMessage());
			}
			finally
			{
				sleep();
			}
		}
	}
	
	private void sleep()
	{
		try {
			Thread.sleep(sleepSeconds*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}