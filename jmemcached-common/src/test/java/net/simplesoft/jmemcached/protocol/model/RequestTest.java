package net.simplesoft.jmemcached.protocol.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestTest {

    private Request request;

    @Before
    public void before() {
        request = new Request(Command.CLEAR);
    }

    @Test
    public void hasKeyTrue() {
        request.setKey("key");
        assertTrue(request.hasKey());
    }

    @Test
    public void hasKeyFalse() {
        assertFalse(request.hasKey());
    }

    @Test
    public void hasTTLTrue() {
        request.setTtl(1L);
        assertTrue(request.hasTTL());
    }

    @Test
    public void hasTTLFalse() {
        assertFalse(request.hasTTL());
    }

    @Test
    public void toStringClear() {
        assertEquals("CLEAR", request.toString());
    }

    @Test
    public void toStringRemove() {
        request = new Request(Command.REMOVE);
        request.setKey("key");
        assertEquals("REMOVE[key]", request.toString());
    }

    @Test
    public void toStringPut() {
        request = new Request(Command.PUT);
        request.setKey("key");
        request.setData(new byte[]{1, 2, 3});
        assertEquals("PUT[key]=3 bytes", request.toString());
    }

    @Test
    public void toStringPutWithTTL() {
        request = new Request(Command.PUT);
        request.setKey("key");
        request.setTtl(1484166240528L);
        request.setData(new byte[]{1, 2, 3});
        assertEquals("PUT[key]=3 bytes (Wed Jan 11 23:24:00 MSK 2017)", request.toString());
    }

}