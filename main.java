import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap; 
import java.util.Hashtable; 
import java.util.Map; 
import java.util.Iterator;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.TreeMap; 
import java.io.*;
public class main implements RPCFunctions {

	//Array that holds the chord ids for each machine
	static long [] IDS = {0L,429496729L,858993459L,1288490188L,1717986918L,2147483648L,2576980377L,3006477107L,3435973836L,3865470566L};
    static Hashtable <Long,Integer> ID_TO_INDEX;

    static TreeMap<Long, Integer> LIVE_NODE_ID_TO_PID; // need locking
	static boolean [] LIVE_NODES = {false,false,false,false,false,false,false,false,false,false}; // need locking
	static long[] LIVE_IDS; // need locking
	
	static Hashtable <String, String> KV; // need live_ids_locking
	
    static int port_num = 2001;
    static ConnectToOtherRPCs rpc_connect;
    static int PROCESS_NUM;

    static int left_replica = -1; // need to implement. Need live_ids_locking
    static int right_replica = -1; // need to implement. Need live_ids_locking

    static final Lock live_ids_lock = new ReentrantLock(); // lock for LIVE_IDS
    static final Lock live_node_id_to_pid_lock = new ReentrantLock(); // lock for LIVE_NODE_ID_TO_PID
    static final Lock replica_lock = new ReentrantLock(); // lock for replica integers
    static final Lock kv_lock = new ReentrantLock(); // lock for KV

    public main() {}

    // implement header functions in here for RPC
    
    public String get(String key)
    {
        // return found or not found in local machine and send it to proper person
        String value = null;
        kv_lock.lock();
        try{
        value = KV.get(key); }
        finally { kv_lock.unlock();}
		if (value != null) return "Found: " + value;
		return "Not found"; 
    }

    public String set(String key, String value)
    {
        // set/update key-value pair locally
        kv_lock.lock();
        try {
        KV.put(key, value); }
        finally { kv_lock.unlock();}
		return "SET OK";
    }

	public static void list_local(PrintStream output)
    {
        /*
        kv_lock.lock();
        try {
		Iterator it = KV.entrySet().iterator();
    	while (it.hasNext()) 
        {
        	Map.Entry pair = (Map.Entry)it.next();
       		output.println(pair.getKey());
            output.flush();
       		//it.remove(); // avoids a ConcurrentModificationException
    	}		
        output.println("END LIST");
        output.flush();
        }
        finally { kv_lock.unlock();}
        */
        kv_lock.lock();
        try
        {
            String[] keys = KV.keySet().toArray(new String[KV.size()]);
            Arrays.sort(keys);
            for(int i = 0; i < keys.length; i++)
            {
                output.println(keys[i]);
                output.flush();
            }
            output.println("END LIST");
            output.flush();
        }
        finally { kv_lock.unlock();}
	}

    public static void handle_one_line(String cmd, PrintStream output)
    {
        if(cmd.length() == 0)
            return;
        try
        {
            // find out which process handles this
            // create rpc connection
            // RPCFunction r = rpc_connect.getConnection(PROCESS_NUM);
            //TODO: handle replication	
            	
            // we are guaranteed that at least one of these try catch blive_ids_locks will execute its try completely 

			String input []  = cmd.split(" ");	
			if (input[0].equalsIgnoreCase("SET"))
            {
                boolean done = false;
				int pid = Executor.route(input[1]); 
                // System.err.println("PID: " + pid);
                try 
                {
					RPCFunctions r = rpc_connect.get_connection(pid);
                    String result = r.set(input[1], String.join(" ", Arrays.copyOfRange(input, 2, input.length)));
                    output.println(result);
                    output.flush();
                    done = true;
                }
                catch (Exception e) {}
                try 
                {
				    int lower_pid = Stabilizer.get_lower_entry(IDS[pid - 1]); // left_replica; // pid - 1 > 0 ? pid - 1 : LIVE_IDS.length; // need to recalculate
                    /// System.err.println("LOWER_ID: " + lower_pid);
					RPCFunctions r = rpc_connect.get_connection(lower_pid);
                    String result = r.set(input[1], String.join(" ", Arrays.copyOfRange(input, 2, input.length)));
                    if(!done)
                    {
                        output.println(result);
                        output.flush();
                        done = true;
                    }
                }
                catch (Exception e) {}
                try 
                {
				    int higher_pid = Stabilizer.get_higher_entry(IDS[pid - 1]); // right_replica; // pid + 1 > LIVE_IDS.length ? 0 : pid + 1; // need to recalculate
                    // System.err.println("HIGHER_ID: " + higher_pid);
					RPCFunctions r = rpc_connect.get_connection(higher_pid);
                    String result = r.set(input[1], String.join(" ", Arrays.copyOfRange(input, 2, input.length)));
                    if(!done)
                        output.println(result);
                        output.flush();
                }
                catch (Exception e) {}
			}
            else if (input[0].equalsIgnoreCase("GET")){
                boolean done = false;
                int pid = Executor.route(input[1]); 
                try 
                {
                    RPCFunctions r = rpc_connect.get_connection(pid);
					String result = r.get(input[1]);
                    if(!result.equals("Not found"))
                    {
                        done = true;
                        output.println(result);
                        output.flush();
                    }
                }
                catch (Exception e) {}
                try 
                {
                    int lower_pid = Stabilizer.get_lower_entry(IDS[pid - 1]); // left_replica; // pid - 1 > 0 ? pid - 1 : LIVE_IDS.length; // need to recalculate
                    RPCFunctions r = rpc_connect.get_connection(lower_pid);
					String result = r.get(input[1]);
                    if(!result.equals("Not found") && !done)
                    {
                        done = true;
                        output.println(result);
                        output.flush();
                    }
                }
                catch (Exception e) {}
                try 
                {
                    int higher_pid = Stabilizer.get_higher_entry(IDS[pid - 1]); // right_replica; // pid + 1 > LIVE_IDS.length ? 0 : pid + 1; // need to recalculate
                    RPCFunctions r = rpc_connect.get_connection(higher_pid);
					String result = r.get(input[1]);
                    if(!done)
                    {
                        done = true;
                        output.println(result);
                        output.flush();
                    }
                }
                catch (Exception e) {}
                if(!done)
                {
                    output.println("Not found");
                    output.flush();
                }
			}
            else if (input[0].equalsIgnoreCase("LIST_LOCAL"))
            {
                // NEED TO SORT LIST!
				try{ list_local(output);}
			    catch (Exception e){}
			
			}
            else if(input[0].equalsIgnoreCase("OWNERS")) 
            {
				int pid = Executor.route(input[1]);
				long id = IDS[pid-1]; 
							
				int higher = Stabilizer.get_higher_entry(id); 
				int lower = Stabilizer.get_lower_entry(id); 
			
				int [] owners = {lower, pid, higher};  	
				String check = null; 
				boolean found = false; 
				
				for (int i = 0; i < 3; i ++){
					try{
						RPCFunctions r = rpc_connect.get_connection(owners[i]); 
						check = r.get(input[1]); 
						if (!check.equals("Not found")){
							found  = true; 
							break;
						} 
		
					}catch(Exception ex){}
				}
			

				if (found){
					output.println(lower + " " + pid + " " + higher); 
				}else{
					output.println("No key found"); 
				}
                output.flush();
 
			}
            else if (input[0].equalsIgnoreCase("BATCH"))
            {
		        PrintStream out = System.out; 
		
		        try{ out = new PrintStream(new BufferedOutputStream(new FileOutputStream(input[2])));}
                catch(Exception e){ out = System.out;}

		        BufferedReader br = null; 
		        try
                {
			        br = new BufferedReader(new FileReader(input[1]));
			        String line = br.readLine();
    		        while (line != null) 
                    {
                        handle_one_line(line, out);
				        line = br.readLine();
			        }
		        }
                catch(Exception ex){}
		        finally 
                { 
                    try{ br.close();}
                    catch(Exception ex){}
		        }
			}
            else
				System.err.println("Operation not supported");
		}
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    public boolean heartbeat()
    {
        return true;
    }

    public void notify_failure(int failed_pid)
    {
	    LIVE_NODES[failed_pid - 1] = false;
		update_live(failed_pid, false); 
    }

    public void notify_connection(int connected_pid)
    {
        LIVE_NODES[connected_pid - 1] = true;
    	update_live(connected_pid, true); 
	}


	//fxn to update list of living nodes
	public static void update_live(int pid, boolean alive)
    {
        // System.err.println("Updating live nodes!");
		ArrayList<Long> ids = new ArrayList<Long>(); 

        live_node_id_to_pid_lock.lock();
        live_ids_lock.lock();
        try
        {
            LIVE_NODE_ID_TO_PID = new TreeMap<Long, Integer>();
		    for (int i = 0; i < 10; i++){
			    if(LIVE_NODES[i]){
				    ids.add(IDS[i]); 
                    LIVE_NODE_ID_TO_PID.put(IDS[i], i + 1);
			    }
		    }

		    LIVE_IDS = new long[ids.size()]; // this is not thread safe
		
		    for (int i = 0; i < ids.size();i ++){
			    LIVE_IDS[i] = ids.get(i); 
		    }
        }
        finally 
        { 
            live_ids_lock.unlock();
            live_node_id_to_pid_lock.unlock();
        }

        Stabilizer.check_to_update_left_and_right_replicas(pid, alive);
        Stabilizer.rebalance(pid, alive);
	}


	//function to initialize map between index and large number
	public static void init_map()
    {
		ID_TO_INDEX = new Hashtable<Long, Integer>();
		for (int i = 0; i < 10; i ++){
			ID_TO_INDEX.put(IDS[i], i + 1); 			
		}
	}
	public static void main(String [] args)
    {
		init_map();
		
	    LIVE_NODE_ID_TO_PID = new TreeMap<Long, Integer>();
        KV = new Hashtable <String,String>();
		try
        {
            PROCESS_NUM = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17));
        }
        catch (Exception e) {}
        // System.err.println("My PROCESS NUM IS: " + PROCESS_NUM + " and my Node ID is: " + IDS[PROCESS_NUM - 1]);
		
		LIVE_NODES[PROCESS_NUM - 1] = true;//make myself alive
        update_live(PROCESS_NUM, true); 
		// failure detector 
        Thread failure = new Thread(new FailureDetector(port_num, IDS, LIVE_NODES, PROCESS_NUM));
        failure.start();
        
        // need a thread to accept connections to our RPC functions
        Thread accept = new Thread(new AcceptRPCConnections(port_num));
        accept.start();

        Scanner scan = new Scanner(System.in);
        rpc_connect = new ConnectToOtherRPCs(port_num);
        while(true)
        {
            String cmd = scan.nextLine();
            handle_one_line(cmd, System.out);
        }
	}
}
