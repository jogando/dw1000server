package common;

public class Coordinate {
	public float x;
	public float y;
	
	public Coordinate()
	{}
	
	public Coordinate(float _x, float _y)
	{
		x = _x;
		y = _y;
	}
	
	public Coordinate(Coordinate c)
	{
		x = c.x;
		y = c.y;
	}
}
