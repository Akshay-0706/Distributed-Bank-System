package Server;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Random;
import java.util.Base64.Encoder;
// import java.util.Base64.Decoder;
import Log.Log;
import UI.Printer;

public class Server extends UnicastRemoteObject implements ServerIF {

    private int port, time, leader;
    private long instance;
    private boolean timerSemaphore = true, printerSemaphore = false, portsSemaphore = true, newServerSemaphore = false;
    private ArrayList<Integer> ports;
    private ServerMasterIF serverMasterIF;
    private ServerIF leaderIF;
    private String className = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/bank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String username = "sqluser";
    private String password = "password";
    Connection connection;
    Statement statement;

    public Server(int port, int time, ServerMasterIF serverMasterIF) throws RemoteException {
        super();
        this.port = port;
        this.time = time;
        this.serverMasterIF = serverMasterIF;

        timer();

        instance = this.serverMasterIF.getInstance();

        ports = this.serverMasterIF.getPorts(port);

        if (!ports.contains(port)) {
            for (Integer current : ports) {
                try {
                    Registry registry = LocateRegistry.getRegistry(current);
                    ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(current));
                    serverIF.addNewServer(port);
                } catch (NotBoundException e) {
                    messagePrinter("Not bound exception for server " + current);
                }
            }

            ports.add(port);
        }
        this.serverMasterIF.notifyServerIsReady(port);

        assignLeader();

        Log.serverLog(port, this.time, instance, "Server is online");

        checkServerLife();
    }

    @Override
    public void notifyLogOut(int accId) throws RemoteException {
        Log.serverLog(port, this.time, instance, accId + ": Logged out");
    }

    public void notifyStartConnection() throws RemoteException {
        try {
            Class.forName(className);
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
            Log.serverLog(port, this.time, instance, "Server is busy");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyStopConnection() throws RemoteException {
        try {
            statement.close();
            connection.close();
            Log.serverLog(port, this.time, instance, "Server is free");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void messagePrinter(String message) {
        try {
            while (timerSemaphore) {
                Thread.sleep(1000);
            }
            printerSemaphore = true;
            for (int i = 0; i < 6 + String.valueOf(time).length() + 4; i++) {
                System.out.print("\b");
            }
            System.out.println(message);
            printerSemaphore = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void timer() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        for (int i = 0; i < 6 + String.valueOf(time).length() + 4; i++) {
                            System.out.print("\b");
                        }
                        while (printerSemaphore) {
                            Thread.sleep(1000);
                        }
                        timerSemaphore = true;
                        System.out.print("Time: ");
                        System.out.print(time);
                        System.out.print(" sec");
                        timerSemaphore = false;
                        Thread.sleep(1000);
                        time++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void checkServerLife() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Registry registry;
                    ServerIF serverIF;
                    while (true) {

                        while (newServerSemaphore) {
                            Thread.sleep(1000);
                        }
                        portsSemaphore = true;
                        for (Integer current : ports) {
                            if (current != port)
                                try {
                                    registry = LocateRegistry.getRegistry(current);
                                    serverIF = (ServerIF) registry.lookup(String.valueOf(current));
                                    serverIF.checkIfServerIsAlive();
                                } catch (RemoteException e) {
                                    messagePrinter("Server " + current + " is dead");
                                    messagePrinter("Reassigning leader...");
                                    ports.remove(current);
                                    assignLeader();
                                    break;
                                } catch (NotBoundException e) {
                                    e.printStackTrace();
                                }
                        }
                        portsSemaphore = false;
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public static void main(String[] args) {
        // try {
        // Account account = new Account(45634764, "Akshay", "1234", 1000);
        // Server server = new Server(3435);
        // // server.loginAccount(account.getAccId(), account.getPassword());
        // // server.withdraw(account.getAccId(), 100);
        // // server.deposit(account.getAccId(), 200);
        // server.notifyStartConnection();
        // // server.transfer(45634764, 45634664, 200);
        // server.createAccount(account.getUsername(), account.getPassword(),
        // account.getBalance());
        // server.notifyStopConnection();
        // } catch (RemoteException e) {
        // e.printStackTrace();
        // }
    }

    @Override
    public int createAccount(String username, String password, double balance, int time) throws RemoteException {
        if (this.time < time) {
            this.time = time;
            if (leader == port)
                notifyTimeChanged();
            else
                leaderIF.notifyTimeChanged();
        }
        Random random = new Random();
        int accId = random.nextInt(900000000) + 100000000;
        try {
            messagePrinter("Account creation request granted...");

            String query = "insert into accounts values (" + accId + ", '" + username + "', '"
                    + encoder(password)
                    + "', "
                    + balance + ");";
            statement.executeUpdate(query);
            Log.serverLog(port, this.time, instance, accId + ": Account created");

            // messagePrinter("Account created successfully!");
            Printer.boxPrinter("Account created!");
            System.out.println();
        } catch (SQLException e) {
            messagePrinter("Account ID already exists, creating another one...\n");
            return createAccount(username, password, balance, time);
        }
        return accId;
    }

    @Override
    public String[] loginAccount(int accId, String password, int time) throws RemoteException {
        if (this.time < time) {
            this.time = time;
            if (leader == port)
                notifyTimeChanged();
            else
                leaderIF.notifyTimeChanged();
        }
        String credentials[] = null;
        try {
            Log.serverLog(port, this.time, instance, accId + ": Login requested");
            messagePrinter("Account login request granted...");

            String query = "select * from accounts where accId = '" + accId + "' and password = '" + encoder(password)
                    + "' limit 1;";
            ResultSet resultSet = statement.executeQuery(query);
            // connection.close();
            if (resultSet.next()) {
                // account = new Account(accId, resultSet.getString("name"), password,
                // resultSet.getDouble("balance"));
                credentials = new String[2];
                credentials[0] = resultSet.getString("name");
                credentials[1] = String.valueOf(resultSet.getDouble("balance"));
                Log.serverLog(port, this.time, instance, accId + ": Logged in");
                // messagePrinter("Logged in successfully!");
                Printer.boxPrinter("Logged in!");
                System.out.println();
            } else {
                Log.serverLog(port, this.time, instance, accId + ": Login failed");
                messagePrinter("Invalid details or account does not exists!\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return credentials;
    }

    @Override
    public int deleteAccount(int accId, String password, int time) throws RemoteException {
        int status = 0;
        if (this.time < time) {
            this.time = time;
            if (leader == port)
                notifyTimeChanged();
            else
                leaderIF.notifyTimeChanged();
        }
        if (loginAccount(accId, password, time) != null) {
            try {
                messagePrinter("Account deletion request granted...");

                String query = "delete from accounts where accId = " + accId + ";";
                statement.executeUpdate(query);
                Log.serverLog(port, this.time, instance, accId + ": Account deleted");
                // messagePrinter("Account deleted successfully!");
                Printer.boxPrinter("Account deleted!");
                System.out.println();
                status = 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else
            status = 1;
        return status;
    }

    @Override
    public void withdraw(int accId, double money, boolean isToBeTransfered, int time) throws RemoteException {
        if (this.time < time) {
            this.time = time;
            if (leader == port)
                notifyTimeChanged();
            else
                leaderIF.notifyTimeChanged();
        }
        try {
            messagePrinter("Money withdraw request received...");

            String query = "update accounts set balance = " + (getBalance(accId) - money) + " where accId = "
                    + accId + ";";
            statement.executeUpdate(query);
            if (!isToBeTransfered) {
                query = "insert into transactions (accId, receiverId, mode, amount, date, time) values("
                        + accId + ", " + accId + ", 'Withdraw', " + money
                        + ", curdate(), curtime());";
                statement.executeUpdate(query);
            }
            // messagePrinter("Money withdrawn successfully!");
            Printer.boxPrinter("Withdrawn " + money);
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deposit(int accId, double money, boolean isTransfered, int time) throws RemoteException {
        if (this.time < time) {
            this.time = time;
            if (leader == port)
                notifyTimeChanged();
            else
                leaderIF.notifyTimeChanged();
        }
        try {
            messagePrinter("Money deposit request received...");
            String query = "update accounts set balance = " + (getBalance(accId) + money) + " where accId = "
                    + accId + ";";
            statement.executeUpdate(query);
            if (!isTransfered) {
                query = "insert into transactions (accId, senderId, mode, amount, date, time) values("
                        + accId + ", " + accId + ", 'Deposit', " + money
                        + ", curdate(), curtime());";
                statement.executeUpdate(query);
            }
            // messagePrinter("Money deposited successfully!");
            Printer.boxPrinter("Deposited " + money);
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean transfer(int senderId, int receiverId, double money, int time) throws RemoteException {
        if (this.time < time) {
            this.time = time;
            if (leader == port)
                notifyTimeChanged();
            else
                leaderIF.notifyTimeChanged();
        }
        if (getBalance(receiverId) != -1) {
            messagePrinter("Money transfer request received...");
            withdraw(senderId, money, true, time);
            deposit(receiverId, money, true, time);
            String query = "insert into transactions values("
                    + senderId + ", " + senderId + ", " + receiverId + ", 'Transfer', " + money
                    + ", curdate(), curtime());";
            try {
                statement.executeUpdate(query);
                Printer.boxPrinter("Transfered " + money);
                System.out.println();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        } else
            return false;
    }

    @Override
    public long sendInstance() {
        return instance;
    }

    private double getBalance(int accId) {
        double balance = -1;
        try {

            String query = "select * from accounts where accId = " + accId + " limit 1;";
            ResultSet resultSet = statement.executeQuery(query);
            // connection.close();
            if (resultSet.next()) {
                balance = resultSet.getDouble("balance");
            } else {
                messagePrinter("Account does not exists: " + accId);
                System.out.println();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    private String encoder(String text) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(text.getBytes());
    }

    public void assignLeader() {

        try {
            Collections.sort(ports, Collections.reverseOrder());

            leader = ports.get(0);

            if (leader == port)
                messagePrinter("This server is now the leader!\n");
            else {

                while (!serverMasterIF.areAllServersReady()) {
                    Thread.sleep(1000);
                }

                Registry registry = LocateRegistry.getRegistry(leader);
                leaderIF = (ServerIF) registry.lookup(String.valueOf(leader));
            }

        } catch (NotBoundException e) {
            try {
                Thread.sleep(1000);
                assignLeader();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getLeader(int leader) throws RemoteException {
        this.leader = leader;
    }

    @Override
    public void setTime(int newTime) throws RemoteException {
        this.time = newTime;
        messagePrinter("Time has been updated to " + this.time);
        System.out.println();
    }

    @Override
    public int getTime() throws RemoteException {
        return time;
    }

    @Override
    public void notifyTimeChanged() throws RemoteException {
        int currentTime = 0, maxTime = time;
        for (Integer port : ports) {
            if (this.port != port)
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

        for (Integer port : ports) {
            if (this.port != port)
                try {
                    Registry registry = LocateRegistry.getRegistry(port);
                    ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(port));
                    serverIF.setTime(maxTime);
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            else
                setTime(maxTime);
        }
    }

    @Override
    public void addNewServer(int port) throws RemoteException {
        messagePrinter("Added new server " + port);
        try {
            while (portsSemaphore) {
                Thread.sleep(1000);
            }
            newServerSemaphore = true;
            ports.add(port);
            newServerSemaphore = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assignLeader();
    }

    @Override
    public void checkIfServerIsAlive() throws RemoteException {
        // I am alive!
    }

    // private String decoder(String text) {
    // Decoder decoder = Base64.getDecoder();
    // return new String(decoder.decode(text));
    // }
}
