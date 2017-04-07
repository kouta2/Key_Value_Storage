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

	@Override
	public void run()
    {
        while(true)
        {
            for(int i = 1; i < 11; i++)
            {
                ConnectToOtherRPCs rpc_connect = new ConnectToOtherRPCs(port_num);
                if(i != pid)
                {
                    try
                    {
                        RPCFunctions r = rpc_connect.get_connection(i);
                        r.heartbeat();
                        alive[i - 1] = true;
                    }
                    catch (Exception e)
                    {
                        alive[i - 1] = false;
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
