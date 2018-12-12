import java.io.*;                                           // Serializable Interface
import org.apache.commons.math3.random.MersenneTwister;     // MersenneTwister
import org.apache.commons.math3.random.RandomGenerator;     // RandomGenerator type for MersenneTwister
import org.apache.commons.math3.random.RandomDataGenerator; // RandomDataGenerator
import java.lang.Math;                                      // log, exp
import mpi.*;                                               // MPIException

public class SA_MPI
{
    // Class constants:
    private static final double DEFAULT_INITIAL_HEAT = 100; // default value for starting heat of the system
    private static final double DEFAULT_MIN_HEAT = 0.001;   // default value for stopping heat of the system
    private static final int DEFAULT_NUM_SWAPS = 1;         // default value for number of swaps to generate a neighbor solution

    // Member constants:
    private final double INITIAL_HEAT;                      // starting heat of the system
    private final double MIN_HEAT;                          // stopping heat of the system
    private final int NUM_SWAPS;                            // number of swaps to generate a neighbor solution
    private final int MY_RANK;                              // this node's rank
    private final int MPI_SIZE;                             // size of MPI cluster


    // Constructors:
    public SA_MPI(int rank, int mpi_size)
    {
        // delegate to the other constructor with default values
        this(rank, mpi_size, DEFAULT_INITIAL_HEAT, DEFAULT_MIN_HEAT, DEFAULT_NUM_SWAPS);
    } // end Constructor(int, int)


    public SA_MPI(int rank, int mpi_size, double initial_heat, double min_heat, int num_swaps)
    {
        MY_RANK = rank;
        MPI_SIZE = mpi_size;
        INITIAL_HEAT = initial_heat;
        MIN_HEAT = min_heat; 
        NUM_SWAPS = num_swaps;
    } // end Constructor(int, int, double, double, int)


    // Run function:
    public Solution simulated_annealing(Graph g, int n, RandomDataGenerator rng) throws MPIException
    {
        double heat = INITIAL_HEAT;                 // entropy of the system
        int step = 2;                               // annealing step counter used for temperature calculation
        Solution x_best = new Solution();           // best solution ever seen
        x_best.init(g);                             // create a random starting solution
        Solution candidate = new Solution(x_best);  // current candidate solution

        // annealing stops once system cools down
        while (heat > MIN_HEAT)
        {
            // allow system to stabilize before cooling further
            for (int i = 0; i < (n / MPI_SIZE); i++)
            {
                // create a new solution from our current candidate
                Solution new_solution = new Solution(candidate);

                // find a neighboring solution by swapping a number of random cities
                new_solution.random_swap(NUM_SWAPS);

                // we will always accept a better solution
                if (new_solution.energy() < candidate.energy())
                {
                    candidate = new Solution(new_solution);

                    // check if this is better than what we've ever seen
                    if (candidate.energy() < x_best.energy())
                    {
                        x_best = new Solution(candidate);                       
                    } // end if
                } // end if
                else
                {
                    // check if we will accept this new solution
                    if(acceptance_probability(candidate.energy(), new_solution.energy(), heat) > rng.nextUniform(0.0, 1.0))
                    {
                        // new solution was accepted
                        candidate = new Solution(new_solution);
                    } // end if
                } // end else
            } // end for i

            // get the best solution from the other MPI nodes
            candidate = (Solution)Communicator.get_best_solution(candidate, MY_RANK, MPI_SIZE);

            // update system entropy and annealing step counter
            heat = temperature(heat, step);
            step += 1;
        } // end while

        return x_best;
    } // end method simulated_annealing


    // Utility functions:
    private static double temperature(double current_heat, int current_time)
    {
        return (current_heat / Math.log(current_time));
    } // end method temperature


    private static double acceptance_probability(double previous_solution, double new_solution, double current_heat)
    {
        return (1 / (1 + Math.exp((new_solution - previous_solution) / current_heat)));
    } // end method acceptance_probability
} // end class SA
