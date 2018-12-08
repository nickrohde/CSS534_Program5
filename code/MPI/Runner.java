import java.io.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import java.lang.Math;
import mpi.*;

public class Runner
{
    public static void main(String[] argv) throws MPIException
    {
        MPI.Init(argv);                                               // initialize MPI
        //MPI.COMM_WORLD.setErrhandler(MPI.ERRORS_RETURN);              // set error handler in case of exception

        int n = Integer.parseInt(argv[0]);                            // time to wait during annealing steps for the system to stabilize
        Graph g = make_graph(argv[1]);                                // input graph file

        RandomGenerator mt19937 = new MersenneTwister(60 * MPI.COMM_WORLD.Rank());           // random engine
        RandomDataGenerator rng = new RandomDataGenerator(mt19937);   // RNG used by annealing

        try
        {
            SA_MPI SA = new SA_MPI(MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());
            long time = System.currentTimeMillis();

            Solution x = SA.simulated_annealing(g, n, rng);
            
            time = System.currentTimeMillis() - time;

            //Solution res = compare_solutions(x, MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());
            Solution res = (Solution)Communicator.get_best_solution(x, MPI.COMM_WORLD.Rank(), MPI.COMM_WORLD.Size());

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

        MPI.Finalize();                                               // let MPI know we're done
    } // end Main


    private static Solution compare_solutions(Solution x, int my_rank, int mpi_size) throws MPIException
    {
        if(my_rank == 0)
        {
            Solution res = null;
            Solution temp[] = new Solution[1];

            for (int src = 1; src < mpi_size; src++)
            {
                // receive actual message
                MPI.COMM_WORLD.Recv(temp, 0, 1, MPI.OBJECT, src, 0);

                if (res == null || res.energy() > temp[0].energy())
                {
                    res = new Solution(temp[0]);
                } // end if
            } // end for

            return res;
        } // end if
        else
        {
            // prepare the message
            Solution msg[] = new Solution[1];
            msg[0] = new Solution(x);

            // send the message
            MPI.COMM_WORLD.Send(msg, 0, msg.length, MPI.OBJECT, 0, 0);

            return null;
        } // end else
    } // end method compare_solutions


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
