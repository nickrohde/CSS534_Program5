package uwb.css534.prog5;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.spark.SparkEnv;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;


public class Annealing implements Function<Solution, Solution> {
    double heat = 0;
    int iterations = 0;
    int swaps = 1;
    Broadcast<Solution> best_broadcast;
    Broadcast<RandomDataGenerator[]> rand_broadcast;
    
    public Annealing(double heat, int iterations, int swaps, Broadcast<Solution> best_broadcast, Broadcast<RandomDataGenerator[]> rand_broadcast) {
        this.heat = heat;
        this.iterations = iterations;
        this.swaps = swaps;
        this.best_broadcast = best_broadcast;
        this.rand_broadcast = rand_broadcast;
    }

    public void UpdateHeat(double heat){
        this.heat = heat;
    }

    public void UpdateBroadcast(Broadcast<Solution> best_broadcast)
    {
        this.best_broadcast = best_broadcast;
    }

    public Solution call(Solution candidate) {
        Solution best = best_broadcast.value();
        int rand_index = Integer.parseInt(SparkEnv.get().executorId());
        RandomDataGenerator rng = rand_broadcast.value()[rand_index];
        for (int i = 0; i < iterations; i++) {
            // find a neighboring solution by swapping a number of random cities
            Solution new_solution = new Solution(candidate);
            new_solution.random_swap(swaps);

            // we will always accept a better solution
            if (new_solution.energy() < candidate.energy()) {
                candidate = new Solution(new_solution);

                // check if this is better than what we've ever seen
                if (candidate.energy() < best.energy()) {
                    best = new Solution(candidate);
                } // end if
            } // end if
            else {
                // check if we will accept this new solution
                if (acceptance_probability(candidate.energy(), new_solution.energy()) > rng.nextUniform(0.0, 1.0)) {
                    // new solution was accepted
                    candidate = new Solution(new_solution);
                } // end if
            } // end else
        }
        return best;
    }

    private double acceptance_probability(double previous_solution, double new_solution) {
        return (1 / (1 + Math.exp((new_solution - previous_solution) / heat)));
    } // end method acceptance_probability
}