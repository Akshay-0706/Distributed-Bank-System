package Bank.Account;
import java.rmi.*;

public interface AccountIF extends Remote {
    public double getBalance() throws RemoteException;

    public void setBalance(double balance) throws RemoteException;

    public String getUsername() throws RemoteException;

    public void setUsername(String username) throws RemoteException;

    public String getPassword() throws RemoteException;

    public void setPassword(String password) throws RemoteException;
}
