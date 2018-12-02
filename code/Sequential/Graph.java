import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.text.DecimalFormat;

public class Graph
{
	// Class constants:
	private static final Coordinate_Pair _origin = new Coordinate_Pair(0.0, 0.0);


	// Members:
	private List<Coordinate_Pair> _city_locations;


	// Constructors:
	public Graph(FileReader file)
	{
		BufferedReader reader;
		_city_locations = new ArrayList<Coordinate_Pair>(36);

		try
		{
			reader = new BufferedReader(file);
			String line = reader.readLine();

			while(line != null)
			{
				String[] stuff = line.split(",");
				_city_locations.add(new Coordinate_Pair(Integer.parseInt(stuff[0]), Integer.parseInt(stuff[1])));
				line = reader.readLine();
			} // end while

			reader.close();
		} // end try
		catch (IOException e)
		{
			e.printStackTrace();
		} // end catch
	} // end Constructor(FileReader)

	public Graph(Graph other)
	{
		_city_locations = new ArrayList<Coordinate_Pair>(other.size());

		for (Coordinate_Pair pair : other._city_locations)
		{
			_city_locations.add(new Coordinate_Pair(pair));
		} // end for pair
	} // end Copy Constructor 


	// Accessors:
	public int size()
	{
		return _city_locations.size();
	} // end method size

	public double get_distance(int a, int b)
	{
		return Coordinate_Pair.distance(_city_locations.get(a), _city_locations.get(b));
	} // end method get_distance


	public double distance_to_origin(int a)
	{
		return Coordinate_Pair.distance(_origin, _city_locations.get(a));
	} // end method distance_to_origin

	// Utility Functions:
	@Override
	public String toString()
	{
		String out = "";

		for (int i = 0; i < _city_locations.size(); i++)
		{
			out += "[" + i + "]: " + _city_locations.get(i) + "\n";
		} // end for i

		return out;
	} // end method toString(void)

	public String toString(DecimalFormat format)
	{
		String out = "";

		for (int i = 0; i < _city_locations.size(); i++)
		{
			out += "[" + i + "]: " + _city_locations.get(i).toString(format) + "\n";
		} // end for i

		return out;
	} // end method toString(DecimalFormat)
} // end class Graph
