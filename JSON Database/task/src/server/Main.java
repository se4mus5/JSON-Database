package server;

import server.controller.ServerLogic;

//TODO add
// - test coverage (when requirements seem stable)
// - diagnostic logging (appropriate log level)
public class Main {
    public static void main(String[] args) {
        ServerLogic serverLogic = new ServerLogic();
        serverLogic.serve();
    }
}
