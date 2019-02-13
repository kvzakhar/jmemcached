package net.simplesoft.jmemcached.client.impl;

import net.simplesoft.jmemcached.client.ClientConfig;
import net.simplesoft.jmemcached.protocol.ObjectSerializer;
import net.simplesoft.jmemcached.protocol.RequestConverter;
import net.simplesoft.jmemcached.protocol.ResponseConverter;
import net.simplesoft.jmemcached.protocol.impl.DefaultObjectSerializer;
import net.simplesoft.jmemcached.protocol.impl.DefaultRequestConverter;
import net.simplesoft.jmemcached.protocol.impl.DefaultResponseConverter;

class DefaultClientConfig implements ClientConfig {

    private final String host;
    private final int port;
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;
    private final ObjectSerializer objectSerializer;

    DefaultClientConfig(String host, int port) {
        this.host = host;
        this.port = port;
        this.requestConverter = new DefaultRequestConverter();
        this.responseConverter = new DefaultResponseConverter();
        this.objectSerializer = new DefaultObjectSerializer();
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public RequestConverter getRequestConverter() {
        return this.requestConverter;
    }

    @Override
    public ResponseConverter getResponseConverter() {
        return this.responseConverter;
    }

    @Override
    public ObjectSerializer getObjectSerializer() {
        return this.objectSerializer;
    }
}