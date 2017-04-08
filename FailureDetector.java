import java.io.*;
import java.net.*;

public class FailureDetector extends Thread{

    int port_num;
    long network [];
    boolean alive [];
    int pid;

    public FailureDetector(int p_num, long[] IDS, boolean[] LIVE_NODES, int PROCESS_NUM)
    {
        port_num = p_num;
        network = IDS;
        alive = LIVE_NODES;
        pid = PROCESS_NUM;
    }

    private void notify(ConnectToOtherRPCs rpc_connect, boolean connect, int pid, int new_connection_id)
    {
        RPCFunctions r;
        try
        {
            r = rpc_connect.get_connection(pid);
            if(connect)
                r.notify_connection(new_connection_id);
            else
                r.notify_failure(new_connection_id);
        }
        catch (Exception e) {}
            
    }

	@Override
	public void run()
    {
        ConnectToOtherRPCs rpc_connect = new ConnectToOtherRPCs(port_num);
        while(true)
        {
            for(int i = 1; i < 11; i++)
            {
                if(i != pid)
                {
                    try
                    {
                        RPCFunctions r = rpc_connect.get_connection(i);
                        r.heartbeat();
                        if(alive[i - 1] == false)
                        {
                            System.err.println("Process id: " + i + " connected");
	                        alive[i - 1] = true;
                            main.LIVE_NODES[i-1] = true; 
							main.update_live(); 
							for(int j = 1; j < 11; j++)
                                if(j != pid && j != i)
                                    notify(rpc_connect, true, j, i);
                        }
                    }
                    catch (Exception e)
                    {
                        if(alive[i - 1] == true)
                        {
                            System.err.println("Process id: " + i + " failed");
                            alive[i - 1] = false;
                            main.LIVE_NODES[i-1] = false; 
							main.update_live(); 
							for(int j = 1; j < 11; j++)
                                if(j != pid && j != i)
                                    notify(rpc_connect, false, j, i);
                        }
                    }
                }
            }
            try
            {
                Thread.sleep(500);
            }
            catch (Exception e) {}
        }
	}
}
