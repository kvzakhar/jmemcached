package net.simplesoft.jmemcached.protocol.impl;

import net.simplesoft.jmemcached.exception.JMemcachedException;
import net.simplesoft.jmemcached.protocol.RequestConverter;
import net.simplesoft.jmemcached.protocol.model.Command;
import net.simplesoft.jmemcached.protocol.model.Request;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DefaultRequestConverter extends AbstractPackageConverter implements RequestConverter {

    @Override
    public Request readRequest(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        checkProtocolVersion(dataInputStream.readByte());
        byte command = dataInputStream.readByte();
        byte flags = dataInputStream.readByte();
        boolean hasKey = (flags & 1) != 0;
        boolean hasTTL = (flags & 2) != 0;
        boolean hasData = (flags & 4) != 0;

        return readRequest(command, hasKey, hasTTL, hasData, dataInputStream);
    }

    protected Request readRequest(byte cmd, boolean hasKey, boolean hasTTL, boolean hasData, DataInputStream dis)
            throws IOException {
        Request request = new Request(Command.valueOf(cmd));
        if (hasKey) {
            byte keyLength = dis.readByte();
            byte[] keyBytes = IOUtils.readFully(dis, keyLength);
            request.setKey(new String(keyBytes, StandardCharsets.US_ASCII));
        }
        if (hasTTL) {
            request.setTtl(dis.readLong());
        }
        if (hasData) {
            int dataLength = dis.readInt();
            byte[] data = IOUtils.readFully(dis, dataLength);
            request.setData(data);
        }
        return request;
    }

    @Override
    public void writeRequest(OutputStream outputStream, Request request) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeByte(getVersionByte());
        dataOutputStream.writeByte(request.getCommand().getByteCode());
        dataOutputStream.writeByte(getFlagsByte(request));
        if (request.hasKey()) {
            writeKey(dataOutputStream, request);
        }
        if (request.hasTTL()) {
            dataOutputStream.writeLong(request.getTtl());
        }
        if (request.hasData()) {
            dataOutputStream.writeInt(request.getData().length);
            dataOutputStream.write(request.getData());
        }
        dataOutputStream.flush();
    }

    protected byte getFlagsByte(Request request) {
        byte flags = 0;
        if (request.hasKey()) {
            flags |= 1;
        }
        if (request.hasTTL()) {
            flags |= 2;
        }
        if (request.hasData()) {
            flags |= 4;
        }
        return flags;
    }

    protected void writeKey(DataOutputStream dataOutputStream, Request request) throws IOException {
        byte[] key = request.getKey().getBytes(StandardCharsets.US_ASCII);
        if (key.length > 127) {
            throw new JMemcachedException("Key length should be <=127 bytes for key: " + request.getKey());
        }
        dataOutputStream.writeByte(key.length);
        dataOutputStream.write(key);
    }
}
