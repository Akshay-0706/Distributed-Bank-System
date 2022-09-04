package LoadBalancer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerIF {

    private int current = 0;
    private int ports[];
    private HashMap<Integer, Boolean> portsAreBusy;
    private ArrayList<Integer> activeAcc;

    public LoadBalancer() throws RemoteException {
        super();
    }

    @Override
    public void setServers(int ports[], HashMap<Integer, Boolean> portsAreBusy) throws RemoteException {
        this.ports = ports;
        this.portsAreBusy = portsAreBusy;
        activeAcc = new ArrayList<Integer>();
    }

    @Override
    public int requestServer() throws RemoteException {
        System.out.println("Checking for available servers...");
        while (portsAreBusy.get(ports[current % ports.length])) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            current++;
        }
        portsAreBusy.put(ports[current % ports.length], true);
        System.out.println("Server " + ports[current % ports.length] + " is available...");
        current++;
        return ports[(current - 1) % ports.length];
    }

    @Override
    public void freeServer(int port) throws RemoteException {
        portsAreBusy.put(port, false);
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
}
