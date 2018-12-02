import java.io.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import java.lang.Math;

public class SA
{
	private static final double INITIAL_HEAT = 100;
	private static final double MIN_HEAT = 0.001;
	private static final int NUM_SWAPS = 1;

	public static void main(String[] argv)
	{
		int n = Integer.parseInt(argv[0]);							// time to wait during annealing steps for the system to stabilize
		Graph g = make_graph(argv[1]);								// input graph file
		RandomGenerator mt19937 = new MersenneTwister(60L);			// random engine
		RandomDataGenerator rng = new RandomDataGenerator(mt19937);	// RNG used by annealing
		run(g, n, rng);
	} // end Main


	public static void run(Graph g, int n, RandomDataGenerator rng)
	{
		long start = System.currentTimeMillis();

		Solution x = simulated_annealing(g, n, rng);

		long end = System.currentTimeMillis();
		System.out.println("Best solution found:" + x);
		System.out.println("Elapsed time:" + (end - start) + " ms.");
	} // end method run


	public static Solution simulated_annealing(Graph g, int n, RandomDataGenerator rng)
	{
		double heat = INITIAL_HEAT;					// entropy of the system
		int step = 2; 								// annealing step counter
		Solution x_best = new Solution();   		// best solution ever seen
		x_best.init(g);
		Solution candidate = new Solution(x_best);	// current candidate solution

		// annealing stops once system cools down
		while (heat > MIN_HEAT)
		{
			// allow system to stabilize before cooling further
			for (int i = 0; i < n; i++)
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

			// update system entropy and annealing step counter
			heat = temperature(heat, step);
			step += 1;
		} // end while

		return x_best;
	} // end method simulated_annealing


	private static double temperature(double current_heat, int current_time)
	{
		return (current_heat / Math.log(current_time));
	}

	private static double acceptance_probability(double previous_solution, double new_solution, double current_heat)
	{
		return (1 / (1 + Math.exp((new_solution - previous_solution) / current_heat)));
	} // end method acceptance_probability

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