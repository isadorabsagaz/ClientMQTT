package org.example;

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
            System.out.println("Connected to broker in " + HOST + ":" + PORT);

            while (true) {
                System.out.println("Type a message: ");
                String input = keyboard.readLine();

                if (input.equalsIgnoreCase("exit")) break;

                out.println(input);
                String response = in.readLine();
                System.out.println("Broker response: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}