package net.simplesoft.jmemcached.server;

import net.simplesoft.jmemcached.protocol.model.Status;

public interface Storage extends AutoCloseable {

    Status put(String key, Long ttl, byte[] data);

    byte[] get(String key);

    Status remove(String key);

    Status clear();
}
