package net.simplesoft.jmemcached.server.impl;

import net.simplesoft.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class ClearExpiredDataJobTest {

    private Logger logger;
    private DefaultStorage.ClearExpiredDataJob clearExpiredDataJob;
    private Map<String, DefaultStorage.StorageItem> map;
    private Set<Map.Entry<String, DefaultStorage.StorageItem>> set;
    private Iterator<Map.Entry<String, DefaultStorage.StorageItem>> iterator;
    private int clearDataIntervalInMs = 10000;

    @Before
    public void before() throws IllegalAccessException {
        logger = mock(Logger.class);
        map = mock(Map.class);
        set = mock(Set.class);
        when(map.entrySet()).thenReturn(set);
        iterator = mock(Iterator.class);
        when(set.iterator()).thenReturn(iterator);
        clearExpiredDataJob = spy(new DefaultStorage.ClearExpiredDataJob(map, clearDataIntervalInMs) {
            private boolean stop = true;

            @Override
            protected boolean interrupted() {
                stop = !stop;
                return stop;
            }

            @Override
            protected void sleepClearExpiredDataJob() throws InterruptedException {
                // do nothing
            }
        });
        TestUtils.setLoggerViaReflection(DefaultStorage.class, logger);
    }

    @Test
    public void verifyWhenMapIsEmpty() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(false);
        clearExpiredDataJob.run();

        verifyCommonOperations();
    }

    @Test
    public void verifyWhenMapEntryIsNotExpired() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        Map.Entry<String, DefaultStorage.StorageItem> entry = mock(Map.Entry.class);
        when(iterator.next()).thenReturn(entry);
        DefaultStorage.StorageItem storageItem = mock(DefaultStorage.StorageItem.class);
        when(entry.getValue()).thenReturn(storageItem);
        when(storageItem.isExpired()).thenReturn(false);

        clearExpiredDataJob.run();
        verifyCommonOperations();
        verify(map, never()).remove(anyString());
        verify(logger, never()).debug("removed expired storage item=" + storageItem);
    }

    @Test
    public void verifyWhenMapEntryIsExpired() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        Map.Entry<String, DefaultStorage.StorageItem> entry = mock(Map.Entry.class);
        when(iterator.next()).thenReturn(entry);
        DefaultStorage.StorageItem storageItem = mock(DefaultStorage.StorageItem.class);

        when(entry.getKey()).thenReturn("key");
        when(entry.getValue()).thenReturn(storageItem);
        when(storageItem.isExpired()).thenReturn(true);
        when(map.remove("key")).thenReturn(storageItem);

        clearExpiredDataJob.run();

        verifyCommonOperations();
        verify(map).remove(anyString());
        verify(logger).debug("removed expired storage item=" + storageItem);

    }

    @Test
    public void verifyWhenInterruptedException() throws InterruptedException {
        when(iterator.hasNext()).thenReturn(false);

        clearExpiredDataJob = spy(new DefaultStorage.ClearExpiredDataJob(map, clearDataIntervalInMs) {
            @Override
            protected void sleepClearExpiredDataJob() throws InterruptedException {
                throw new InterruptedException();
            }
        });

        clearExpiredDataJob.run();

        verify(logger).trace("Invoke clear job");
        verify(logger).debug("ClearExpiredDataJobThread started with interval {} ms", clearDataIntervalInMs);
        verify(clearExpiredDataJob).sleepClearExpiredDataJob();
        verify(clearExpiredDataJob, times(1)).interrupted();
    }

    private void verifyCommonOperations() throws InterruptedException {
        verify(logger).trace("Invoke clear job");
        verify(logger).debug("ClearExpiredDataJobThread started with interval {} ms", clearDataIntervalInMs);
        verify(clearExpiredDataJob).sleepClearExpiredDataJob();
        verify(clearExpiredDataJob, times(2)).interrupted();
    }

}