package Bank.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Bank.Account.Account;

public interface ServerIF extends Remote {
    public int createAccount(Account account) throws RemoteException;

    public Account loginAccount(int accId, String password) throws RemoteException;

    public void deleteAccount(int accId, String password) throws RemoteException;

    public void withdraw(int accId, double money, boolean isToBeTransfered) throws RemoteException;

    public void deposit(int accId, double money, boolean isTransfered) throws RemoteException;

    public void transfer(int senderId, int receiverId, double money) throws RemoteException;

    public double getBalance(int accId) throws RemoteException;
}
