package server.ui;

import server.misc.MockDB;

import java.util.Arrays;
import java.util.Scanner;

public class TextUserInterface {
    private final Scanner scan;
    private final MockDB mockDB;

    public TextUserInterface() {
        scan = new Scanner(System.in);
        mockDB = new MockDB();
    }

    public void start() {
        // TODO refactor commands to enums
        while (true) {
            String[] commandParts = scan.nextLine().strip().split(" ");
            String command = commandParts[0];
            if (command.equals("exit")) break;
            int cellId = Integer.parseInt(commandParts[1]);
            try {
                switch (command) {
                    case "get" -> System.out.println(mockDB.get(cellId));
                    case "delete" -> mockDB.delete(cellId);
                    case "set" -> {
                        String value = String.join(" ", Arrays.copyOfRange(commandParts, 2, commandParts.length));
                        mockDB.set(cellId, value);
                    }
                }
                System.out.println("OK");
            } catch (RuntimeException e) {
                System.out.println("ERROR");
            }
        }
    }
}
