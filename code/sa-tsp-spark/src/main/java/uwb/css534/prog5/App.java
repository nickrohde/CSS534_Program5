package uwb.css534.prog5;

import java.io.*;
import java.lang.Math;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.api.java.JavaRDD;

public class App {
	private static int NUM_PARTITIONS = 32;
	private static int MAX_EXECUTORS = 12;

	private static JavaSparkContext jsc;// = new JavaSparkContext();
	private static final double INITIAL_HEAT = 100;
	private static final double MIN_HEAT = 0.001;
	private static final int NUM_SWAPS = 1;

	public static void main(String[] argv) {
		int n = Integer.parseInt(argv[0]); // time to wait during annealing steps for the system to stabilize
		Graph g = make_graph(argv[1]); // input graph file
		NUM_PARTITIONS = Integer.parseInt(argv[2]);
		RandomGenerator mt19937 = new MersenneTwister(60L); // random engine
		RandomDataGenerator rng = new RandomDataGenerator(mt19937); // RNG used by annealing
		configurSpark(); // configer spark driver
		run(g, n, rng); // run simulated annealing algorithm
	} // end Main

	public static void configurSpark() {
		// configuer driver
		SparkConf conf = new SparkConf().setAppName("SA-TSP");

		// register Kryo classes
		conf.registerKryoClasses((Class<?>[]) Arrays
				.<Class<?>>asList(Solution.class, Coordinate_Pair.class, Graph.class, Solution.class, Annealing.class)
				.toArray());

		jsc = new JavaSparkContext(conf);
		jsc.setLogLevel("OFF");
	}

	public static void run(Graph g, int n, RandomDataGenerator rng) {
		long start = System.currentTimeMillis();

		Solution x = simulated_annealing(g, n, rng);

		long end = System.currentTimeMillis();
		System.out.println("Best solution found:" + x);
		System.out.println("Elapsed time:" + (end - start) + " ms.");
	} // end method run

	public static Solution simulated_annealing(Graph g, int n, RandomDataGenerator rng) {
		double heat = INITIAL_HEAT; // entropy of the system
		int step = 2; // annealing step counter
		Solution best = new Solution();
		best.init(g);
		Solution candidate = new Solution(best);

		// for broadcasting best found solution
		Broadcast<Solution> best_broadcast = jsc.broadcast(best); 

		RandomDataGenerator[] rngs = new RandomDataGenerator[MAX_EXECUTORS];
		rngs[0] = rng;
		for(int i=1; i< MAX_EXECUTORS; i++)
			rngs[i] = new RandomDataGenerator( new MersenneTwister(60L * i));
		

		// broadcast random generator once
		Broadcast<RandomDataGenerator[]> rng_broadcast = jsc.broadcast(rngs); 

		List<Solution> candidatesList = Collections.nCopies(NUM_PARTITIONS, candidate);
		JavaRDD<Solution> candidates = jsc.parallelize(candidatesList, NUM_PARTITIONS);

		Annealing annealingProcess = new Annealing(heat, n / NUM_PARTITIONS, NUM_SWAPS, best_broadcast, rng_broadcast);

		do { // allow system to stabilize before cooling further

			// start annealing process -> see Annealing.java
			candidates = candidates.map(annealingProcess).repartition(NUM_PARTITIONS);

			// log loop time
			long start = System.currentTimeMillis();

			// reduce to best solution
			Solution a_best = candidates.reduce((acc, next) -> acc.Reduce(next));
			if(best.GetDistance() > a_best.GetDistance()){
				annealingProcess.UpdateBroadcast(jsc.broadcast(a_best));
				best = a_best;
			}
			//System.out.println(candidates.collect());
			// log loop time
			long end = System.currentTimeMillis();
			// log best solution found (distance just)
			System.out.println("Best distance found:" + best.GetDistance() + ", At heat: "+heat);
			System.out.println("Elapsed time:" + (end - start) + " ms.");

			// update system entropy and annealing step counter
			heat = temperature(heat, step);
			step += 1;
			annealingProcess.UpdateHeat(heat);

		} while (heat > MIN_HEAT);

		return best;

	} // end method simulated_annealing

	private static double temperature(double current_heat, int current_time) {
		return (current_heat / Math.log(current_time));
	}

	private static Graph make_graph(String file_name) {
		FileReader file;
		Graph g = null;

		try {
			file = new FileReader(file_name);
			g = new Graph(file);
			file.close();
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		} // end catch

		return g;
	} // end method make_graph
} // end class SA