package in.zapr.druid.druidry.client;

public class DruidException extends RuntimeIoException {
    private final DruidError error;

    public DruidException(DruidError error) {
        this.error = error;
    }

    public DruidError getError() {
        return error;
    }

    @Override
    public String toString() {
        return error.toString();
    }
}
