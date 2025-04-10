package org.example;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {

    public static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader keyboard = new BufferedReader(
                        new InputStreamReader(System.in)
                );
        ) {
            System.out.println("Connected to broker in " + HOST + ": " + PORT);

            //listening server
            new Thread(() -> {
                String line;
                try{
                    while ((line = in.readLine()) != null){
                        Message received = new Gson().fromJson(line, Message.class);

                        if ("MESSAGE".equalsIgnoreCase(received.type)) {
                            System.out.println("[Topic: "+received.topic+"] \n Message: " + received.payload);
                        }
                    }
                }catch (IOException e){
                    e.fillInStackTrace();
                }
            }).start();

            while (true) {
                System.out.println("Type 'exit' to quit");
                System.out.print("Type the command (SUBSCRIBE / PUBLISH): ");
                String type = keyboard.readLine();

                if ("exit".equalsIgnoreCase(type)) {
                    System.out.println("Goodbye!");
                    out.close();
                    break;
                }

                System.out.println("Type the topic: ");
                String topic = keyboard.readLine();

                String payload = null;
                if ("PUBLISH".equalsIgnoreCase(type)) {
                    System.out.println("Type the message: ");
                    payload = keyboard.readLine();
                }

                //generates message to send to server
                Message msg = new Message();
                msg.type = type;
                msg.topic = topic;
                msg.payload = payload;

                //sends it
                String json = new Gson().toJson(msg);
                out.println(json);
            }

        } catch (IOException e) {
            e.fillInStackTrace();
            throw new RuntimeException(e);
        }
    }
}