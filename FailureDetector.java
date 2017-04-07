import java.io.*;
import java.net.*;

public class FailureDetector extends Thread{

    static int port_num = 1234;
    static long network [];
    static boolean alive [];
	@Override
	public void run()
    {
        network = main.IDS;
        alive = main.LIVE_NODES;
        DatagramSocket serverSocket = new DatagramSocket(port_num);

        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        while(true)
        {
            
        }
	}

}
