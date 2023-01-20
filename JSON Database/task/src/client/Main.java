package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import common.Request;
import common.RequestType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import com.google.gson.Gson;

public class Main {
    @Parameter(names={"-t"})
    static String command;
    @Parameter(names={"-k"})
    static String key;
    @Parameter(names={"-v"})
    static String value;
    @Parameter(names={"-in"})
    static String fileName;

    static private final String address = "127.0.0.1";
    static private final int port = 23456;
    static private final String dataDir = "./src/client/data";

    public static void main(String[] args) {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        System.out.println("Client started!");
        new File(dataDir).mkdirs();
        try (Socket socket = new Socket(InetAddress.getByName(address), port)) {
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());

                Request request;
                if (fileName == null || fileName.isEmpty()) {
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
                } else {
                    BufferedReader reader = new BufferedReader(new FileReader(dataDir + "/" + fileName));
                    String requestJson = reader.readLine().trim();
                    request = new Gson().fromJson(requestJson, Request.class);
                    reader.close();
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
