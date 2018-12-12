import java.lang.Math;          // sqrt, pow
import java.text.DecimalFormat; // DecimalFormat
import java.io.*;               // Serializable Interface

// Storage class for an x-y coordinate pair
public class Coordinate_Pair implements Serializable
{
    // Members:
    public double _x;
    public double _y;


    // Constructors:
    public Coordinate_Pair(double x, double y)
    {
        _x = x;
        _y = y;
    } // end Constructor(double, double)

    public Coordinate_Pair(Coordinate_Pair other)
    {
        this(other._x, other._y);
    } // end Copy Constructor


    // Utility Functions:
    public static double distance(Coordinate_Pair a, Coordinate_Pair b)
    {
        return Math.sqrt((Math.pow((b._x - a._x), 2.0)) + (Math.pow((b._y - a._y), 2.0)));
    } // end method distance

    @Override
    public String toString()
    {
        return "(" + _x + ", " + _y + ")";
    } // end method toString(void)

    public String toString(DecimalFormat format)
    {
        return "(" + format.format(_x) + ", " + format.format(_y) + ")";
    } // end method toString(DecimalFormat)
} // end class Coordinate_Pair
