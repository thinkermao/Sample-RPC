package net.hashcoding.simplerpc.common;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by MaoChuan on 2017/5/14.
 */
public class Promise<V> {

    private FutureTask<V> task;
    private V value;
    private Exception exception;
    private boolean initialize;
    private Runnable runnable;

    public Promise() {
        task = new FutureTask<V>(() -> {
            if (exception != null)
                throw exception;
            return value;
        });
        initialize = false;
    }

    public Future<V> getFuture() {
        return task;
    }

    public void setValue(V value) {
        synchronized (this) {
            check();
            this.value = value;
            notifyFuture();
        }
    }

    public void setException(Exception exception) {
        synchronized (this) {
            check();
            this.exception = exception;
            notifyFuture();
        }
    }

    public void setFinallyListener(Runnable runnable) {
        synchronized (this) {
            this.runnable = runnable;
        }
    }

    private void notifyFuture() {
        task.run();
        if (this.runnable != null) {
            runnable.run();
        }
    }

    private void check() {
        if (initialize) {
            throw new RuntimeException("try set value twice");
        }
    }
}
