package Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
// import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;

import Account.Account;
import LoadBalancer.LoadBalancerIF;
import Log.Log;
import Server.ServerIF;

public class Client {
    // protected Client() throws RemoteException {
    // super();
    // }
    private static LoadBalancerIF loadBalancerIF;
    private static ServerIF serverIF;
    private static Account account;
    private static Scanner sc = new Scanner(System.in);
    private static int port, time;
    private static long instance;
    private static String assistance[] = { "What can we do for you?", "How can we help you?", "Anything else?",
            "Need more assistance?",
            "Choose any option:", "You can modify your account", "Want to create another account?",
            "You can access your account from anywhere at anytime!" };

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Time input is required!");
            return;
        }
        time = Integer.parseInt(args[0]);
        try {
            timer();

            Registry registry = LocateRegistry.getRegistry(3000);
            loadBalancerIF = (LoadBalancerIF) registry.lookup("load");

            System.out.println("Please wait, while we look for some of our servers to get free...");

            port = loadBalancerIF.requestServer();

            System.out.println("Found one!\nYou are connected to our server " + port);

            Registry registry2 = LocateRegistry.getRegistry(port);
            serverIF = (ServerIF) registry2.lookup(String.valueOf(port));

            serverIF.notifyStartConnection();
            instance = serverIF.sendInstance();
            initializeMenu();
            serverIF.notifyStopConnection();
            loadBalancerIF.freeServer(port);
            System.out.println("END");

        } catch (RemoteException e) {
            System.out.println("Start the server first: " + e.getMessage());
        } catch (NotBoundException e) {
            System.out.println("One of two registries are failed to lookup for object: "
                    + e.getMessage());
        }
    }

    private static void initializeMenu() {
        Random random = new Random();
        int option = -1;

        System.out.println("Welcome to ASK BANK!\n");
        while (true) {
            System.out.println(assistance[random.nextInt(assistance.length)]);

            System.out.println("\n0. Exit\t\t1. Create\t2. Login to\t3. Delete");
            System.out.println("\t\t   account\t   my account\t   account");
            System.out.print("-> ");
            option = sc.nextInt();

            if (option == 0) {
                System.out.println("Thank you for using our service!");
                break;
            }
            switch (option) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    loginAccount();
                    break;
                case 3:
                    deleteAccount();
                    break;

                default:
                    System.out.println("We are sure, no such option is there!\n");
                    break;
            }
        }
    }

    private static void createAccount() {
        System.out.println("\nCREATE ACCOUNT");
        try {
            System.out.println("We will need few details first:\n");
            System.out.print("Name -> ");
            sc.nextLine();
            String username = sc.nextLine();
            System.out.print("Password -> ");
            String password = sc.nextLine();
            System.out.print("Initial balance -> ");
            double balance = sc.nextDouble();
            System.out.println("Above");
            System.out.println(account);
            int accId = serverIF.createAccount(username, password, balance, time);
            account = new Account(accId, username, password, balance);
            System.out.println("Below");
            Log.clientLog(port, time, instance, accId, "Account created");
            System.out.println("Your account has been created!");
            System.out.println("Account ID -> " + accId);
            System.out.println("Keep your Account ID for future usage.\n");

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void loginAccount() {
        Random random = new Random();

        System.out.println("\nLOGIN");
        System.out.println("Enter required credentials:\n");
        System.out.print("Account ID -> ");
        int accId = sc.nextInt();
        sc.nextLine();
        System.out.print("Password -> ");
        String password = sc.nextLine();

        try {
            Log.clientLog(port, time, instance, accId, "Login request");
            String credentials[] = serverIF.loginAccount(accId, password, time);

            if (credentials != null) {
                account = new Account(accId, credentials[0], password, Double.parseDouble(credentials[1]));
                Log.clientLog(port, time, instance, accId, "Logged in");
                loadBalancerIF.lockAccount(accId);
                System.out.println("Welcome back Sir/Madam!");
                while (true) {
                    System.out.println(assistance[random.nextInt(assistance.length)]);
                    System.out.println("Balance -> " + account.getBalance());
                    System.out.println("\n0. No thanks\t1. Withdraw\t2. Deposit\t3. Transfer Money");
                    System.out.println("\t\t   money\t   money");
                    int choice = sc.nextInt();

                    if (choice == 0) {
                        serverIF.notifyLogOut(accId);
                        loadBalancerIF.unlockAccount(accId);
                        Log.clientLog(port, time, instance, accId, "Logged out");
                        System.out.println("Logged out!");
                        break;
                    }

                    switch (choice) {
                        case 1:
                            withdraw();
                            break;
                        case 2:
                            deposit();
                            break;
                        case 3:
                            transfer();
                            break;
                        default:
                            System.out.println("We are sure, no such option is there!\n");
                            break;
                    }
                }
            } else {
                Log.clientLog(port, time, instance, accId, "Login failed");
                System.out.println("Incorrect username or password!");
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public static void withdraw() {
        System.out.print("Amount to withdraw: ");
        double money = sc.nextDouble();
        try {
            if (money > account.getBalance())
                System.out.println("Insufficient balance!");
            else {
                serverIF.withdraw(account.getAccId(), money, false, time);
                account.setBalance(account.getBalance() - money);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void deposit() {
        System.out.print("Amount to deposit: ");
        double money = sc.nextDouble();
        try {
            serverIF.deposit(account.getAccId(), money, false, time);
            account.setBalance(account.getBalance() + money);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void transfer() {
        System.out.print("Receiver Account ID: ");
        int receiverId = sc.nextInt();
        System.out.print("Amount to transfer: ");
        double money = sc.nextDouble();

        try {
            if (money > account.getBalance())
                System.out.println("Insufficient balance!");
            else {
                boolean success = serverIF.transfer(account.getAccId(), receiverId, money, time);
                if (success)
                    account.setBalance(account.getBalance() - money);
                else
                    System.out.println("Receiver's account does not exists");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void deleteAccount() {
        System.out.println("\nDELETE ACCOUNT");
        try {
            System.out.println("We will need few details first:\n");
            System.out.print("Account ID -> ");
            int accId = sc.nextInt();
            sc.nextLine();
            System.out.print("Password -> ");
            String password = sc.nextLine();
            System.out.print("Write DELETE to confirm -> ");
            String confirm = sc.nextLine();
            if (confirm.equals("DELETE")) {
                Log.clientLog(port, time, instance, accId, "Account deletion request");
                serverIF.deleteAccount(accId, password, time);
                Log.clientLog(port, time, instance, accId, "Account deleted");

            } else
                System.out.println("Cancelling...\n");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void timer() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    time++;
                }
            }
        });
        t.start();
    }
}
