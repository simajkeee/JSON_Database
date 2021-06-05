package server;

import com.google.gson.*;
import common.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataBaseHandler implements Runnable {
    private Socket socket;
    private String dis;
    private DataOutputStream dos;
    private static String dbPath = System.getProperty("user.dir") + "/src/server/data/db.json";
    private static File dbFile = new File(dbPath);
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    DataBaseHandler(Socket socket, String dis, DataOutputStream dos) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        try {
            Request req = new RequestParser(dis).getRequest();
            String dbResponse = getDbResponse(req);
            dos.writeUTF(dbResponse);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }


    private String getDbResponse(Request req) {
        DbResponse r = null;
        switch (req.getType()) {
            case "get":
                if (req.isMultiLevel()) {
                    String[] multilevelKeys = req.getAllLevels();
                    r = getMultiLevel(multilevelKeys);
                } else {
                    r = get(req.getKey());
                }
                break;
            case "set":
                if (req.isMultiLevel()) {
                    String[] multilevelKeys = req.getAllLevels();
                    r = setMultiLevel(multilevelKeys, req.getOrigValue());
                } else {
                    String userReqValue = req.getOrigValue();
                    JsonElement arg = (!isValidJson(userReqValue)) ? new JsonPrimitive(userReqValue) : new JSONParser(userReqValue).getJobject();
                    r = set(req.getKey(), arg);
                }
                break;
            case "delete":
                if (req.isMultiLevel()) {
                    String[] multilevelKeys = req.getAllLevels();
                    r = deleteMultiLevel(multilevelKeys);
                } else {
                    r = delete(req.getKey());
                }
                break;
        }
        return new Gson().toJson(r);
    }


    public DbResponse deleteMultiLevel(String[] keys) {
        JSONParser parser = new JSONParser(getDb());

        if (!parser.deleteInDepthValue(keys)) {
            return getErrorKeyResponse();
        }
        simpleWriteToFile(new Gson().toJson(parser.getJobject()));
        return getOkResponse();
    }

    public DbResponse delete(String key) {
        JSONParser parser = new JSONParser(getDb());

        if (!parser.getJobject().has(key)) {
            return getErrorKeyResponse();
        }

        parser.removeJsonObjectKey(parser.getJobject(), key);
        simpleWriteToFile(new Gson().toJson(parser.getJobject()));
        return getOkResponse();
    }

    public DbResponse getMultiLevel(String[] keys) {
        JSONParser parser = new JSONParser(getDb());
        JsonElement foundVal = parser.getInDepthValue(keys);
        if (foundVal == null) {
            getErrorKeyResponse();
        }
        return getOkResponseWithValue(foundVal);
    }

    public DbResponse get(String key) {
        JsonObject db = new JSONParser(getDb()).getJobject();
        if (!db.has(key)) {
            return getErrorKeyResponse();
        }

        return getOkResponseWithValue(db.get(key));
    }

    public DbResponse setMultiLevel(String[] keys, String val) {
        String initialKey = keys[0];
        JsonObject db = getDbJsonEl();
        JsonElement toSet = null;
        if (db.has(initialKey)) {
            toSet = db.get(initialKey);
        } else {
            toSet = JSONParser.getMultiLevelJson(Arrays.copyOfRange(keys, 1, keys.length)); //creates new JsonElement
        }

        JSONParser parser = new JSONParser(toSet);
        parser.setInDepthValue(Arrays.copyOfRange(keys, 1, keys.length), val);

        set(initialKey, parser.getJobject());
        return getOkResponse();
    }

    public DbResponse set(String key, JsonElement el) {
        String result = getDb();
        JsonObject db = new JSONParser(result).getJobject();
        if (db == null) {
            db = new JsonObject();
        }
        db.add(key, el);
        simpleWriteToFile(db.toString());
        return getOkResponse();
    }
    public JsonObject getDbJsonEl() {
        return new JSONParser(getDb()).getJobject();
    }

    public String getDb() {
        readLock.lock();
        String result = "";
        try(FileReader reader = new FileReader(dbFile);) {
            BufferedReader bReader = new BufferedReader(reader);
            String currentLine = null;
            while((currentLine = bReader.readLine()) != null) {
                result += currentLine;
            }
            bReader.close();
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public void simpleWriteToFile(String str) {
        writeLock.lock();
        try (FileWriter writer = new FileWriter(dbFile)) {
            writer.write(str);
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    public static boolean isValidJson(String json) {
        try {
            return JsonParser.parseString(json) != null;
        } catch (Throwable ignored) {}
        return false;
    }

    public static String getDbPath() {
        return dbPath;
    }

    private void clearDb() {
        writeLock.lock();
        try {
            new PrintWriter(DataBaseHandler.getDbPath()).close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    private DbResponse getOkResponseWithValue(JsonElement v) {
        DbResponse r = getOkResponse();
        if (v != null) {
            r.setValue(v);
        }
        return r;
    }

    private DbResponse getOkResponse() {
        DbResponse r = new DbResponse();
        r.setResponse(DbResponse.okMsg);
        return r;
    }

    private DbResponse getErrorKeyResponse() {
        DbResponse r = new DbResponse();
        r.setResponse(DbResponse.errMsg);
        r.setReason("No such key");
        return r;
    }
}
