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

    private static JSONObject serverAdderLog(int port, long instance) {
        JSONObject jsonObject = readLog();

        JSONObject jsonObject2 = new JSONObject();

        JSONObject jsonObject3 = new JSONObject();

        jsonObject3.put("Server", new JSONObject());

        jsonObject3.put("Client", new JSONObject());

        jsonObject2.put(String.valueOf(instance), jsonObject3);

        jsonObject.put(String.valueOf(port), jsonObject2);

        writeLog(jsonObject);

        return jsonObject2;
    }

    private static void clientAdderLog(int port, long instance, int accId) {
        JSONObject jsonObject = readLog();

        JSONObject jsonObject2 = (JSONObject) jsonObject.get(String.valueOf(port));

        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(String.valueOf(instance));

        JSONObject jsonObject4 = (JSONObject) jsonObject3.get("Client");
        if (jsonObject4 == null)
            jsonObject4 = new JSONObject();
        HashMap<Long, String> log = new HashMap<Long, String>();

        jsonObject4.put(String.valueOf(accId), log);

        jsonObject3.put("Client", jsonObject4);

        jsonObject2.put(String.valueOf(instance), jsonObject3);

        jsonObject.put(String.valueOf(port), jsonObject2);
        writeLog(jsonObject);
    }

    public static void serverLog(int port, int time, long instance, String message) {
        JSONObject jsonObject = readLog();
        JSONObject jsonObject2 = (JSONObject) jsonObject.get(String.valueOf(port));

        if (jsonObject2 == null) {
            jsonObject2 = serverAdderLog(port, instance);
        }

        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(String.valueOf(instance));
        if (jsonObject3 == null) {
            jsonObject3 = new JSONObject();
            jsonObject3.put("Server", new JSONObject());
            jsonObject3.put("Client", new JSONObject());
        }

        HashMap<Integer, String> log = (HashMap<Integer, String>) jsonObject3.get("Server");
        if (log == null)
            log = new HashMap<Integer, String>();

        log.put(time, message);
        jsonObject3.put("Server", log);
        jsonObject2.put(String.valueOf(instance), jsonObject3);
        jsonObject.put(String.valueOf(port), jsonObject2);
        writeLog(jsonObject);
    }

    public static void clientLog(int port, int time, long instance, int accId, String message) {
        JSONObject jsonObject = readLog();
        JSONObject jsonObject2 = (JSONObject) jsonObject.get(String.valueOf(port));

        if (jsonObject2 == null) {
            System.out.println("Attempt to write without connecting to server!");
            return;
        }

        JSONObject jsonObject3 = (JSONObject) jsonObject2.get(String.valueOf(instance));

        if (jsonObject3 == null) {
            System.out.println("Attempt to write without adding instance!");
            return;
        }

        JSONObject jsonObject4 = (JSONObject) jsonObject3.get(String.valueOf("Client"));

        HashMap<Integer, String> log = (HashMap<Integer, String>) jsonObject4.get(String.valueOf(accId));

        if (log == null) {
            clientAdderLog(port, instance, accId);
            log = new HashMap<Integer, String>();
        }

        log.put(time, message);
        jsonObject4.put(String.valueOf(accId), log);
        jsonObject3.put("Client", jsonObject4);
        jsonObject2.put(String.valueOf(instance), jsonObject3);
        jsonObject.put(String.valueOf(port), jsonObject2);
        writeLog(jsonObject);
    }

    public static void main(String[] args) {
        // long instance = System.currentTimeMillis();
        // serverLog(4345, 50, instance, "Server online");
        // clientLog(4345, 100, instance, 453453, "New Account request");

    }
}
