package net.simplesoft.jmemcached.protocol.impl;

import net.simplesoft.jmemcached.exception.JMemcachedException;
import net.simplesoft.jmemcached.protocol.model.Command;
import net.simplesoft.jmemcached.protocol.model.Request;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultRequestConverterTest {

    private final byte[] requestClear = {16, 0, 0};
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private DefaultRequestConverter defaultRequestConverter = new DefaultRequestConverter();

    @Test
    public void getFlagsByteEmpty() {
        Request request = new Request(Command.CLEAR);
        byte flags = defaultRequestConverter.getFlagsByte(request);
        assertEquals(0, flags);
    }

    @Test
    public void getFlagsByte() {
        Request request = new Request(Command.CLEAR, "key", System.currentTimeMillis(), new byte[]{1});
        byte flags = defaultRequestConverter.getFlagsByte(request);
        assertEquals(7, flags);
    }

    @Test
    public void writeKeySuccess() throws IOException {
        DataOutputStream dataOutputStream = spy(new DataOutputStream(mock(OutputStream.class)));
        String key = "key";
        defaultRequestConverter.writeKey(dataOutputStream, new Request(Command.PUT, key));

        verify(dataOutputStream).write(key.getBytes(StandardCharsets.US_ASCII));
        verify(dataOutputStream).writeByte(3);
    }

    @Test
    public void writeKeyFailed() throws IOException {
        String key = StringUtils.repeat("a", 128);
        thrown.expect(JMemcachedException.class);
        thrown.expectMessage(is("Key length should be <=127 bytes for key: " + key));
        DataOutputStream dataOutputStream = new DataOutputStream(null);
        defaultRequestConverter.writeKey(dataOutputStream, new Request(Command.PUT, key));

    }

    @Test
    public void readRequestWithoutDataSuccess() throws IOException {
        Request request = defaultRequestConverter.readRequest(new ByteArrayInputStream(requestClear));
        assertEquals(Command.CLEAR, request.getCommand());
        assertFalse(request.hasTTL());
        assertFalse(request.hasKey());
        assertFalse(request.hasData());
    }

    @Test
    public void readPutWithData() throws IOException {
        Request request = defaultRequestConverter.readRequest(new ByteArrayInputStream(new byte[]{
                16, 1, 7, 3, 49, 50, 51, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 3, 1, 2, 3
        }));
        assertEquals(Command.PUT, request.getCommand());
        assertTrue(request.hasKey());
        assertEquals("123", request.getKey());
        assertTrue(request.hasTTL());
        assertEquals(Long.valueOf(5), request.getTtl());
        assertTrue(request.hasData());
        assertArrayEquals(new byte[]{1, 2, 3}, request.getData());
    }

    @Test
    public void writeRequestWithoutData() throws IOException {
        Request request = new Request(Command.CLEAR);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        defaultRequestConverter.writeRequest(byteArrayOutputStream, request);
        assertArrayEquals(requestClear, byteArrayOutputStream.toByteArray());
    }

    @Test
    public void writeRequestWithData() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        defaultRequestConverter.writeRequest(byteArrayOutputStream, new Request(Command.PUT, "123", 5L, new byte[]{1, 2, 3}));
        assertArrayEquals(new byte[]{
                16, 1, 7, 3, 49, 50, 51, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 3, 1, 2, 3
        }, byteArrayOutputStream.toByteArray());
    }

}