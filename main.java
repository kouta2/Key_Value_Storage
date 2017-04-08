import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.Map; 
import java.util.Iterator;
import java.util.Arrays;
public class main implements RPCFunctions {

	//Array that holds the chord ids for each machine
	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};
	static boolean [] LIVE_NODES = {false,false,false,false,false,false,false,false,false,false}; 
	static HashMap<Long,Integer> ID_TO_INDEX; 
	static long[] LIVE_IDS; 
	
	static HashMap <String, String> KV;			
	
    static RPCFunctions list_of_client_rpcs [] = new RPCFunctions[10]; // list of RPCs to communicate with other machines
    static int port_num = 2001;

    static int PROCESS_NUM;

    static int left_replica; // need to implement
    static int right_replica; // need to implement

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

	public static void list_local(){
		Iterator it = KV.entrySet().iterator();
    	while (it.hasNext()) {
        	Map.Entry pair = (Map.Entry)it.next();
       		System.out.println(pair.getKey());
        System.out.println("END LIST");
       		//it.remove(); // avoids a ConcurrentModificationException
    	}		
	}

    public boolean heartbeat()
    {
        return true;
    }

    public void notify_failure(int failed_pid)
    {
		System.out.println("Notified!");
	    LIVE_NODES[failed_pid - 1] = false;
		update_live(); 
    }

    public void notify_connection(int connected_pid)
    {
		System.out.println("Notified!"); 
        LIVE_NODES[connected_pid - 1] = true;
    	update_live(); 
	}


	//fxn to update list of living nodes
	public static void update_live(){
		System.out.println("Updating live nodes!"); 
		ArrayList<Long> ids = new ArrayList<Long>(); 

		for (int i = 0; i < 10; i++){
			if(LIVE_NODES[i]){
				ids.add(IDS[i]); 
			}
		}

		LIVE_IDS = new long[ids.size()]; 
		
		for (int i = 0; i < ids.size();i ++){
			LIVE_IDS[i] = ids.get(i); 
			System.out.println(LIVE_IDS[i]); 
		}

	}


	//function to initialize map between index and large number
	public static void init_map(){
		ID_TO_INDEX = new HashMap<Long, Integer>();
		for (int i = 0; i < 10; i ++){
			ID_TO_INDEX.put(IDS[i], i + 1); 			
		}
	}



	public static void main(String [] args)
    {
		init_map();
		
		KV = new HashMap <String,String>();
		try
        {
            PROCESS_NUM = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17));
        }
        catch (Exception e) {}
		
		LIVE_NODES[PROCESS_NUM - 1] = true;//make myself alive
        update_live(); 
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
            	
                // we are guaranteed that at least one of these try catch blocks will execute its try completely 

				String input []  = cmd.split(" ");	
				System.out.println(input[0]); 
				if (input[0].equalsIgnoreCase("SET")){
                    boolean done = false;
				    int pid = Executor.route(input[1]); 
                    try {
                	    System.out.println(pid); 
						RPCFunctions r = rpc_connect.get_connection(pid);
                        String result = r.set(input[1], String.join(" ", Arrays.copyOfRange(input, 2, input.length)));
                        System.out.println(result);
                        done = true;
                    }
                    catch (Exception e) {}
                    try {
				        int lower_pid = pid - 1 > 0 ? pid - 1 : LIVE_IDS.length; 
                	    System.out.println(lower_pid); 
						RPCFunctions r = rpc_connect.get_connection(lower_pid);
                        String result = r.set(input[1], String.join(" ", Arrays.copyOfRange(input, 2, input.length)));
                        if(!done)
                        {
                            System.out.println(result);
                            done = true;
                        }
                    }
                    catch (Exception e) {}
                    try {
				        int higher_pid = pid + 1 > LIVE_IDS.length ? 0 : pid + 1;
                	    System.out.println(higher_pid); 
						RPCFunctions r = rpc_connect.get_connection(higher_pid);
                        String result = r.set(input[1], String.join(" ", Arrays.copyOfRange(input, 2, input.length)));
                        if(!done)
                            System.out.println(result);
                    }
                    catch (Exception e) {}
				}



                else if (input[0].equalsIgnoreCase("GET")){
                    boolean done = false;
                    int pid = Executor.route(input[1]); 
                    try {
                        RPCFunctions r = rpc_connect.get_connection(pid);
					    String result = r.get(input[1]);
                        if(!result.equals("Not found"))
                        {
                            done = true;
                            System.out.println(result);
                        }
                    }
                    catch (Exception e) {}
                    try {
                        int lower_pid = pid - 1 > 0 ? pid - 1 : LIVE_IDS.length;
                        RPCFunctions r = rpc_connect.get_connection(lower_pid);
					    String result = r.get(input[1]);
                        if(!result.equals("Not found") && !done)
                        {
                            done = true;
                            System.out.println(result);
                        }
                    }
                    catch (Exception e) {}
                    try {
                        int higher_pid = pid + 1 > LIVE_IDS.length ? 0 : pid + 1;
                        RPCFunctions r = rpc_connect.get_connection(higher_pid);
					    String result = r.get(input[1]);
                        if(!done)
                        {
                            done = true;
                            System.out.println(result);
                        }
                    }
                    catch (Exception e) {}
                    if(!done)
                        System.out.println("Not found");
				}



                else if (input[0].equalsIgnoreCase("LIST_LOCAL")){
                    // NEED TO SORT LIST!
					try{
						list_local(); 
					}catch (Exception e){}
			
				}else{
					System.err.println("Operation not supported");
				}

			}
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
	}
}








