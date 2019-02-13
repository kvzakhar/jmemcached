package net.simplesoft.jmemcached.server.impl;

import net.simplesoft.jmemcached.protocol.RequestConverter;
import net.simplesoft.jmemcached.protocol.ResponseConverter;
import net.simplesoft.jmemcached.protocol.model.Request;
import net.simplesoft.jmemcached.protocol.model.Response;
import net.simplesoft.jmemcached.server.ClientSocketHandler;
import net.simplesoft.jmemcached.server.CommandHandler;
import net.simplesoft.jmemcached.server.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class DefaultClientSocketHandler implements ClientSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClientSocketHandler.class);
    private final Socket socket;
    private final ServerConfig serverConfig;

    DefaultClientSocketHandler(Socket socket, ServerConfig serverConfig) {
        this.socket = socket;
        this.serverConfig = serverConfig;
    }

    protected boolean interrupted() {
        return Thread.interrupted();
    }

    @Override
    public void run() {
        try {
            RequestConverter requestConverter = this.serverConfig.getRequestConverter();
            ResponseConverter responseConverter = this.serverConfig.getResponseConverter();
            CommandHandler commandHandler = serverConfig.getCommandHandler();
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            while (!interrupted()) {
                try {
                    Request request = requestConverter.readRequest(inputStream);
                    Response response = commandHandler.handle(request);
                    responseConverter.writeResponse(outputStream, response);
                    LOGGER.debug("Command {} -> {}", request, response);
                } catch (RuntimeException e) {
                    LOGGER.error("Handle request failed: " + e.getMessage(), e);
                }

            }
        } catch (EOFException | SocketException e) {
            LOGGER.info("Remote client connection closed: "
                    + socket.getRemoteSocketAddress().toString() + ": " + e.getMessage());
        } catch (IOException e) {
            if (!socket.isClosed()) {
                LOGGER.error("IO Error: " + e.getMessage(), e);
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.error("Close socket failed: " + e.getMessage(), e);
            }
        }
    }
}
