package in.zapr.druid.druidry.client;

public class RuntimeIoException extends RuntimeException {
    protected RuntimeIoException() {

    }

    public RuntimeIoException(Exception cause) {
        super(cause);
    }

    public RuntimeIoException(String message, Exception cause) {
        super(message, cause);
    }
}
