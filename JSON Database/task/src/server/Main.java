package server;

import server.controller.ServerLogic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

//TODO
// - re-enable and add more logging
// - add persistence across shutdowns
// - add test coverage
public class Main {
    private static final String LOGGING_PROPERTY_FILE = "logging.properties";
    public static final Logger logger = Logger.getLogger("");

    static {
        //initialize server-side logging
        //this interferes with the Hyperskill tests, disable if you need to run those
        final InputStream inputStream = Main.class.getResourceAsStream(File.separator + LOGGING_PROPERTY_FILE);
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (final IOException e) {
            Logger.getAnonymousLogger().severe(String.format("Could not load default %s file", LOGGING_PROPERTY_FILE));
            Logger.getAnonymousLogger().severe(e.getMessage());
        }
    }
    public static void main(String[] args) {
        try {
            ServerLogic serverLogic = new ServerLogic();
            serverLogic.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
