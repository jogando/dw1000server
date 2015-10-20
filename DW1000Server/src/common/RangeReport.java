package common;

import java.util.Date;

public class RangeReport {
	
	public RangeReport(RangeReport atd)
	{
		anchorId = atd.anchorId;
		tagId = atd.tagId;
		distance = atd.distance;
		ts = atd.ts;
	}
	
	public RangeReport()
	{}
		
	public String anchorId;
	public String tagId;
	public float distance;
	public Date ts;
	
	
}
