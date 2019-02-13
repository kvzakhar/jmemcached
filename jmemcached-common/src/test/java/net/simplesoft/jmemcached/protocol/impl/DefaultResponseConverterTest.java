package net.simplesoft.jmemcached.protocol.impl;

import net.simplesoft.jmemcached.protocol.model.Response;
import net.simplesoft.jmemcached.protocol.model.Status;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class DefaultResponseConverterTest {

    private final DefaultResponseConverter defaultResponseConverter = new DefaultResponseConverter();

    @Test
    public void readResponseWithoutData() throws IOException {
        Response response =
                defaultResponseConverter.readResponse(new ByteArrayInputStream(new byte[]{
                        //version status flag
                        16, 0, 0
                }));
        assertEquals(Status.ADDED, response.getStatus());
        assertFalse(response.hasData());
    }

    @Test
    public void readResponseWithData() throws IOException {
        Response response =
                defaultResponseConverter.readResponse(new ByteArrayInputStream(new byte[]{
                        //version status flag int length byte array
                        16, 0, 1, 0, 0, 0, 3, 1, 2, 3
                }));
        assertEquals(Status.ADDED, response.getStatus());
        assertTrue(response.hasData());
        assertArrayEquals(new byte[]{1, 2, 3}, response.getData());
    }

    @Test
    public void writeResponseWithoutData() throws IOException {
        Response response = new Response(Status.GOTTEN);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        defaultResponseConverter.writeResponse(byteArrayOutputStream, response);

        assertArrayEquals(new byte[]{16, 2, 0}, byteArrayOutputStream.toByteArray());
    }

    @Test
    public void writeResponseWithData() throws IOException {
        Response response = new Response(Status.ADDED, new byte[]{1, 2, 3});
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        defaultResponseConverter.writeResponse(byteArrayOutputStream, response);
        //version status flag length byte array
        assertArrayEquals(new byte[]{16, 0, 1, 0, 0, 0, 3, 1, 2, 3}, byteArrayOutputStream.toByteArray());
    }
}