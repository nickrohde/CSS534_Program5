import java.io.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import java.lang.Math;
import mpi.*;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.util.FSTInputStream;

public class Runner
{
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

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


    private static Solution compare_solutions(Solution x, int my_rank, int mpi_size)
    {
        if(my_rank == 0)
        {
            Solution res = null;

            for (int src = 1; src < mpi_size; src++)
            {
                // get message length from source node
                int length[] = new int[1];
                MPI.COMM_WORLD.recv(length, 0, 1, MPI.INT, src, 0);

                // receive actual message
                byte msg[] = new byte[length[0]];
                MPI.COMM_WORLD.recv(msg, 0, 1, MPI.BYTE, src, 0);

                // add object to list
                Solution temp = (Solution)conf.asObject(msg);

                if (res == null || res.energy() > temp.energy())
                {
                    res = new Solution(temp);
                } // end if
            } // end for

            return res;
        } // end if
        else
        {
            byte msg = conf.asByteArray(x);
            int length[] = new int[1];
            length[0] = msg.length;
            MPI.COMM_WORLD.send(length, 0, 1, MPI.INT, 0, 0);
            MPI.COMM_WORLD.send(msg, 0, msg.length, MPI.BYTE, 0, 1);

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