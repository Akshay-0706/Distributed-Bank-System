package LoadBalancer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.TreeMap;

public interface LoadBalancerIF extends Remote {
    public void setServers(ArrayList<Integer> ports, TreeMap<Integer, Integer> portsAreBusy) throws RemoteException;

    public int requestServer() throws RemoteException;

    public void freeServer(int port) throws RemoteException;

    public void lockAccount(int accId) throws RemoteException;

    public void unlockAccount(int accId) throws RemoteException;

    public void addServer(int port) throws RemoteException;

    public void removeServer(int port) throws RemoteException;
}
