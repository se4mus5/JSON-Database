package client.common;

// irregular enum naming: uses lowercase constants to simplify GSON serialization code
public enum RequestType {
    set,
    get,
    delete,
    exit
}
