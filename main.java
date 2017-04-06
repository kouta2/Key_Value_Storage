import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.util.Scanner;

public class main implements RPCFunctions {

    public main() {}


    // implement header functions in here for RPC
    
    public String get(int pid, String key)
    {
        // return found or not found in local machine and send it to proper person
        System.out.println("GET METHOD OF PROCESS ID: " + PROCESS_NUM);
        System.out.println("key is " + key);
        return null;
    }

    public String set(int pid, String key, String value)
    {
        // set/update key-value pair locally
        // let proper process know it was a success
        System.out.println("SET METHOD OF PROCESS ID: " + PROCESS_NUM);
        System.out.println("key is " + key + " and value is " + value);
        return null;
    }

	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};
 	static long ID = IDS[0];

    static RPCFunctions list_of_client_rpcs [] = new RPCFunctions[9]; // list of RPCs to communicate with other machines
    static int port_num = 2001;

    static int PROCESS_NUM;

	public static void main(String [] args)
    {
        try
        {
            PROCESS_NUM = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17));
        }
        catch (Exception e) {}

        // need a failure detector thread
            // Thread failure = new Thread(new FailureDetector());
            // failure.start();
        
        // need a stabilize thread
            // Thread stabilizer = new Thread(new Stabilizer());
            // stabilizer.start();

        // need a thread to try and connect to other processes
            // connect_to_other_rpcs
        Thread connect = new Thread(new ConnectToOtherRPCs(port_num, list_of_client_rpcs, PROCESS_NUM));
        connect.start();

        // need a thread to accept connections to our RPC functions
            // accept_client_connections();
        Thread accept = new Thread(new AcceptConnections(port_num));
        accept.start();

        // need a thread to listen to stdin and print to stdout
            // main thread
        Scanner scan = new Scanner(System.in);
        while(true)
        {
            String cmd = scan.nextLine();
            System.out.println("Locally commannd is: " + cmd);
            try
            {
                list_of_client_rpcs[1].get(1, cmd);
            }
            catch (Exception e) {}
        }
	}

}
