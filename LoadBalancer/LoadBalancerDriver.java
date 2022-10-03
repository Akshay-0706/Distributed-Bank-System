package LoadBalancer;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Server.ServerMaster;
import Server.ServerMasterIF;
import UI.Printer;

public class LoadBalancerDriver {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Server port numbers are required!\nTerminating...");
            return;
        }
        Scanner sc = new Scanner(System.in);

        // System.out.print("Path to LoadBalancer.java file: ");
        // String pathToLoadBalancer = sc.nextLine();

        // String command = "java " + pathToLoadBalancer.replace("\\", "/");
        try {
            // Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"" + command + "\""); //
            // ports already has space at the
            // beginning...
            createLoadBalancer();
            ArrayList<Integer> ports = new ArrayList<Integer>();
            HashMap<Integer, Integer> servers = new HashMap<Integer, Integer>();

            for (int i = 0; i < args.length; i++) {
                ports.add(Integer.parseInt(args[i]));
                servers.put(Integer.parseInt(args[i]), 0);
            }

            LoadBalancer loadBalancer = new LoadBalancer();
            UnicastRemoteObject.unexportObject(loadBalancer, true);
            LoadBalancerIF loadBalancerIF = (LoadBalancerIF) UnicastRemoteObject.exportObject(loadBalancer, 0);
            Registry registry = LocateRegistry.getRegistry(3000);
            registry.rebind("load", loadBalancerIF);
            loadBalancerIF.setServers(ports, servers);

            Registry registry2 = LocateRegistry.getRegistry(2000);
            ServerMasterIF serverMasterIF = (ServerMasterIF) registry2.lookup("master");
            serverMasterIF.setLoadBalancerIsReady();

        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public static void createLoadBalancer() {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    // System.out.println("Trying to start load balancer on port " + 3000 + "...");
                    LocateRegistry.createRegistry(3000);
                } catch (RemoteException e) {
                    try {
                        // System.out.println("Port already in use, trying to reconnect...");
                        LocateRegistry.getRegistry(3000);
                    } catch (RemoteException e1) {
                        System.out.println("Unable to reconnect to port: " + e.getMessage());
                    }
                }
                // System.out.println("Load Balancer running on port " + 3000 + "...\n");
                Printer.boxPrinter("Load Balancer: " + 3000);
                System.out.println();

                try {
                    System.in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}
