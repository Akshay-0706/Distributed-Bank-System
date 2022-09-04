package Log;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Log {

    private static JSONObject readLog() {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;

        try (FileReader fileReader = new FileReader("log/log.json")) {
            jsonObject = (JSONObject) jsonParser.parse(fileReader);
            return jsonObject;

        } catch (IOException | ParseException e) {

        }

        return jsonObject == null ? new JSONObject() : jsonObject;
    }

    private static void writeLog(JSONObject log) {
        try (FileWriter fileWriter = new FileWriter("log/log.json")) {
            fileWriter.write(log.toJSONString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject serverAdderLog(int port) {
        JSONObject jsonObject = readLog();

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("Server", new JSONObject());
        jsonObject2.put("Client", new JSONObject());
        jsonObject.put(String.valueOf(port), jsonObject2);
        writeLog(jsonObject);
        return jsonObject2;
    }

    private static void clientAdderLog(int port, int accId) {
        JSONObject jsonObject = readLog();
        JSONObject jsonObject2 = (JSONObject) jsonObject.get(String.valueOf(port));
        JSONObject jsonObject3 = (JSONObject) jsonObject.get("Client");
        if (jsonObject3 == null)
            jsonObject3 = new JSONObject();
        HashMap<Long, String> log = new HashMap<Long, String>();
        jsonObject3.put(String.valueOf(accId), log);
        jsonObject2.put("Client", jsonObject3);
        jsonObject.put(String.valueOf(port), jsonObject2);
        writeLog(jsonObject);
    }

    public static void serverLog(int port, String message) {
        JSONObject jsonObject = readLog();
        JSONObject jsonObject2 = (JSONObject) jsonObject.get(String.valueOf(port));
        if (jsonObject2 == null) {
            jsonObject2 = serverAdderLog(port);
        }

        HashMap<Long, String> log = (HashMap<Long, String>) jsonObject2.get("Server");
        if (log == null)
            log = new HashMap<Long, String>();
        log.put(System.currentTimeMillis(), message);
        jsonObject2.put("Server", log);
        jsonObject.put(String.valueOf(port), jsonObject2);
        writeLog(jsonObject);
    }

    public static void clientLog(int port, int accId, String message) {
        JSONObject jsonObject = readLog();
        JSONObject jsonObject2 = (JSONObject) jsonObject.get(String.valueOf(port));

        if (jsonObject2 == null) {
            System.out.println("Attempt to write without connecting to server!");
            return;
        }
        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(String.valueOf("Client"));
        
        HashMap<Long, String> log = (HashMap<Long, String>) jsonObject3.get(String.valueOf(accId));
        if (log == null) {
            clientAdderLog(port, accId);
            log = new HashMap<Long, String>();
        }
        log.put(System.currentTimeMillis(), message);
        jsonObject3.put(String.valueOf(accId), log);
        jsonObject2.put("Client", jsonObject3);
        jsonObject.put(String.valueOf(port), jsonObject2);
        writeLog(jsonObject);
    }

    public static void main(String[] args) {
        serverLog(4345, "Server online");
        clientLog(4345, 453453, "New Account request");

    }
}
