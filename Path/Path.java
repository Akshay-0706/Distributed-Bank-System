package Path;

import java.io.FileWriter;
import java.io.IOException;

public class Path {

    private static String current = System.getProperty("user.dir").replace("\\", "\\\\");

    // private static String parentOfCurrent = current.substring(0,
    // current.lastIndexOf("\\\\"));

    private static String client = current + "\\\\Client\\\\Client.java";

    private static String loadBalancer = current + "\\\\LoadBalancer\\\\LoadBalancer.java";
    private static String loadBalancerDriver = current + "\\\\LoadBalancer\\\\LoadBalancerDriver.java";

    private static String log = current + "\\\\Log\\\\Log.java";

    private static String server = current + "\\\\Server\\\\Server.java";
    private static String serverDriver = current + "\\\\Server\\\\ServerDriver.java";
    private static String serverCreater = current + "\\\\Server\\\\ServerCreater.java";

    private static String json = current + "\\\\json.jar";
    private static String mysql = current + "\\\\mysql.jar";

    private static String include = "-cp \"" + current + ";" + mysql + ";" + json + "\"";

    public static void main(String[] args) {
        try {
            FileWriter fileWriter = new FileWriter("include.argfile");
            fileWriter.write(include);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrent() {
        return current;
    }

    public static void setCurrent(String current) {
        Path.current = current;
    }

    // public static String getParentOfCurrent() {
    // return parentOfCurrent;
    // }

    // public static void setParentOfCurrent(String parentOfCurrent) {
    // Path.parentOfCurrent = parentOfCurrent;
    // }

    public static String getClient() {
        return client;
    }

    public static void setClient(String client) {
        Path.client = client;
    }

    public static String getLoadBalancer() {
        return loadBalancer;
    }

    public static void setLoadBalancer(String loadBalancer) {
        Path.loadBalancer = loadBalancer;
    }

    public static String getLoadBalancerDriver() {
        return loadBalancerDriver;
    }

    public static void setLoadBalancerDriver(String loadBalancerDriver) {
        Path.loadBalancerDriver = loadBalancerDriver;
    }

    public static String getLog() {
        return log;
    }

    public static void setLog(String log) {
        Path.log = log;
    }

    public static String getServer() {
        return server;
    }

    public static void setServer(String server) {
        Path.server = server;
    }

    public static String getServerDriver() {
        return serverDriver;
    }

    public static void setServerDriver(String serverDriver) {
        Path.serverDriver = serverDriver;
    }

    public static String getServerCreater() {
        return serverCreater;
    }

    public static void setServerCreater(String serverCreater) {
        Path.serverCreater = serverCreater;
    }

    public static String getJson() {
        return json;
    }

    public static void setJson(String json) {
        Path.json = json;
    }

    public static String getMysql() {
        return mysql;
    }

    public static void setMysql(String mysql) {
        Path.mysql = mysql;
    }
}