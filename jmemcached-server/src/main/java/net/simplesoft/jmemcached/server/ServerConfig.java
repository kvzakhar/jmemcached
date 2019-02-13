package net.simplesoft.jmemcached.server;

import net.simplesoft.jmemcached.protocol.RequestConverter;
import net.simplesoft.jmemcached.protocol.ResponseConverter;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

public interface ServerConfig extends AutoCloseable {

    RequestConverter getRequestConverter();

    ResponseConverter getResponseConverter();

    ThreadFactory getWorkerThreadFactory();

    Storage getStorage();

    CommandHandler getCommandHandler();

    int getClearDataIntervalInMills();

    int getServerPort();

    int getInitThreadCount();

    int getMaxThreadCount();

    ClientSocketHandler buildNewClientSocketHandler(Socket clientSocket);

}
