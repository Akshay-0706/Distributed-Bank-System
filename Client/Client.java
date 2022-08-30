package Bank.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import Bank.Account.Account;
import Bank.LoadBalancer.LoadBalancerIF;
import Bank.Server.ServerIF;

public class Client extends UnicastRemoteObject implements ClientIF {
    protected Client() throws RemoteException {
        super();
    }

    static ServerIF serverIF;
    public static int port;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(3000);
            LoadBalancerIF loadBalancerIF = (LoadBalancerIF) registry.lookup("load");

            port = loadBalancerIF.requestServer();

            System.out.println("Server available is " + port);

            Registry registry2 = LocateRegistry.getRegistry(port);
            serverIF = (ServerIF) registry2.lookup(String.valueOf(port));

            createAccount();
        } catch (RemoteException e) {
            System.out.println("Start the server first: " + e.getMessage());
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    public static void createAccount() {
        System.out.println("Trying to contact server to create account...");
        try {
            serverIF.createAccount("Akshay", "1234");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void loginAccount() {
    }

    public static void deleteAccount() {
    }

    @Override
    public void withdraw(Account account) throws RemoteException {

    }

    @Override
    public void deposit(Account account) throws RemoteException {

    }

}
