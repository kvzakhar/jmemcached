package net.simplesoft.jmemcached.protocol.model;

import java.util.Date;

public class Request extends AbstractPackage {
    private final Command command;
    private String key;
    private Long ttl;

    public Request(Command command) {
        this.command = command;
    }

    public Request(Command command, String key, Long ttl, byte[] data) {
        super(data);
        this.command = command;
        this.key = key;
        this.ttl = ttl;
    }

    public Request(Command command, String key) {
        this.command = command;
        this.key = key;
    }

    public Command getCommand() {
        return command;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public boolean hasKey() {
        return this.key != null;
    }

    public boolean hasTTL() {
        return this.ttl != null;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(command.name());
        if (hasKey()) {
            sb.append('[').append(getKey()).append(']');
        }
        if (hasData()) {
            sb.append("=").append(getData().length).append(" bytes");
        }
        if (hasTTL()) {
            sb.append(" (").append(new Date(getTtl())).append(')');
        }

        return sb.toString();
    }
}
