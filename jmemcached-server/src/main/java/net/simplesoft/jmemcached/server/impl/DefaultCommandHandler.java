package net.simplesoft.jmemcached.server.impl;

import net.simplesoft.jmemcached.exception.JMemcachedException;
import net.simplesoft.jmemcached.protocol.model.Command;
import net.simplesoft.jmemcached.protocol.model.Request;
import net.simplesoft.jmemcached.protocol.model.Response;
import net.simplesoft.jmemcached.protocol.model.Status;
import net.simplesoft.jmemcached.server.CommandHandler;
import net.simplesoft.jmemcached.server.ServerConfig;
import net.simplesoft.jmemcached.server.Storage;

public class DefaultCommandHandler implements CommandHandler {

    private final Storage storage;

    DefaultCommandHandler(ServerConfig serverConfig) {
        this.storage = serverConfig.getStorage();
    }

    @Override
    public Response handle(Request request) {
        Status status;
        byte[] data = null;
        if (request.getCommand() == Command.CLEAR) {
            status = this.storage.clear();
        } else if (request.getCommand() == Command.PUT) {
            status = this.storage.put(request.getKey(), request.getTtl(), request.getData());
        } else if (request.getCommand() == Command.REMOVE) {
            status = this.storage.remove(request.getKey());
        } else if (request.getCommand() == Command.GET) {
            data = this.storage.get(request.getKey());
            status = data == null ? Status.NOT_FOUND : Status.GOTTEN;
        } else {
            throw new JMemcachedException("Unsupported command: " + request.getCommand());
        }

        return new Response(status, data);
    }
}
