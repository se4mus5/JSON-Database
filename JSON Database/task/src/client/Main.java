package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import client.common.Request;
import client.common.RequestType;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.stream.Collectors;

public class Main {
    @Parameter(names={"-t"})
    static String type;
    @Parameter(names={"-k"})
    static String key;
    @Parameter(names={"-v"})
    static String value;
    @Parameter(names={"-in"})
    static String fileName;

    static private final String address = "127.0.0.1";
    static private final int port = 23456;
    private static final String dataDir = System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "client" + File.separator +
            "data";

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
                String requestJson;
                if (fileName == null || fileName.isEmpty()) {
                    switch (type) {
                        case "get" -> request = new Request(RequestType.get, key);
                        case "delete" -> request = new Request(RequestType.delete, key);
                        case "set" -> request = new Request(RequestType.set, key, value);
                        // no need to explicitly terminate the client: CLI client exits after each command
                        case "exit" ->  request = new Request(RequestType.exit); // send request to server to shut down
                        default -> { return; } // TODO add error handling
                    }
                    requestJson = request.toJson();
                } else {
                    String fullyQualifiedDatafileName = dataDir + File.separator + fileName;
                    BufferedReader reader = new BufferedReader(new FileReader(fullyQualifiedDatafileName));
                    requestJson = reader.lines()
                            .collect(Collectors.joining(System.lineSeparator()));
                    reader.close();
                }

                output.writeUTF(requestJson);
                System.out.println("Sent: " + requestJson);

                String responseJson = input.readUTF();
                System.out.println("Received: " + responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
