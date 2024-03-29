package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerMasterIF extends Remote {
    // public void assignLeader() throws RemoteException;

    // public void notifyTimeChanged() throws RemoteException;

    public void notifyServerIsReady(int port) throws RemoteException;

    public long getInstance() throws RemoteException;

    public ArrayList<Integer> getPorts(int port) throws RemoteException;

    public boolean areAllServersReady() throws RemoteException;

    public boolean areOtherServersReady(int port) throws RemoteException;

    public void waitForLoadBalancerToBeReady() throws RemoteException;

    public void setLoadBalancerIsReady() throws RemoteException;

    public void removeServer(int port) throws RemoteException;

    // public void checkServerLife() throws RemoteException;
}
