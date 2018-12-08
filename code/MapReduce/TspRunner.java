import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;
import java.lang.Math;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.filecache.DistributedCache;

public class TspRunner {    
    public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.out.println("Wrong number of arguments specified.");
			System.out.println("Correct: <input> <output>");
			System.exit(-1);
		}

		JobConf conf = new JobConf(TspRunner.class);
		conf.setJobName("TspRunner");
		
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
		
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		DistributedCache.addFileToClassPath(new Path("/user/anjald_css534/jars/commons-math3-3.6.1.jar"), conf);
		
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		long startTime = System.currentTimeMillis();

		JobClient.runJob(conf);

		long totalTime = System.currentTimeMillis() - startTime;

		System.out.println("Execution Time: " + totalTime + "ms");
	}
	
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		private final static IntWritable one = new IntWritable(1);
		private static final double INITIAL_HEAT = 100;
    	private static final double MIN_HEAT = 0.001;
		private static final int NUM_SWAPS = 1;

		private RandomGenerator mt19937 = new MersenneTwister(60L);         // random engine
		private RandomDataGenerator rng = new RandomDataGenerator(mt19937);

		private static Solution simulated_annealing(Graph g, int n, RandomDataGenerator rng){
			double heat = INITIAL_HEAT;                 // entropy of the system
			int step = 2;                               // annealing step counter
			Solution x_best = new Solution();           // best solution ever seen
			x_best.init(g);
			Solution candidate = new Solution(x_best);  // current candidate solution

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
		}

		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			Graph g = new Graph(value.toString());
			Solution x = simulated_annealing(g, 100000, rng);
			output.collect(new Text(one.toString()), new Text(x.toString()));
		}
    }
	 
    public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		private Pattern p = Pattern.compile("(?<=distance: )\\d*.*");

		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			String found_best = values.next().toString();
			Matcher dist_match = p.matcher(found_best);
			boolean found = dist_match.find();
			float min = Float.parseFloat(dist_match.group(0));
			String temp = found_best;

			while(values.hasNext()){
				temp = values.next().toString();
				dist_match = p.matcher(temp);
				found = dist_match.find();
				if(Float.parseFloat(dist_match.group(0)) < min){
					min = Float.parseFloat(dist_match.group(0));
					found_best = temp;
				}
			}

			output.collect(new Text("Best Solution Found:"), new Text(found_best));
		}
	}
}
