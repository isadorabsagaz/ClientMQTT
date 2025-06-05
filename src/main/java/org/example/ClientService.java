package org.example;

import org.example.Threads.Rx;
import org.example.Threads.Tx;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

    public static final String HOST = "localhost";
    private static final int PORT = 5000;
    public static List<String> latestTopics = new ArrayList<>();
    public static volatile boolean topicsUpdated = false;

    public static void main(String[] args) {

        Socket socket = null;
        try {
            socket = new Socket(HOST, PORT);
            System.out.println("Connected to broker on " + HOST + ":" + PORT);

            new Thread(new Rx(socket.getInputStream())).start();
            new Thread(new Tx(socket.getOutputStream())).start();
        }
        catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            if (socket != null && socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }

    }
}