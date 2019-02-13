package net.simplesoft.jmemcached.server.impl;

import net.simplesoft.jmemcached.protocol.model.Status;
import net.simplesoft.jmemcached.server.ServerConfig;
import net.simplesoft.jmemcached.server.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

class DefaultStorage implements Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorage.class);

    protected final Map<String, StorageItem> map;
    protected final ExecutorService executorService;
    protected final Runnable clearExpiredDataJob;

    DefaultStorage(ServerConfig serverConfig) {
        int clearDataIntervalInMs = serverConfig.getClearDataIntervalInMills();
        this.map = this.createMap();
        this.executorService = this.createClearExpiredDataExecutorService();
        this.clearExpiredDataJob = new ClearExpiredDataJob(this.map, clearDataIntervalInMs);
        executorService.submit(clearExpiredDataJob);

    }

    protected Map<String, StorageItem> createMap() {
        return new ConcurrentHashMap<>();
    }

    protected ExecutorService createClearExpiredDataExecutorService() {
        return Executors.newSingleThreadExecutor(createClearExpiredDataThreadFactory());
    }

    protected ThreadFactory createClearExpiredDataThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread clearExpiredDataJobThread = new Thread(runnable, "ClearExpiredDataJobThread");
                clearExpiredDataJobThread.setPriority(Thread.MIN_PRIORITY);
                clearExpiredDataJobThread.setDaemon(true);
                return clearExpiredDataJobThread;
            }
        };
    }

    @Override
    public Status put(String key, Long ttl, byte[] data) {
        StorageItem oldItem = this.map.put(key, new StorageItem(key, ttl, data));
        return oldItem == null ? Status.ADDED : Status.REPLACED;
    }

    @Override
    public byte[] get(String key) {
        StorageItem item = this.map.get(key);
        if (item == null || item.isExpired()) {
            return null;
        }
        return item.data;
    }

    @Override
    public Status remove(String key) {
        StorageItem item = this.map.remove(key);
        if (item == null || item.isExpired()) {
            return Status.NOT_FOUND;
        }
        return Status.REMOVED;
    }

    @Override
    public Status clear() {
        this.map.clear();
        return Status.CLEARED;
    }

    @Override
    public void close() throws Exception {
        //Do nothing. daemon threads are destroyed automatically.
    }

    protected static class StorageItem {
        private final String key;
        private final byte[] data;
        private final Long ttl;

        protected StorageItem(String key, Long ttl, byte[] data) {
            this.key = key;
            this.data = data;
            this.ttl = (ttl != null) ? ttl + System.currentTimeMillis() : null;
        }

        protected boolean isExpired() {
            return ttl != null && ttl.longValue() < System.currentTimeMillis();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[").append(key).append("]=");
            if (data == null) {
                sb.append("null");
            } else {
                sb.append(data.length).append(" bytes");
            }
            if (ttl != null) {
                sb.append(" (").append(new Date(this.ttl.longValue())).append(')');
            }
            return sb.toString();
        }
    }

    protected static class ClearExpiredDataJob implements Runnable {

        private final Map<String, StorageItem> map;
        private final int clearDataIntervalInMs;

        public ClearExpiredDataJob(Map<String, StorageItem> map, int clearDataIntervalInMs) {
            this.map = map;
            this.clearDataIntervalInMs = clearDataIntervalInMs;
        }

        protected boolean interrupted() {
            return Thread.interrupted();
        }

        protected void sleepClearExpiredDataJob() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(this.clearDataIntervalInMs);
        }

        @Override
        public void run() {
            LOGGER.debug("ClearExpiredDataJobThread started with interval {} ms", clearDataIntervalInMs);
            while (!interrupted()) {
                LOGGER.trace("Invoke clear job");
                for (Map.Entry<String, StorageItem> entry : this.map.entrySet()) {
                    if (entry.getValue().isExpired()) {
                        StorageItem item = this.map.remove(entry.getKey());
                        LOGGER.debug("removed expired storage item=" + item);
                    }
                }

             /*   this.map.entrySet().stream()
                        .filter(e->e.getValue().isExpired())
                        .peek((item)->LOGGER.debug("removed expired storage item=" + item))
                        .forEach(this.map::remove);*/
                //    this.map.values().removeIf(StorageItem::isExpired);

                try {
                    sleepClearExpiredDataJob();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
