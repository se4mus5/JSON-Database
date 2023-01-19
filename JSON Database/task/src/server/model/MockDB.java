package server.model;

public class MockDB {
    private final String[] strings;

    public MockDB() {
        strings = new String[1000];
    }

    public String get(int cellId) {
        if (cellId < 1 || cellId > strings.length) return "";
        return strings[cellId - 1] == null ? "" : strings[cellId - 1];
    }

    public boolean set(int cellId, String value) {
        if (cellId < 1 || cellId > strings.length) return false;
        strings[cellId - 1] = value;
        return true;
    }

    public boolean delete(int cellId) {
        if (cellId < 1 || cellId > strings.length) return false;
        strings[cellId - 1] = "";
        return true;
    }
}
