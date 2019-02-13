package net.simplesoft.jmemcached.client;

import net.simplesoft.jmemcached.protocol.ObjectSerializer;
import net.simplesoft.jmemcached.protocol.RequestConverter;
import net.simplesoft.jmemcached.protocol.ResponseConverter;

public interface ClientConfig {

    String getHost();

    int getPort();

    RequestConverter getRequestConverter();

    ResponseConverter getResponseConverter();

    ObjectSerializer getObjectSerializer();
}
