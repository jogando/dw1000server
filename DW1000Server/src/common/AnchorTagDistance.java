package common;

import java.util.Date;

public class AnchorTagDistance {
	
	public AnchorTagDistance(AnchorTagDistance atd)
	{
		anchorId = atd.anchorId;
		tagId = atd.tagId;
		distance = atd.distance;
		ts = atd.ts;
	}
	
	public AnchorTagDistance()
	{}
		
	public String anchorId;
	public String tagId;
	public float distance;
	public Date ts;
	
	
}
