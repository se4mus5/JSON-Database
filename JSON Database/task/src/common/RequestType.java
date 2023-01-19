package common;

// irregular enum uses lowercase constants, which simplifies GSON serialization code significantly
// while adapting to the changing String representation requirements
public enum RequestType {
    set,
    get,
    delete,
    exit
}
