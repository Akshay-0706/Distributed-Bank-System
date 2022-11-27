package LoadBalancer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import UI.Printer;

public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerIF {

    private int current = 0;
    private int maxCapacity = 2;
    private TreeMap<Integer, Integer> servers;
    private ArrayList<Integer> ports, activeAcc;

    public LoadBalancer() throws RemoteException {
        super();
    }

    @Override
    public void setServers(ArrayList<Integer> ports, TreeMap<Integer, Integer> servers) throws RemoteException {
        this.ports = ports;
        this.servers = new TreeMap<Integer, Integer>(Comparator.reverseOrder());
        this.servers.putAll(servers);
        activeAcc = new ArrayList<Integer>();
    }

    @Override
    public int requestServer() throws RemoteException {

        System.out.println("Checking for available servers...");

        while (servers.get(ports.get(current % ports.size())) == maxCapacity) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            current++;
        }

        servers.put(ports.get(current % ports.size()), servers.get(ports.get(current % ports.size())) + 1);
        Printer.boxPrinter("Allocating Server " + ports.get(current % ports.size()));
        System.out.println();
        current++;
        return ports.get((current - 1) % ports.size());
    }

    @Override
    public void freeServer(int port) throws RemoteException {
        servers.put(port, servers.get(port) - 1);
        current = ports.indexOf(port);
    }

    @Override
    public void lockAccount(int accId) throws RemoteException {
        if (activeAcc.contains(accId))
            System.out.println(
                    "\nYou have already logged in to this account.\nLog out from other account to continue...");
        while (activeAcc.contains(accId)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        activeAcc.add(accId);
    }

    @Override
    public void unlockAccount(int accId) throws RemoteException {
        activeAcc.remove(activeAcc.indexOf(accId));
    }

    @Override
    public void addServer(int port) throws RemoteException {
        Printer.boxPrinter("Added Server " + port);
        ports.add(port);
        servers.put(port, 0);
    }

    @Override
    public void removeServer(int port) throws RemoteException {
        Printer.boxPrinter("Removed Server " + port);
        ports.remove(ports.indexOf(port));
        if (activeAcc.contains(port))
            activeAcc.remove(activeAcc.indexOf(port));
        servers.remove(port);
    }
}
