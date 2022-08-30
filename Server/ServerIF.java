package Bank.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Bank.Account.Account;

public interface ServerIF extends Remote {
    public void createAccount(String username, String password) throws RemoteException;
    public Account loginAccount(String username, String password) throws RemoteException;
    public void deleteAccount(String username, String password) throws RemoteException;
    public int withdraw(Account account) throws RemoteException;
    public void deposit(Account account, int money) throws RemoteException;
}
