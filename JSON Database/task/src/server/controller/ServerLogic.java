package server.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import server.common.Response;
import server.common.ResponseType;
import server.model.Database;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static server.Main.logger;

public class ServerLogic {
    private final Database db;
    private final ExecutorService executorService;
    private final String address = "127.0.0.1";
    private final int port = 23456;
    private static final String fileName = "db.json";
    private static final String dataDir = System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "server" + File.separator +
            "data";
    private static final String fullyQualifiedDatafileName = dataDir + File.separator + fileName;
    private final File file;

    public ServerLogic() throws IOException {
        db = new Database();
        executorService = Executors.newSingleThreadExecutor();
        new File(dataDir).mkdirs();
        file = new File(fullyQualifiedDatafileName);
    }

    public void serve() {
        try (ServerSocket serverSocket = new ServerSocket(port, 63, InetAddress.getByName(address))) {
            logger.log(Level.INFO, "========================== Jason Database Server started ==========================");
            System.out.println("Server started!");
            while (true) {
                Thread.sleep(5); // TODO remove this wait if stage tests pass
                executorService.submit(() -> {
                    try {
                        Socket socket = serverSocket.accept();
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                        String requestAsJsonString = input.readUTF();
                        JsonElement requestAsJsonElement = JsonParser.parseString(requestAsJsonString);
                        JsonObject requestAsJsonObject = requestAsJsonElement.getAsJsonObject();

                        Response response;
                        System.out.println("Received: " + requestAsJsonString);

                        boolean terminating = false;
                        switch (requestAsJsonObject.get("type").getAsString()) {
                            case "get" -> response = serveGet(requestAsJsonObject);
                            case "set" -> response = serveSet(requestAsJsonObject);
                            case "delete" -> response = serveDelete(requestAsJsonObject);
                            case "exit" -> {
                                response = serveExit();
                                terminating = true;
                            }
                            default -> response = new Response(ResponseType.ERROR);
                        }

                        String responseJson = response.toJson();
                        output.writeUTF(responseJson);

                        System.out.println("Sent: " + responseJson);
                        input.close();
                        output.close();

                        if (terminating == true) { // this exit logic is needed for test suite compliance
                            executorService.shutdownNow();
                            System.exit(0);
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    }});
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // NOP
        }
    }

    private synchronized Response serveDelete(JsonObject requestAsJsonObject) throws IOException {
        Response response = db.delete(requestAsJsonObject.get("key")) ? new Response(ResponseType.OK)
                : new Response(ResponseType.ERROR,  "No such key");
        exportDBToFile();
        return response;
    }

    private synchronized Response serveSet(JsonObject requestAsJsonObject) throws IOException {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        logger.log(Level.FINE, String.format("## DIAG ## Request: %s", requestAsJsonObject));

        Response response = db.set(requestAsJsonObject.get("key"), requestAsJsonObject.get("value"))
                ? new Response(ResponseType.OK) : new Response(ResponseType.ERROR);
        exportDBToFile();
        return response;
    }

    private synchronized Response serveGet(JsonObject requestAsJsonObject) {
        JsonElement value = db.get(requestAsJsonObject.get("key"));

        return value == null ? new Response(ResponseType.ERROR,  "No such key")
                : new Response(ResponseType.OK, value, 1);
    }

    private synchronized Response serveExit() {
        return new Response(ResponseType.OK);
    }

    private synchronized void exportDBToFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.append(db.getDbExport());
        writer.close();
    }
}