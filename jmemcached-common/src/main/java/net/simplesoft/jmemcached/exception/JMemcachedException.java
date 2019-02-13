package net.simplesoft.jmemcached.exception;

public class JMemcachedException extends RuntimeException {

    public JMemcachedException(String s) {
        super(s);
    }

    public JMemcachedException(Throwable throwable) {
        super(throwable);
    }

    public JMemcachedException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
