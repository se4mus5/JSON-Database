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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerLogic {
    private final Database db;
    private final ExecutorService executorService;
    private final String address = "127.0.0.1";
    private final int port = 23456;
    private final String dataDir = "./src/server/data";
    private final String dataFileName = "db.json";

    public ServerLogic() {
        db = new Database();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void serve() {
        new File(dataDir).mkdirs();
        File file = new File(dataDir + '/' + dataFileName);

        ReadWriteLock lock = new ReentrantReadWriteLock();
        Lock readLock = lock.readLock();
        Lock writeLock = lock.writeLock();

        try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            System.out.println("Server started!");
            while (true) {
                Thread.sleep(25);
                executorService.submit(() -> {
                try {
                    Socket socket = serverSocket.accept();
                    //System.out.println("## DIAG ## new client connection accepted.");
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                    String requestJson = input.readUTF();
                    Request request = new Gson().fromJson(requestJson, Request.class);
                    //System.out.println("## DIAG ## " + request);
                    Response response;
                    System.out.println("Received: " + requestJson);

                    ThreadLocal<Boolean> terminating = new ThreadLocal<>();
                    terminating.set(false);

                    // JSONs are written to db file as events, will use this to reconstruct in-memory map source-of-truth
                    switch (request.getType()) {
                        case get -> {
                            readLock.lock();
                            response = serveGet(request);
                            readLock.unlock();
                        }
                        case set -> {
                            writeLock.lock();
                            response = serveSet(request);
                            writer.write(requestJson);
                            writer.newLine();
                            writeLock.unlock();
                        }
                        case delete -> {
                            writeLock.lock();
                            response = serveDelete(request);
                            writer.write(requestJson);
                            writer.newLine();
                            writeLock.unlock();
                        }
                        case exit -> {
                            response = serveExit();
                            terminating.set(true);
                        }
                        default -> response = new Response(ResponseType.ERROR);
                    }

                    String responseJson = response.toJson();
                    output.writeUTF(responseJson);

                    System.out.println("Sent: " + responseJson);
                    input.close();
                    output.close();
                    writer.flush();

                    if (terminating.get().booleanValue() == true) { // this exit logic is needed for test suite compliance
                        writer.close();
                        //executorService.shutdownNow();
                        System.exit(0);
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // do nothing! - kept separate from IOException for different desired behavior
        }
    }

    private synchronized Response serveDelete(Request request) {
        return db.delete(request.getKey()) ? new Response(ResponseType.OK)
                : new Response(ResponseType.ERROR,  "No such key");
    }

    private synchronized Response serveSet(Request request) {
        return db.set(request.getKey(), request.getValue())
                ? new Response(ResponseType.OK) : new Response(ResponseType.ERROR);
    }

    private synchronized Response serveGet(Request request) {
        String value = db.get(request.getKey());
        return !value.equals("") ? new Response(ResponseType.OK, value, 1)
                : new Response(ResponseType.ERROR,  "No such key");
    }

    private synchronized Response serveExit() {
        return new Response(ResponseType.OK);
    }
}