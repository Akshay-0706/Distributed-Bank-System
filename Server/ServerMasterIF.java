package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerMasterIF extends Remote {
    public void assignLeader() throws RemoteException;

    public void notifyTimeChanged() throws RemoteException;
}
