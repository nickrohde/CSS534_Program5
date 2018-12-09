import mpi.*;



public class Communicator
{
    private static final int MASTER = 0;

    public static Object[] prepare_msg(Object msg)
    {
        Object[] res = new Object[1];
        res[0] = msg;

        return res;
    } // end method prepare_msg


    public static void send_to(Object[] msg, int destination, int channel) throws MPIException
    {
        if (msg == null)
        {
            return;
        } // end if
        
        MPI.COMM_WORLD.Send(msg, 0, msg.length, MPI.OBJECT, destination, channel);
    } // end method send_to


    public static Object[] receive_from(int count, int source, int channel) throws MPIException
    {
        Object res[] = new Object[count];
        MPI.COMM_WORLD.Recv(res, 0, count, MPI.OBJECT, source, channel);

        return res;
    } // end method receive_from


    public static Object get_best_solution(Object solution, int my_rank, int mpi_size) throws MPIException
    {
        if(my_rank == 0)
        {
            Solution result = new Solution((Solution)solution);
            Object temp[];

            for (int src = 1; src < mpi_size; src++)
            {
                temp = receive_from(1, src, 0);

                if (temp.length > 0 && ((Solution)temp[0]).energy() < result.energy())
                {
                    result = new Solution((Solution)temp[0]);
                } // end if
            } // end for

            Object[] msg = prepare_msg(result);

            for(int dest = 1; dest < mpi_size; dest++)
            {
                send_to(msg, dest, 1);
            } // end for

            return result;
        } // end if
        else
        {
            Object[] msg = prepare_msg(solution);

            send_to(msg, MASTER, 0);

            Object res[] = receive_from(1, MASTER, 1);

            return res[0];
        } // end else
    } // end method get_best_solution
} // end class Communicator
