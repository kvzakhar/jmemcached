package net.simplesoft.jmemcached.protocol.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AbstractPackageTest {

    private static AbstractPackage newInstance(byte[] array) {
        return new AbstractPackage(array) {
        };
    }

    @Test
    public void hasDataNull() {
        AbstractPackage abstractPackage = newInstance(null);
        assertFalse(abstractPackage.hasData());
    }

    @Test
    public void hasDataEmpty() {
        AbstractPackage abstractPackage = newInstance(new byte[0]);
        assertFalse(abstractPackage.hasData());
    }

    @Test
    public void hasData() {
        AbstractPackage abstractPackage = newInstance(new byte[]{1, 2, 3});
        assertTrue(abstractPackage.hasData());
    }

}