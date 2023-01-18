package server.misc;

public class MockDB {
    private String[] strings;

    public MockDB() {
        strings = new String[100];
    }

    public String get(int cellId) {
        if (strings[cellId - 1].isEmpty())
            throw new RuntimeException();
        else
            return strings[cellId - 1];
    }

    public void set(int cellId, String value) {
        strings[cellId - 1] = value;
    }

    public void delete(int cellId) {
        strings[cellId - 1] = "";
    }
}
