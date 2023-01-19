package server;

import server.controller.ServerLogic;

// TODO test coverage (when requirements seem stable)
public class Main {
    public static void main(String[] args) {
        ServerLogic serverLogic = new ServerLogic();
        serverLogic.serve();
    }
}
