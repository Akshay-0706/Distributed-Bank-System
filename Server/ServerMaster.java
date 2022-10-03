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
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

import Path.Path;
import UI.Printer;

public class ServerMaster extends UnicastRemoteObject implements ServerMasterIF {
    protected ServerMaster() throws RemoteException {
        super();
        createMasterServer();
    }

    private static TreeMap<Integer, Boolean> servers;
    private static ArrayList<Integer> ports;
    private static long instance;
    private static int count;
    // private static Integer leader;

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

        String commandForServerDriver = "java \"@include.argfile\" " + Path.getServerDriver();
        String commandForLoadBalancerDriver = "java \"@include.argfile\" " + Path.getLoadBalancerDriver();

        System.out.println("\nWelcome!\n");

        System.out.print("No of servers: ");
        count = sc.nextInt();

        servers = new TreeMap<Integer, Boolean>();
        ports = new ArrayList<Integer>();

        instance = System.currentTimeMillis();

        if (count > 10) {
            System.out.println(
                    "Better not to create more than 10 servers for now!\nMax 10 servers are allowed.\nServer count is decreased to 10.");
            count = 10;
        }
        System.out.println("Enter " + count + " Port numbers:");

        String loadBalancerPorts = "";

        try {
            for (int i = 0; i < count; i++) {
                System.out.print("Port " + (i + 1) + ": ");
                int port = sc.nextInt();
                while (port < 1 || port > 65535) {
                    System.out.println("This port no. is invalid!");
                    System.out.print("Reneter Port " + (i + 1) + ": ");
                    port = sc.nextInt();
                }
                System.out.print("Time " + (i + 1) + ": ");
                final int time = sc.nextInt();

                loadBalancerPorts += " " + port;

                servers.put(port, false);

                Runtime.getRuntime()
                        .exec("cmd /c start cmd.exe /K \"" + commandForServerDriver + " " + port + " " + time + "\"");

                ports.add(port);
            }

            System.out.println();
            sc.close();

            // System.out.println("Sending ports to other servers");
            // serverMaster.sendPortsToServers();

            try {
                while (!serverMaster.areAllServersReady()) {
                    Thread.sleep(1000);
                }

                Runtime.getRuntime()
                        .exec("cmd /c start cmd.exe /K \"" + commandForLoadBalancerDriver + loadBalancerPorts + "\"");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }
    }

    @Override
    public boolean areAllServersReady() {
        boolean allReady = true;
        for (Boolean ready : servers.values()) {
            if (!ready) {
                allReady = false;
                break;
            }
        }
        return allReady;
    }

    @Override
    public boolean areOtherServersReady(int port) {
    TreeMap<Integer, Boolean> otherServers = new TreeMap<Integer, Boolean>(servers);
    otherServers.remove(port);
    boolean allReady = true;
        for (Boolean ready : otherServers.values()) {
            if (!ready) {
                allReady = false;
                break;
            }
        }
        return allReady;
    }

    // private void sendPortsToServers() {
    // try {
    // while (!areAllServersReady()) {
    // Thread.sleep(1000);
    // }

    // for (Integer port : ports) {
    // Registry registry = LocateRegistry.getRegistry(port);
    // ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));
    // serverIF.setPorts(ports);
    // }

    // } catch (NotBoundException e) {
    // e.printStackTrace();
    // } catch (RemoteException e) {
    // e.printStackTrace();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }

    // public void assignLeader() throws RemoteException {
    // // servers.sort(new Comparator<Integer>() {

    // // @Override
    // // public int compare(Integer o1, Integer o2) {
    // // return o1 <= o2 ? 1 : -1;
    // // }
    // // });
    // try {
    // while (!areAllServersReady()) {
    // Thread.sleep(1000);
    // }

    // leader = servers.lastKey();
    // for (Integer port : servers.keySet()) {

    // Registry registry = LocateRegistry.getRegistry(port);
    // ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));
    // serverIF.getLeader(leader);
    // }

    // } catch (NotBoundException e) {
    // e.printStackTrace();
    // } catch (RemoteException e) {
    // e.printStackTrace();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }

    // @Override
    // public void notifyTimeChanged() throws RemoteException {
    // int currentTime = 0, maxTime = 0;
    // for (Integer port : servers.keySet()) {
    // try {
    // Registry registry = LocateRegistry.getRegistry(port);
    // ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));

    // currentTime = serverIF.getTime();

    // if (currentTime > maxTime)
    // maxTime = currentTime;

    // } catch (NotBoundException e) {
    // e.printStackTrace();
    // } catch (RemoteException e) {
    // e.printStackTrace();
    // }
    // }

    // for (Integer port : servers.keySet()) {
    // try {
    // Registry registry = LocateRegistry.getRegistry(port);
    // ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));
    // serverIF.setTime(maxTime);
    // } catch (NotBoundException e) {
    // e.printStackTrace();
    // } catch (RemoteException e) {
    // e.printStackTrace();
    // }
    // }
    // }

    public static void createMasterServer() {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    // System.out.println("Trying to create server on port " + 2000 + "...");
                    LocateRegistry.createRegistry(2000);
                } catch (RemoteException e) {
                    try {
                        // System.out.println("Port already in use, trying to reconnect...");
                        LocateRegistry.getRegistry(2000);
                    } catch (RemoteException e1) {
                        System.out.println("Unable to reconnect to port: " + e.getMessage());
                    }
                }
                System.setProperty("java.rmi.server.hostname", "127.0.0.1");
                // System.out.println("Master Server is now running on port " + 2000 +
                // "...");
                Printer.boxPrinter("Master Server: " + 2000);
                System.out.println();

            }
        });
        t.start();
    }

    @Override
    public void notifyServerIsReady(int port) throws RemoteException {
        servers.put(port, true);
    }

    @Override
    public ArrayList<Integer> getPorts(int port) throws RemoteException {
        try {
            while (count != ports.size()) {
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<Integer> oldPorts = new ArrayList<Integer>(ports);
        if (!ports.contains(port)) {
            System.out.println("Added new server " + port);
            ports.add(port);
            servers.put(port, false);
            count++;
        }

        return oldPorts;
    }

    @Override
    public long getInstance() throws RemoteException {
        return instance;
    }

    // @Override
    // public void checkServerLife() throws RemoteException {

    // }

}