package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Account.Account;

public interface ServerIF extends Remote {
    public void notifyLogOut(int accId) throws RemoteException;

    public void notifyStartConnection() throws RemoteException;

    public void notifyStopConnection() throws RemoteException;

    public int createAccount(String username, String password, double balance) throws RemoteException;

    public String[] loginAccount(int accId, String password) throws RemoteException;

    public void deleteAccount(int accId, String password) throws RemoteException;

    public void withdraw(int accId, double money, boolean isToBeTransfered) throws RemoteException;

    public void deposit(int accId, double money, boolean isTransfered) throws RemoteException;

    public boolean transfer(int senderId, int receiverId, double money) throws RemoteException;
}
