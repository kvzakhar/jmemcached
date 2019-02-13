package net.simplesoft.jmemcached.protocol.model;

public class Response extends AbstractPackage {
    private final Status status;

    public Response(Status status, byte[] data) {
        super(data);
        this.status = status;
    }

    public Response(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(status.name());
        if (hasData()) {
            sb.append(" [").append(getData().length).append(" bytes]");
        }
        return sb.toString();
    }
}
