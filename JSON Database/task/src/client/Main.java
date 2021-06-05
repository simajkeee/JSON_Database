package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import common.Request;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {

    @Parameter(names="-t", description = "Type of request")
    private String requestType;

    @Parameter(names="-k", description = "Key in HashMap")
    private String keyToAccess;

    @Parameter(names="-v", description = "Value in HashMap")
    private String userData;

    @Parameter(names="-in", description = "File to parse request from")
    private String fileAsRequest;

    private boolean test = false;
    private static String address = "127.0.0.1";
    private static int port = 23456;
    private static String clientFileRequestPath = System.getProperty("user.dir") + "/src/client/data/";
    private static String clientFileRequestPathTest = System.getProperty("user.dir") + "/JSON Database/task/src/client/test/";
    public static void main(String[] args) {
        Main main = new Main();
        JCommander ttt = JCommander.newBuilder()
                .addObject(main)
                .build();
        ttt.parse(args);
        main.run();
    }

    public void run() {
        try(
            Socket socket = new Socket(InetAddress.getByName(address), port);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            Request requestBody = null;
            String fileRequestBody = "";

            if (null == fileAsRequest) {
                requestBody = new Request(requestType);
                if (keyToAccess != null) {
                    requestBody.setKey(keyToAccess);
                }
                if (userData != null) {
                    requestBody.setValue(userData);
                }
            } else {
                ReadWriteLock lock = new ReentrantReadWriteLock();
                Lock readLock = lock.readLock();
                readLock.lock();
                String path = test ? clientFileRequestPathTest : clientFileRequestPath;
                try(FileReader reader = new FileReader(path + fileAsRequest);) {
                    BufferedReader bReader = new BufferedReader(reader);
                    String currentLine = null;
                    while((currentLine = bReader.readLine()) != null) {
                        fileRequestBody += currentLine;
                    }
                    bReader.close();
                } catch (IOException e) {
                    e.getStackTrace();
                } finally {
                    readLock.unlock();
                }
            }
            String request = requestBody == null ? fileRequestBody : new Gson().toJson(requestBody);
            output.writeUTF(request);
            String receivedMsg = input.readUTF();
            System.out.println("Client started!");
            System.out.println("Sent: " + request);
            System.out.println("Received: " + receivedMsg);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }
}
