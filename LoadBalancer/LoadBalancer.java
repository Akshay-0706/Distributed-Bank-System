package Bank.LoadBalancer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerIF {

    private int current = 0;
    private int ports[];
    private HashMap<Integer, Boolean> portsAreBusy;

    public LoadBalancer() throws RemoteException {
        super();
    }

    @Override
    public void setServers(int ports[], HashMap<Integer, Boolean> portsAreBusy) throws RemoteException {
        this.ports = ports;
        this.portsAreBusy = portsAreBusy;
    }

    @Override
    public int requestServer() throws RemoteException {
        System.out.println("Checking for available servers...");
        while (portsAreBusy.get(ports[current % ports.length])) {
            current++;
        }
        portsAreBusy.put(ports[current], true);
        System.out.println("Server " + ports[current] + " is available...");
        current++;
        return ports[current - 1];
    }

    @Override
    public void freeServer(int port) throws RemoteException {
        portsAreBusy.put(port, false);
    }
}
