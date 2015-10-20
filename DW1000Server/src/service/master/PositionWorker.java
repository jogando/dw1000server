package service.master;

import java.util.ArrayList;
import java.util.List;

//This class calculates the position of a TAG using as an input the Range Reports received from
// 3 different anchors.
public class PositionWorker {
	public static common.TagPosition getTagPositionByTagId(String tagId)
	{
		common.TagPosition result = null;
		
		List<common.Anchor> listAnchorsInRange = new ArrayList<common.Anchor>();
		List<Float> listDistances = new ArrayList<Float>();
		
		List<common.RangeReport> listRangeReports = service.master.Service.getInstance().getListRangeReport();
		List<common.RangeReport> listRangeReportsUsed = new ArrayList<common.RangeReport>();
		
		for(common.RangeReport rr : listRangeReports)
		{
			if(rr.tagId.equals(tagId))
			{
				listAnchorsInRange.add(common.Config.getAnchorById(rr.anchorId));
				listDistances.add(rr.distance);
				
				listRangeReportsUsed.add(rr);
				
				if(listAnchorsInRange.size() == 3)//we only need 3 anchors for trilaterating
				{
					break;
				}
			}
		}
		
		result = new common.TagPosition();
		
		result.tag = new common.Tag(tagId);
		result.listRangeReports = listRangeReportsUsed; 
		
		if(listAnchorsInRange.size() == 3)//we only need 3 anchors for trilaterating
		{
			common.Coordinate position = getCoordinates(
					listAnchorsInRange.get(0).coordinates, 
					listAnchorsInRange.get(1).coordinates, 
					listAnchorsInRange.get(2).coordinates, 
					listDistances.get(0), 
					listDistances.get(1), 
					listDistances.get(2));
			
			result.coordinates = position;
		}
		else// not enough anchors for trilaterating
		{
			result.coordinates = null;
			common.Util.addToLog(common.LogType.INFO, "Not enough anchors for trilaterating");
		}
		
		return result;
	}
	
	private static common.Coordinate getCoordinates(common.Coordinate a,common.Coordinate b,common.Coordinate c, float dA, float dB, float dC)
    {
		common.Coordinate result = new common.Coordinate();
        float W, Z, x, y, y2;

        W = dA * dA - dB * dB - a.x * a.x - a.y * a.y + b.x * b.x + b.y * b.y;
        Z = dB * dB - dC * dC - b.x * b.x - b.y * b.y + c.x * c.x + c.y * c.y;

        x = (W * (c.y - b.y) - Z * (b.y - a.y)) / (2 * ((b.x - a.x) * (c.y - b.y) - (c.x - b.x) * (b.y - a.y)));
        y = (W - 2 * x * (b.x - a.x)) / (2 * (b.y - a.y));
        y2 = (Z - 2 * x * (c.x - b.x)) / (2 * (c.y - b.y));

        y = (y + y2) / 2;
        result.y = y;
        result.x = x;

        return result;
    }
}
