package LoadBalancer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface LoadBalancerIF extends Remote {
    public void setServers(int ports[], HashMap<Integer, Boolean> portsAreBusy) throws RemoteException;

    public int requestServer() throws RemoteException;

    public void freeServer(int port) throws RemoteException;

    public void lockAccount(int accId) throws RemoteException;

    public void unlockAccount(int accId) throws RemoteException;
}
