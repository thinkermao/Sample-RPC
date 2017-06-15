package net.hashcoding.simplerpc;

import java.util.concurrent.Future;

/**
 * Created by MaoChuan on 2017/5/13.
 */
public interface Registry {
    void start();

    Future<Boolean> startAsync();
    void shutdown();
}
