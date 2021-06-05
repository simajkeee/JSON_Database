package server;

import com.google.gson.Gson;
import common.DbResponse;
import common.Request;
import common.RequestParser;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static int poolSize              = Runtime.getRuntime().availableProcessors();
    private static String address            = "127.0.0.1";
    private static int port                  = 23456;
    public static volatile boolean end;
    private ExecutorService executor  = Executors.newFixedThreadPool(poolSize);
    private ServerSocket serverSocket;
    private Socket clientSocket;

    public static void main(String[] args) {
//        try {
//            new PrintWriter(DataBaseHandler.getDbPath()).close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        Main server = new Main();
        server.init();
    }

    private void init() {
        startSocket();
    }

    private void startSocket() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
            System.out.println("Server started!");
            Main.end = false;
            while(!end) {
                try {
                    clientSocket = serverSocket.accept();
                    DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                    String msgInput = input.readUTF();
                    if (msgInput.equals("")) {
                        continue;
                    }
                    if (isExit(msgInput)) {
                        DbResponse r = new DbResponse();
                        r.setResponse(DbResponse.okMsg);
                        output.writeUTF(new Gson().toJson(r));
                        end = true;
                        continue;
                    }
                    DataBaseHandler dbh = new DataBaseHandler(clientSocket, msgInput, output);
                    executor.submit(dbh);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutDown();
        }
    }

    public void shutDownClientSocket() {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void shutDownServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        executor.shutdownNow();
        shutDownClientSocket();
        shutDownServerSocket();
    }

    private static boolean isExit(String signal) {
        Request r = new RequestParser(signal).getRequest();
        return r.getType().equals("exit");
    }
}
