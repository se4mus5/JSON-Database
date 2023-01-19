package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import common.Request;
import common.RequestType;
import common.Response;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    @Parameter(names={"-t"})
    static String command;
    @Parameter(names={"-i"})
    static int cellId;
    @Parameter(names={"-m"})
    static String payload;

    public static void main(String[] args) {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        // Scanner scan = new Scanner(System.in);
        System.out.println("Client started!");
        String address = "127.0.0.1";
        int port = 23456;
        try (Socket socket = new Socket(InetAddress.getByName(address), port)) {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                Request request;
                switch (command) {
                    case "get" -> request = new Request(RequestType.GET, cellId);
                    case "delete" -> request = new Request(RequestType.DELETE, cellId);
                    case "set" -> {
                        request = new Request(RequestType.SET, cellId, payload);
                    }
                    // no need to explicitly terminate the client: CLI client exits after each command
                    case "exit" ->  request = new Request(RequestType.EXIT);
                    default -> { return; }
                }

                output.writeObject(request);
                System.out.println("Sent: " + request);

                Response response = (Response) input.readObject();
                System.out.println("Received: " + response);
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
