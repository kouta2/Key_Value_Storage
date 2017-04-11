import java.util.*; 
import java.lang.Math.*;
import java.util.concurrent.ConcurrentHashMap;

public class Stabilizer {

    public static int get_lower_entry(long node_id)
    {
        main.live_node_id_to_pid_lock.lock();
        main.live_ids_lock.lock();
        
        TreeMap<Long, Integer> node_id_to_pid = main.LIVE_NODE_ID_TO_PID;
        long[] live_ids = main.LIVE_IDS;
        int pid = main.PROCESS_NUM;

        try
        {
            Map.Entry<Long, Integer> pair = node_id_to_pid.lowerEntry(node_id);
            if(pair == null)
            {
                Integer val = null;
                val = node_id_to_pid.get(live_ids[live_ids.length - 1]);
                if(val == main.ID_TO_INDEX.get(node_id))
                    return -1;
                else
                    return val;
            }
            else
                return pair.getValue();
        }
        finally 
        { 
            main.live_ids_lock.unlock();
            main.live_node_id_to_pid_lock.unlock();
        }
    }

    public static int get_higher_entry(long node_id)
    {
        main.live_node_id_to_pid_lock.lock();
        main.live_ids_lock.lock();

        TreeMap<Long, Integer> node_id_to_pid = main.LIVE_NODE_ID_TO_PID;
        long[] live_ids = main.LIVE_IDS;
        int pid = main.PROCESS_NUM;

        try
        {
            Map.Entry<Long, Integer> pair = node_id_to_pid.higherEntry(node_id);
            if(pair == null)
            {
                Integer val = null;
                val = node_id_to_pid.get(live_ids[0]);
                if(val == main.ID_TO_INDEX.get(node_id))
                    return -1;
                else
                    return val;
            }
            else
                return pair.getValue();
        }
        finally 
        { 
            main.live_ids_lock.unlock();
            main.live_node_id_to_pid_lock.unlock();
        }
    }

    public static void rebalance(int pid, boolean alive)
    {
        long[] IDS = main.IDS;
        int my_pid = main.PROCESS_NUM;

        long node_id = IDS[pid - 1];
        int left = get_lower_entry(node_id);
        int right = get_higher_entry(node_id);
        int right_right = -1;
        if(right != -1)
            right_right = get_higher_entry(IDS[right - 1]);

        if(my_pid == left || my_pid == right || my_pid == right_right)
        {
            main.replica_lock.lock();
            main.kv_lock.lock();
            try
            {
                int left_rep = main.left_replica;
                int right_rep = main.right_replica;
                ConcurrentHashMap<String, String> kv = main.KV;
                Iterator it = kv.entrySet().iterator();
                while(it.hasNext())
                {
                    Map.Entry pair = (Map.Entry)it.next();
                    String key = (String)pair.getKey();
                    String value = (String)pair.getValue();
                    int owner = Executor.route(key);
                    ConnectToOtherRPCs rpc_connect = new ConnectToOtherRPCs(main.port_num);
                    if(owner == my_pid)
                    {
                        main.kv_lock.unlock();
                        try
                        {
                            RPCFunctions r = rpc_connect.get_connection(left_rep);
                            r.set(key, value);
                        }
                        catch (Exception e) {}
                        try
                        {
                            RPCFunctions r = rpc_connect.get_connection(right_rep);
                            r.set(key, value);
                        }
                        catch (Exception e) {}
                        main.kv_lock.lock();
                    }
                    else if(owner == left_rep)
                    {
                        main.kv_lock.unlock();
                        try
                        {
                            RPCFunctions r = rpc_connect.get_connection(left_rep);
                            r.set(key, value);
                        }
                        catch (Exception e) {}
                        main.kv_lock.lock();
                    }
                    else if(owner == right_rep)
                    {
                        main.kv_lock.unlock();
                        try
                        {
                            RPCFunctions r = rpc_connect.get_connection(right_rep);
                            r.set(key, value);
                        }
                        catch (Exception e) {}
                        main.kv_lock.lock();
                    }
                    else
                        kv.remove(key);
                }
            }
            // catch (Exception e) {}
            finally 
            { 
                main.kv_lock.unlock();
                main.replica_lock.unlock();
            }
        }
    }

    private static boolean closer_left(int new_id, int old_id)
    {
        int pid = main.PROCESS_NUM;
        int min = Math.min(Math.min(new_id, old_id), pid);
        int max = Math.max(Math.max(new_id, old_id), pid);
        if(min != pid && max != pid)
            return min == new_id;
        else
            return Math.max(new_id, old_id) == new_id;
    }

    private static boolean closer_right(int new_id, int old_id)
    {
        int pid = main.PROCESS_NUM;
        int min = Math.min(Math.min(new_id, old_id), pid);
        int max = Math.max(Math.max(new_id, old_id), pid);
        if(min != pid && max != pid)
            return max == new_id;
        else
            return Math.min(new_id, old_id) == new_id;
    }

    public static void check_to_update_left_and_right_replicas(int new_pid, boolean alive)
    {
        if(new_pid == main.PROCESS_NUM)
            return;
        main.replica_lock.lock();
        try
        {
            if(alive)
            {
                if(main.left_replica == -1) // neither replica is set
                    main.left_replica = new_pid;
                else if(main.right_replica == -1) // left replica is set
                {
                    if(closer_left(new_pid, main.left_replica))
                    {
                        main.right_replica = main.left_replica;
                        main.left_replica = new_pid;
                    }
                    else
                        main.right_replica = new_pid;
                }
                else
                {
                    if(closer_left(new_pid, main.left_replica))
                        main.left_replica = new_pid;
                    else if(closer_right(new_pid, main.right_replica))
                        main.right_replica = new_pid;
                }
            }
            else
            {
                if(new_pid == main.left_replica)
                    main.left_replica = get_lower_entry(main.IDS[main.PROCESS_NUM - 1]);
                else if(new_pid == main.right_replica)
                    main.right_replica = get_higher_entry(main.IDS[main.PROCESS_NUM - 1]);
            }
        }
        finally { main.replica_lock.unlock();}
        // System.err.println("left: " + main.left_replica);
        // System.err.println("right: " + main.right_replica);
    }
}
