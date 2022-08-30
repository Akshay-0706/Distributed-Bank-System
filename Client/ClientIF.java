package Bank.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Bank.Account.Account;

public interface ClientIF extends Remote {
    public void withdraw(Account account) throws RemoteException;
    public void deposit(Account account) throws RemoteException;
}
