package net.simplesoft.jmemcached.server.impl;

import net.simplesoft.jmemcached.exception.JMemcachedException;
import net.simplesoft.jmemcached.server.Server;
import net.simplesoft.jmemcached.server.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class DefaultServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServer.class);
    private final ServerConfig serverConfig;
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final Thread mainServerThread;
    private volatile boolean serverStopped;

    public DefaultServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.serverSocket = createServerSocket();
        this.executorService = createExecutorService();
        this.mainServerThread = createMainServerThread(createServerRunnable());
    }

    protected ServerSocket createServerSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.serverConfig.getServerPort());
            serverSocket.setReuseAddress(true);
            return serverSocket;
        } catch (IOException e) {
            throw new JMemcachedException(
                    "Can't create server socket with with port: " + this.serverConfig.getServerPort(), e);
        }
    }

    protected ExecutorService createExecutorService() {
        ThreadFactory threadFactory = this.serverConfig.getWorkerThreadFactory();
        int initThreadCount = this.serverConfig.getInitThreadCount();
        int maxThreadCount = this.serverConfig.getMaxThreadCount();

        return new ThreadPoolExecutor(
                initThreadCount,
                maxThreadCount,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    protected Thread createMainServerThread(Runnable r) {
        Thread thread = new Thread(r, "Main Server Thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(false);
        return thread;
    }

    protected Runnable createServerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                while (!mainServerThread.isInterrupted()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        try {
                            executorService.submit(serverConfig.buildNewClientSocketHandler(clientSocket));
                            LOGGER.info("A new client connection established: "
                                    + clientSocket.getRemoteSocketAddress().toString());
                        } catch (RejectedExecutionException e) {
                            LOGGER.error("All worker threads are busy. A new connection rejected: " + e.getMessage());
                            clientSocket.close();
                        }
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            LOGGER.error("Can't accept client socket: " + e.getMessage(), e);
                        }
                        destroyJMemCachedServer();
                        break;
                    }
                }
            }
        };
    }

    protected Thread getShutdownHook() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                if (!serverStopped) {
                    destroyJMemCachedServer();
                }
            }
        }, "ShutdownHook");
    }

    protected void destroyJMemCachedServer() {
        try {
            serverConfig.close();
        } catch (Exception e) {
            LOGGER.error("Close server config failed: " + e.getMessage(), e);
        }
        executorService.shutdownNow();
        LOGGER.info("Server stopped");
        serverStopped = true;
    }

    @Override
    public void start() {
        if (mainServerThread.getState() != Thread.State.NEW) {
            throw new JMemcachedException("Current JMemcached server already started or stopped! Please create a new server instance");
        }
        Runtime.getRuntime().addShutdownHook(getShutdownHook());
        mainServerThread.start();
        LOGGER.info("Server started: " + serverConfig);
    }

    @Override
    public void stop() {
        LOGGER.info("Detected stop cmd");
        mainServerThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.warn("Error during close server socket: " + e.getMessage(), e);
        }
    }
}
