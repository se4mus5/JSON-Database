package server.controller;

import com.google.gson.Gson;
import common.Request;
import common.Response;
import common.ResponseType;
import server.model.Database;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerLogic {
    private final Database db;

    public ServerLogic() { db = new Database(); }

    public void serve() {
        System.out.println("Server started!");

        String address = "127.0.0.1";
        int port = 23456;
        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
            boolean terminating = false;
            while (true) {
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                String requestJson = input.readUTF();
                Request request = new Gson().fromJson(requestJson, Request.class);
                //System.out.println("## DIAG ## " + request);
                Response response;
                System.out.println("Received: " + requestJson);

                switch (request.getType()) {
                    case get -> response = serveGet(request);
                    case set -> response = serveSet(request);
                    case delete -> response = serveDelete(request);
                    case exit -> { response = serveExit(); terminating = true; }
                    default -> response = new Response(ResponseType.ERROR);
                }

                String responseJson = response.toJson();
                output.writeUTF(responseJson);
                System.out.println("Sent: " + responseJson);

                if (terminating) { // this exit logic is needed for test suite compliance
                    input.close();
                    output.close();
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response serveDelete(Request request) {
        return db.delete(request.getKey()) ? new Response(ResponseType.OK)
                : new Response(ResponseType.ERROR,  "No such key");
    }

    private Response serveSet(Request request) {
        return db.set(request.getKey(), request.getValue())
                ? new Response(ResponseType.OK) : new Response(ResponseType.ERROR);
    }

    private Response serveGet(Request request) {
        String value = db.get(request.getKey());
        return !value.equals("") ? new Response(ResponseType.OK, value, 1)
                : new Response(ResponseType.ERROR,  "No such key");
    }

    private Response serveExit() {
        return new Response(ResponseType.OK);
    }
}