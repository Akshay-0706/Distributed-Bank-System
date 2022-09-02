package Bank.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;

import Bank.Account.Account;
import Bank.LoadBalancer.LoadBalancerIF;
import Bank.Server.ServerIF;

public class Client {
    // protected Client() throws RemoteException {
    // super();
    // }

    private static ServerIF serverIF;
    private static Account account;
    private static int port;
    private static String assistance[] = { "What can we do for you?", "How can we help you?", "Anything else",
            "Need more assistance?",
            "Choose any option:", "You can modify your account", "Want to create another account?",
            "You can access your account from anywhere at anytime!" };

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(3000);
            LoadBalancerIF loadBalancerIF = (LoadBalancerIF) registry.lookup("load");

            System.out.println("Please wait, while we look for some of our servers to get free...");

            port = loadBalancerIF.requestServer();

            System.out.println("Found one!\nYou are connected to our server " + port);

            Registry registry2 = LocateRegistry.getRegistry(port);
            serverIF = (ServerIF) registry2.lookup(String.valueOf(port));

            initializeMenu();

        } catch (RemoteException e) {
            System.out.println("Start the server first: " + e.getMessage());
        } catch (NotBoundException e) {
            System.out.println("One of two registries are failed to lookup for object: " + e.getMessage());
        }

    }

    private static void initializeMenu() {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();
        int option = -1;

        System.out.println("Welcome to ASK BANK!\n");
        while (true) {
            System.out.println(assistance[random.nextInt(assistance.length)]);

            System.out.println("0. Exit\t1. Create\t2. Login\t3. Delete");
            System.out.println("\tAccount\t\tMy Account");
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

                default:
                    break;
            }
        }
        sc.close();
    }

    private static void createAccount() {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();
        System.out.println("CREATE ACCOUNT");
        try {
            System.out.println("We will need few details first:");
            System.out.print("Name -> ");
            String username = sc.nextLine();
            System.out.print("Password -> ");
            String password = sc.nextLine();
            System.out.print("Initial balance -> ");
            double balance = sc.nextDouble();
            account = new Account(random.nextInt(900000000) + 100000000, username, password, balance);
            int accId = serverIF.createAccount(account);
            System.out.println("Your account has been created!");
            System.out.println("Account ID -> " + accId);
            System.out.println("Keep your Account ID for future usage.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    private static void loginAccount() {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        System.out.println("LOGIN");
        System.out.println("Name -> ");
        System.out.print("Account ID -> ");
        int accId = sc.nextInt();
        System.out.print("Password -> ");
        String password = sc.nextLine();

        try {
            Account account = serverIF.loginAccount(accId, password);

            if (account != null) {
                System.out.println("Welcome back Sir/Madam!");
                while (true) {

                    System.out.println(assistance[random.nextInt(assistance.length)]);
                    System.out.println("\n0. No thanks\t1. Withdraw\t2. Deposit");
                    System.out.println("\tmoney\tmoney");
                    int choice = sc.nextInt();
                    if (choice == 0) {
                        System.out.println("Thank you!");
                        break;
                    }

                    switch (choice) {
                        case 1:
                            withdraw(account);
                            break;
                        case 2:
                            deposit(account);
                            break;
                        default:
                            System.out.println("We are sure, no such option is there!");
                            break;
                    }
                }
            } else
                System.out.println("Incorrect username or password!");

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        sc.close();
    }

    public static void withdraw(Account account) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Amount to withdraw: ");
        double money = sc.nextDouble();
        sc.close();
        try {
            serverIF.withdraw(account.getAccId(), money, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void deposit(Account account) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Amount to deposit: ");
        double money = sc.nextDouble();
        sc.close();
        try {
            serverIF.deposit(account.getAccId(), money, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void deleteAccount() {
        Scanner sc = new Scanner(System.in);
        System.out.println("DELETE ACCOUNT");
        try {
            System.out.println("We will need few details first:");
            System.out.print("Account ID -> ");
            int accId = sc.nextInt();
            System.out.print("Password -> ");
            String password = sc.nextLine();
            System.out.println("Write DELETE to confirm -> ");
            String confirm = sc.nextLine();
            if (confirm == "DELETE")
                serverIF.deleteAccount(accId, password);
            else
                System.out.println("Terminating session!");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        sc.close();
    }
}
