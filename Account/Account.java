package Bank.Account;

import java.rmi.*;

public class Account implements AccountIF {
    private  int accId;
    private double balance;
    private String username, password;

    public Account(int accId, String username, String password, double balance) throws RemoteException {
        this.accId = accId;
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public int getAccId() throws RemoteException {
        return accId;
    }

    public void setAccId(int accId) throws RemoteException {
        this.accId = accId;
    }

    public double getBalance() throws RemoteException {
        return balance;
    }

    public void setBalance(double balance) throws RemoteException {
        this.balance = balance;
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    public void setUsername(String username) throws RemoteException {
        this.username = username;
    }

    public String getPassword() throws RemoteException {
        return password;
    }

    public void setPassword(String password) throws RemoteException {
        this.password = password;
    }
}
