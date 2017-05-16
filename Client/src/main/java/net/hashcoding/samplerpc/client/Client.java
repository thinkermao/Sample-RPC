package net.hashcoding.samplerpc.client;

import net.hashcoding.samplerpc.common.Response;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public interface Client {
    Future<Boolean> addTargetService(Class<?> clazz);

    Response sendMessage(Class<?> clazz, Method method, Object[] args);

    <T> T getInterface(Class<T> clazz);

    void close();
}
