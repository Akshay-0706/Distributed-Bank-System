package Bank.Server;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

import java.util.Base64;
import java.util.Random;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import Bank.Account.Account;

public class Server extends UnicastRemoteObject implements ServerIF {

    private String className = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/bank";
    private String username = "sqluser";
    private String password = "password";
    Connection connection;
    Statement statement;

    public Server() throws RemoteException {
        super();
        try {
            Class.forName(className);
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void notifyWorkDone() {
        try {
            connection.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Account account = new Account(45634764, "Akshay", "1234", 1000);
            Server server = new Server();
            // server.createAccount(account);
            // server.loginAccount(account.getAccId(), account.getPassword());
            // server.withdraw(account.getAccId(), 100);
            // server.deposit(account.getAccId(), 200);
            server.transfer(45634764, 45634664, 200);
            System.out.println("Hello");
            server.notifyWorkDone();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int createAccount(Account account) throws RemoteException {
        Random random = new Random();
        try {
            System.out.println("Account creation request granted...");

            String query = "insert into accounts values (" + account.getAccId() + ", '" + account.getUsername() + "', '"
                    + encoder(account.getPassword())
                    + "', "
                    + account.getBalance() + ");";
            statement.executeUpdate(query);
            System.out.println("Account created successfully!");
        } catch (SQLException e) {
            System.out.println("Account ID already exists, creating another one...");
            account.setAccId(random.nextInt(900000000) + 100000000);
            return createAccount(account);
        }
        return account.getAccId();
    }

    @Override
    public Account loginAccount(int accId, String password) throws RemoteException {
        Account account = null;
        try {
            System.out.println("Account login request granted...");

            String query = "select * from accounts where accId = '" + accId + "' and password = '" + encoder(password)
                    + "' limit 1;";
            ResultSet resultSet = statement.executeQuery(query);
            // connection.close();
            if (resultSet.next()) {
                account = new Account(accId, resultSet.getString("name"), password, resultSet.getDouble("balance"));
                System.out.println("Logged in successfully!");
            } else
                System.out.println("Account does not exists!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }

    @Override
    public void deleteAccount(int accId, String password) throws RemoteException {
        if (loginAccount(accId, password) != null) {
            try {
                System.out.println("Account deletion request granted...");

                String query = "delete from clients where accId = " + accId + ";";
                statement.executeUpdate(query);
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
    public void transfer(int senderId, int receiverId, double money) throws RemoteException {
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
    }

    public double getBalance(int accId) throws RemoteException {
        double balance = 0;
        try {

            String query = "select * from accounts where accId = " + accId + " limit 1;";
            ResultSet resultSet = statement.executeQuery(query);
            // connection.close();
            if (resultSet.next()) {
                balance = resultSet.getDouble("balance");
            } else
                System.out.println("Problem in fetching balance from account with id: " + accId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    private String encoder(String text) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(text.getBytes());
    }

    private String decoder(String text) {
        Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(text));
    }

}
