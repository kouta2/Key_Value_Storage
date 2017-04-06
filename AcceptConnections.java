import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/*
 * handles the client connections to us to use our RPC calls
 * 
 */
public class AcceptConnections implements Runnable
{
    int port_num;

    public AcceptConnections(int p_num)
    {
        port_num = p_num;
    }    

    public void run()
    {
        try
        {
            Registry registry = LocateRegistry.getRegistry(port_num);
            RPCFunctions stub = (RPCFunctions) UnicastRemoteObject.exportObject(new main(), 0);
            registry.bind("RPCFunctions", stub);
            System.err.println("Server ready");
        }
        catch (Exception e)
        {
            // System.err.println("Server exception: " + e.toString());
            // e.printStackTrace();
        }
    }
}
