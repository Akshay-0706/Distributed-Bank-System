package Server;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import UI.Ascii;
import UI.Printer;

public class ServerDriver {
    private static boolean failed = false, done = false;

    public static void main(String[] args) {

        int port = Integer.parseInt(args[0]);
        int time = Integer.parseInt(args[1]);

        try {
            createServer(port);
            while (!done) {
                Thread.sleep(1000);
            }
            if (failed)
                return;

            Registry registry = LocateRegistry.getRegistry(2000);
            ServerMasterIF serverMasterIF = (ServerMasterIF) registry.lookup("master");

            Server server = new Server(port, time, serverMasterIF);
            UnicastRemoteObject.unexportObject(server, true);
            ServerIF serverIF = (ServerIF) UnicastRemoteObject.exportObject(server, 0);
            Registry registry2 = LocateRegistry.getRegistry(port);
            registry2.rebind(String.valueOf(port), serverIF);
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void createServer(int port) {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    if (port == 2000 || port == 3000) {
                        System.out.println("Port 2000 & 3000 are not available, please use another!");
                        failed = true;
                        done = true;
                        return;
                    }
                    // System.out.println("Trying to create server on port " + port + "...");
                    LocateRegistry.createRegistry(port);
                } catch (RemoteException e) {
                    try {
                        // System.out.println("Port already in use, trying to reconnect...");
                        LocateRegistry.getRegistry(port);
                    } catch (RemoteException e1) {
                        System.out.println("Unable to reconnect to port: " + e.getMessage());
                    }
                }
                System.setProperty("java.rmi.server.hostname", "127.0.0.1");
                // System.out.println("Server running on port " + port + "...");
                Printer.boxPrinter("Server: " + port);
                done = true;
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
