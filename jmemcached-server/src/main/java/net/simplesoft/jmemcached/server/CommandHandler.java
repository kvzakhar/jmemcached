package net.simplesoft.jmemcached.server;

import net.simplesoft.jmemcached.protocol.model.Request;
import net.simplesoft.jmemcached.protocol.model.Response;

public interface CommandHandler {

    Response handle(Request request);
}
