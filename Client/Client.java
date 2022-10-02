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
import UI.Printer;

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

            // System.out.println("Please wait, while we look for some of our servers to get
            // free...");

            port = loadBalancerIF.requestServer();

            // System.out.println("Found one!\nYou are connected to our server " + port);

            Printer.boxPrinter("Connected to " + port);
            System.out.println();

            Registry registry2 = LocateRegistry.getRegistry(port);
            serverIF = (ServerIF) registry2.lookup(String.valueOf(port));

            serverIF.notifyStartConnection();
            instance = serverIF.sendInstance();
            initializeMenu();
            serverIF.notifyStopConnection();
            loadBalancerIF.freeServer(port);
            System.exit(0);

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
            Printer.boxPrinter("0. Exit    1. Create    2. Login    3. Delete");
            System.out.print("-> ");
            option = sc.nextInt();
            System.out.println();

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
        System.out.println("CREATE ACCOUNT");
        try {
            System.out.println("\nWe will need few details first:");
            System.out.print("Name -> ");
            sc.nextLine();
            String username = sc.nextLine();
            System.out.print("Password -> ");
            String password = sc.nextLine();
            System.out.print("Initial balance -> ");
            double balance = sc.nextDouble();
            System.out.println();
            int accId = serverIF.createAccount(username, password, balance, time);
            account = new Account(accId, username, password, balance);
            Log.clientLog(port, time, instance, accId, "Account created");
            System.out.println("Your account has been created!");
            Printer.boxPrinter("Account ID: " + accId);
            System.out.println("Keep your Account ID for future usage.\n");

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void loginAccount() {
        Random random = new Random();

        System.out.println("LOGIN");
        System.out.println("\nEnter required credentials:");
        System.out.print("Account ID -> ");
        int accId = sc.nextInt();
        sc.nextLine();
        System.out.print("Password -> ");
        String password = sc.nextLine();
        System.out.println();

        try {
            Log.clientLog(port, time, instance, accId, "Login request");
            String credentials[] = serverIF.loginAccount(accId, password, time);

            if (credentials != null) {
                account = new Account(accId, credentials[0], password, Double.parseDouble(credentials[1]));
                Log.clientLog(port, time, instance, accId, "Logged in");
                loadBalancerIF.lockAccount(accId);
                System.out.println("Welcome back Sir/Madam!");
                while (true) {
                    Printer.boxPrinter("Balance: " + account.getBalance());
                    System.out.println();
                    System.out.println(assistance[random.nextInt(assistance.length)]);
                    Printer.boxPrinter("0. Logout    1. Withdraw    2. Deposit    3. Transfer");
                    System.out.print("-> ");
                    int choice = sc.nextInt();
                    System.out.println();

                    if (choice == 0) {
                        serverIF.notifyLogOut(accId);
                        loadBalancerIF.unlockAccount(accId);
                        Log.clientLog(port, time, instance, accId, "Logged out");
                        System.out.println("Logged out!\n");
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
                System.out.println("Incorrect username or password!\n");
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public static void withdraw() {
        System.out.println("WITHDRAW");
        System.out.print("\nAmount -> ");
        double money = sc.nextDouble();
        try {
            if (money > account.getBalance())
                System.out.println("Failed: Insufficient balance :(\n");
            else {
                serverIF.withdraw(account.getAccId(), money, false, time);
                account.setBalance(account.getBalance() - money);
                System.out.println("Success!");
                System.out.println();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void deposit() {
        System.out.println("DEPOSIT");
        System.out.print("\nAmount -> ");
        double money = sc.nextDouble();
        try {
            serverIF.deposit(account.getAccId(), money, false, time);
            account.setBalance(account.getBalance() + money);
            System.out.println("Success!");
            System.out.println();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void transfer() {
        System.out.println("TRANSFER");
        System.out.print("\nReceiver's Account ID -> ");
        int receiverId = sc.nextInt();
        System.out.print("Amount -> ");
        double money = sc.nextDouble();

        try {
            if (receiverId == account.getAccId())
                System.out.println("Error: Can't do self-transfer!\n");
            else if (money > account.getBalance())
                System.out.println("Failed: Insufficient balance :(\n");
            else {
                boolean success = serverIF.transfer(account.getAccId(), receiverId, money, time);
                if (success) {
                    account.setBalance(account.getBalance() - money);
                    System.out.println("Success!");
                    System.out.println();
                } else
                    System.out.println("Error: Receiver's account does not exists!\n");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void deleteAccount() {
        System.out.println("DELETE ACCOUNT");
        try {
            System.out.println("\nWe will need few details first:");
            System.out.print("Account ID -> ");
            int accId = sc.nextInt();
            sc.nextLine();
            System.out.print("Password -> ");
            String password = sc.nextLine();
            System.out.print("Write DELETE to confirm -> ");
            String confirm = sc.nextLine();
            System.out.println();
            if (confirm.equals("DELETE")) {
                Log.clientLog(port, time, instance, accId, "Account deletion request");
                int status = serverIF.deleteAccount(accId, password, time);
                if (status == 0) {
                    System.out.println("Account deleted :(\n");
                    Log.clientLog(port, time, instance, accId, "Account deleted");
                } else
                    System.out.println("Invalid details or account does not exists!\n");
            } else
                System.out.println("Canceled :)\n");
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
