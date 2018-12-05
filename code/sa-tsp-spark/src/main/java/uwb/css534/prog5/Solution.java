package uwb.css534.prog5;

import java.util.Arrays;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.Serializable;
import java.text.DecimalFormat;
//import java.util.Comparator;

public class Solution implements Serializable {
	// Static members:
	private RandomGenerator _mt19937;
	private RandomDataGenerator _rng;

	// Members:
	private int[] _order;
	private double _distance;
	private Graph _network;

	// Constructors:
	public Solution() {
		_order = null;
		_distance = -1;
		_network = null;
		_mt19937 = new MersenneTwister(0x5d11141018463324L);
		_rng = new RandomDataGenerator(_mt19937);
	} // end Default Constructor

	public Solution(long seed) {
		this();
		_mt19937 = new MersenneTwister(seed);
		_rng = new RandomDataGenerator(_mt19937);
	} // end Constructor(long)

	public Solution(Solution other) {
		_order = Arrays.copyOf(other._order, other._order.length);
		_distance = other._distance;
		_network = new Graph(other._network);
		_mt19937 = other._mt19937;
		_rng = other._rng;
	} // end Copy Constructor

	// Class initiate functions:
	private void init(int[] order, double distance) {
		_order = Arrays.copyOf(order, order.length);
		_distance = distance;
	} // end method init(int[], double)

	public void init(int[] order, Graph network) {
		// delegate to private init with calculated trip length
		init(order, calculate_trip_length(order, network));
		_network = new Graph(network);
	} // end method init(int[], Graph)

	public void init(Graph network) {
		// delegate to init(int[], Graph) with random trip
		init(gen_random_trip(network), network);
	} // end method init(Graph)

	// Accessors:
	public int[] order() {
		return Arrays.copyOf(_order, _order.length);
	} // end method order

	public int length() {
		return _order.length;
	} // end method length

	public double energy() {
		return _distance;
	} // end method energy

	// Mutators:
	public void random_swap(int num_swaps) {
		for (int i = 0; i < num_swaps; i++) {
			int swap_1 = _rng.nextInt(0, _order.length - 1), swap_2 = _rng.nextInt(0, _order.length - 1),
					temp = _order[swap_1];
			_order[swap_1] = _order[swap_2];
			_order[swap_2] = temp;
		} // end for i

		_distance = calculate_trip_length(_order, _network);
	} // end method random_swap

	public void reseed(long seed) {
		_mt19937.setSeed(seed);
		_rng = new RandomDataGenerator(_mt19937);
	} // end method reseed

	// Internal utility functions:
	private double calculate_trip_length(int[] trip, Graph network) {
		double distance = network.distance_to_origin(trip[0]);

		for (int i = 1; i < trip.length; i++) {
			// distance between consecutive cities
			distance += network.get_distance(trip[i - 1], trip[i]);
		} // end for i

		return distance;
	} // end method calculate_trip_length

	private int[] gen_random_trip(Graph network) {
		int size = network.size();
		return _rng.nextPermutation(size, size);
	} // end method gen_random_trip

	// Relational Operators:
	public boolean equals(Solution other) {
		boolean out = true;

		for (int i = 0; i < _order.length; i++) {
			if (_order[i] != other._order[i]) {
				out = false;
				break;
			} // end if
		} // end for

		return out;
	} // end method equals

	public double GetDistance() {
		return _distance;
	}

	public Solution Reduce(Solution solution) {
		if (solution.GetDistance() < this.GetDistance())
			return solution;
		return this;
	}

	// Utility functions:
	@Override
	public String toString() {
		String out = "path: ";
		for (int i = 0; i < _order.length; i++) {
			out += _order[i];

			if (i + 1 < _order.length) {
				out += " -> ";
			} // end if
			else {
				out += " | distance: " + _distance;
			} // end else
		} // end for i

		return out;
	} // end method toString(void)

	public String toString(DecimalFormat format) {
		String out = "path: ";
		for (int i = 0; i < _order.length; i++) {
			out += _order[i];

			if (i + 1 < _order.length) {
				out += " -> ";
			} // end if
			else {
				out += " | distance: " + format.format(_distance);
			} // end else
		} // end for i

		return out;
	} // end method toString(DecimalFormat)
} // end class Solution
