package Bank.LoadBalancer;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Scanner;

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
            createServer();
            int[] ports = new int[args.length];
            HashMap<Integer, Boolean> portsAreBusy = new HashMap<Integer, Boolean>();
            for (int i = 0; i < args.length; i++) {
                ports[i] = Integer.parseInt(args[i]);
                portsAreBusy.put(Integer.parseInt(args[i]), false);
            }
            LoadBalancer loadBalancer = new LoadBalancer();
            UnicastRemoteObject.unexportObject(loadBalancer, true);
            LoadBalancerIF loadBalancerIF = (LoadBalancerIF) UnicastRemoteObject.exportObject(loadBalancer, 0);
            Registry registry = LocateRegistry.getRegistry(3000);
            registry.rebind("load", loadBalancerIF);
            loadBalancerIF.setServers(ports, portsAreBusy);
            System.out.print("Load Balancer is now bounded with name 'load'\n\n");
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        sc.close();
    }

    public static void createServer() {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {

                    System.out.println("Trying to start load balancer on port " + 3000 + "...");
                    LocateRegistry.createRegistry(3000);
                } catch (RemoteException e) {
                    try {
                        System.out.println("Port already in use, trying to reconnect...");
                        LocateRegistry.getRegistry(3000);
                    } catch (RemoteException e1) {
                        System.out.println("Unable to reconnect to port: " + e.getMessage());
                    }
                }
                System.out.println("Load Balancer running on port " + 3000 + "...\n");
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
