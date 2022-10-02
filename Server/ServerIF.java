package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerIF extends Remote {
    public void notifyLogOut(int accId) throws RemoteException;

    public void notifyStartConnection() throws RemoteException;

    public void notifyStopConnection() throws RemoteException;

    public int createAccount(String username, String password, double balance, int time) throws RemoteException;

    public String[] loginAccount(int accId, String password, int time) throws RemoteException;

    public int deleteAccount(int accId, String password, int time) throws RemoteException;

    public void withdraw(int accId, double money, boolean isToBeTransfered, int time) throws RemoteException;

    public void deposit(int accId, double money, boolean isTransfered, int time) throws RemoteException;

    public boolean transfer(int senderId, int receiverId, double money, int time) throws RemoteException;

    public long sendInstance() throws RemoteException;

    public void getLeader(int leader) throws RemoteException;

    public void setTime(int newTime) throws RemoteException;

    public int getTime() throws RemoteException;

    public void notifyTimeChanged() throws RemoteException;

    public void addNewServer(int port) throws RemoteException;

    public void checkIfServerIsAlive() throws RemoteException;
}
