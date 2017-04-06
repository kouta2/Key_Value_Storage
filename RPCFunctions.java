import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RPCFunctions extends Remote {
    // String sayHello() throws RemoteException;

    // Add RPC function headers hear
    String get(int pid, String key) throws RemoteException;

    String set(int pid, String key, String value) throws RemoteException;

    // String owners(String key) throws RemoteException;
}

