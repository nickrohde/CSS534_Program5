import java.io.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import java.lang.Math;
import mpi.*;

public class Runner
{
    public static void main(String[] argv)
    {
        MPI.Init(argv);                                               // initialize MPI
        MPI.COMM_WORLD.setErrhandler(MPI.ERRORS_RETURN);              // set error handler in case of exception

        int n = Integer.parseInt(argv[0]);                            // time to wait during annealing steps for the system to stabilize
        Graph g = make_graph(argv[1]);                                // input graph file

        RandomGenerator mt19937 = new MersenneTwister(60L);           // random engine
        RandomDataGenerator rng = new RandomDataGenerator(mt19937);   // RNG used by annealing

        try
        {
            SA_MPI SA = new SA_MPI(MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());
            long time = System.currentTimeMillis();

            Solution x = SA.simulated_annealing(g, n, rng);

            time = System.currentTimeMillis() - time;

            System.out.println("Solution is:" + x);
            System.out.println("Execution time: " + time + " ms.");
        } // end try
        catch(Exception e)
        {
            e.printStackTrace();
        } // end catch

        MPI.Finalize();                                               // let MPI know we're done
    } // end Main


    private static Graph make_graph(String file_name)
    {
        FileReader file;
        Graph g = null;

        try
        {
            file = new FileReader(file_name);
            g = new Graph(file);
            file.close();
        } // end try
        catch(IOException e)
        {
            e.printStackTrace();
        } // end catch

        return g;
    } // end method make_graph
} // end class SA