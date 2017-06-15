package net.hashcoding.simplerpc.client;

import net.hashcoding.simplerpc.common.message.InvokeResponse;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public interface Client {
    Future<Boolean> addTargetService(Class<?> clazz);

    InvokeResponse sendMessage(Class<?> clazz, Method method, Object[] args);

    <T> T getInterface(Class<T> clazz);

    void close();
}
