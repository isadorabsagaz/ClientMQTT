package org.example.Threads;

import com.google.gson.Gson;
import org.example.ClientService;
import org.example.Models.Message;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Tx implements Runnable {
    private final PrintWriter writer;
    private final Scanner scanner;

    public Tx(OutputStream out) {
        this.writer = new PrintWriter(out, true);
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (true) {
            showMenu();
            String type = scanner.nextLine().trim();

            switch (type) {
                case "1" -> handleSubscribe();
                case "2" -> handlePublish();
                case "3" -> {
                    System.out.println("Goodbye!");
                    writer.close();
                    return;
                }
                default -> System.out.println("Invalid command. Try again.");
            }
        }
    }
    private void showMenu() {
        System.out.println("\n== MENU ==");
        System.out.println("1. SUBSCRIBE - Subscribe to a topic");
        System.out.println("2. PUBLISH   - Send a message to a topic");
        System.out.println("3. EXIT      - Disconnect");
        System.out.print("> ");
    }

    private void handleSubscribe() {
       List<String> topics = requestAndSelectTopic("LIST_ALL_TOPICS", "SUBSCRIBE");
       if (topics == null) return;

       String topic = chooseTopic("LIST_ALL_TOPICS", topics, "SUBSCRIBE");
       if (topic == null) return;

       send(createMessage("SUBSCRIBE", topic, null));
    }

    private void handlePublish() {
        List<String> topics = requestAndSelectTopic("LIST_MY_TOPICS", "PUBLISH");
        if (topics == null) return;

        String topic = chooseTopic("LIST_MY_TOPICS", topics, "PUBLISH");
        if (topic == null) return;

        System.out.println("[Topic]: " + topic);
        System.out.println("Enter message to publish: ");
        String payload = scanner.nextLine().trim();

         send(createMessage("PUBLISH", topic, payload));
    }

    private List<String> requestAndSelectTopic(String type, String action){
        requestTopics(type);
        pause();

        List<String> topics = new ArrayList<>(ClientService.latestTopics);
        String topic = "";

        if (topics.isEmpty()) {
            System.out.println("No topics available yet. Create a new one to "+action+": ");
            topic = scanner.nextLine().trim();
            if (topic.isEmpty()) {
                System.out.println("Topic cannot be empty. Try again.");
                return null;
            }
            ClientService.latestTopics.add(topic);
        }
        return topics;
    }

    private String chooseTopic(String type, List<String> topics, String action) {
        requestTopics(type);
        System.out.print("Select the topic number to "+action+" or type -1 to create a new one: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index == -1) {
            System.out.println("Create a new topic: ");
            String topic = scanner.nextLine().trim();
            if (topic.isEmpty()) {
                System.out.println("Topic cannot be empty. Try again.");
                return null;
            }
            return topic;
        }

        if (index >= 0 && index < topics.size()) {
            return topics.get(index);
        }

        System.out.println("Invalid number. Try again.");
        return null;
    }

    private Message createMessage(String type, String topic, String payload) {
        Message msg = new Message();
        msg.type = type;
        msg.topic = topic;
        msg.payload = payload;
        msg.date = LocalDate.now().toString();
        msg.time = LocalTime.now().toString();
        return msg;
    }

    private void send(Message msg) {
        String json = new Gson().toJson(msg);
        writer.println(json);
        System.out.println("Message sent: " + json);
    }

    private void requestTopics(String type) {
        send(createMessage(type, null, null));
        ClientService.topicsUpdated = false;
    }

    private void pause() {
        int attempts = 0;

        while (!ClientService.topicsUpdated && attempts < 20) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            attempts++;
        }
    }
}
