package net.hashcoding.samplerpc.client;

/**
 * Created by MaoChuan on 2017/5/12.
 */
public interface Proxy {
    <T> T getInterface(Client client, final Class<T> serviceInterface);
}
