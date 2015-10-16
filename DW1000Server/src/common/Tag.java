package common;

import java.util.Date;

public class Tag {
	public Tag()
	{}
	
	public Tag(String _id)
	{id = _id;}
	
	public Tag(Tag t)
	{id = t.id;}
	
	public String id;
	public Date lastSeen;
	
}
