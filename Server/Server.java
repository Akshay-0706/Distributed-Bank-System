package Bank.Server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import Bank.Account.Account;

public class Server extends UnicastRemoteObject implements ServerIF {

    public Server() throws RemoteException {
        super();
    }

    // public static void main(String[] args) {
    // if (args.length == 0) {
    // System.out.println("Please give command line input for port");
    // return;
    // }
    // int port = Integer.parseInt(args[0]);
    // try {
    // if (port == 3000) {
    // System.out.println("Port 3000 is already assigned to Load Balancer, please
    // use another!");
    // return;
    // }
    // System.out.println("Trying to create server on port " + port + "...");
    // LocateRegistry.createRegistry(port);
    // } catch (RemoteException e) {
    // try {
    // System.out.println("Port already in use, trying to reconnect...");
    // LocateRegistry.getRegistry(port);
    // } catch (RemoteException e1) {
    // System.out.println("Unable to reconnect to port: " + e.getMessage());
    // }
    // }
    // System.setProperty("java.rmi.server.hostname", "127.0.0.1");
    // System.out.print("Server running on port " + port + "...");
    // try {
    // System.in.read();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

    @Override
    public void createAccount(String username, String password) throws RemoteException {
        System.out.println("Account created");
    }

    @Override
    public Account loginAccount(String username, String password) throws RemoteException {
        System.out.println("Logged in");
        return null;
    }

    @Override
    public void deleteAccount(String username, String password) throws RemoteException {
        System.out.println("Account deleted");

    }

    @Override
    public int withdraw(Account account) throws RemoteException {
        System.out.println("Money withdrawn");
        return 0;
    }

    @Override
    public void deposit(Account account, int money) throws RemoteException {
        System.out.println("Money deposited");
    }

}
