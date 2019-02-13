package net.simplesoft.jmemcached.server.impl;

import net.simplesoft.jmemcached.exception.JMemcachedConfigException;
import net.simplesoft.jmemcached.protocol.RequestConverter;
import net.simplesoft.jmemcached.protocol.ResponseConverter;
import net.simplesoft.jmemcached.protocol.impl.DefaultRequestConverter;
import net.simplesoft.jmemcached.protocol.impl.DefaultResponseConverter;
import net.simplesoft.jmemcached.server.ClientSocketHandler;
import net.simplesoft.jmemcached.server.CommandHandler;
import net.simplesoft.jmemcached.server.ServerConfig;
import net.simplesoft.jmemcached.server.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;

public class DefaultServerConfig implements ServerConfig {

    private final Properties applicationProperties = new Properties();
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;
    private final Storage storage;
    private final CommandHandler commandHandler;

    DefaultServerConfig(Properties overrideApplicationProperties) {
        loadApplicationProperties("server.properties");
        if (overrideApplicationProperties != null) {
            this.applicationProperties.putAll(overrideApplicationProperties);
        }
        this.requestConverter = createRequestConverter();
        this.responseConverter = createResponseConverter();
        this.storage = createStorage(); // must be initialized before command handler
        this.commandHandler = createCommandHandler();
    }

    protected RequestConverter createRequestConverter() {
        return new DefaultRequestConverter();
    }

    protected ResponseConverter createResponseConverter() {
        return new DefaultResponseConverter();
    }

    protected Storage createStorage() {
        return new DefaultStorage(this);
    }

    protected CommandHandler createCommandHandler() {
        return new DefaultCommandHandler(this);
    }

    protected InputStream getClassPathResourceInputStream(String classPathResource) {
        return getClass().getClassLoader().getResourceAsStream(classPathResource);
    }

    protected void loadApplicationProperties(String classPathResource) {
        try (InputStream in = getClassPathResourceInputStream(classPathResource)) {
            if (in == null) {
                throw new JMemcachedConfigException("Classpath resource not found: " + classPathResource);
            } else {
                applicationProperties.load(in);
            }
        } catch (IOException e) {
            throw new JMemcachedConfigException("Can't load application properties from classpath:" + classPathResource, e);
        }
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
    public ThreadFactory getWorkerThreadFactory() {
        return new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "Worker-" + threadCount);
                threadCount++;
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    @Override
    public Storage getStorage() {
        return this.storage;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    @Override
    public int getClearDataIntervalInMills() {
        String value = applicationProperties.getProperty("jmemcached.storage.clear.data.interval.ms");
        try {
            int interval = Integer.parseInt(value);
            if (interval < 1000) {
                throw new JMemcachedConfigException("interval should be >= 1000");
            }
            return interval;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException("port should be a number", e);
        }
    }

    @Override
    public int getServerPort() {
        String value = applicationProperties.getProperty("jmemcached.server.port");
        try {
            int port = Integer.parseInt(value);
            if (port < 0 || port > 65535) {
                throw new JMemcachedConfigException("port should be between 0 and 65535");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException("port should be a number", e);
        }
    }

    protected int getThreadCount(String propertyName) {
        String value = applicationProperties.getProperty(propertyName);
        try {
            int threadCount = Integer.parseInt(value);
            if (threadCount < 1) {
                throw new JMemcachedConfigException(propertyName + " should be >= 1");
            }
            return threadCount;
        } catch (NumberFormatException e) {
            throw new JMemcachedConfigException(propertyName + " should be a number", e);
        }
    }

    @Override
    public int getInitThreadCount() {
        return getThreadCount("jmemcached.server.init.thread.count");
    }

    @Override
    public int getMaxThreadCount() {
        return getThreadCount("jmemcached.server.max.thread.count");
    }

    @Override
    public ClientSocketHandler buildNewClientSocketHandler(Socket clientSocket) {
        return new DefaultClientSocketHandler(clientSocket, this);
    }

    @Override
    public void close() throws Exception {
        storage.close();
    }

    @Override
    public String toString() {
        return String.format("DefaultServerConfig: port=%s, initThreadCount=%s, maxThreadCount=%s, clearDataIntervalInMs=%sms",
                getServerPort(), getInitThreadCount(), getMaxThreadCount(), getClearDataIntervalInMills());
    }
}
