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
		int num_nodes = Integer.parseInt(args[2]);                          // time to wait during annealing steps for the system to stabilize
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
		 * Create all Places (having dimensions of x, y, and z)
		 * ( the total number of Place objects that will be created is: x * y * z )
		 */
	
		MASS.getLogger().debug( "Quickstart creating Places..." );
		//Object[] arguments = new Object[] { g, n/num_nodes};
		
		Places places = new Places( 1, Annealing.class.getName(), (Object) new Integer(n/num_nodes), num_places); // creating places
		MASS.getLogger().debug( "Places created" );

		Object[] placeCallAllObjs = new Object[num_places];

		double heat = INITIAL_HEAT;
		int step = 2;

		Solution best = new Solution();
		best.init(g);

		while (heat > MIN_HEAT)
        {
			for(int i=0; i<placeCallAllObjs.length; i++){
				Object[] placeArgHelper = new Object[2]; 
				placeArgHelper[0] = (Object) best;
				placeArgHelper[1] = (Object) new Double(heat);
				placeCallAllObjs[i] = (Object) new Arg_Helper(placeArgHelper);
			}

			Object[] calledPlacesResults = (Object[]) places.callAll(Annealing.START_ANNEALING, placeCallAllObjs);

			double best_energy = ((Solution) calledPlacesResults[0]).energy();
			int best_index = 0;

			for(int j=1; j<calledPlacesResults.length ; j++){
				if( ((Solution) calledPlacesResults[j]).energy() < best_energy ){
					best_index = j;
					best_energy = ((Solution) calledPlacesResults[j]).energy();
				}
			}

			best = new Solution( (Solution)calledPlacesResults[best_index] );
            // update system entropy and annealing step counter
            heat = temperature(heat, step);
            step += 1;
        } // end while

		/*
		// instruct all places to return the hostnames of the machines on which they reside
		Object[] placeCallAllObjs = new Object[ x * y * z ];
		MASS.getLogger().debug( "Quickstart sending callAll to Places..." );
		Object[] calledPlacesResults = ( Object[] ) places.callAll( Matrix.GET_HOSTNAME, placeCallAllObjs );
		MASS.getLogger().debug( "Places callAll operation complete" );
		
		

		// create Agents (number of Agents = x * y in this case), in Places
		MASS.getLogger().debug( "Quickstart creating Agents..." );
		Agents agents = new Agents( 1, Nomad.class.getName(), null, places, x * y );
		MASS.getLogger().debug( "Agents created" );

		// instruct all Agents to return the hostnames of the machines on which they reside
		Object[] agentsCallAllObjs = new Object[ x * y ];
		MASS.getLogger().debug( "Quickstart sending callAll to Agents..." );
		Object[] calledAgentsResults = ( Object[] ) agents.callAll( Nomad.GET_HOSTNAME, agentsCallAllObjs );
		MASS.getLogger().debug( "Agents callAll operation complete" );
		
		// move all Agents across the Z dimension to cover all Places
		for (int i = 0; i < z; i ++) {
			
			// tell Agents to move
			MASS.getLogger().debug( "Quickstart instructs all Agents to migrate..." );
			agents.callAll(Nomad.MIGRATE);
			MASS.getLogger().debug( "Agent migration complete" );
			
			// sync all Agent status
			MASS.getLogger().debug( "Quickstart sending manageAll to Agents..." );
			agents.manageAll();
			MASS.getLogger().debug( "Agents manageAll operation complete" );
			
			// find out where they live now
			MASS.getLogger().debug( "Quickstart sending callAll to Agents..." );
			calledAgentsResults = ( Object[] ) agents.callAll( Nomad.GET_HOSTNAME, agentsCallAllObjs );
			MASS.getLogger().debug( "Agents callAll operation complete" );
			
		}
		
		// find out where all of the Agents wound up when all movements complete
		MASS.getLogger().debug( "Quickstart sending callAll to Agents to get final landing spot..." );
		calledAgentsResults = ( Object[] ) agents.callAll(Nomad.GET_HOSTNAME, agentsCallAllObjs );
		MASS.getLogger().debug( "Agents callAll operation complete" );
		
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
