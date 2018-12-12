import java.util.List;          // List<T> for ArrayList
import java.util.ArrayList;     // ArrayList<T>
import java.io.*;               // Serializable Interface, FileReader, BufferedReader
import java.text.DecimalFormat; // Decimal Format

// Storage class for a set of points in a fully connected graph
public class Graph implements Serializable
{
    // Class constants:
    private static final Coordinate_Pair _origin = new Coordinate_Pair(0.0, 0.0);   // Origin location for calculation of path lengths


    // Members:
    private List<Coordinate_Pair> _city_locations;  // the graph coordinates


    // Constructors:
    public Graph(FileReader file)
    {
        BufferedReader reader;
        _city_locations = new ArrayList<Coordinate_Pair>(36);   // instantiate and reserve memory for 36 cities

        try
        {
            // read our file line by line
            reader = new BufferedReader(file);
            String line = reader.readLine();

            // read our file line by line
            while(line != null)
            {
                // coordinate stored in the format X,Y
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
        // reserve space for faster graph building
        _city_locations = new ArrayList<Coordinate_Pair>(other.size());

        // copy over the coordinates from the other graph
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
