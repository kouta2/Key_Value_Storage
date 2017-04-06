import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

/*
 * attempts to connect to other processes to use their RPCs
 *
 */
public class ConnectToOtherRPCs implements Runnable
{
    
    boolean ip_connected_clients[] = {false, false, false, false, false, false, false, false, false, false};
    int port_num = 2001;
    String ip_addrs [] = {"172.22.146.231", "172.22.146.233", "172.22.146.235", "172.22.146.237", "172.22.146.239", "172.22.146.241", "172.22.146.243", "172.22.146.245", "172.22.146.247", "172.22.146.249"}; // list of ip addresses used for accepting clients and connecting to clients
    RPCFunctions list_of_client_rpcs [];
    public ConnectToOtherRPCs(int p_num, RPCFunctions[] client_rpcs, int process)
    {
        // ip_addrs = ArrayUtils.removeElement(ip_addrs, ip_addrs[process - 1]);
        port_num = p_num;
        list_of_client_rpcs = client_rpcs;
    }

    public void run()
    {
        // in the future, we should put this thread to sleep when there are no more clients to connect to and have the failure detector class wake us up when it detects a failure
        while(true)
        {
            for(int i = 0; i < ip_addrs.length; i++)
            {
                if(!ip_connected_clients[i])
                {
                    try
                    {
                        Registry client_sock = LocateRegistry.getRegistry(ip_addrs[i], port_num);
                        list_of_client_rpcs[i] = (RPCFunctions) client_sock.lookup("RPCFunctions");
                        ip_connected_clients[i] = true;
                    }
                    catch (Exception e)
                    {
                        // System.err.println("Client exception: " + e.toString());
                        // e.printStackTrace();
                    }
                }
            }
        }
    }
}
