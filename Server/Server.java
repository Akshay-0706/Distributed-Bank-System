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
    String[] accounts = { "AlphaAccount", "BetaAccount" };
    int primaryAccIndex, secondaryAccIndex;

    public Server(int port, int time, ServerMasterIF serverMasterIF) throws RemoteException {
        super();
        this.port = port;
        this.time = time;
        this.serverMasterIF = serverMasterIF;

        timer();

        Random random = new Random();
        primaryAccIndex = random.nextInt(accounts.length);
        secondaryAccIndex = primaryAccIndex == 0 ? 1 : 0;
        messagePrinter(accounts[primaryAccIndex], true);

        instance = this.serverMasterIF.getInstance();

        ports = this.serverMasterIF.getPorts(port);

        if (!ports.contains(port))
            newServerAdder();
        else
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

    private void messagePrinter(String message, boolean inBox) {
        try {
            while (timerSemaphore) {
                Thread.sleep(1000);
            }
            printerSemaphore = true;
            for (int i = 0; i < 6 + String.valueOf(time).length() + 4; i++) {
                System.out.print("\b \b");
            }
            if (inBox)
                Printer.boxPrinter(message);
            else
                System.out.println(message);
            printerSemaphore = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void newServerAdder() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {

                    while (!serverMasterIF.areAllServersReady()) {
                        Thread.sleep(1000);
                    }

                    serverMasterIF.waitForLoadBalancerToBeReady();

                    for (Integer current : ports) {
                        try {
                            Registry registry = LocateRegistry.getRegistry(current);
                            ServerIF serverIF = (ServerIF) registry.lookup(String.valueOf(current));
                            serverIF.addNewServer(port, true);
                        } catch (NotBoundException e) {
                            messagePrinter("NOT BOUND EXCEPTION: " + current, false);
                        }
                    }
                    addNewServer(port, false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void timer() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        for (int i = 0; i < 6 + String.valueOf(time).length() + 4; i++) {
                            System.out.print("\b \b");
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
                    while (!serverMasterIF.areOtherServersReady(port)) {
                        Thread.sleep(1000);
                    }
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
                                    messagePrinter("Server " + current + " is dead", true);
                                    messagePrinter("Reassigning leader...", false);
                                    ports.remove(current);
                                    assignLeader();
                                    if (leader == port)
                                        serverMasterIF.removeServer(current);
                                    break;
                                } catch (NotBoundException e) {
                                    messagePrinter("NOT BOUND EXCEPTION: " + current, false);
                                }
                        }
                        portsSemaphore = false;
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void getLeaderInterface() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    while (!serverMasterIF.areOtherServersReady(port)) {
                        Thread.sleep(1000);
                    }
                    Registry registry = LocateRegistry.getRegistry(leader);
                    leaderIF = (ServerIF) registry.lookup(String.valueOf(leader));

                    messagePrinter("Leader is Server " + leader, true);

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
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

    private void addToAnother(int mode, int accId, String username, String password, double balance) {
        try {
            String query = "";
            switch (mode) {
                case 0:
                    query = "insert into " + accounts[secondaryAccIndex] + " values (" + accId + ", '"
                            + username + "', '"
                            + encoder(password)
                            + "', "
                            + balance + ");";
                    break;
                case 1:
                    query = "delete from " + accounts[secondaryAccIndex] + " where accId = " + accId
                            + ";";
                    break;
                case 2:
                    query = "update " + accounts[secondaryAccIndex] + " set balance = "
                            + balance + " where accId = "
                            + accId + ";";
                    break;
                default:
                    break;
            }
            statement.executeUpdate(query);

        } catch (SQLException e) {
            messagePrinter("Error in sending data to secondary database!\n", false);
        }
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
            messagePrinter("Account creation request granted...", false);

            String query = "insert into " + accounts[primaryAccIndex] + " values (" + accId + ", '"
                    + username + "', '"
                    + encoder(password)
                    + "', "
                    + balance + ");";
            statement.executeUpdate(query);
            addToAnother(0, accId, username, password, balance);
            Log.serverLog(port, this.time, instance, accId + ": Account created");

            Printer.boxPrinter("Account created!");
            System.out.println();
        } catch (SQLException e) {
            messagePrinter("Account ID already exists, creating another one...\n", false);
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
            messagePrinter("Account login request granted...", false);

            String query = "select * from " + accounts[primaryAccIndex] + " where accId = '" + accId
                    + "' and password = '" + encoder(password)
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
                messagePrinter("Invalid details or account does not exists!\n", false);
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
                messagePrinter("Account deletion request granted...", false);

                String query = "delete from " + accounts[primaryAccIndex] + " where accId = " + accId
                        + ";";
                statement.executeUpdate(query);
                addToAnother(1, accId, "", "", 0);
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
            messagePrinter("Money withdraw request received...", false);
            double newBalance = getBalance(accId) - money;
            String query = "update " + accounts[primaryAccIndex] + " set balance = "
                    + newBalance + " where accId = "
                    + accId + ";";
            statement.executeUpdate(query);
            addToAnother(2, accId, "", "", newBalance);
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
            messagePrinter("Money deposit request received...", false);
            double newBalance = getBalance(accId) + money;

            String query = "update " + accounts[primaryAccIndex] + " set balance = "
                    + newBalance + " where accId = "
                    + accId + ";";
            statement.executeUpdate(query);
            addToAnother(2, accId, "", "", newBalance);

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
            messagePrinter("Money transfer request received...", false);
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

    @Override
    public double getBalance(int accId) {
        double balance = -1;
        try {
            String query = "select * from " + accounts[primaryAccIndex] + " where accId = " + accId
                    + " limit 1;";
            ResultSet resultSet = statement.executeQuery(query);
            // connection.close();
            if (resultSet.next()) {
                balance = resultSet.getDouble("balance");
            } else {
                messagePrinter("Account does not exists: " + accId, false);
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

        // try {
        // while (portsSemaphore) {
        // Thread.sleep(1000);
        // messagePrinter("Waiting...", false);
        // }
        // newServerSemaphore = true;
        Collections.sort(ports, Collections.reverseOrder());
        // newServerSemaphore = false;
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        leader = ports.get(0);

        if (leader == port) {
            messagePrinter("Leader", true);
        } else {
            getLeaderInterface();
        }

    }

    @Override
    public void getLeader(int leader) throws RemoteException {
        this.leader = leader;
    }

    @Override
    public void setTime(int newTime) throws RemoteException {
        this.time = newTime;
        messagePrinter("Time updated to " + this.time, true);
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
    public void addNewServer(int port, boolean printMessage) throws RemoteException {
        if (printMessage)
            messagePrinter("New server " + port, true);
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
