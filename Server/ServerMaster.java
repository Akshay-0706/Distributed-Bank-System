package Server;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import Path.Path;

public class ServerMaster extends UnicastRemoteObject implements ServerMasterIF {
    protected ServerMaster() throws RemoteException {
        super();
        createMasterServer();
    }

    private static ArrayList<Integer> servers;
    private static Integer leader;

    public static void main(String[] args) {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");

        ServerMaster serverMaster = null;
        try {
            serverMaster = new ServerMaster();
            UnicastRemoteObject.unexportObject(serverMaster, true);
            ServerMasterIF serverMasterIF = (ServerMasterIF) UnicastRemoteObject.exportObject(serverMaster, 0);
            Registry registry = LocateRegistry.getRegistry(2000);
            registry.rebind("master", serverMasterIF);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }

        Scanner sc = new Scanner(System.in);

        String command = "java \"@include.argfile\" " + Path.getServerDriver();

        System.out.println("\nWelcome!\n");

        System.out.print("No of servers: ");
        int count = sc.nextInt();

        servers = new ArrayList<Integer>();

        long instance = System.currentTimeMillis();

        if (count > 10) {
            System.out.println(
                    "Better not to create more than 10 servers for now!\nMax 10 servers are allowed.\nServer count is decreased to 10.");
            count = 10;
        }
        System.out.println("Enter " + count + " Port numbers:");

        try {
            for (int i = 0; i < count; i++) {
                System.out.print("Port " + (i + 1) + ": ");
                final int port = sc.nextInt();
                System.out.print("Time " + (i + 1) + ": ");
                final int time = sc.nextInt();

                servers.add(port);

                Runtime.getRuntime()
                        .exec("cmd /c start cmd.exe /K \"" + command + " " + port + " " + time + " " + instance + "\"");
            }

            System.out.println();
            sc.close();

            System.out.println("Assigning leader now...");
            serverMaster.assignLeader();
            System.out.println("Server " + leader + " is now the Leader!\n");

        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }

    }

    public void assignLeader() throws RemoteException {
        servers.sort(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 <= o2 ? 1 : -1;
            }
        });
        leader = servers.get(0);
        for (Integer port : servers) {
            try {
                Registry registry = LocateRegistry.getRegistry(port);
                ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));
                serverIF.getLeader(leader);
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void notifyTimeChanged() throws RemoteException {
        int currentTime = 0, maxTime = 0;
        for (Integer port : servers) {
            try {
                Registry registry = LocateRegistry.getRegistry(port);
                ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));

                currentTime = serverIF.getTime();

                if (currentTime > maxTime)
                    maxTime = currentTime;

            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        for (Integer port : servers) {
            try {
                Registry registry = LocateRegistry.getRegistry(port);
                ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));
                serverIF.setTime(maxTime);
            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createMasterServer() {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    System.out.println("Trying to create server on port " + 2000 + "...");
                    LocateRegistry.createRegistry(2000);
                } catch (RemoteException e) {
                    try {
                        System.out.println("Port already in use, trying to reconnect...");
                        LocateRegistry.getRegistry(2000);
                    } catch (RemoteException e1) {
                        System.out.println("Unable to reconnect to port: " + e.getMessage());
                    }
                }
                System.setProperty("java.rmi.server.hostname", "127.0.0.1");
                System.out.println("Master Server is now running on port " + 2000 + "...");
            }
        });
        t.start();
    }

}