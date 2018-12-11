/*

 	MASS Java Software License
	© 2012-2015 University of Washington

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	The following acknowledgment shall be used where appropriate in publications, presentations, etc.:      

	© 2012-2015 University of Washington. MASS was developed by Computing and Software Systems at University of 
	Washington Bothell.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.

*/

package edu.uwb.sa_mass;

import java.net.InetAddress;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import edu.uw.bothell.css.dsl.MASS.Place;

public class Annealing extends Place {

	// public static final int GET_HOSTNAME = 0;
	public static final int START_ANNEALING = 0;
	private RandomDataGenerator rng;
	//private Solution candidate;
	private int iterations = 0;

	/**
	 * This constructor will be called upon instantiation by MASS The Object
	 * supplied MAY be the same object supplied when Places was created
	 * 
	 * @param obj
	 */
	public Annealing(Object args) {
		RandomGenerator mt19937 = new MersenneTwister(60L * getIndex()[0]); // random engine
		rng = new RandomDataGenerator(mt19937); // RNG used by annealing
		// candidate = new Solution(60L * getIndex()[0]);
		// candidate.init((Graph) args[0]);
		// candidate = (Solution) initSolution;
		this.iterations = (Integer) args;
	}

	/**
	 * This method is called when "callAll" is invoked from the master node
	 */
	public Object callMethod(int method, Object o) {

		Arg_Helper args = (Arg_Helper) o;
		switch (method) {

		case START_ANNEALING:
			return runAnnealingProcess((Solution) args.args[0], (Double) args.args[1]);

		default:
			return new String("Unknown Method Number: " + method);

		}

	}

	/**
	 * Return a String identifying where this Place is actually located
	 * 
	 * @param o
	 * @return The hostname (as a String) where this Place is located
	 */
	public Object runAnnealingProcess(Solution candidate, double heat) {

		try {
			Solution best = candidate;
			for (int i = 0; i < this.iterations; i++) {
				// create a new solution from our current candidate
				Solution new_solution = new Solution(candidate);

				// find a neighboring solution by swapping a number of random cities
				new_solution.random_swap(1);

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
					if (acceptance_probability(candidate.energy(), new_solution.energy(), heat) > rng.nextUniform(0.0,
							1.0)) {
						// new solution was accepted
						candidate = new Solution(new_solution);
					} // end if
				} // end else
			} // end for i

			return (Solution) best;
		}

		catch (Exception e) {
			return "Error : " + e.getLocalizedMessage() + e.getStackTrace();
		}

	}

	private static double acceptance_probability(double previous_solution, double new_solution, double current_heat) {
		return (1 / (1 + Math.exp((new_solution - previous_solution) / current_heat)));
	} // end method acceptance_probability

}
