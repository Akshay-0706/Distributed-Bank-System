package Server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

import java.util.Base64;
import java.util.Random;
import java.util.Base64.Encoder;
// import java.util.Base64.Decoder;
import Account.Account;
import Log.Log;

public class Server extends UnicastRemoteObject implements ServerIF {

    private int port;
    private String className = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/bank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String username = "sqluser";
    private String password = "password";
    Connection connection;
    Statement statement;

    public Server(int port) throws RemoteException {
        super();
        this.port = port;
        Log.serverLog(port, "Server is online");
    }

    @Override
    public void notifyLogOut(int accId) throws RemoteException {
        Log.serverLog(port, accId + ": Logged out");
    }

    public void notifyStartConnection() throws RemoteException {
        try {
            Class.forName(className);
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
            Log.serverLog(port, "Server is busy");
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
            Log.serverLog(port, "Server is free");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public int createAccount(String username, String password, double balance) throws RemoteException {
        Random random = new Random();
        int accId = random.nextInt(900000000) + 100000000;
        try {
            System.out.println("Account creation request granted...");

            String query = "insert into accounts values (" + accId + ", '" + username + "', '"
                    + encoder(password)
                    + "', "
                    + balance + ");";
            statement.executeUpdate(query);
            Log.serverLog(port, accId + ": Account created");

            System.out.println("Account created successfully!");
        } catch (SQLException e) {
            System.out.println("Account ID already exists, creating another one...");
            return createAccount(username, password, balance);
        }
        return accId;
    }

    @Override
    public String[] loginAccount(int accId, String password) throws RemoteException {
        String credentials[] = null;
        try {
            Log.serverLog(port, accId + ": Login requested");
            System.out.println("Account login request granted...");

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
                Log.serverLog(port, accId + ": Logged in");
                System.out.println("Logged in successfully!");
            } else {
                Log.serverLog(port, accId + ": Login failed");
                System.out.println("Account does not exists!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return credentials;
    }

    @Override
    public void deleteAccount(int accId, String password) throws RemoteException {
        if (loginAccount(accId, password) != null) {
            try {
                System.out.println("Account deletion request granted...");

                String query = "delete from accounts where accId = " + accId + ";";
                statement.executeUpdate(query);
                Log.serverLog(port, accId + ": Account deleted");
                System.out.println("Account deleted successfully!");
                // connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void withdraw(int accId, double money, boolean isToBeTransfered) throws RemoteException {
        try {
            System.out.println("Money withdraw request granted...");

            String query = "update accounts set balance = " + (getBalance(accId) - money) + " where accId = "
                    + accId + ";";
            statement.executeUpdate(query);
            if (!isToBeTransfered) {
                query = "insert into transactions (accId, receiverId, mode, amount, date, time) values("
                        + accId + ", " + accId + ", 'Withdraw', " + money
                        + ", curdate(), curtime());";
                statement.executeUpdate(query);
            }
            System.out.println("Money withdrawn successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void deposit(int accId, double money, boolean isTransfered) throws RemoteException {
        try {
            System.out.println("Money deposit request granted...");
            String query = "update accounts set balance = " + (getBalance(accId) + money) + " where accId = "
                    + accId + ";";
            statement.executeUpdate(query);
            if (!isTransfered) {
                query = "insert into transactions (accId, senderId, mode, amount, date, time) values("
                        + accId + ", " + accId + ", 'Deposit', " + money
                        + ", curdate(), curtime());";
                statement.executeUpdate(query);
            }
            System.out.println("Money deposited successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean transfer(int senderId, int receiverId, double money) throws RemoteException {
        if (getBalance(receiverId) != -1) {
            withdraw(senderId, money, true);
            deposit(receiverId, money, true);
            String query = "insert into transactions values("
                    + senderId + ", " + senderId + ", " + receiverId + ", 'Transfer', " + money
                    + ", curdate(), curtime());";
            try {
                statement.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        } else
            return false;
    }

    private double getBalance(int accId) {
        double balance = -1;
        try {

            String query = "select * from accounts where accId = " + accId + " limit 1;";
            ResultSet resultSet = statement.executeQuery(query);
            // connection.close();
            if (resultSet.next()) {
                balance = resultSet.getDouble("balance");
            } else
                System.out.println("Account does not exists: " + accId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    private String encoder(String text) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(text.getBytes());
    }

    // private String decoder(String text) {
    // Decoder decoder = Base64.getDecoder();
    // return new String(decoder.decode(text));
    // }
}
