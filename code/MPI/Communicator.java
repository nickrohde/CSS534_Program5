import mpi.*;



public class Communicator
{
    private static final int MASTER = 0;    // rank assigned to be the master

    public static Object[] prepare_msg(Object msg)
    {
        // pack our message into an object array for MPI
        Object[] res = new Object[1];
        res[0] = msg;

        return res;
    } // end method prepare_msg


    public static void send_to(Object[] msg, int destination, int channel) throws MPIException
    {
        // ensure there is a message to send
        if (msg == null)
        {
            return;
        } // end if

        // send the message to the given rank using the given channel
        MPI.COMM_WORLD.Send(msg, 0, msg.length, MPI.OBJECT, destination, channel);
    } // end method send_to


    public static Object[] receive_from(int count, int source, int channel) throws MPIException
    {
        // prepare a receive buffer
        Object res[] = new Object[count];
        // wait for message to arrive
        MPI.COMM_WORLD.Recv(res, 0, count, MPI.OBJECT, source, channel);

        return res;
    } // end method receive_from


    public static Object get_best_solution(Object solution, int my_rank, int mpi_size) throws MPIException
    {
        // master is in charge of gathering solutions and broadcasting the best solution to all other ranks
        if(my_rank == MASTER)
        {
            Solution result = new Solution((Solution)solution);
            Object temp[];

            // receive solutions from other ranks
            for (int src = 1; src < mpi_size; src++)
            {
                temp = receive_from(1, src, 0);

                // check if this solution is better than what we have seen previously
                if (temp.length > 0 && ((Solution)temp[0]).energy() < result.energy())
                {
                    // replace old result
                    result = new Solution((Solution)temp[0]);
                } // end if
            } // end for

            // prepare our best solution for broadcast
            Object[] msg = prepare_msg(result);

            // send the best solution to every other rank
            for(int dest = 1; dest < mpi_size; dest++)
            {
                send_to(msg, dest, 1);
            } // end for

            // return the best solution
            return result;
        } // end if
        else // slaves send their solution to master and then wait for master to send the best solution
        {
            // send this ranks solution to the master
            send_to(prepare_msg(solution), MASTER, 0);

            // wait for master to send the best solution
            Object res[] = receive_from(1, MASTER, 1);

            return res[0];
        } // end else
    } // end method get_best_solution
} // end class Communicator
