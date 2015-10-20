package service.master;

//This class will handle the deletion of old data.
//For example, if a range report was received more than X seconds, it needs to be deleted since the
//TAG could be somewhere else... we would be showing a wrong position
public class DataMaintainer implements Runnable
{
	private int sleepSeconds;
	
	public DataMaintainer()
	{
		sleepSeconds = 1;
	}
	
	@Override
	public void run() {
		while (true)
		{
			try
			{
				service.master.Service.getInstance().purgeOldRangeReports();
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