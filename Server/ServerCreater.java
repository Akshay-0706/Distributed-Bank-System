package Server;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.Scanner;

import Path.Path;

public class ServerCreater {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        System.out.println("\nWelcome!\n");

        String command = "java \"@include.argfile\" " + Path.getServerDriver();

        System.out.print("No of servers: ");
        int count = sc.nextInt();

        if (count > 10) {
            System.out.println(
                    "Better not to create more than 10 servers for now!\nMax 10 servers are allowed.\nServer count is decreased to 10.");
            count = 10;
        }
        System.out.print("Enter " + count + " Port numbers: ");
        try {
            for (int i = 0; i < count; i++) {
                final int port = sc.nextInt();

                Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"" + command + " " + port + "\"");
            }

        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }

        sc.close();
        System.out.println();
    }

}