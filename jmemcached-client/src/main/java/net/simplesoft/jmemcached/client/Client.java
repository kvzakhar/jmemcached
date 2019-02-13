package net.simplesoft.jmemcached.client;

import net.simplesoft.jmemcached.protocol.model.Status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface Client extends AutoCloseable {

    Status put(String key, Object object) throws IOException;

    Status put(String key, Object object, Integer ttl, TimeUnit timeUnit) throws IOException;

    <T> T get(String key) throws IOException;

    Status remove(String key) throws IOException;

    Status clear() throws IOException;

}

