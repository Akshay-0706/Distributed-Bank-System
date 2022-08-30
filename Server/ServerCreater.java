package Bank.Server;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerCreater {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try {
            createServer(port);

            Server server = new Server();
            UnicastRemoteObject.unexportObject(server, true);
            ServerIF serverIF = (ServerIF) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind(String.valueOf(port), serverIF);
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void createServer(int port) {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    if (port == 3000) {
                        System.out
                                .println("Port 3000 is already assigned to Load Balancer, please use another!");
                        return;
                    }
                    System.out.println("Trying to create server on port " + port + "...");
                    LocateRegistry.createRegistry(port);
                } catch (RemoteException e) {
                    try {
                        System.out.println("Port already in use, trying to reconnect...");
                        LocateRegistry.getRegistry(port);
                    } catch (RemoteException e1) {
                        System.out.println("Unable to reconnect to port: " + e.getMessage());
                    }
                }
                System.setProperty("java.rmi.server.hostname", "127.0.0.1");
                System.out.println("Server running on port " + port + "...");
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
