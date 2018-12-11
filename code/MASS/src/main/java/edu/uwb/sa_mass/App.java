
package edu.uwb.sa_mass;

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import edu.uw.bothell.css.dsl.MASS.Agents;
import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.Places;
import edu.uw.bothell.css.dsl.MASS.logging.LogLevel;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomDataGenerator;

public class App {

	private static final String NODE_FILE = "nodes.xml";
    private static final double INITIAL_HEAT = 100;
    private static final double MIN_HEAT = 0.001;
	private static final int NUM_SWAPS = 1;
	
	@SuppressWarnings("unused")		// some unused variables left behind for easy debugging
	public static void main( String[] args ) {

		int n = Integer.parseInt(args[0]);                          // time to wait during annealing steps for the system to stabilize
		int num_nodes = Integer.parseInt(args[2]);                  // number of nodes
        Graph g = make_graph(args[1]);                              // input graph file

		int num_places = num_nodes;
		
		// remember starting time
		long startTime = new Date().getTime();
		
		// init MASS library
		MASS.setNodeFilePath( NODE_FILE );
		MASS.setLoggingLevel( LogLevel.ERROR );
		
		// start MASS
		MASS.getLogger().debug( "SA initializing MASS library..." );
		MASS.init();
		MASS.getLogger().debug( "MASS library initialized" );
		
		/* 
		 * Create all Places with the same number as the number of nodes
		 */
	
		MASS.getLogger().debug( "App creating Places..." );
		//Object[] arguments = new Object[] { g, n/num_nodes};
		
		Places places = new Places( 1, Annealing.class.getName(), (Object) new Integer(n/num_nodes), num_places); // creating places
		MASS.getLogger().debug( "Places created" );

		//Object array to store the arguments for callALL
		Object[] placeCallAllObjs = new Object[num_places];

		double heat = INITIAL_HEAT;
		int step = 2;

		//initalize the best solution
		Solution best = new Solution();
		best.init(g);

		while (heat > MIN_HEAT)
        {
			//store arguements for each place
			for(int i=0; i<placeCallAllObjs.length; i++){
				Object[] placeArgHelper = new Object[2]; 
				placeArgHelper[0] = (Object) best;
				placeArgHelper[1] = (Object) new Double(heat);
				placeCallAllObjs[i] = (Object) new Arg_Helper(placeArgHelper);
			}

			//Run and store the output of the annealing method for each place
			Object[] calledPlacesResults = (Object[]) places.callAll(Annealing.START_ANNEALING, placeCallAllObjs);

			double best_energy = ((Solution) calledPlacesResults[0]).energy();
			int best_index = 0;

			//Select the solution from with the minmum distance from all places
			for(int j=1; j<calledPlacesResults.length ; j++){
				if( ((Solution) calledPlacesResults[j]).energy() < best_energy ){
					best_index = j;
					best_energy = ((Solution) calledPlacesResults[j]).energy();
				}
			}

			//set the best solution obtained from above as the new best solution
			best = new Solution( (Solution)calledPlacesResults[best_index] );
		   
			// update system entropy and annealing step counter
            heat = temperature(heat, step);
            step += 1;
        } // end while

		/*
		// orderly shutdown
		MASS.getLogger().debug( "Quickstart instructs MASS library to finish operations..." );
		*/

		MASS.finish();
		MASS.getLogger().debug( "MASS library has stopped" );
		
		// calculate / display execution time
		long execTime = new Date().getTime() - startTime;

		System.out.println("Best Solution:" + best);
		System.out.println( "Execution time = " + execTime + " milliseconds" );
		
	 }

	 private static double temperature(double current_heat, int current_time)
	 {
		 return (current_heat / Math.log(current_time));
	 }

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
	 
}
