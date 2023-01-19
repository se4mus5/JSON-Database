package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import common.Request;
import common.RequestType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Main {
    @Parameter(names={"-t"})
    static String command;
    @Parameter(names={"-k"})
    static String key;
    @Parameter(names={"-v"})
    static String value;

    public static void main(String[] args) {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        System.out.println("Client started!");
        String address = "127.0.0.1";
        int port = 23456;
        try (Socket socket = new Socket(InetAddress.getByName(address), port)) {
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());

                Request request;
                switch (command) {
                    case "get" -> request = new Request(RequestType.get, key);
                    case "delete" -> request = new Request(RequestType.delete, key);
                    case "set" -> {
                        request = new Request(RequestType.set, key, value);
                    }
                    // no need to explicitly terminate the client: CLI client exits after each command
                    case "exit" ->  request = new Request(RequestType.exit);
                    default -> { return; }
                }

                String requestJson = request.toJson();
                output.writeUTF(requestJson);
                System.out.println("Sent: " + requestJson);

                String responseJson = input.readUTF();
                System.out.println("Received: " + responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
