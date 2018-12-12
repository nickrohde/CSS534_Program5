import java.io.*;                                           // FileReader
import org.apache.commons.math3.random.MersenneTwister;     // MersenneTwister
import org.apache.commons.math3.random.RandomGenerator;     // RandomGenrator for MersenneTwister
import org.apache.commons.math3.random.RandomDataGenerator; // RandomDataGenerator
import mpi.*;                                               // MPI, MPIException

public class Runner
{
    public static void main(String[] argv) throws MPIException
    {
        try
        {
            MPI.Init(argv); // initialize MPI

            run(argv);      // run our simulation

            MPI.Finalize(); // let MPI know we're done
        } // end try
        catch (Exception e)
        {
            e.printStackTrace();
        } // end catch
    } // end Main


    private static void run(String[] argv) throws MPIException
    {
        int n = Integer.parseInt(argv[0]);  // time to wait during annealing steps for the system to stabilize
        Graph g = make_graph(argv[1]);      // input graph file

        // each rank must receive a unique seed to ensure they all take a different path
        RandomGenerator mt19937 = new MersenneTwister(60 * MPI.COMM_WORLD.Rank());  // random engine
        RandomDataGenerator rng = new RandomDataGenerator(mt19937);                 // RNG used by annealing

        try
        {
            // instantiate our class
            SA_MPI SA = new SA_MPI(MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());
            long time = System.currentTimeMillis();

            // run the annealing
            Solution x = SA.simulated_annealing(g, n, rng);

            // gather our best solution
            Solution res = (Solution)Communicator.get_best_solution(x, MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());

            time = System.currentTimeMillis() - time;

            if (MPI.COMM_WORLD.Rank() == 0)
            {
                 System.out.println("Solution is:" + res);
                 System.out.println("Execution time: " + time + " ms.");
            } // end if
        } // end try
        catch(Exception e)
        {
            e.printStackTrace();
        } // end catch
    } // end method run


    private static double benchmark(String[] argv, int iterations) throws MPIException
    {
        int n = Integer.parseInt(argv[0]);                                                  // time to wait during annealing steps for the system to stabilize
        Graph g = make_graph(argv[1]);                                                      // input graph file

        RandomGenerator mt19937 = new MersenneTwister(60 * MPI.COMM_WORLD.Rank());          // random engine
        RandomDataGenerator rng = new RandomDataGenerator(mt19937);                         // RNG used by annealing
        double duration = 0L;

        for (int i = 0; i < iterations; i++)
        {
            try
            {
                SA_MPI SA = new SA_MPI(MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());
                long time = System.currentTimeMillis();

                Solution x = SA.simulated_annealing(g, n, rng);
                
                time = System.currentTimeMillis() - time;
                duration += ((double)time / iterations);

                Solution res = (Solution)Communicator.get_best_solution(x, MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());
            } // end try
            catch(Exception e)
            {
                e.printStackTrace();
            } // end catch
        } // end for

        return duration;

    } // end method benchmark



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
