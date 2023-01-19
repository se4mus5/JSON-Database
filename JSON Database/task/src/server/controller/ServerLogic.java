package server.controller;

import common.Request;
import common.Response;
import common.ResponseStatus;
import server.model.MockDB;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerLogic {
    private final MockDB mockDB;

    public ServerLogic() { mockDB = new MockDB(); }

    public void serve() {
        System.out.println("Server started!");

        String address = "127.0.0.1";
        int port = 23456;
        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
            while (true) {
                Socket socket = server.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                Request request = (Request) input.readObject();
                Response response;
                System.out.println("Received: " + request);

                switch (request.getRequestType()) {
                    case GET -> response = serveGet(request);
                    case SET -> response = serveSet(request);
                    case DELETE -> response = serveDelete(request);
                    case EXIT -> { response = serveExit();
                        input.close();
                        output.close();
                        System.exit(0); }
                    default -> response = new Response(ResponseStatus.ERROR);
                }

                output.writeObject(response);
                System.out.println("Sent: " + response);
            }
        } catch (IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Response serveDelete(Request request) {
        return mockDB.delete(request.getCellId()) ? new Response(ResponseStatus.OK) : new Response(ResponseStatus.ERROR);
    }

    private Response serveSet(Request request) {
        return mockDB.set(request.getCellId(), request.getPayload()) ? new Response(ResponseStatus.OK) : new Response(ResponseStatus.ERROR);
    }

    private Response serveGet(Request request) {
        String payload = mockDB.get(request.getCellId());
        return !payload.equals("") ? new Response(payload) : new Response(ResponseStatus.ERROR);
    }

    private Response serveExit() {
        return new Response(ResponseStatus.OK);
    }
}