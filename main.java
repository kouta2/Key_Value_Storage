import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap; 

public class main implements RPCFunctions {

	//Array that holds the chord ids for each machine
	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};
	static boolean [] LIVE_NODES = {false,false,false,false,false,false,false,false,false,false}; 
	static HashMap <String, String> KV;			
	
    static RPCFunctions list_of_client_rpcs [] = new RPCFunctions[10]; // list of RPCs to communicate with other machines
    static int port_num = 2001;

    static int PROCESS_NUM;

    public main() {}


    // implement header functions in here for RPC
    
    public String get(String key)
    {
        // return found or not found in local machine and send it to proper person
    	
	    String value = KV.get(key);
		if (value != null) return "Found: " + value;
		return "Not found"; 

    }

    public String set(String key, String value)
    {
        // set/update key-value pair locally
       	KV.put(key, value); 
		return "SET OK";
    }

    public boolean heartbeat()
    {
        return true;
    }


	public static void main(String [] args)
    {
		KV = new HashMap <String,String>();
        

		try
        {
            PROCESS_NUM = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17));
        }
        catch (Exception e) {}

        // failure detector 
        Thread failure = new Thread(new FailureDetector(port_num, IDS, LIVE_NODES, PROCESS_NUM));
        failure.start();
        
        // need a thread to accept connections to our RPC functions
        Thread accept = new Thread(new AcceptRPCConnections(port_num));
        accept.start();

        Scanner scan = new Scanner(System.in);
        ConnectToOtherRPCs rpc_connect = new ConnectToOtherRPCs(port_num);
        while(true)
        {
            String cmd = scan.nextLine();
            try
            {
                // find out which process handles this
                // create rpc connection
            	// RPCFunction r = rpc_connect.getConnection(PROCESS_NUM);
            	//TODO: handle replication	

			}
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
	}
}
